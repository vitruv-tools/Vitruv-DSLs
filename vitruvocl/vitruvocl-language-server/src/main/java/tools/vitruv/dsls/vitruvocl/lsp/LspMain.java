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

import java.io.PrintStream;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Entry point for the vitruvOCL-OCL Language Server.
 *
 * <p>Communicates over stdio (stdin/stdout). The VS Code extension spawns this process and connects
 * via the LSP JSON-RPC protocol.
 *
 * <p>stdout is captured for LSP frames before any user code runs so that accidental
 * System.out.println() calls in the pipeline are redirected to stderr and cannot corrupt the LSP
 * stream.
 */
public class LspMain {

  /**
   * Starts the language server on stdio.
   *
   * @param args unused
   * @throws Exception if the server fails to start or the connection is interrupted
   */
  @SuppressWarnings("java:S106")
  public static void main(String[] args) throws Exception {
    // Capture the real stdout before anything else touches it.
    PrintStream lspOut = System.out; // NOSONAR: required for LSP stdio protocol

    // Redirect casual prints to stderr so the LSP stream stays clean.
    System.setOut(System.err); // NOSONAR: intentional stdout→stderr redirect for LSP

    OCLLanguageServer server = new OCLLanguageServer();

    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, lspOut);

    server.connect(launcher.getRemoteProxy());

    // Blocks until the client disconnects.
    launcher.startListening().get();
  }
}
