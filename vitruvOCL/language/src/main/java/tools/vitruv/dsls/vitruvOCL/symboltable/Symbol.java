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

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Abstract base class for all symbols stored in the symbol table.
 *
 * <p>A {@code Symbol} represents a named program element that can be declared in a {@link Scope}.
 * Concrete subclasses model different kinds of symbols, such as:
 *
 * <ul>
 *   <li>{@link VariableSymbol}: local variables, iterator variables, parameters
 *   <li>{@link TypeSymbol}: metaclasses from the metamodel
 *   <li>{@link OperationSymbol}: OCL operations
 * </ul>
 *
 * <p>Each symbol has a name, an associated {@link Type}, and a defining scope. The type may be
 * refined during later compiler phases.
 *
 * @see VariableSymbol
 * @see TypeSymbol
 * @see OperationSymbol
 */
public abstract class Symbol {

  /** The name of the symbol as it appears in the source code. */
  protected final String name;

  /**
   * The type of this symbol.
   *
   * <p>This field is intentionally not final, as the type may be refined during a later compiler
   * pass (e.g., type checking).
   */
  protected Type type;

  /** The scope in which this symbol is defined. */
  protected final Scope definingScope;

  /**
   * Creates a new symbol.
   *
   * @param name the name of the symbol
   * @param type the initial type of the symbol
   * @param definingScope the scope in which the symbol is defined
   */
  protected Symbol(String name, Type type, Scope definingScope) {
    this.name = name;
    this.type = type;
    this.definingScope = definingScope;
  }

  /**
   * Returns the name of this symbol.
   *
   * @return the symbol name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current type of this symbol.
   *
   * @return the symbol type
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets or refines the type of this symbol.
   *
   * <p>This method is typically used in the second compiler pass (type checking) to refine types
   * that were initially set to {@code Type.ANY} during the first pass (symbol table construction).
   *
   * <p>Typical examples include:
   *
   * <ul>
   *   <li>Iterator variables refined from {@code Type.ANY} to their element type
   *   <li>{@code let} variables without an explicit type refined to an inferred type
   * </ul>
   *
   * @param type the refined type of the symbol
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Returns the scope in which this symbol was defined.
   *
   * @return the defining scope
   */
  public Scope getDefiningScope() {
    return definingScope;
  }
}
