/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.lsp;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * ANTLR error listener that converts parser/lexer errors to LSP {@link Diagnostic} objects.
 *
 * <p>Installed on both the {@code VitruvOCLLexer} and {@code VitruvOCLParser} so that all syntax errors are
 * captured in one place. ANTLR reports 1-based lines; LSP uses 0-based — the conversion is applied
 * here.
 */
final class LspErrorListener extends BaseErrorListener {

  private static final Logger LOG = Logger.getLogger(LspErrorListener.class.getName());

  /** OCL structure keywords users might mistype — checked for "did you mean?" suggestions. */
  private static final List<String> OCL_KEYWORDS = List.of(
      "context", "inv", "self", "implies", "and", "or", "xor", "not",
      "if", "then", "else", "endif", "let", "in", "null", "true", "false"
  );

  private final List<Diagnostic> diagnostics = new ArrayList<>();

  @Override
  @SuppressWarnings("java:S3776")
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {

    // ANTLR: line is 1-based → LSP: 0-based.
    int lspLine = Math.max(0, line - 1);
    int lspStart = Math.max(0, charPositionInLine);

    // Try to produce a better message when the offending token looks like a keyword typo.
    String suggestion = null;
    int lspEnd = lspStart + 1;

    if (offendingSymbol instanceof Token tok) {
      String text = tok.getText();
      lspEnd = lspStart + Math.max(1, text.length());

      if (!text.equals("<EOF>") && text.chars().allMatch(Character::isLetterOrDigit)) {
        String lower = text.toLowerCase(Locale.ROOT);
        String best = OCL_KEYWORDS.stream()
            .min(java.util.Comparator.comparingInt(k -> EditDistance.damerauLevenshtein(lower, k)))
            .orElse(null);
        int dist = best == null ? Integer.MAX_VALUE : EditDistance.damerauLevenshtein(lower, best);
        int threshold = EditDistance.editThreshold(text.length());

        if (dist <= threshold) {
          msg = "Unknown keyword '" + text + "' — did you mean '" + best + "'?";
          suggestion = best;
        }
      }
    }

    Position start = new Position(lspLine, lspStart);
    Position end   = new Position(lspLine, lspEnd);

    Diagnostic diag =
        new Diagnostic(new Range(start, end), msg, DiagnosticSeverity.Error, "vitruvOCL");
    if (suggestion != null) {
      diag.setData(suggestion); // enables Quick Fix replacement in OCLTextDocumentService
    }
    diagnostics.add(diag);
    final int capLine = lspLine;
    final int capStart = lspStart;
    final int capEnd = lspEnd;
    final String capMsg = msg;
    LOG.fine(() -> String.format(
        "[OCL-LS] DIAG syntax-error   L%d:C%d → L%d:C%d  %s",
        capLine, capStart, capLine, capEnd, capMsg));
  }

  List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }
}


