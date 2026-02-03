package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * Local scope for nested declarations (let expressions, iterator variables, etc.)
 *
 * <p>Only variables can be defined in LocalScope. Type and Operation lookups are delegated to
 * parent scopes.
 *
 * @see Scope interface for Scope-Operations
 */
public class LocalScope implements Scope {

  private final Scope parent;
  private final Map<String, VariableSymbol> variables = new HashMap<>();

  public LocalScope(Scope parent) {
    this.parent = parent;
  }

  @Override
  public void defineVariable(VariableSymbol symbol) {
    variables.put(symbol.getName(), symbol);
  }

  @Override
  public void defineType(TypeSymbol symbol) {
    // Types can only be defined in GlobalScope
    throw new UnsupportedOperationException(
        "Cannot define types in local scope. Types must be defined in GlobalScope.");
  }

  @Override
  public void defineOperation(OperationSymbol symbol) {
    // Operations can only be defined in GlobalScope
    throw new UnsupportedOperationException(
        "Cannot define operations in local scope. Operations must be defined in GlobalScope.");
  }

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

  @Override
  public TypeSymbol resolveType(String name) {
    // Types are only in GlobalScope, delegate to parent
    return parent != null ? parent.resolveType(name) : null;
  }

  @Override
  public OperationSymbol resolveOperation(String name) {
    // Operations are only in GlobalScope, delegate to parent
    return parent != null ? parent.resolveOperation(name) : null;
  }

  @Override
  public Scope getEnclosingScope() {
    return parent;
  }

  /**
   * Check if a variable is defined ONLY in this scope (not in parent scopes).
   *
   * <p>Used for duplicate detection while allowing shadowing.
   *
   * @param name Variable name to check
   * @return true if variable exists in this scope only
   */
  public boolean hasVariableInCurrentScope(String name) {
    return variables.containsKey(name);
  }
}