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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
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
 * Correctness proof for the {@code nodeTypes} key-mismatch fix in {@link
 * EvaluationVisitor#visitPropertyAccessWithReceiver}.
 *
 * <p><b>The bug:</b> {@link tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor#visitPropertyNav}
 * stores the combined Ctype for a navigation {@code e.p} keyed by the outer {@code
 * PropertyNavContext} ({@code nodeTypes.put(ctx, resultType)}). {@code
 * visitPropertyAccessWithReceiver} used to look the type back up keyed by the <em>inner</em>
 * {@code PropertyAccessContext} - a different parse tree node - so the lookup always missed and
 * silently fell back to {@code Type.set(Type.ANY)}, which is always {@code unique=true}. That made
 * {@code LazyOperations#dedupe} run unconditionally on every {@code e.p} navigation regardless of
 * the feature's real {@code unique} flag, silently dropping genuine duplicates from the result of
 * any many-valued, non-unique (Bag/Sequence) feature access. Order was never affected (dedupe does
 * not reorder), which is why no prior test caught this - none of them used duplicate-valued data.
 *
 * <p>{@code liste} here is a many-valued {@code EInt} attribute with {@code unique=false, ordered=
 * true} (Sequence semantics: duplicates allowed, order preserved), populated with real duplicates
 * ({@code [5, 3, 5, 3, 9]} - two distinct values repeated, one singleton, in a non-trivial order).
 */
class NavigationDedupeKeyMismatchTest {

  private static final EPackage DUMMY_PACKAGE;
  private static final EClass CONTAINER_CLASS;
  private static final EAttribute LISTE_FEATURE;

  static {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    DUMMY_PACKAGE = factory.createEPackage();
    DUMMY_PACKAGE.setName("dummy2");
    DUMMY_PACKAGE.setNsPrefix("dummy2");
    DUMMY_PACKAGE.setNsURI("http://test/dummy2");

    CONTAINER_CLASS = factory.createEClass();
    CONTAINER_CLASS.setName("Container");
    DUMMY_PACKAGE.getEClassifiers().add(CONTAINER_CLASS);

    LISTE_FEATURE = factory.createEAttribute();
    LISTE_FEATURE.setName("liste");
    LISTE_FEATURE.setEType(EcorePackage.Literals.EINT);
    LISTE_FEATURE.setUpperBound(-1); // many-valued
    LISTE_FEATURE.setOrdered(true);
    LISTE_FEATURE.setUnique(false); // Sequence: duplicates allowed, order preserved
    CONTAINER_CLASS.getEStructuralFeatures().add(LISTE_FEATURE);
  }

  /** A {@code dummy2::Container} instance whose {@code liste} feature holds real duplicates. */
  private static final class Container extends DynamicEObjectImpl {
    private final List<Integer> liste;

    Container(List<Integer> liste) {
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
        return "dummy2".equals(metamodelName) && "Container".equals(className)
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
        return Set.of("dummy2");
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

  private boolean evaluateInvariant(String constraint, MetamodelWrapperInterface wrapper) {
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

    assertTrue(result.size() == 1, "Expected exactly one invariant result");
    Boolean bool = result.getElements().get(0).tryGetBool();
    assertTrue(bool != null, "Expected a boolean invariant result");
    return bool;
  }

  @Test
  void navigationOnNonUniqueManyValuedFeaturePreservesDuplicateCount() {
    Container instance = new Container(List.of(5, 3, 5, 3, 9));

    // size() == 5 only holds if the two "5"s and two "3"s all survive; if dedupe wrongly ran
    // (the bug), the result would collapse to the 3 distinct values {5, 3, 9} and size() == 3.
    boolean result =
        evaluateInvariant(
            "context dummy2::Container inv: self.liste.size() == 5", wrapperFor(instance));

    assertTrue(result, "Expected self.liste.size() == 5 (duplicates preserved)");
  }

  @Test
  void navigationOnNonUniqueManyValuedFeaturePreservesElementOrder() {
    Container instance = new Container(List.of(5, 3, 5, 3, 9));

    // Checks every position individually (not just size) so a reordering, not just a dedup,
    // would also be caught. Deliberately checks index 3 ("5" again, not "3") and index 5 ("9",
    // the unique tail) so this cannot pass by coincidence of a partially-correct fix.
    boolean result =
        evaluateInvariant(
            "context dummy2::Container inv: "
                + "self.liste.at(1) == 5 and self.liste.at(2) == 3 and self.liste.at(3) == 5 "
                + "and self.liste.at(4) == 3 and self.liste.at(5) == 9",
            wrapperFor(instance));

    assertTrue(result, "Expected self.liste to preserve [5, 3, 5, 3, 9] in order");
  }
}
