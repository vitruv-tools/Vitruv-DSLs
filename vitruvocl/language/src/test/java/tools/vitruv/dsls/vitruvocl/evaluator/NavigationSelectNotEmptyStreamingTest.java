/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * End-to-end consumption-count verification for {@code self.liste.select(x | x > 2).notEmpty()}
 * through the <em>real</em> parser/type-checker/evaluator pipeline - not the internal {@code
 * LazyOperations} layer directly (see {@link tools.vitruv.dsls.vitruvocl.evaluator.lazy}'s tests
 * for that level).
 *
 * <h2>History (read before trusting the numbers)</h2>
 *
 * <p>This test originally found that navigation ({@code self.liste}) pulled all 10,000 raw
 * elements regardless of match position, because {@link EvaluationVisitor#navigateOne}'s
 * many-valued-feature branch used to eagerly copy the entire feature value into a fresh {@code
 * ArrayList} before {@code select()}/{@code notEmpty()} ever got a chance to run. That was fixed
 * by replacing the eager copy with {@link EvaluationVisitor.SkipNullMappingIterator}, a lazy,
 * lookahead-based mapping iterator over the feature's own {@code list.iterator()} - see the
 * "early match" test below for the now-genuine early-termination result.
 *
 * <p>A second, independent bug was found and fixed while verifying the above: {@code
 * visitPropertyAccessWithReceiver} used to look up the navigation's Ctype under the wrong parse
 * tree node ({@code nodeTypes.get(ctx)} with the inner {@code PropertyAccessContext}, while {@code
 * TypeCheckVisitor} stores it under the outer {@code PropertyNavContext}), always missing and
 * silently defaulting to {@code Type.set(Type.ANY)} - always {@code unique=true}. That made {@code
 * LazyOperations#dedupe} run on every navigation regardless of the feature's real {@code unique}
 * flag, both dropping genuine duplicates (see {@code NavigationDedupeKeyMismatchTest}) <em>and</em>
 * adding one extra lookahead layer that inflated the early-match pull count from 2 to 3. With both
 * fixes in place, the early-match count below is the fully-explained {@code 2}, not the naively
 * expected {@code 1} - see that test's comment for exactly where the two lookahead pulls come from.
 *
 * <p>This does not contradict the {@code lazy} package's Category 1 classification of navigation
 * as "fully lazy" - that classification is about not needing to visit more <em>receiver</em>
 * elements than necessary (e.g. {@code allObjects.select(...).collect(o | o.p)}, where {@code
 * allObjects} has many elements). It says nothing about the internal size of a single many-valued
 * feature access on one receiver element, and that case was never previously exercised by a
 * consumption-counting test - every prior lazy chain test used collection literals or a receiver
 * with multiple elements, never a single object's own many-valued feature.
 */
class NavigationSelectNotEmptyStreamingTest {

  private static final EPackage DUMMY_PACKAGE;
  private static final EClass CONTAINER_CLASS;
  private static final EAttribute LISTE_FEATURE;

  static {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    DUMMY_PACKAGE = factory.createEPackage();
    DUMMY_PACKAGE.setName("dummy");
    DUMMY_PACKAGE.setNsPrefix("dummy");
    DUMMY_PACKAGE.setNsURI("http://test/dummy");

    CONTAINER_CLASS = factory.createEClass();
    CONTAINER_CLASS.setName("Container");
    DUMMY_PACKAGE.getEClassifiers().add(CONTAINER_CLASS);

    LISTE_FEATURE = factory.createEAttribute();
    LISTE_FEATURE.setName("liste");
    LISTE_FEATURE.setEType(EcorePackage.Literals.EINT);
    LISTE_FEATURE.setUpperBound(-1); // many-valued
    LISTE_FEATURE.setOrdered(true);
    LISTE_FEATURE.setUnique(false); // avoid the incremental-dedupe lazy layer; not what's tested here
    CONTAINER_CLASS.getEStructuralFeatures().add(LISTE_FEATURE);
  }

  /**
   * A many-valued feature's raw backing list, instrumented to count how many elements are
   * actually pulled via {@link #iterator()}. This is the same "count real pulls, not logical
   * results" technique {@code LazyOperationsTest}'s {@code CountingSource} uses one layer up (on
   * {@code OCLElementSource} directly) - it could not be reused verbatim here because the
   * boundary this test needs to instrument is one layer further down: the raw {@code
   * java.util.List} that {@code EObject.eGet()} returns for a many-valued feature, which is what
   * {@link EvaluationVisitor#navigateOne} actually touches.
   */
  private static final class CountingIntFeatureList extends AbstractList<Object> {
    private final int size;
    private final int matchIndex;
    private final AtomicInteger pulls = new AtomicInteger(0);

    CountingIntFeatureList(int size, int matchIndex) {
      this.size = size;
      this.matchIndex = matchIndex;
    }

    int pullCount() {
      return pulls.get();
    }

    @Override
    public int size() {
      return size; // reports the fixed length without iterating - matches the EList contract
    }

    @Override
    public Object get(int index) {
      throw new UnsupportedOperationException("expected navigateOne() to use iterator(), not get(int)");
    }

    @Override
    public Iterator<Object> iterator() {
      return new Iterator<>() {
        private int i = 0;

        @Override
        public boolean hasNext() {
          return i < size;
        }

        @Override
        public Object next() {
          if (i >= size) {
            throw new NoSuchElementException();
          }
          pulls.incrementAndGet();
          int value = (i == matchIndex) ? 3 : 1; // only matchIndex satisfies x > 2
          i++;
          return value;
        }
      };
    }
  }

  /** A {@code dummy::Container} instance whose {@code liste} feature is the counting spy above. */
  private static final class CountingContainer extends DynamicEObjectImpl {
    private final CountingIntFeatureList liste;

    CountingContainer(CountingIntFeatureList liste) {
      super(CONTAINER_CLASS);
      this.liste = liste;
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
      if (feature == LISTE_FEATURE) {
        return liste;
      }
      return super.eGet(feature);
    }
  }

  private MetamodelWrapperInterface wrapperFor(EObject instance) {
    return new MetamodelWrapperInterface() {
      @Override
      public EClass resolveEClass(String metamodelName, String className) {
        return "dummy".equals(metamodelName) && "Container".equals(className)
            ? CONTAINER_CLASS
            : null;
      }

      @Override
      public List<EObject> getAllInstances(EClass eClass) {
        return eClass == CONTAINER_CLASS ? List.of(instance) : List.of();
      }

      @Override
      public org.eclipse.emf.ecore.EEnum resolveEEnum(String enumName) {
        return null;
      }

      @Override
      public Set<String> getAvailableMetamodels() {
        return Set.of("dummy");
      }

      @Override
      public String getInstanceNameByIndex(int index) {
        return null;
      }

      @Override
      public List<EObject> getAllRootObjects() {
        return List.of(instance);
      }

      @Override
      public EObject getContextObjectByIndex(int index) {
        return index == 0 ? instance : null;
      }

      @Override
      public EClass resolveEClassByShortName(String shortName) {
        return "Container".equals(shortName) ? CONTAINER_CLASS : null;
      }

      @Override
      public String getSourceFileForInstance(EObject inst) {
        return null;
      }

      @Override
      public Set<EObject> getCorrespondingObjects(EObject source) {
        return Set.of();
      }

      @Override
      public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
        return false;
      }
    };
  }

  private boolean evaluateNotEmptyResult(String constraint, MetamodelWrapperInterface wrapper) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(constraint));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.contextDeclCS();

    SymbolTable symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);
    if (errors.hasErrors()) {
      throw new AssertionError("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.visit(tree);
    if (typeChecker.hasErrors()) {
      StringBuilder sb = new StringBuilder("Pass 2 (Type checking) failed:");
      for (var e : typeChecker.getErrorCollector().getErrors()) {
        sb.append("\n  ").append(e.getMessage());
      }
      throw new AssertionError(sb.toString());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);
    if (evaluator.hasErrors()) {
      throw new AssertionError("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    // visitClassifierContextCS returns Value.of(allResults, Type.bag(Type.BOOLEAN)): one element
    // per invariant per instance. Here: exactly one instance, one invariant.
    assertEquals(1, result.size(), "Expected exactly one invariant result");
    Boolean bool = result.getElements().get(0).tryGetBool();
    assertTrue(bool != null, "Expected a boolean invariant result");
    return bool;
  }

  private static final String CONSTRAINT =
      "context dummy::Container inv: self.liste.select(x | x > 2).notEmpty()";

  @Test
  void earlyMatchPullsOnlyTwoElementsInsteadOfAllTenThousand() {
    CountingIntFeatureList liste = new CountingIntFeatureList(10_000, 0); // match at index 0
    CountingContainer instance = new CountingContainer(liste);

    boolean result = evaluateNotEmptyResult(CONSTRAINT, wrapperFor(instance));

    assertTrue(result, "Expected notEmpty() == true (index 0 satisfies x > 2)");
    // Verified via stack-trace instrumentation (not guessed) after the navigateOne() streaming fix
    // and the nodeTypes key-mismatch fix (which removed a spurious LazyOperations#dedupe layer -
    // see class javadoc), the count is 2, not the naively expected 1, from two independent,
    // one-element lookahead layers each contributing exactly one extra pull:
    //   pull #1 (index 0, the actual match): EvaluationVisitor.SkipNullMappingIterator's
    //     constructor runs its own advance() the moment FlatMapIterator first expands the {self}
    //     receiver element, to have hasNext() answerable without a further pull.
    //   pull #2 (index 1, never consumed downstream): when FlatMapIterator#next() extracts index
    //     0's already-cached value, SkipNullMappingIterator#next() immediately prefetches the NEXT
    //     raw element (its own advance()) to keep its own hasNext() cheap - mirroring
    //     LazyOperations.FilterIterator's identical one-ahead lookahead pattern used elsewhere in
    //     this codebase (see isUnique()/one()'s consumption-counting tests for the same +1).
    // Before the nodeTypes fix this was 3: the wrongly-applied dedupe() added its own FilterIterator
    // wrapping FlatMapIterator, contributing a THIRD, independent one-ahead lookahead pull (verified
    // via a stack trace showing LazyOperations.lambda$filterStateful$1 in the call chain). Removing
    // the spurious dedupe layer removed that third pull, dropping 3 -> 2.
    assertEquals(
        2,
        liste.pullCount(),
        "navigateOne() should now pull only as far as needed to find the first match, plus the "
            + "one-element lookahead inherent to both SkipNullMappingIterator and FilterIterator");
  }

  @Test
  void lateMatchStillPullsAllTenThousandControllingForATestHarnessThatNeverPullsMore() {
    CountingIntFeatureList liste = new CountingIntFeatureList(10_000, 9_999); // match at last index
    CountingContainer instance = new CountingContainer(liste);

    boolean result = evaluateNotEmptyResult(CONSTRAINT, wrapperFor(instance));

    assertTrue(result, "Expected notEmpty() == true (index 9999 satisfies x > 2)");
    // Unlike the early-match case, a match at the very last position genuinely requires seeing
    // every element first - no lookahead layer can shortcut that, lazy or not. This confirms the
    // "2" above really is an early-termination win tied to match position, not an artifact of a
    // test harness that happens to never pull more than 2 regardless of the data.
    assertEquals(10_000, liste.pullCount(), "A late match requires all 10,000 pulls, as expected");
  }
}
