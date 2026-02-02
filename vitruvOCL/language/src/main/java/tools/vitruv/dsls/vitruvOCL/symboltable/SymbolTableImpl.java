package tools.vitruv.dsls.vitruvOCL.symboltable;

import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Implementation for SymbolTable
 *
 * <p>starts with GlobalScope as currentScope
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
  public void defineVariable(VariableSymbol symbol) {
    currentScope.defineVariable(symbol);
  }

  @Override
  public void defineType(TypeSymbol symbol) {
    // Types werden nur im GlobalScope definiert
    globalScope.defineType(symbol);
  }

  @Override
  public void defineOperation(OperationSymbol symbol) {
    // Operations werden nur im GlobalScope definiert
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
        TypeSymbol symbol = globalScope.resolveType(typeName);
        if (symbol != null && symbol.getType() != null) {
          return symbol.getType();
        }
        return null;
    }
  }
}