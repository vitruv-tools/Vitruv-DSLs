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

import java.util.List;
import java.util.Optional;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Provides LSP {@code textDocument/signatureHelp} responses.
 *
 * <p>When the user types {@code (} or {@code ,} inside an OCL# expression the language server calls
 * this provider. It scans backward from the cursor to find the innermost active call, looks up the
 * operation name in {@link OclOperationDocs}, and returns a {@link SignatureHelp} with the correct
 * active parameter highlighted.
 *
 * <p>Two parameter-counting strategies are used depending on the operation:
 *
 * <ul>
 *   <li><b>Iterator operations</b> (signature contains {@code |}): the pipe character separates the
 *       iterator variable from the body expression. Cursor before {@code |} → first param
 *       highlighted; cursor after {@code |} → second param highlighted.
 *   <li><b>Regular operations</b> (e.g. {@code substring(lower, upper)}): commas at depth 0 between
 *       the opening {@code (} and the cursor determine the active parameter index.
 * </ul>
 */
final class SignatureHelpProvider {

  /** Scans the document and returns a {@link SignatureHelp}, or {@code null} if not applicable. */
  SignatureHelp getSignatureHelp(String documentText, Position cursor) {
    if  (documentText == null) {
      return null;
    }

    String prefix = textUpToPosition(documentText, cursor);
    ActiveCall call = findActiveCall(prefix);
    if  (call == null) {
      return null;
    }

    Optional<OclOperationDocs.OperationDoc> docOpt = OclOperationDocs.lookup(call.name());
    if  (docOpt.isEmpty()) {
      return null;
    }

    OclOperationDocs.OperationDoc doc = docOpt.get();
    SignatureInformation sig = buildSignature(doc);

    int activeParam = computeActiveParam(call, doc);

    SignatureHelp help = new SignatureHelp();
    help.setSignatures(List.of(sig));
    help.setActiveSignature(0);
    help.setActiveParameter(activeParam);
    return help;
  }

  // ---------------------------------------------------------------------------
  // Signature construction
  // ---------------------------------------------------------------------------

  private static SignatureInformation buildSignature(OclOperationDocs.OperationDoc doc) {
    SignatureInformation sig = new SignatureInformation(doc.signature());

    // Each ParamDoc becomes a ParameterInformation whose label is "name: type".
    // The label must be a substring of the full signature label so VS Code can
    // highlight the correct region when the parameter is active.
    List<ParameterInformation> paramInfos =
        doc.params().stream()
            .map(
                p -> {
                  ParameterInformation pi = new ParameterInformation();
                  pi.setLabel(Either.forLeft(p.name() + ": " + p.type()));
                  if (!p.description().isEmpty()) {
                    pi.setDocumentation(Either.forLeft(p.description()));
                  }
                  return pi;
                })
            .toList();
    sig.setParameters(paramInfos);

    // Compose Markdown documentation: description + @return line.
    String mdText =
        doc.description()
            + (doc.returnDescription().isEmpty()
                ? ""
                : "\n\n**Returns:** " + doc.returnDescription());
    MarkupContent mdContent = new MarkupContent(MarkupKind.MARKDOWN, mdText);
    sig.setDocumentation(Either.forRight(mdContent));

    return sig;
  }

  // ---------------------------------------------------------------------------
  // Active-parameter resolution
  // ---------------------------------------------------------------------------

  private static int computeActiveParam(ActiveCall call, OclOperationDocs.OperationDoc doc) {
    List<OclOperationDocs.ParamDoc> params = doc.params();
    if  (params.isEmpty()) {
      return 0;
    }

    // Iterator operations (signature contains |): pipe position determines active param.
    if (doc.signature().contains("|") && params.size() >= 2) {
      return call.cursorAfterPipe() ? 1 : 0;
    }

    // Regular multi-param operations: count commas between ( and cursor.
    return Math.min(call.commas(), params.size() - 1);
  }

  // ---------------------------------------------------------------------------
  // Call detection — backward scan from cursor
  // ---------------------------------------------------------------------------

  /**
   * Scans {@code prefix} (text from document start up to cursor) backwards to find the innermost
   * unmatched {@code (}. Returns the operation name and context needed to choose the active
   * parameter, or {@code null} if the cursor is not inside a recognised call.
   */
  @SuppressWarnings("java:S3776")
  private static ActiveCall findActiveCall(String prefix) {
    int depth = 0; // nesting depth relative to innermost (
    int commas = 0; // commas at depth 0 (between the active ( and cursor)
    boolean cursorAfterPipe = false;

    for (int i = prefix.length() - 1; i >= 0; i--) {
      char c = prefix.charAt(i);

      if (c == ')') {
        depth++;
      } else if (c == '(') {
        if (depth > 0) {
          depth--;
        } else {
          // Found the opening paren — extract the identifier immediately before it.
          // Skip any whitespace, then grab letters/digits/underscores.
          int end = i;
          while  (end > 0 && Character.isWhitespace(prefix.charAt(end - 1))) {
            end--;
          }
          int start = end;
          while  (start > 0 && isIdentChar(prefix.charAt(start - 1))) {
            start--;
          }

          if (start < end) {
            return new ActiveCall(prefix.substring(start, end), commas, cursorAfterPipe);
          }
          return null; // ( not preceded by an identifier (e.g. grouping paren)
        }
      } else if (depth == 0) {
        // Track items between the active ( and the cursor.
        if (c == ',') {
          commas++;
        } else if (c == '|') {
          // Scanning backwards: we encounter | before we reach the (.
          // That means the cursor position was to the right of |, i.e. in the body.
          cursorAfterPipe = true;
        }
      }
    }
    return null; // No unmatched ( found
  }

  private static boolean isIdentChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  // ---------------------------------------------------------------------------
  // Text slicing
  // ---------------------------------------------------------------------------

  /** Returns the document text from the start up to (not including) the cursor position. */
  private static String textUpToPosition(String text, Position cursor) {
    String[] lines = text.split("\n", -1);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cursor.getLine() && i < lines.length; i++) {
      sb.append(lines[i]).append('\n');
    }
    if (cursor.getLine() < lines.length) {
      String line = lines[cursor.getLine()];
      int col = Math.min(cursor.getCharacter(), line.length());
      sb.append(line, 0, col);
    }
    return sb.toString();
  }

  // ---------------------------------------------------------------------------
  // Data record
  // ---------------------------------------------------------------------------

  /**
   * Represents the active call context found by the backward scan.
   *
   * @param name identifier immediately before the opening {@code (}
   * @param commas number of top-level commas between {@code (} and cursor
   * @param cursorAfterPipe {@code true} when a top-level {@code |} was found between {@code (} and
   *     the cursor (iterator body region)
   */
  private record ActiveCall(String name, int commas, boolean cursorAfterPipe) {}
}
