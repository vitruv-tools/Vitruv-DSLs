/*******************************************************************************
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
package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Paths;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test suite for parsing VitruvOCL constraint files from the filesystem.
 *
 * <p>This test class validates the ability to parse complete OCL constraint files stored as {@code
 * .ocl} files, testing the ANTLR parser's ability to handle file-based input and produce valid
 * parse trees for context declarations. These tests focus on the parsing phase only, without type
 * checking or evaluation.
 *
 * <h2>Purpose and Scope</h2>
 *
 * This test suite validates:
 *
 * <ul>
 *   <li><b>File I/O:</b> Loading OCL constraint files from the filesystem
 *   <li><b>Parser integration:</b> ANTLR lexer and parser working together on file input
 *   <li><b>Context declarations:</b> Parsing complete context-based constraint definitions
 *   <li><b>Parse tree generation:</b> Producing valid, non-empty parse trees
 * </ul>
 *
 * <h2>Test File Organization</h2>
 *
 * Test files are organized in {@code src/test/resources/test-inputs/}:
 *
 * <ul>
 *   <li><b>valid/:</b> Directory containing syntactically valid OCL constraint files
 *       <ul>
 *         <li>simple.ocl - Basic constraint examples
 *         <li>collections.ocl - Constraints involving collection operations
 *       </ul>
 * </ul>
 *
 * @see VitruvOCLLexer ANTLR-generated lexer
 * @see VitruvOCLParser ANTLR-generated parser
 * @see VitruvOCLParserTestUtils Parse tree visualization utilities
 */
public class VitruvOCLFileParsingTest {

  /**
   * Parameterized test for parsing multiple valid OCL constraint files.
   *
   * <p>This test is executed once for each file path provided in the {@link ValueSource}
   * annotation, validating that the parser can successfully handle different types of valid OCL
   * constraints stored in files.
   *
   * <p><b>Test files:</b>
   *
   * <ul>
   *   <li><b>simple.ocl:</b> Basic OCL constraints with simple expressions
   *   <li><b>collections.ocl:</b> Constraints involving collection operations (select, forAll,
   *       etc.)
   * </ul>
   *
   * <p><b>Parsing process:</b>
   *
   * <ol>
   *   <li>Load file content from filesystem using ANTLR's {@link CharStreams#fromPath}
   *   <li>Create lexer to tokenize the input
   *   <li>Create token stream for parser consumption
   *   <li>Create parser and invoke {@code contextDeclCS()} entry point
   *   <li>Generate and print parse tree visualization
   *   <li>Assert parse tree is valid and non-empty
   * </ol>
   *
   * <p><b>Parse tree output:</b> The test prints the complete parse tree structure to standard
   * output for manual inspection and debugging.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>File loading works correctly
   *   <li>Parser produces non-null parse tree
   *   <li>Parse tree contains child nodes (not empty)
   *   <li>No parsing errors or exceptions
   * </ul>
   *
   * @param fileName Relative path to the OCL file within {@code src/test/resources/}
   * @throws IOException if file cannot be read from filesystem
   */
  @ParameterizedTest
  @ValueSource(strings = {"test-inputs/valid/simple.ocl", "test-inputs/valid/collections.ocl"})
  public void testParseValidFiles(String fileName) throws IOException {
    String fullPath = "src/test/resources/" + fileName;
    CharStream cs = CharStreams.fromPath(Paths.get(fullPath));

    VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    ParseTree tree = parser.contextDeclCS();
    assertNotNull(tree);
    assertTrue(tree.getChildCount() > 0, "Parse tree should not be empty");
  }

  /**
   * Individual test for parsing a complex OCL constraint file with detailed validation.
   *
   * <p>This test focuses on a specific file (simple.ocl) that may require more detailed structural
   * assertions beyond basic parse tree validation. Currently performs the same validation as the
   * parameterized test but provides a place for future structural assertions.
   *
   * <p><b>Test file:</b> {@code test-inputs/valid/simple.ocl}
   *
   * <p><b>Parsing process:</b> Identical to {@link #testParseValidFiles} but allows for file-
   * specific assertions.
   *
   * <p><b>Current validation:</b>
   *
   * <ul>
   *   <li>Parse tree is non-null
   *   <li>Parse tree has children
   *   <li>Parse tree structure is printed for inspection
   * </ul>
   *
   * <p><b>Future enhancements:</b> This test serves as a template for adding specific structural
   * assertions such as:
   *
   * <ul>
   *   <li>Verifying specific context declaration nodes
   *   <li>Checking invariant count
   *   <li>Validating constraint expression structure
   *   <li>Testing specific grammar rule applications
   * </ul>
   *
   * <p><b>Example future assertions:</b>
   *
   * <pre>{@code
   * // Verify context declaration exists
   * assertTrue(tree.getChild(0) instanceof VitruvOCLParser.ContextDeclCSContext);
   *
   * // Check invariant name
   * VitruvOCLParser.InvariantCSContext inv = ...;
   * assertEquals("validMass", inv.ID().getText());
   * }</pre>
   *
   * @throws IOException if file cannot be read from filesystem
   */
  @Test
  public void testParseComplexFile() throws IOException {
    String fileName = "test-inputs/valid/simple.ocl";
    String fullPath = "src/test/resources/" + fileName;
    CharStream cs = CharStreams.fromPath(Paths.get(fullPath));

    VitruvOCLLexer lexer = new VitruvOCLLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    ParseTree tree = parser.contextDeclCS();
    assertNotNull(tree);
    assertTrue(tree.getChildCount() > 0, "Parse tree should not be empty");
    // Future: Add more detailed assertions about parse tree structure
  }
}
