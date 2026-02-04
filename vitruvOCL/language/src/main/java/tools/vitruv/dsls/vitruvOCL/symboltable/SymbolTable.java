package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Interface for the symbol table of the OCL compiler.
 *
 * <p>The {@code SymbolTable} manages a hierarchy of {@link Scope}s and provides type-safe access to
 * different symbol namespaces, including:
 *
 * <ul>
 *   <li><b>Variables</b> (local variables, iterator variables, parameters)
 *   <li><b>Types</b> (metaclasses from the underlying metamodels)
 *   <li><b>Operations</b> (OCL operations)
 * </ul>
 *
 * <p>The symbol table supports entering and leaving scopes during tree traversal and delegates
 * symbol definition and resolution to the currently active scope.
 *
 * @see SymbolTableBuilder
 * @see Scope
 */
public interface SymbolTable {

  // ---------------------------------------------------------------------------
  // Scope management
  // ---------------------------------------------------------------------------

  /**
   * Enters a new scope and makes it the current scope.
   *
   * <p>This method is typically called when descending into a new syntactic construct that
   * introduces a scope (e.g., {@code let} expressions, iterator expressions, or operation bodies).
   *
   * @param newScope the scope to enter
   */
  void enterScope(Scope newScope);

  /**
   * Exits the current scope and restores the enclosing scope as the current one.
   *
   * <p>This method is typically called when leaving a syntactic construct that introduced a scope.
   */
  void exitScope();

  /**
   * Returns the currently active scope.
   *
   * @return the current scope
   */
  Scope getCurrentScope();

  /**
   * Returns the global (outermost) scope.
   *
   * <p>The global scope contains globally defined symbols such as types and operations.
   *
   * @return the global scope
   */
  Scope getGlobalScope();

  // ---------------------------------------------------------------------------
  // Type-safe symbol definition
  // ---------------------------------------------------------------------------

  /**
   * Defines a variable symbol in the current scope.
   *
   * @param symbol the variable symbol to define
   */
  void defineVariable(VariableSymbol symbol);

  /**
   * Defines a type symbol in the global scope.
   *
   * @param symbol the type symbol to define
   */
  void defineType(TypeSymbol symbol);

  /**
   * Defines an operation symbol in the global scope.
   *
   * @param symbol the operation symbol to define
   */
  void defineOperation(OperationSymbol symbol);

  // ---------------------------------------------------------------------------
  // Type-safe symbol resolution
  // ---------------------------------------------------------------------------

  /**
   * Resolves a variable symbol by name, starting from the current scope.
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

  // ---------------------------------------------------------------------------
  // Type lookup
  // ---------------------------------------------------------------------------

  /**
   * Looks up a semantic {@link Type} by its name.
   *
   * <p>This method is typically used during type checking to map metamodel type names to their
   * corresponding {@link Type} representations.
   *
   * @param typeName the name of the type
   * @return the corresponding {@link Type}, or {@code null} if not found
   */
  Type lookupType(String typeName);
}
