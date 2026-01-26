package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintSpecification;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Implementation for SymbolTable
 *
 * <p>starts with GLobalScope as currentScope
 *
 * @see SymbolTable interface with all operations
 * @see Scope single scope
 */
public class SymbolTableImpl implements SymbolTable {

  private final GlobalScope globalScope;
  private final ConstraintSpecification specification;
  private Scope currentScope;

  public SymbolTableImpl(ConstraintSpecification specification) {
    this.globalScope = new GlobalScope();
    this.currentScope = globalScope;
    this.specification = specification;
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
  public void define(Symbol symbol) {
    currentScope.define(symbol);
  }

  @Override
  public Symbol resolve(String name) {
    return currentScope.resolve(name);
  }

  @Override
  public Scope getCurrentScope() {
    return currentScope;
  }

  @Override
  public Scope getGlobalScope() {
    return globalScope;
  }

  @Override
  public Type lookupType(String typeName) {
    // Try to resolve as primitive type first
    switch (typeName) {
      case "Integer":
        return Type.INTEGER;
      case "Double":
        return Type.DOUBLE;
      case "String":
        return Type.STRING;
      case "Boolean":
        return Type.BOOLEAN;
      default:
        // Check if it's a qualified metamodel type (Metamodel::Class)
        if (typeName.contains("::")) {
          String[] parts = typeName.split("::");
          if (parts.length == 2) {
            String metamodel = parts[0];
            String className = parts[1];
            // TODO: Lookup from TypeRegistry or VSUM
            // For now, create a placeholder MetaclassType
            // This will be replaced with real VSUM integration
            return null; // Will be implemented with metamodel integration
          }
        }

        // Try to resolve as unqualified metamodel type from global scope
        Symbol symbol = globalScope.resolve(typeName);
        if (symbol != null && symbol.getType() != null) {
          return symbol.getType();
        }
        return null;
    }
  }
}