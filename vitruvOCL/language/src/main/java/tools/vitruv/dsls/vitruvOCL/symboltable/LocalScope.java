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
package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * Local scope implementation for nested variable declarations in VitruvOCL.
 *
 * <p>Local scopes are created for nested contexts during constraint evaluation, including:
 *
 * <ul>
 *   <li><b>Let expressions:</b> Variables introduced via {@code let x = expr in body}
 *   <li><b>Iterator variables:</b> Loop variables in operations like {@code select(x | x > 5)},
 *       {@code forAll(s | s.active)}, {@code collect(p | p.name)}
 *   <li><b>Nested iterators:</b> Multiple levels of iteration with independent variable bindings
 * </ul>
 *
 * <p><b>Scope Chaining:</b> Each local scope maintains a reference to its parent scope, forming a
 * scope chain that ultimately reaches the global scope. Variable resolution follows lexical scoping
 * rules: names are first looked up in the current scope, then recursively in parent scopes until
 * found or the global scope is reached.
 *
 * <p><b>Shadowing Semantics:</b> Local variables can shadow (hide) variables from outer scopes with
 * the same name. The innermost definition takes precedence during resolution. The {@link
 * #hasVariableInCurrentScope(String)} method enables duplicate detection within a single scope
 * while allowing legitimate shadowing across scope boundaries.
 *
 * <p><b>Design Constraint:</b> Types and operations cannot be defined in local scopes. All type and
 * operation definitions must reside in the global scope. Attempts to define types or operations
 * locally will throw {@link UnsupportedOperationException}.
 *
 * @see Scope
 * @see GlobalScope
 * @see SymbolTable
 */
public class LocalScope implements Scope {

  /** The enclosing parent scope in the scope chain (ultimately leading to GlobalScope). */
  private final Scope parent;

  /** Variables defined in this local scope (let bindings, iterator variables). */
  private final Map<String, VariableSymbol> variables = new HashMap<>();

  /**
   * Creates a new local scope with the specified parent scope.
   *
   * @param parent the enclosing scope (must not be {@code null})
   */
  public LocalScope(Scope parent) {
    this.parent = parent;
  }

  /**
   * Defines a variable in this local scope.
   *
   * <p>If a variable with the same name already exists in this scope, it is replaced. Variables in
   * parent scopes are not affected (shadowing).
   *
   * @param symbol the variable symbol to define
   */
  @Override
  public void defineVariable(VariableSymbol symbol) {
    variables.put(symbol.getName(), symbol);
  }

  /**
   * Types cannot be defined in local scopes.
   *
   * @param symbol (unused)
   * @throws UnsupportedOperationException always, as types must be defined in GlobalScope
   */
  @Override
  public void defineType(TypeSymbol symbol) {
    // Types can only be defined in GlobalScope
    throw new UnsupportedOperationException(
        "Cannot define types in local scope. Types must be defined in GlobalScope.");
  }

  /**
   * Operations cannot be defined in local scopes.
   *
   * @param symbol (unused)
   * @throws UnsupportedOperationException always, as operations must be defined in GlobalScope
   */
  @Override
  public void defineOperation(OperationSymbol symbol) {
    // Operations can only be defined in GlobalScope
    throw new UnsupportedOperationException(
        "Cannot define operations in local scope. Operations must be defined in GlobalScope.");
  }

  /**
   * Resolves a variable name using lexical scoping rules.
   *
   * <p>Resolution proceeds as follows:
   *
   * <ol>
   *   <li>Check if the variable is defined in this local scope
   *   <li>If not found, recursively check the parent scope
   *   <li>Continue up the scope chain until found or global scope is exhausted
   * </ol>
   *
   * <p>This implements proper lexical scoping with shadowing: the innermost definition takes
   * precedence.
   *
   * @param name the variable name to resolve
   * @return the variable symbol, or {@code null} if not found in any scope
   */
  @Override
  public VariableSymbol resolveVariable(String name) {
    // First check local scope
    VariableSymbol symbol = variables.get(name);
    if (symbol != null) {
      return symbol;
    }
    // Then check parent scope (scope chain)
    if (parent != null) {
      return parent.resolveVariable(name);
    }
    return null;
  }

  /**
   * Resolves a type name by delegating to the parent scope.
   *
   * <p>Since types can only be defined in GlobalScope, this method immediately delegates to the
   * parent scope without checking locally.
   *
   * @param name the type name to resolve
   * @return the type symbol from the scope chain, or {@code null} if not found
   */
  @Override
  public TypeSymbol resolveType(String name) {
    // Types are only in GlobalScope, delegate to parent
    return parent != null ? parent.resolveType(name) : null;
  }

  /**
   * Resolves an operation name by delegating to the parent scope.
   *
   * <p>Since operations can only be defined in GlobalScope, this method immediately delegates to
   * the parent scope without checking locally.
   *
   * @param name the operation name to resolve
   * @return the operation symbol from the scope chain, or {@code null} if not found
   */
  @Override
  public OperationSymbol resolveOperation(String name) {
    // Operations are only in GlobalScope, delegate to parent
    return parent != null ? parent.resolveOperation(name) : null;
  }

  /**
   * Returns the enclosing parent scope.
   *
   * @return the parent scope in the scope chain
   */
  @Override
  public Scope getEnclosingScope() {
    return parent;
  }

  /**
   * Checks if a variable is defined in this scope only (not in parent scopes).
   *
   * <p>This method is used for duplicate variable detection within a single scope while still
   * allowing variables to shadow definitions from outer scopes. For example, this prevents {@code
   * let x = 5 let x = 10} (duplicate in same scope) while allowing {@code let x = 5 in let x = 10}
   * (shadowing across scope boundaries).
   *
   * @param name the variable name to check
   * @return {@code true} if the variable exists in this scope only, {@code false} otherwise
   */
  public boolean hasVariableInCurrentScope(String name) {
    return variables.containsKey(name);
  }
}