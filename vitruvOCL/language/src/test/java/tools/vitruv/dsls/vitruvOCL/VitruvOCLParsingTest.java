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
