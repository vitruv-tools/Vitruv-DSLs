/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.Multiplicity;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Tests for {@code visitIncludingOp}'s cardinality-bound computation (the χ case system for
 * {@code including()}): the element case (argument conforms to the declared element type) vs. the
 * collection case (argument conforms to the receiver's full collection type), and the
 * element-case-first priority used to resolve the (specification-underdetermined) overlap between
 * them.
 *
 * <p>{@code uniqueNums}/{@code bagNums} are {@code dummy4::Root} features with explicit EMF bounds
 * (lowerBound=2, upperBound=5) so the lower/upper-bound arithmetic is exercised with genuinely
 * non-default numbers, not just the (0, UNBOUNDED) defaults literal collections get.
 */
class IncludingBoundsTypeTest {

  private static final EClass ROOT_CLASS;

  static {
    EcoreFactory factory = EcoreFactory.eINSTANCE;
    EPackage pkg = factory.createEPackage();
    pkg.setName("dummy4");
    pkg.setNsPrefix("dummy4");
    pkg.setNsURI("http://test/dummy4");

    ROOT_CLASS = factory.createEClass();
    ROOT_CLASS.setName("Root");
    pkg.getEClassifiers().add(ROOT_CLASS);

    EAttribute uniqueNums = factory.createEAttribute();
    uniqueNums.setName("uniqueNums");
    uniqueNums.setEType(EcorePackage.Literals.EINT);
    uniqueNums.setLowerBound(2);
    uniqueNums.setUpperBound(5);
    uniqueNums.setOrdered(false);
    uniqueNums.setUnique(true);
    ROOT_CLASS.getEStructuralFeatures().add(uniqueNums);

    EAttribute bagNums = factory.createEAttribute();
    bagNums.setName("bagNums");
    bagNums.setEType(EcorePackage.Literals.EINT);
    bagNums.setLowerBound(2);
    bagNums.setUpperBound(5);
    bagNums.setOrdered(false);
    bagNums.setUnique(false);
    ROOT_CLASS.getEStructuralFeatures().add(bagNums);
  }

  private static final MetamodelWrapperInterface WRAPPER =
      new MetamodelWrapperInterface() {
        @Override
        public EClass resolveEClass(String metamodelName, String className) {
          return "dummy4".equals(metamodelName) && "Root".equals(className) ? ROOT_CLASS : null;
        }

        @Override
        public EClass resolveEClassByShortName(String shortName) {
          return "Root".equals(shortName) ? ROOT_CLASS : null;
        }

        @Override
        public List<EObject> getAllInstances(EClass eClass) {
          return List.of();
        }

        @Override
        public Set<String> getAvailableMetamodels() {
          return Set.of("dummy4");
        }

        @Override
        public String getInstanceNameByIndex(int index) {
          return null;
        }

        @Override
        public List<EObject> getAllRootObjects() {
          return List.of();
        }

        @Override
        public EObject getContextObjectByIndex(int index) {
          return null;
        }

        @Override
        public EEnum resolveEEnum(String enumName) {
          return null;
        }

        @Override
        public String getSourceFileForInstance(EObject instance) {
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

  /** Result of type-checking a snippet: whether it errored, and the including() call's type. */
  private record Outcome(boolean hasErrors, Type includingType) {}

  private Outcome typeCheck(ParseTree tree) {
    SymbolTable symbolTable = new SymbolTableImpl(WRAPPER);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    new SymbolTableBuilder(symbolTable, WRAPPER, errors, scopeAnnotator).visit(tree);
    assertFalse(errors.hasErrors(), "Symbol table build failed: " + errors.getErrors());

    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, WRAPPER, errors, scopeAnnotator);
    typeChecker.visit(tree);

    ParseTree includingNode = findIncludingOp(tree);
    assertNotNull(includingNode, "Expected an including() call in the parsed tree");

    return new Outcome(typeChecker.hasErrors(), typeChecker.getNodeTypes().get(includingNode));
  }

  private static ParseTree findIncludingOp(ParseTree node) {
    if (node instanceof VitruvOCLParser.IncludingOpContext) {
      return node;
    }
    for (int i = 0; i < node.getChildCount(); i++) {
      ParseTree found = findIncludingOp(node.getChild(i));
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /** Parses a standalone expression (no {@code context}/{@code self} needed). */
  private Outcome checkExpr(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    return typeCheck(parser.prefixedExpCS());
  }

  /** Parses a {@code context ... inv: ...} declaration, for expressions that need {@code self}. */
  private Outcome checkContext(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    VitruvOCLParser parser = new VitruvOCLParser(new CommonTokenStream(lexer));
    return typeCheck(parser.contextDeclCS());
  }

  // ══════════════════════════════════════════════════════════════
  // 4a) Both conditions hold at once → element case wins (first if/else-if branch)
  // ══════════════════════════════════════════════════════════════

  /**
   * {@code Sequence{5,6}.first()} has type {@code !Integer!} (Singleton). A Singleton conforms to
   * both the receiver's element type (Integer) and, via the Singleton-conforms-to-anything rule,
   * to the receiver's full collection type — so both the element case and the collection case
   * apply simultaneously here.
   *
   * <p>Note: for a Singleton argument, L2 = U2 = 1 always, which makes the collection-case
   * formula (L1+L2 / U1+U2) numerically coincide with the element-case formula (L1+1 / U1+1) —
   * so this test cannot distinguish the two cases by the resulting numbers alone. What it does
   * verify is the thing item 2 of the task actually cares about: no exclusivity/ambiguity check
   * rejects this overlap, and the first (element-case) branch runs to completion producing a
   * valid, non-ERROR result.
   */
  @Test
  void bothCasesApplicable_elementCaseRunsWithoutAmbiguityError() {
    Outcome o = checkExpr("Bag{1,2}.including(Sequence{5,6}.first())");
    assertFalse(o.hasErrors(), "Overlapping element/collection cases must not be rejected");
    assertEquals(Multiplicity.BAG, o.includingType().getMultiplicity());
    assertEquals(1, o.includingType().getLowerBound(), "non-unique element case: L1+1");
    assertEquals(Type.UNBOUNDED, o.includingType().getUpperBound());
  }

  // ══════════════════════════════════════════════════════════════
  // 4b) Element case: L computation for Un1=1 (unique) vs Un1=0 (non-unique)
  // ══════════════════════════════════════════════════════════════

  @Test
  void elementCase_uniqueReceiver_lowerBoundUnchanged() {
    // uniqueNums: Set{Integer}, [2,5]. 99 conforms to the element type only (not to the whole
    // Set), so this is the element case. Unique -> L_neu = L1 (99 might already be present).
    Outcome o = checkContext("context dummy4::Root inv: self.uniqueNums.including(99).notEmpty()");
    assertFalse(o.hasErrors());
    assertEquals(Multiplicity.SET, o.includingType().getMultiplicity());
    assertEquals(2, o.includingType().getLowerBound(), "unique element case: L_neu = L1");
    assertEquals(6, o.includingType().getUpperBound(), "U_neu = U1 + 1");
  }

  @Test
  void elementCase_nonUniqueReceiver_lowerBoundIncrements() {
    // bagNums: Bag{Integer}, [2,5]. Non-unique -> L_neu = L1 + 1 (the new element is always
    // added, duplicates or not).
    Outcome o = checkContext("context dummy4::Root inv: self.bagNums.including(99).notEmpty()");
    assertFalse(o.hasErrors());
    assertEquals(Multiplicity.BAG, o.includingType().getMultiplicity());
    assertEquals(3, o.includingType().getLowerBound(), "non-unique element case: L_neu = L1 + 1");
    assertEquals(6, o.includingType().getUpperBound(), "U_neu = U1 + 1");
  }

  // ══════════════════════════════════════════════════════════════
  // 4c) Collection case: L computation for Un1=1 (unique) vs Un1=0 (non-unique)
  // ══════════════════════════════════════════════════════════════

  @Test
  void collectionCase_uniqueReceiver_lowerBoundUnchanged() {
    // self.uniqueNums (Set{Integer}, [2,5]) conforms to the *whole* receiver type (not to the
    // scalar element type, since it isn't a Singleton) -> collection case.
    // Unique -> L_neu = L1 (the merged-in collection might fully overlap).
    Outcome o =
        checkContext(
            "context dummy4::Root inv: self.uniqueNums.including(self.uniqueNums).notEmpty()");
    assertFalse(o.hasErrors());
    assertEquals(Multiplicity.SET, o.includingType().getMultiplicity());
    assertEquals(2, o.includingType().getLowerBound(), "unique collection case: L_neu = L1");
    assertEquals(10, o.includingType().getUpperBound(), "U_neu = U1 + U2 = 5 + 5");
  }

  @Test
  void collectionCase_nonUniqueReceiver_lowerBoundsSum() {
    // Non-unique -> L_neu = L1 + L2 (no de-duplication possible).
    Outcome o =
        checkContext("context dummy4::Root inv: self.bagNums.including(self.bagNums).notEmpty()");
    assertFalse(o.hasErrors());
    assertEquals(Multiplicity.BAG, o.includingType().getMultiplicity());
    assertEquals(4, o.includingType().getLowerBound(), "non-unique collection case: L1 + L2");
    assertEquals(10, o.includingType().getUpperBound(), "U_neu = U1 + U2 = 5 + 5");
  }

  // ══════════════════════════════════════════════════════════════
  // 4d) Neither case applies → type error (receiver type kept for error recovery)
  // ══════════════════════════════════════════════════════════════

  @Test
  void neitherCaseApplies_stillReportsTypeError() {
    // "hello" conforms neither to Integer (element case) nor to Bag{Integer} (collection case).
    Outcome o = checkExpr("Bag{1,2}.including(\"hello\")");
    assertTrue(o.hasErrors(), "Expected a type error for an incompatible including() argument");
    // Matches the pre-existing recovery behavior: the receiver type is kept unchanged, not ERROR.
    assertEquals(Multiplicity.BAG, o.includingType().getMultiplicity());
    assertEquals(0, o.includingType().getLowerBound());
    assertEquals(Type.UNBOUNDED, o.includingType().getUpperBound());
  }
}
