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
 * Global scope implementation for the VitruvOCL symbol table.
 *
 * <p>The global scope serves as the root of the symbol table hierarchy and contains:
 *
 * <ul>
 *   <li><b>Built-in types:</b> Primitive types (Integer, Real, String, Boolean) and collection
 *       types (Collection, Set, Bag, Sequence) defined during symbol table initialization
 *   <li><b>Standard library operations:</b> Built-in operations like arithmetic operators (+, -, *,
 *       /), collection operations (size, isEmpty, first, last, at), string operations (concat,
 *       substring, toUpper, toLower), and iterator operations (select, reject, collect, forAll,
 *       exists)
 *   <li><b>Global variables:</b> Optional global variable definitions (currently unused but
 *       supported for future extensions)
 * </ul>
 *
 * <p>The global scope has no enclosing scope and serves as the ultimate fallback for symbol
 * resolution. Local scopes created during constraint evaluation chain to the global scope through
 * their parent references, enabling lexical scoping with proper shadowing semantics.
 *
 * <p><b>Symbol Resolution:</b> When a symbol is not found in the global scope, resolution fails and
 * returns {@code null}. This differs from local scopes which delegate to their enclosing scope when
 * a symbol is not found locally.
 *
 * @see Scope
 * @see LocalScope
 * @see SymbolTable
 */
public class GlobalScope implements Scope {

  /** Variables defined at global scope (currently used for future extensions). */
  private final Map<String, VariableSymbol> variables = new HashMap<>();

  /** Built-in types: Integer, Real, String, Boolean, Collection, Set, Bag, Sequence. */
  private final Map<String, TypeSymbol> types = new HashMap<>();

  /** Standard library operations: arithmetic, comparison, collection, string, and iterators. */
  private final Map<String, OperationSymbol> operations = new HashMap<>();

  /**
   * Defines a variable in the global scope.
   *
   * <p>If a variable with the same name already exists, it is replaced (shadowed) by the new
   * definition.
   *
   * @param symbol the variable symbol to define
   */
  @Override
  public void defineVariable(VariableSymbol symbol) {
    variables.put(symbol.getName(), symbol);
  }

  /**
   * Defines a type in the global scope.
   *
   * <p>Used during symbol table initialization to register built-in VitruvOCL types.
   *
   * @param symbol the type symbol to define
   */
  @Override
  public void defineType(TypeSymbol symbol) {
    types.put(symbol.getName(), symbol);
  }

  /**
   * Defines an operation in the global scope.
   *
   * <p>Used during symbol table initialization to register standard library operations.
   *
   * @param symbol the operation symbol to define
   */
  @Override
  public void defineOperation(OperationSymbol symbol) {
    operations.put(symbol.getName(), symbol);
  }

  /**
   * Resolves a variable name in the global scope.
   *
   * @param name the variable name to resolve
   * @return the variable symbol, or {@code null} if not found
   */
  @Override
  public VariableSymbol resolveVariable(String name) {
    return variables.get(name);
  }

  /**
   * Resolves a type name in the global scope.
   *
   * @param name the type name to resolve (e.g., "Integer", "Collection", "String")
   * @return the type symbol, or {@code null} if not found
   */
  @Override
  public TypeSymbol resolveType(String name) {
    return types.get(name);
  }

  /**
   * Resolves an operation name in the global scope.
   *
   * @param name the operation name to resolve (e.g., "+", "size", "select")
   * @return the operation symbol, or {@code null} if not found
   */
  @Override
  public OperationSymbol resolveOperation(String name) {
    return operations.get(name);
  }

  /**
   * Returns the enclosing scope.
   *
   * <p>The global scope has no enclosing scope, so this method always returns {@code null}.
   *
   * @return {@code null} (global scope is the root of the scope hierarchy)
   */
  @Override
  public Scope getEnclosingScope() {
    return null;
  }
}