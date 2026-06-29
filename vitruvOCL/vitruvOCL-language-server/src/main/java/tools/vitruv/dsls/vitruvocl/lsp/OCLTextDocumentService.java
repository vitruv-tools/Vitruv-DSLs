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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Handles all {@code textDocument/*} requests from VS Code.
 *
 * <p>On every {@code didOpen} / {@code didChange} the current text is scheduled for re-analysis
 * with a 200 ms debounce. The resulting {@link DocumentAnalysis} is stored per URI and consulted
 * synchronously for hover and completion requests.
 */
public class OCLTextDocumentService implements TextDocumentService {

  private static final Logger LOG = Logger.getLogger(OCLTextDocumentService.class.getName());

  private static final long DEBOUNCE_MS = 200;

  private LanguageClient client;
  private final DocumentAnalyzer analyzer;
  private final CompletionProvider completionProvider;
  private final HoverProvider hoverProvider;
  private final SymbolProvider symbolProvider;
  private final DefinitionProvider definitionProvider;
  private final SignatureHelpProvider signatureHelpProvider;
  private final InlayHintProvider inlayHintProvider;

  /** Latest document text per URI. */
  private final Map<String, String> documents = new ConcurrentHashMap<>();

  /** Latest analysis result per URI — updated after each debounce cycle. */
  private final Map<String, DocumentAnalysis> analyses = new ConcurrentHashMap<>();

  /** Pending debounce futures per URI — cancelled when a new edit arrives. */
  private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

  private final ScheduledExecutorService debouncer =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "ocl-ls-debouncer");
            t.setDaemon(true);
            return t;
          });

  public OCLTextDocumentService(DocumentAnalyzer analyzer, MetamodelWrapper wrapper) {
    this.analyzer = analyzer;
    this.completionProvider = new CompletionProvider(wrapper);
    this.hoverProvider = new HoverProvider();
    this.symbolProvider = new SymbolProvider();
    this.definitionProvider = new DefinitionProvider();
    this.signatureHelpProvider = new SignatureHelpProvider();
    this.inlayHintProvider = new InlayHintProvider();
  }

  public void setClient(LanguageClient client) {
    this.client = client;
  }

  // ---------------------------------------------------------------------------
  // Document lifecycle
  // ---------------------------------------------------------------------------

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    String text = params.getTextDocument().getText();
    documents.put(uri, text);
    scheduleAnalysis(uri, text);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    // Full sync: the client always sends the complete document text.
    if (!params.getContentChanges().isEmpty()) {
      String text = params.getContentChanges().get(0).getText();
      documents.put(uri, text);
      scheduleAnalysis(uri, text);
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    documents.remove(uri);
    analyses.remove(uri);
    // Clear diagnostics for the closed file.
    if (client != null) {
      client.publishDiagnostics(new PublishDiagnosticsParams(uri, List.of()));
    }
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    // Re-analysis is already triggered by didChange; nothing extra needed.
  }

  // ---------------------------------------------------------------------------
  // Completion
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams params) {
    String uri = params.getTextDocument().getUri();
    String text = documents.getOrDefault(uri, "");
    DocumentAnalysis analysis = analyses.get(uri);

    List<CompletionItem> items =
        completionProvider.getCompletions(text, params.getPosition(), analysis);
    return CompletableFuture.completedFuture(Either.forLeft(items));
  }

  // ---------------------------------------------------------------------------
  // Hover
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<Hover> hover(HoverParams params) {
    String uri = params.getTextDocument().getUri();
    DocumentAnalysis analysis = analyses.get(uri);
    if (analysis == null) {
      return CompletableFuture.completedFuture(null);
    }
    Hover hover = hoverProvider.getHover(params.getPosition(), analysis);
    return CompletableFuture.completedFuture(hover);
  }

  // ---------------------------------------------------------------------------
  // Document symbols (outline view)
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
      DocumentSymbolParams params) {
    String uri = params.getTextDocument().getUri();
    DocumentAnalysis analysis = analyses.get(uri);
    List<Either<SymbolInformation, DocumentSymbol>> symbols = symbolProvider.getSymbols(analysis);
    return CompletableFuture.completedFuture(symbols);
  }

  // ---------------------------------------------------------------------------
  // Go-to-definition
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
      definition(DefinitionParams params) {
    String uri = params.getTextDocument().getUri();
    DocumentAnalysis analysis = analyses.get(uri);
    Location location = definitionProvider.getDefinition(params.getPosition(), analysis);
    if (location == null) {
      return CompletableFuture.completedFuture(Either.forLeft(List.of()));
    }
    return CompletableFuture.completedFuture(Either.forLeft(List.of(location)));
  }

  // ---------------------------------------------------------------------------
  // Signature help
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
    String uri = params.getTextDocument().getUri();
    String text = documents.getOrDefault(uri, "");
    SignatureHelp help = signatureHelpProvider.getSignatureHelp(text, params.getPosition());
    return CompletableFuture.completedFuture(help);
  }

  // ---------------------------------------------------------------------------
  // Inlay hints
  // ---------------------------------------------------------------------------

  @Override
  public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
    String uri = params.getTextDocument().getUri();
    DocumentAnalysis analysis = analyses.get(uri);
    List<InlayHint> hints = inlayHintProvider.getHints(analysis, params.getRange());
    return CompletableFuture.completedFuture(hints);
  }

  // ---------------------------------------------------------------------------
  // Debounced analysis
  // ---------------------------------------------------------------------------

  private void scheduleAnalysis(String uri, String text) {
    ScheduledFuture<?> existing = pending.get(uri);
    if (existing != null) {
      existing.cancel(false);
    }

    ScheduledFuture<?> future =
        debouncer.schedule(() -> runAnalysis(uri, text), DEBOUNCE_MS, TimeUnit.MILLISECONDS);
    pending.put(uri, future);
  }

  private void runAnalysis(String uri, String text) {
    try {
      DocumentAnalysis analysis = analyzer.analyze(text);
      analyses.put(uri, analysis);

      if (client != null) {
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, analysis.getDiagnostics()));
      }
    } catch (Exception e) {
      LOG.fine("[OCL-LS] Analysis failed for " + uri + ": " + e.getMessage());
    }
  }

  // ---------------------------------------------------------------------------
  // Code actions (Quick Fix)
  // ---------------------------------------------------------------------------

  /**
   * Returns quick-fix code actions for diagnostics that carry a suggestion.
   *
   * <p>When the type checker detects an unknown operation it stores the closest known operation name
   * in {@link tools.vitruv.dsls.vitruvocl.common.CompileError#getSuggestion()}. The
   * {@link DocumentAnalyzer} serialises that suggestion into {@link Diagnostic#getData()}. Here we
   * read it back and produce a {@code QuickFix} {@link CodeAction} whose {@link WorkspaceEdit}
   * replaces the squiggled token range with the suggestion.
   *
   * <p>VS Code renders each action as a blue-highlighted item in the "Quick Fix…" menu (⌘. / Ctrl+.)
   * directly below the squiggle.
   */
  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    return CompletableFuture.supplyAsync(() -> {
      List<Either<Command, CodeAction>> actions = new ArrayList<>();
      String uri = params.getTextDocument().getUri();

      for (Diagnostic diag : params.getContext().getDiagnostics()) {
        Object data = diag.getData();
        if (data == null) continue;

        // data is a String (the replacement text) serialised as a JSON string by lsp4j
        String suggestion = data instanceof String s ? s : data.toString();
        // lsp4j may wrap the value in quotes when deserialised via Gson
        if (suggestion.startsWith("\"") && suggestion.endsWith("\"") && suggestion.length() >= 2) {
          suggestion = suggestion.substring(1, suggestion.length() - 1);
        }
        if (suggestion.isBlank()) continue;

        TextEdit edit = new TextEdit(diag.getRange(), suggestion);
        WorkspaceEdit wsEdit = new WorkspaceEdit(Map.of(uri, List.of(edit)));

        CodeAction action = new CodeAction("Replace with '" + suggestion + "'");
        action.setKind(CodeActionKind.QuickFix);
        action.setDiagnostics(List.of(diag));
        action.setEdit(wsEdit);
        // Mark as preferred so VS Code highlights it in blue
        action.setIsPreferred(true);

        actions.add(Either.forRight(action));
      }

      return actions;
    });
  }

  /**
   * Re-schedules analysis for every currently-open document.
   *
   * <p>Called after a metamodel hot-reload so that all open {@code .ocl} files pick up the new type
   * information without the user having to make a dummy edit.
   */
  public void reanalyzeAll() {
    documents.forEach(this::scheduleAnalysis);
  }

  public void shutdown() {
    debouncer.shutdownNow();
  }
}
