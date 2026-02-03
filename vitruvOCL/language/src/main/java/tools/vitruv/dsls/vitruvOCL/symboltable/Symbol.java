package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Parent Class for all symbols example symbols - VariableSymbol: local variables, iterator
 * variables - TypeSymbol: metaclasses - OperationSymbol: ocl operations
 *
 * @see VariableSymbol for concrete Implementation
 * @see TypeSymbol for concrete Implementation
 * @see OperationSymbol for concrete Implementation
 */
public abstract class Symbol {

  protected final String name;
  protected Type type; // NOT final - can be refined in Pass 2
  protected final Scope definingScope;

  protected Symbol(String name, Type type, Scope definingScope) {
    this.name = name;
    this.type = type;
    this.definingScope = definingScope;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  /**
   * Set or refine the type of this symbol.
   *
   * <p>Used in Pass 2 (Type Checking) to refine types that were initially set to Type.ANY in Pass 1
   * (Symbol Table Construction).
   *
   * <p>Examples: - Iterator variables: refined from Type.ANY to actual element type - Let variables
   * without explicit type: refined to inferred type
   *
   * @param type the refined type
   */
  public void setType(Type type) {
    this.type = type;
  }

  public Scope getDefiningScope() {
    return definingScope;
  }
}