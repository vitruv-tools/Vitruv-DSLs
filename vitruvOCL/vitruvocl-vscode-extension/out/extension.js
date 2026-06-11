"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.activate = activate;
exports.deactivate = deactivate;
const vscode = require("vscode");
const child_process = require("child_process");
const util = require("util");
const fs = require("fs");
const path = require("path");
const node_1 = require("vscode-languageclient/node");
const execFile = util.promisify(child_process.execFile);
const constraintResults = new Map();
let codeLensProvider;
let currentDecorations = new Map();
let diagnosticCollection;
let languageClient;
async function applyThemeOnFirstInstall(context) {
    const THEME_KEY = 'vitruvocl.themeApplied';
    const alreadyApplied = context.globalState.get(THEME_KEY, false);
    if (alreadyApplied) {
        return;
    }
    const config = vscode.workspace.getConfiguration();
    const currentTheme = config.get('workbench.colorTheme');
    if (currentTheme === 'MultiModelOCL Dark') {
        await context.globalState.update(THEME_KEY, true);
        return;
    }
    const answer = await vscode.window.showInformationMessage('VitruvOCL: Apply the included "MultiModelOCL Dark" theme for optimized syntax highlighting?', 'Yes', 'No');
    if (answer === 'Yes') {
        await config.update('workbench.colorTheme', 'MultiModelOCL Dark', vscode.ConfigurationTarget.Global);
    }
    await context.globalState.update(THEME_KEY, true);
}
function activate(context) {
    applyThemeOnFirstInstall(context);
    diagnosticCollection = vscode.languages.createDiagnosticCollection('vitruvocl');
    context.subscriptions.push(diagnosticCollection);
    codeLensProvider = new OCLCodeLensProvider();
    context.subscriptions.push(vscode.languages.registerCodeLensProvider({ language: 'vitruvocl' }, codeLensProvider));
    context.subscriptions.push(vscode.commands.registerCommand('vitruvocl.runConstraint', async (constraintName, documentUri) => {
        await runConstraint(constraintName, documentUri);
    }));
    context.subscriptions.push(vscode.commands.registerCommand('vitruvocl.runAllConstraints', async () => {
        await runAllConstraints();
    }));
    context.subscriptions.push(vscode.window.onDidChangeActiveTextEditor(editor => {
        if (editor && editor.document.languageId === 'vitruvocl') {
            updateGutterIcons(editor);
            updateInlineErrors(editor);
        }
    }));
    context.subscriptions.push(vscode.workspace.onDidChangeTextDocument(event => {
        const editor = vscode.window.activeTextEditor;
        if (editor && editor.document === event.document && editor.document.languageId === 'vitruvocl') {
            updateGutterIcons(editor);
            updateInlineErrors(editor);
            triggerSuggestAfterInvNewline(editor, event);
        }
    }));
    if (vscode.window.activeTextEditor?.document.languageId === 'vitruvocl') {
        updateGutterIcons(vscode.window.activeTextEditor);
        updateInlineErrors(vscode.window.activeTextEditor);
    }
    startLanguageClient(context);
}
function updateGutterIcons(editor) {
    currentDecorations.forEach((decoration, key) => {
        if (key.startsWith('constraint_')) {
            decoration.dispose();
        }
    });
    const text = editor.document.getText();
    const lines = text.split('\n');
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const match = line.match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
        if (match) {
            const constraintName = match[2];
            const status = constraintResults.get(constraintName);
            let iconUri;
            if (!status || !status.ran) {
                iconUri = createRunIcon();
            }
            else if (status.passed) {
                iconUri = createPassIcon();
            }
            else {
                iconUri = createFailIcon();
            }
            const decoration = vscode.window.createTextEditorDecorationType({
                gutterIconPath: iconUri,
                gutterIconSize: 'contain'
            });
            const range = new vscode.Range(i, 0, i, 0);
            editor.setDecorations(decoration, [range]);
            currentDecorations.set(`constraint_${i}`, decoration);
        }
    }
}
function updateInlineErrors(editor, constraintDetails) {
    const oldInlineDecoration = currentDecorations.get('inline_errors');
    if (oldInlineDecoration) {
        oldInlineDecoration.dispose();
    }
    const text = editor.document.getText();
    const lines = text.split('\n');
    const inlineDecorations = [];
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const match = line.match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
        if (match) {
            const constraintName = match[2];
            const status = constraintResults.get(constraintName);
            if (status?.ran && !status.passed) {
                const lineLength = line.length;
                const range = new vscode.Range(i, lineLength, i, lineLength);
                let errorText = `${constraintName} violated`;
                const details = constraintDetails?.get(constraintName);
                if (details && details.length > 0) {
                    errorText = details[0];
                    if (details.length > 1) {
                        errorText += ` (+${details.length - 1} more)`;
                    }
                }
                const hover = new vscode.MarkdownString();
                hover.appendMarkdown(`**❌ Constraint violated**\n\n`);
                if (details && details.length > 0) {
                    for (const d of details) {
                        hover.appendMarkdown(`- ${d}\n`);
                    }
                }
                else {
                    hover.appendMarkdown(`${constraintName} violated`);
                }
                hover.isTrusted = true;
                const decoration = {
                    range,
                    hoverMessage: hover,
                    renderOptions: {
                        after: {
                            contentText: ` ◀ ${errorText}`,
                            color: new vscode.ThemeColor('errorForeground'),
                            margin: '0 0 0 20px'
                        }
                    }
                };
                inlineDecorations.push(decoration);
            }
        }
    }
    const inlineDecorationType = vscode.window.createTextEditorDecorationType({});
    editor.setDecorations(inlineDecorationType, inlineDecorations);
    currentDecorations.set('inline_errors', inlineDecorationType);
}
function createRunIcon() {
    return vscode.Uri.parse('data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="none" stroke="#858585" stroke-width="1.5"/>
            <path d="M 6 5 L 6 11 L 11 8 Z" fill="#858585"/>
        </svg>
    `));
}
function createPassIcon() {
    return vscode.Uri.parse('data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="#73C991"/>
            <path d="M 5 8 L 7 10 L 11 6" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `));
}
function createFailIcon() {
    return vscode.Uri.parse('data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="#F48771"/>
            <path d="M 5.5 5.5 L 10.5 10.5 M 10.5 5.5 L 5.5 10.5" stroke="white" stroke-width="2" stroke-linecap="round"/>
        </svg>
    `));
}
async function runAllConstraints() {
    const editor = vscode.window.activeTextEditor;
    if (!editor || editor.document.languageId !== 'vitruvocl') {
        vscode.window.showErrorMessage('No vitruvocl file open');
        return;
    }
    const document = editor.document;
    constraintResults.clear();
    diagnosticCollection.clear();
    codeLensProvider.refresh();
    updateGutterIcons(editor);
    updateInlineErrors(editor);
    try {
        const compilerPath = await findCompilerJar();
        if (!compilerPath) {
            vscode.window.showErrorMessage('vitruvocl.jar not found');
            return;
        }
        const ecoreFiles = await findEcoreFiles(document.uri);
        const instanceFiles = await findInstanceFiles(document.uri);
        if (ecoreFiles.length === 0) {
            vscode.window.showWarningMessage('No .ecore metamodel files found in workspace.');
            return;
        }
        if (instanceFiles.length === 0 || !instanceFiles.some(f => f.endsWith('.correspondence'))) {
            vscode.window.showErrorMessage('No VSUM found. Run VSUMExample.main() from your IDE to create one, then try again.');
            return;
        }
        const hasInstances = true;
        vscode.window.showInformationMessage('Running all constraints...');
        const batchArgs = [
            '-jar', compilerPath,
            'eval-batch',
            document.fileName,
            '--ecore', ecoreFiles.join(','),
            ...(hasInstances ? ['--xmi', instanceFiles.join(',')] : [])
        ];
        const execResult = await execFile('java', batchArgs).catch(err => ({
            stdout: err.stdout || '',
            stderr: err.stderr || err.message
        }));
        const stdout = execResult.stdout;
        const stderr = execResult.stderr || '';
        // Surface CLI stderr in the output channel so debug prints are visible
        if (stderr && outputChannel) {
            outputChannel.appendLine('[CLI-STDERR] ' + stderr.trim().split('\n').join('\n[CLI-STDERR] '));
        }
        const result = JSON.parse(stdout);
        if (!result.success) {
            vscode.window.showErrorMessage('Batch evaluation failed');
            return;
        }
        let passed = 0;
        let failed = 0;
        const constraintDetails = new Map();
        for (const constraint of result.constraints) {
            constraintResults.set(constraint.name, {
                name: constraint.name,
                passed: constraint.satisfied,
                ran: true
            });
            updateDiagnosticsForConstraintBatch(document, constraint);
            if (constraint.satisfied) {
                passed++;
            }
            else {
                failed++;
                if (constraint.warnings) {
                    constraintDetails.set(constraint.name, constraint.warnings);
                }
            }
        }
        codeLensProvider.refresh();
        updateGutterIcons(editor);
        updateInlineErrors(editor, constraintDetails);
        showSummaryResult(passed, failed, result.constraints);
    }
    catch (error) {
        vscode.window.showErrorMessage(`Run all failed: ${error.message}`);
    }
}
async function runConstraint(constraintName, documentUri) {
    const document = await vscode.workspace.openTextDocument(documentUri);
    const editor = vscode.window.visibleTextEditors.find(e => e.document === document);
    try {
        const compilerPath = await findCompilerJar();
        if (!compilerPath) {
            vscode.window.showErrorMessage('vitruvocl.jar not found');
            return;
        }
        const ecoreFiles = await findEcoreFiles(documentUri);
        const instanceFiles = await findInstanceFiles(documentUri);
        if (ecoreFiles.length === 0) {
            vscode.window.showWarningMessage('No .ecore metamodel files found in workspace.');
            return;
        }
        if (instanceFiles.length === 0 || !instanceFiles.some(f => f.endsWith('.correspondence'))) {
            vscode.window.showErrorMessage('No VSUM found. Run VSUMExample.main() from your IDE to create one, then try again.');
            return;
        }
        const singleConstraint = extractConstraint(document.getText(), constraintName);
        if (!singleConstraint) {
            vscode.window.showErrorMessage(`Constraint not found: ${constraintName}`);
            return;
        }
        const tempFile = await writeTempConstraint(singleConstraint);
        vscode.window.showInformationMessage(`Running: ${constraintName}...`);
        const { stdout } = await execFile('java', [
            '-jar', compilerPath, 'eval', tempFile,
            '--ecore', ecoreFiles.join(','),
            '--xmi', instanceFiles.join(',')
        ]).catch(err => ({ stdout: err.stdout || '' }));
        const result = JSON.parse(stdout);
        constraintResults.set(constraintName, {
            name: constraintName,
            passed: result.satisfied,
            ran: true
        });
        updateDiagnosticsForConstraint(document, constraintName, result);
        codeLensProvider.refresh();
        if (editor) {
            updateGutterIcons(editor);
            const details = new Map();
            if (!result.satisfied && result.warnings) {
                details.set(constraintName, result.warnings);
            }
            updateInlineErrors(editor, details);
        }
        showEvalResult(constraintName, result);
        fs.unlinkSync(tempFile);
    }
    catch (error) {
        vscode.window.showErrorMessage(`Run failed: ${error.message}`);
        constraintResults.set(constraintName, {
            name: constraintName,
            passed: false,
            ran: true
        });
        codeLensProvider.refresh();
        if (editor) {
            updateGutterIcons(editor);
            updateInlineErrors(editor);
        }
    }
}
function updateDiagnosticsForConstraint(document, constraintName, result) {
    const diagnostics = [];
    if (!result.success) {
        for (const error of result.errors) {
            const line = Math.max(0, error.line - 1);
            const range = new vscode.Range(line, 0, line, 1000);
            const diagnostic = new vscode.Diagnostic(range, `Compilation Error: ${error.message}`, vscode.DiagnosticSeverity.Error);
            diagnostic.source = 'vitruvocl';
            diagnostics.push(diagnostic);
        }
    }
    const existingDiagnostics = Array.from(diagnosticCollection.get(document.uri) || []);
    const otherDiagnostics = existingDiagnostics.filter(d => {
        const line = d.range.start.line;
        const constraintAtLine = getConstraintNameAtLine(document.getText(), line);
        return constraintAtLine !== constraintName;
    });
    diagnosticCollection.set(document.uri, [...otherDiagnostics, ...diagnostics]);
}
function updateDiagnosticsForConstraintBatch(document, constraint) {
    const diagnostics = Array.from(diagnosticCollection.get(document.uri) || []);
    if (!constraint.success) {
        if (constraint.errors) {
            for (const error of constraint.errors) {
                const line = Math.max(0, error.line - 1);
                const range = new vscode.Range(line, 0, line, 1000);
                const diagnostic = new vscode.Diagnostic(range, `Compilation Error: ${error.message}`, vscode.DiagnosticSeverity.Error);
                diagnostic.source = 'vitruvocl';
                diagnostics.push(diagnostic);
            }
        }
    }
    diagnosticCollection.set(document.uri, diagnostics);
}
function getConstraintNameAtLine(text, line) {
    const lines = text.split('\n');
    if (line >= 0 && line < lines.length) {
        const match = lines[line].match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
        if (match) {
            return match[2];
        }
    }
    return null;
}
class OCLCodeLensProvider {
    constructor() {
        this._onDidChangeCodeLenses = new vscode.EventEmitter();
        this.onDidChangeCodeLenses = this._onDidChangeCodeLenses.event;
    }
    refresh() {
        this._onDidChangeCodeLenses.fire();
    }
    provideCodeLenses(document) {
        const codeLenses = [];
        const text = document.getText();
        const lines = text.split('\n');
        let firstContextLine = -1;
        for (let i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith('context')) {
                firstContextLine = i;
                break;
            }
        }
        if (firstContextLine >= 0) {
            codeLenses.push(new vscode.CodeLens(new vscode.Range(firstContextLine, 0, firstContextLine, 0), {
                title: '▶ Run All',
                command: 'vitruvocl.runAllConstraints'
            }));
        }
        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            const match = line.match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
            if (match) {
                const constraintName = match[2];
                codeLenses.push(new vscode.CodeLens(new vscode.Range(i, 0, i, line.length), {
                    title: '▶',
                    command: 'vitruvocl.runConstraint',
                    arguments: [constraintName, document.uri]
                }));
            }
        }
        return codeLenses;
    }
}
function extractConstraint(text, constraintName) {
    const lines = text.split('\n');
    let collecting = false;
    let constraint = '';
    let startLine = -1;
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        if (line.match(new RegExp(`inv\\s+${constraintName}\\s*:`))) {
            collecting = true;
            startLine = i;
            for (let j = i; j >= 0; j--) {
                if (lines[j].trim().startsWith('context')) {
                    constraint = lines[j] + '\n';
                    break;
                }
            }
        }
        if (collecting && i > startLine) {
            const trimmed = line.trim();
            // Stop at the next constraint's context declaration
            if (trimmed.startsWith('context ')) {
                break;
            }
            // Skip comment lines — the CLI eval command does not strip them
            if (trimmed.startsWith('--')) {
                continue;
            }
            constraint += line + '\n';
        }
    }
    return constraint.trim() || null;
}
async function writeTempConstraint(constraint) {
    const os = await Promise.resolve().then(() => require('os'));
    const tempDir = os.tmpdir();
    const tempFile = path.join(tempDir, `vitruvocl-${Date.now()}.ocl`);
    fs.writeFileSync(tempFile, constraint, 'utf-8');
    return tempFile;
}
async function findCompilerJar() {
    const config = vscode.workspace.getConfiguration('vitruvocl');
    const configured = config.get('compilerPath');
    if (configured && fs.existsSync(configured))
        return configured;
    const extPath = vscode.extensions.getExtension('vitruvocl.vitruvocl')?.extensionPath;
    if (extPath) {
        const bundled = path.join(extPath, 'lib', 'vitruvOCL.jar');
        if (fs.existsSync(bundled))
            return bundled;
    }
    return null;
}
// ---------------------------------------------------------------------------
// File discovery — VSUM-aware
// ---------------------------------------------------------------------------
async function findEcoreFiles(constraintFileUri) {
    const workspaceRoot = constraintFileUri
        ? vscode.workspace.getWorkspaceFolder(constraintFileUri)?.uri.fsPath
        : vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
    if (!workspaceRoot)
        return [];
    // Primary: ecore/ folder at workspace root
    const ecorePath = path.join(workspaceRoot, 'ecore');
    if (fs.existsSync(ecorePath)) {
        return fs.readdirSync(ecorePath)
            .filter(f => f.endsWith('.ecore'))
            .map(f => path.join(ecorePath, f))
            .filter(f => fs.statSync(f).isFile());
    }
    // Fallback: workspace-wide search (skips target/ implicitly via VS Code glob)
    const files = await vscode.workspace.findFiles('**/*.ecore', '**/target/**');
    return files.map(u => u.fsPath);
}
/** Directories that should never be treated as model instance sources. */
const VSUM_SKIP_DIRS = new Set(['.git', 'target', 'src', 'node_modules']);
/**
 * Recursively collects all model instance files from a VSUM storage folder.
 * Skips the internal vsum/ metadata subfolder and other non-model directories.
 * Model files can be at the root level OR inside subdirectories.
 */
function collectVsumFiles(vsumFolder) {
    if (!fs.existsSync(vsumFolder) || !fs.statSync(vsumFolder).isDirectory())
        return [];
    const result = [];
    function walk(dir) {
        for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
            const fullPath = path.join(dir, entry.name);
            if (entry.isFile()) {
                const ext = path.extname(entry.name).toLowerCase();
                // Skip build artifacts, source files, and internal Vitruvius metadata
                if (ext === '.ecore' || ext === '.genmodel' || ext === '.java'
                    || ext === '.class' || ext === '.jar' || ext === '.mwe2'
                    || ext === '.reactions' || ext === '.xtend'
                    || ext === '.uuid' || ext === '.models'
                    || entry.name.endsWith('.marker_vitruv')) {
                    continue;
                }
                result.push(fullPath);
            }
            else if (entry.isDirectory()) {
                if (VSUM_SKIP_DIRS.has(entry.name))
                    continue;
                walk(fullPath);
            }
        }
    }
    walk(vsumFolder);
    return result;
}
async function findInstanceFiles(constraintFileUri) {
    const workspaceRoot = constraintFileUri
        ? vscode.workspace.getWorkspaceFolder(constraintFileUri)?.uri.fsPath
        : vscode.workspace.workspaceFolders?.[0]?.uri.fsPath;
    if (!workspaceRoot)
        return [];
    // 0. User-configured VSUM path (vitruvocl.vsumPath setting)
    const config = vscode.workspace.getConfiguration('vitruvocl');
    const configuredPath = config.get('vsumPath');
    if (configuredPath) {
        const resolved = path.isAbsolute(configuredPath)
            ? configuredPath
            : path.join(workspaceRoot, configuredPath);
        const files = collectVsumFiles(resolved);
        if (files.length > 0)
            return files;
    }
    // 1. Standard Maven test VSUM: vsum/target/vsumtest/
    const vsumBase = path.join(workspaceRoot, 'vsum', 'target', 'vsumtest');
    const files1 = collectVsumFiles(vsumBase);
    if (files1.length > 0)
        return files1;
    // 2. VSUMExample default: vsumexample/ at workspace root or inside vsum/
    for (const candidate of [
        path.join(workspaceRoot, 'vsumexample'),
        path.join(workspaceRoot, 'vsum', 'vsumexample'),
    ]) {
        const files2 = collectVsumFiles(candidate);
        if (files2.length > 0)
            return files2;
    }
    // 3. Broader search: any folder named vsumtest, vsumexample, or vsum-storage
    const broader = await vscode.workspace.findFiles('{**/vsumtest/**,**/vsumexample/**,**/vsum-storage/**}', '**/target/**');
    if (broader.length > 0)
        return broader.map(u => u.fsPath);
    // Fallback: legacy instances/ folder relative to constraint file
    const searchRoot = constraintFileUri ? path.dirname(constraintFileUri.fsPath) : workspaceRoot;
    const instancesPath = path.join(searchRoot, 'instances');
    if (fs.existsSync(instancesPath)) {
        return fs.readdirSync(instancesPath)
            .map(f => path.join(instancesPath, f))
            .filter(f => fs.statSync(f).isFile());
    }
    return [];
}
// ---------------------------------------------------------------------------
// Output
// ---------------------------------------------------------------------------
let outputChannel;
function showEvalResult(name, result) {
    if (!outputChannel) {
        outputChannel = vscode.window.createOutputChannel('VitruvOCL');
    }
    const channel = outputChannel;
    channel.clear();
    channel.show(true);
    channel.appendLine(`=== ${name} ===\n`);
    if (!result.success) {
        channel.appendLine('❌ COMPILATION ERRORS:');
        result.errors.forEach(e => channel.appendLine(`  Line ${e.line}: ${e.message}`));
    }
    else if (result.satisfied) {
        channel.appendLine('✅ CONSTRAINT SATISFIED\nAll instances pass.');
    }
    else {
        channel.appendLine('❌ CONSTRAINT VIOLATED');
        if (result.warnings.length > 0) {
            channel.appendLine('\nViolations:');
            result.warnings.forEach(w => channel.appendLine(`  • ${w}`));
        }
    }
    channel.appendLine('');
}
function showSummaryResult(passed, failed, constraints) {
    if (!outputChannel) {
        outputChannel = vscode.window.createOutputChannel('VitruvOCL');
    }
    const channel = outputChannel;
    channel.clear();
    channel.show(true);
    const total = passed + failed;
    channel.appendLine(`=== All Constraints (${total} total) ===\n`);
    if (failed === 0) {
        channel.appendLine(`✅ ALL PASSED (${passed}/${total})\n`);
    }
    else {
        channel.appendLine(`❌ SOME FAILED (${failed}/${total})\n`);
        channel.appendLine(`Passed: ${passed}`);
        channel.appendLine(`Failed: ${failed}\n`);
    }
    channel.appendLine('Individual Results:');
    for (const constraint of constraints) {
        const icon = constraint.satisfied ? '✅' : '❌';
        channel.appendLine(`  ${icon} ${constraint.name}`);
        if (!constraint.satisfied && constraint.warnings) {
            constraint.warnings.forEach(w => {
                channel.appendLine(`     ${w}`);
            });
        }
    }
    channel.appendLine('');
    if (failed === 0) {
        vscode.window.showInformationMessage(`✅ All ${total} constraints passed!`);
    }
    else {
        vscode.window.showWarningMessage(`❌ ${failed}/${total} constraints failed`);
    }
}
/**
 * After the user presses Enter on an 'inv ...:' line, automatically opens
 * the suggestion widget on the new blank/indented line below.
 */
function triggerSuggestAfterInvNewline(editor, event) {
    for (const change of event.contentChanges) {
        if (!change.text.includes('\n'))
            continue;
        const newLine = change.range.start.line + 1;
        const doc = editor.document;
        if (newLine >= doc.lineCount)
            continue;
        // The new line must be empty / whitespace only.
        const newLineText = doc.lineAt(newLine).text;
        if (newLineText.trim() !== '')
            continue;
        // Walk upward from the new line: only suggest annotations when every non-blank
        // line between here and the 'inv ...:' header is itself an annotation line.
        // The moment we hit any other content (OCL body), we stop.
        let inAnnotationZone = false;
        for (let i = newLine - 1; i >= 0; i--) {
            const lineText = doc.lineAt(i).text;
            const trimmed = lineText.trim();
            if (trimmed === '')
                continue; // blank lines are fine
            if (/\binv\s+\w+\s*:/.test(lineText)) {
                inAnnotationZone = true; // reached the inv header — we're in the zone
                break;
            }
            if (/^\s*@(severity|message)\b/.test(lineText))
                continue; // other annotation — ok
            break; // anything else means we're in the OCL body
        }
        if (!inAnnotationZone)
            continue;
        // Move cursor to end of indentation and fire suggest.
        const indentEnd = newLineText.length;
        const pos = new vscode.Position(newLine, indentEnd);
        editor.selection = new vscode.Selection(pos, pos);
        vscode.commands.executeCommand('editor.action.triggerSuggest');
        break;
    }
}
function deactivate() {
    currentDecorations.forEach(decoration => decoration.dispose());
    return languageClient?.stop();
}
// ---------------------------------------------------------------------------
// Language client (LSP)
// ---------------------------------------------------------------------------
function startLanguageClient(context) {
    const serverJar = findLanguageServerJar(context);
    if (!serverJar) {
        console.warn('[OCL-LS] language-server.jar not found — LSP features disabled.');
        return;
    }
    const serverOptions = {
        command: 'java',
        args: ['-jar', serverJar],
    };
    const clientOptions = {
        documentSelector: [{ language: 'vitruvocl' }],
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/*.ecore'),
        },
    };
    languageClient = new node_1.LanguageClient('vitruvocl-ls', 'VitruvOCL Language Server', serverOptions, clientOptions);
    languageClient.start();
    context.subscriptions.push(languageClient);
}
function findLanguageServerJar(context) {
    const config = vscode.workspace.getConfiguration('vitruvocl');
    const configured = config.get('languageServerPath');
    if (configured && fs.existsSync(configured))
        return configured;
    const bundled = path.join(context.extensionPath, 'lib', 'language-server.jar');
    if (fs.existsSync(bundled))
        return bundled;
    return null;
}
//# sourceMappingURL=extension.js.map