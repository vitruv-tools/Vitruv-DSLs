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
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Runs the 2-pass analysis pipeline (symbol-table construction + type checking) on the current
 * document text and produces a {@link DocumentAnalysis}.
 *
 * <p>The evaluation pass (Pass 3) is intentionally omitted — the language server performs static
 * analysis only; constraint evaluation is handled separately by the CLI runner.
 *
 * <p>ANTLR's built-in error recovery ensures that a parse tree is always produced, even for
 * syntactically broken documents. Both passes are always attempted so that type-error diagnostics
 * are emitted even when the document has syntax errors.
 */
public class DocumentAnalyzer {

  private static final Logger LOG = Logger.getLogger(DocumentAnalyzer.class.getName());

  private static final String LANGUAGE_ID = "vitruvOCL";

  private final MetamodelWrapper wrapper;

  public DocumentAnalyzer(MetamodelWrapper wrapper) {
    this.wrapper = wrapper;
  }

  /**
   * Analyzes {@code documentText} and returns a fresh {@link DocumentAnalysis}.
   *
   * <p>Never throws — all exceptions are swallowed and reported as an internal-error diagnostic so
   * that the language server remains stable even when the document is in an extreme broken state.
   */
  /* Strips import declaration lines, preserving line numbers by replacing content with a comment. */
  private static String stripImportLines(String text) {
    StringBuilder sb = new StringBuilder(text.length());
    for (String line : text.split("\n", -1)) {
      String trimmed = line.stripLeading();
      if (trimmed.startsWith("import ")) {
        // Replace with a blank comment of the same length so line numbers are preserved
        sb.append("--").append(" ".repeat(Math.max(0, line.length() - 2)));
      } else {
        sb.append(line);
      }
      sb.append('\n');
    }
    // Remove the trailing '\n' added for the last line if original didn't have it
    if (!text.endsWith("\n") && !sb.isEmpty()) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  @SuppressWarnings("java:S1141")
  public DocumentAnalysis analyze(String documentText) {
    List<Diagnostic> diagnostics = new ArrayList<>();
    VitruvOCLParser.ContextDeclCSContext tree = null;
    ParseTreeProperty<Type> nodeTypes = null;

    // Strip import declarations before parsing — the VitruvOCL grammar has no import rule.
    // We replace them with blank comments to preserve line numbers for diagnostics.
    documentText = stripImportLines(documentText);

    // Pre-parse scan: flag @keyword typos with quick-fix suggestions before ANTLR runs.
    checkAnnotationKeywordTypos(documentText, diagnostics);

    try {
      // -----------------------------------------------------------------------
      // Lexer + Parser
      // -----------------------------------------------------------------------
      LspErrorListener errorListener = new LspErrorListener();

      VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(documentText));
      lexer.removeErrorListeners();
      lexer.addErrorListener(errorListener);

      CommonTokenStream tokens = new CommonTokenStream(lexer);

      VitruvOCLParser parser = new VitruvOCLParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(errorListener);
      parser.setErrorHandler(new OclErrorStrategy());

      tree = parser.contextDeclCS();
      List<Diagnostic> syntaxDiags = errorListener.getDiagnostics();
      diagnostics.addAll(syntaxDiags);

      // -----------------------------------------------------------------------
      // Pass 1 – Symbol Table Construction
      // -----------------------------------------------------------------------
      ErrorCollector errors = new ErrorCollector();
      SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
      ScopeAnnotator scopeAnnotator = new ScopeAnnotator();

      try {
        SymbolTableBuilder builder =
            new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
        builder.visit(tree);
      } catch (Exception e) {
        LOG.fine("[OCL-LS] SymbolTableBuilder error: " + e.getMessage());
      }

      // -----------------------------------------------------------------------
      // Pass 2 – Type Checking (always run for hover/completion even with errors)
      // -----------------------------------------------------------------------
      try {
        TypeCheckVisitor typeChecker =
            new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
        typeChecker.setTokenStream(tokens);
        typeChecker.visit(tree);
        nodeTypes = typeChecker.getNodeTypes();
      } catch (Exception e) {
        LOG.fine("[OCL-LS] TypeCheckVisitor error: " + e.getMessage());
      }

      // -----------------------------------------------------------------------
      // Convert ErrorCollector entries to LSP Diagnostics
      // Type-checker errors for those orphaned nodes are suppressed to prevent
      // squiggles bleeding into the previous constraint's invariant body.
      // -----------------------------------------------------------------------
      List<int[]> orphanedRanges = findOrphanedInvRanges(tree, syntaxDiags);
      for (CompileError error : errors.getErrors()) {
        int errLine = Math.max(0, error.getLine() - 1); // 0-based
        boolean orphaned = false;
        for (int[] r : orphanedRanges) {
          if (errLine >= r[0] && errLine <= r[1]) {
            orphaned = true;
            break;
          }
        }
        if (orphaned) continue;

        Diagnostic d = toDiagnostic(error);
        diagnostics.add(d);
        LOG.fine(String.format(
            "[OCL-LS] DIAG type-checker  L%d:C%d → L%d:C%d  %s",
            d.getRange().getStart().getLine(),
            d.getRange().getStart().getCharacter(),
            d.getRange().getEnd().getLine(),
            d.getRange().getEnd().getCharacter(),
            d.getMessage()));
      }

    } catch (Exception e) {
      // Catch-all — return empty analysis with a single internal-error diagnostic.
      LOG.fine("[OCL-LS] Unexpected analysis error: " + e.getMessage());
      diagnostics.add(
          new Diagnostic(
              new Range(new Position(0, 0), new Position(0, 1)),
              "Internal language server error: " + e.getMessage(),
              DiagnosticSeverity.Error,
              LANGUAGE_ID));
    }

    return new DocumentAnalysis(tree, nodeTypes, diagnostics);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  // ---------------------------------------------------------------------------
  // Annotation keyword typo detection
  // ---------------------------------------------------------------------------

  private static final Pattern AT_WORD = Pattern.compile("@(\\w+)");
  private static final List<String> ANNOTATION_KEYWORDS = List.of("severity", "message");

  /**
   * Scans {@code text} for {@code @word} tokens that look like misspellings of
   * {@code @severity} or {@code @message} and appends a diagnostic with a quick-fix suggestion.
   */
  @SuppressWarnings("java:S3776")
  private static void checkAnnotationKeywordTypos(String text, List<Diagnostic> diagnostics) {
    String[] lines = text.split("\n", -1);
    for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
      Matcher m = AT_WORD.matcher(lines[lineIdx]);
      while (m.find()) {
        String word = m.group(1);
        // Exact match — valid annotation keyword, skip.
        if (ANNOTATION_KEYWORDS.contains(word)) continue;

        // Find the closest known annotation keyword.
        String bestKeyword = null;
        int bestDist = Integer.MAX_VALUE;
        for (String kw : ANNOTATION_KEYWORDS) {
          int dist = damerauLevenshtein(word.toLowerCase(), kw);
          if (dist < bestDist) { bestDist = dist; bestKeyword = kw; }
        }

        // Only flag if within edit distance 3 (generous — covers @Severity, @severit, @sev, @msg).
        int threshold = editThreshold(word.length());
        if (bestKeyword == null || bestDist > threshold) continue;

        int startCol = m.start(); // position of '@'
        int endCol   = m.end();
        String suggestion = "@" + bestKeyword;
        String badAnnotation = "@" + word;

        Diagnostic d = new Diagnostic(
            new Range(new Position(lineIdx, startCol), new Position(lineIdx, endCol)),
            "Unknown annotation '" + badAnnotation + "'. Did you mean '" + suggestion + "'?",
            DiagnosticSeverity.Error,
            LANGUAGE_ID);
        d.setData(suggestion); // picked up by the code-action handler for the quick fix
        diagnostics.add(d);
        LOG.fine(String.format(
            "[OCL-LS] DIAG annotation-typo L%d:C%d  %s → %s",
            lineIdx, startCol, badAnnotation, suggestion));
      }
    }
  }

  private static int editThreshold(int len) {
    if (len <= 3) return 1;
    if (len <= 6) return 2;
    return 3;
  }

  private static int damerauLevenshtein(String a, String b) {
    if (a.equals(b)) return 0;
    if (a.isEmpty()) return b.length();
    if (b.isEmpty()) return a.length();
    int la = a.length(), lb = b.length();
    int[][] d = new int[la + 1][lb + 1];
    for (int i = 0; i <= la; i++) d[i][0] = i;
    for (int j = 0; j <= lb; j++) d[0][j] = j;
    for (int i = 1; i <= la; i++) {
      for (int j = 1; j <= lb; j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(d[i-1][j] + 1, Math.min(d[i][j-1] + 1, d[i-1][j-1] + cost));
        if (i > 1 && j > 1
            && a.charAt(i-1) == b.charAt(j-2)
            && a.charAt(i-2) == b.charAt(j-1)) {
          d[i][j] = Math.min(d[i][j], d[i-2][j-2] + cost);
        }
      }
    }
    return d[la][lb];
  }

  /**
   * Returns line ranges (0-based, inclusive) of {@code invCS} nodes that are almost certainly
   * mis-parented due to ANTLR error recovery consuming tokens from the next context block.
   *
   * <p>Heuristic: if a syntax error falls inside a {@code classifierContextCS}'s token range, any
   * {@code invCS} child whose start line is <em>after</em> that syntax error is considered orphaned
   * — it was absorbed by error recovery, not legitimately part of this context.
   */
  @SuppressWarnings("java:S3776")
  private static List<int[]> findOrphanedInvRanges(
      VitruvOCLParser.ContextDeclCSContext tree, List<Diagnostic> syntaxDiags) {

    List<int[]> orphaned = new ArrayList<>();
    if (tree == null || syntaxDiags.isEmpty()) return orphaned;

    for (VitruvOCLParser.ClassifierContextCSContext ctx : tree.classifierContextCS()) {
      if (ctx.getStart() == null) continue;
      int ctxStart = ctx.getStart().getLine() - 1; // 0-based
      int ctxStop = ctx.getStop() != null ? ctx.getStop().getLine() - 1 : Integer.MAX_VALUE;

      // First syntax-error line that falls strictly inside this context block.
      int firstErrLine = Integer.MAX_VALUE;
      for (Diagnostic d : syntaxDiags) {
        int l = d.getRange().getStart().getLine(); // already 0-based
        if (l > ctxStart && l <= ctxStop && l < firstErrLine) {
          firstErrLine = l;
        }
      }

      if (firstErrLine == Integer.MAX_VALUE) continue;

      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        if (inv.getStart() == null) continue;
        int invStart = inv.getStart().getLine() - 1; // 0-based
        int invStop = inv.getStop() != null ? inv.getStop().getLine() - 1 : invStart;

        // Case 1: invCS that starts after the first syntax error in this context.
        if (invStart > firstErrLine) {
          orphaned.add(new int[] {invStart, invStop});
          continue;
        }

        // Case 2: invCS that directly contains a syntax error — type-checker errors inside
        // would be unreliable cascade diagnostics, so suppress them too.
        boolean invHasSyntaxError = false;
        for (Diagnostic d : syntaxDiags) {
          int l = d.getRange().getStart().getLine(); // already 0-based
          if (l >= invStart && l <= invStop) {
            invHasSyntaxError = true;
            break;
          }
        }
        if (invHasSyntaxError) {
          orphaned.add(new int[] {invStart, invStop});
        }
      }
    }
    return orphaned;
  }

  private static Diagnostic toDiagnostic(CompileError error) {
    // ANTLR: line is 1-based, col 0-based → LSP: both 0-based.
    int startLine = Math.max(0, error.getLine() - 1);
    int startCol = Math.max(0, error.getColumn());

    int endLine;
    int endCol;
    if (error.getEndLine() > 0) {
      // Full span available — underline exactly the erroneous token range.
      endLine = Math.max(startLine, error.getEndLine() - 1);
      endCol = Math.max(startCol + 1, error.getEndColumn());
    } else {
      // No span — fall back to a single character so VS Code shows something.
      endLine = startLine;
      endCol = startCol + 1;
    }

    DiagnosticSeverity severity =
        error.getSeverity() == ErrorSeverity.ERROR
            ? DiagnosticSeverity.Error
            : DiagnosticSeverity.Warning;

    Diagnostic d = new Diagnostic(
        new Range(new Position(startLine, startCol), new Position(endLine, endCol)),
        error.getMessage(),
        severity,
        LANGUAGE_ID);

    // Attach the quick-fix suggestion as diagnostic data so the codeAction handler
    // can build a WorkspaceEdit without re-analysing the document.
    if (error.getSuggestion() != null) {
      d.setData(error.getSuggestion());
    }

    return d;
  }
}
