package tools.vitruv.dsls.vitruvOCL.symboltable;

import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

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
   * Looks up a semantic {@link Type} by its name.
   *
   * <p>The lookup proceeds in the following order:
   *
   * <ol>
   *   <li>Primitive OCL types ({@code Integer}, {@code Double}, {@code String}, {@code Boolean})
   *   <li>Qualified metamodel types of the form {@code Metamodel::Class}
   *   <li>Unqualified metamodel types defined in the global scope
   * </ol>
   *
   * @param typeName the name of the type
   * @return the resolved {@link Type}, or {@code null} if the type cannot be resolved
   */
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
            EClass eClass = wrapper.resolveEClass(metamodel, className);
            if (eClass != null) {
              return Type.metaclassType(eClass);
            }
          }
        }

        // Try to resolve as unqualified metamodel type from the global scope
        TypeSymbol symbol = globalScope.resolveType(typeName);
        if (symbol != null && symbol.getType() != null) {
          return symbol.getType();
        }
        return null;
    }
  }
}
