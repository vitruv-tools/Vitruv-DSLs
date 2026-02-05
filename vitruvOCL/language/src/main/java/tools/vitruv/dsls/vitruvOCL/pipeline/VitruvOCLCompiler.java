/*******************************************************************************
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
package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

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

  /** Metamodel and instance access interface */
  private final MetamodelWrapperInterface wrapper;

  /** Optional OCL file path (for file-based compilation) */
  private final Path oclFile;

  /** Accumulates errors across all passes */
  private final ErrorCollector errors = new ErrorCollector();

  /**
   * Creates compiler with metamodel access.
   *
   * @param wrapper Interface to metamodels and instances
   * @param oclFile Optional file path for file-based compilation
   */
  public VitruvOCLCompiler(MetamodelWrapperInterface wrapper, Path oclFile) {
    this.wrapper = wrapper;
    this.oclFile = oclFile;
  }

  /**
   * Compiles constraint from path (treating path as string source).
   *
   * @param sourcePath Path object used as constraint source
   * @return Evaluation result, or null if any pass fails
   */
  public Value compile(Path sourcePath) {
    String source = sourcePath.toString();
    CharStream input = CharStreams.fromString(source);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) return null;

    // Initialize 3-pass architecture
    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();

    // PASS 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) return null;

    // PASS 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (errors.hasErrors()) return null;

    // PASS 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    return evaluator.visit(tree);
  }

  /**
   * Compiles constraint from configured file path.
   *
   * @return Validation result with errors (evaluation results not captured)
   * @throws IOException If file cannot be read
   */
  public ValidationResult compile() throws IOException {
    CharStream input = CharStreams.fromPath(oclFile);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    // Initialize 3-pass architecture
    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();

    // PASS 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, wrapper, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    // PASS 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    // PASS 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    evaluator.visit(tree);

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
    CharStream input = CharStreams.fromString(oclSource);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.contextDeclCS();

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
      System.out.println("Errors After Pass 1 (Symbol Table): " + errors.getErrorCount());
      errors
          .getErrors()
          .forEach(
              err -> System.err.println("  " + err.getMessage() + " at line " + err.getLine()));
      return null;
    }

    // PASS 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, wrapper, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      System.out.println("Errors After Pass 2 (Type Checking): " + errors.getErrorCount());
      errors
          .getErrors()
          .forEach(
              err -> System.err.println("  " + err.getMessage() + " at line " + err.getLine()));
      return null;
    }

    // PASS 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    if (errors.hasErrors()) {
      System.out.println("Errors After Pass 3 (Evaluation): " + errors.getErrorCount());
      errors
          .getErrors()
          .forEach(
              err -> System.err.println("  " + err.getMessage() + " at line " + err.getLine()));
      return null;
    }

    return result;
  }

  /**
   * @return {@code true} if any compilation errors occurred
   */
  public boolean hasErrors() {
    return errors.hasErrors();
  }

  /**
   * @return Error collector with all accumulated errors
   */
  public ErrorCollector getErrors() {
    return errors;
  }
}