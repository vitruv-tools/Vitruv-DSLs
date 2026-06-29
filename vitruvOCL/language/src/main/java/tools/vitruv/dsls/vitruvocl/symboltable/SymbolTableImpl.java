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
package tools.vitruv.dsls.vitruvocl.symboltable;

import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Default implementation of the {@link SymbolTable} interface.
 *
 * <p>This implementation maintains a hierarchy of {@link Scope}s and starts with a {@link
 * GlobalScope} as the initial and current scope.
 *
 * <p>Variables are defined in the currently active scope, whereas types and operations are always
 * defined in the global scope.
 *
 * <p>The symbol table also provides a unified lookup mechanism for semantic {@link Type}s,
 * including primitive OCL types and metamodel-based types.
 *
 * @see SymbolTable
 * @see Scope
 * @see GlobalScope
 */
public class SymbolTableImpl implements SymbolTable {

  /** The global (outermost) scope of the symbol table. */
  private final GlobalScope globalScope;

  /**
   * Wrapper providing access to the underlying EMF metamodels.
   *
   * <p>Used to resolve qualified metamodel types (e.g., {@code Metamodel::Class}).
   */
  private final MetamodelWrapperInterface wrapper;

  /** The currently active scope. */
  private Scope currentScope;

  /**
   * Creates a new symbol table instance.
   *
   * <p>The symbol table is initialized with a {@link GlobalScope} as the current scope.
   *
   * @param wrapper the metamodel wrapper used to resolve metamodel types
   */
  public SymbolTableImpl(MetamodelWrapperInterface wrapper) {
    this.globalScope = new GlobalScope();
    this.currentScope = globalScope;
    this.wrapper = wrapper;
  }

  @Override
  public void enterScope(Scope newScope) {
    currentScope = newScope;
  }

  @Override
  public void exitScope() {
    if (currentScope.getEnclosingScope() != null) {
      currentScope = currentScope.getEnclosingScope();
    }
  }

  @Override
  public void defineVariable(VariableSymbol symbol) {
    currentScope.defineVariable(symbol);
  }

  @Override
  public void defineType(TypeSymbol symbol) {
    // Types are only defined in the global scope
    globalScope.defineType(symbol);
  }

  @Override
  public void defineOperation(OperationSymbol symbol) {
    // Operations are only defined in the global scope
    globalScope.defineOperation(symbol);
  }

  @Override
  public VariableSymbol resolveVariable(String name) {
    return currentScope.resolveVariable(name);
  }

  @Override
  public TypeSymbol resolveType(String name) {
    return currentScope.resolveType(name);
  }

  @Override
  public OperationSymbol resolveOperation(String name) {
    return currentScope.resolveOperation(name);
  }

  @Override
  public Scope getCurrentScope() {
    return currentScope;
  }

  @Override
  public Scope getGlobalScope() {
    return globalScope;
  }

  /**
   * Looks up a semantic {@link Type} by name.
   *
   * <p>The lookup proceeds in the following order:
   *
   * <ol>
   *   <li>Primitive OCL types ({@code Integer}, {@code Double}, {@code Float}, {@code String},
   *       {@code Boolean})
   *   <li>Qualified metamodel types of the form {@code Metamodel::Class}
   *   <li>Unqualified metamodel types registered in the global scope
   *   <li>Short-name fallback: search all loaded metamodels for a class whose simple name matches
   *       (handles unqualified type annotations like {@code Coordinate} in let-expressions)
   * </ol>
   *
   * @param typeName the name of the type (qualified or unqualified)
   * @return the resolved {@link Type}, or {@code null} if not found
   */
  @Override
  public Type lookupType(String typeName) {
    // 1. Primitive types
    switch (typeName) {
      case "Integer":
        return Type.INTEGER;
      case "Double", "Real":
        return Type.DOUBLE;
      case "Float":
        return Type.FLOAT;
      case "String":
        return Type.STRING;
      case "Boolean":
        return Type.BOOLEAN;
      case "OclAny":
        return Type.ANY;
      default:
        break;
    }

    // 2. Qualified metamodel type (Metamodel::Class)
    if (typeName.contains("::")) {
      String[] parts = typeName.split("::");
      if (parts.length == 2) {
        EClass eClass = wrapper.resolveEClass(parts[0], parts[1]);
        if (eClass != null) {
          return Type.metaclassType(eClass);
        }
      }
    }

    // 3. Unqualified type registered in the global scope (e.g., via SymbolTableBuilder)
    TypeSymbol symbol = globalScope.resolveType(typeName);
    if (symbol != null && symbol.getType() != null) {
      return symbol.getType();
    }

    // 4. Short-name fallback: search all loaded metamodels
    //    Handles unqualified annotations like "Coordinate" in let-declarations
    EClass eClass = wrapper.resolveEClassByShortName(typeName);
    if (eClass != null) {
      return Type.metaclassType(eClass);
    }

    return null;
  }
}

