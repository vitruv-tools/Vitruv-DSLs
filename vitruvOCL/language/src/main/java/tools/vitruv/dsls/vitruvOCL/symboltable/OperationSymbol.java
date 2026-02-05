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

import java.util.List;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Symbol representing an operation in the VitruvOCL symbol table.
 *
 * <p>Operations include:
 *
 * <ul>
 *   <li><b>Arithmetic operations:</b> {@code +}, {@code -}, {@code *}, {@code /}, {@code %}
 *   <li><b>Comparison operations:</b> {@code <}, {@code <=}, {@code >}, {@code >=}, {@code =},
 *       {@code <>}
 *   <li><b>Boolean operations:</b> {@code and}, {@code or}, {@code not}, {@code implies}
 *   <li><b>Collection operations:</b> {@code size}, {@code isEmpty}, {@code notEmpty}, {@code
 *       first}, {@code last}, {@code at}, {@code includes}, {@code excludes}, {@code union}, {@code
 *       intersection}
 *   <li><b>String operations:</b> {@code concat}, {@code substring}, {@code toUpper}, {@code
 *       toLower}, {@code size}
 *   <li><b>Iterator operations:</b> {@code select}, {@code reject}, {@code collect}, {@code
 *       forAll}, {@code exists}, {@code one}, {@code any}
 * </ul>
 *
 * <p>The {@code isCollectionOperation} flag distinguishes between operations that require a
 * collection receiver (e.g., {@code select}, {@code size}) and operations that work on primitive
 * types (e.g., {@code +}, {@code <}).
 *
 * @see Symbol
 * @see GlobalScope
 */
public class OperationSymbol extends Symbol {

  /** The return type of this operation after execution. */
  private final Type returnType;

  /** The list of parameter types this operation accepts (in order). */
  private final List<Type> parameterTypes;

  /**
   * Whether this is a collection operation requiring a collection receiver (e.g., {@code size},
   * {@code select}).
   */
  private final boolean isCollectionOperation;

  /**
   * Creates a new operation symbol.
   *
   * @param name the operation name (e.g., "+", "size", "select")
   * @param type the type of the operation itself
   * @param definingScope the scope where this operation is defined (typically GlobalScope)
   * @param returnType the return type after operation execution
   * @param parameterTypes the list of parameter types (in order)
   * @param isCollectionOperation {@code true} if this operation requires a collection receiver,
   *     {@code false} otherwise
   */
  public OperationSymbol(
      String name,
      Type type,
      Scope definingScope,
      Type returnType,
      List<Type> parameterTypes,
      boolean isCollectionOperation) {
    super(name, type, definingScope);
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.isCollectionOperation = isCollectionOperation;
  }

  /**
   * Returns the return type of this operation.
   *
   * @return the type returned after operation execution
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Returns the parameter types of this operation.
   *
   * @return an immutable list of parameter types (in order)
   */
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

  /**
   * Checks if this is a collection operation.
   *
   * @return {@code true} if this operation requires a collection receiver, {@code false} otherwise
   */
  public boolean isCollectionOperation() {
    return isCollectionOperation;
  }
}