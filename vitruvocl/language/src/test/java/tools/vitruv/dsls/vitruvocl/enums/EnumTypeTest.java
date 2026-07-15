/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: EEnum — Ecore Enumeration type.
 *
 * <p>EEnum is a subtype of EDataType in Ecore. Enum literals use the syntax {@code
 * EnumType::LITERAL} (e.g. {@code Status::ACTIVE}). In OCL# an enum literal evaluates to a
 * singleton ¡EnumType!.
 *
 * <p>Test metamodel defines:
 *
 * <pre>
 *   enum Status { ACTIVE, INACTIVE, PENDING }
 *   enum Direction { NORTH, SOUTH, EAST, WEST }
 * </pre>
 *
 * <p>Type matrix:
 *
 * <pre>
 * Operation                    | Result      | Valid?
 * ─────────────────────────────────────────────────────────────
 * Status::ACTIVE               | ¡Status!    | ✓ literal
 * Status::ACTIVE == Status::ACTIVE  | ¡Boolean! | ✓ same type
 * Status::ACTIVE == Status::INACTIVE| ¡Boolean! | ✓ (false)
 * Status::ACTIVE != Status::INACTIVE| ¡Boolean! | ✓ (true)
 * Status::ACTIVE == 1          | ERROR       | ✗ cross-type
 * Status::ACTIVE == "ACTIVE"   | ERROR       | ✗ cross-type
 * Status::ACTIVE < Status::INACTIVE | ERROR  | ✗ no ordering
 * Status::ACTIVE + 1           | ERROR       | ✗ no arithmetic
 * Status::ACTIVE and true      | ERROR       | ✗ no logical
 * not Status::ACTIVE           | ERROR       | ✗ no not
 * -Status::ACTIVE              | ERROR       | ✗ no unary minus
 * Set{Status::ACTIVE, Status::INACTIVE} | Set{Status} | ✓
 * Sequence{Status::ACTIVE}     | Sequence{Status} | ✓
 * Bag{Status::ACTIVE, Status::ACTIVE}   | Bag{Status}| ✓ (dups)
 * OrderedSet{Status::ACTIVE}   | OrdSet{Status}   | ✓
 * coll.includes(Status::ACTIVE)| ¡Boolean!   | ✓
 * coll.select(e | e == E::A)   | Set{Status} | ✓
 * coll.forAll(e | e != null)   | ¡Boolean!   | ✓
 * coll.count(Status::ACTIVE)   | ¡Integer!   | ✓
 * coll.size()                  | ¡Integer!   | ✓
 * Sequence{E::A}.first()       | ¡Status!    | ✓
 * Set{E::A}.first()            | ERROR       | ✗ unordered
 * coll.sortedBy(e | e)         | OrderedSet  | ✓
 * if true then E::A else E::A  | ¡Status!    | ✓
 * if true then E::A else E::B  | ¡Status!    | ✓ (same type)
 * if true then E::A else 1     | ERROR       | ✗ incompatible
 * let x = Status::ACTIVE in x == Status::ACTIVE | ¡Boolean! | ✓
 * Direction::NORTH == Status::ACTIVE | ERROR | ✗ different enums
 * </pre>
 */
@SuppressWarnings("java:S125")
class EnumTypeTest extends DummyTestSpecification {

  private static final org.eclipse.emf.ecore.EPackage STATUS_PACKAGE;
  private static final org.eclipse.emf.ecore.EEnum STATUS_ENUM;
  private static final org.eclipse.emf.ecore.EEnum DIRECTION_ENUM;

  static {
    org.eclipse.emf.ecore.EcoreFactory factory = org.eclipse.emf.ecore.EcoreFactory.eINSTANCE;
    STATUS_PACKAGE = factory.createEPackage();
    STATUS_PACKAGE.setName("Status");
    STATUS_PACKAGE.setNsPrefix("Status");
    STATUS_PACKAGE.setNsURI("http://test/Status");

    STATUS_ENUM = factory.createEEnum();
    STATUS_ENUM.setName("Status");
    for (String name : new String[] {"ACTIVE", "INACTIVE", "PENDING"}) {
      org.eclipse.emf.ecore.EEnumLiteral lit = factory.createEEnumLiteral();
      lit.setName(name);
      lit.setValue(STATUS_ENUM.getELiterals().size());
      STATUS_ENUM.getELiterals().add(lit);
    }
    STATUS_PACKAGE.getEClassifiers().add(STATUS_ENUM);

    DIRECTION_ENUM = factory.createEEnum();
    DIRECTION_ENUM.setName("Direction");
    for (String name : new String[] {"NORTH", "SOUTH", "EAST", "WEST"}) {
      org.eclipse.emf.ecore.EEnumLiteral lit = factory.createEEnumLiteral();
      lit.setName(name);
      lit.setValue(DIRECTION_ENUM.getELiterals().size());
      DIRECTION_ENUM.getELiterals().add(lit);
    }
    STATUS_PACKAGE.getEClassifiers().add(DIRECTION_ENUM);
  }

  @Override
  protected MetamodelWrapperInterface buildDummySpec() {
    return new MetamodelWrapperInterface() {
      @Override
      public EClass resolveEClass(String metamodel, String className) {
        return null;
      }

      @Override
      public List<EObject> getAllInstances(EClass eClass) {
        return List.of();
      }

      @Override
      public EClass resolveEClassByShortName(String s) {
        return null;
      }

      @Override
      public Set<String> getAvailableMetamodels() {
        return Set.of();
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
      public org.eclipse.emf.ecore.EEnum resolveEEnum(String enumName) {
        if ("Status".equals(enumName)) {
          return STATUS_ENUM;
        }
        if ("Direction".equals(enumName)) {
          return DIRECTION_ENUM;
        }
        return null;
      }

      @Override
      public String getSourceFileForInstance(EObject instance) {
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFileForInstance'");
      }

      @Override
      public Set<EObject> getCorrespondingObjects(EObject source) {
        throw new UnsupportedOperationException("Unimplemented method 'getCorrespondingObjects'");
      }

      @Override
      public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
        throw new UnsupportedOperationException("Unimplemented method 'correspondenceHasTag'");
      }
    };
  }

  protected void compileExpectError(String input) {
    ParseTree tree = parse(input);
    ErrorCollector errors = new ErrorCollector();
    var dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    SymbolTableBuilder stb = new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    stb.visit(tree);
    TypeCheckVisitor tc = new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    tc.visit(tree);
    assertTrue(tc.hasErrors() || errors.hasErrors(), "Expected type error for: " + input);
  }

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.expCS();
  }

  // NOTE: Enum literals require a metamodel context for the type checker
  // to resolve the enum type. These tests use the dummy spec which returns
  // null for resolveEClass(). Tests marked with [NEEDS_METAMODEL] require
  // the full pipeline with brakesystem.ecore or a dedicated enum ecore.
  // Tests WITHOUT that marker test enum-related ERROR cases which work
  // purely syntactically, and enum literal parsing which is grammar-level.

  // ══════════════════════════════════════════════════════════════
  // Enum literal: equality (==, !=)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumLiteralEqualsSameValue() {
    assertSingleBool(compile("Status::ACTIVE == Status::ACTIVE"), true);
  }

  @Test
  void testEnumLiteralEqualsDifferentValue() {
    // Status::ACTIVE == Status::INACTIVE → ¡Boolean! false
    Value result = compile("Status::ACTIVE == Status::INACTIVE");
    assertSingleBool(result, false);
  }

  @Test
  void testEnumLiteralNotEqualsDifferentValue() {
    // Status::ACTIVE != Status::INACTIVE → ¡Boolean! true
    Value result = compile("Status::ACTIVE != Status::INACTIVE");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumLiteralNotEqualsSameValue() {
    // Status::ACTIVE != Status::ACTIVE → ¡Boolean! false
    Value result = compile("Status::ACTIVE != Status::ACTIVE");
    assertSingleBool(result, false);
  }

  @Test
  void testEnumThreeDistinctValues() {
    // Status::PENDING != Status::ACTIVE and Status::PENDING != Status::INACTIVE
    Value result =
        compile("Status::PENDING != Status::ACTIVE and Status::PENDING != Status::INACTIVE");
    assertSingleBool(result, true);
  }

  // ══════════════════════════════════════════════════════════════
  // Enum equality: ERROR cases (cross-type)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumEqualsIntegerFails() {
    // Status::ACTIVE == 1 → ERROR (enum ≠ integer)
    compileExpectError("Status::ACTIVE == 1");
  }

  @Test
  void testEnumEqualsStringFails() {
    // Status::ACTIVE == "ACTIVE" → ERROR (enum ≠ string)
    compileExpectError("Status::ACTIVE == \"ACTIVE\"");
  }

  @Test
  void testEnumEqualsBooleanFails() {
    // Status::ACTIVE == true → ERROR
    compileExpectError("Status::ACTIVE == true");
  }

  @Test
  void testEnumEqualsDoubleFails() {
    // Status::ACTIVE == 1.0 → ERROR
    compileExpectError("Status::ACTIVE == 1.0");
  }

  @Test
  void testDifferentEnumTypeComparisonFails() {
    // Direction::NORTH == Status::ACTIVE → ERROR (different enum types)
    compileExpectError("Direction::NORTH == Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: arithmetic → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumPlusIntegerFails() {
    compileExpectError("Status::ACTIVE + 1");
  }

  @Test
  void testEnumMinusIntegerFails() {
    compileExpectError("Status::ACTIVE - 1");
  }

  @Test
  void testEnumTimesIntegerFails() {
    compileExpectError("Status::ACTIVE * 2");
  }

  @Test
  void testEnumDividesIntegerFails() {
    compileExpectError("Status::ACTIVE / 1");
  }

  @Test
  void testIntegerPlusEnumFails() {
    compileExpectError("1 + Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: ordering → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumLessThanEnumFails() {
    compileExpectError("Status::ACTIVE < Status::INACTIVE");
  }

  @Test
  void testEnumLessOrEqualEnumFails() {
    compileExpectError("Status::ACTIVE <= Status::INACTIVE");
  }

  @Test
  void testEnumGreaterThanEnumFails() {
    compileExpectError("Status::ACTIVE > Status::INACTIVE");
  }

  @Test
  void testEnumGreaterOrEqualEnumFails() {
    compileExpectError("Status::ACTIVE >= Status::INACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: logical → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumAndBooleanFails() {
    compileExpectError("Status::ACTIVE and true");
  }

  @Test
  void testEnumOrBooleanFails() {
    compileExpectError("Status::ACTIVE or false");
  }

  @Test
  void testBooleanAndEnumFails() {
    compileExpectError("true and Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: unary → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  void testNotEnumFails() {
    // not Status::ACTIVE → ERROR (not a Boolean)
    compileExpectError("not Status::ACTIVE");
  }

  @Test
  void testUnaryMinusEnumFails() {
    // -Status::ACTIVE → ERROR (not numeric)
    compileExpectError("-Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collection literals — all 4 kinds
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetOfEnumLiterals() {
    // Set{Status::ACTIVE, Status::INACTIVE} → Set{¡Status!}
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}");
    assertEquals(2, result.size());
  }

  @Test
  void testSetOfEnumDeduplication() {
    // Set deduplicates: Set{Status::ACTIVE, Status::ACTIVE} → 1 element
    Value result = compile("Set{Status::ACTIVE, Status::ACTIVE}");
    assertEquals(1, result.size());
  }

  @Test
  void testSequenceOfEnumLiterals() {
    // Sequence preserves order and duplicates
    Value result = compile("Sequence{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}");
    assertEquals(3, result.size());
  }

  @Test
  void testSequenceOfEnumPreservesOrder() {
    Value result = compile("Sequence{Status::ACTIVE, Status::INACTIVE}");
    assertEquals(2, result.size());
    // First element should be ACTIVE
    OCLElement first = result.getElements().get(0);
    assertTrue(
        first instanceof OCLElement.EnumValue,
        "Expected EnumValue but got: " + first.getClass().getSimpleName());
    assertEquals("ACTIVE", ((OCLElement.EnumValue) first).literal().getName());
  }

  @Test
  void testBagOfEnumKeepsDuplicates() {
    // Bag: duplicates kept
    Value result = compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}");
    assertEquals(3, result.size());
  }

  @Test
  void testOrderedSetOfEnumDeduplicates() {
    // OrderedSet: unique + ordered
    Value result = compile("OrderedSet{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}");
    assertEquals(2, result.size());
  }

  @Test
  void testOrderedSetOfEnumPreservesOrder() {
    Value result = compile("OrderedSet{Status::INACTIVE, Status::ACTIVE}");
    assertEquals(2, result.size());
    OCLElement first = result.getElements().get(0);
    assertEquals("INACTIVE", ((OCLElement.EnumValue) first).literal().getName());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collection operations
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumSetIncludes() {
    // Set{Status::ACTIVE}.includes(Status::ACTIVE) → true
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.includes(Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumSetIncludesNotFound() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.includes(Status::PENDING)");
    assertSingleBool(result, false);
  }

  @Test
  void testEnumSetExcludes() {
    Value result = compile("Set{Status::ACTIVE}.excludes(Status::INACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumSetSize() {
    assertSingleInt(compile("Set{Status::ACTIVE, Status::INACTIVE}.size()"), 2);
  }

  @Test
  void testEnumSequenceSize() {
    assertSingleInt(
        compile("Sequence{Status::ACTIVE, Status::INACTIVE, Status::PENDING}.size()"), 3);
  }

  @Test
  void testEnumBagSize() {
    assertSingleInt(compile("Bag{Status::ACTIVE, Status::ACTIVE}.size()"), 2);
  }

  @Test
  void testEnumSetIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  @Test
  void testEnumSetNotEmpty() {
    assertSingleBool(compile("Set{Status::ACTIVE}.notEmpty()"), true);
  }

  @Test
  void testEnumCountInBag() {
    // Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.count(Status::ACTIVE) == 2
    assertSingleInt(
        compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.count(Status::ACTIVE)"), 2);
  }

  @Test
  void testEnumCountNotPresent() {
    assertSingleInt(compile("Set{Status::ACTIVE}.count(Status::INACTIVE)"), 0);
  }

  @Test
  void testEnumSequenceFirst() {
    // Sequence{Status::INACTIVE, Status::ACTIVE}.first() → ¡Status!
    Value result = compile("Sequence{Status::INACTIVE, Status::ACTIVE}.first()");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSequenceLast() {
    Value result = compile("Sequence{Status::INACTIVE, Status::ACTIVE}.last()");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSetFirstFails() {
    compileExpectError("Set{Status::ACTIVE, Status::INACTIVE}.first()");
  }

  @Test
  void testEnumBagLastFails() {
    compileExpectError("Bag{Status::ACTIVE}.last()");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: iterator operations
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumSelect() {
    // select active values only
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSelectNone() {
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == Status::PENDING)");
    assertEquals(0, result.size());
  }

  @Test
  void testEnumReject() {
    Value result =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::PENDING}"
                + ".reject(e | e == Status::ACTIVE)");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumForAllSameType() {
    // All elements are Status enums → true
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.forAll(e | e != Status::PENDING)");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumForAllFalse() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.forAll(e | e == Status::ACTIVE)");
    assertSingleBool(result, false);
  }

  @Test
  void testEnumExists() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.exists(e | e == Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumExistsFalse() {
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.exists(e | e == Status::PENDING)");
    assertSingleBool(result, false);
  }

  @Test
  void testEnumOne() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.one(e | e == Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  void testEnumSortedBy() {
    // sortedBy on enum collection → OrderedSet
    Value result = compile("Set{Status::INACTIVE, Status::ACTIVE}.sortedBy(e | e)");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumAny() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.any(e | e == Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumIsUnique() {
    // All values in Set are unique by definition
    assertSingleBool(compile("Set{Status::ACTIVE, Status::INACTIVE}.isUnique(e | e)"), true);
  }

  @Test
  void testEnumIsUniqueOnBagFalse() {
    // Bag with duplicates → not unique
    assertSingleBool(compile("Bag{Status::ACTIVE, Status::ACTIVE}.isUnique(e | e)"), false);
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: if-then-else
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumIfThenElseSameType() {
    // if true then Status::ACTIVE else Status::INACTIVE → ¡Status!
    Value result = compile("if true then Status::ACTIVE else Status::INACTIVE endif");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumIfThenElseFalseBranch() {
    Value result = compile("if false then Status::ACTIVE else Status::INACTIVE endif");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumIfThenElseInCondition() {
    // if (e == Status::ACTIVE) then 1 else 0 — enum in condition
    Value result = compile("let e = Status::ACTIVE in if e == Status::ACTIVE then 1 else 0 endif");
    assertSingleInt(result, 1);
  }

  @Test
  void testEnumIfThenElseMixedTypesFails() {
    // if true then Status::ACTIVE else 1 → ERROR (incompatible branches)
    compileExpectError("if true then Status::ACTIVE else 1 endif");
  }

  @Test
  void testEnumIfThenElseDifferentEnumsFails() {
    // if true then Status::ACTIVE else Direction::NORTH → ERROR
    compileExpectError("if true then Status::ACTIVE else Direction::NORTH endif");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: let-in
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumLetBinding() {
    // let x = Status::ACTIVE in x == Status::ACTIVE → true
    assertSingleBool(compile("let x = Status::ACTIVE in x == Status::ACTIVE"), true);
  }

  @Test
  void testEnumLetBindingNotEquals() {
    assertSingleBool(compile("let x = Status::ACTIVE in x != Status::INACTIVE"), true);
  }

  @Test
  void testEnumLetBindingInCollectionOp() {
    Value result =
        compile(
            "let active = Status::ACTIVE in "
                + "Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == active)");
    assertEquals(1, result.size());
  }

  @Test
  void testEnumLetInIfThenElse() {
    assertSingleBool(
        compile(
            "let s = Status::PENDING in " + "if s == Status::ACTIVE then true else false endif"),
        false);
  }

  @Test
  void testEnumLetDeclaredTypeMismatchFails() {
    // let x : Integer = Status::ACTIVE → ERROR
    compileExpectError("let x : Integer = Status::ACTIVE in x");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: including / excluding
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumSetIncluding() {
    Value result = compile("Set{Status::ACTIVE}.including(Status::INACTIVE)");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumSetExcluding() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.excluding(Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSequencePrepend() {
    Value result = compile("Sequence{Status::INACTIVE}.prepend(Status::ACTIVE)");
    assertEquals(2, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSequenceAppend() {
    Value result = compile("Sequence{Status::ACTIVE}.append(Status::INACTIVE)");
    assertEquals(2, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(1)).literal().getName());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: union / intersection
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumSetUnion() {
    Value result = compile("Set{Status::ACTIVE}.union(Set{Status::INACTIVE})");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumSetUnionDedup() {
    Value result = compile("Set{Status::ACTIVE}.union(Set{Status::ACTIVE, Status::INACTIVE})");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumSetIntersection() {
    Value result =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE}"
                + ".intersection(Set{Status::INACTIVE, Status::PENDING})");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSetSymmetricDifference() {
    Value result =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE}"
                + ".symmetricDifference(Set{Status::INACTIVE, Status::PENDING})");
    assertEquals(2, result.size()); // ACTIVE + PENDING
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: asSet / asBag / asSequence / asOrderedSet
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumBagAsSet() {
    // Bag{ACTIVE, ACTIVE, INACTIVE}.asSet() → Set{ACTIVE, INACTIVE}
    Value result = compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.asSet()");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumSetAsSequence() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.asSequence()");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumSequenceAsOrderedSet() {
    // Sequence{ACTIVE, ACTIVE, INACTIVE}.asOrderedSet() deduplicates
    Value result =
        compile("Sequence{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.asOrderedSet()");
    assertEquals(2, result.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collect
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumCollect() {
    // collect returns same enum values mapped through identity
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.collect(e | e)");
    assertEquals(2, result.size());
  }

  @Test
  void testEnumCollectToBoolean() {
    // collect(e | e == Status::ACTIVE) → Set{true, false}
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.collect(e | e == Status::ACTIVE)");
    assertEquals(2, result.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: chaining
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEnumSelectThenSize() {
    assertSingleInt(
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::PENDING}"
                + ".select(e | e == Status::ACTIVE).size()"),
        1);
  }

  @Test
  void testEnumSelectThenFirst() {
    Value result =
        compile(
            "Sequence{Status::INACTIVE, Status::ACTIVE}"
                + ".select(e | e == Status::INACTIVE).first()");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  void testEnumSetSelectThenFirst() {
    // select on Set → Set (unordered) → need asSequence() before first()
    Value r =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE}.select(e | e =="
                + " Status::ACTIVE).asSequence().first()");
    assertEquals(1, r.size());
  }

  @Test
  void testEnumSortedByThenFirst() {
    Value result = compile("Set{Status::INACTIVE, Status::ACTIVE}.sortedBy(e | e).first()");
    assertEquals(1, result.size());
  }

  @Test
  void testEnumIterateSum() {
    // iterate to count active elements
    assertSingleInt(
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}.iterate(e; acc : Integer = 0 |"
                + " if e == Status::ACTIVE then acc + 1 else acc endif)"),
        1); // Set deduplicates, so only 1 ACTIVE
  }
}
