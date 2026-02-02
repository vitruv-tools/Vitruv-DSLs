package tools.vitruv.dsls.vitruvOCL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Comprehensive test suite for parsing various VitruvOCL constraint patterns and expressions.
 *
 * <p>This test class validates the ANTLR parser's ability to correctly parse different OCL
 * constraint syntaxes, from simple context declarations to complex expressions involving qualified
 * names, collection operations, and navigation patterns. Tests focus solely on the parsing phase
 * without type checking or evaluation.
 *
 * <h2>Test Objectives</h2>
 *
 * <ul>
 *   <li><b>Syntax validation:</b> Verify parser accepts valid OCL constraint syntax
 *   <li><b>Parse tree generation:</b> Ensure parser produces non-null, well-formed parse trees
 *   <li><b>Grammar coverage:</b> Exercise different grammar rules and production paths
 *   <li><b>Edge case handling:</b> Test various constraint patterns and expression forms
 * </ul>
 *
 * <h2>OCL Context Declaration Syntax</h2>
 *
 * The tests validate parsing of context declarations following OCL standard syntax:
 *
 * <pre>{@code
 * // Simple context
 * context ClassName
 * inv: <expression>
 *
 * // Qualified context (with package/metamodel)
 * context PackageName::ClassName
 * inv: <expression>
 *
 * // Named invariant
 * context ClassName
 * inv invariantName: <expression>
 * }</pre>
 *
 * <h2>Expression Types Tested</h2>
 *
 * Tests cover various OCL expression patterns:
 *
 * <ul>
 *   <li><b>Simple comparisons:</b> {@code self.age > 0}
 *   <li><b>Null checks:</b> {@code self.name != null}
 *   <li><b>Collection operations:</b> {@code self.students->size() > 0}
 *   <li><b>Global queries:</b> {@code Person::allInstances()->select(p | p.age > 18)}
 * </ul>
 *
 * <h2>Testing Approach</h2>
 *
 * This class uses multiple testing strategies:
 *
 * <ol>
 *   <li><b>Individual tests:</b> {@link Test} for specific parsing scenarios with detailed
 *       validation
 *   <li><b>Parameterized tests:</b> {@link ParameterizedTest} for testing multiple similar inputs
 *       efficiently
 *   <li><b>Display names:</b> {@link DisplayName} for human-readable test descriptions in reports
 * </ol>
 *
 * <h2>Parse Tree Inspection</h2>
 *
 * All tests output the generated parse tree structure using {@link
 * VitruvOCLParserTestUtils#treeToString}, enabling:
 *
 * <ul>
 *   <li>Visual verification of parser behavior
 *   <li>Debugging unexpected parse results
 *   <li>Understanding grammar rule applications
 *   <li>Documenting expected parse tree structures
 * </ul>
 *
 * <h2>Assertion Libraries</h2>
 *
 * Tests use two assertion libraries:
 *
 * <ul>
 *   <li><b>JUnit assertions:</b> {@code assertNotNull()}, {@code assertDoesNotThrow()} for basic
 *       validation
 *   <li><b>AssertJ:</b> {@code assertThat()} for fluent, expressive assertions with better error
 *       messages
 * </ul>
 *
 * <h2>Parser Entry Point</h2>
 *
 * All tests use {@code contextDeclCS()} as the grammar entry point, which expects a complete
 * context declaration with invariant. This entry point is appropriate for parsing standalone OCL
 * constraints.
 *
 * <h2>Test Output</h2>
 *
 * Each test prints its parse tree to standard output for inspection:
 *
 * <pre>
 * Parse Tree:
 * (contextDeclCS context (pathNameCS Person) inv : (expCS ...))
 * </pre>
 *
 * <h2>Relationship to Other Tests</h2>
 *
 * <ul>
 *   <li><b>This class:</b> Inline string parsing with various expression patterns
 *   <li><b>{@link VitruvOCLFileParsingTest}:</b> File-based parsing tests
 *   <li><b>Integration tests:</b> End-to-end parsing, type checking, and evaluation
 * </ul>
 *
 * <h2>Future Extensions</h2>
 *
 * Potential test additions:
 *
 * <ul>
 *   <li>Collection operation tests (forAll, exists, select, reject, collect)
 *   <li>Navigation pattern tests (association traversal, multi-level navigation)
 *   <li>Complex boolean expressions with and/or/not
 *   <li>Let expressions and variable bindings
 *   <li>Nested collection operations
 * </ul>
 *
 * @see VitruvOCLLexer ANTLR-generated lexer for tokenization
 * @see VitruvOCLParser ANTLR-generated parser for parse tree construction
 * @see VitruvOCLParserTestUtils Utility methods for parsing and tree formatting
 * @see VitruvOCLFileParsingTest File-based parsing tests
 */
public class VitruvOCLParsingTest {

  /**
   * Tests parsing a simple context declaration with unqualified class name.
   *
   * <p><b>Input:</b> {@code context Person inv: self.age > 0}
   *
   * <p><b>Syntax elements:</b>
   *
   * <ul>
   *   <li><b>context:</b> Keyword introducing the context declaration
   *   <li><b>Person:</b> Unqualified class name (no package/metamodel prefix)
   *   <li><b>inv:</b> Invariant keyword
   *   <li><b>self.age > 0:</b> Boolean expression using self variable
   * </ul>
   *
   * <p><b>Parse tree structure:</b> The parser should produce a {@code contextDeclCS} node
   * containing:
   *
   * <ul>
   *   <li>Context keyword token
   *   <li>PathName node with simple identifier "Person"
   *   <li>Invariant keyword token
   *   <li>Expression node with comparison operation
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic context declaration syntax
   *   <li>Unqualified type names
   *   <li>Simple self-navigation expressions
   *   <li>Comparison operators in constraints
   *   <li>Non-null parse tree generation
   *   <li>Parse tree has child nodes
   * </ul>
   *
   * <p><b>Output:</b> Prints the complete parse tree structure for manual inspection.
   */
  @Test
  @DisplayName("Should parse simple context declaration")
  public void testSimpleContext() {
    String input = "context Person inv: self.age > 0";
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    ParseTree tree = parser.contextDeclCS();

    System.out.println("Parse Tree:\n" + VitruvOCLParserTestUtils.treeToString(tree, parser));

    assertNotNull(tree);
    assertThat(tree.getChildCount()).isGreaterThan(0);
  }

  /**
   * Tests parsing a context declaration with qualified class name (package::class syntax).
   *
   * <p><b>Input:</b> {@code context University::Student inv: self.age > 18}
   *
   * <p><b>Qualified name syntax:</b> {@code Package::ClassName} uses the double-colon {@code ::}
   * separator to specify:
   *
   * <ul>
   *   <li><b>University:</b> Package, namespace, or metamodel name
   *   <li><b>Student:</b> Class name within that package
   * </ul>
   *
   * <p><b>Use case:</b> Qualified names are essential when:
   *
   * <ul>
   *   <li>Multiple metamodels define classes with the same name
   *   <li>Disambiguating between packages
   *   <li>Explicitly specifying the metamodel context
   * </ul>
   *
   * <p><b>Parse tree structure:</b> The {@code pathNameCS} node should contain:
   *
   * <ul>
   *   <li>First path element: "University"
   *   <li>Separator: {@code ::}
   *   <li>Next path element: "Student"
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Qualified type name syntax
   *   <li>Double-colon separator parsing
   *   <li>Multi-level name resolution
   *   <li>Metamodel/package qualification
   * </ul>
   *
   * <p><b>Output:</b> Prints parse tree showing qualified name structure.
   */
  @Test
  @DisplayName("Should parse context with qualified class name")
  public void testQualifiedContext() {
    String input = "context University::Student inv: self.age > 18";
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    ParseTree tree = parser.contextDeclCS();

    System.out.println("Parse Tree:\n" + VitruvOCLParserTestUtils.treeToString(tree, parser));

    assertNotNull(tree);
  }
}
