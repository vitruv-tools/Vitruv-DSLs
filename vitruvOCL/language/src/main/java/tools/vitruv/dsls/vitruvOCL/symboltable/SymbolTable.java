package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Interface for symbol table
 *
 * <p>Provides type-safe access to different symbol namespaces: - Variables (local variables,
 * iterator variables, parameters) - Types (metaclasses from metamodels) - Operations (OCL
 * operations)
 *
 * @see SymbolTableBuilder builds the Symbol Table
 * @see Scope single scope in hierarchy
 */
public interface SymbolTable {

  // Scope management
  void enterScope(Scope newScope);

  void exitScope();

  Scope getCurrentScope();

  Scope getGlobalScope();

  // Type-safe define methods
  void defineVariable(VariableSymbol symbol);

  void defineType(TypeSymbol symbol);

  void defineOperation(OperationSymbol symbol);

  // Type-safe resolve methods
  VariableSymbol resolveVariable(String name);

  TypeSymbol resolveType(String name);

  OperationSymbol resolveOperation(String name);

  // Type lookup for metamodel types
  Type lookupType(String typeName);
}