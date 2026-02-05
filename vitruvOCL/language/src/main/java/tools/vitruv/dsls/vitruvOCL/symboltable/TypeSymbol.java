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
 * Symbol representing a type in the symbol table.
 *
 * <p>{@code TypeSymbol}s are used to model all kinds of types that can occur in OCL expressions,
 * including:
 *
 * <ul>
 *   <li>Metamodel types (metaclasses)
 *   <li>Primitive OCL types
 *   <li>Collection types
 * </ul>
 *
 * <p>Type symbols are typically defined in the global scope and can be referenced by their simple
 * or qualified names.
 *
 * @see Symbol
 */
public class TypeSymbol extends Symbol {

  /**
   * The fully qualified name of the type.
   *
   * <p>This is typically used for metamodel types and follows the form {@code Metamodel::Class}.
   */
  private final String qualifiedName;

  /**
   * Creates a new type symbol.
   *
   * @param name the simple (unqualified) name of the type
   * @param type the semantic type representation
   * @param definingScope the scope in which the type is defined
   * @param qualifiedName the fully qualified name of the type
   */
  public TypeSymbol(String name, Type type, Scope definingScope, String qualifiedName) {
    super(name, type, definingScope);
    this.qualifiedName = qualifiedName;
  }

  /**
   * Returns the fully qualified name of this type.
   *
   * @return the qualified type name
   */
  public String getQualifiedName() {
    return qualifiedName;
  }
}
