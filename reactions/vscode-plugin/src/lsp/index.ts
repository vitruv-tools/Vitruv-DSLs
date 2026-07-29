/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

import * as path from "path";

import * as vscode from "vscode";
import { ExtensionContext } from "vscode";
import {
	Executable,
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	TransportKind,
} from "vscode-languageclient/node";

interface PlainPosition {
	line: number;
	character: number;
}

interface PlainRange {
	start: PlainPosition;
	end: PlainPosition;
}

interface PlainLocation {
	uri: string;
	range: PlainRange;
}

function toPosition(position: PlainPosition): vscode.Position {
	return new vscode.Position(position.line, position.character);
}

function toLocation(location: PlainLocation): vscode.Location {
	return new vscode.Location(
		vscode.Uri.parse(location.uri),
		new vscode.Range(toPosition(location.range.start), toPosition(location.range.end))
	);
}

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
		"reactions-lsp",
		"Reactions Language Server",
		xtextServerOptions,
		clientOptions
	);

	// The "N callers" code lens reports its jump target as plain JSON (uri/position/locations),
	// since that is all the language server can put into a lsp4j Command's arguments. VS Code's
	// built-in "editor.action.showReferences" command validates its arguments with `instanceof`
	// checks against real vscode.Uri/Position/Location instances, so calling it directly with the
	// raw JSON fails with "argument does not match one of these constraints". This command
	// converts the plain data into real vscode objects first.
	context.subscriptions.push(
		vscode.commands.registerCommand(
			"reactions.showReferences",
			(uri: string, position: PlainPosition, locations: PlainLocation[]) => {
				vscode.commands.executeCommand(
					"editor.action.showReferences",
					vscode.Uri.parse(uri),
					toPosition(position),
					locations.map(toLocation)
				);
			}
		)
	);

	await client.start();

	context.subscriptions.push(client);

	return client;
}
