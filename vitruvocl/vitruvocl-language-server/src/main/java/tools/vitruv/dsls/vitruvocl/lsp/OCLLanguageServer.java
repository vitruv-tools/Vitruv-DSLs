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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Root language server implementation.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Negotiate capabilities with VS Code on {@code initialize}
 *   <li>Scan workspace folders for {@code .ecore} files and pre-load them into {@link
 *       MetamodelWrapper} so type resolution works immediately
 *   <li>Delegate all text-document and workspace events to the respective services
 * </ul>
 */
public class OCLLanguageServer implements LanguageServer, LanguageClientAware {

  private static final Logger LOG = Logger.getLogger(OCLLanguageServer.class.getName());

  private LanguageClient client;
  private final MetamodelWrapper wrapper = new MetamodelWrapper();
  private final OCLTextDocumentService textDocumentService;
  private final OCLWorkspaceService workspaceService;

  /** Creates the language server, wiring up its text document and workspace services. */
  public OCLLanguageServer() {
    DocumentAnalyzer analyzer = new DocumentAnalyzer(wrapper);
    textDocumentService = new OCLTextDocumentService(analyzer, wrapper);
    workspaceService = new OCLWorkspaceService(wrapper, textDocumentService);
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
    textDocumentService.setClient(client);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    // Load all .ecore files found in the workspace so the type checker can resolve metamodel refs.
    loadEcoreFiles(params);

    ServerCapabilities caps = new ServerCapabilities();

    // Full-document sync: the client sends the entire text on every change.
    caps.setTextDocumentSync(TextDocumentSyncKind.Full);

    // Completion: trigger on '.' (property access) and ':' (detects '::' namespace separator).
    CompletionOptions completionOptions = new CompletionOptions();
    completionOptions.setTriggerCharacters(List.of(".", ":", "@", " "));
    completionOptions.setResolveProvider(false);
    caps.setCompletionProvider(completionOptions);

    // Signature help: show parameter info when typing ( or , inside a call.
    SignatureHelpOptions sigHelpOptions = new SignatureHelpOptions();
    sigHelpOptions.setTriggerCharacters(List.of("(", ",", "|"));
    caps.setSignatureHelpProvider(sigHelpOptions);

    // Hover: show inferred OCL# type on any expression.
    caps.setHoverProvider(Either.forLeft(true));

    // Document symbols: populate the outline / breadcrumb view.
    caps.setDocumentSymbolProvider(Either.forLeft(true));

    // Go-to-definition: navigate to the EClass declaration in its .ecore file.
    caps.setDefinitionProvider(Either.forLeft(true));

    // Inlay hints: type annotations for let-variables, iterator vars, and metaclass attributes.
    caps.setInlayHintProvider(Either.forLeft(true));

    // Code actions: Quick Fix for unknown operations ("did you mean?" replace).
    caps.setCodeActionProvider(Either.forLeft(true));

    return CompletableFuture.completedFuture(new InitializeResult(caps));
  }

  @Override
  public void initialized(InitializedParams params) {
    // Register a dynamic file watcher for all .ecore files in the workspace.
    FileSystemWatcher ecoreWatcher = new FileSystemWatcher();
    ecoreWatcher.setGlobPattern(Either.forLeft("**/*.ecore"));

    DidChangeWatchedFilesRegistrationOptions watchOpts =
        new DidChangeWatchedFilesRegistrationOptions(List.of(ecoreWatcher));

    Registration reg =
        new Registration("ocl-ecore-watcher", "workspace/didChangeWatchedFiles", watchOpts);

    client.registerCapability(new RegistrationParams(List.of(reg)));
  }

  @Override
  @SuppressWarnings("java:S1186")
  public void setTrace(SetTraceParams params) {
    // Intentionally empty: trace level changes are not used by this server implementation.
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    textDocumentService.shutdown();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    System.exit(0);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return workspaceService;
  }

  // ---------------------------------------------------------------------------
  // Ecore scanning
  // ---------------------------------------------------------------------------

  @SuppressWarnings("java:S1874")
  private void loadEcoreFiles(InitializeParams params) {
    List<Path> ecoreFiles = new ArrayList<>();

    // Prefer workspace folders (multi-root support).
    List<WorkspaceFolder> folders = params.getWorkspaceFolders();
    if (folders != null) {
      for (WorkspaceFolder folder : folders) {
        scanForEcore(uriToPath(folder.getUri()), ecoreFiles);
      }
    } else if (params.getRootPath() != null) {
      scanForEcore(Path.of(params.getRootPath()), ecoreFiles);
    }

    // Before loading: register platform:/plugin/ → local-file mappings so cross-ecore
    // inheritance (e.g. PCMRandomVariable → stoex::RandomVariable) resolves automatically.
    wrapper.registerWorkspaceEcoresForPlatformResolution(ecoreFiles);

    for (Path ecoreFile : ecoreFiles) {
      try {
        wrapper.loadMetamodel(ecoreFile);
        LOG.fine(() -> "[OCL-LS] Loaded metamodel: " + ecoreFile);
      } catch (IOException e) {
        LOG.fine(() -> "[OCL-LS] Could not load " + ecoreFile + ": " + e.getMessage());
      }
    }
  }

  private static void scanForEcore(Path root, List<Path> result) {
    if (root == null || !Files.isDirectory(root)) {
      return;
    }
    try {
      Files.walkFileTree(
          root,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (file.toString().endsWith(".ecore")) {
                result.add(file);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              // Skip hidden directories (e.g. .git, node_modules).
              String name = dir.getFileName() != null ? dir.getFileName().toString() : "";
              if (name.startsWith(".") || name.equals("node_modules") || name.equals("target")) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      LOG.fine(() -> "[OCL-LS] Error scanning " + root + ": " + e.getMessage());
    }
  }

  private static Path uriToPath(String uriString) {
    if (uriString == null) {
      return null;
    }
    try {
      return Path.of(URI.create(uriString));
    } catch (Exception e) {
      return null;
    }
  }
}
