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

package tools.vitruv.dsls.vitruvocl.incremental;

import java.util.List;
import java.util.function.Function;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ConstraintAnalysis;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.FeatureIndex;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.PathAnalyzer;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.PolarityAnalyzer;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ReadSet;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ReadSetAnalyzer;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * A constraint text compiled once (parse, symbol table, type check) and evaluable many times
 * (building block A: compile once, evaluate often).
 *
 * <p>The parse tree and the type checker's {@code nodeTypes} are kept and only read afterwards
 * (the evaluator never writes {@code nodeTypes}). The symbol table, however, is mutated during
 * evaluation ({@code self} bindings, iterator and {@code let} scopes), so every {@link #evaluate}
 * builds a fresh one by re-running pass 1 on the cached tree. A single instance must not be
 * evaluated concurrently; the session evaluates each constraint from exactly one task at a time.
 *
 * <p>If compilation fails, the instance only remembers that, and {@link #evaluate} re-runs the
 * standard from-scratch pipeline, so that the reported errors are exactly those of a full
 * evaluation.
 */
final class CompiledConstraint {

  private final String text;
  private final MetamodelWrapperInterface wrapper;
  private final VitruvOCLParser.ContextDeclCSContext tree;
  private final ParseTreeProperty<tools.vitruv.dsls.vitruvocl.typechecker.Type> nodeTypes;
  private final ConstraintAnalysis analysis;

  private CompiledConstraint(
      String text,
      MetamodelWrapperInterface wrapper,
      VitruvOCLParser.ContextDeclCSContext tree,
      ParseTreeProperty<tools.vitruv.dsls.vitruvocl.typechecker.Type> nodeTypes,
      ConstraintAnalysis analysis) {
    this.text = text;
    this.wrapper = wrapper;
    this.tree = tree;
    this.nodeTypes = nodeTypes;
    this.analysis = analysis;
  }

  /** Same as {@link #compile(String, MetamodelWrapperInterface, FeatureIndex)} with a fresh index. */
  static CompiledConstraint compile(String text, MetamodelWrapperInterface wrapper) {
    return compile(text, wrapper, FeatureIndex.forWrapper(wrapper));
  }

  /** Runs passes 1 and 2 once and analyses the result; never throws for invalid constraints. */
  static CompiledConstraint compile(
      String text, MetamodelWrapperInterface wrapper, FeatureIndex features) {
    CommonTokenStream tokens = new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(text)));
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    parser.removeErrorListeners();
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();
    if (parser.getNumberOfSyntaxErrors() > 0) {
      return uncompilable(text, wrapper);
    }

    ErrorCollector errors = new ErrorCollector();
    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    ScopeAnnotator scopes = new ScopeAnnotator();
    new SymbolTableBuilder(symbolTable, wrapper, errors, scopes).visit(tree);
    if (errors.hasErrors()) {
      return uncompilable(text, wrapper);
    }
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors, scopes);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);
    if (errors.hasErrors()) {
      return uncompilable(text, wrapper);
    }
    ParseTreeProperty<tools.vitruv.dsls.vitruvocl.typechecker.Type> nodeTypes =
        typeChecker.getNodeTypes();
    return new CompiledConstraint(
        text, wrapper, tree, nodeTypes, analyse(tree, nodeTypes, features));
  }

  private static ConstraintAnalysis analyse(
      VitruvOCLParser.ContextDeclCSContext tree,
      ParseTreeProperty<tools.vitruv.dsls.vitruvocl.typechecker.Type> nodeTypes,
      FeatureIndex features) {
    ReadSet readSet = ReadSetAnalyzer.analyze(tree, nodeTypes, features);
    return new ConstraintAnalysis(
        readSet,
        PolarityAnalyzer.analyze(tree, nodeTypes, readSet),
        PathAnalyzer.analyze(tree, nodeTypes, readSet));
  }

  private static CompiledConstraint uncompilable(String text, MetamodelWrapperInterface wrapper) {
    return new CompiledConstraint(text, wrapper, null, null, ConstraintAnalysis.forUncompilable());
  }

  String text() {
    return text;
  }

  ConstraintAnalysis analysis() {
    return analysis;
  }

  /** The static context class of a classifier context of this constraint, or {@code null}. */
  org.eclipse.emf.ecore.EClass contextClass(VitruvOCLParser.ClassifierContextCSContext ctx) {
    tools.vitruv.dsls.vitruvocl.typechecker.Type type = nodeTypes == null ? null : nodeTypes.get(ctx);
    return type == null ? null : type.getEClass();
  }

  boolean isCompiled() {
    return tree != null;
  }

  /**
   * Evaluates the constraint on the wrapper's current state.
   *
   * @param transaction the changes of the transaction, for {@code @pre}/lifecycle predicates
   * @param transactionSupported whether pre/post blocks are evaluated ({@code true}) or skipped
   * @param instanceOverride optional: per classifier context, the instances to evaluate instead of
   *     the full extension ({@code null} = full extension, today's behaviour)
   */
  EvaluationOutcome evaluate(
      List<EChange<EObject>> transaction,
      boolean transactionSupported,
      Function<VitruvOCLParser.ClassifierContextCSContext, List<EObject>> instanceOverride) {
    if (!isCompiled()) {
      return tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations.evaluateFresh(
          text, wrapper, transaction, transactionSupported);
    }
    ErrorCollector errors = new ErrorCollector();
    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    new SymbolTableBuilder(symbolTable, wrapper, errors, new ScopeAnnotator()).visit(tree);
    if (errors.hasErrors()) {
      return EvaluationOutcome.failure(errors.getErrors());
    }
    EvaluationVisitor evaluator =
        new EvaluationVisitor(
            symbolTable, wrapper, errors, nodeTypes, transaction, transactionSupported);
    if (instanceOverride != null) {
      evaluator.setContextInstanceOverride(instanceOverride);
    }
    evaluator.visit(tree);
    if (errors.hasErrors()) {
      return EvaluationOutcome.failure(errors.getErrors());
    }
    return new EvaluationOutcome(
        false,
        List.of(),
        evaluator.getViolationRecords(),
        evaluator.getSkippedConstraints(),
        evaluator.getEvaluatedInstanceCount());
  }
}
