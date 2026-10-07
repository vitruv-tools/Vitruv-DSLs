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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Provides LSP {@code textDocument/inlayHint} responses.
 *
 * <p>Three categories of hints are produced, each targeting a position where the user might not
 * immediately know the type without consulting the metamodel or the let-binding initialiser:
 *
 * <ul>
 *   <li><b>Let-variable types</b>: {@code let x = self.children} → {@code x: Set{JavaMM::Class}}
 *       whenever no explicit type annotation is present.
 *   <li><b>Iterator-variable types</b>: {@code select(x | ...)} → {@code x: JavaMM::Class} —
 *       derived by scanning the first usage of the variable in the iterator body.
 *   <li><b>Metaclass attribute types</b>: {@code self.children} → {@code children:
 *       Set{JavaMM::Class}} — shown only when the result type is a metaclass or collection type
 *       (primitive String/Integer/Boolean attributes are intentionally suppressed to avoid noise).
 * </ul>
 *
 * <p>All hints use {@link InlayHintKind#Type} and are filtered to the viewport range supplied by
 * the client so that large files do not produce an excessive number of hints.
 */
final class InlayHintProvider {

  /** Returns inlay hints visible within {@code range} for the given analysis snapshot. */
  List<InlayHint> getHints(DocumentAnalysis analysis, Range range) {
    if (analysis == null || analysis.getTree() == null || analysis.getNodeTypes() == null) {
      return List.of();
    }

    List<InlayHint> hints = new ArrayList<>();
    new Walker(analysis.getNodeTypes(), hints, range).visit(analysis.getTree());
    return hints;
  }

  // ---------------------------------------------------------------------------
  // Tree walker
  // ---------------------------------------------------------------------------

  private static final class Walker {

    private final ParseTreeProperty<Type> types;
    private final List<InlayHint> hints;
    private final Range range; // may be null → no filter

    Walker(ParseTreeProperty<Type> types, List<InlayHint> hints, Range range) {
      this.types = types;
      this.hints = hints;
      this.range = range;
    }

    void visit(ParseTree node) {
      if (node == null) {
        return;
      }

      // ── Let-variable type hint ──────────────────────────────────────────────
      if (node instanceof VitruvOCLParser.VariableDeclarationContext ctx) {
        handleVarDecl(ctx);
      }

      // ── Iterator-variable type hints ───────────────────────────────────────
      if (node instanceof VitruvOCLParser.SelectOpContext ctx) {
        handleIteratorVars(ctx.iteratorVars, ctx.body);
      }
      if (node instanceof VitruvOCLParser.RejectOpContext ctx) {
        handleIteratorVars(ctx.iteratorVars, ctx.body);
      }
      if (node instanceof VitruvOCLParser.CollectOpContext ctx) {
        handleIteratorVars(ctx.iteratorVars, ctx.body);
      }
      if (node instanceof VitruvOCLParser.ForAllOpContext ctx) {
        handleIteratorVars(ctx.iteratorVars, ctx.body);
      }
      if (node instanceof VitruvOCLParser.ExistsOpContext ctx) {
        handleIteratorVars(ctx.iteratorVars, ctx.body);
      }

      // ── Property-navigation type hint (metaclass attributes) ───────────────
      if (node instanceof VitruvOCLParser.PropertyNavContext ctx) {
        handlePropertyNav(ctx);
      }

      // Recurse
      for (int i = 0; i < node.getChildCount(); i++) {
        visit(node.getChild(i));
      }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private void handleVarDecl(VitruvOCLParser.VariableDeclarationContext ctx) {
      // Only hint when the user wrote no explicit type annotation.
      if (ctx.varType != null || ctx.varInit == null || ctx.varName == null) {
        return;
      }
      Type t = resolveExpType(ctx.varInit);
      if (t != null && !t.isError()) {
        addHint(ctx.varName, ": " + t.getTypeName());
      }
    }

    private void handleIteratorVars(
        VitruvOCLParser.IteratorVarListContext varList, VitruvOCLParser.ExpCSContext body) {
      if (varList == null || body == null) {
        return;
      }
      for (TerminalNode idNode : varList.ID()) {
        String name = idNode.getText();
        // Derive type from the first usage of the variable in the body.
        Type t = findFirstUsageType(body, name);
        if (t != null && !t.isError()) {
          addHint(idNode.getSymbol(), ": " + t.getTypeName());
        }
      }
    }

    private void handlePropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
      if (ctx.propertyAccess() == null) {
        return;
      }
      Token tok = ctx.propertyAccess().propertyName;
      if (tok == null) {
        return;
      }

      // Prefer annotation on the nav-target context; fall back to property-access child.
      Type t = types.get(ctx);
      if (t == null) {
        t = types.get(ctx.propertyAccess());
      }
      if (t == null || t.isError()) {
        return;
      }

      if (!t.isCollection() && !t.isMetaclassType() && !t.isOptional()) {
        return;
      }

      addHint(tok, ": " + t.getTypeName());
    }

    // ── Type resolution helpers ───────────────────────────────────────────────

    /**
     * Resolves the type of an {@code expCS} node. The expression tree has an extra level ({@code
     * expCS → infixedExpCS}) that the TypeCheckVisitor may annotate on either node.
     */
    private Type resolveExpType(VitruvOCLParser.ExpCSContext ctx) {
      if (ctx == null) {
        return null;
      }
      Type t = types.get(ctx);
      if (t != null) {
        return t;
      }
      // expCS: infixedExpCS — try the single child
      if (ctx.infixedExpCS() != null) {
        t = types.get(ctx.infixedExpCS());
      }
      return t;
    }

    /**
     * Finds the type of an iterator variable by scanning {@code node} for the first {@code
     * variableExpCS} (or its {@code variable} wrapper) whose name matches {@code varName}.
     *
     * <p>This is more reliable than annotating the {@code iteratorVarList} node directly because
     * the TypeCheckVisitor puts the variable in scope and annotates its usage sites.
     */
    @SuppressWarnings("java:S3776")
    private Type findFirstUsageType(ParseTree node, String varName) {
      if (node == null) {
        return null;
      }

      // variableExpCS: varName=ID
      if (node instanceof VitruvOCLParser.VariableExpCSContext ctx
          && ctx.varName != null
          && varName.equals(ctx.varName.getText())) {
        Type t = types.get(ctx);
        if (t != null && !t.isError()) {
          return t;
        }
      }
      // primaryExpCS alternative: # variable  →  wraps variableExpCS
      if (node instanceof VitruvOCLParser.VariableContext ctx
          && ctx.variableExpCS() != null
          && varName.equals(ctx.variableExpCS().varName.getText())) {
        Type t = types.get(ctx);
        if (t == null) {
          t = types.get(ctx.variableExpCS());
        }
        if (t != null && !t.isError()) {
          return t;
        }
      }

      for (int i = 0; i < node.getChildCount(); i++) {
        Type found = findFirstUsageType(node.getChild(i), varName);
        if (found != null) {
          return found;
        }
      }
      return null;
    }

    // ── Hint placement ────────────────────────────────────────────────────────

    private void addHint(Token tok, String label) {
      if (tok == null) {
        return;
      }
      // ANTLR line is 1-based; LSP is 0-based.
      int line = tok.getLine() - 1;
      int col = tok.getCharPositionInLine() + tok.getText().length();
      Position pos = new Position(line, col);
      if (!posInRange(pos)) {
        return;
      }

      InlayHint hint = new InlayHint(pos, Either.forLeft(label));
      hint.setKind(InlayHintKind.Type);
      hint.setPaddingLeft(false);
      hints.add(hint);
    }

    private boolean posInRange(Position pos) {
      if (range == null) {
        return true;
      }
      int line = pos.getLine();
      int col = pos.getCharacter();
      int sl = range.getStart().getLine();
      int sc = range.getStart().getCharacter();
      int el = range.getEnd().getLine();
      int ec = range.getEnd().getCharacter();
      if (line < sl || (line == sl && col < sc)) {
        return false;
      }
      return !(line > el || (line == el && col > ec));
    }
  }
}
