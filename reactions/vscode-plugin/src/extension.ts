import * as vscode from "vscode";
import { ExtensionContext } from "vscode";

import * as lsp from "./lsp";

export async function activate(context: ExtensionContext) {
	lsp.activate(context);

	// TODO remove
	vscode.window.showInformationMessage(
		`Reactions initialized`
	);
}

export function deactivate() {}
