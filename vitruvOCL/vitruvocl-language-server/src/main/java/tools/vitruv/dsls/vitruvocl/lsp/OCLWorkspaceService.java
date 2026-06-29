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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;

import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Workspace service that hot-reloads {@code .ecore} files whenever VS Code reports a {@code
 * workspace/didChangeWatchedFiles} event for them.
 *
 * <p>When the server starts it registers a glob watcher for {@code **}{@code /*.ecore} (see {@link
 * OCLLanguageServer#initialized}). VS Code then sends {@code didChangeWatchedFiles} notifications
 * whenever any matching file is created, modified, or deleted.
 *
 * <p>On every such notification:
 *
 * <ol>
 *   <li>The affected metamodels are reloaded (or removed) in {@link MetamodelWrapper}.
 *   <li>All currently-open OCL documents are re-analysed so that the updated type information is
 *       reflected immediately without requiring a dummy edit.
 * </ol>
 */
public class OCLWorkspaceService implements WorkspaceService {

  private static final Logger LOG = Logger.getLogger(OCLWorkspaceService.class.getName());

  private final MetamodelWrapper wrapper;
  private final OCLTextDocumentService textDocumentService;

  public OCLWorkspaceService(MetamodelWrapper wrapper, OCLTextDocumentService textDocumentService) {
    this.wrapper = wrapper;
    this.textDocumentService = textDocumentService;
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    // No configuration keys consumed by the language server at this time.
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    boolean anyChange = false;

    for (FileEvent event : params.getChanges()) {
      String uri = event.getUri();
      if (!uri.endsWith(".ecore")) continue;

      Path ecorePath = uriToPath(uri);
      if (ecorePath == null) {
        LOG.fine(() -> "[OCL-LS] Could not resolve path for URI: " + uri);
        continue;
      }

      FileChangeType type = event.getType();

      if (type == FileChangeType.Deleted) {
        // Remove the metamodel; don't try to load a file that no longer exists.
        wrapper.unloadMetamodel(ecorePath);
        LOG.fine(() -> "[OCL-LS] Metamodel deleted, unloaded: " + ecorePath);
        anyChange = true;

      } else if (type == FileChangeType.Created || type == FileChangeType.Changed) {
        // Reload: unload old version (no-op if not yet loaded) then load fresh copy.
        try {
          wrapper.reloadMetamodel(ecorePath);
          LOG.fine(() -> "[OCL-LS] Metamodel reloaded: " + ecorePath);
          anyChange = true;
        } catch (IOException e) {
          LOG.fine(() -> 
              "[OCL-LS] Failed to reload metamodel " + ecorePath + ": " + e.getMessage());
        }
      }
    }

    // Kick off re-analysis of all open documents so diagnostics and type hints
    // reflect the updated metamodel without requiring a dummy edit.
    if (anyChange) {
      textDocumentService.reanalyzeAll();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static Path uriToPath(String uriString) {
    if (uriString == null) return null;
    try {
      return Path.of(URI.create(uriString));
    } catch (Exception e) {
      return null;
    }
  }
}


