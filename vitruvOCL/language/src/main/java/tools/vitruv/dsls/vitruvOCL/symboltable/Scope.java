package tools.vitruv.dsls.vitruvOCL.symboltable;

/**
 * Interface for scopes in the symbol table hierarchy
 *
 * <p>Provides type-safe access to different symbol namespaces: - Variables (local variables,
 * iterator variables, parameters) - can be defined in any scope - Types (metaclasses from
 * metamodels) - only defined in GlobalScope - Operations (OCL operations) - only defined in
 * GlobalScope
 */
public interface Scope {

  // Type-safe define methods
  void defineVariable(VariableSymbol symbol);

  void defineType(TypeSymbol symbol);

  void defineOperation(OperationSymbol symbol);

  // Type-safe resolve methods
  VariableSymbol resolveVariable(String name);

  TypeSymbol resolveType(String name);

  OperationSymbol resolveOperation(String name);

  // Scope hierarchy
  Scope getEnclosingScope();
}