package tools.vitruv.dsls.vitruvOCL.symboltable;

import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
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
  private final MetamodelWrapperInterface wrapper;
  private Scope currentScope;

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
          System.out.println("resolving metaclass: " + typeName);
          String[] parts = typeName.split("::");
          if (parts.length == 2) {
            String metamodel = parts[0];
            String className = parts[1];
            EClass eClass = wrapper.resolveEClass(metamodel, className);
            if (eClass != null) {
              return Type.metaclassType(eClass);
            }
            System.err.println("eClass == " + eClass + ": " + metamodel + " with " + className);
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