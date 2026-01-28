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
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Tests for oclIsKindOf operation.
 *
 * <p>oclIsKindOf(TypeName): Collection(T) → Collection(Boolean) Tests type checking for primitive
 * types: Integer, String, Boolean
 */
public class OCLIsKindOfTest {

  // ==================== Integer Type Checking ====================

  @Test
  public void testIntegerIsKindOfInteger() {
    String input = "Set{5}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testIntegerIsKindOfString() {
    String input = "Set{5}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testIntegerIsKindOfBoolean() {
    String input = "Set{5}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== String Type Checking ====================

  @Test
  public void testStringIsKindOfString() {
    String input = "Set{\"hello\"}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testStringIsKindOfInteger() {
    String input = "Set{\"hello\"}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testStringIsKindOfBoolean() {
    String input = "Set{\"hello\"}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== Boolean Type Checking ====================

  @Test
  public void testBooleanIsKindOfBoolean() {
    String input = "Set{true}.oclIsKindOf(Boolean)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testBooleanIsKindOfInteger() {
    String input = "Set{true}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testBooleanIsKindOfString() {
    String input = "Set{false}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value());
  }

  // ==================== Multiple Elements ====================

  @Test
  public void testMultipleIntegersIsKindOfInteger() {
    String input = "Set{1, 2, 3}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  @Test
  public void testMultipleIntegersIsKindOfString() {
    String input = "Set{1, 2, 3}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  @Test
  public void testMultipleStringsIsKindOfString() {
    String input = "Set{\"a\", \"b\", \"c\"}.oclIsKindOf(String)";
    Value result = compile(input);

    assertEquals(3, result.size());
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

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

  @Test
  public void testEmptyCollectionIsKindOf() {
    String input = "Set{}.oclIsKindOf(Integer)";
    Value result = compile(input);

    assertEquals(0, result.size());
  }

  // ==================== Sequence Preservation ====================

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
    ErrorCollector errors = new ErrorCollector();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

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

    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertTrue(resultType.isOrdered());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

  // ==================== Unknown Types ====================

  /**
   * @Test public void testUnknownTypeReturnsFalse() { String input =
   * "Set{5}.oclIsKindOf(MyCustomClass)";
   *
   * <p>System.out.println("=== DEBUG: testUnknownTypeReturnsFalse ==="); System.out.println("Input:
   * " + input);
   *
   * <p>// Vor dem compile() Value result = compile(input);
   *
   * <p>assertEquals(1, result.size()); OCLElement elem = result.getElements().get(0);
   * assertFalse(((OCLElement.BoolValue) elem).value()); }
   */

  // ==================== Mixed Type Collections ====================

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

  private Value compile(String input) {
    ParseTree tree = parse(input);

    System.out.println("=== DEBUG compile() ===");
    System.out.println("Input: " + input);

    // Dummy specification
    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            System.out.println(
                "  resolveEClass called: metamodel=" + metamodel + ", className=" + className);
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

    // Pass 1: Symbol Table
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();

    System.out.println("=== Pass 2: Type Checking ===");
    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    System.out.println("Type checking errors: " + errors.getErrors().size());
    for (CompileError error : errors.getErrors()) {
      System.out.println("  ERROR: " + error.getMessage() + " at line " + error.getLine());
    }

    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    System.out.println("=== Pass 3: Evaluation ===");
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
    }

    System.out.println("Result: " + result);
    return result;
  }

  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }
}