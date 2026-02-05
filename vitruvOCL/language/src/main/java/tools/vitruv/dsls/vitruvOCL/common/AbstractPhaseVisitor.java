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
package tools.vitruv.dsls.vitruvOCL.common;

import org.antlr.v4.runtime.ParserRuleContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLBaseVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.Symbol;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Abstract base class for visitor implementations used across compilation phases.
 *
 * <p>Provides shared infrastructure for both type checking (Pass 2) and runtime evaluation (Pass 3)
 * phases, including:
 *
 * <ul>
 *   <li>Unified symbol table access for variable resolution
 *   <li>Centralized error collection and reporting
 *   <li>Access to metamodel information via VSUM wrapper
 * </ul>
 *
 * <p>The generic type parameter {@code T} allows phase-specific return types: {@link
 * tools.vitruv.dsls.vitruvOCL.common.Type} for type checking and {@link
 * tools.vitruv.dsls.vitruvOCL.evaluator.Value} for evaluation.
 *
 * <p>Subclasses must implement {@link #handleUndefinedSymbol(String, ParserRuleContext)} to provide
 * phase-appropriate error handling for unresolved variables.
 *
 * @param <T> Return type of visitor methods - {@code Type} for type checking phase, {@code Value}
 *     for evaluation phase
 * @see TypeCheckVisitor for Pass 2 implementation (type checking)
 * @see EvaluationVisitor for Pass 3 implementation (runtime evaluation)
 */
public abstract class AbstractPhaseVisitor<T> extends VitruvOCLBaseVisitor<T> {

  /** Symbol table for variable resolution across scopes */
  protected final SymbolTable symbolTable;

  /** Collector for compilation errors and warnings */
  protected final ErrorCollector errors;

  /** Interface to VSUM for metamodel and EObject access */
  protected final MetamodelWrapperInterface specification;

  /**
   * Constructs a phase visitor with required compiler infrastructure.
   *
   * @param symbolTable Symbol table containing variable bindings
   * @param specification VSUM wrapper for metamodel access
   * @param errors Error collector for this compilation phase
   */
  protected AbstractPhaseVisitor(
      SymbolTable symbolTable, MetamodelWrapperInterface specification, ErrorCollector errors) {
    this.symbolTable = symbolTable;
    this.specification = specification;
    this.errors = errors;
  }

  /**
   * Resolves a variable name to its symbol table entry.
   *
   * <p>Searches the symbol table for the given variable name, invoking phase-specific error
   * handling if the symbol cannot be resolved.
   *
   * @param name Variable name to resolve
   * @param ctx Parse tree context for error reporting
   * @return Resolved symbol, or {@code null} if not found (after recording error)
   */
  protected Symbol resolveSymbol(String name, ParserRuleContext ctx) {
    Symbol symbol = symbolTable.resolveVariable(name);
    if (symbol == null) {
      handleUndefinedSymbol(name, ctx);
    }
    return symbol;
  }

  /**
   * Handles undefined symbol errors in a phase-specific manner.
   *
   * <p>Type checking phase may record a type error, while evaluation phase may throw a runtime
   * exception or return a sentinel value.
   *
   * @param name The undefined variable name
   * @param ctx Parse tree context for error location information
   */
  protected abstract void handleUndefinedSymbol(String name, ParserRuleContext ctx);

  /**
   * Checks whether any errors were collected during this phase.
   *
   * @return {@code true} if errors were recorded, {@code false} otherwise
   */
  public boolean hasErrors() {
    return errors.hasErrors();
  }

  /**
   * Returns the error collector for this compilation phase.
   *
   * @return Error collector containing all recorded errors and warnings
   */
  public ErrorCollector getErrorCollector() {
    return errors;
  }
}