package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.Scope;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.Symbol;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.VariableSymbol;

/**
 * Phase 2 visitor that performs static type checking on OCL expressions.
 *
 * <p>This visitor implements the <b>type checking phase</b> of the VitruvOCL compiler pipeline,
 * operating after symbol table construction (Phase 1) and before evaluation (Phase 3). It validates
 * type rules, ensures type safety, and annotates the parse tree with type information.
 *
 * <h2>Core Responsibilities</h2>
 *
 * <ul>
 *   <li><b>Type inference:</b> Determines the type of each expression in the parse tree
 *   <li><b>Type checking:</b> Validates that operations are applied to compatible types
 *   <li><b>Type annotation:</b> Stores computed types in {@code nodeTypes} for use by the evaluator
 *   <li><b>Error reporting:</b> Collects type errors with source location information
 *   <li><b>Scope management:</b> Handles variable scoping for let-expressions and iterators
 * </ul>
 *
 * <h2>Architecture</h2>
 *
 * The type checker uses a {@code receiverStack} to track receiver types during navigation chain
 * type checking. For example, in {@code person.company.employees}, the stack helps propagate types
 * through the chain:
 *
 * <ol>
 *   <li>Push {@code Person} type
 *   <li>Check {@code .company} → push {@code Company} type
 *   <li>Check {@code .employees} → push {@code Set(Employee)} type
 * </ol>
 *
 * <h2>Type System Features</h2>
 *
 * <ul>
 *   <li><b>OCL# semantics:</b> "Everything is a collection" - singletons are {@code
 *       Collection(T,1,1)}
 *   <li><b>Collection types:</b> Set, Bag, Sequence, OrderedSet with element types
 *   <li><b>Primitive types:</b> Integer, String, Boolean, Real (Double)
 *   <li><b>Metaclass types:</b> Types representing EMF EClasses
 *   <li><b>Type conformance:</b> Subtype relationships and type compatibility checking
 *   <li><b>Implicit operations:</b> Implicit collect on collections during property access
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * Type errors are collected in the {@link ErrorCollector} with severity levels (ERROR, WARNING).
 * The visitor continues checking after errors to report multiple issues in one pass. Errors
 * include:
 *
 * <ul>
 *   <li>Type mismatches in operations (e.g., {@code 5 + "hello"})
 *   <li>Unknown properties or operations
 *   <li>Incompatible types in if-then-else branches
 *   <li>Invalid receiver types for operations
 *   <li>Variable redefinition in scopes
 * </ul>
 *
 * <h2>Example Type Checking</h2>
 *
 * <pre>{@code
 * // Type checking this constraint:
 * context Person inv:
 *   self.age >= 18 and self.company.employees->size() > 0
 *
 * // Type checking flow:
 * 1. context Person → bind self: Person
 * 2. self.age → Person has age: Integer → Integer
 * 3. 18 → Integer
 * 4. >= → requires Integer, Integer → Boolean ✓
 * 5. self.company → Person.company: Company → !Company!
 * 6. .employees → Company.employees: Set(Employee) → Set(Employee)
 * 7. ->size() → Set(T).size(): Integer → Integer
 * 8. > 0 → Integer > Integer → Boolean ✓
 * 9. and → Boolean and Boolean → Boolean ✓
 * }</pre>
 *
 * <h2>Usage in Pipeline</h2>
 *
 * <pre>{@code
 * SymbolTable symbolTable = symbolTableBuilder.build(parseTree);
 * TypeCheckVisitor typeChecker = new TypeCheckVisitor(
 *     symbolTable, metamodelWrapper, errorCollector);
 * typeChecker.setTokenStream(tokens);
 * typeChecker.visit(parseTree);
 *
 * if (!errorCollector.hasErrors()) {
 *     ParseTreeProperty<Type> nodeTypes = typeChecker.getNodeTypes();
 *     // Pass to evaluation phase
 * }
 * }</pre>
 *
 * @author Max
 * @see Type The type system implementation
 * @see TypeResolver Helper class for binary operation type resolution
 * @see EvaluationVisitor Phase 3 visitor that uses the type information
 */
public class TypeCheckVisitor extends AbstractPhaseVisitor<Type> {

  // ==================== Instance Fields ====================

  /**
   * Stack of receiver types for navigation chain type checking.
   *
   * <p>During navigation like {@code a.b.c()}, this stack tracks the type at each step:
   *
   * <ul>
   *   <li>Push type of {@code a}
   *   <li>Check {@code .b} using type from stack, push result type
   *   <li>Check {@code .c()} using type from stack
   * </ul>
   *
   * This allows operation visitor methods to access their receiver type via {@code
   * receiverStack.peek()}.
   */
  private final Deque<Type> receiverStack = new ArrayDeque<>();

  /**
   * Maps parse tree nodes to their computed types.
   *
   * <p>This property is populated during type checking and retrieved by {@link #getNodeTypes()} for
   * use in the evaluation phase. The evaluator uses these pre-computed types for type-dependent
   * operations.
   */
  private final ParseTreeProperty<Type> nodeTypes = new ParseTreeProperty<>();

  /** Symbol table for variable and type resolution. */
  private final SymbolTable symbolTable;

  /**
   * Scope annotator for retrieving scopes created in Pass 1.
   *
   * <p>Pass 1 (SymbolTableBuilder) annotates parse tree nodes with their scopes. Pass 2 (this
   * visitor) retrieves these annotations to enter the correct scopes.
   */
  private final ScopeAnnotator scopeAnnotator;

  /**
   * Token stream for accessing keyword positions.
   *
   * <p>Used in {@link #visitIfExpCS} to determine which expressions belong to condition,
   * then-branch, and else-branch based on keyword positions.
   */
  private org.antlr.v4.runtime.TokenStream tokens;

  // ==================== Constructor ====================

  /**
   * Constructs a TypeCheckVisitor for Phase 2 of the compilation pipeline.
   *
   * @param symbolTable The symbol table containing variable and type definitions from Phase 1
   * @param wrapper The metamodel wrapper providing access to ECore metamodel information
   * @param errors The error collector for reporting type errors
   * @param scopeAnnotator The scope annotator containing scope annotations from Phase 1
   */
  public TypeCheckVisitor(
      SymbolTable symbolTable,
      MetamodelWrapperInterface wrapper,
      ErrorCollector errors,
      ScopeAnnotator scopeAnnotator) {
    super(symbolTable, wrapper, errors);
    this.symbolTable = symbolTable;
    this.scopeAnnotator = scopeAnnotator;
  }

  // ==================== Error Reporting ====================

  /**
   * Handles undefined symbol errors.
   *
   * <p>Currently throws {@link UnsupportedOperationException} - should be implemented to report
   * errors properly.
   *
   * @param name The undefined symbol name
   * @param ctx The parse tree context where the error occurred
   * @throws UnsupportedOperationException Always (not yet implemented)
   */
  @Override
  protected void handleUndefinedSymbol(String name, ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Undefined variable: " + name,
        ErrorSeverity.ERROR,
        "type-checker");
  }

  // ==================== Context Declaration ====================

  /**
   * Type checks the top-level context declaration.
   *
   * <p>Processes all classifier contexts (e.g., {@code context Person}, {@code context Company})
   * and returns the type of the last one.
   *
   * @param ctx The context declaration node
   * @return The type of the last classifier context
   */
  @Override
  public Type visitContextDeclCS(VitruvOCLParser.ContextDeclCSContext ctx) {
    Type lastType = Type.ERROR;

    for (VitruvOCLParser.ClassifierContextCSContext classifierCtx : ctx.classifierContextCS()) {
      lastType = visit(classifierCtx);
    }

    nodeTypes.put(ctx, lastType);
    return lastType;
  }

  /**
   * Type checks a classifier context declaration.
   *
   * <p>A classifier context binds constraints to a metaclass type. This method:
   *
   * <ol>
   *   <li>Resolves the context type (qualified or unqualified name)
   *   <li>Creates a new scope for the context
   *   <li>Binds {@code self} to the context type
   *   <li>Type checks all invariants within this scope
   * </ol>
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Person inv:           // Unqualified
   *   self.age >= 0
   *
   * context model::Employee inv:  // Qualified
   *   self.salary > 0
   * }</pre>
   *
   * @param ctx The classifier context node
   * @return The context type (metaclass type)
   */
  /**
   * Type checks a classifier context declaration.
   *
   * <p>Enters the scope created by Pass 1 (SymbolTableBuilder) which already contains the 'self'
   * variable.
   */
  @Override
  public Type visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    // Resolve context type (qualified or unqualified)
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
          "type-checker");
      return Type.ERROR;
    }

    if (contextType == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unknown context type",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, contextType);

    // Enter scope created by Pass 1 - scope already contains 'self' variable
    Scope contextScope = scopeAnnotator.getScope(ctx);
    if (contextScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(contextScope);

    try {
      // Type-check all invariants
      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        visit(inv);
      }
      return contextType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks an invariant constraint.
   *
   * <p>Invariants must evaluate to Boolean type. This method checks all specification expressions
   * and validates that they are conformant to Boolean.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * inv: self.age >= 0        // Must be Boolean
   * inv: self.name.size() > 0 // Must be Boolean
   * }</pre>
   *
   * @param ctx The invariant node
   * @return Type.BOOLEAN if valid, Type.ERROR otherwise
   */
  @Override
  public Type visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Type resultType = Type.BOOLEAN;

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      Type specType = visit(spec);

      // Check Boolean conformance (handles both Boolean and !Boolean!)
      if (!specType.isConformantTo(Type.BOOLEAN) && !specType.equals(Type.ERROR)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Invariant must be Boolean, got " + specType,
            ErrorSeverity.ERROR,
            "type-checker");
        resultType = Type.ERROR;
      }
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Type Expressions ====================

  /**
   * Type checks a type expression.
   *
   * <p>Type expressions represent types in declarations and type operations. Delegates to either
   * type name or type literal checking.
   *
   * @param ctx The type expression node
   * @return The resolved type
   */
  @Override
  public Type visitTypeExpCS(VitruvOCLParser.TypeExpCSContext ctx) {
    if (ctx.typeNameExpCS() != null) {
      return visitTypeNameExpCS(ctx.typeNameExpCS());
    }
    if (ctx.typeLiteralCS() != null) {
      return visitTypeLiteralCS(ctx.typeLiteralCS());
    }

    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Invalid type expression",
        ErrorSeverity.ERROR,
        "type-checker");
    return Type.ERROR;
  }

  /**
   * Type checks a type literal (primitive or collection type).
   *
   * @param ctx The type literal node
   * @return The resolved type
   */
  @Override
  public Type visitTypeLiteralCS(VitruvOCLParser.TypeLiteralCSContext ctx) {
    if (ctx.primitiveTypeCS() != null) {
      return visitPrimitiveTypeCS(ctx.primitiveTypeCS());
    }
    if (ctx.collectionTypeCS() != null) {
      return visit(ctx.collectionTypeCS());
    }

    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Invalid type literal",
        ErrorSeverity.ERROR,
        "type-checker");
    return Type.ERROR;
  }

  /**
   * Type checks a collection type declaration.
   *
   * <p>Constructs collection types from their syntax. Supports:
   *
   * <ul>
   *   <li>{@code Set(T)} - unordered, unique
   *   <li>{@code Bag(T)} - unordered, non-unique
   *   <li>{@code Sequence(T)} - ordered, non-unique
   *   <li>{@code OrderedSet(T)} - ordered, unique
   *   <li>{@code Collection(T)} - generic (defaults to Set)
   * </ul>
   *
   * <p><b>Example:</b> {@code Set(Integer)}, {@code Sequence(Person)}
   *
   * @param ctx The collection type node
   * @return The constructed collection type
   */
  @Override
  public Type visitCollectionTypeCS(VitruvOCLParser.CollectionTypeCSContext ctx) {
    String kind = ctx.collectionKind.getText();

    // Get element type if specified
    Type elementType = Type.ANY; // Default placeholder
    if (ctx.typeExpCS() != null) {
      elementType = visit(ctx.typeExpCS());
      if (elementType == Type.ERROR) {
        return Type.ERROR;
      }
    }

    Type collectionType =
        switch (kind) {
          case "Set" -> Type.set(elementType);
          case "Sequence" -> Type.sequence(elementType);
          case "Bag" -> Type.bag(elementType);
          case "OrderedSet" -> Type.orderedSet(elementType);
          case "Collection" -> Type.set(elementType); // Generic → Set
          default -> {
            errors.add(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                "Unknown collection type: " + kind,
                ErrorSeverity.ERROR,
                "type-checker");
            yield Type.ERROR;
          }
        };

    nodeTypes.put(ctx, collectionType);
    return collectionType;
  }

  /**
   * Handles collection type identifiers (lexical tokens).
   *
   * <p>Not used for actual type construction - parent {@link #visitCollectionTypeCS} handles that.
   *
   * @param ctx The collection type identifier node
   * @return Type.ANY (placeholder)
   */
  @Override
  public Type visitCollectionTypeIdentifier(VitruvOCLParser.CollectionTypeIdentifierContext ctx) {
    return Type.ANY; // Placeholder, not used
  }

  /**
   * Type checks a primitive type reference.
   *
   * <p>Maps OCL primitive type names to internal {@link Type} constants:
   *
   * <ul>
   *   <li>Boolean → {@link Type#BOOLEAN}
   *   <li>Integer → {@link Type#INTEGER}
   *   <li>Real → {@link Type#DOUBLE}
   *   <li>String → {@link Type#STRING}
   *   <li>ID → {@link Type#STRING} (mapped)
   *   <li>UnlimitedNatural → {@link Type#INTEGER} (mapped)
   *   <li>OclAny → {@link Type#ANY}
   * </ul>
   *
   * @param ctx The primitive type node
   * @return The corresponding primitive type
   */
  @Override
  public Type visitPrimitiveTypeCS(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    String typeName = ctx.getText();

    Type primitiveType =
        switch (typeName) {
          case "Boolean" -> Type.BOOLEAN;
          case "Integer" -> Type.INTEGER;
          case "Real" -> Type.DOUBLE;
          case "String" -> Type.STRING;
          case "ID" -> Type.STRING; // Map to String
          case "UnlimitedNatural" -> Type.INTEGER; // Map to Integer
          case "OclAny" -> Type.ANY;
          default -> null;
        };

    if (primitiveType == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unknown primitive type: " + typeName,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, primitiveType);
    return primitiveType;
  }

  /**
   * Type checks a type name expression (qualified or unqualified).
   *
   * <p>Resolves type names to their corresponding types. Handles:
   *
   * <ul>
   *   <li>Primitive types: Integer, String, Boolean, Real
   *   <li>Metamodel types (qualified): {@code metamodel::ClassName}
   *   <li>Metamodel types (unqualified): {@code ClassName}
   * </ul>
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Integer} → Type.INTEGER
   *   <li>{@code Person} → metaclass type for Person
   *   <li>{@code company::Employee} → metaclass type for Employee
   * </ul>
   *
   * @param ctx The type name expression node
   * @return The resolved type
   */
  @Override
  public Type visitTypeNameExpCS(VitruvOCLParser.TypeNameExpCSContext ctx) {
    String typeName;

    if (ctx.metamodel != null && ctx.className != null) {
      typeName = ctx.metamodel.getText() + "::" + ctx.className.getText();
    } else if (ctx.unqualified != null) {
      typeName = ctx.unqualified.getText();
    } else {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Invalid type name",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Check primitives first
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
      nodeTypes.put(ctx, primitiveType);
      return primitiveType;
    }

    // Lookup in symbol table (metamodel types)
    Type resolvedType = symbolTable.lookupType(typeName);
    if (resolvedType == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unknown type: " + typeName,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, resolvedType);
    return resolvedType;
  }

  // ==================== Specifications & Expressions ====================

  /**
   * Type checks a specification (OCL constraint body).
   *
   * <p>Evaluates all expressions in sequence and returns the type of the last one.
   *
   * @param ctx The specification node
   * @return The type of the last expression
   */
  @Override
  public Type visitSpecificationCS(VitruvOCLParser.SpecificationCSContext ctx) {
    if (ctx.expCS().isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Empty specification",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = null;
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      resultType = visit(exp);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Delegates expression type checking to infixed expression.
   *
   * @param ctx The expression node
   * @return The expression type
   */
  @Override
  public Type visitExpCS(VitruvOCLParser.ExpCSContext ctx) {
    return visit(ctx.infixedExpCS());
  }

  // ==================== Comparison Operations ====================

  /**
   * Type checks comparison operations (==, !=, <, <=, >, >=).
   *
   * <p>All comparison operators return Boolean. Validates that operands are comparable (same type
   * or one conforms to the other).
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code 5 < 10} → Boolean
   *   <li>{@code "hello" == "world"} → Boolean
   *   <li>{@code person1 == person2} → Boolean
   * </ul>
   *
   * @param ctx The comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitComparison(VitruvOCLParser.ComparisonContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;

    // Check if types are comparable
    if (!areComparable(leftType, rightType)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Cannot compare incompatible types: " + leftType + " and " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
      resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Checks if two types are comparable.
   *
   * <p>Types are comparable if:
   *
   * <ul>
   *   <li>Either is Type.ERROR (error already reported)
   *   <li>They are equal
   *   <li>One conforms to the other (subtype relationship)
   * </ul>
   *
   * @param t1 First type
   * @param t2 Second type
   * @return true if types are comparable
   */
  private boolean areComparable(Type t1, Type t2) {
    if (t1 == Type.ERROR || t2 == Type.ERROR) return true;
    if (t1.equals(t2)) return true;
    if (t1.isConformantTo(t2) || t2.isConformantTo(t1)) return true;
    return false;
  }

  // ==================== Arithmetic Operations ====================

  /**
   * Type checks multiplicative operations (* and /).
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to determine result type based on operand types.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code 6 * 7} → Integer
   *   <li>{@code 10 / 2} → Integer
   * </ul>
   *
   * @param ctx The multiplicative operation node
   * @return The result type (typically Integer)
   */
  @Override
  public Type visitMultiplicative(VitruvOCLParser.MultiplicativeContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = ctx.op.getText();
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks additive operations (+ and -).
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to determine result type.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code 3 + 4} → Integer
   *   <li>{@code 10 - 3} → Integer
   * </ul>
   *
   * @param ctx The additive operation node
   * @return The result type (typically Integer)
   */
  @Override
  public Type visitAdditive(VitruvOCLParser.AdditiveContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = ctx.op.getText();
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Logical Operations ====================

  /**
   * Type checks logical operations (and, or, xor).
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to validate Boolean operands and return Boolean
   * result.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code true and false} → Boolean
   *   <li>{@code true or false} → Boolean
   *   <li>{@code true xor true} → Boolean
   * </ul>
   *
   * @param ctx The logical operation node
   * @return Type.BOOLEAN if operands are Boolean, Type.ERROR otherwise
   */
  @Override
  public Type visitLogical(VitruvOCLParser.LogicalContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = ctx.op.getText();
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the implication operation (implies).
   *
   * <p>Both operands must be Boolean. Returns Boolean.
   *
   * <p><b>Example:</b> {@code age >= 18 implies canVote == true} → Boolean
   *
   * @param ctx The implication operation node
   * @return Type.BOOLEAN if both operands are Boolean
   */
  @Override
  public Type visitImplication(VitruvOCLParser.ImplicationContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    if (!leftType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Left operand of 'implies' must be Boolean, got " + leftType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    if (!rightType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Right operand of 'implies' must be Boolean, got " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  // ==================== Unary Operations & Navigation ====================

  @Override
  public Type visitPrimaryWithNav(VitruvOCLParser.PrimaryWithNavContext ctx) {
    // Get base type from primary expression
    Type currentType = visit(ctx.base);

    if (currentType == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Base expression has no type",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Process navigation chain using receiverStack
    List<VitruvOCLParser.NavigationChainCSContext> navChain = ctx.navigationChainCS();
    receiverStack.push(currentType);
    for (VitruvOCLParser.NavigationChainCSContext nav : navChain) {
      Type resultType = visit(nav);
      receiverStack.pop();
      receiverStack.push(resultType);
      currentType = resultType;
    }
    if (!navChain.isEmpty()) {
      receiverStack.pop();
    }

    nodeTypes.put(ctx, currentType);
    return currentType;
  }

  @Override
  public Type visitUnaryMinus(VitruvOCLParser.UnaryMinusContext ctx) {
    Type operandType = visit(ctx.operand);

    // Unwrap one level
    Type baseType = operandType;
    if (baseType.isSingleton() || baseType.isCollection()) {
      baseType = baseType.getElementType();
    }

    if (baseType != Type.INTEGER && baseType != Type.DOUBLE && baseType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unary minus requires numeric type, got " + operandType,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, operandType);
    return operandType;
  }

  @Override
  public Type visitLogicalNot(VitruvOCLParser.LogicalNotContext ctx) {
    Type operandType = visit(ctx.operand);

    // Unwrap one level only
    Type baseType = operandType;
    if (baseType.isSingleton() || baseType.isCollection()) {
      baseType = baseType.getElementType();
    }

    if (baseType != Type.BOOLEAN && baseType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Logical not requires Boolean type, got " + operandType,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, operandType);
    return operandType;
  }

  // ==================== Cross-Metamodel Support ====================

  /**
   * Type checks fully qualified metaclass references.
   *
   * <p>Resolves qualified names like {@code spaceMission::Spacecraft} to their corresponding
   * metaclass types and handles navigation chains (typically {@code .allInstances()}).
   *
   * <p><b>Example:</b> {@code satelliteSystem::Satellite.allInstances()} → Set(Satellite)
   *
   * @param ctx The prefixed qualified name node
   * @return The result type after navigation
   */
  @Override
  public Type visitPrefixedQualified(VitruvOCLParser.PrefixedQualifiedContext ctx) {
    String metamodel = ctx.metamodel.getText();
    String className = ctx.className.getText();

    EClass eClass = specification.resolveEClass(metamodel, className);
    if (eClass == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Cannot resolve " + metamodel + "::" + className,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type metaclassType = Type.metaclassType(eClass);

    // Store the base type BEFORE navigation
    nodeTypes.put(ctx, metaclassType);

    // Process navigation chain (e.g., .allInstances())
    receiverStack.push(metaclassType);
    Type currentType = metaclassType;
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      currentType = visit(nav);
      nodeTypes.put(nav, currentType);
      receiverStack.pop();
      receiverStack.push(currentType);
    }
    if (!ctx.navigationChainCS().isEmpty()) {
      receiverStack.pop();
    }

    return currentType;
  }

  // ==================== Navigation ====================

  /**
   * Delegates navigation chain type checking to navigation target.
   *
   * @param ctx The navigation chain node
   * @return The navigation result type
   */
  @Override
  public Type visitNavigationChainCS(VitruvOCLParser.NavigationChainCSContext ctx) {
    return visit(ctx.navigationTargetCS());
  }

  /**
   * Type checks property navigation.
   *
   * <p>Accesses a property on the receiver type from the receiverStack.
   *
   * @param ctx The property navigation node
   * @return The property type
   */
  @Override
  public Type visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
    Type receiverType = receiverStack.peek();

    // Unwrap singleton for property access (OCL# compatibility)
    if (receiverType.getMultiplicity() == Multiplicity.SINGLETON) {
      receiverType = receiverType.getElementType();
    }

    String propertyName = ctx.propertyAccess().propertyName.getText();

    Type resultType = typeCheckPropertyAccess(receiverType, propertyName, ctx);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks operation navigation.
   *
   * <p>Delegates to the specific operation call visitor.
   *
   * @param ctx The operation navigation node
   * @return The operation result type
   */
  @Override
  public Type visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
    return visit(ctx.operationCall());
  }

  /**
   * Type checks property access on a receiver.
   *
   * <p>Handles both singleton and collection receivers:
   *
   * <ul>
   *   <li><b>Singleton:</b> {@code person.name} → direct property access
   *   <li><b>Collection:</b> {@code companies.name} → implicit collect, returns flattened
   *       collection
   * </ul>
   *
   * @param receiverType The type of the receiver
   * @param propName The property name
   * @param ctx The parse tree context for error reporting
   * @return The property type (may be wrapped in collection)
   */
  private Type typeCheckPropertyAccess(Type receiverType, String propName, ParserRuleContext ctx) {
    // Implicit collect for collections
    if (receiverType.isCollection()) {
      Type elemType = receiverType.getElementType();

      if (!elemType.isMetaclassType()) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Cannot navigate on non-object type",
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      EClass eClass = elemType.getEClass();
      EStructuralFeature feature = eClass.getEStructuralFeature(propName);

      if (feature == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown property: " + propName,
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      Type featureType = mapFeatureToType(feature);
      return Type.set(featureType.getElementType()); // Flatten
    }

    // Singleton property access
    if (receiverType.isMetaclassType()) {
      EClass eClass = receiverType.getEClass();
      EStructuralFeature feature = eClass.getEStructuralFeature(propName);

      if (feature == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown property: " + propName,
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      return mapFeatureToType(feature);
    }

    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Cannot access property on " + receiverType,
        ErrorSeverity.ERROR,
        "type-checker");
    return Type.ERROR;
  }

  /**
   * Maps an EMF structural feature to a VitruvOCL type.
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>Multi-valued features (upper bound > 1 or -1) → Set type
   *   <li>Single-valued features → Singleton type
   * </ul>
   *
   * @param feature The EMF structural feature
   * @return The corresponding VitruvOCL type
   */
  private Type mapFeatureToType(EStructuralFeature feature) {
    Type baseType = mapEClassifierToType(feature.getEType());

    if (feature.getUpperBound() > 1 || feature.getUpperBound() == -1) {
      return Type.set(baseType);
    } else {
      return Type.singleton(baseType);
    }
  }

  /**
   * Maps an EMF EClassifier to a VitruvOCL type.
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>EString → Type.STRING
   *   <li>EInt → Type.INTEGER
   *   <li>EBoolean → Type.BOOLEAN
   *   <li>EClass → metaclass type
   * </ul>
   *
   * @param classifier The EMF classifier
   * @return The corresponding VitruvOCL type
   */
  private Type mapEClassifierToType(EClassifier classifier) {
    // Handle primitive types
    switch (classifier.getName()) {
      case "EString":
        return Type.STRING;
      case "EInt":
        return Type.INTEGER;
      case "EBoolean":
        return Type.BOOLEAN;
      default:
        if (EcorePackage.Literals.ECLASS.equals(classifier.eClass())) {
          return Type.metaclassType((EClass) classifier);
        }
        return Type.ERROR;
    }
  }

  // ==================== Control Flow ====================

  /**
   * Type checks if-then-else expressions.
   *
   * <p>Validates:
   *
   * <ol>
   *   <li>Condition must be Boolean
   *   <li>Then and else branches must have compatible types
   * </ol>
   *
   * <p>Result type is the common supertype of both branches.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * if age >= 18 then 'adult' else 'minor' endif  // → String
   * if condition then 1 else 2.0 endif            // → Real (common supertype)
   * }</pre>
   *
   * @param ctx The if-expression node
   * @return The common supertype of both branches
   */
  @Override
  public Type visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    // Parse structure using token positions to identify condition, then, and else parts
    List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();

    int thenIndex = findKeywordIndex(ctx, "then");
    int elseIndex = findKeywordIndex(ctx, "else");

    // Partition expressions into condition, then-branch, and else-branch
    List<VitruvOCLParser.ExpCSContext> condExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> thenExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> elseExps = new ArrayList<>();

    for (VitruvOCLParser.ExpCSContext exp : allExps) {
      int expIndex = exp.getStart().getTokenIndex();
      if (expIndex < thenIndex) {
        condExps.add(exp);
      } else if (expIndex < elseIndex) {
        thenExps.add(exp);
      } else {
        elseExps.add(exp);
      }
    }

    // Type-check condition
    Type condType = null;
    for (VitruvOCLParser.ExpCSContext exp : condExps) {
      condType = visit(exp);
    }

    if (condType != null && !condType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "If condition must be Boolean",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    // Type-check then-branch
    Type thenType = null;
    for (VitruvOCLParser.ExpCSContext exp : thenExps) {
      thenType = visit(exp);
    }

    // Type-check else-branch
    Type elseType = null;
    for (VitruvOCLParser.ExpCSContext exp : elseExps) {
      elseType = visit(exp);
    }

    if (thenType == null || elseType == null) {
      return Type.ERROR;
    }

    // Determine result type (common supertype of branches)
    Type resultType;
    if (thenType.equals(elseType)) {
      resultType = thenType;
    } else if (thenType.isConformantTo(elseType)) {
      resultType = elseType;
    } else if (elseType.isConformantTo(thenType)) {
      resultType = thenType;
    } else {
      resultType = Type.commonSuperType(thenType, elseType);
      if (resultType == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Incompatible branch types",
            ErrorSeverity.ERROR,
            "type-checker");
        resultType = Type.ERROR;
      }
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Helper method to find keyword token index in if-expression.
   *
   * @param ctx The if-expression context
   * @param keyword The keyword to find ("then" or "else")
   * @return The token index, or Integer.MAX_VALUE if not found
   */
  private int findKeywordIndex(VitruvOCLParser.IfExpCSContext ctx, String keyword) {
    if (tokens == null) return Integer.MAX_VALUE;

    int start = ctx.getStart().getTokenIndex();
    int stop = ctx.getStop().getTokenIndex();

    for (int i = start; i <= stop; i++) {
      if (tokens.get(i).getText().equals(keyword)) {
        return i;
      }
    }
    return Integer.MAX_VALUE;
  }

  /**
   * Type checks let-expressions with variable bindings.
   *
   * <p>Creates a new scope, defines variables, and type-checks the body expression.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * let x = 5, y = 'hello' in x + y.size()  // → Integer
   * }</pre>
   *
   * @param ctx The let-expression node
   * @return The type of the body expression
   */
  @Override
  public Type visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    // Enter scope created by Pass 1 - variables already defined
    Scope letScope = scopeAnnotator.getScope(ctx);
    if (letScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(letScope);

    try {
      // Type-check variable declarations (validates type conformance only)
      visit(ctx.variableDeclarations());

      // Type-check body expressions
      List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();
      Type bodyType = null;
      for (VitruvOCLParser.ExpCSContext exp : allExps) {
        bodyType = visit(exp);
      }

      if (bodyType == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Let body empty",
            ErrorSeverity.ERROR,
            "type-checker");
        bodyType = Type.ERROR;
      }

      nodeTypes.put(ctx, bodyType);
      return bodyType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Processes variable declarations (in let-expressions).
   *
   * @param ctx The variable declarations node
   * @return Type.ANY (not used)
   */
  @Override
  public Type visitVariableDeclarations(VitruvOCLParser.VariableDeclarationsContext ctx) {
    for (VitruvOCLParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
      visit(varDecl);
    }
    return Type.ANY; // Not used
  }

  /**
   * Type checks a single variable declaration.
   *
   * <p>Validates:
   *
   * <ul>
   *   <li>No duplicate variable names in current scope
   *   <li>Initializer type conforms to declared type (if present)
   * </ul>
   *
   * <p><b>Example:</b> {@code let x : Integer = 5} - checks that 5 conforms to Integer
   *
   * @param ctx The variable declaration node
   * @return The variable type
   */
  @Override
  public Type visitVariableDeclaration(VitruvOCLParser.VariableDeclarationContext ctx) {
    String varName = ctx.varName.getText();

    // Variable already defined in Pass 1 - just look it up
    VariableSymbol symbol = symbolTable.resolveVariable(varName);
    if (symbol == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Variable '" + varName + "' not found (Pass 1 error?)",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Type-check initializer
    Type initType = visit(ctx.varInit);

    // Check explicit type if present
    Type declaredType = symbol.getType();
    if (ctx.varType != null) {
      Type explicitType = visit(ctx.varType);
      if (!initType.isConformantTo(explicitType)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Type mismatch: got " + initType + ", expected " + explicitType,
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }
      declaredType = explicitType;
      // Refine type if it was ANY from Pass 1
      if (symbol.getType() == Type.ANY) {
        symbol.setType(declaredType);
      }
    } else {
      // No explicit type - refine symbol with inferred type from initializer
      if (declaredType == Type.ANY) {
        symbol.setType(initType);
        declaredType = initType;
      } else if (!initType.isConformantTo(declaredType)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Type mismatch: got " + initType + ", expected " + declaredType,
            ErrorSeverity.ERROR,
            "type-checker");
      }
    }

    nodeTypes.put(ctx, declaredType);
    return declaredType;
  }

  // ==================== Literal Values ====================

  /**
   * Type checks integer literals.
   *
   * @param ctx The number literal node
   * @return Type.INTEGER
   */
  @Override
  public Type visitNumberLit(VitruvOCLParser.NumberLitContext ctx) {
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Type checks string literals.
   *
   * @param ctx The string literal node
   * @return Type.STRING
   */
  @Override
  public Type visitStringLit(VitruvOCLParser.StringLitContext ctx) {
    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  /**
   * Type checks boolean literals.
   *
   * @param ctx The boolean literal node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitBooleanLit(VitruvOCLParser.BooleanLitContext ctx) {
    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  // ==================== Variable References ====================

  /**
   * Type checks variable references.
   *
   * <p>Looks up the variable in the symbol table and returns its declared type.
   *
   * @param ctx The variable expression node
   * @return The variable's type
   */
  @Override
  public Type visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    String varName = ctx.varName.getText();

    VariableSymbol symbol = symbolTable.resolveVariable(varName);
    if (symbol == null) {
      handleUndefinedSymbol(varName, ctx);
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    Type varType = symbol.getType();
    nodeTypes.put(ctx, varType);
    return varType;
  }

  /**
   * Type checks {@code self} references.
   *
   * <p>Returns the type of the special {@code self} variable from the current context.
   *
   * @param ctx The self expression node
   * @return The context type (type of self)
   */
  @Override
  public Type visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    Symbol selfSymbol = symbolTable.resolveVariable("self");

    if (selfSymbol == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'self' not defined in current context",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    Type selfType = selfSymbol.getType();
    nodeTypes.put(ctx, selfType);
    return selfType;
  }

  // ==================== Collection Literals ====================

  /**
   * Type checks collection literal expressions.
   *
   * <p>Infers element type from collection arguments and constructs appropriate collection type.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{1,2,3}} → Set(Integer)
   *   <li>{@code Bag{'a','b'}} → Bag(String)
   *   <li>{@code Sequence{}} → Sequence(Any)
   * </ul>
   *
   * @param ctx The collection literal node
   * @return The inferred collection type
   */
  @Override
  public Type visitCollectionLiteralExpCS(VitruvOCLParser.CollectionLiteralExpCSContext ctx) {
    Type collectionType = visit(ctx.collectionKind);

    if (collectionType == Type.ERROR) {
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Empty collection: keep ANY as element type
    if (ctx.arguments == null) {
      nodeTypes.put(ctx, collectionType);
      return collectionType;
    }

    // Infer element type from arguments
    Type inferredElementType = visit(ctx.arguments);

    if (inferredElementType == Type.ERROR) {
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Create collection with inferred element type
    Type resultType = preserveCollectionKind(collectionType, inferredElementType);

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Preserves collection kind while changing element type.
   *
   * <p>Maintains the collection's unique/ordered properties when creating a new collection type
   * with a different element type.
   *
   * @param collectionType The original collection type
   * @param newElementType The new element type
   * @return A collection of the same kind with the new element type
   */
  private Type preserveCollectionKind(Type collectionType, Type newElementType) {
    if (collectionType.isUnique() && collectionType.isOrdered()) {
      return Type.orderedSet(newElementType);
    } else if (collectionType.isUnique()) {
      return Type.set(newElementType);
    } else if (collectionType.isOrdered()) {
      return Type.sequence(newElementType);
    } else {
      return Type.bag(newElementType);
    }
  }

  /**
   * Type checks collection arguments (elements in collection literal).
   *
   * <p>Computes the common supertype of all elements.
   *
   * @param ctx The collection arguments node
   * @return The common element type
   */
  @Override
  public Type visitCollectionArguments(VitruvOCLParser.CollectionArgumentsContext ctx) {
    Type commonType = null;

    for (VitruvOCLParser.CollectionLiteralPartCSContext part : ctx.collectionLiteralPartCS()) {
      Type partType = visit(part);

      if (partType == Type.ERROR) continue;

      if (commonType == null) {
        commonType = partType;
      } else if (!partType.equals(commonType)) {
        Type superType = Type.commonSuperType(commonType, partType);
        commonType = (superType != null) ? superType : Type.ANY;
      }
    }

    return (commonType != null) ? commonType : Type.ANY;
  }

  /**
   * Type checks a collection literal part (element or range).
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>Single elements: {@code 42}
   *   <li>Ranges: {@code 1..10} (both bounds must be Integer)
   * </ul>
   *
   * @param ctx The collection literal part node
   * @return The element type (Integer for ranges)
   */
  @Override
  public Type visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    Type firstType = visit(ctx.expCS(0));

    // Range: 1..10
    if (ctx.expCS().size() == 2) {
      Type secondType = visit(ctx.expCS(1));

      if (!firstType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Range start must be Integer",
            ErrorSeverity.ERROR,
            "type-checker");
      }
      if (!secondType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Range end must be Integer",
            ErrorSeverity.ERROR,
            "type-checker");
      }

      return Type.INTEGER;
    }

    return firstType;
  }

  // ==================== Collection Operations ====================

  /**
   * Type checks the {@code size()} operation.
   *
   * <p>Requires collection receiver, returns Integer.
   *
   * @param ctx The size operation node
   * @return Type.INTEGER
   */
  @Override
  public Type visitSizeOp(VitruvOCLParser.SizeOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "size() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = Type.INTEGER;
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code first()} operation.
   *
   * @param ctx The first operation node
   * @return Optional of element type
   */
  @Override
  public Type visitFirstOp(VitruvOCLParser.FirstOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "first() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }
    Type resultType = Type.singleton(receiverType.getElementType());
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code last()} operation.
   *
   * @param ctx The last operation node
   * @return Optional of element type
   */
  @Override
  public Type visitLastOp(VitruvOCLParser.LastOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "last() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }
    // Return singleton collection instead of optional
    Type resultType = Type.singleton(receiverType.getElementType());
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code reverse()} operation.
   *
   * @param ctx The reverse operation node
   * @return Same collection type as receiver
   */
  @Override
  public Type visitReverseOp(VitruvOCLParser.ReverseOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "reverse() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }
    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Type checks the {@code isEmpty()} operation.
   *
   * @param ctx The isEmpty operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitIsEmptyOp(VitruvOCLParser.IsEmptyOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "isEmpty() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }
    Type resultType = Type.singleton(Type.BOOLEAN);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code notEmpty()} operation.
   *
   * @param ctx The notEmpty operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitNotEmptyOp(VitruvOCLParser.NotEmptyOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "notEmpty() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = Type.BOOLEAN;
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks collection modification operations (including, excluding, etc.).
   *
   * <p>These operations validate argument type compatibility and return the receiver collection
   * type.
   */
  @Override
  public Type visitIncludingOp(VitruvOCLParser.IncludingOpContext ctx) {
    Type receiverType = receiverStack.peek();
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'including' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!argType.isConformantTo(elemType)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Incompatible argument type",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  @Override
  public Type visitExcludingOp(VitruvOCLParser.ExcludingOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excluding' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  @Override
  public Type visitIncludesOp(VitruvOCLParser.IncludesOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'includes' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  @Override
  public Type visitExcludesOp(VitruvOCLParser.ExcludesOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excludes' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Type checks the {@code flatten()} operation.
   *
   * <p>Requires Collection(Collection(T)), returns Collection(T).
   *
   * @param ctx The flatten operation node
   * @return Flattened collection type
   */
  @Override
  public Type visitFlattenOp(VitruvOCLParser.FlattenOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'flatten' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type innerType = receiverType.getElementType();
    if (!innerType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Expected Collection(Collection(T))",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type flatElementType = innerType.getElementType();
    Type resultType = preserveCollectionKind(receiverType, flatElementType);

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code union()} operation.
   *
   * <p>Combines two collections, computing result type based on collection properties.
   *
   * @param ctx The union operation node
   * @return Combined collection type
   */
  @Override
  public Type visitUnionOp(VitruvOCLParser.UnionOpContext ctx) {
    Type receiverType = receiverStack.peek();
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection() || !argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'union' requires collection operands",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type commonElemType =
        Type.commonSuperType(receiverType.getElementType(), argType.getElementType());

    boolean bothUnique = receiverType.isUnique() && argType.isUnique();
    boolean anyOrdered = receiverType.isOrdered() || argType.isOrdered();

    Type resultType;
    if (bothUnique && anyOrdered) {
      resultType = Type.orderedSet(commonElemType);
    } else if (bothUnique) {
      resultType = Type.set(commonElemType);
    } else if (anyOrdered) {
      resultType = Type.sequence(commonElemType);
    } else {
      resultType = Type.bag(commonElemType);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  @Override
  public Type visitAppendOp(VitruvOCLParser.AppendOpContext ctx) {
    // Same logic as union
    Type receiverType = receiverStack.peek();
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection() || !argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'append' requires collection operands",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type commonElemType =
        Type.commonSuperType(receiverType.getElementType(), argType.getElementType());
    boolean bothUnique = receiverType.isUnique() && argType.isUnique();
    boolean anyOrdered = receiverType.isOrdered() || argType.isOrdered();

    Type resultType;
    if (bothUnique && anyOrdered) {
      resultType = Type.orderedSet(commonElemType);
    } else if (bothUnique) {
      resultType = Type.set(commonElemType);
    } else if (anyOrdered) {
      resultType = Type.sequence(commonElemType);
    } else {
      resultType = Type.bag(commonElemType);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Aggregate Operations ====================

  /**
   * Type checks aggregate operations (sum, max, min, avg).
   *
   * <p>All require numeric collection receivers and return singleton Integer.
   */
  @Override
  public Type visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'sum' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (elemType != Type.ANY && !elemType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'sum' requires numeric collection",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    Type resultType = Type.singleton(Type.INTEGER);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  @Override
  public Type visitMaxOp(VitruvOCLParser.MaxOpContext ctx) {
    return visitAggregateOp(ctx, "max");
  }

  @Override
  public Type visitMinOp(VitruvOCLParser.MinOpContext ctx) {
    return visitAggregateOp(ctx, "min");
  }

  @Override
  public Type visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    return visitAggregateOp(ctx, "avg");
  }

  /** Helper for aggregate operation type checking. */
  private Type visitAggregateOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (elemType != Type.ANY && !elemType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires numeric collection",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    Type resultType = Type.singleton(Type.INTEGER);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Numeric Operations ====================

  /**
   * Type checks numeric operations (abs, floor, ceil, round).
   *
   * <p>All require numeric collection receivers and preserve collection type.
   */
  @Override
  public Type visitAbsOp(VitruvOCLParser.AbsOpContext ctx) {
    return visitNumericOp(ctx, "abs");
  }

  @Override
  public Type visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    return visitNumericOp(ctx, "floor");
  }

  @Override
  public Type visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    return visitNumericOp(ctx, "ceil");
  }

  @Override
  public Type visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    return visitNumericOp(ctx, "round");
  }

  /** Helper for numeric operation type checking. */
  private Type visitNumericOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!elemType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires numeric collection",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    // Preserve collection type
    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Type checks the {@code lift()} operation.
   *
   * <p>Wraps collection in another collection: Collection(T) → Collection(Collection(T))
   *
   * @param ctx The lift operation node
   * @return Nested collection type
   */
  @Override
  public Type visitLiftOp(VitruvOCLParser.LiftOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'lift' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = preserveCollectionKind(receiverType, receiverType);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Iterator Operations ====================

  /**
   * Type checks the {@code select()} iterator.
   *
   * <p>Creates scope with iterator variable, validates predicate is Boolean, returns same
   * collection type.
   *
   * @param ctx The select operation node
   * @return Same collection type as receiver
   */
  @Override
  public Type visitSelectOp(VitruvOCLParser.SelectOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'select' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Get iterator variables (already defined in Pass 1)
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "select requires at least one iterator variable",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();

    // Enter scope created by Pass 1 - iterator variables already defined
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      // Refine iterator variable types from Type.ANY to actual element type
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(elemType);
        }
      }

      Type bodyType = visit(ctx.body);

      if (!bodyType.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "select body must return Boolean",
            ErrorSeverity.ERROR,
            "type-checker");
      }

      nodeTypes.put(ctx, receiverType);
      return receiverType;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Type visitRejectOp(VitruvOCLParser.RejectOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'reject' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();

    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "reject requires at least one iterator variable",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Enter scope created by Pass 1
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      // Refine iterator variable types
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(elemType);
        }
      }

      Type bodyType = visit(ctx.body);

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "reject predicate must return Boolean",
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      nodeTypes.put(ctx, receiverType);
      return receiverType;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Type visitCollectOp(VitruvOCLParser.CollectOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'collect' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "collect requires at least one iterator variable",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();

    // Enter scope created by Pass 1
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      // Refine iterator variable types
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(elemType);
        }
      }

      Type bodyType = visit(ctx.body);

      if (bodyType == Type.ERROR) {
        return Type.ERROR;
      }

      Type resultType = preserveCollectionKind(receiverType, bodyType);
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Type visitForAllOp(VitruvOCLParser.ForAllOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'forAll' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();

    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "forAll requires at least one iterator variable",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Enter scope created by Pass 1
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      // Refine iterator variable types
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(elemType);
        }
      }

      Type bodyType = visit(ctx.body);

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "forAll predicate must return Boolean",
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      Type resultType = Type.BOOLEAN;
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Type visitExistsOp(VitruvOCLParser.ExistsOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'exists' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();

    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "exists requires at least one iterator variable",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Enter scope created by Pass 1
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Internal error: No scope annotation from Pass 1",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      // Refine iterator variable types
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(elemType);
        }
      }

      Type bodyType = visit(ctx.body);

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "exists predicate must return Boolean",
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      Type resultType = Type.BOOLEAN;
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  // ==================== String Operations ====================

  /**
   * Type checks string operations (concat, substring, toUpper, etc.).
   *
   * <p>All validate String receiver and appropriate argument types.
   */
  @Override
  public Type visitConcatOp(VitruvOCLParser.ConcatOpContext ctx) {
    Type receiverType = receiverStack.peek();
    Type argType = visit(ctx.arg);

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "concat requires String receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "concat argument must be String",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  @Override
  public Type visitSubstringOp(VitruvOCLParser.SubstringOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'substring' requires String",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type startType = visit(ctx.start);
    Type endType = visit(ctx.end);

    if (!startType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Start index must be Integer",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    if (!endType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "End index must be Integer",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  @Override
  public Type visitToUpperOp(VitruvOCLParser.ToUpperOpContext ctx) {
    return visitStringNoArgOp(ctx, "toUpper");
  }

  @Override
  public Type visitToLowerOp(VitruvOCLParser.ToLowerOpContext ctx) {
    return visitStringNoArgOp(ctx, "toLower");
  }

  @Override
  public Type visitIndexOfOp(VitruvOCLParser.IndexOfOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'indexOf' requires String",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Argument must be String",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  @Override
  public Type visitEqualsIgnoreCaseOp(VitruvOCLParser.EqualsIgnoreCaseOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'equalsIgnoreCase' requires String",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Argument must be String",
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /** Helper for string operations with no arguments. */
  private Type visitStringNoArgOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires String",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  // ==================== Type Operations ====================

  /**
   * Type checks the {@code oclIsKindOf()} operation.
   *
   * <p>Returns collection of Boolean values, one per element.
   *
   * @param ctx The oclIsKindOf operation node
   * @return Collection(Boolean) with same kind as receiver
   */
  @Override
  public Type visitOclIsKindOfOp(VitruvOCLParser.OclIsKindOfOpContext ctx) {
    Type receiverType = receiverStack.peek();

    Type targetType = visit(ctx.type);
    if (targetType == null && ctx.type != null) {
      targetType = resolveTypeExpression(ctx.type);
    }

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "oclIsKindOf() requires collection receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Preserve collection kind from receiver
    Type resultType = preserveCollectionKind(receiverType, Type.BOOLEAN);
    nodeTypes.put(ctx, resultType);
    nodeTypes.put(ctx.type, targetType);
    return resultType;
  }

  /** Helper to resolve type expressions for type operations. */
  private Type resolveTypeExpression(VitruvOCLParser.TypeExpCSContext ctx) {
    String text = ctx.getText();
    return switch (text) {
      case "Integer" -> Type.INTEGER;
      case "String" -> Type.STRING;
      case "Boolean" -> Type.BOOLEAN;
      default -> Type.ANY;
    };
  }

  @Override
  public Type visitOclIsTypeOfOp(VitruvOCLParser.OclIsTypeOfOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'oclIsTypeOf' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = preserveCollectionKind(receiverType, Type.BOOLEAN);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  @Override
  public Type visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'oclAsType' requires collection",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type targetType = visit(ctx.type);

    Type resultType = preserveCollectionKind(receiverType, targetType);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Metamodel Operations ====================
  /**
   * Type checks the {@code allInstances()} operation.
   *
   * <p>Requires metaclass receiver, returns Set of that metaclass type.
   *
   * <p><b>Example:</b> {@code Person.allInstances()} → Set(Person)
   *
   * @param ctx The allInstances operation node
   * @return Set(MetaclassType)
   */
  @Override
  public Type visitAllInstancesOp(VitruvOCLParser.AllInstancesOpContext ctx) {
    Type receiverType = receiverStack.peek();

    if (!receiverType.isMetaclassType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "allInstances() requires metaclass receiver",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType = Type.set(receiverType);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Not Yet Implemented ====================
  /** Placeholder for correspondence operator (~). */
  @Override
  public Type visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    Type rightType = visit(ctx.right);

    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Correspondence operator '~' not yet implemented",
        ErrorSeverity.WARNING,
        "type-checker");

    Type resultType = Type.set(rightType);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /** Placeholder for message operator (^). */
  @Override
  public Type visitMessage(VitruvOCLParser.MessageContext ctx) {
    Type leftType = visit(ctx.left);

    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Message operator '^' not yet implemented",
        ErrorSeverity.WARNING,
        "type-checker");

    nodeTypes.put(ctx, leftType);
    return leftType;
  }

  // ==================== Delegation & Miscellaneous ====================
  @Override
  public Type visitPrefixedExpr(VitruvOCLParser.PrefixedExprContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  @Override
  public Type visitLiteral(VitruvOCLParser.LiteralContext ctx) {
    return visit(ctx.literalExpCS());
  }

  @Override
  public Type visitConditional(VitruvOCLParser.ConditionalContext ctx) {
    return visit(ctx.ifExpCS());
  }

  @Override
  public Type visitLetBinding(VitruvOCLParser.LetBindingContext ctx) {
    return visit(ctx.letExpCS());
  }

  @Override
  public Type visitCollectionLiteral(VitruvOCLParser.CollectionLiteralContext ctx) {
    return visit(ctx.collectionLiteralExpCS());
  }

  @Override
  public Type visitTypeLiteral(VitruvOCLParser.TypeLiteralContext ctx) {
    return visit(ctx.typeLiteralExpCS());
  }

  @Override
  public Type visitNested(VitruvOCLParser.NestedContext ctx) {
    return visit(ctx.nestedExpCS());
  }

  @Override
  public Type visitSelf(VitruvOCLParser.SelfContext ctx) {
    return visit(ctx.selfExpCS());
  }

  @Override
  public Type visitVariable(VitruvOCLParser.VariableContext ctx) {
    return visit(ctx.variableExpCS());
  }

  @Override
  public Type visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    List<VitruvOCLParser.ExpCSContext> exps = ctx.expCS();
    if (exps.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Empty nested expression",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    Type resultType = null;
    for (VitruvOCLParser.ExpCSContext exp : exps) {
      resultType = visit(exp);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  @Override
  public Type visitTypeLiteralExpCS(VitruvOCLParser.TypeLiteralExpCSContext ctx) {
    return visit(ctx.typeLiteralCS());
  }

  @Override
  public Type visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    Type receiverType = receiverStack.peek();
    String propertyName = ctx.propertyName.getText();
    Type resultType = typeCheckPropertyAccess(receiverType, propertyName, ctx);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  @Override
  public Type visitCollectionOperation(VitruvOCLParser.CollectionOperationContext ctx) {
    return visit(ctx.collectionOpCS());
  }

  @Override
  public Type visitStringOperation(VitruvOCLParser.StringOperationContext ctx) {
    return visit(ctx.stringOpCS());
  }

  @Override
  public Type visitIteratorOperation(VitruvOCLParser.IteratorOperationContext ctx) {
    return visit(ctx.iteratorOpCS());
  }

  @Override
  public Type visitTypeOperation(VitruvOCLParser.TypeOperationContext ctx) {
    return visit(ctx.typeOpCS());
  }

  @Override
  public Type visitOnespace(VitruvOCLParser.OnespaceContext ctx) {
    return Type.ANY; // Lexical token
  }

  // ==================== Accessors ====================
  /**
   * Returns the computed type annotations for the parse tree.
   *
   * <p>This property is used by the evaluation phase to access pre-computed type information for
   *
   * <p>type-dependent operations.
   *
   * @return The parse tree property mapping nodes to types
   */
  public ParseTreeProperty<Type> getNodeTypes() {
    return nodeTypes;
  }

  /**
   * Sets the token stream for keyword-based parsing.
   *
   * <p>Required for {@link #visitIfExpCS} to determine expression partitioning.
   *
   * @param tokens The ANTLR token stream
   */
  public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
    this.tokens = tokens;
  }
}