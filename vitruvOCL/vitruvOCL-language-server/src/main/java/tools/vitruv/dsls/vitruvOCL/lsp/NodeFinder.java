/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.lsp;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Locates the deepest parse-tree node that covers a given LSP cursor position.
 *
 * <p>ANTLR positions are 1-based lines / 0-based characters; LSP positions are 0-based for both.
 * The conversion {@code antlrLine - 1 == lspLine} is applied throughout this class.
 */
final class NodeFinder {

  private NodeFinder() {}

  /**
   * Returns the deepest {@link ParseTree} node that spans {@code (lspLine, lspChar)}, or {@code
   * null} if no node covers that position.
   *
   * @param tree root of the parse tree to search
   * @param lspLine 0-based line number from LSP HoverParams / CompletionParams
   * @param lspChar 0-based character offset within the line
   */
  @SuppressWarnings("java:S3776")
  static ParseTree findAt(ParseTree tree, int lspLine, int lspChar) {
    if (tree == null) return null;

    if (tree instanceof TerminalNode terminal) {
      Token token = terminal.getSymbol();
      if (token == null || token.getType() == Token.EOF) return null;

      int tokenLine = token.getLine() - 1; // 0-based
      int tokenStart = token.getCharPositionInLine();
      int tokenEnd = tokenStart + token.getText().length();

      if (tokenLine == lspLine && tokenStart <= lspChar && lspChar < tokenEnd) {
        return terminal;
      }
      return null;
    }

    if (tree instanceof ParserRuleContext ctx) {
      if (ctx.start == null) return null;

      int startLine = ctx.start.getLine() - 1;
      int startChar = ctx.start.getCharPositionInLine();
      int endLine = ctx.stop != null ? ctx.stop.getLine() - 1 : startLine;
      int endChar =
          ctx.stop != null
              ? ctx.stop.getCharPositionInLine() + ctx.stop.getText().length()
              : startChar + 1;

      // Quick range check before recursing.
      if (lspLine < startLine) return null;
      if (lspLine > endLine) return null;
      if (lspLine == startLine && lspChar < startChar) return null;
      if (lspLine == endLine && lspChar >= endChar) return null;

      // Prefer the deepest child match.
      for (int i = 0; i < ctx.getChildCount(); i++) {
        ParseTree found = findAt(ctx.getChild(i), lspLine, lspChar);
        if (found != null) return found;
      }

      // Cursor is within this rule but no child matched (e.g. whitespace between tokens).
      return ctx;
    }

    return null;
  }
}


