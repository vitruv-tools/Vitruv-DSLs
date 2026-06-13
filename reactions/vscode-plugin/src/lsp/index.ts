/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

import * as path from "node:path";

import { ExtensionContext, workspace } from "vscode";
import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	TransportKind,
} from "vscode-languageclient/node";

const LSP_JAR = "tools.vitruv.dsls.reactions.ide.jar";
const LSP_MAIN_CLASS = "tools.vitruv.dsls.reactions.ide.ReactionsServerLauncher";

function buildJavaArgs(metamodelJars: string[]): string[] {
	if (metamodelJars.length === 0) {
		return ["-jar", LSP_JAR, "-log", "-trace"];
	}

	const classpaths = [LSP_JAR, ...metamodelJars].join(path.delimiter);
	return ["-cp", classpaths, LSP_MAIN_CLASS, "-log", "-trace"];
}

export async function activate(context: ExtensionContext) {
	const additionalJars = workspace
		.getConfiguration("reactions")
		.get<string[]>("metamodelJars", []);

	const xtextServerOptions: ServerOptions = {
		command: "java",
		transport: TransportKind.stdio,
		args: buildJavaArgs(additionalJars),
		options: {
			cwd: context.extensionPath,
		},
	};

	const clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: "file", language: "reaction" }],
	};

	const client = new LanguageClient(
		"reactions-lsp",
		"Reactions Language Server",
		xtextServerOptions,
		clientOptions
	);

	await client.start();
	context.subscriptions.push(client);

	return client;
}
