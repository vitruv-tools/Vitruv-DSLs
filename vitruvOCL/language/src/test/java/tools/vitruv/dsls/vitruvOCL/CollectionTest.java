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
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for collection operations and literals.
 *
 * <p>This test class validates the complete implementation of OCL collections in VitruvOCL, testing
 * both collection literals and operations through the full compilation pipeline (parsing, type
 * checking, and evaluation).
 *
 * <h2>Tested Collection Types</h2>
 *
 * <ul>
 *   <li><b>Set:</b> Unordered, unique elements - {@code Set{1,2,3}}
 *   <li><b>Sequence:</b> Ordered, allows duplicates - {@code Sequence{1,2,2,3}}
 *   <li><b>Bag:</b> Unordered, allows duplicates - {@code Bag{1,2,2,3}}
 *   <li><b>OrderedSet:</b> Ordered, unique elements - {@code OrderedSet{1,2,3}}
 * </ul>
 *
 * <h2>Tested Operations</h2>
 *
 * <ul>
 *   <li><b>Query operations:</b> {@code size()}, {@code isEmpty()}, {@code notEmpty()}, {@code
 *       includes()}, {@code excludes()}
 *   <li><b>Set operations:</b> {@code union()}, {@code including()}, {@code excluding()}
 *   <li><b>Ordered operations:</b> {@code first()}, {@code last()}, {@code reverse()}
 *   <li><b>Ranges:</b> {@code 1..5} (ascending), {@code 5..1} (descending)
 *   <li><b>Chained operations:</b> {@code Set{1,2}.including(3).excluding(2).size()}
 * </ul>
 *
 * @see Value Runtime collection representation
 * @see OCLElement Collection element types
 * @see EvaluationVisitor Evaluates collection operations
 */
public class CollectionTest {

  // ==================== Collection Literals ====================

  /**
   * Tests creation of a Set literal with distinct elements.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}}
   *
   * <p><b>Expected:</b> Collection with 3 elements (1, 2, 3)
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Set literal syntax is correctly parsed
   *   <li>All elements are present in the result
   *   <li>Element count is correct
   * </ul>
   */
  @Test
  public void testSetLiteral() {
    String input = "Set{1,2,3}";
    Value result = compile(input);

    assertNotNull(result, "Result should not be null");
    assertEquals(3, result.size(), "Set{1,2,3} should have 3 elements");

    // Check that all elements are present
    assertTrue(result.includes(new OCLElement.IntValue(1)), "Set should include 1");
    assertTrue(result.includes(new OCLElement.IntValue(2)), "Set should include 2");
    assertTrue(result.includes(new OCLElement.IntValue(3)), "Set should include 3");
  }

  /**
   * Tests that Set literals automatically remove duplicate elements.
   *
   * <p><b>Input:</b> {@code Set{1,2,2,3}}
   *
   * <p><b>Expected:</b> Collection with 3 unique elements (duplicates removed)
   *
   * <p><b>Set semantics:</b> Sets enforce uniqueness - duplicate elements are automatically
   * filtered out during construction.
   */
  @Test
  public void testSetRemovesDuplicates() {
    String input = "Set{1,2,2,3}";
    Value result = compile(input);

    assertEquals(3, result.size(), "Set{1,2,2,3} should have only 3 unique elements");
  }

  /**
   * Tests creation of an empty Set.
   *
   * <p><b>Input:</b> {@code Set{}}
   *
   * <p><b>Expected:</b> Empty collection (size = 0, isEmpty = true)
   */
  @Test
  public void testEmptySet() {
    String input = "Set{}";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "Set{} should be empty");
    assertEquals(0, result.size(), "Empty set size should be 0");
  }

  /**
   * Tests creation of a Sequence literal.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}}
   *
   * <p><b>Expected:</b> Ordered collection with 3 elements
   *
   * <p><b>Sequence semantics:</b> Maintains insertion order and allows duplicates.
   */
  @Test
  public void testSequenceLiteral() {
    String input = "Sequence{1,2,3}";
    Value result = compile(input);

    assertEquals(3, result.size(), "Sequence{1,2,3} should have 3 elements");
  }

  /**
   * Tests that Sequence literals preserve duplicate elements.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,2,3}}
   *
   * <p><b>Expected:</b> Collection with 4 elements (duplicates kept)
   *
   * <p><b>Sequence vs Set:</b> Unlike Sets, Sequences do not remove duplicates.
   */
  @Test
  public void testSequenceKeepsDuplicates() {
    String input = "Sequence{1,2,2,3}";
    Value result = compile(input);

    assertEquals(4, result.size(), "Sequence{1,2,2,3} should keep all 4 elements");
  }

  // ==================== Ranges ====================

  /**
   * Tests ascending range expression.
   *
   * <p><b>Input:</b> {@code Set{1..5}}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3, 4, 5}
   *
   * <p><b>Range semantics:</b> {@code start..end} generates all integers from start to end
   * (inclusive).
   */
  @Test
  public void testRange() {
    String input = "Set{1..5}";
    Value result = compile(input);

    assertEquals(5, result.size(), "Set{1..5} should have 5 elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests descending range expression.
   *
   * <p><b>Input:</b> {@code Sequence{5..1}}
   *
   * <p><b>Expected:</b> Sequence containing {5, 4, 3, 2, 1} in that order
   *
   * <p><b>Descending ranges:</b> When start > end, the range generates elements in descending
   * order. This is particularly useful in Sequences where order matters.
   */
  @Test
  public void testDescendingRange() {
    String input = "Sequence{5..1}";
    Value result = compile(input);

    assertEquals(5, result.size(), "Sequence{5..1} should have 5 elements");

    // Check order: should be 5, 4, 3, 2, 1
    OCLElement first = result.getElements().get(0);
    assertEquals(5, ((OCLElement.IntValue) first).value(), "First element should be 5");

    OCLElement last = result.getElements().get(4);
    assertEquals(1, ((OCLElement.IntValue) last).value(), "Last element should be 1");
  }

  // ==================== Query Operations ====================

  /**
   * Tests the {@code size()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   */
  @Test
  public void testSize() {
    String input = "Set{1,2,3}.size()";
    Value result = compile(input);

    assertEquals(1, result.size(), "size() should return a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(3, ((OCLElement.IntValue) elem).value(), "Set{1,2,3}.size() should be 3");
  }

  /**
   * Tests the {@code isEmpty()} operation on an empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.isEmpty()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   *
   * <p><b>Usage:</b> Query operation to check if a collection has zero elements.
   */
  @Test
  public void testIsEmpty() {
    String input = "Set{}.isEmpty()";
    Value result = compile(input);

    assertEquals(1, result.size(), "isEmpty() should return a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set{}.isEmpty() should be true");
  }

  /**
   * Tests the {@code notEmpty()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.notEmpty()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   *
   * <p><b>Usage:</b> Convenient negation of {@code isEmpty()}, often used in constraints.
   */
  @Test
  public void testNotEmpty() {
    String input = "Set{1,2,3}.notEmpty()";
    Value result = compile(input);

    assertEquals(1, result.size(), "notEmpty() should return a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.notEmpty() should be true");
  }

  /**
   * Tests the {@code includes()} operation with an element present in the collection.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.includes(2)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   *
   * <p><b>Semantics:</b> Returns true if the collection contains an element equal to the argument.
   */
  @Test
  public void testIncludes() {
    String input = "Set{1,2,3}.includes(2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "includes() should return a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.includes(2) should be true");
  }

  /**
   * Tests the {@code includes()} operation with an element not in the collection.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.includes(5)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [false]}
   */
  @Test
  public void testIncludesFalse() {
    String input = "Set{1,2,3}.includes(5)";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.includes(5) should be false");
  }

  /**
   * Tests the {@code excludes()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.excludes(5)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   *
   * <p><b>Semantics:</b> Returns true if the collection does NOT contain the element. This is the
   * logical negation of {@code includes()}.
   */
  @Test
  public void testExcludes() {
    String input = "Set{1,2,3}.excludes(5)";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.excludes(5) should be true");
  }

  // ==================== Set Operations ====================

  /**
   * Tests the {@code union()} operation with disjoint sets.
   *
   * <p><b>Input:</b> {@code Set{1,2}.union(Set{3,4})}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3, 4} (4 elements)
   *
   * <p><b>Set theory:</b> Union combines all elements from both sets.
   */
  @Test
  public void testUnion() {
    String input = "Set{1,2}.union(Set{3,4})";
    Value result = compile(input);

    assertEquals(4, result.size(), "Union should have 4 elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
  }

  /**
   * Tests the {@code union()} operation with overlapping sets.
   *
   * <p><b>Input:</b> {@code Set{1,2}.union(Set{2,3})}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3} (3 elements, duplicate 2 removed)
   *
   * <p><b>Set semantics:</b> Union removes duplicates for Set results, maintaining set uniqueness
   * property.
   */
  @Test
  public void testUnionWithDuplicates() {
    String input = "Set{1,2}.union(Set{2,3})";
    Value result = compile(input);

    assertEquals(3, result.size(), "Union should remove duplicates: {1,2,3}");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  /**
   * Tests the {@code including()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2}.including(3)}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3}
   *
   * <p><b>Usage:</b> Adds a single element to the collection. Returns a new collection; the
   * original is unchanged (immutable operations).
   */
  @Test
  public void testIncluding() {
    String input = "Set{1,2}.including(3)";
    Value result = compile(input);

    assertEquals(3, result.size(), "including(3) should add element");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  /**
   * Tests the {@code excluding()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.excluding(2)}
   *
   * <p><b>Expected:</b> Set containing {1, 3} (element 2 removed)
   *
   * <p><b>Usage:</b> Removes all occurrences of an element from the collection. Returns a new
   * collection.
   */
  @Test
  public void testExcluding() {
    String input = "Set{1,2,3}.excluding(2)";
    Value result = compile(input);

    assertEquals(2, result.size(), "excluding(2) should remove element");
    assertFalse(result.includes(new OCLElement.IntValue(2)));
  }

  // ==================== Ordered Operations ====================

  /**
   * Tests the {@code first()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.first()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [1]}
   *
   * <p><b>Ordered collection semantics:</b> Returns the first element. Only defined for ordered
   * collections (Sequence, OrderedSet).
   */
  @Test
  public void testFirst() {
    String input = "Sequence{1,2,3}.first()";
    Value result = compile(input);

    assertEquals(1, result.size(), "first() should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(1, ((OCLElement.IntValue) elem).value(), "first() should be 1");
  }

  /**
   * Tests the {@code last()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.last()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   *
   * <p><b>Ordered collection semantics:</b> Returns the last element. Only defined for ordered
   * collections.
   */
  @Test
  public void testLast() {
    String input = "Sequence{1,2,3}.last()";
    Value result = compile(input);

    assertEquals(1, result.size(), "last() should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(3, ((OCLElement.IntValue) elem).value(), "last() should be 3");
  }

  /**
   * Tests the {@code reverse()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.reverse()}
   *
   * <p><b>Expected:</b> Sequence containing {3, 2, 1} in that order
   *
   * <p><b>Ordered collection semantics:</b> Returns a new collection with elements in reverse
   * order. Only defined for ordered collections.
   *
   * <p><b>Validation:</b> Checks both size and element order.
   */
  @Test
  public void testReverse() {
    String input = "Sequence{1,2,3}.reverse()";
    Value result = compile(input);

    assertEquals(3, result.size(), "reverse() should have 3 elements");

    // Check reversed order: 3, 2, 1
    assertEquals(3, ((OCLElement.IntValue) result.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(1)).value());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(2)).value());
  }

  // ==================== Chained Operations ====================

  /**
   * Tests multiple collection operations chained together.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.including(4).excluding(2).size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>{@code Set{1,2,3}.including(4)} → {@code {1,2,3,4}}
   *   <li>{@code {1,2,3,4}.excluding(2)} → {@code {1,3,4}}
   *   <li>{@code {1,3,4}.size()} → {@code 3}
   * </ol>
   *
   * <p><b>Validates:</b> Proper chaining through receiverStack mechanism in evaluator.
   */
  @Test
  public void testChainedOperations() {
    String input = "Set{1,2,3}.including(4).excluding(2).size()";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertEquals(
        3,
        ((OCLElement.IntValue) elem).value(),
        "Set{1,2,3}.including(4).excluding(2).size() should be 3");
  }

  // ==================== Edge Cases ====================

  /**
   * Tests {@code size()} on an empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [0]}
   *
   * <p><b>Edge case:</b> Ensures operations work correctly on empty collections.
   */
  @Test
  public void testEmptySetSize() {
    String input = "Set{}.size()";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertEquals(0, ((OCLElement.IntValue) elem).value(), "Empty set size should be 0");
  }

  /**
   * Tests {@code size()} on a singleton collection.
   *
   * <p><b>Input:</b> {@code Set{42}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [1]}
   */
  @Test
  public void testSingletonSize() {
    String input = "Set{42}.size()";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertEquals(1, ((OCLElement.IntValue) elem).value(), "Singleton set size should be 1");
  }

  /**
   * Tests range generation with a large range.
   *
   * <p><b>Input:</b> {@code Set{1..100}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [100]}
   *
   * <p><b>Performance test:</b> Validates that range generation works efficiently for larger ranges
   * (100 elements).
   */
  @Test
  public void testLargeRange() {
    String input = "Set{1..100}.size()";
    Value result = compile(input);

    OCLElement elem = result.getElements().get(0);
    assertEquals(100, ((OCLElement.IntValue) elem).value(), "Set{1..100}.size() should be 100");
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL collection expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process for collection expressions:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input string to ANTLR parse tree
   *   <li><b>Phase 2 - Type Checking:</b> Validates collection types and operations
   *   <li><b>Phase 3 - Evaluation:</b> Computes runtime collection values
   * </ol>
   *
   * <p><b>No metamodels required:</b> Collection tests use a dummy metamodel wrapper since
   * collection operations don't require metamodel context.
   *
   * <p><b>Error handling:</b> Fails the test immediately if type checking or evaluation encounters
   * errors, printing detailed error messages.
   *
   * @param input The OCL collection expression to compile and evaluate
   * @return The evaluated result as a {@link Value} (collection of {@link OCLElement}s)
   * @throws AssertionError if any compilation phase fails
   */
  private Value compile(String input) {
    // Phase 1: Parse
    ParseTree tree = parse(input);

    // Create dummy specification - collections don't need metamodels
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

    // Phase 1: Symbol Table (trivial for collection literals)
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();

    // Phase 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    // Check for type errors
    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Phase 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    // Check for evaluation errors
    if (evaluator.hasErrors()) {
      fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL collection expression string into an ANTLR parse tree.
   *
   * <p>Uses {@code prefixedExpCS} as the entry point, which handles collection literals and
   * operations. This is more specific than using the general {@code expCS} entry point.
   *
   * <p><b>Grammar coverage:</b> {@code prefixedExpCS} covers:
   *
   * <ul>
   *   <li>Collection literals: {@code Set{...}}, {@code Sequence{...}}
   *   <li>Navigation chains: {@code collection.operation().operation()}
   *   <li>Unary operators: {@code not}, {@code -}
   * </ul>
   *
   * @param input The OCL collection expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  private ParseTree parse(String input) {
    // Lexer
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Parser
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    // Parse as prefixed expression (covers collection literals)
    return parser.prefixedExpCS();
  }
}