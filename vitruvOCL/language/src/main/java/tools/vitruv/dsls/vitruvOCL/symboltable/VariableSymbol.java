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

import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Symbol representing a variable in the symbol table.
 *
 * <p>{@code VariableSymbol}s are used to model different kinds of variables, including:
 *
 * <ul>
 *   <li>Local variables
 *   <li>Iterator variables
 *   <li>Parameters
 * </ul>
 *
 * <p>During compilation, variable symbols participate in multiple phases:
 *
 * <ul>
 *   <li><b>Symbol table construction:</b> variables are declared and associated with a scope
 *   <li><b>Type checking:</b> the variable type may be refined or inferred
 *   <li><b>Evaluation:</b> the variable may be associated with a runtime value
 * </ul>
 *
 * @see Symbol
 */
public class VariableSymbol extends Symbol {

  /**
   * Indicates whether this variable is an iterator variable.
   *
   * <p>Iterator variables are introduced by iterator expressions (e.g., {@code select}, {@code
   * collect}) and typically range over collection elements.
   */
  private final boolean isIteratorVariable;

  /**
   * The runtime value of this variable.
   *
   * <p>This field is {@code null} during symbol table construction and type checking, and is only
   * set during the evaluation phase.
   */
  private Value value;

  /**
   * Creates a new variable symbol.
   *
   * @param name the name of the variable
   * @param type the initial type of the variable
   * @param definingScope the scope in which the variable is defined
   * @param isIteratorVariable {@code true} if this variable is an iterator variable
   */
  public VariableSymbol(String name, Type type, Scope definingScope, boolean isIteratorVariable) {
    super(name, type, definingScope);
    this.isIteratorVariable = isIteratorVariable;
    this.value = null;
  }

  /**
   * Returns whether this variable is an iterator variable.
   *
   * @return {@code true} if this is an iterator variable, {@code false} otherwise
   */
  public boolean isIteratorVariable() {
    return isIteratorVariable;
  }

  /**
   * Returns the runtime value of this variable.
   *
   * <p>This method is used during the evaluation phase.
   *
   * @return the runtime value, or {@code null} if no value has been assigned
   */
  public Value getValue() {
    return value;
  }

  /**
   * Sets the runtime value of this variable.
   *
   * <p>This method is used during the evaluation phase.
   *
   * @param value the runtime value to assign
   */
  public void setValue(Value value) {
    this.value = value;
  }
}
