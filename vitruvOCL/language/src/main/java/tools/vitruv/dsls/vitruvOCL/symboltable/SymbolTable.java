package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Interface for symbol table
 * 
 * 
 * @see SymbolTableBuilder builds the Symbol Table
 * @see Scope single scope in hierarchie
 */
public interface SymbolTable {

    void enterScope(Scope newScope);

    void exitScope();

    void define(Symbol symbol);

    Symbol resolve(String name);

    Scope getCurrentScope();

    Scope getGlobalScope();

    Type lookupType(String typeName);

}