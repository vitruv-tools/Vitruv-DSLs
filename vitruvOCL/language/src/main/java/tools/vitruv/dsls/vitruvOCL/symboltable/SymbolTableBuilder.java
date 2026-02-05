/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
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
 * Phase 1 visitor that constructs the symbol table by collecting variable declarations and building
 * scope hierarchies.
 *
 * <p>This is the first pass in the VitruvOCL compiler's 3-pass architecture, executed before type
 * checking (Pass 2) and evaluation (Pass 3). It walks the ANTLR parse tree to populate the symbol
 * table with all variable bindings and their declared types.
 *
 * <h2>Core Responsibilities</h2>
 *
 * <ul>
 *   <li><b>Scope management:</b> Creates nested scope hierarchy (global, context, let, iterator)
 *   <li><b>Variable definition:</b> Registers variables in appropriate scopes (self, let variables,
 *       iterator variables)
 *   <li><b>Duplicate detection:</b> Reports errors when variables are redeclared in same scope
 *       (shadowing parent scopes is allowed)
 *   <li><b>Type resolution:</b> Resolves declared types from grammar nodes (primitives,
 *       collections, metamodel types)
 *   <li><b>Parse tree annotation:</b> Attaches scope information to grammar nodes for Pass 2 lookup
 * </ul>
 *
 * <h2>What This Pass Does NOT Do</h2>
 *
 * <ul>
 *   <li><b>Type inference:</b> Iterator variables get placeholder Type.ANY (refined in Pass 2)
 *   <li><b>Type checking:</b> No conformance validation or operation signature checking
 *   <li><b>Expression evaluation:</b> Does not compute values or execute operations
 *   <li><b>Cross-reference resolution:</b> Does not validate that referenced variables exist (Pass
 *       2 responsibility)
 * </ul>
 *
 * <h2>Scope Hierarchy Example</h2>
 *
 * <pre>{@code
 * context Person inv:                    // Creates context scope
 *   let x = 5 in                         //   Creates let scope (child of context)
 *     self.friends.select(f |            //     Creates iterator scope (child of let)
 *       f.age > x)
 *
 * Resulting scope tree:
 * GlobalScope
 *   └─ ContextScope(Person)              [self: Person]
 *        └─ LetScope                     [x: Integer]
 *             └─ IteratorScope           [f: Person]
 * }</pre>
 *
 * <h2>Variable Shadowing Rules</h2>
 *
 * Inner scopes can shadow variables from outer scopes, but redeclaration within the same scope is
 * an error:
 *
 * <pre>{@code
 * let x = 5 in              // Defines x in let scope
 *   let x = "hello" in      // ✓ ALLOWED: shadows outer x
 *     x                     // Resolves to inner x: String
 *
 * let x = 5, x = 10 in      // ✗ ERROR: duplicate in same scope
 *   x
 * }</pre>
 *
 * <h2>Type Resolution Strategy</h2>
 *
 * <p>Pass 1 performs basic type resolution for declared types but defers full type inference:
 *
 * <ul>
 *   <li><b>Primitive types:</b> Resolved immediately (Integer, String, Boolean, Real)
 *   <li><b>Collection types:</b> Element type resolved recursively (Set{Integer}, Sequence[T])
 *   <li><b>Metamodel types:</b> Looked up via TypeRegistry (spacemissionmodel::Spacecraft)
 *   <li><b>Iterator variables:</b> Assigned Type.ANY placeholder (refined to element type in Pass
 *       2)
 *   <li><b>Unresolvable types:</b> Default to Type.ANY to allow compilation to continue
 * </ul>
 *
 * <h2>Usage in Compiler Pipeline</h2>
 *
 * <pre>{@code
 * // Initialize components
 * ErrorCollector errors = new ErrorCollector();
 * SymbolTable symbolTable = new SymbolTableImpl(metamodelWrapper);
 * ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
 *
 * // Pass 1: Symbol Table Construction
 * SymbolTableBuilder builder = new SymbolTableBuilder(
 *     symbolTable, metamodelWrapper, errors, scopeAnnotator);
 * builder.visit(parseTree);
 *
 * if (errors.hasErrors()) {
 *     // Report errors and abort compilation
 *     return;
 * }
 *
 * // Pass 2: Type Checking (uses completed symbol table)
 * TypeCheckVisitor typeChecker = new TypeCheckVisitor(
 *     symbolTable, metamodelWrapper, errors, scopeAnnotator);
 * typeChecker.visit(parseTree);
 * }</pre>
 *
 * <h2>Parse Tree Annotation</h2>
 *
 * This pass annotates grammar nodes with their associated scopes using {@link ScopeAnnotator}. Pass
 * 2 retrieves these annotations to determine which scope to use for variable lookups.
 *
 * <h2>Error Handling</h2>
 *
 * Errors are collected but do not halt traversal. This allows detecting multiple errors in a single
 * pass. Common errors:
 *
 * <ul>
 *   <li>Duplicate variable in same scope
 *   <li>Unknown context type in context declaration
 *   <li>Invalid context declaration syntax
 *   <li>Iterator without variables
 * </ul>
 *
 * @author Max
 * @see SymbolTable The symbol table being populated
 * @see TypeCheckVisitor Pass 2 type checking visitor
 * @see ScopeAnnotator Parse tree scope annotation utility
 * @see AbstractPhaseVisitor Base class with common error handling
 */
public class SymbolTableBuilder extends AbstractPhaseVisitor<Void> {

  /** The symbol table being populated during this pass. */
  private final SymbolTable symbolTable;

  /** Utility for annotating parse tree nodes with scope references for Pass 2 lookup. */
  private final ScopeAnnotator scopeAnnotator;

  /**
   * Constructs a SymbolTableBuilder for Phase 1 symbol table construction.
   *
   * @param symbolTable The symbol table to populate with variable definitions
   * @param wrapper The metamodel wrapper for resolving metamodel types
   * @param errors The error collector for reporting symbol-related errors
   * @param scopeAnnotator The scope annotator for attaching scope info to parse tree nodes
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

  /**
   * Handles undefined symbol errors during symbol table construction.
   *
   * <p>Note: This should rarely occur in Pass 1 since we're defining symbols, not looking them up.
   * Most undefined symbol errors occur in Pass 2 during type checking.
   *
   * @param name The undefined symbol name
   * @param ctx The parse tree context where the error occurred
   */
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

  /**
   * Visits the top-level context declaration node.
   *
   * <p>Delegates to each classifier context (usually just one per file).
   *
   * @param ctx The context declaration parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitContextDeclCS(VitruvOCLParser.ContextDeclCSContext ctx) {
    for (VitruvOCLParser.ClassifierContextCSContext classifierCtx : ctx.classifierContextCS()) {
      visit(classifierCtx);
    }
    return null;
  }

  /**
   * Creates a context scope and defines the 'self' variable.
   *
   * <p>This handles both qualified and unqualified context declarations:
   *
   * <pre>{@code
   * context Person inv:                    // Unqualified
   *   self.age >= 0
   *
   * context model::Employee inv:           // Qualified with metamodel name
   *   self.salary > 0
   * }</pre>
   *
   * <p>The context type must be resolvable via the TypeRegistry (either a primitive or a metamodel
   * type). If resolution fails, an error is reported and processing continues.
   *
   * <p>The created context scope is annotated on the parse tree node for Pass 2 retrieval.
   *
   * @param ctx The classifier context parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    // Resolve context type (qualified or unqualified name)
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

    // Create context scope as child of global scope
    Scope contextScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(contextScope);
    symbolTable.defineVariable(new VariableSymbol("self", contextType, contextScope, false));

    // Annotate parse tree node with scope for Pass 2 variable lookup
    scopeAnnotator.annotate(ctx, contextScope);

    try {
      // Visit all invariants to collect nested let/iterator variables
      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        visit(inv);
      }
      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Visits an invariant declaration node.
   *
   * <p>Delegates to specification nodes to process constraint expressions.
   *
   * @param ctx The invariant parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    for (VitruvOCLParser.SpecificationCSContext spec : ctx.specificationCS()) {
      visit(spec);
    }
    return null;
  }

  /**
   * Visits a specification node containing the constraint expression.
   *
   * @param ctx The specification parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSpecificationCS(VitruvOCLParser.SpecificationCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  /**
   * Visits an expression node.
   *
   * <p>Delegates to the infixed expression (handles operator precedence).
   *
   * @param ctx The expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitExpCS(VitruvOCLParser.ExpCSContext ctx) {
    return visit(ctx.infixedExpCS());
  }

  // ==================== Let Expressions ====================

  /**
   * Creates a let scope and defines let-bound variables.
   *
   * <p>Let expressions introduce new variables scoped to their body:
   *
   * <pre>{@code
   * let x = 5, y = "hello" in     // Creates scope, defines x: Integer, y: String
   *   x + y.size()                // Body uses let variables
   * }</pre>
   *
   * <p>Let variables can shadow variables from outer scopes but cannot duplicate variables within
   * the same let declaration.
   *
   * <p>Type resolution for let variables:
   *
   * <ul>
   *   <li>Explicit type annotation: {@code let x: Integer = 5} uses Integer
   *   <li>No annotation: {@code let x = 5} uses Type.ANY in Pass 1 (inferred in Pass 2)
   * </ul>
   *
   * @param ctx The let expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    // Create let scope as child of current scope
    LocalScope letScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(letScope);

    // Annotate parse tree node with scope for Pass 2 variable lookup
    scopeAnnotator.annotate(ctx, letScope);

    try {
      // Process variable declarations (defines variables in let scope)
      visit(ctx.variableDeclarations());

      // Visit body expressions to collect nested lets/iterators
      for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
        visit(exp);
      }

      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Visits a list of variable declarations in a let expression.
   *
   * @param ctx The variable declarations parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitVariableDeclarations(VitruvOCLParser.VariableDeclarationsContext ctx) {
    for (VitruvOCLParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
      visit(varDecl);
    }
    return null;
  }

  /**
   * Defines a let-bound variable in the current scope.
   *
   * <p>Handles both explicitly typed and untyped variable declarations:
   *
   * <pre>{@code
   * let x: Integer = 5         // Explicit type: uses Integer
   * let y = "hello"            // No type: uses Type.ANY (inferred in Pass 2)
   * }</pre>
   *
   * <p>Performs duplicate checking within the current scope only - shadowing outer scope variables
   * is allowed:
   *
   * <pre>{@code
   * let x = 5 in
   *   let x = "hello" in      // ✓ ALLOWED: shadows outer x
   *     x
   *
   * let x = 5, x = 10 in      // ✗ ERROR: duplicate in same scope
   *   x
   * }</pre>
   *
   * @param ctx The variable declaration parse tree node
   * @return null (void visitor)
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

    // Resolve declared type (or use Type.ANY if no type annotation)
    Type declaredType = Type.ANY;
    if (ctx.varType != null) {
      declaredType = resolveTypeExpression(ctx.varType);
    }

    // Define variable in current scope
    symbolTable.defineVariable(new VariableSymbol(varName, declaredType, currentScope, false));

    // Visit initializer to collect nested lets/iterators
    visit(ctx.varInit);

    return null;
  }

  /**
   * Resolves a type expression from the grammar to a Type object.
   *
   * <p>Handles both named types and type literals:
   *
   * <ul>
   *   <li>Type names: {@code Integer}, {@code model::Employee}
   *   <li>Type literals: {@code Set{Integer}}, {@code Sequence[String]}
   * </ul>
   *
   * <p>Returns Type.ANY if resolution fails to allow compilation to continue.
   *
   * @param ctx The type expression parse tree node
   * @return The resolved Type, or Type.ANY if unresolvable
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

  /**
   * Resolves a type name (qualified or unqualified).
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code Integer} → Type.INTEGER
   *   <li>{@code String} → Type.STRING
   *   <li>{@code model::Employee} → looked up in TypeRegistry
   * </ul>
   *
   * @param ctx The type name parse tree node
   * @return The resolved Type, or Type.ANY if unresolvable
   */
  private Type resolveTypeName(VitruvOCLParser.TypeNameExpCSContext ctx) {
    String typeName;

    if (ctx.metamodel != null && ctx.className != null) {
      // Qualified: metamodel::ClassName
      typeName = ctx.metamodel.getText() + "::" + ctx.className.getText();
    } else if (ctx.unqualified != null) {
      // Unqualified: ClassName
      typeName = ctx.unqualified.getText();
    } else {
      return Type.ANY;
    }

    // Check primitive types first
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

    // Lookup metamodel type in TypeRegistry
    Type resolvedType = symbolTable.lookupType(typeName);
    return (resolvedType != null) ? resolvedType : Type.ANY;
  }

  /**
   * Resolves a type literal (primitive or collection type).
   *
   * @param ctx The type literal parse tree node
   * @return The resolved Type, or Type.ANY if unresolvable
   */
  private Type resolveTypeLiteral(VitruvOCLParser.TypeLiteralCSContext ctx) {
    if (ctx.primitiveTypeCS() != null) {
      return resolvePrimitiveType(ctx.primitiveTypeCS());
    }
    if (ctx.collectionTypeCS() != null) {
      return resolveCollectionType(ctx.collectionTypeCS());
    }
    return Type.ANY;
  }

  /**
   * Resolves a primitive type literal.
   *
   * <p>Maps OCL primitive type names to VitruvOCL Type constants:
   *
   * <ul>
   *   <li>Boolean → Type.BOOLEAN
   *   <li>Integer, UnlimitedNatural → Type.INTEGER
   *   <li>Real → Type.DOUBLE
   *   <li>String, ID → Type.STRING
   *   <li>OclAny → Type.ANY
   * </ul>
   *
   * @param ctx The primitive type parse tree node
   * @return The primitive Type constant
   */
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

  /**
   * Resolves a collection type literal.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code Set{Integer}} → Type.set(Type.INTEGER)
   *   <li>{@code Sequence[String]} → Type.sequence(Type.STRING)
   *   <li>{@code Bag{{Real}}} → Type.bag(Type.DOUBLE)
   * </ul>
   *
   * <p>Element type is resolved recursively, defaulting to Type.ANY if unresolvable.
   *
   * @param ctx The collection type parse tree node
   * @return The collection Type
   */
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
      case "Collection" -> Type.set(elementType); // Generic collection defaults to Set
      default -> Type.set(Type.ANY);
    };
  }

  // ==================== Iterator Operations ====================

  /**
   * Handles select operation: {@code collection.select(x | x > 5)}.
   *
   * <p>Creates iterator scope and defines iterator variable(s).
   *
   * @param ctx The select operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSelectOp(VitruvOCLParser.SelectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Handles reject operation: {@code collection.reject(x | x <= 5)}.
   *
   * @param ctx The reject operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitRejectOp(VitruvOCLParser.RejectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Handles collect operation: {@code collection.collect(x | x.property)}.
   *
   * @param ctx The collect operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectOp(VitruvOCLParser.CollectOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Handles forAll operation: {@code collection.forAll(x | x > 0)}.
   *
   * @param ctx The forAll operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitForAllOp(VitruvOCLParser.ForAllOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Handles exists operation: {@code collection.exists(x | x < 0)}.
   *
   * @param ctx The exists operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitExistsOp(VitruvOCLParser.ExistsOpContext ctx) {
    return visitIteratorOp(ctx, ctx.iteratorVars, ctx.body);
  }

  /**
   * Generic handler for all iterator operations (select, reject, collect, forAll, exists).
   *
   * <p>Creates an iterator scope and defines iterator variables with placeholder type Type.ANY.
   * Pass 2 will refine these to the actual element type of the source collection.
   *
   * <p>Examples:
   *
   * <pre>{@code
   * collection.select(x | x > 5)           // Single iterator: x
   * collection.forAll(a, b | a != b)       // Multiple iterators: a, b
   * }</pre>
   *
   * <p>The iterator scope is annotated on the parse tree node for Pass 2 retrieval.
   *
   * @param ctx The iterator operation parse tree node
   * @param iteratorVars The iterator variable list
   * @param body The iterator body expression
   * @return null (void visitor)
   */
  private Void visitIteratorOp(
      ParserRuleContext ctx,
      VitruvOCLParser.IteratorVarListContext iteratorVars,
      VitruvOCLParser.ExpCSContext body) {

    // Extract iterator variable names
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

    // Create iterator scope as child of current scope
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    // Annotate parse tree node with scope for Pass 2 variable lookup
    scopeAnnotator.annotate(ctx, iterScope);

    try {
      // Define all iterator variables with placeholder type
      // (Pass 2 will refine to actual element type)
      for (String iterVar : iterVars) {
        symbolTable.defineVariable(new VariableSymbol(iterVar, Type.ANY, iterScope, true));
      }

      // Visit body to collect nested lets/iterators
      visit(body);

      return null;
    } finally {
      symbolTable.exitScope();
    }
  }

  // ==================== Traversal Methods ====================
  // These methods traverse the parse tree to find let expressions and iterator operations
  // without affecting the symbol table. They ensure we visit all nested scopes.

  /**
   * Visits a primary expression with navigation chains.
   *
   * <p>Example: {@code self.friends.select(f | f.age > 18)}
   *
   * @param ctx The primary with navigation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitPrimaryWithNav(VitruvOCLParser.PrimaryWithNavContext ctx) {
    visit(ctx.base);
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      visit(nav);
    }
    return null;
  }

  /**
   * Visits a navigation chain (property access or operation call).
   *
   * @param ctx The navigation chain parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitNavigationChainCS(VitruvOCLParser.NavigationChainCSContext ctx) {
    return visit(ctx.navigationTargetCS());
  }

  /**
   * Visits an operation call in a navigation chain.
   *
   * @param ctx The operation navigation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
    return visit(ctx.operationCall());
  }

  /**
   * Visits an if-then-else expression.
   *
   * <p>Example: {@code if x > 0 then x else -x endif}
   *
   * @param ctx The if expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  /**
   * Visits a nested expression (parenthesized).
   *
   * <p>Example: {@code (x + y) * z}
   *
   * @param ctx The nested expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  /**
   * Visits a prefixed qualified expression.
   *
   * <p>Example: {@code package::Class::operation()}
   *
   * @param ctx The prefixed qualified expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitPrefixedQualified(VitruvOCLParser.PrefixedQualifiedContext ctx) {
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      visit(nav);
    }
    return null;
  }

  // ==================== Delegation Methods ====================
  // Binary and unary operations don't affect symbol table, just traverse operands

  /**
   * Visits a comparison operation (=, &lt;&gt;, &lt;, &gt;, &lt;=, &gt;=).
   *
   * @param ctx The comparison parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitComparison(VitruvOCLParser.ComparisonContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits a multiplicative operation (*, /, mod).
   *
   * @param ctx The multiplicative parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitMultiplicative(VitruvOCLParser.MultiplicativeContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits an additive operation (+, -).
   *
   * @param ctx The additive parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitAdditive(VitruvOCLParser.AdditiveContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits a logical operation (and, or, xor).
   *
   * @param ctx The logical parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitLogical(VitruvOCLParser.LogicalContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits an implication operation (implies).
   *
   * @param ctx The implication parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitImplication(VitruvOCLParser.ImplicationContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits a unary minus operation (-x).
   *
   * @param ctx The unary minus parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitUnaryMinus(VitruvOCLParser.UnaryMinusContext ctx) {
    return visit(ctx.operand);
  }

  /**
   * Visits a logical not operation (not x).
   *
   * @param ctx The logical not parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitLogicalNot(VitruvOCLParser.LogicalNotContext ctx) {
    return visit(ctx.operand);
  }

  /**
   * Visits a correspondence operation (~ operator for cross-metamodel constraints).
   *
   * <p>Example: {@code spacecraft ~ satellite}
   *
   * @param ctx The correspondence parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  /**
   * Visits a message operation (~ operator for cross-metamodel correspondence).
   *
   * <p>Example: {@code spacecraft ~ satellite}
   *
   * @param ctx The message parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitMessage(VitruvOCLParser.MessageContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    return null;
  }

  // ==================== Collection Operations with Arguments ====================

  /**
   * Visits including operation: {@code collection.including(element)}.
   *
   * <p>Traverses argument to collect nested let/iterator scopes.
   *
   * @param ctx The including operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitIncludingOp(VitruvOCLParser.IncludingOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits excluding operation: {@code collection.excluding(element)}.
   *
   * @param ctx The excluding operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitExcludingOp(VitruvOCLParser.ExcludingOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits includes operation: {@code collection.includes(element)}.
   *
   * @param ctx The includes operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitIncludesOp(VitruvOCLParser.IncludesOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits excludes operation: {@code collection.excludes(element)}.
   *
   * @param ctx The excludes operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitExcludesOp(VitruvOCLParser.ExcludesOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits union operation: {@code collection1.union(collection2)}.
   *
   * @param ctx The union operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitUnionOp(VitruvOCLParser.UnionOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits append operation: {@code sequence.append(element)}.
   *
   * @param ctx The append operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitAppendOp(VitruvOCLParser.AppendOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits concat operation: {@code string1.concat(string2)}.
   *
   * @param ctx The concat operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitConcatOp(VitruvOCLParser.ConcatOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits substring operation: {@code string.substring(start, end)}.
   *
   * <p>Traverses both start and end arguments.
   *
   * @param ctx The substring operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSubstringOp(VitruvOCLParser.SubstringOpContext ctx) {
    visit(ctx.start);
    visit(ctx.end);
    return null;
  }

  /**
   * Visits indexOf operation: {@code string.indexOf(substring)}.
   *
   * @param ctx The indexOf operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitIndexOfOp(VitruvOCLParser.IndexOfOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  /**
   * Visits equalsIgnoreCase operation: {@code string1.equalsIgnoreCase(string2)}.
   *
   * @param ctx The equalsIgnoreCase operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitEqualsIgnoreCaseOp(VitruvOCLParser.EqualsIgnoreCaseOpContext ctx) {
    visit(ctx.arg);
    return null;
  }

  // ==================== Type Checking Operations ====================

  /**
   * Visits oclIsKindOf operation: {@code object.oclIsKindOf(Type)}.
   *
   * <p>Checks if object is an instance of Type or any of its subtypes.
   *
   * @param ctx The oclIsKindOf operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitOclIsKindOfOp(VitruvOCLParser.OclIsKindOfOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  /**
   * Visits oclIsTypeOf operation: {@code object.oclIsTypeOf(Type)}.
   *
   * <p>Checks if object is exactly an instance of Type (not subtypes).
   *
   * @param ctx The oclIsTypeOf operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitOclIsTypeOfOp(VitruvOCLParser.OclIsTypeOfOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  /**
   * Visits oclAsType operation: {@code object.oclAsType(Type)}.
   *
   * <p>Casts object to the specified Type.
   *
   * @param ctx The oclAsType operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    visit(ctx.type);
    return null;
  }

  // ==================== Collection Literals ====================

  /**
   * Visits collection literal expression.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code Set{1, 2, 3}}
   *   <li>{@code Sequence["a", "b", "c"]}
   *   <li>{@code Bag{1, 1, 2, 3}}
   * </ul>
   *
   * @param ctx The collection literal expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectionLiteralExpCS(VitruvOCLParser.CollectionLiteralExpCSContext ctx) {
    visit(ctx.collectionKind);
    if (ctx.arguments != null) {
      visit(ctx.arguments);
    }
    return null;
  }

  /**
   * Visits collection literal arguments (the elements inside the collection literal).
   *
   * @param ctx The collection arguments parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectionArguments(VitruvOCLParser.CollectionArgumentsContext ctx) {
    for (VitruvOCLParser.CollectionLiteralPartCSContext part : ctx.collectionLiteralPartCS()) {
      visit(part);
    }
    return null;
  }

  /**
   * Visits a single part of a collection literal (one element or range).
   *
   * @param ctx The collection literal part parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp);
    }
    return null;
  }

  // ==================== Leaf Operations (No Traversal Needed) ====================

  /**
   * Visits size operation: {@code collection.size()}.
   *
   * <p>No traversal needed - no arguments or nested scopes.
   *
   * @param ctx The size operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSizeOp(VitruvOCLParser.SizeOpContext ctx) {
    return null;
  }

  /**
   * Visits first operation: {@code sequence.first()}.
   *
   * @param ctx The first operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitFirstOp(VitruvOCLParser.FirstOpContext ctx) {
    return null;
  }

  /**
   * Visits last operation: {@code sequence.last()}.
   *
   * @param ctx The last operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitLastOp(VitruvOCLParser.LastOpContext ctx) {
    return null;
  }

  /**
   * Visits reverse operation: {@code sequence.reverse()}.
   *
   * @param ctx The reverse operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitReverseOp(VitruvOCLParser.ReverseOpContext ctx) {
    return null;
  }

  /**
   * Visits isEmpty operation: {@code collection.isEmpty()}.
   *
   * @param ctx The isEmpty operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitIsEmptyOp(VitruvOCLParser.IsEmptyOpContext ctx) {
    return null;
  }

  /**
   * Visits notEmpty operation: {@code collection.notEmpty()}.
   *
   * @param ctx The notEmpty operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitNotEmptyOp(VitruvOCLParser.NotEmptyOpContext ctx) {
    return null;
  }

  /**
   * Visits flatten operation: {@code nestedCollection.flatten()}.
   *
   * <p>Flattens one level of nesting: {@code Set{Set{1,2}, Set{3}} → Set{1,2,3}}
   *
   * @param ctx The flatten operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitFlattenOp(VitruvOCLParser.FlattenOpContext ctx) {
    return null;
  }

  /**
   * Visits sum operation: {@code collection.sum()}.
   *
   * @param ctx The sum operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    return null;
  }

  /**
   * Visits max operation: {@code collection.max()}.
   *
   * @param ctx The max operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitMaxOp(VitruvOCLParser.MaxOpContext ctx) {
    return null;
  }

  /**
   * Visits min operation: {@code collection.min()}.
   *
   * @param ctx The min operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitMinOp(VitruvOCLParser.MinOpContext ctx) {
    return null;
  }

  /**
   * Visits avg operation: {@code collection.avg()}.
   *
   * @param ctx The avg operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    return null;
  }

  /**
   * Visits abs operation: {@code number.abs()}.
   *
   * @param ctx The abs operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitAbsOp(VitruvOCLParser.AbsOpContext ctx) {
    return null;
  }

  /**
   * Visits floor operation: {@code number.floor()}.
   *
   * @param ctx The floor operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    return null;
  }

  /**
   * Visits ceil operation: {@code number.ceil()}.
   *
   * @param ctx The ceil operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    return null;
  }

  /**
   * Visits round operation: {@code number.round()}.
   *
   * @param ctx The round operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    return null;
  }

  /**
   * Visits lift operation: {@code value.lift()}.
   *
   * <p>Wraps singleton value in optional: {@code !5! → ?5?}
   *
   * @param ctx The lift operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitLiftOp(VitruvOCLParser.LiftOpContext ctx) {
    return null;
  }

  /**
   * Visits toUpper operation: {@code string.toUpper()}.
   *
   * @param ctx The toUpper operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitToUpperOp(VitruvOCLParser.ToUpperOpContext ctx) {
    return null;
  }

  /**
   * Visits toLower operation: {@code string.toLower()}.
   *
   * @param ctx The toLower operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitToLowerOp(VitruvOCLParser.ToLowerOpContext ctx) {
    return null;
  }

  /**
   * Visits allInstances operation: {@code Type.allInstances()}.
   *
   * <p>Returns all instances of a metamodel type from the VSUM.
   *
   * @param ctx The allInstances operation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitAllInstancesOp(VitruvOCLParser.AllInstancesOpContext ctx) {
    return null;
  }

  // ==================== Literal Nodes ====================

  /**
   * Visits number literal: {@code 42}, {@code 3.14}.
   *
   * <p>No traversal needed - leaf node.
   *
   * @param ctx The number literal parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitNumberLit(VitruvOCLParser.NumberLitContext ctx) {
    return null;
  }

  /**
   * Visits string literal: {@code "hello"}.
   *
   * <p>No traversal needed - leaf node.
   *
   * @param ctx The string literal parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitStringLit(VitruvOCLParser.StringLitContext ctx) {
    return null;
  }

  /**
   * Visits boolean literal: {@code true}, {@code false}.
   *
   * <p>No traversal needed - leaf node.
   *
   * @param ctx The boolean literal parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitBooleanLit(VitruvOCLParser.BooleanLitContext ctx) {
    return null;
  }

  /**
   * Visits variable reference: {@code x}, {@code myVariable}.
   *
   * <p>No traversal needed - leaf node. Variable lookup happens in Pass 2.
   *
   * @param ctx The variable expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    return null;
  }

  /**
   * Visits self reference: {@code self}.
   *
   * <p>No traversal needed - leaf node. Self lookup happens in Pass 2.
   *
   * @param ctx The self expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    return null;
  }

  /**
   * Visits property navigation: {@code object.property}.
   *
   * <p>No traversal needed - property access resolved in Pass 2.
   *
   * @param ctx The property navigation parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
    return null;
  }

  /**
   * Visits property access (direct property reference).
   *
   * <p>No traversal needed - property access resolved in Pass 2.
   *
   * @param ctx The property access parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    return null;
  }

  // ==================== Delegation Patterns ====================
  // These methods delegate to child nodes based on ANTLR labeled alternatives

  /**
   * Delegates to prefixed expression child node.
   *
   * @param ctx The prefixed expression parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitPrefixedExpr(VitruvOCLParser.PrefixedExprContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  /**
   * Delegates to literal expression child node.
   *
   * @param ctx The literal parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitLiteral(VitruvOCLParser.LiteralContext ctx) {
    return visit(ctx.literalExpCS());
  }

  /**
   * Delegates to if-expression child node.
   *
   * @param ctx The conditional parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitConditional(VitruvOCLParser.ConditionalContext ctx) {
    return visit(ctx.ifExpCS());
  }

  /**
   * Delegates to let-expression child node.
   *
   * @param ctx The let binding parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitLetBinding(VitruvOCLParser.LetBindingContext ctx) {
    return visit(ctx.letExpCS());
  }

  /**
   * Delegates to collection literal child node.
   *
   * @param ctx The collection literal parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitCollectionLiteral(VitruvOCLParser.CollectionLiteralContext ctx) {
    return visit(ctx.collectionLiteralExpCS());
  }

  /**
   * Delegates to type literal child node.
   *
   * @param ctx The type literal parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitTypeLiteral(VitruvOCLParser.TypeLiteralContext ctx) {
    return visit(ctx.typeLiteralExpCS());
  }

  /**
   * Delegates to nested expression child node.
   *
   * @param ctx The nested parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitNested(VitruvOCLParser.NestedContext ctx) {
    return visit(ctx.nestedExpCS());
  }

  /**
   * Delegates to self expression child node.
   *
   * @param ctx The self parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitSelf(VitruvOCLParser.SelfContext ctx) {
    return visit(ctx.selfExpCS());
  }

  /**
   * Delegates to variable expression child node.
   *
   * @param ctx The variable parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitVariable(VitruvOCLParser.VariableContext ctx) {
    return visit(ctx.variableExpCS());
  }

  /**
   * Delegates to type literal child node.
   *
   * @param ctx The type literal expression parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitTypeLiteralExpCS(VitruvOCLParser.TypeLiteralExpCSContext ctx) {
    return visit(ctx.typeLiteralCS());
  }

  /**
   * Delegates to collection operation child node.
   *
   * @param ctx The collection operation parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitCollectionOperation(VitruvOCLParser.CollectionOperationContext ctx) {
    return visit(ctx.collectionOpCS());
  }

  /**
   * Delegates to string operation child node.
   *
   * @param ctx The string operation parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitStringOperation(VitruvOCLParser.StringOperationContext ctx) {
    return visit(ctx.stringOpCS());
  }

  /**
   * Delegates to iterator operation child node.
   *
   * @param ctx The iterator operation parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitIteratorOperation(VitruvOCLParser.IteratorOperationContext ctx) {
    return visit(ctx.iteratorOpCS());
  }

  /**
   * Delegates to type operation child node.
   *
   * @param ctx The type operation parse tree node
   * @return Result from child visitor
   */
  @Override
  public Void visitTypeOperation(VitruvOCLParser.TypeOperationContext ctx) {
    return visit(ctx.typeOpCS());
  }

  // ==================== Type Expression Nodes (No Symbol Table Impact) ====================

  /**
   * Visits type expression node.
   *
   * <p>Type expressions don't affect symbol table - they're resolved during type checking in Pass
   * 2.
   *
   * @param ctx The type expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitTypeExpCS(VitruvOCLParser.TypeExpCSContext ctx) {
    return null; // Type expressions don't affect symbol table
  }

  /**
   * Visits type literal node.
   *
   * <p>Type literals are resolved during type checking, not during symbol table construction.
   *
   * @param ctx The type literal parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitTypeLiteralCS(VitruvOCLParser.TypeLiteralCSContext ctx) {
    return null;
  }

  /**
   * Visits collection type node.
   *
   * <p>Collection types are resolved when needed, not during symbol table construction.
   *
   * @param ctx The collection type parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectionTypeCS(VitruvOCLParser.CollectionTypeCSContext ctx) {
    return null;
  }

  /**
   * Visits collection type identifier (Set, Sequence, Bag, OrderedSet).
   *
   * <p>No symbol table impact.
   *
   * @param ctx The collection type identifier parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitCollectionTypeIdentifier(VitruvOCLParser.CollectionTypeIdentifierContext ctx) {
    return null;
  }

  /**
   * Visits primitive type node (Integer, String, Boolean, Real).
   *
   * <p>No symbol table impact.
   *
   * @param ctx The primitive type parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitPrimitiveTypeCS(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    return null;
  }

  /**
   * Visits type name expression node.
   *
   * <p>Type names are resolved when needed, not during symbol table construction.
   *
   * @param ctx The type name expression parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitTypeNameExpCS(VitruvOCLParser.TypeNameExpCSContext ctx) {
    return null;
  }

  /**
   * Visits onespace node (grammar rule for whitespace handling).
   *
   * <p>No symbol table impact.
   *
   * @param ctx The onespace parse tree node
   * @return null (void visitor)
   */
  @Override
  public Void visitOnespace(VitruvOCLParser.OnespaceContext ctx) {
    return null;
  }
}