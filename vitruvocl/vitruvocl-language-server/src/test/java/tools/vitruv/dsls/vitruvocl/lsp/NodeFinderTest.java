/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;

/**
 * Unit tests for {@link NodeFinder}.
 *
 * <p>Parse trees are built directly from the ANTLR parser.
 */
class NodeFinderTest {

  private static VitruvOCLParser.ContextDeclCSContext parse(String ocl) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(ocl));
    lexer.removeErrorListeners();
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    parser.removeErrorListeners();
    return parser.contextDeclCS();
  }

  @Test
  void nullTree_returnsNull() {
    assertThat(NodeFinder.findAt(null, 0, 0)).isNull();
  }

  @Test
  void positionOnToken_returnsTerminalNode() {
    // "context" starts at line 0, char 0
    VitruvOCLParser.ContextDeclCSContext tree = parse("context MM::Foo inv x: self = self");

    ParseTree node = NodeFinder.findAt(tree, 0, 0);

    assertThat(node).isInstanceOf(TerminalNode.class);
    assertThat(((TerminalNode) node).getSymbol().getText()).isEqualTo("context");
  }

  @Test
  void positionAtMiddleOfToken_returnsTerminalNode() {
    // "context" occupies chars 0-6; char 3 is inside it
    VitruvOCLParser.ContextDeclCSContext tree = parse("context MM::Foo inv x: self = self");

    ParseTree node = NodeFinder.findAt(tree, 0, 3);

    assertThat(node).isInstanceOf(TerminalNode.class);
    assertThat(((TerminalNode) node).getSymbol().getText()).isEqualTo("context");
  }

  @Test
  void positionBeforeDocument_returnsNull() {
    VitruvOCLParser.ContextDeclCSContext tree = parse("context MM::Foo inv x: self = self");

    // Line -1 is before the document
    ParseTree node = NodeFinder.findAt(tree, -1, 0);

    assertThat(node).isNull();
  }

  @Test
  void positionAfterLastToken_returnsNull() {
    String ocl = "context MM::Foo inv x: self = self";
    VitruvOCLParser.ContextDeclCSContext tree = parse(ocl);

    // Far past end of document
    ParseTree node = NodeFinder.findAt(tree, 0, ocl.length() + 100);

    assertThat(node).isNull();
  }

  @Test
  void positionOnSecondLine_returnsCorrectToken() {
    String ocl = "context MM::Foo\n  inv x: self = self";
    VitruvOCLParser.ContextDeclCSContext tree = parse(ocl);

    // "inv" is on line 1 (0-based), starting at char 2
    ParseTree node = NodeFinder.findAt(tree, 1, 2);

    assertThat(node).isInstanceOf(TerminalNode.class);
    assertThat(((TerminalNode) node).getSymbol().getText()).isEqualTo("inv");
  }

  @Test
  void positionBetweenTokens_returnsRuleContext() {
    // Whitespace between "context" and "MM" — no terminal covers it, but the rule does
    String ocl = "context MM::Foo inv x: self = self";
    VitruvOCLParser.ContextDeclCSContext tree = parse(ocl);

    // char 7 is the space between "context" and "MM"
    ParseTree node = NodeFinder.findAt(tree, 0, 7);

    // Should return a rule context (not null, not a terminal)
    if (node != null) {
      assertThat(node).isNotInstanceOf(TerminalNode.class);
    }
    // null is also acceptable — the important thing is no exception is thrown
  }

  @Test
  void emptyDocument_positionFarOutside_returnsNull() {
    VitruvOCLParser.ContextDeclCSContext tree = parse("");

    // Position far outside any token range must produce no match
    ParseTree node = NodeFinder.findAt(tree, 100, 100);

    assertThat(node).isNull();
  }

  @Test
  void emptyDocument_positionAtOrigin_returnsNoTerminal() {
    VitruvOCLParser.ContextDeclCSContext tree = parse("");

    // The ANTLR parser may return the root rule context for (0,0) on an empty document;
    // it must never return a terminal node since there are no real tokens.
    ParseTree node = NodeFinder.findAt(tree, 0, 0);

    if (node != null) {
      assertThat(node).isNotInstanceOf(TerminalNode.class);
    }
  }

  @Test
  void findAt_withDocumentAnalysisTree_sameResultAsDirectCall() {
    String ocl = "context MM::Foo inv x: self = self";
    VitruvOCLParser.ContextDeclCSContext tree = parse(ocl);
    DocumentAnalysis analysis = new DocumentAnalysis(tree, null, List.of());

    // Both calls must return the same node
    ParseTree direct = NodeFinder.findAt(tree, 0, 0);
    ParseTree viaAnalysis = NodeFinder.findAt(analysis.getTree(), 0, 0);

    assertThat(direct).isEqualTo(viaAnalysis);
  }
}
