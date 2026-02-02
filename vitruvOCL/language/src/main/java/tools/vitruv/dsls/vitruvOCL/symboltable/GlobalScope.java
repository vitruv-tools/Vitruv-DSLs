package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * Global scope - contains built-in VitruvOCL types and standard library operations
 *
 * <p>Types and Operations are only defined in GlobalScope. Variables can be defined here for global
 * variables (if needed).
 *
 * @see Scope interface for Scope-Operations
 */
public class GlobalScope implements Scope {

  private final Map<String, VariableSymbol> variables = new HashMap<>();
  private final Map<String, TypeSymbol> types = new HashMap<>();
  private final Map<String, OperationSymbol> operations = new HashMap<>();

  public GlobalScope() {
    // TODO: initializeBuiltInTypes();
    // TODO: initializeStandardLibrary();
  }

  @Override
  public void defineVariable(VariableSymbol symbol) {
    variables.put(symbol.getName(), symbol);
  }

  @Override
  public void defineType(TypeSymbol symbol) {
    types.put(symbol.getName(), symbol);
  }

  @Override
  public void defineOperation(OperationSymbol symbol) {
    operations.put(symbol.getName(), symbol);
  }

  @Override
  public VariableSymbol resolveVariable(String name) {
    return variables.get(name);
  }

  @Override
  public TypeSymbol resolveType(String name) {
    return types.get(name);
  }

  @Override
  public OperationSymbol resolveOperation(String name) {
    return operations.get(name);
  }

  @Override
  public Scope getEnclosingScope() {
    return null;
  }
}