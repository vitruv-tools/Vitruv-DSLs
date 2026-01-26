package tools.vitruv.dsls.vitruvOCL.common;

import org.antlr.v4.runtime.ParserRuleContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLBaseVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintSpecification;
import tools.vitruv.dsls.vitruvOCL.symboltable.Symbol;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * abstract base class for the visitor implementations needed in the different phases
 *
 * <p>- access to symbol-table - error collection - access to VSUM-Wrapper
 *
 * @param <T> Type für Type Checker, Value für Evaluator
 * @see TypeCheckVisitor for concrete implementation of pass 2(typechecking)
 * @see EvaluationVisitor for concrete implementation of pass 3(runtime evaluation)
 */
public abstract class AbstractPhaseVisitor<T> extends VitruvOCLBaseVisitor<T> {

  protected final SymbolTable symbolTable;
  protected final ErrorCollector errors;
  protected final ConstraintSpecification specification;

  protected AbstractPhaseVisitor(
      SymbolTable symbolTable, ConstraintSpecification specification, ErrorCollector errors) {
    this.symbolTable = symbolTable;
    this.specification = specification;
    this.errors = errors;
  }

  protected Symbol resolveSymbol(String name, ParserRuleContext ctx) {
    Symbol symbol = symbolTable.resolve(name);
    if (symbol == null) {
      handleUndefinedSymbol(name, ctx);
    }
    return symbol;
  }

  protected abstract void handleUndefinedSymbol(String name, ParserRuleContext ctx);

  public boolean hasErrors() {
    return errors.hasErrors();
  }

  public ErrorCollector getErrorCollector() {
    return errors;
  }
}