package tools.vitruv.dsls.vitruvocl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Abstract base class for OCL expression tests, providing a shared compilation pipeline and
 * assertion utilities.
 *
 * <p>Subclasses can use {@link #compile(String)} to run OCL expressions through the full
 * three-phase pipeline (parsing, type checking, evaluation) against a dummy metamodel, and the
 * provided {@code assert*} helpers to verify the resulting {@link Value}.
 *
 * <p>Intended for unit tests that do not require a real metamodel context, such as collection,
 * arithmetic, boolean, and string expression tests.
 */
public abstract class DummyTestSpecification {

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
   * @param input The OCL collection expression to compile and evaluate
   * @return The evaluated result as a {@link Value} (collection of {@link OCLElement}s)
   * @throws AssertionError if any compilation phase fails
   */
  protected Value compile(String input) {
    ParseTree tree = parse(input);

    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      for (CompileError s : typeChecker.getErrorCollector().getErrors()) {
        System.err.println("Type check error: " + s.getColumn() + ": " + s.getMessage());
      }
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      for (CompileError s : evaluator.getErrorCollector().getErrors()) {
        System.err.println("Evaluation error: " + s.getColumn() + ": " + s.getMessage());
      }
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL collection expression string into an ANTLR parse tree.
   *
   * @param input The OCL collection expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.prefixedExpCS();
  }

  // ==================== Assertion Helpers ====================

  /**
   * Asserts that the result collection has the expected size.
   *
   * @param result The evaluated collection value
   * @param expected The expected number of elements
   */
  protected void assertSize(Value result, int expected) {
    assertEquals(expected, result.size(), "Expected collection size " + expected);
  }

  /**
   * Asserts that the result is a singleton containing a single integer value.
   *
   * @param result The evaluated collection value
   * @param expected The expected integer value
   */
  protected void assertSingleInt(Value result, int expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    assertEquals(
        expected,
        ((OCLElement.IntValue) result.getElements().get(0)).value(),
        "Expected integer value " + expected);
  }

  /**
   * Asserts that the result is a singleton containing a single boolean value.
   *
   * @param result The evaluated collection value
   * @param expected The expected boolean value
   */
  protected void assertSingleBool(Value result, boolean expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    assertEquals(
        expected,
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Expected boolean value " + expected);
  }

  /**
   * Asserts that the result is a singleton containing a single real value (Double or Float).
   *
   * @param result The evaluated collection value
   * @param expected The expected numeric value
   */
  protected void assertSingleReal(Value result, double expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    OCLElement element = result.getElements().get(0);
    Double actual = element.tryGetDouble();
    if (actual == null) {
      fail("Expected real value but got " + element.getClass().getSimpleName());
    }
    assertEquals(expected, actual, 1e-9, "Expected real value " + expected);
  }

  /**
   * Asserts that the result collection includes the given integer element.
   *
   * @param result The evaluated collection value
   * @param value The integer value expected to be present
   */
  protected void assertIncludes(Value result, int value) {
    assertTrue(
        result.includes(new OCLElement.IntValue(value)), "Expected collection to include " + value);
  }

  /**
   * Asserts that the element at the given index is the expected integer value.
   *
   * @param result The evaluated collection value
   * @param index The 0-based index to check
   * @param expected The expected integer value at that index
   * @param message Assertion failure message
   */
  protected void assertIntAt(Value result, int index, int expected, String message) {
    assertEquals(
        expected, ((OCLElement.IntValue) result.getElements().get(index)).value(), message);
  }

  /**
   * Asserts that the result is a singleton containing a single string value.
   *
   * @param result The evaluated collection value
   * @param expected The expected string value
   */
  protected void assertSingleString(Value result, String expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    assertEquals(
        expected,
        ((OCLElement.StringValue) result.getElements().get(0)).value(),
        "Expected string value \"" + expected + "\"");
  }

  /**
   * Asserts that the result collection contains exactly the given integer values
   * (order-independent).
   *
   * @param result The evaluated collection value
   * @param expected The expected integer values
   */
  protected void assertCollection(Value result, int... expected) {
    assertEquals(expected.length, result.size(), "Collection size mismatch");
    List<Integer> actual =
        result.getElements().stream().map(e -> ((OCLElement.IntValue) e).value()).sorted().toList();
    List<Integer> expectedList = java.util.Arrays.stream(expected).boxed().sorted().toList();
    assertEquals(expectedList, actual, "Collection content mismatch");
  }

  /**
   * Asserts that the result collection excludes the given integer element.
   *
   * @param result The evaluated collection value
   * @param value The integer value expected to be absent
   */
  protected void assertExcludes(Value result, int value) {
    assertFalse(
        result.includes(new OCLElement.IntValue(value)), "Expected collection to exclude " + value);
  }

  /**
   * Creates a no-op {@link MetamodelWrapperInterface} for use in tests that do not require
   * metamodel or instance data.
   *
   * <p>All instance-related methods return empty results or throw {@link
   * UnsupportedOperationException} if called unexpectedly.
   *
   * @return a dummy {@link MetamodelWrapperInterface} implementation
   */
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
        throw new UnsupportedOperationException("Unimplemented method 'getInstanceNameByIndex'");
      }

      @Override
      public List<EObject> getAllRootObjects() {
        throw new UnsupportedOperationException("Unimplemented method 'getAllRootObjects'");
      }

      @Override
      public EObject getContextObjectByIndex(int index) {
        throw new UnsupportedOperationException("Unimplemented method 'getContextObjectByIndex'");
      }

      @Override
      public EEnum resolveEEnum(String enumName) {
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

  /**
   * Asserts that the result is a singleton containing a single double or float value (within
   * epsilon).
   *
   * @param result The evaluated collection value
   * @param expected The expected double value
   */
  protected void assertSingleDouble(Value result, double expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    OCLElement elem = result.getElements().get(0);
    double actual;
    if (elem.tryGetDouble() != null) {
      actual = elem.tryGetDouble();
    } else if (elem.tryGetFloat() != null) {
      actual = elem.tryGetFloat();
    } else if (elem.tryGetInt() != null) {
      actual = elem.tryGetInt();
    } else {
      fail("Expected numeric singleton but got: " + elem);
      return;
    }
    assertEquals(expected, actual, 1e-9, "Expected double value " + expected);
  }
}
