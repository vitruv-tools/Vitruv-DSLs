/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * 3-pass compiler for VitruvOCL constraints.
 *
 * <p>Executes compilation pipeline:
 *
 * <ol>
 *   <li><b>Pass 1 - Symbol Table Construction</b>: Collects variable declarations and scopes
 *   <li><b>Pass 2 - Type Checking</b>: Validates types and builds nodeTypes mapping
 *   <li><b>Pass 3 - Evaluation</b>: Executes constraint against model instances
 * </ol>
 *
 * <p>Each pass accumulates errors in {@link ErrorCollector}. If any pass encounters errors,
 * subsequent passes are skipped. Supports compilation from strings, files, or paths.
 */
public class VitruvOCLCompiler {

  /** Metamodel and instance access interface. */
  private final MetamodelWrapperInterface wrapper;

  /** Optional OCL file path (for file-based compilation). */
  private final Path oclFile;

  /** Accumulates errors across all passes. */
  private final ErrorCollector errors = new ErrorCollector();

  /**
   * The transaction (ordered atomic changes between pre-state and current post-state) that {@code
   * @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code OCLisDeleted} are evaluated against. Empty
   * by default — see {@link tools.vitruv.dsls.vitruvocl.evaluator.transaction.TransactionModel}.
   */
  private final List<EChange<EObject>> transaction;

  /**
   * Whether {@link #transaction} reflects a real transaction context — {@code true} only when the
   * explicit-transaction constructor was used (even with an empty list). {@code false} for the
   * no-argument constructor, used by every caller with no notion of a transaction at all (CLI, VS
   * Code plugin): there, {@code pre}/{@code post} blocks are skipped rather than evaluated with
   * vacuous empty-transaction semantics — see {@code EvaluationVisitor#transactionSupported}.
   */
  private final boolean transactionSupported;

  /**
   * Last evaluator instance — available after any {@code compile} call for violation introspection.
   *
   * <p>Callers can use {@code getLastEvaluator().getViolatingInstances()} to retrieve the concrete
   * EObjects that violated the constraint, enabling precise per-instance violation messages.
   */
  private EvaluationVisitor lastEvaluator = null;

  /**
   * Creates compiler with metamodel access and no transaction support — {@code pre}/{@code post}
   * blocks are skipped during evaluation, only {@code inv} is evaluated.
   *
   * @param wrapper Interface to metamodels and instances
   * @param oclFile Optional file path for file-based compilation
   */
  public VitruvOCLCompiler(MetamodelWrapperInterface wrapper, Path oclFile) {
    this.wrapper = wrapper;
    this.oclFile = oclFile;
    this.transaction = List.of();
    this.transactionSupported = false;
  }

  /**
   * Creates compiler with metamodel access and an explicit transaction — {@code pre}/{@code post}
   * blocks are evaluated against {@code transaction} (which may itself be empty).
   *
   * @param wrapper Interface to metamodels and instances
   * @param oclFile Optional file path for file-based compilation
   * @param transaction the ordered list of atomic changes between pre-state and the current
   *     post-state, evaluated by {@code @pre}/{@code OCLisNew}/{@code OCLisModified}/{@code
   *     OCLisDeleted} (may be empty)
   */
  public VitruvOCLCompiler(
      MetamodelWrapperInterface wrapper, Path oclFile, List<EChange<EObject>> transaction) {
    this.wrapper = wrapper;
    this.oclFile = oclFile;
    this.transaction = transaction == null ? List.of() : transaction;
    this.transactionSupported = true;
  }

  /**
   * Compiles constraint from path (treating path as string source).
   *
   * @param sourcePath Path object used as constraint source
   * @return Evaluation result, or null if any pass fails
   */
  public Value compile(Path sourcePath) {
    return runPipeline(CharStreams.fromString(sourcePath.toString()));
  }

  /**
   * Compiles constraint from configured file path.
   *
   * @return Validation result with errors (evaluation results not captured)
   * @throws IOException If file cannot be read
   */
  public ValidationResult compile() throws IOException {
    runPipeline(CharStreams.fromPath(oclFile));
    return new ValidationResult(errors.getErrors(), java.util.List.of());
  }

  /**
   * Compiles constraint from string source with debug output.
   *
   * <p>Prints error messages to stderr after each pass for debugging.
   *
   * @param oclSource OCL constraint expression
   * @return Evaluation result, or null if any pass fails
   */
  public Value compile(String oclSource) {
    Value result = runPipeline(CharStreams.fromString(oclSource));

    if (errors.hasErrors()) {
      return null;
    }

    return result;
  }

  /**
   * Runs the full 3-pass pipeline on the given input and returns the evaluation result.
   *
   * <p>Accumulates errors in {@link #errors}; returns {@code null} if parsing fails or any pass
   * before evaluation reports errors. Sets {@link #lastEvaluator} when evaluation runs.
   *
   * @param input character stream of the constraint source
   * @return the evaluation {@link Value}, or {@code null} if a pass before evaluation failed
   */
  private Value runPipeline(CharStream input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) {
      return null;
    }

    // Initialize 3-pass architecture
    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();

    // PASS 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      return null;
    }

    // PASS 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      return null;
    }

    // PASS 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(
            symbolTable,
            wrapper,
            errors,
            typeChecker.getNodeTypes(),
            transaction,
            transactionSupported);
    lastEvaluator = evaluator;
    return evaluator.visit(tree);
  }

  /**
   * Returns whether any compilation errors occurred.
   *
   * @return {@code true} if any compilation errors occurred
   */
  public boolean hasErrors() {
    return errors.hasErrors();
  }

  /**
   * Returns the error collector with all accumulated errors.
   *
   * @return Error collector with all accumulated errors
   */
  public ErrorCollector getErrors() {
    return errors;
  }

  /**
   * Returns the evaluator from the last {@code compile} call.
   *
   * <p>Use {@code getLastEvaluator().getViolatingInstances()} to retrieve the concrete EObjects
   * that violated the constraint, for precise per-instance violation messages.
   *
   * @return The last EvaluationVisitor, or null if no evaluation has run yet
   */
  public EvaluationVisitor getLastEvaluator() {
    return lastEvaluator;
  }
}
