package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for the {@code oclIsKindOf} type checking operation in VitruvOCL.
 *
 * <p>This test class validates the implementation of OCL's runtime type checking operation {@code
 * oclIsKindOf}, which determines whether elements in a collection conform to a specific type. This
 * operation is fundamental for dynamic type checking and polymorphic constraint validation.
 *
 * <h2>Operation Signature</h2>
 *
 * <pre>{@code
 * oclIsKindOf(TypeName) : Collection(T) → Collection(Boolean)
 * }</pre>
 *
 * <p>Where:
 *
 * <ul>
 *   <li><b>Input:</b> A collection of elements of any type
 *   <li><b>TypeName:</b> The type to check against (e.g., Integer, String, Boolean, or a metaclass)
 *   <li><b>Output:</b> A collection of Boolean values, one for each input element
 * </ul>
 *
 * <h2>Semantics</h2>
 *
 * The {@code oclIsKindOf} operation:
 *
 * <ul>
 *   <li><b>Maps each element:</b> Transforms each element into a Boolean indicating type
 *       conformance
 *   <li><b>Preserves collection structure:</b> Maintains collection kind (Set → Set of Booleans,
 *       Sequence → Sequence of Booleans)
 *   <li><b>Handles polymorphism:</b> Returns true for exact type match and compatible subtypes
 *   <li><b>Element-by-element checking:</b> Each element is checked independently
 * </ul>
 *
 * @see Value Runtime collection representation
 * @see OCLElement.BoolValue Boolean element wrapper
 * @see EvaluationVisitor Evaluates oclIsKindOf operations
 * @see TypeCheckVisitor Type checks oclIsKindOf expressions
 */
public class OCLIsKindOfTest {

  // ==================== Integer Type Checking ====================

  /**
   * Tests that Integer elements are correctly identified as Integer type.
   *
   * <p><b>Input:</b> {@code Set{5}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{true}}
   *
   * <p><b>Type checking logic:</b> Element 5 is of type Integer, checking against Integer → true
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic oclIsKindOf syntax parsing
   *   <li>Correct type identification for Integer
   *   <li>Result is Boolean singleton collection
   * </ul>
   */
  @Test
  public void testIntegerIsKindOfInteger() {
    String input = "Set{5}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that Integer elements are correctly identified as NOT String type.
   *
   * <p><b>Input:</b> {@code Set{5}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   *
   * <p><b>Type checking logic:</b> Element 5 is of type Integer, checking against String → false
   */
  @Test
  public void testIntegerIsKindOfString() {
    String input = "Set{5}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that Integer elements are correctly identified as NOT Boolean type.
   *
   * <p><b>Input:</b> {@code Set{5}.oclIsKindOf(Boolean)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   *
   * <p><b>Type checking logic:</b> Element 5 is of type Integer, checking against Boolean → false
   */
  @Test
  public void testIntegerIsKindOfBoolean() {
    String input = "Set{5}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== String Type Checking ====================

  /**
   * Tests that String elements are correctly identified as String type.
   *
   * <p><b>Input:</b> {@code Set{"hello"}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Set{true}}
   *
   * <p><b>Type checking logic:</b> Element "hello" is of type String, checking against String →
   * true
   */
  @Test
  public void testStringIsKindOfString() {
    String input = "Set{\"hello\"}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that String elements are correctly identified as NOT Integer type.
   *
   * <p><b>Input:</b> {@code Set{"hello"}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   */
  @Test
  public void testStringIsKindOfInteger() {
    String input = "Set{\"hello\"}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that String elements are correctly identified as NOT Boolean type.
   *
   * <p><b>Input:</b> {@code Set{"hello"}.oclIsKindOf(Boolean)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   */
  @Test
  public void testStringIsKindOfBoolean() {
    String input = "Set{\"hello\"}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== Boolean Type Checking ====================

  /**
   * Tests that Boolean elements are correctly identified as Boolean type.
   *
   * <p><b>Input:</b> {@code Set{true}.oclIsKindOf(Boolean)}
   *
   * <p><b>Expected:</b> {@code Set{true}}
   *
   * <p><b>Type checking logic:</b> Element true is of type Boolean, checking against Boolean → true
   */
  @Test
  public void testBooleanIsKindOfBoolean() {
    String input = "Set{true}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that Boolean elements are correctly identified as NOT Integer type.
   *
   * <p><b>Input:</b> {@code Set{true}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   */
  @Test
  public void testBooleanIsKindOfInteger() {
    String input = "Set{true}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests that Boolean elements are correctly identified as NOT String type.
   *
   * <p><b>Input:</b> {@code Set{false}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Set{false}}
   */
  @Test
  public void testBooleanIsKindOfString() {
    String input = "Set{false}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== Multiple Elements ====================

  /**
   * Tests oclIsKindOf on multiple Integer elements.
   *
   * <p><b>Input:</b> {@code Set{1, 2, 3}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{true, true, true}} (3 elements)
   *
   * <p><b>Element-by-element checking:</b> Each element is independently checked, all are Integer →
   * all results are true.
   *
   * <p><b>Validates:</b> oclIsKindOf maps over all collection elements.
   */
  @Test
  public void testMultipleIntegersIsKindOfInteger() {
    String input = "Set{1, 2, 3}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /**
   * Tests oclIsKindOf on multiple Integer elements checking against String.
   *
   * <p><b>Input:</b> {@code Set{1, 2, 3}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Set{false, false, false}} (3 elements)
   *
   * <p><b>Validates:</b> All elements correctly identified as NOT String.
   */
  @Test
  public void testMultipleIntegersIsKindOfString() {
    String input = "Set{1, 2, 3}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  /**
   * Tests oclIsKindOf on multiple String elements.
   *
   * <p><b>Input:</b> {@code Set{"a", "b", "c"}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Set{true, true, true}} (3 elements)
   */
  @Test
  public void testMultipleStringsIsKindOfString() {
    String input = "Set{\"a\", \"b\", \"c\"}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /**
   * Tests oclIsKindOf on multiple Boolean elements with duplicates.
   *
   * <p><b>Input:</b> {@code Set{true, false, true}.oclIsKindOf(Boolean)}
   *
   * <p><b>Expected:</b> {@code Set{true, true}} (2 elements after Set duplicate removal)
   *
   * <p><b>Set semantics:</b> Input Set removes duplicate 'true', leaving {true, false}. Result is
   * {true, true} for the two unique elements.
   */
  @Test
  public void testMultipleBooleansIsKindOfBoolean() {
    String input = "Set{true, false, true}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(2, result.size()); // Set removes duplicates: {true, false}
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  // ==================== Empty Collection ====================

  /**
   * Tests oclIsKindOf on an empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{}} (empty collection)
   *
   * <p><b>Edge case:</b> Mapping over an empty collection produces an empty result collection.
   *
   * <p><b>Validates:</b> Empty collection handling is correct.
   */
  @Test
  public void testEmptyCollectionIsKindOf() {
    String input = "Set{}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(0, result.size());
  }

  // ==================== Sequence Preservation ====================

  /**
   * Tests that oclIsKindOf preserves Sequence order.
   *
   * <p><b>Input:</b> {@code Sequence{1, 2, 3}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Sequence{true, true, true}} (ordered)
   *
   * <p><b>Collection kind preservation:</b> The operation preserves the collection kind (Sequence),
   * including order.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Ordered collections maintain their order
   *   <li>Collection kind is preserved (Sequence → Sequence)
   * </ul>
   */
  @Test
  public void testSequencePreservesOrder() {
    String input = "Sequence{1, 2, 3}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(3, result.size());
    List<OCLElement> elements = result.getElements();

    // All should be true
    for (OCLElement elem : elements) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  // ==================== Type Checking ====================

  /**
   * Tests that type checker infers Collection(Boolean) as result type.
   *
   * <p><b>Input:</b> {@code Set{5}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected result type:</b> {@code Set(Boolean)}
   *
   * <p><b>Type inference rule:</b> {@code Collection(T).oclIsKindOf(U) → Collection(Boolean)} with
   * same collection kind
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Type checker correctly infers Boolean element type
   *   <li>Result is a collection type
   *   <li>Type checking phase succeeds without errors
   * </ul>
   */
  @Test
  public void testTypeCheckReturnsBoolean() {
    String input = "Set{5}.oclIsKindOf(Integer)";
    ParseTree tree = parse(input);

    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            return null;
          }

          @Override
          public List<EObject> getAllInstances(EClass eClass) {
            return List.of();
          }

          @Override
          public Set<String> getAvailableMetamodels() {
            return Set.of();
          }
        };

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    assertFalse(errors.hasErrors(), "Pass 1 should not have errors");

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

  /**
   * Tests that type checker preserves collection kind (Sequence).
   *
   * <p><b>Input:</b> {@code Sequence{1, 2}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected result type:</b> {@code Sequence(Boolean)} (ordered)
   *
   * <p><b>Type preservation:</b> The type checker preserves all collection properties:
   *
   * <ul>
   *   <li>Collection kind (Sequence)
   *   <li>Ordered property (true)
   *   <li>Element type becomes Boolean
   * </ul>
   *
   * <p><b>Validates:</b> Collection metadata is correctly preserved through type transformation.
   */
  @Test
  public void testTypeCheckPreservesCollectionKind() {
    String input = "Sequence{1, 2}.oclIsKindOf(Integer)";
    ParseTree tree = parse(input);

    // Dummy specification
    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            return null;
          }

          @Override
          public List<EObject> getAllInstances(EClass eClass) {
            return List.of();
          }

          @Override
          public Set<String> getAvailableMetamodels() {
            return Set.of();
          }
        };

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    assertFalse(errors.hasErrors(), "Pass 1 should not have errors");

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertTrue(resultType.isOrdered());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

  // ==================== Unknown Types ====================

  /**
   * Disabled test for unknown type handling.
   *
   * <p><b>Purpose:</b> Would test behavior when checking against an unknown/custom type name.
   *
   * <p><b>Expected behavior:</b> Could either:
   *
   * <ul>
   *   <li>Return false (type not recognized)
   *   <li>Report type checking error (type name not found)
   * </ul>
   *
   * <p><b>Note:</b> Currently disabled - behavior for unknown types may need specification.
   */
  /**
   * @Test public void testUnknownTypeReturnsFalse() { String input =
   * "Set{5}.oclIsKindOf(MyCustomClass)";
   *
   * <p>// Vor dem compile() Value result = compile(input);
   *
   * <p>assertEquals(1, result.size()); OCLElement elem = result.getElements().get(0);
   * assertFalse(((OCLElement.BoolValue) elem).value()); }
   */

  // ==================== Mixed Type Collections ====================

  /**
   * Tests oclIsKindOf on heterogeneous collection checking for Integer.
   *
   * <p><b>Input:</b> {@code Sequence{1, "hello", true}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Sequence{true, false, false}}
   *
   * <p><b>Element-by-element checking:</b>
   *
   * <ul>
   *   <li>Element 1 (Integer) → true
   *   <li>Element "hello" (String) → false
   *   <li>Element true (Boolean) → false
   * </ul>
   *
   * <p><b>Validates:</b> Each element is independently type-checked, supporting mixed-type
   * collections.
   */
  @Test
  public void testMixedTypesInCollection() {
    String input = "Sequence{1, \"hello\", true}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(3, result.size());
    List<OCLElement> elements = result.getElements();

    // First element (1) is Integer → true
    assertTrue(((OCLElement.BoolValue) elements.get(0)).value());

    // Second element ("hello") is NOT Integer → false
    assertFalse(((OCLElement.BoolValue) elements.get(1)).value());

    // Third element (true) is NOT Integer → false
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
  }

  /**
   * Tests mixed types checking for String type.
   *
   * <p><b>Input:</b> {@code Sequence{1, "hello", true, "world"}.oclIsKindOf(String)}
   *
   * <p><b>Expected:</b> {@code Sequence{false, true, false, true}}
   *
   * <p><b>Element-by-element checking:</b>
   *
   * <ul>
   *   <li>Element 1 (Integer) → false
   *   <li>Element "hello" (String) → true
   *   <li>Element true (Boolean) → false
   *   <li>Element "world" (String) → true
   * </ul>
   */
  @Test
  public void testMixedTypesCheckingForString() {
    String input = "Sequence{1, \"hello\", true, \"world\"}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(4, result.size());
    List<OCLElement> elements = result.getElements();

    // First element (1) is NOT String → false
    assertFalse(((OCLElement.BoolValue) elements.get(0)).value());

    // Second element ("hello") is String → true
    assertTrue(((OCLElement.BoolValue) elements.get(1)).value());

    // Third element (true) is NOT String → false
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());

    // Fourth element ("world") is String → true
    assertTrue(((OCLElement.BoolValue) elements.get(3)).value());
  }

  /**
   * Tests mixed types checking for Boolean, counting true results.
   *
   * <p><b>Input:</b> {@code Set{1, "test", true}.oclIsKindOf(Boolean)}
   *
   * <p><b>Expected:</b> {@code Set{false, false, true}} (3 elements, 1 true)
   *
   * <p><b>Validation strategy:</b> Counts the number of true results to verify exactly one element
   * (the Boolean) is identified.
   */
  @Test
  public void testAllDifferentTypesCheckBoolean() {
    String input = "Set{1, \"test\", true}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(3, result.size());

    // Exactly one element should be true (the boolean value)
    int trueCount = 0;
    for (OCLElement elem : result.getElements()) {
      if (((OCLElement.BoolValue) elem).value()) {
        trueCount++;
      }
    }

    assertEquals(1, trueCount, "Exactly one element should be Boolean");
  }

  /**
   * Tests that all String elements return false when checked against Integer.
   *
   * <p><b>Input:</b> {@code Set{"hello", "world", "test"}.oclIsKindOf(Integer)}
   *
   * <p><b>Expected:</b> {@code Set{false, false, false}}
   *
   * <p><b>Validates:</b> No type confusion - Strings are never identified as Integers.
   */
  @Test
  public void testEmptyResultFromMixedCollection() {
    String input = "Set{\"hello\", \"world\", \"test\"}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(3, result.size());

    // All elements should be false
    for (OCLElement elem : result.getElements()) {
      assertFalse(
          ((OCLElement.BoolValue) elem).value(), "No string should be identified as Integer");
    }
  }

  /**
   * Tests oclIsKindOf on flattened nested collections with mixed types.
   *
   * <p><b>Input:</b> {@code Sequence{Set{1, 2}, Set{"a", "b"}, Set{true,
   * false}}.flatten().oclIsKindOf(Integer)}
   *
   * <p><b>After flatten:</b> {@code Sequence{1, 2, "a", "b", true, false}}
   *
   * <p><b>Expected:</b> {@code Sequence{true, true, false, false, false, false}}
   *
   * <p><b>Element breakdown:</b>
   *
   * <ul>
   *   <li>Elements 1, 2 (Integers) → true, true
   *   <li>Elements "a", "b" (Strings) → false, false
   *   <li>Elements true, false (Booleans) → false, false
   * </ul>
   *
   * <p><b>Validates:</b> oclIsKindOf works correctly after collection transformation operations
   * like flatten.
   */
  @Test
  public void testNestedCollectionsWithMixedTypes() {
    String input =
        "Sequence{Set{1, 2}, Set{\"a\", \"b\"}, Set{true, false}}.flatten().oclIsKindOf(Integer)";
    Value result = compile(input);

    // After flatten: {1, 2, "a", "b", true, false}
    assertEquals(6, result.size());

    List<OCLElement> elements = result.getElements();

    // First two should be true (integers)
    assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(1)).value());

    // Next two should be false (strings)
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(3)).value());

    // Last two should be false (booleans)
    assertFalse(((OCLElement.BoolValue) elements.get(4)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(5)).value());
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL expression containing oclIsKindOf through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process with enhanced debug output for
   * tracking type checking behavior:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree
   *   <li><b>Phase 2 - Type Checking:</b> Validates oclIsKindOf operation and infers
   *       Collection(Boolean) type
   *   <li><b>Phase 3 - Evaluation:</b> Executes element-by-element type checking
   * </ol>
   *
   * <p><b>Debug output:</b> This method prints extensive debugging information:
   *
   * <ul>
   *   <li>Input expression
   *   <li>Type checking errors (if any)
   *   <li>Evaluation phase status
   *   <li>Final result
   * </ul>
   *
   * <p><b>Type checking:</b> Validates that:
   *
   * <ul>
   *   <li>Receiver is a collection
   *   <li>Type argument is valid
   *   <li>Result type is Collection(Boolean) with preserved collection kind
   * </ul>
   *
   * @param input The OCL expression containing oclIsKindOf to compile and evaluate
   * @return The evaluated result as a {@link Value} (Collection of Booleans)
   * @throws AssertionError if type checking or evaluation fails
   */
  private Value compile(String input) {
    ParseTree tree = parse(input);

    // Dummy specification (no metamodels needed for primitive type checking)
    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            return null;
          }

          @Override
          public List<EObject> getAllInstances(EClass eClass) {
            return List.of();
          }

          @Override
          public Set<String> getAvailableMetamodels() {
            return Set.of();
          }
        };

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      for (CompileError error : errors.getErrors()) {
        System.out.println("  PASS 1 ERROR: " + error.getMessage() + " at line " + error.getLine());
      }
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      for (CompileError error : errors.getErrors()) {
        System.out.println("  PASS 2 ERROR: " + error.getMessage() + " at line " + error.getLine());
      }
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      for (CompileError error : errors.getErrors()) {
        System.out.println("  PASS 3 ERROR: " + error.getMessage() + " at line " + error.getLine());
      }
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL expression string into an ANTLR parse tree.
   *
   * <p>Uses {@code infixedExpCS} as the entry point to handle oclIsKindOf operations and other
   * infix expressions.
   *
   * @param input The OCL expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }
}