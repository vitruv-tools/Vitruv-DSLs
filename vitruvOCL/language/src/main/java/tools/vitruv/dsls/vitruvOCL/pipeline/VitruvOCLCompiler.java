package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import org.antlr.v4.runtime.*;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

public class VitruvOCLCompiler {

  private final ConstraintSpecification specification;
  private final Path oclFile;
  private final ErrorCollector errors = new ErrorCollector();

  public VitruvOCLCompiler(ConstraintSpecification specification, Path oclFile) {
    this.specification = specification;
    this.oclFile = oclFile;
  }

  public Value compile(String source) {
    CharStream input = CharStreams.fromString(source);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) return null;

    SymbolTableImpl symbolTable = new SymbolTableImpl(specification);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, specification, errors);
    typeChecker.visit(tree);

    if (errors.hasErrors()) return null;

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, specification, errors, typeChecker.getNodeTypes());
    return evaluator.visit(tree);
  }

  public ValidationResult compile() throws IOException {
    CharStream input = CharStreams.fromPath(oclFile);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    SymbolTableImpl symbolTable = new SymbolTableImpl(specification);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, specification, errors);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, specification, errors, typeChecker.getNodeTypes());
    evaluator.visit(tree);

    return new ValidationResult(errors.getErrors(), java.util.List.of());
  }

  public boolean hasErrors() {
    return errors.hasErrors();
  }

  public ErrorCollector getErrors() {
    return errors;
  }
}