/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

import * as path from "path";

import { ExtensionContext } from "vscode";
import {
	Executable,
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	TransportKind,
} from "vscode-languageclient/node";

export async function activate(context: ExtensionContext) {
	const xtextServerOptions: ServerOptions = {
		command: "java",
		transport: TransportKind.stdio,
		args: [
			"-jar", "tools.vitruv.dsls.reactions.ide.jar","-log",'-trace'
		],
		options: {
			cwd: context.extensionPath,
		},
	};

	const clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: "file", language: "reaction" }],
	};

	const client = new LanguageClient(
		"hw-lsp",
		"hw Language Server",
		xtextServerOptions,
		clientOptions
	);

	await client.start();

	context.subscriptions.push(client);

	return client;
}
