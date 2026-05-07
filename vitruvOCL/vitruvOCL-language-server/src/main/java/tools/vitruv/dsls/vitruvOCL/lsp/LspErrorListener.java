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

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
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

  private final List<Diagnostic> diagnostics = new ArrayList<>();

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {

    // ANTLR: line is 1-based → LSP: 0-based.
    int lspLine = Math.max(0, line - 1);
    int lspChar = Math.max(0, charPositionInLine);

    Position start = new Position(lspLine, lspChar);
    Position end = new Position(lspLine, lspChar + 1);

    Diagnostic diag =
        new Diagnostic(new Range(start, end), msg, DiagnosticSeverity.Error, "vitruvOCL");
    diagnostics.add(diag);
    System.err.printf(
        "[OCL-LS] DIAG syntax-error   L%d:C%d → L%d:C%d  %s%n",
        lspLine, lspChar, lspLine, lspChar + 1, msg);
  }

  List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }
}


