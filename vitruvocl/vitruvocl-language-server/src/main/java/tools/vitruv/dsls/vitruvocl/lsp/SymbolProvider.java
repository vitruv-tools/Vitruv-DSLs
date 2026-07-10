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

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;

/**
 * Produces document symbols (outline view) from a parsed OCL# document.
 *
 * <p>Each {@code context MM::ClassName} block becomes a top-level {@link SymbolKind#Class} entry.
 * Named invariants inside it ({@code inv ConstraintName:}) become {@link SymbolKind#Property}
 * children. Anonymous invariants appear as {@code <anonymous>}.
 */
public class SymbolProvider {

  /**
   * Builds the full symbol tree for the given analysis.
   *
   * @return list of top-level symbols (each may have children); empty if nothing is parsed
   */
  public List<Either<SymbolInformation, DocumentSymbol>> getSymbols(DocumentAnalysis analysis) {
    if  (analysis == null || analysis.getTree() == null) {
      return List.of();
    }

    VitruvOCLParser.ContextDeclCSContext root = analysis.getTree();
    List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();

    // Use the typed ANTLR accessor — avoids manually walking mixed child lists
    for (VitruvOCLParser.ClassifierContextCSContext ctx : root.classifierContextCS()) {
      result.add(Either.forRight(buildContextSymbol(ctx)));
    }

    return result;
  }

  // ---------------------------------------------------------------------------

  private static DocumentSymbol buildContextSymbol(VitruvOCLParser.ClassifierContextCSContext ctx) {
    String name;
    if (ctx.metamodel != null && ctx.className != null) {
      name = ctx.metamodel.getText() + "::" + ctx.className.getText();
    } else if (ctx.contextName != null) {
      name = ctx.contextName.getText();
    } else {
      name = "<context>";
    }

    Range full = ruleRange(ctx);
    Range selection = nameSelectionRange(ctx);

    DocumentSymbol symbol = new DocumentSymbol(name, SymbolKind.Class, full, selection);

    // Use typed accessor for invCS children
    List<DocumentSymbol> children = new ArrayList<>();
    for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
      children.add(buildInvSymbol(inv));
    }
    if (!children.isEmpty()) {
      symbol.setChildren(children);
    }

    return symbol;
  }

  private static DocumentSymbol buildInvSymbol(VitruvOCLParser.InvCSContext ctx) {
    String name = ctx.ID() != null ? ctx.ID().getText() : "<anonymous>";
    Range range = ruleRange(ctx);
    return new DocumentSymbol(name, SymbolKind.Property, range, range);
  }

  // ---------------------------------------------------------------------------
  // Range helpers
  // ---------------------------------------------------------------------------

  private static Range ruleRange(ParserRuleContext ctx) {
    Token start = ctx.getStart();
    Token stop = ctx.getStop();
    if  (start == null) {
      return zero();
    }
    int startLine = Math.max(0, start.getLine() - 1);
    int startChar = Math.max(0, start.getCharPositionInLine());
    int stopLine = stop != null ? Math.max(0, stop.getLine() - 1) : startLine;
    int stopChar =
        stop != null
            ? Math.max(0, stop.getCharPositionInLine() + stop.getText().length())
            : startChar;
    return new Range(new Position(startLine, startChar), new Position(stopLine, stopChar));
  }

  /** Selection range highlights just the class/context name token(s). */
  private static Range nameSelectionRange(VitruvOCLParser.ClassifierContextCSContext ctx) {
    if (ctx.metamodel != null && ctx.className != null) {
      int startLine = Math.max(0, ctx.metamodel.getLine() - 1);
      int startChar = Math.max(0, ctx.metamodel.getCharPositionInLine());
      int stopLine = Math.max(0, ctx.className.getLine() - 1);
      int stopChar =
          Math.max(0, ctx.className.getCharPositionInLine() + ctx.className.getText().length());
      return new Range(new Position(startLine, startChar), new Position(stopLine, stopChar));
    }
    if (ctx.contextName != null) {
      Token t = ctx.contextName;
      int line = Math.max(0, t.getLine() - 1);
      int col = Math.max(0, t.getCharPositionInLine());
      return new Range(new Position(line, col), new Position(line, col + t.getText().length()));
    }
    return ruleRange(ctx);
  }

  private static Range zero() {
    return new Range(new Position(0, 0), new Position(0, 0));
  }
}
