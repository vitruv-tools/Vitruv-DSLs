/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.enums;

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
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: EEnum — Ecore Enumeration type
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
public class EnumTypeTest extends DummyTestSpecification {

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
        if ("Status".equals(enumName)) return STATUS_ENUM;
        if ("Direction".equals(enumName)) return DIRECTION_ENUM;
        return null;
      }

      @Override
      public String getSourceFileForInstance(EObject instance) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFileForInstance'");
      }

      @Override
      public Set<EObject> getCorrespondingObjects(EObject source) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCorrespondingObjects'");
      }

      @Override
      public boolean correspondenceHasTag(EObject obj1, EObject obj2, String tag) {
        // TODO Auto-generated method stub
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
  public void testEnumLiteralEqualsSameValue() {
    assertSingleBool(compile("Status::ACTIVE == Status::ACTIVE"), true);
  }

  @Test
  public void testEnumLiteralEqualsDifferentValue() {
    // Status::ACTIVE == Status::INACTIVE → ¡Boolean! false
    Value result = compile("Status::ACTIVE == Status::INACTIVE");
    assertSingleBool(result, false);
  }

  @Test
  public void testEnumLiteralNotEqualsDifferentValue() {
    // Status::ACTIVE != Status::INACTIVE → ¡Boolean! true
    Value result = compile("Status::ACTIVE != Status::INACTIVE");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumLiteralNotEqualsSameValue() {
    // Status::ACTIVE != Status::ACTIVE → ¡Boolean! false
    Value result = compile("Status::ACTIVE != Status::ACTIVE");
    assertSingleBool(result, false);
  }

  @Test
  public void testEnumThreeDistinctValues() {
    // Status::PENDING != Status::ACTIVE and Status::PENDING != Status::INACTIVE
    Value result =
        compile("Status::PENDING != Status::ACTIVE and Status::PENDING != Status::INACTIVE");
    assertSingleBool(result, true);
  }

  // ══════════════════════════════════════════════════════════════
  // Enum equality: ERROR cases (cross-type)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumEqualsIntegerFails() {
    // Status::ACTIVE == 1 → ERROR (enum ≠ integer)
    compileExpectError("Status::ACTIVE == 1");
  }

  @Test
  public void testEnumEqualsStringFails() {
    // Status::ACTIVE == "ACTIVE" → ERROR (enum ≠ string)
    compileExpectError("Status::ACTIVE == \"ACTIVE\"");
  }

  @Test
  public void testEnumEqualsBooleanFails() {
    // Status::ACTIVE == true → ERROR
    compileExpectError("Status::ACTIVE == true");
  }

  @Test
  public void testEnumEqualsDoubleFails() {
    // Status::ACTIVE == 1.0 → ERROR
    compileExpectError("Status::ACTIVE == 1.0");
  }

  @Test
  public void testDifferentEnumTypeComparisonFails() {
    // Direction::NORTH == Status::ACTIVE → ERROR (different enum types)
    compileExpectError("Direction::NORTH == Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: arithmetic → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumPlusIntegerFails() {
    compileExpectError("Status::ACTIVE + 1");
  }

  @Test
  public void testEnumMinusIntegerFails() {
    compileExpectError("Status::ACTIVE - 1");
  }

  @Test
  public void testEnumTimesIntegerFails() {
    compileExpectError("Status::ACTIVE * 2");
  }

  @Test
  public void testEnumDividesIntegerFails() {
    compileExpectError("Status::ACTIVE / 1");
  }

  @Test
  public void testIntegerPlusEnumFails() {
    compileExpectError("1 + Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: ordering → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumLessThanEnumFails() {
    compileExpectError("Status::ACTIVE < Status::INACTIVE");
  }

  @Test
  public void testEnumLessOrEqualEnumFails() {
    compileExpectError("Status::ACTIVE <= Status::INACTIVE");
  }

  @Test
  public void testEnumGreaterThanEnumFails() {
    compileExpectError("Status::ACTIVE > Status::INACTIVE");
  }

  @Test
  public void testEnumGreaterOrEqualEnumFails() {
    compileExpectError("Status::ACTIVE >= Status::INACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: logical → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumAndBooleanFails() {
    compileExpectError("Status::ACTIVE and true");
  }

  @Test
  public void testEnumOrBooleanFails() {
    compileExpectError("Status::ACTIVE or false");
  }

  @Test
  public void testBooleanAndEnumFails() {
    compileExpectError("true and Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: unary → ERROR
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testNotEnumFails() {
    // not Status::ACTIVE → ERROR (not a Boolean)
    compileExpectError("not Status::ACTIVE");
  }

  @Test
  public void testUnaryMinusEnumFails() {
    // -Status::ACTIVE → ERROR (not numeric)
    compileExpectError("-Status::ACTIVE");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collection literals — all 4 kinds
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetOfEnumLiterals() {
    // Set{Status::ACTIVE, Status::INACTIVE} → Set{¡Status!}
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}");
    assertEquals(2, result.size());
  }

  @Test
  public void testSetOfEnumDeduplication() {
    // Set deduplicates: Set{Status::ACTIVE, Status::ACTIVE} → 1 element
    Value result = compile("Set{Status::ACTIVE, Status::ACTIVE}");
    assertEquals(1, result.size());
  }

  @Test
  public void testSequenceOfEnumLiterals() {
    // Sequence preserves order and duplicates
    Value result = compile("Sequence{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}");
    assertEquals(3, result.size());
  }

  @Test
  public void testSequenceOfEnumPreservesOrder() {
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
  public void testBagOfEnumKeepsDuplicates() {
    // Bag: duplicates kept
    Value result = compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}");
    assertEquals(3, result.size());
  }

  @Test
  public void testOrderedSetOfEnumDeduplicates() {
    // OrderedSet: unique + ordered
    Value result = compile("OrderedSet{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}");
    assertEquals(2, result.size());
  }

  @Test
  public void testOrderedSetOfEnumPreservesOrder() {
    Value result = compile("OrderedSet{Status::INACTIVE, Status::ACTIVE}");
    assertEquals(2, result.size());
    OCLElement first = result.getElements().get(0);
    assertEquals("INACTIVE", ((OCLElement.EnumValue) first).literal().getName());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collection operations
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumSetIncludes() {
    // Set{Status::ACTIVE}.includes(Status::ACTIVE) → true
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.includes(Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumSetIncludesNotFound() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.includes(Status::PENDING)");
    assertSingleBool(result, false);
  }

  @Test
  public void testEnumSetExcludes() {
    Value result = compile("Set{Status::ACTIVE}.excludes(Status::INACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumSetSize() {
    assertSingleInt(compile("Set{Status::ACTIVE, Status::INACTIVE}.size()"), 2);
  }

  @Test
  public void testEnumSequenceSize() {
    assertSingleInt(
        compile("Sequence{Status::ACTIVE, Status::INACTIVE, Status::PENDING}.size()"), 3);
  }

  @Test
  public void testEnumBagSize() {
    assertSingleInt(compile("Bag{Status::ACTIVE, Status::ACTIVE}.size()"), 2);
  }

  @Test
  public void testEnumSetIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  @Test
  public void testEnumSetNotEmpty() {
    assertSingleBool(compile("Set{Status::ACTIVE}.notEmpty()"), true);
  }

  @Test
  public void testEnumCountInBag() {
    // Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.count(Status::ACTIVE) == 2
    assertSingleInt(
        compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.count(Status::ACTIVE)"), 2);
  }

  @Test
  public void testEnumCountNotPresent() {
    assertSingleInt(compile("Set{Status::ACTIVE}.count(Status::INACTIVE)"), 0);
  }

  @Test
  public void testEnumSequenceFirst() {
    // Sequence{Status::INACTIVE, Status::ACTIVE}.first() → ¡Status!
    Value result = compile("Sequence{Status::INACTIVE, Status::ACTIVE}.first()");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSequenceLast() {
    Value result = compile("Sequence{Status::INACTIVE, Status::ACTIVE}.last()");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSetFirstFails() {
    compileExpectError("Set{Status::ACTIVE, Status::INACTIVE}.first()");
  }

  @Test
  public void testEnumBagLastFails() {
    compileExpectError("Bag{Status::ACTIVE}.last()");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: iterator operations
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumSelect() {
    // select active values only
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSelectNone() {
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == Status::PENDING)");
    assertEquals(0, result.size());
  }

  @Test
  public void testEnumReject() {
    Value result =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::PENDING}"
                + ".reject(e | e == Status::ACTIVE)");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumForAllSameType() {
    // All elements are Status enums → true
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.forAll(e | e != Status::PENDING)");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumForAllFalse() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.forAll(e | e == Status::ACTIVE)");
    assertSingleBool(result, false);
  }

  @Test
  public void testEnumExists() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.exists(e | e == Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumExistsFalse() {
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.exists(e | e == Status::PENDING)");
    assertSingleBool(result, false);
  }

  @Test
  public void testEnumOne() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.one(e | e == Status::ACTIVE)");
    assertSingleBool(result, true);
  }

  @Test
  public void testEnumSortedBy() {
    // sortedBy on enum collection → OrderedSet
    Value result = compile("Set{Status::INACTIVE, Status::ACTIVE}.sortedBy(e | e)");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumAny() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.any(e | e == Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumIsUnique() {
    // All values in Set are unique by definition
    assertSingleBool(compile("Set{Status::ACTIVE, Status::INACTIVE}.isUnique(e | e)"), true);
  }

  @Test
  public void testEnumIsUniqueOnBagFalse() {
    // Bag with duplicates → not unique
    assertSingleBool(compile("Bag{Status::ACTIVE, Status::ACTIVE}.isUnique(e | e)"), false);
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: if-then-else
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumIfThenElseSameType() {
    // if true then Status::ACTIVE else Status::INACTIVE → ¡Status!
    Value result = compile("if true then Status::ACTIVE else Status::INACTIVE endif");
    assertEquals(1, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumIfThenElseFalseBranch() {
    Value result = compile("if false then Status::ACTIVE else Status::INACTIVE endif");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumIfThenElseInCondition() {
    // if (e == Status::ACTIVE) then 1 else 0 — enum in condition
    Value result = compile("let e = Status::ACTIVE in if e == Status::ACTIVE then 1 else 0 endif");
    assertSingleInt(result, 1);
  }

  @Test
  public void testEnumIfThenElseMixedTypesFails() {
    // if true then Status::ACTIVE else 1 → ERROR (incompatible branches)
    compileExpectError("if true then Status::ACTIVE else 1 endif");
  }

  @Test
  public void testEnumIfThenElseDifferentEnumsFails() {
    // if true then Status::ACTIVE else Direction::NORTH → ERROR
    compileExpectError("if true then Status::ACTIVE else Direction::NORTH endif");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: let-in
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumLetBinding() {
    // let x = Status::ACTIVE in x == Status::ACTIVE → true
    assertSingleBool(compile("let x = Status::ACTIVE in x == Status::ACTIVE"), true);
  }

  @Test
  public void testEnumLetBindingNotEquals() {
    assertSingleBool(compile("let x = Status::ACTIVE in x != Status::INACTIVE"), true);
  }

  @Test
  public void testEnumLetBindingInCollectionOp() {
    Value result =
        compile(
            "let active = Status::ACTIVE in "
                + "Set{Status::ACTIVE, Status::INACTIVE}.select(e | e == active)");
    assertEquals(1, result.size());
  }

  @Test
  public void testEnumLetInIfThenElse() {
    assertSingleBool(
        compile(
            "let s = Status::PENDING in " + "if s == Status::ACTIVE then true else false endif"),
        false);
  }

  @Test
  public void testEnumLetDeclaredTypeMismatchFails() {
    // let x : Integer = Status::ACTIVE → ERROR
    compileExpectError("let x : Integer = Status::ACTIVE in x");
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: including / excluding
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumSetIncluding() {
    Value result = compile("Set{Status::ACTIVE}.including(Status::INACTIVE)");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumSetExcluding() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.excluding(Status::ACTIVE)");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSequencePrepend() {
    Value result = compile("Sequence{Status::INACTIVE}.prepend(Status::ACTIVE)");
    assertEquals(2, result.size());
    assertEquals(
        "ACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSequenceAppend() {
    Value result = compile("Sequence{Status::ACTIVE}.append(Status::INACTIVE)");
    assertEquals(2, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(1)).literal().getName());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: union / intersection
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumSetUnion() {
    Value result = compile("Set{Status::ACTIVE}.union(Set{Status::INACTIVE})");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumSetUnionDedup() {
    Value result = compile("Set{Status::ACTIVE}.union(Set{Status::ACTIVE, Status::INACTIVE})");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumSetIntersection() {
    Value result =
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE}"
                + ".intersection(Set{Status::INACTIVE, Status::PENDING})");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSetSymmetricDifference() {
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
  public void testEnumBagAsSet() {
    // Bag{ACTIVE, ACTIVE, INACTIVE}.asSet() → Set{ACTIVE, INACTIVE}
    Value result = compile("Bag{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.asSet()");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumSetAsSequence() {
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.asSequence()");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumSequenceAsOrderedSet() {
    // Sequence{ACTIVE, ACTIVE, INACTIVE}.asOrderedSet() deduplicates
    Value result =
        compile("Sequence{Status::ACTIVE, Status::ACTIVE, Status::INACTIVE}.asOrderedSet()");
    assertEquals(2, result.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: collect
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumCollect() {
    // collect returns same enum values mapped through identity
    Value result = compile("Set{Status::ACTIVE, Status::INACTIVE}.collect(e | e)");
    assertEquals(2, result.size());
  }

  @Test
  public void testEnumCollectToBoolean() {
    // collect(e | e == Status::ACTIVE) → Set{true, false}
    Value result =
        compile("Set{Status::ACTIVE, Status::INACTIVE}.collect(e | e == Status::ACTIVE)");
    assertEquals(2, result.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Enum: chaining
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEnumSelectThenSize() {
    assertSingleInt(
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::PENDING}"
                + ".select(e | e == Status::ACTIVE).size()"),
        1);
  }

  @Test
  public void testEnumSelectThenFirst() {
    Value result =
        compile(
            "Sequence{Status::INACTIVE, Status::ACTIVE}"
                + ".select(e | e == Status::INACTIVE).first()");
    assertEquals(1, result.size());
    assertEquals(
        "INACTIVE", ((OCLElement.EnumValue) result.getElements().get(0)).literal().getName());
  }

  @Test
  public void testEnumSetSelectThenFirst() {
    // select on Set → Set (unordered) → need asSequence() before first()
    Value r = compile("Set{Status::ACTIVE, Status::INACTIVE}.select(e | e.notEmpty()).asSequence().first()");
    assertEquals(1, r.size());
  }

  @Test
  public void testEnumSortedByThenFirst() {
    Value result = compile("Set{Status::INACTIVE, Status::ACTIVE}.sortedBy(e | e).first()");
    assertEquals(1, result.size());
  }

  @Test
  public void testEnumIterateSum() {
    // iterate to count active elements
    assertSingleInt(
        compile(
            "Set{Status::ACTIVE, Status::INACTIVE, Status::ACTIVE}.iterate(e; acc : Integer = 0 |"
                + " if e == Status::ACTIVE then acc + 1 else acc endif)"),
        1); // Set deduplicates, so only 1 ACTIVE
  }
}
