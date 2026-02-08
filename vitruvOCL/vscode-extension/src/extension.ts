import * as vscode from 'vscode';
import * as child_process from 'child_process';
import * as util from 'util';
import * as fs from 'fs';
import * as path from 'path';

const execFile = util.promisify(child_process.execFile);

const constraintResults = new Map<string, ConstraintStatus>();
let codeLensProvider: VitruvOCLCodeLensProvider;
let currentDecorations: Map<string, vscode.TextEditorDecorationType> = new Map();
let diagnosticCollection: vscode.DiagnosticCollection;

interface ConstraintStatus {
    name: string;
    passed: boolean;
    ran: boolean;
}

interface EvalResult {
    success: boolean;
    satisfied: boolean;
    errors: Array<{ line: number, column: number, message: string, severity: string }>;
    warnings: string[];
}

interface BatchEvalResult {
    success: boolean;
    constraints: ConstraintBatchResult[];
}

interface ConstraintBatchResult {
    name: string;
    success: boolean;
    satisfied: boolean;
    errors?: Array<{ line: number, column: number, message: string }>;
    warnings?: string[];
}

export function activate(context: vscode.ExtensionContext) {
    console.log('VitruvOCL extension activated');

    diagnosticCollection = vscode.languages.createDiagnosticCollection('vitruvocl');
    context.subscriptions.push(diagnosticCollection);

    codeLensProvider = new VitruvOCLCodeLensProvider();

    context.subscriptions.push(
        vscode.languages.registerCodeLensProvider(
            { language: 'vitruvocl' },
            codeLensProvider
        )
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('vitruvocl.runConstraint', async (constraintName, documentUri) => {
            await runConstraint(constraintName, documentUri);
        })
    );

    context.subscriptions.push(
        vscode.commands.registerCommand('vitruvocl.runAllConstraints', async () => {
            await runAllConstraints();
        })
    );

    context.subscriptions.push(
        vscode.window.onDidChangeActiveTextEditor(editor => {
            if (editor && editor.document.languageId === 'vitruvocl') {
                updateGutterIcons(editor);
                updateInlineErrors(editor);
            }
        })
    );

    context.subscriptions.push(
        vscode.workspace.onDidChangeTextDocument(event => {
            const editor = vscode.window.activeTextEditor;
            if (editor && editor.document === event.document && editor.document.languageId === 'vitruvocl') {
                updateGutterIcons(editor);
                updateInlineErrors(editor);
            }
        })
    );

    if (vscode.window.activeTextEditor?.document.languageId === 'vitruvocl') {
        updateGutterIcons(vscode.window.activeTextEditor);
        updateInlineErrors(vscode.window.activeTextEditor);
    }

    console.log('✓ VitruvOCL extension ready');
}

function updateGutterIcons(editor: vscode.TextEditor) {
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

            let iconUri: vscode.Uri;

            if (!status || !status.ran) {
                iconUri = createRunIcon();
            } else if (status.passed) {
                iconUri = createPassIcon();
            } else {
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

function updateInlineErrors(editor: vscode.TextEditor, constraintDetails?: Map<string, string[]>) {
    const oldInlineDecoration = currentDecorations.get('inline_errors');
    if (oldInlineDecoration) {
        oldInlineDecoration.dispose();
    }

    const text = editor.document.getText();
    const lines = text.split('\n');
    const inlineDecorations: vscode.DecorationOptions[] = [];

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
                let detailedErrorText = `${constraintName} violated`;
                const details = constraintDetails?.get(constraintName);
                if (details && details.length > 0) {
                    detailedErrorText = details[0];
                    if (details.length > 1) {
                        detailedErrorText += ` (+${details.length - 1} more)`;
                    }
                }


                const hover = new vscode.MarkdownString();

                hover.appendMarkdown(`**❌ Constraint violated**\n\n`);

                if (details && details.length > 0) {
                    for (const d of details) {
                        hover.appendMarkdown(`- ${d}\n`);
                    }
                } else {
                    hover.appendMarkdown(`${constraintName} violated`);
                }

                hover.isTrusted = true;

                const decoration: vscode.DecorationOptions = {
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

function createRunIcon(): vscode.Uri {
    return vscode.Uri.parse('data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="none" stroke="#858585" stroke-width="1.5"/>
            <path d="M 6 5 L 6 11 L 11 8 Z" fill="#858585"/>
        </svg>
    `));
}

function createPassIcon(): vscode.Uri {
    return vscode.Uri.parse('data:image/svg+xml;utf8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16">
            <circle cx="8" cy="8" r="7" fill="#73C991"/>
            <path d="M 5 8 L 7 10 L 11 6" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
    `));
}

function createFailIcon(): vscode.Uri {
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
        vscode.window.showErrorMessage('No VitruvOCL file open');
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

        const ecoreFiles = await findEcoreFiles();
        const instanceFiles = await findInstanceFiles();

        if (ecoreFiles.length === 0 || instanceFiles.length === 0) {
            vscode.window.showWarningMessage('Missing .ecore or instance files');
            return;
        }

        vscode.window.showInformationMessage('Running all constraints...');

        const { stdout, stderr } = await execFile('java', [
            '-jar', compilerPath,
            'eval-batch',
            document.fileName,
            '--ecore', ecoreFiles.join(','),
            '--xmi', instanceFiles.join(',')
        ]).catch(err => ({
            stdout: err.stdout || '',
            stderr: err.stderr || err.message
        }));

        console.log('Batch eval STDOUT:', stdout);
        if (stderr) console.log('Batch eval STDERR:', stderr);

        const result = JSON.parse(stdout) as BatchEvalResult;

        if (!result.success) {
            vscode.window.showErrorMessage('Batch evaluation failed');
            return;
        }

        let passed = 0;
        let failed = 0;
        const constraintDetails = new Map<string, string[]>();

        for (const constraint of result.constraints) {
            constraintResults.set(constraint.name, {
                name: constraint.name,
                passed: constraint.satisfied,
                ran: true
            });

            updateDiagnosticsForConstraintBatch(document, constraint);

            if (constraint.satisfied) {
                passed++;
            } else {
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

    } catch (error: any) {
        vscode.window.showErrorMessage(`Run all failed: ${error.message}`);
        console.error('Full error:', error);
    }
}

async function runConstraint(constraintName: string, documentUri: vscode.Uri) {
    const document = await vscode.workspace.openTextDocument(documentUri);
    const editor = vscode.window.visibleTextEditors.find(e => e.document === document);

    try {
        const compilerPath = await findCompilerJar();
        if (!compilerPath) {
            vscode.window.showErrorMessage('vitruvocl.jar not found');
            return;
        }

        const ecoreFiles = await findEcoreFiles();
        const instanceFiles = await findInstanceFiles();

        if (ecoreFiles.length === 0 || instanceFiles.length === 0) {
            vscode.window.showWarningMessage('Missing files');
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

        const result = JSON.parse(stdout) as EvalResult;

        constraintResults.set(constraintName, {
            name: constraintName,
            passed: result.satisfied,
            ran: true
        });

        updateDiagnosticsForConstraint(document, constraintName, result);

        codeLensProvider.refresh();
        if (editor) {
            updateGutterIcons(editor);
            const details = new Map<string, string[]>();
            if (!result.satisfied && result.warnings) {
                details.set(constraintName, result.warnings);
            }
            updateInlineErrors(editor, details);
        }

        showEvalResult(constraintName, result);
        fs.unlinkSync(tempFile);

    } catch (error: any) {
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

function updateDiagnosticsForConstraint(
    document: vscode.TextDocument,
    constraintName: string,
    result: EvalResult
) {
    const diagnostics: vscode.Diagnostic[] = [];

    if (!result.success) {
        // Nur Compilation Errors anzeigen
        for (const error of result.errors) {
            const line = Math.max(0, error.line - 1);
            const range = new vscode.Range(line, 0, line, 1000);
            const diagnostic = new vscode.Diagnostic(
                range,
                `Compilation Error: ${error.message}`,
                vscode.DiagnosticSeverity.Error
            );
            diagnostic.source = 'VitruvOCL';
            diagnostics.push(diagnostic);
        }
    } else if (!result.satisfied) {
        // intentionally no diagnostics for constraint violations
        // Violations werden nur inline angezeigt
    }

    // Convert readonly to mutable array
    const existingDiagnostics = Array.from(diagnosticCollection.get(document.uri) || []);
    const otherDiagnostics = existingDiagnostics.filter(d => {
        const line = d.range.start.line;
        const constraintAtLine = getConstraintNameAtLine(document.getText(), line);
        return constraintAtLine !== constraintName;
    });

    diagnosticCollection.set(document.uri, [...otherDiagnostics, ...diagnostics]);
}

function updateDiagnosticsForConstraintBatch(
    document: vscode.TextDocument,
    constraint: ConstraintBatchResult
) {
    // FIX: Convert readonly to mutable array
    const diagnostics: vscode.Diagnostic[] = Array.from(diagnosticCollection.get(document.uri) || []);

    if (!constraint.success) {
        if (constraint.errors) {
            for (const error of constraint.errors) {
                const line = Math.max(0, error.line - 1);
                const range = new vscode.Range(line, 0, line, 1000);
                const diagnostic = new vscode.Diagnostic(
                    range,
                    `Compilation Error: ${error.message}`,
                    vscode.DiagnosticSeverity.Error
                );
                diagnostic.source = 'VitruvOCL';
                diagnostics.push(diagnostic);
            }
        }
    } else if (!constraint.satisfied) {
        // intentionally no diagnostics for constraint violations
    }


    diagnosticCollection.set(document.uri, diagnostics);
}

function findConstraintLine(text: string, constraintName: string): number {
    const lines = text.split('\n');
    for (let i = 0; i < lines.length; i++) {
        const match = lines[i].match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
        if (match && match[2] === constraintName) {
            return i;
        }
    }
    return -1;
}

function getConstraintNameAtLine(text: string, line: number): string | null {
    const lines = text.split('\n');
    if (line >= 0 && line < lines.length) {
        const match = lines[line].match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);
        if (match) {
            return match[2];
        }
    }
    return null;
}

class VitruvOCLCodeLensProvider implements vscode.CodeLensProvider {
    private _onDidChangeCodeLenses = new vscode.EventEmitter<void>();
    public readonly onDidChangeCodeLenses = this._onDidChangeCodeLenses.event;

    refresh() {
        this._onDidChangeCodeLenses.fire();
    }

    provideCodeLenses(document: vscode.TextDocument): vscode.CodeLens[] {
        const codeLenses: vscode.CodeLens[] = [];
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
            codeLenses.push(new vscode.CodeLens(
                new vscode.Range(firstContextLine, 0, firstContextLine, 0),
                {
                    title: '▶ Run All',
                    command: 'vitruvocl.runAllConstraints'
                }
            ));
        }

        for (let i = 0; i < lines.length; i++) {
            const line = lines[i];
            const match = line.match(/^\s*context\s+(\S+)\s+inv\s+(\w+)\s*:/);

            if (match) {
                const constraintName = match[2];

                codeLenses.push(new vscode.CodeLens(
                    new vscode.Range(i, 0, i, line.length),
                    {
                        title: '▶',
                        command: 'vitruvocl.runConstraint',
                        arguments: [constraintName, document.uri]
                    }
                ));
            }
        }

        return codeLenses;
    }
}

function extractConstraint(text: string, constraintName: string): string | null {
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
            constraint += line + '\n';
            if (line.trim().startsWith('context')) {
                constraint = constraint.substring(0, constraint.length - line.length - 1);
                break;
            }
        }
    }

    return constraint.trim() || null;
}

async function writeTempConstraint(constraint: string): Promise<string> {
    const os = await import('os');
    const tempDir = os.tmpdir();
    const tempFile = path.join(tempDir, `vitruvocl-${Date.now()}.ocl`);
    fs.writeFileSync(tempFile, constraint, 'utf-8');
    return tempFile;
}

async function findCompilerJar(): Promise<string | null> {
    const config = vscode.workspace.getConfiguration('vitruvocl');
    const configured = config.get<string>('compilerPath');
    if (configured && fs.existsSync(configured)) return configured;

    const extPath = vscode.extensions.getExtension('vitruvius.vitruvocl')?.extensionPath;
    if (extPath) {
        const bundled = path.join(extPath, 'lib', 'vitruvocl.jar');
        if (fs.existsSync(bundled)) return bundled;
    }

    return null;
}

async function findEcoreFiles(): Promise<string[]> {
    const files = await vscode.workspace.findFiles('**/metamodels/*.ecore');
    return files.map(uri => uri.fsPath);
}

async function findInstanceFiles(): Promise<string[]> {
    const files = await vscode.workspace.findFiles('**/instances/*.*');
    return files.map(uri => uri.fsPath);
}

let outputChannel: vscode.OutputChannel | undefined;

function showEvalResult(name: string, result: EvalResult) {
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
    } else if (result.satisfied) {
        channel.appendLine('✅ CONSTRAINT SATISFIED\nAll instances pass.');
    } else {
        channel.appendLine('❌ CONSTRAINT VIOLATED');
        if (result.warnings.length > 0) {
            channel.appendLine('\nViolations:');
            result.warnings.forEach(w => channel.appendLine(`  • ${w}`));
        }
    }
    channel.appendLine('');
}

function showSummaryResult(passed: number, failed: number, constraints: ConstraintBatchResult[]) {
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
    } else {
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
    } else {
        vscode.window.showWarningMessage(`❌ ${failed}/${total} constraints failed`);
    }
}

export function deactivate() {
    currentDecorations.forEach(decoration => decoration.dispose());
}