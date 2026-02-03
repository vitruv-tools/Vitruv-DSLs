package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Phase 1 visitor that constructs the symbol table.
 *
 * <p>This visitor implements the <b>symbol table construction phase</b> of the VitruvOCL compiler
 * pipeline. It operates before type checking (Phase 2) and evaluation (Phase 3), collecting all
 * variable declarations and building scope hierarchies.
 *
 * <h2>Core Responsibilities</h2>
 *
 * <ul>
 *   <li><b>Scope management:</b> Creates and maintains scope hierarchy (context scopes, let scopes,
 *       iterator scopes)
 *   <li><b>Variable definition:</b> Defines variables in appropriate scopes (self, let variables,
 *       iterator variables)
 *   <li><b>Duplicate detection:</b> Reports errors for duplicate variable names in same scope
 *   <li><b>Type resolution:</b> Resolves declared types but does NOT validate type conformance
 * </ul>
 *
 * <h2>What This Pass Does NOT Do</h2>
 *
 * <ul>
 *   <li>Type checking or type inference
 *   <li>Expression evaluation
 *   <li>Type conformance validation
 *   <li>Operation signature checking
 * </ul>
 *
 * <h2>Scope Hierarchy Example</h2>
 *
 * <pre>{@code
 * context Person inv:                    // Creates context scope
 *   let x = 5 in                         //   Creates let scope
 *     self.friends.select(f |            //     Creates iterator scope
 *       f.age > x)
 *
 * Scope tree:
 * GlobalScope
 *   └─ ContextScope(Person)
 *        self: Person
 *        └─ LetScope
 *             x: Integer
 *             └─ IteratorScope
 *                  f: Person
 * }</pre>
 *
 * <h2>Usage in Pipeline</h2>
 *
 * <pre>{@code
 * // Pass 1: Symbol Table Construction
 * SymbolTableImpl symbolTable = new SymbolTableImpl(wrapper);
 * SymbolTableBuilder builder = new SymbolTableBuilder(symbolTable, wrapper, errors);
 * builder.visit(parseTree);
 *
 * if (errors.hasErrors()) return;
 *
 * // Pass 2: Type Checking (uses completed symbol table)
 * TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, wrapper, errors);
 * typeChecker.visit(parseTree);
 * }</pre>
 *
 * @author Max
 * @see TypeCheckVisitor Phase 2 type checking
 * @see SymbolTable The symbol table being populated
 */
public class SymbolTableBuilder extends AbstractPhaseVisitor<Void> {

  private final SymbolTable symbolTable;
  private final ScopeAnnotator scopeAnnotator;

  /**
   * Constructs a SymbolTableBuilder for Phase 1.
   *
   * @param symbolTable The symbol table to populate
   * @param wrapper The metamodel wrapper for type resolution
   * @param errors The error collector for reporting symbol errors
   * @param scopeAnnotator The scope annotator for annotating parse tree nodes with scopes
   */
  public SymbolTableBuilder(
      SymbolTable symbolTable,
      MetamodelWrapperInterface wrapper,
      ErrorCollector errors,
      ScopeAnnotator scopeAnnotator) {
    super(symbolTable, wrapper, errors);
    this.symbolTable = symbolTable;
    this.scopeAnnotator = scopeAnnotator;
  }

  @Override
  protected void handleUndefinedSymbol(String name, ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Undefined symbol in Pass 1: " + name,
        ErrorSeverity.ERROR,
        "symbol-table-builder");
  }

  // ==================== Context Declaration ====================

  @Override
  public Void visitContextDeclCS(VitruvOCLParser.ContextDeclCSContext ctx) {
    for (VitruvOCLParser.ClassifierContextCSContext classifierCtx : ctx.classifierContextCS()) {
      visit(classifierCtx);
    }
    return null;
  }

  /**
   * Creates context scope and defines 'self' variable.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Person inv:          // Creates scope, defines self: Person
   *   self.age >= 0
   *
   * context model::Employee inv: // Qualified name
   *   self.salary > 0
   * }</pre>
   */
  @Override
  public Void visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    // Resolve context type
    Type contextType;
    if (ctx.metamodel != null && ctx.className != null) {
      // Qualified: metamodel::ClassName
      String qualifiedName = ctx.metamodel.getText() + "::" + ctx.className.getText();
      contextType = symbolTable.lookupType(qualifiedName);
    } else if (ctx.contextName != null) {
      // Unqualified: ClassName
      contextType = symbolTable.lookupType(ctx.contextName.getText());
    } else {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Invalid context declaration",
          ErrorSeverity.ERROR,
          "symbol-table-builder");
      return null;
    }

    if (contextType == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unknown context type",
          ErrorSeverity.ERROR,
          "symbol-table-builder");
      return null;
    }

    // Create context scope and define 'self'
    Scope contextScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(contextScope);
    symbolTable.defineVariable(new VariableSymbol("self", contextType, contextScope, false));

    // Annotate parse tree node with scope for Pass 2
    scopeAnnotator.annotate(ctx, contextScope);

    try {
      // Visit all invariants (to collect nested let/iterator variables)
      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        visit(inv);
      }
      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Void visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    for (VitruvOCLParser.SpecificationCSContext spec : ctx.specificationCS()) {
      visit(spec);
    }
    return null;
  }

  @Override
  public Void visitSpecificationCS(VitruvOCLParser.SpecificationCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  @Override
  public Void visitExpCS(VitruvOCLParser.ExpCSContext ctx) {
    return visit(ctx.infixedExpCS());
  }

  // ==================== Let Expressions ====================

  /**
   * Creates let scope and defines let variables.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * let x = 5, y = "hello" in  // Creates scope, defines x: Integer, y: String
   *   x + y.size()
   * }</pre>
   */
  @Override
  public Void visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    LocalScope letScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(letScope);

    // Annotate parse tree node with scope for Pass 2
    scopeAnnotator.annotate(ctx, letScope);

    try {
      // Process variable declarations
      visit(ctx.variableDeclarations());

      // Visit body expressions (for nested lets/iterators)
      for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
        visit(exp);
      }

      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Void visitVariableDeclarations(VitruvOCLParser.VariableDeclarationsContext ctx) {
    for (VitruvOCLParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
      visit(varDecl);
    }
    return null;
  }

  /**
   * Defines a let variable in the current scope.
   *
   * <p>Checks for duplicates and resolves the declared or inferred type.
   */
  @Override
  public Void visitVariableDeclaration(VitruvOCLParser.VariableDeclarationContext ctx) {
    String varName = ctx.varName.getText();

    // Check for duplicate ONLY in current scope (allow shadowing of parent scopes)
    LocalScope currentScope = (LocalScope) symbolTable.getCurrentScope();
    if (currentScope.hasVariableInCurrentScope(varName)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Variable '" + varName + "' already defined in this scope",
          ErrorSeverity.ERROR,
          "symbol-table-builder");
      return null;
    }

    // Rest bleibt gleich...
    Type declaredType = Type.ANY;
    if (ctx.varType != null) {
      declaredType = resolveTypeExpression(ctx.varType);
    }

    symbolTable.defineVariable(new VariableSymbol(varName, declaredType, currentScope, false));
    visit(ctx.varInit);

    return null;
  }

  /**
   * Helper to resolve type expressions in Pass 1.
   *
   * <p>Returns a basic type or Type.ANY - detailed type checking happens in Pass 2.
   */
  private Type resolveTypeExpression(VitruvOCLParser.TypeExpCSContext ctx) {
    if (ctx.typeNameExpCS() != null) {
      return resolveTypeName(ctx.typeNameExpCS());
    }
    if (ctx.typeLiteralCS() != null) {
      return resolveTypeLiteral(ctx.typeLiteralCS());
    }
    return Type.ANY;
  }

  private Type resolveTypeName(VitruvOCLParser.TypeNameExpCSContext ctx) {
    String typeName;

    if (ctx.metamodel != null && ctx.className != null) {
      typeName = ctx.metamodel.getText() + "::" + ctx.className.getText();
    } else if (ctx.unqualified != null) {
      typeName = ctx.unqualified.getText();
    } else {
      return Type.ANY;
    }

    // Check primitives
    Type primitiveType =
        switch (typeName) {
          case "Integer" -> Type.INTEGER;
          case "String" -> Type.STRING;
          case "Boolean" -> Type.BOOLEAN;
          case "Real", "Double" -> Type.DOUBLE;
          case "OclAny" -> Type.ANY;
          default -> null;
        };

    if (primitiveType != null) {
      return primitiveType;
    }

    // Lookup metamodel type
    Type resolvedType = symbolTable.lookupType(typeName);
    return (resolvedType != null) ? resolvedType : Type.ANY;
  }

  private Type resolveTypeLiteral(VitruvOCLParser.TypeLiteralCSContext ctx) {
    if (ctx.primitiveTypeCS() != null) {
      return resolvePrimitiveType(ctx.primitiveTypeCS());
    }
    if (ctx.collectionTypeCS() != null) {
      return resolveCollectionType(ctx.collectionTypeCS());
    }
    return Type.ANY;
  }

  private Type resolvePrimitiveType(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    String typeName = ctx.getText();
    return switch (typeName) {
      case "Boolean" -> Type.BOOLEAN;
      case "Integer" -> Type.INTEGER;
      case "Real" -> Type.DOUBLE;
      case "String" -> Type.STRING;
      case "ID" -> Type.STRING;
      case "UnlimitedNatural" -> Type.INTEGER;
      case "OclAny" -> Type.ANY;
      default -> Type.ANY;
    };
  }

  private Type resolveCollectionType(VitruvOCLParser.CollectionTypeCSContext ctx) {
    String kind = ctx.collectionKind.getText();
    Type elementType = Type.ANY;

    if (ctx.typeExpCS() != null) {
      elementType = resolveTypeExpression(ctx.typeExpCS());
    }

    return switch (kind) {
      case "Set" -> Type.set(elementType);
      case "Sequence" -> Type.sequence(elementType);
      case "Bag" -> Type.bag(elementType);
      case "OrderedSet" -> Type.orderedSet(elementType);
      case "Collection" -> Type.set(elementType);
      default -> Type.set(Type.ANY);
    };
  }

  // ==================== Iterator Operations ====================

  /**
   * Creates iterator scope and defines iterator variables.
   *
   * <p><b>Examples:</b>
   *
   * <pre>{@code
   * collection.select(x | x > 5)           // Defines x: ElementType
   * collection.forAll(a, b | a != b)       // Defines a, b: ElementType
   * }</pre>
   */
  @Override
  public Void visitSelectOp(VitruvOCLParser.SelectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  @Override
  public Void visitRejectOp(VitruvOCLParser.RejectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  @Override
  public Void visitCollectOp(VitruvOCLParser.CollectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  @Override
  public Void visitForAllOp(VitruvOCLParser.ForAllOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  @Override
  public Void visitExistsOp(VitruvOCLParser.ExistsOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Generic iterator operation handler.
   *
   * <p>Creates scope and defines all iterator variables as Type.ANY (will be refined in Pass 2).
   */
  private Void visitIteratorOp(
      ParserRuleContext ctx,
      VitruvOCLParser.IteratorVarListContext iteratorVars,
      VitruvOCLParser.ExpCSContext body) {

    List<String> iterVars = new ArrayList<>();
    if (iteratorVars != null) {
      for (TerminalNode id : iteratorVars.ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Iterator requires at least one variable",
          ErrorSeverity.ERROR,
          "symbol-table-builder");
      return null;
    }

    // Create iterator scope
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    // Annotate parse tree node with scope for Pass 2
    scopeAnnotator.annotate(ctx, iterScope);

    try {
      // Define all iterator variables with placeholder type
      // (will be refined to actual element type in Pass 2)
      for (String iterVar : iterVars) {
        symbolTable.defineVariable(new VariableSymbol(iterVar, Type.ANY, iterScope, true));
      }

      // Visit body (for nested lets/iterators)
      visit(body);

      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  // ==================== Traversal Methods ====================
  // These methods just traverse the tree to find let expressions and iterator operations

  @Override
  public Void visitPrimaryWithNav(VitruvOCLParser.PrimaryWithNavContext ctx) {
    visit(ctx.base);
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      visit(nav);
    }
    return null;
  }

  @Override
  public Void visitNavigationChainCS(VitruvOCLParser.NavigationChainCSContext ctx) {
    return visit(ctx.navigationTargetCS());
  }

  @Override
  public Void visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
    return visit(ctx.operationCall());
  }

  @Override
  public Void visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  @Override
  public Void visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  @Override
  public Void visitPrefixedQualified(VitruvOCLParser.PrefixedQualifiedContext ctx) {
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      visit(nav);
    }
    return null;
  }

  // ==================== Delegation Methods ====================
  // All other operations don't affect symbol table, just traverse

  @Override
  public Void visitComparison(VitruvOCLParser.ComparisonContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitMultiplicative(VitruvOCLParser.MultiplicativeContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitAdditive(VitruvOCLParser.AdditiveContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitLogical(VitruvOCLParser.LogicalContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitImplication(VitruvOCLParser.ImplicationContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitUnaryMinus(VitruvOCLParser.UnaryMinusContext ctx) {
    return visit(ctx.operand);
  }

  @Override
  public Void visitLogicalNot(VitruvOCLParser.LogicalNotContext ctx) {
    return visit(ctx.operand);
  }

  @Override
  public Void visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  @Override
  public Void visitMessage(VitruvOCLParser.MessageContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  // Operations with arguments
  @Override
  public Void visitIncludingOp(VitruvOCLParser.IncludingOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitExcludingOp(VitruvOCLParser.ExcludingOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitIncludesOp(VitruvOCLParser.IncludesOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitExcludesOp(VitruvOCLParser.ExcludesOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitUnionOp(VitruvOCLParser.UnionOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitAppendOp(VitruvOCLParser.AppendOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitConcatOp(VitruvOCLParser.ConcatOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitSubstringOp(VitruvOCLParser.SubstringOpContext ctx) {
    visit(ctx.start);
    visit(ctx.end);
    return null;
  }

  @Override
  public Void visitIndexOfOp(VitruvOCLParser.IndexOfOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitEqualsIgnoreCaseOp(VitruvOCLParser.EqualsIgnoreCaseOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  @Override
  public Void visitOclIsKindOfOp(VitruvOCLParser.OclIsKindOfOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  @Override
  public Void visitOclIsTypeOfOp(VitruvOCLParser.OclIsTypeOfOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  @Override
  public Void visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  @Override
  public Void visitCollectionLiteralExpCS(VitruvOCLParser.CollectionLiteralExpCSContext ctx) {
    visit(ctx.collectionKind);
    if (ctx.arguments != null) {
      visit(ctx.arguments);
    }
    return null;
  }

  @Override
  public Void visitCollectionArguments(VitruvOCLParser.CollectionArgumentsContext ctx) {
    for (VitruvOCLParser.CollectionLiteralPartCSContext part : ctx.collectionLiteralPartCS()) {
      visit(part);
    }
    return null;
  }

  @Override
  public Void visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  // Leaf nodes and operations without arguments - no traversal needed
  @Override
  public Void visitSizeOp(VitruvOCLParser.SizeOpContext ctx) {
    return null;
  }

  @Override
  public Void visitFirstOp(VitruvOCLParser.FirstOpContext ctx) {
    return null;
  }

  @Override
  public Void visitLastOp(VitruvOCLParser.LastOpContext ctx) {
    return null;
  }

  @Override
  public Void visitReverseOp(VitruvOCLParser.ReverseOpContext ctx) {
    return null;
  }

  @Override
  public Void visitIsEmptyOp(VitruvOCLParser.IsEmptyOpContext ctx) {
    return null;
  }

  @Override
  public Void visitNotEmptyOp(VitruvOCLParser.NotEmptyOpContext ctx) {
    return null;
  }

  @Override
  public Void visitFlattenOp(VitruvOCLParser.FlattenOpContext ctx) {
    return null;
  }

  @Override
  public Void visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    return null;
  }

  @Override
  public Void visitMaxOp(VitruvOCLParser.MaxOpContext ctx) {
    return null;
  }

  @Override
  public Void visitMinOp(VitruvOCLParser.MinOpContext ctx) {
    return null;
  }

  @Override
  public Void visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    return null;
  }

  @Override
  public Void visitAbsOp(VitruvOCLParser.AbsOpContext ctx) {
    return null;
  }

  @Override
  public Void visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    return null;
  }

  @Override
  public Void visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    return null;
  }

  @Override
  public Void visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    return null;
  }

  @Override
  public Void visitLiftOp(VitruvOCLParser.LiftOpContext ctx) {
    return null;
  }

  @Override
  public Void visitToUpperOp(VitruvOCLParser.ToUpperOpContext ctx) {
    return null;
  }

  @Override
  public Void visitToLowerOp(VitruvOCLParser.ToLowerOpContext ctx) {
    return null;
  }

  @Override
  public Void visitAllInstancesOp(VitruvOCLParser.AllInstancesOpContext ctx) {
    return null;
  }

  @Override
  public Void visitNumberLit(VitruvOCLParser.NumberLitContext ctx) {
    return null;
  }

  @Override
  public Void visitStringLit(VitruvOCLParser.StringLitContext ctx) {
    return null;
  }

  @Override
  public Void visitBooleanLit(VitruvOCLParser.BooleanLitContext ctx) {
    return null;
  }

  @Override
  public Void visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    return null;
  }

  @Override
  public Void visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    return null;
  }

  @Override
  public Void visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
    return null;
  }

  @Override
  public Void visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    return null;
  }

  // Delegation patterns
  @Override
  public Void visitPrefixedExpr(VitruvOCLParser.PrefixedExprContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  @Override
  public Void visitLiteral(VitruvOCLParser.LiteralContext ctx) {
    return visit(ctx.literalExpCS());
  }

  @Override
  public Void visitConditional(VitruvOCLParser.ConditionalContext ctx) {
    return visit(ctx.ifExpCS());
  }

  @Override
  public Void visitLetBinding(VitruvOCLParser.LetBindingContext ctx) {
    return visit(ctx.letExpCS());
  }

  @Override
  public Void visitCollectionLiteral(VitruvOCLParser.CollectionLiteralContext ctx) {
    return visit(ctx.collectionLiteralExpCS());
  }

  @Override
  public Void visitTypeLiteral(VitruvOCLParser.TypeLiteralContext ctx) {
    return visit(ctx.typeLiteralExpCS());
  }

  @Override
  public Void visitNested(VitruvOCLParser.NestedContext ctx) {
    return visit(ctx.nestedExpCS());
  }

  @Override
  public Void visitSelf(VitruvOCLParser.SelfContext ctx) {
    return visit(ctx.selfExpCS());
  }

  @Override
  public Void visitVariable(VitruvOCLParser.VariableContext ctx) {
    return visit(ctx.variableExpCS());
  }

  @Override
  public Void visitTypeLiteralExpCS(VitruvOCLParser.TypeLiteralExpCSContext ctx) {
    return visit(ctx.typeLiteralCS());
  }

  @Override
  public Void visitCollectionOperation(VitruvOCLParser.CollectionOperationContext ctx) {
    return visit(ctx.collectionOpCS());
  }

  @Override
  public Void visitStringOperation(VitruvOCLParser.StringOperationContext ctx) {
    return visit(ctx.stringOpCS());
  }

  @Override
  public Void visitIteratorOperation(VitruvOCLParser.IteratorOperationContext ctx) {
    return visit(ctx.iteratorOpCS());
  }

  @Override
  public Void visitTypeOperation(VitruvOCLParser.TypeOperationContext ctx) {
    return visit(ctx.typeOpCS());
  }

  @Override
  public Void visitTypeExpCS(VitruvOCLParser.TypeExpCSContext ctx) {
    return null; // Type expressions don't affect symbol table
  }

  @Override
  public Void visitTypeLiteralCS(VitruvOCLParser.TypeLiteralCSContext ctx) {
    return null;
  }

  @Override
  public Void visitCollectionTypeCS(VitruvOCLParser.CollectionTypeCSContext ctx) {
    return null;
  }

  @Override
  public Void visitCollectionTypeIdentifier(VitruvOCLParser.CollectionTypeIdentifierContext ctx) {
    return null;
  }

  @Override
  public Void visitPrimitiveTypeCS(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    return null;
  }

  @Override
  public Void visitTypeNameExpCS(VitruvOCLParser.TypeNameExpCSContext ctx) {
    return null;
  }

  @Override
  public Void visitOnespace(VitruvOCLParser.OnespaceContext ctx) {
    return null;
  }
}