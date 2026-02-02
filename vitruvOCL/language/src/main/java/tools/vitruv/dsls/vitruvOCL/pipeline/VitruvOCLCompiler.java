package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.io.IOException;
import java.nio.file.Path;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

public class VitruvOCLCompiler {

  private final MetamodelWrapperInterface wrapper;
  private final Path oclFile;
  private final ErrorCollector errors = new ErrorCollector();

  public VitruvOCLCompiler(MetamodelWrapperInterface wrapper, Path oclFile) {
    this.wrapper = wrapper;
    this.oclFile = oclFile;
  }

  public Value compile(Path sourcePath) {
    String source = sourcePath.toString();
    CharStream input = CharStreams.fromString(source);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    VitruvOCLParser.ContextDeclCSContext tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) return null;

    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors);
    typeChecker.visit(tree);

    if (errors.hasErrors()) return null;

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
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

    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      return new ValidationResult(errors.getErrors(), java.util.List.of());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    evaluator.visit(tree);

    return new ValidationResult(errors.getErrors(), java.util.List.of());
  }

  public Value compile(String oclSource) {
    CharStream input = CharStreams.fromString(oclSource);
    VitruvOCLLexer lexer = new VitruvOCLLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.contextDeclCS();

    if (parser.getNumberOfSyntaxErrors() > 0) {
      return null;
    }

    SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      System.out.println("Errors After Type Checking: " + errors.getErrorCount());
      errors
          .getErrors()
          .forEach(
              err -> System.err.println("  " + err.getMessage() + " at line " + err.getLine()));
      return null;
    }
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, wrapper, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);
    if (errors.hasErrors()) {
      System.out.println("Errors After Type Checking: " + errors.getErrorCount());
      errors
          .getErrors()
          .forEach(
              err -> System.err.println("  " + err.getMessage() + " at line " + err.getLine()));
      return null;
    }
    return result;
  }

  public boolean hasErrors() {
    return errors.hasErrors();
  }

  public ErrorCollector getErrors() {
    return errors;
  }
}