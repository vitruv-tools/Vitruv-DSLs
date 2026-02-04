package tools.vitruv.dsls.vitruvOCL.symboltable;

/**
 * Represents a scope in the symbol table hierarchy.
 *
 * <p>A {@code Scope} defines a namespace in which symbols can be declared and resolved. Scopes may
 * be nested and form a hierarchy, where symbol lookup typically proceeds from the innermost scope
 * outward to enclosing scopes.
 *
 * <p>The symbol table distinguishes between different kinds of symbols:
 *
 * <ul>
 *   <li><b>Variables</b> (e.g., local variables, iterator variables, parameters), which may be
 *       defined in any scope
 *   <li><b>Types</b> (metaclasses from metamodels), which are only defined in the global scope
 *   <li><b>Operations</b> (OCL operations), which are only defined in the global scope
 * </ul>
 *
 * <p>This interface provides type-safe methods for defining and resolving these different symbol
 * kinds.
 */
public interface Scope {

  /**
   * Defines a variable symbol in this scope.
   *
   * @param symbol the variable symbol to define
   */
  void defineVariable(VariableSymbol symbol);

  /**
   * Defines a type symbol in this scope.
   *
   * <p>Type symbols are typically only allowed in the global scope.
   *
   * @param symbol the type symbol to define
   */
  void defineType(TypeSymbol symbol);

  /**
   * Defines an operation symbol in this scope.
   *
   * <p>Operation symbols are typically only allowed in the global scope.
   *
   * @param symbol the operation symbol to define
   */
  void defineOperation(OperationSymbol symbol);

  /**
   * Resolves a variable symbol by name.
   *
   * <p>If the variable is not found in this scope, enclosing scopes may be consulted depending on
   * the implementation.
   *
   * @param name the name of the variable
   * @return the resolved variable symbol, or {@code null} if not found
   */
  VariableSymbol resolveVariable(String name);

  /**
   * Resolves a type symbol by name.
   *
   * @param name the name of the type
   * @return the resolved type symbol, or {@code null} if not found
   */
  TypeSymbol resolveType(String name);

  /**
   * Resolves an operation symbol by name.
   *
   * @param name the name of the operation
   * @return the resolved operation symbol, or {@code null} if not found
   */
  OperationSymbol resolveOperation(String name);

  /**
   * Returns the enclosing (parent) scope of this scope.
   *
   * @return the enclosing scope, or {@code null} if this is the global scope
   */
  Scope getEnclosingScope();
}
