package tools.vitruv.dsls.vitruvOCL.typechecker;
import java.util.logging.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;
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
 *   <li>"Everything is a collection" - singletons are {@code Collection(T,1,1)}
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
 * @see Type The type system implementation
 * @see TypeResolver Helper class for binary operation type resolution
 * @see EvaluationVisitor Phase 3 visitor that uses the type information
 */
public class TypeCheckVisitor extends AbstractPhaseVisitor<Type> {

  private static final Logger LOG = Logger.getLogger(TypeCheckVisitor.class.getName());

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
   * @param st The symbol table containing variable and type definitions from Phase 1
   * @param wrapper The metamodel wrapper providing access to ECore metamodel information
   * @param errors The error collector for reporting type errors
   * @param scopeAnnotator The scope annotator containing scope annotations from Phase 1
   */
  public TypeCheckVisitor(
      SymbolTable st,
      MetamodelWrapperInterface wrapper,
      ErrorCollector errors,
      ScopeAnnotator scopeAnnotator) {
    super(st, wrapper, errors);
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
   */
  /* OCL keywords that look like variable names and are commonly mistyped. */
  private static final java.util.List<String> KEYWORD_LIKE_NAMES =
      java.util.List.of("self", "true", "false");

  // ==================== String Constants ====================

  private static final String PHASE_TAG = "type-checker";
  private static final String ERR_NO_SCOPE = "Internal error: No scope annotation from Pass 1";
  private static final String ERR_TYPE_MISMATCH_PREFIX = "Type mismatch: cannot apply '";
  private static final String ERR_TO = "' to ";
  private static final String ERR_DID_YOU_MEAN = "' — did you mean '";
  private static final String AND_SEPARATOR = " and ";
  private static final String ERR_ORDERING_TYPES = "Ordering comparison requires numeric or String types, got: ";
  private static final String ERR_BRANCH_TYPES = "Incompatible branch types: ";
  private static final String ERR_UNKNOWN_OP = "Unknown operation '";
  private static final String OP_SELECT = "select";
  private static final String OP_REJECT = "reject";
  private static final String OP_EXISTS = "exists";
  private static final String OP_FLOOR = "floor";
  private static final String OP_ROUND = "round";
  private static final String OP_CEILING = "ceiling";
  private static final String OP_AS_SET = "asSet";
  private static final String OP_AS_BAG = "asBag";
  private static final String OP_AS_SEQUENCE = "asSequence";
  private static final String OP_AS_ORDERED_SET = "asOrderedSet";
  private static final String TYPE_BOOLEAN = "Boolean";
  private static final String TYPE_INTEGER = "Integer";
  private static final String TYPE_STRING = "String";
  private static final String ERR_OP_DOES_NOT_EXIST = "' — this operation does not exist";

  private static int editThreshold(int len) {
    if (len <= 3) return 1;
    if (len <= 6) return 2;
    return 3;
  }

  protected void handleUndefinedSymbol(String name, ParserRuleContext ctx) {
    String lower = name.toLowerCase(java.util.Locale.ROOT);

    // Check against keyword-like names first (self, null, true, false)
    String bestKeyword = KEYWORD_LIKE_NAMES.stream()
        .min(java.util.Comparator.comparingInt(k ->
            levenshtein(lower, k.toLowerCase(java.util.Locale.ROOT))))
        .orElse(null);
    int kwDist = bestKeyword == null ? Integer.MAX_VALUE
        : levenshtein(lower, bestKeyword.toLowerCase(java.util.Locale.ROOT));

    int threshold = editThreshold(name.length());
    String message;
    String suggestion;
    if (kwDist <= threshold) {
      message    = "Undefined variable '" + name + ERR_DID_YOU_MEAN + bestKeyword + "'?";
      suggestion = bestKeyword;
    } else {
      message    = "Undefined variable: " + name;
      suggestion = null;
    }

    org.antlr.v4.runtime.Token tok = ctx.getStart();
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        message, ErrorSeverity.ERROR, PHASE_TAG, null, suggestion));
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
   * <p>Enters the scope created by Pass 1 (SymbolTableBuilder) which already contains the 'self'
   * variable.
   *
   * @param ctx The classifier context node
   * @return The context type (metaclass type)
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
          PHASE_TAG);
      return Type.ERROR;
    }

    if (contextType == null) {
      return Type.ERROR; // Pass 1 already reported a precise error for this token
    }

    nodeTypes.put(ctx, contextType);

    // Enter scope created by Pass 1 - scope already contains 'self' variable
    Scope contextScope = scopeAnnotator.getScope(ctx);
    if (contextScope == null) {
      return Type.ERROR; // Pass 1 failed for this context — bail out silently
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
  @SuppressWarnings("java:S3776")
  public Type visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    // Check for duplicate @severity / @message annotations
    long severityCount =
        ctx.annotationCS().stream()
            .filter(a -> a instanceof VitruvOCLParser.SeverityAnnotationContext)
            .count();
    long messageCount =
        ctx.annotationCS().stream()
            .filter(a -> a instanceof VitruvOCLParser.MessageAnnotationContext)
            .count();
    if (severityCount > 1) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "@severity may only appear once per constraint",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    if (messageCount > 1) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "@message may only appear once per constraint",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    // Validate annotations before type-checking the body
    for (VitruvOCLParser.AnnotationCSContext ann : ctx.annotationCS()) {
      visit(ann);
    }

    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Type resultType = Type.BOOLEAN;

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      int errorsBefore = errors.getErrors().size();
      Type specType = visit(spec);
      boolean newErrorsInSpec = errors.getErrors().size() > errorsBefore;

      // If the spec already has errors, an error node, or produced new errors, skip the
      // Boolean-conformance check — it would only produce misleading follow-up diagnostics.
      if (specType.equals(Type.ERROR) || hasErrorNode(spec) || newErrorsInSpec) continue;

      // Check whether the spec was cut short by a missing operand: if the token immediately
      // after spec.getStop() is a comparison/arithmetic operator, the right-hand side of an
      // expression was never parsed — report on that operator instead of cascading to inv.
      if (reportMissingOperandAfter(spec)) { resultType = Type.ERROR; continue; }

      // Check for a forgotten logical operator between two expressions
      // e.g.  self.a == "x"  persons::Foo.allInstances()...
      if (reportMissingOperatorBetweenExpressions(spec)) { resultType = Type.ERROR; continue; }

      // Check Boolean conformance (handles both Boolean and !Boolean!)
      Type checkType =
          specType.isSingleton() || specType.isCollection() ? specType.getElementType() : specType;
      if (!checkType.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Invariant must be Boolean, got " + specType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
        resultType = Type.ERROR;
      }
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Annotations ====================

  private static final java.util.Set<String> VALID_SEVERITIES =
      java.util.Set.of("CRITICAL", "WARNING", "MAJOR", "MINOR", "INFO");

  @Override
  public Type visitSeverityAnnotation(VitruvOCLParser.SeverityAnnotationContext ctx) {
    String val = ctx.severityValue.getText();
    if (!VALID_SEVERITIES.contains(val)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unknown severity level '" + val + "'. Valid values: CRITICAL, WARNING, MAJOR, MINOR, INFO",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    return Type.BOOLEAN;
  }

  @Override
  public Type visitMessageAnnotation(VitruvOCLParser.MessageAnnotationContext ctx) {
    return Type.BOOLEAN;
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
        PHASE_TAG);
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
        PHASE_TAG);
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
                PHASE_TAG);
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
          case TYPE_BOOLEAN -> Type.BOOLEAN;
          case TYPE_INTEGER -> Type.INTEGER;
          case "Real" -> Type.DOUBLE;
          case TYPE_STRING -> Type.STRING;
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
          PHASE_TAG);
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
          PHASE_TAG);
      return Type.ERROR;
    }

    // Check primitives first
    Type primitiveType =
        switch (typeName) {
          case TYPE_INTEGER -> Type.INTEGER;
          case TYPE_STRING -> Type.STRING;
          case TYPE_BOOLEAN -> Type.BOOLEAN;
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
          PHASE_TAG);
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
          PHASE_TAG);
      return Type.ERROR;
    }

    // Multiple expCS children means expressions were written without an operator between them.
    // e.g.  self.a == "x"  persons::Person.allInstances().exists(...)
    // Report on the second (unexpected) expression's first token.
    if (ctx.expCS().size() > 1) {
      for (int _i = 1; _i < ctx.expCS().size(); _i++) {
        org.antlr.v4.runtime.Token bad = ctx.expCS().get(_i).getStart();
        errors.add(new CompileError(
            bad.getLine(), bad.getCharPositionInLine(),
            bad.getLine(), bad.getCharPositionInLine() + bad.getText().length(),
            "Missing logical operator before this expression — did you mean 'and', 'or', or 'implies'?",
            ErrorSeverity.ERROR, PHASE_TAG, null));
      }
      return Type.ERROR;
    }

    Type resultType = null;
    boolean hasError = false;
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      Type t = visit(exp);
      if (t != null && t.equals(Type.ERROR)) hasError = true;
      resultType = t;
    }

    if (hasError) {
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
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
   * Type checks equality comparison (==).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code 5 == 10} → Boolean
   *
   * @param ctx The equality comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  /* Reports a type error spanning the full binary expression [left..right]. */
  private void addBinaryTypeError(
      org.antlr.v4.runtime.ParserRuleContext left,
      org.antlr.v4.runtime.ParserRuleContext right,
      String message) {
    org.antlr.v4.runtime.Token s = left.getStart();
    org.antlr.v4.runtime.Token e = right.getStop() != null ? right.getStop() : left.getStop();
    int endLine = e != null ? e.getLine() : s.getLine();
    int endCol  = e != null ? e.getCharPositionInLine() + e.getText().length()
                            : s.getCharPositionInLine() + s.getText().length();
    errors.add(new CompileError(s.getLine(), s.getCharPositionInLine(),
        endLine, endCol, message, ErrorSeverity.ERROR, PHASE_TAG, null));
  }

  /**
   * Checks that both operands of a binary comparison are present and parse-error free.
   * If not, reports the error on the operator token itself (e.g. the {@code ==} sign)
   * and returns {@code false} so the caller can bail out early with {@code Type.ERROR}.
   */
  private boolean checkBinaryOperands(
      org.antlr.v4.runtime.ParserRuleContext left,
      org.antlr.v4.runtime.Token op,
      org.antlr.v4.runtime.ParserRuleContext right) {
    boolean leftBad  = left  == null || hasErrorNode(left);
    boolean rightBad = right == null || hasErrorNode(right);
    if (!leftBad && !rightBad) return true; // both OK
    String side;
    if (leftBad && rightBad) side = "both operands are";
    else if (leftBad) side = "left-hand operand is";
    else side = "right-hand operand is";
    errors.add(new CompileError(
        op.getLine(), op.getCharPositionInLine(),
        op.getLine(), op.getCharPositionInLine() + op.getText().length(),
        "Missing or invalid operand for '" + op.getText() + "' — " + side + " missing or invalid",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return false;
  }

  @Override
  public Type visitEqualityComparison(VitruvOCLParser.EqualityComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;

    // Check if types are comparable
    if (!areComparable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          "Cannot compare incompatible types: " + leftType + AND_SEPARATOR + rightType);
      resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks inequality comparison (!=).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code "hello" != "world"} → Boolean
   *
   * @param ctx The inequality comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitInequalityComparison(VitruvOCLParser.InequalityComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;

    // Check if types are comparable
    if (!areComparable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          "Cannot compare incompatible types: " + leftType + AND_SEPARATOR + rightType);
      resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Reports an unsupported two-character operator sequence.
   *
   * <p>Users sometimes write operators from other OCL dialects or from general programming
   * languages that VitruvOCL does not support as a single operator:
   *
   * <ul>
   *   <li>{@code <>} — OCL standard inequality; use {@code !=} instead
   *   <li>{@code ><} — invalid; use {@code !=}, {@code >}, or {@code <}
   *   <li>{@code +-} — invalid; use {@code +} or {@code -} separately
   *   <li>{@code -+} — invalid; use {@code +} or {@code -} separately
   * </ul>
   *
   * <p>The operands are still visited so that further errors in them are reported, but the result
   * type is {@code ERROR} so nothing cascades to the surrounding expression.
   *
   * @param ctx the invalid binary operator node
   * @return {@code Type.ERROR}
   */
  @Override
  public Type visitInvalidBinaryOp(VitruvOCLParser.InvalidBinaryOpContext ctx) {
    // Visit operands so their own errors are reported
    visit(ctx.left);
    visit(ctx.right);

    String op = ctx.op.getText();
    String hint = switch (op) {
      case "<>" -> " — did you mean `!=`? (OCL standard `<>` is not supported)";
      case "><" -> " — did you mean `!=` or a comparison?";
      case "+-" -> " — use `+` or `-` as separate operators";
      case "-+" -> " — use `-` or `+` as separate operators";
      default   -> "";
    };

    org.antlr.v4.runtime.Token tok = ctx.op;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        "Invalid operator `" + op + "`" + hint,
        ErrorSeverity.ERROR, PHASE_TAG, null));

    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  @Override
  public Type visitSingleEqualsOp(VitruvOCLParser.SingleEqualsOpContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    // Point at the '=' sign: it sits right after the left operand's last token.
    // We approximate its position from the token stream.
    org.antlr.v4.runtime.Token eq = ctx.getStart(); // fallback
    // Walk children to find the '=' terminal
    for (int i = 0; i < ctx.getChildCount(); i++) {
      org.antlr.v4.runtime.tree.ParseTree child = ctx.getChild(i);
      if (child instanceof org.antlr.v4.runtime.tree.TerminalNode) {
        org.antlr.v4.runtime.Token t = ((org.antlr.v4.runtime.tree.TerminalNode) child).getSymbol();
        if ("=".equals(t.getText())) { eq = t; break; }
      }
    }
    errors.add(new CompileError(
        eq.getLine(), eq.getCharPositionInLine(),
        eq.getLine(), eq.getCharPositionInLine() + 1,
        "Use '==' for equality comparison, not '='",
        ErrorSeverity.ERROR, PHASE_TAG, null, "=="));
    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  @Override
  public Type visitMultiEqualsOp(VitruvOCLParser.MultiEqualsOpContext ctx) {
    visit(ctx.left);
    visit(ctx.right);
    errors.add(new CompileError(
        ctx.op.getLine(), ctx.op.getCharPositionInLine(),
        ctx.op.getLine(), ctx.op.getCharPositionInLine() + ctx.op.getText().length(),
        "Invalid operator '" + ctx.op.getText() + "' — did you mean '=='?",
        ErrorSeverity.ERROR, PHASE_TAG, null, "=="));
    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  /**
   * Checks that {@code ctx.body} is non-null (i.e., the user wrote the {@code | body} part).
   *
   * <p>When someone writes e.g. {@code exists(A)} instead of {@code exists(A | A.active)}, ANTLR
   * error-recovery produces a tree where {@code ctx.body == null}. Without this guard the visitor
   * would NPE or silently cascade a bogus type error to the surrounding {@code implies}.
   *
   * @param ctx  the iterator operation context
   * @param opName operation name for the message (e.g. {@code "exists"})
   * @return {@code true} if the body is present and valid; {@code false} when an error was reported
   */
  private boolean requireBody(ParserRuleContext ctx,
                              org.antlr.v4.runtime.ParserRuleContext body,
                              String opName) {
    // A missing-body situation produces a non-null but empty/invalid context:
    // ANTLR error recovery leaves ctx.body with 0 children (empty expCS),
    // or with an exception, or with error nodes.
    boolean invalid = body == null
        || body.exception != null
        || hasErrorNode(body)
        || body.getChildCount() == 0;
    if (invalid) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires 'variable | expression' — "
              + "did you forget the '| body' part? "
              + "Example: " + opName + "(x | x.active)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return false;
    }
    return true;
  }

  private boolean areOrderable(Type t1, Type t2) {
    // Unwrap ¡T! and ¿T? for ordering checks
    Type e1 = (t1.isSingleton() || t1.isOptional()) ? t1.getElementType() : t1;
    Type e2 = (t2.isSingleton() || t2.isOptional()) ? t2.getElementType() : t2;
    if (e1 == Type.ERROR || e2 == Type.ERROR) return true;
    if (e1 == Type.ANY || e2 == Type.ANY) return true;
    return TypeResolver.isNumeric(e1) && TypeResolver.isNumeric(e2);
  }

  /**
   * Type checks less-than comparison (&lt;).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code 5 &lt; 10} → Boolean
   *
   * @param ctx The less-than comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitLessThanComparison(VitruvOCLParser.LessThanComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;
    if (!areOrderable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          ERR_ORDERING_TYPES
              + leftType + AND_SEPARATOR + rightType);
      resultType = Type.ERROR;
    }
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks less-than-or-equal comparison (&lt;=).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code 5 &lt;= 10} → Boolean
   *
   * @param ctx The less-than-or-equal comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitLessThanOrEqualComparison(VitruvOCLParser.LessThanOrEqualComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;
    if (!areOrderable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          ERR_ORDERING_TYPES
              + leftType + AND_SEPARATOR + rightType);
      resultType = Type.ERROR;
    }
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks greater-than comparison (>).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code 10 > 5} → Boolean
   *
   * @param ctx The greater-than comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitGreaterThanComparison(VitruvOCLParser.GreaterThanComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;
    if (!areOrderable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          ERR_ORDERING_TYPES
              + leftType + AND_SEPARATOR + rightType);
      resultType = Type.ERROR;
    }
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks greater-than-or-equal comparison (>=).
   *
   * <p>Validates that operands are comparable (same type or one conforms to the other).
   *
   * <p><b>Example:</b> {@code 10 >= 5} → Boolean
   *
   * @param ctx The greater-than-or-equal comparison operation node
   * @return Type.BOOLEAN if operands are comparable, Type.ERROR otherwise
   */
  @Override
  public Type visitGreaterThanOrEqualComparison(
      VitruvOCLParser.GreaterThanOrEqualComparisonContext ctx) {
    if (!checkBinaryOperands(ctx.left, ctx.op, ctx.right)) return Type.ERROR;
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);
    Type resultType = Type.BOOLEAN;
    if (!areOrderable(leftType, rightType)) {
      addBinaryTypeError(ctx.left, ctx.right,
          ERR_ORDERING_TYPES
              + leftType + AND_SEPARATOR + rightType);
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
  @SuppressWarnings("java:S3776")
  private boolean areComparable(Type t1, Type t2) {
    if (t1 == Type.ERROR || t2 == Type.ERROR) return true;
    if (t1.equals(t2)) return true;
    if (t1.isConformantTo(t2) || t2.isConformantTo(t1)) return true;

    // Optional ANY (?Any?) is comparable to anything — represents null
    if ((t1.isOptional() && t1.getElementType() == Type.ANY)
        || (t2.isOptional() && t2.getElementType() == Type.ANY)) {
      return true;
    }

    // Unwrap singletons and optionals for comparison (¡T! and ¿T? both unwrap to T)
    Type e1 = (t1.isSingleton() || t1.isOptional() || t1.isCollection()) ? t1.getElementType() : t1;
    Type e2 = (t2.isSingleton() || t2.isOptional() || t2.isCollection()) ? t2.getElementType() : t2;
    if (!e1.equals(t1) || !e2.equals(t2)) {
      return areComparable(e1, e2);
    }

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

    if (resultType == Type.ERROR && leftType != Type.ERROR && rightType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_TYPE_MISMATCH_PREFIX + operator + ERR_TO + leftType + AND_SEPARATOR + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
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

    if (resultType == Type.ERROR && leftType != Type.ERROR && rightType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_TYPE_MISMATCH_PREFIX + operator + ERR_TO + leftType + AND_SEPARATOR + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Logical Operations ====================

  /**
   * Type checks logical AND operation.
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to validate Boolean operands and return Boolean
   * result.
   *
   * <p><b>Example:</b> {@code true and false} → Boolean
   *
   * @param ctx The logical AND operation node
   * @return Type.BOOLEAN if operands are Boolean, Type.ERROR otherwise
   */
  @Override
  public Type visitLogicalAnd(VitruvOCLParser.LogicalAndContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = "and";
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR && leftType != Type.ERROR && rightType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_TYPE_MISMATCH_PREFIX + operator + ERR_TO + leftType + AND_SEPARATOR + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks logical OR operation.
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to validate Boolean operands and return Boolean
   * result.
   *
   * <p><b>Example:</b> {@code true or false} → Boolean
   *
   * @param ctx The logical OR operation node
   * @return Type.BOOLEAN if operands are Boolean, Type.ERROR otherwise
   */
  @Override
  public Type visitLogicalOr(VitruvOCLParser.LogicalOrContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = "or";
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR && leftType != Type.ERROR && rightType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_TYPE_MISMATCH_PREFIX + operator + ERR_TO + leftType + AND_SEPARATOR + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks logical XOR operation.
   *
   * <p>Uses {@link TypeResolver#resolveBinaryOp} to validate Boolean operands and return Boolean
   * result.
   *
   * <p><b>Example:</b> {@code true xor true} → Boolean
   *
   * @param ctx The logical XOR operation node
   * @return Type.BOOLEAN if operands are Boolean, Type.ERROR otherwise
   */
  @Override
  public Type visitLogicalXor(VitruvOCLParser.LogicalXorContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = "xor";
    Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

    if (resultType == Type.ERROR && leftType != Type.ERROR && rightType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_TYPE_MISMATCH_PREFIX + operator + ERR_TO + leftType + AND_SEPARATOR + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
    int errsBefore = errors.getErrors().size();
    Type leftType = visit(ctx.left);
    int errsAfterLeft = errors.getErrors().size();
    Type rightType = visit(ctx.right);
    int errsAfterRight = errors.getErrors().size();

    boolean leftFailed  = leftType  == Type.ERROR || hasErrorNode(ctx.left)
                          || errsAfterLeft  > errsBefore;
    boolean rightFailed = rightType == Type.ERROR || hasErrorNode(ctx.right)
                          || errsAfterRight > errsAfterLeft;

    // Swallow cascade when the root cause was already reported in either operand.
    if (leftFailed || rightFailed) {
      nodeTypes.put(ctx, Type.BOOLEAN);
      return Type.BOOLEAN;
    }

    if (!leftType.isConformantTo(Type.BOOLEAN)) {
      org.antlr.v4.runtime.Token ls = ctx.left.getStart();
      errors.add(new CompileError(
          ls.getLine(), ls.getCharPositionInLine(),
          ls.getLine(), ls.getCharPositionInLine() + ls.getText().length(),
          "Left operand of 'implies' must be Boolean, got " + leftType,
          ErrorSeverity.ERROR, PHASE_TAG, null));
    }

    if (!rightType.isConformantTo(Type.BOOLEAN)) {
      // Try to find the unparsed navigation step right after ctx.right that caused the issue
      // (e.g. .reject(~, , , ...) that ANTLR couldn't parse due to extra commas).
      int[] badCallSpan = findUnparsedCorrCallAfter(ctx.right);
      if (badCallSpan.length > 0) {
        org.antlr.v4.runtime.Token opTok  = tokens.get(badCallSpan[0]);
        org.antlr.v4.runtime.Token endTok = tokens.get(badCallSpan[1]);
        errors.add(new CompileError(
            opTok.getLine(), opTok.getCharPositionInLine(),
            endTok.getLine(), endTok.getCharPositionInLine() + endTok.getText().length(),
            "'" + opTok.getText() + "(~, ...)' has invalid arguments — check for syntax errors inside the parentheses",
            ErrorSeverity.ERROR, PHASE_TAG, null));
      } else {
        org.antlr.v4.runtime.Token rs = ctx.right.getStart();
        errors.add(new CompileError(
            rs.getLine(), rs.getCharPositionInLine(),
            rs.getLine(), rs.getCharPositionInLine() + rs.getText().length(),
            "Right operand of 'implies' must be Boolean, got " + rightType,
            ErrorSeverity.ERROR, PHASE_TAG, null));
      }
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  // ==================== Unary Operations & Navigation ====================

  /**
   * Visits a primary expression with navigation (`primaryWithNav`) in the AST.
   *
   * <p>This method performs type checking for a primary expression that may be followed by a chain
   * of navigation operations (e.g., property or association accesses).
   *
   * @param ctx the context of the primary expression with navigation in the AST
   * @return the resulting type of the expression after applying the navigation chain; {@link
   *     Type#ERROR} if the base expression has no type
   */
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
          PHASE_TAG);
      return Type.ERROR;
    }

    // Process navigation chain using receiverStack
    List<VitruvOCLParser.NavigationChainCSContext> navChain = ctx.navigationChainCS();
    receiverStack.push(currentType);
    for (VitruvOCLParser.NavigationChainCSContext nav : navChain) {
      LOG.fine("[DBG] nav target: " + nav.navigationTargetCS().getClass().getSimpleName()
          + " text='" + nav.getText() + "'");
      Type resultType = visit(nav);
      LOG.fine("[DBG] nav result: " + resultType);
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

  /**
   * Visits a unary minus ({@code -}) node in the AST.
   *
   * <p>Accepts any numeric base type: {@link Type#INTEGER}, {@link Type#FLOAT}, or {@link
   * Type#DOUBLE}. The result type equals the operand type (i.e., {@code -3.14f} stays FLOAT).
   *
   * <p>Supports singleton and collection receivers via one level of unwrapping.
   *
   * @param ctx the context of the unary minus node in the AST
   * @return the type of the operand if valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitUnaryMinus(VitruvOCLParser.UnaryMinusContext ctx) {
    Type operandType = visit(ctx.operand);

    // Only unwrap singletons ¡T!, not collections
    Type baseType = operandType.isSingleton() ? operandType.getElementType() : operandType;

    if (baseType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unary minus requires numeric type, got " + operandType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (!TypeResolver.isNumeric(baseType) && baseType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unary minus requires numeric type, got " + operandType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, operandType);
    return operandType;
  }

  /**
   * Visits a logical NOT (`not`) node in the AST.
   *
   * <p>This method performs type checking for the operand of a logical NOT expression. It supports
   * both scalar and collection types (one level of unwrapping):
   *
   * <ul>
   *   <li>If the operand is a singleton or a collection, the base element type is extracted.
   *   <li>Allowed base type is {@link Type#BOOLEAN}.
   *   <li>If the type is invalid, an error is added to the error list and {@link Type#ERROR} is
   *       returned.
   * </ul>
   *
   * The type of the operand is stored in the {@code nodeTypes} map.
   *
   * @param ctx the context of the logical NOT node in the AST
   * @return the type of the operand if valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitLogicalNot(VitruvOCLParser.LogicalNotContext ctx) {
    Type operandType = visit(ctx.operand);

    // Only unwrap singletons ¡T!, not collections
    Type baseType = operandType.isSingleton() ? operandType.getElementType() : operandType;

    if (baseType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Logical not requires Boolean type, got " + operandType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (baseType != Type.BOOLEAN && baseType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Logical not requires Boolean type, got " + operandType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
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

    // First try: EClass (metamodel::ClassName)
    EClass eClass = specification.resolveEClass(metamodel, className);
    if (eClass != null) {
      Type metaclassType = Type.metaclassType(eClass);
      nodeTypes.put(ctx, metaclassType);
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

    // Second try: Enum literal (EnumName::LiteralName)
    org.eclipse.emf.ecore.EEnum eEnum = specification.resolveEEnum(metamodel);
    if (eEnum != null) {
      org.eclipse.emf.ecore.EEnumLiteral literal = eEnum.getEEnumLiteral(className);
      if (literal != null) {
        Type enumType = Type.enumType(eEnum);
        nodeTypes.put(ctx, enumType);
        return enumType;
      }
    }

    if (!specification.getAvailableMetamodels().contains(metamodel)) {
      errors.add(ctx.metamodel, "Unknown metamodel '" + metamodel + "'",
          ErrorSeverity.ERROR, PHASE_TAG);
    } else {
      errors.add(ctx.className, "Unknown class '" + className
          + "' in metamodel '" + metamodel + "'",
          ErrorSeverity.ERROR, PHASE_TAG);
    }
    return Type.ERROR;
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
   * <p>Unwraps singleton {@code !T!} receivers before property access. After the RC2
   * iterator-variable fix all iterator vars are {@code ¡T!}, so this is the normal path for
   * navigating from iterator variables.
   *
   * @param ctx The property navigation node
   * @return The property type
   */
  @Override
  public Type visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    // Track whether the receiver was optional so we can propagate ?T? through navigation
    boolean receiverWasOptional = receiverType.isOptional();

    // Unwrap singleton !T! or optional ?T? to T for property access
    if (receiverType.isSingleton() || receiverType.isOptional()) {
      receiverType = receiverType.getElementType();
    }

    String propertyName = ctx.propertyAccess().propertyName.getText();
    Type resultType = typeCheckPropertyAccess(
        receiverType, propertyName, ctx.propertyAccess().propertyName);

    // Propagate optionality: ?T?.prop where prop : !R! → result is ?R?
    // Collections keep their type (absent optional → treated as empty collection)
    if (receiverWasOptional && resultType != Type.ERROR && resultType.isSingleton()) {
      resultType = Type.optional(resultType.getElementType());
    }

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
    LOG.fine("[DBG] visitOperationNav: text='" + ctx.getText()
        + "' opCall class=" + ctx.operationCall().getClass().getSimpleName()
        + " children=" + ctx.operationCall().getChildCount());
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
  private Type typeCheckPropertyAccess(
      Type receiverType, String propName, Token propertyNameToken) {
    // Implicit collect for collections
    if (receiverType.isCollection()) {
      Type elemType = receiverType.getElementType();

      if (!elemType.isMetaclassType()) {
        errors.add(
            propertyNameToken,
            "Cannot navigate on non-object type",
            ErrorSeverity.ERROR,
            PHASE_TAG);
        return Type.ERROR;
      }

      EClass eClass = elemType.getEClass();
      EStructuralFeature feature = eClass.getEStructuralFeature(propName);

      if (feature == null) {
        feature = findFeatureInSubtypes(eClass, propName);
        if (feature == null) {
          errors.add(propertyNameToken, "Unknown property '" + propName
              + "' on type '" + eClass.getName() + "'"
              + " (not found on declared type or any known subtype;"
              + " use oclIsKindOf + oclAsType to cast to the concrete subtype first)",
              ErrorSeverity.ERROR, PHASE_TAG);
          return Type.ERROR;
        }
      }

      Type featureType = mapFeatureToType(feature);
      return Type.set(featureType.getElementType()); // Flatten
    }

    // Singleton property access
    if (receiverType.isMetaclassType()) {
      EClass eClass = receiverType.getEClass();
      EStructuralFeature feature = eClass.getEStructuralFeature(propName);

      if (feature == null) {
        feature = findFeatureInSubtypes(eClass, propName);
        if (feature == null) {
          errors.add(propertyNameToken, "Unknown property '" + propName
              + "' on type '" + eClass.getName() + "'"
              + " (not found on declared type or any known subtype;"
              + " use oclIsKindOf + oclAsType to cast to the concrete subtype first)",
              ErrorSeverity.ERROR, PHASE_TAG);
          return Type.ERROR;
        }
      }

      return mapFeatureToType(feature);
    }

    errors.add(propertyNameToken, "Cannot access property on " + receiverType,
        ErrorSeverity.ERROR, PHASE_TAG);
    return Type.ERROR;
  }

  /**
   * Searches all known subclasses of {@code abstractClass} (reachable via EMF EPackage hierarchy)
   * for a structural feature named {@code propName}. Returns the feature if found unambiguously on
   * one or more subtypes (all with the same defining EClass), or {@code null} if not found or
   * ambiguous across incompatible types.
   */
  private EStructuralFeature findFeatureInSubtypes(EClass abstractClass, String propName) {
    // Collect all EPackages reachable from the root package
    EPackage rootPkg = abstractClass.getEPackage();
    while (rootPkg.getESuperPackage() != null) {
      rootPkg = rootPkg.getESuperPackage();
    }
    List<EClass> allClasses = new ArrayList<>();
    collectEClasses(rootPkg, allClasses, new HashSet<>());

    // Find feature on any subtype (abstractClass.isSuperTypeOf(candidate) is true for subtypes)
    EStructuralFeature found = null;
    for (EClass candidate : allClasses) {
      if (candidate == abstractClass || !abstractClass.isSuperTypeOf(candidate)) continue;
      EStructuralFeature f = candidate.getEStructuralFeature(propName);
      if (f == null) continue;
      if (found == null) {
        found = f;
      } else if (found.getEType() != f.getEType() || found.isMany() != f.isMany()) {
        // Ambiguous: same feature name but different types on different subtypes — reject
        return null;
      }
    }
    return found;
  }

  private void collectEClasses(EPackage pkg, List<EClass> result, Set<EPackage> visited) {
    if (!visited.add(pkg)) return;
    for (EClassifier c : pkg.getEClassifiers()) {
      if (c instanceof EClass eClass) result.add(eClass);
    }
    for (EPackage sub : pkg.getESubpackages()) {
      collectEClasses(sub, result, visited);
    }
  }

  /**
   * Maps an EMF structural feature to a VitruvOCL type.
   *
   * <p>Respects EMF ordering and uniqueness annotations:
   *
   * <ul>
   *   <li>ordered + unique → OrderedSet {@code <T>}
   *   <li>ordered + !unique → Sequence {@code [T]}
   *   <li>!ordered + unique → Set {@code {T}}
   *   <li>!ordered + !unique → Bag {@code {{T}}}
   *   <li>single-valued → Singleton {@code !T!}
   * </ul>
   *
   * @param feature The EMF structural feature
   * @return The corresponding VitruvOCL type
   */
  @SuppressWarnings("java:S125")
  private Type mapFeatureToType(EStructuralFeature feature) {
    Type baseType = mapEClassifierToType(feature.getEType());

    if (feature.getUpperBound() > 1 || feature.getUpperBound() == -1) {
      // Multi-valued: respect EMF ordering and uniqueness annotations
      boolean ordered = feature.isOrdered();
      boolean unique = feature.isUnique();

      if (ordered && unique) {
        return Type.orderedSet(baseType); // <T>
      } else if (ordered) {
        return Type.sequence(baseType); // [T]
      } else if (unique) {
        return Type.set(baseType); // {T}
      } else {
        return Type.bag(baseType); // {{T}}
      }
    } else {
      // EAttribute with lowerBound=0: primitive always has a default value → ¡T!
      // EReference with lowerBound=0: reference may be absent → ¿T?
      // Any single-valued with lowerBound>=1: always present → ¡T!
      if (feature instanceof org.eclipse.emf.ecore.EReference && feature.getLowerBound() == 0) {
        return Type.optional(baseType);
      } else {
        return Type.singleton(baseType);
      }
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
      case "EFloat":
        return Type.FLOAT;
      case "EDouble":
        return Type.DOUBLE;
      default:
        if (EcorePackage.Literals.ECLASS.equals(classifier.eClass())) {
          return Type.metaclassType((EClass) classifier);
        }
        // Handle EEnum — enum literals are compared by value in OCL
        if (classifier instanceof org.eclipse.emf.ecore.EEnum eEnum) {
          return Type.enumType(eEnum);
        }
        // Handle custom EDataType (e.g. autosar::Identifier with instanceClassName="java.lang.String")
        if (classifier instanceof org.eclipse.emf.ecore.EDataType eDataType) {
          String instanceClass = eDataType.getInstanceClassName();
          if (instanceClass != null) {
            return switch (instanceClass) {
              case "java.lang.String", TYPE_STRING -> Type.STRING;
              case "int", "java.lang.Integer" -> Type.INTEGER;
              case TYPE_BOOLEAN, "java.lang.Boolean" -> Type.BOOLEAN;
              case "float", "java.lang.Float" -> Type.FLOAT;
              case "double", "java.lang.Double" -> Type.DOUBLE;
              default -> Type.STRING; // fallback: treat unknown scalar datatypes as String
            };
          }
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
  @SuppressWarnings("java:S3776")
  public Type visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();

    int thenIndex = -1;
    int elseIndex = -1;
    for (int i = 0; i < ctx.getChildCount(); i++) {
      String text = ctx.getChild(i).getText();
      if ("then".equals(text) && thenIndex == -1) {
        thenIndex = ctx.getChild(i).getSourceInterval().a;
      }
      if ("else".equals(text) && elseIndex == -1) {
        elseIndex = ctx.getChild(i).getSourceInterval().a;
      }
    }

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
    if (condType != null && condType != Type.ERROR) {
      Type condCheck = condType.isSingleton() ? condType.getElementType() : condType;
      if (condCheck.isCollection() || !condCheck.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "If condition must be Boolean, got " + condType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }
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

    // Branches must have compatible types — mixing collection and scalar is an error
    boolean thenIsCollection = thenType.isCollection() && !thenType.isSingleton();
    boolean elseIsCollection = elseType.isCollection() && !elseType.isSingleton();
    if (thenIsCollection != elseIsCollection) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_BRANCH_TYPES + thenType + AND_SEPARATOR + elseType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Both collections must have same kind (ordered/unique)
    if (thenIsCollection && elseIsCollection
        && (thenType.isOrdered() != elseType.isOrdered()
            || thenType.isUnique() != elseType.isUnique())) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_BRANCH_TYPES + thenType + AND_SEPARATOR + elseType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Determine result type
    Type resultType;
    if (thenType.equals(elseType)) {
      resultType = thenType;
    } else if (thenType.isConformantTo(elseType)) {
      resultType = elseType;
    } else if (elseType.isConformantTo(thenType)) {
      resultType = thenType;
    } else {
      resultType = Type.commonSuperType(thenType, elseType);
      if (resultType == null || resultType == Type.ANY) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            ERR_BRANCH_TYPES + thenType + AND_SEPARATOR + elseType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
        resultType = Type.ERROR;
      }
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
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
   *   <li>Initializer type conforms to declared type (if present), using singleton-unwrapping rules
   *       via {@link #conformsWithUnwrapping}
   * </ul>
   *
   * <p>The variable is registered with the declared type (not the wrapped initType) so that
   * downstream navigation operates on the correct member type.
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
          PHASE_TAG);
      return Type.ERROR;
    }

    // Type-check initializer
    Type initType = visit(ctx.varInit);

    // Check explicit type if present
    Type declaredType = symbol.getType();
    if (ctx.varType != null) {
      Type explicitType = visit(ctx.varType);

      // use standard isConformantTo first, then singleton-unwrapping
      if (!initType.isConformantTo(explicitType)
          && !conformsWithUnwrapping(initType, explicitType)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Type mismatch: got " + initType + ", expected " + explicitType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
        return Type.ERROR;
      }

      // Always use the declared type for the variable so navigation on it uses
      // the correct (potentially more specific) type
      declaredType = explicitType;
      if (symbol.getType() == Type.ANY) {
        symbol.setType(declaredType);
      }
    } else {
      // No explicit type — refine symbol with inferred type from initializer
      if (declaredType == Type.ANY) {
        symbol.setType(initType);
        declaredType = initType;
      } else if (!initType.isConformantTo(declaredType)
          && !conformsWithUnwrapping(initType, declaredType)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Type mismatch: got " + initType + ", expected " + declaredType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
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
    String text = ctx.getText();
    Type type = text.contains(".") ? Type.DOUBLE : Type.INTEGER;
    nodeTypes.put(ctx, type);
    return type;
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
   * <p>Handles the special {@code null} keyword, which is the empty optional {@code ?Any?} = {@code
   * []} rather than a variable. All other names are resolved from the symbol table.
   *
   * @param ctx The variable expression node
   * @return The variable's type, or {@code ?Any?} for {@code null}
   */
  @Override
  public Type visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    String varName = ctx.varName.getText();

    // null is not valid in OCL# — use isEmpty() / notEmpty() instead
    if (varName.equals("null")) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'null' is not a valid expression in OCL#. Use isEmpty() / notEmpty() to test optional values.",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

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
          PHASE_TAG);
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
            PHASE_TAG);
      }
      if (!secondType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Range end must be Integer",
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }

      return Type.INTEGER;
    }

    return firstType;
  }

  // ==================== Collection Operations ====================

  /**
   * Type-checks the {@code first()} operation on a collection receiver.
   *
   * <p>Verifies that the receiver is a collection type, then registers and returns a singleton of
   * the receiver's element type as the result type.
   *
   * @param ctx the parse tree node for the {@code first()} operation
   * @return a singleton of the receiver's element type; or {@link Type#ERROR} if the receiver is
   *     not a collection type
   */
  @Override
  public Type visitFirstOp(VitruvOCLParser.FirstOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "first() requires an ordered collection (Sequence or OrderedSet), got: " + receiverType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type resultType = Type.singleton(receiverType.getElementType());
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type-checks the {@code last()} operation on a collection receiver.
   *
   * <p>Verifies that the receiver is a collection type, then registers and returns a singleton of
   * the receiver's element type as the result type.
   *
   * @param ctx the parse tree node for the {@code last()} operation
   * @return a singleton of the receiver's element type; or {@link Type#ERROR} if the receiver is
   *     not a collection type
   */
  @Override
  public Type visitLastOp(VitruvOCLParser.LastOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "last() requires an ordered collection (Sequence or OrderedSet), got: " + receiverType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "reverse() requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "isEmpty() requires a collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "notEmpty() requires a collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'including' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!argType.isConformantTo(elemType)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Incompatible argument type",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Visits an 'excluding' operation node in the AST.
   *
   * <p>This method performs type checking for the 'excluding' operation, which removes an element
   * from a collection.
   *
   * <ul>
   *   <li>The operation requires that the receiver type (top of {@code receiverStack}) is a
   *       collection.
   *   <li>If the receiver is not a collection, an error is added and {@link Type#ERROR} is
   *       returned.
   * </ul>
   *
   * The type of the receiver is stored in the {@code nodeTypes} map.
   *
   * @param ctx the context of the 'excluding' operation node in the AST
   * @return the type of the collection receiver if valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitExcludingOp(VitruvOCLParser.ExcludingOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excluding' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Visits an 'includes' operation node in the AST.
   *
   * <p>This method performs type checking for the 'includes' operation, which checks whether a
   * collection contains a given element.
   *
   * <ul>
   *   <li>The operation requires that the receiver type (top of {@code receiverStack}) is a
   *       collection.
   *   <li>If the receiver is not a collection, an error is added and {@link Type#ERROR} is
   *       returned.
   *   <li>If the type is valid, the resulting type of the operation is {@link Type#BOOLEAN}.
   * </ul>
   *
   * The resulting type is stored in the {@code nodeTypes} map.
   *
   * @param ctx the context of the 'includes' operation node in the AST
   * @return {@link Type#BOOLEAN} if the receiver is a collection; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitIncludesOp(VitruvOCLParser.IncludesOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'includes' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Visits an 'excludes' operation node in the AST.
   *
   * <p>This method performs type checking for the 'excludes' operation, which checks whether a
   * collection does not contain a given element.
   *
   * <ul>
   *   <li>The operation requires that the receiver type (top of {@code receiverStack}) is a
   *       collection.
   *   <li>If the receiver is not a collection, an error is added and {@link Type#ERROR} is
   *       returned.
   *   <li>If the type is valid, the resulting type of the operation is {@link Type#BOOLEAN}.
   * </ul>
   *
   * The resulting type is stored in the {@code nodeTypes} map.
   *
   * @param ctx the context of the 'excludes' operation node in the AST
   * @return {@link Type#BOOLEAN} if the receiver is a collection; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitExcludesOp(VitruvOCLParser.ExcludesOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excludes' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'flatten' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type innerType = receiverType.getElementType();
    if (!innerType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Expected Collection(Collection(T))",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection() || !argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'union' requires collection operands",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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

  /**
   * Visits an 'append' operation node in the AST.
   *
   * <p>This method performs type checking and result type computation for the 'append' operation,
   * which combines two collections by adding all elements of the argument collection to the
   * receiver collection.
   *
   * <p>The steps are as follows:
   *
   * <ol>
   *   <li>Retrieve the receiver type from {@code receiverStack} and the argument type by visiting
   *       {@code ctx.arg}.
   *   <li>Check that both the receiver and argument are collections; if not, an error is added and
   *       {@link Type#ERROR} is returned.
   *   <li>Compute the common element type of the two collections using {@link
   *       Type#commonSuperType}.
   *   <li>Determine the resulting collection type based on uniqueness and ordering:
   *       <ul>
   *         <li>If both are unique and any is ordered → {@link Type#orderedSet}.
   *         <li>If both are unique → {@link Type#set}.
   *         <li>If any is ordered → {@link Type#sequence}.
   *         <li>Otherwise → {@link Type#bag}.
   *       </ul>
   *   <li>The computed result type is stored in {@code nodeTypes}.
   * </ol>
   *
   * @param ctx the context of the 'append' operation node in the AST
   * @return the resulting collection type if operands are valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitAppendOp(VitruvOCLParser.AppendOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'append' requires a collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'append' argument must be a singleton, not a collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  // ==================== Aggregate Operations ====================

  /**
   * Type checks the {@code sum()} collection operation.
   *
   * <p>Requires a numeric collection receiver (elements must be INTEGER, FLOAT, or DOUBLE). The
   * result type is a singleton of the element type (preserves FLOAT for Float collections rather
   * than always returning INTEGER).
   *
   * @param ctx The sum operation node
   * @return Singleton of the element numeric type (INTEGER / FLOAT / DOUBLE)
   */
  @Override
  public Type visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'sum' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (elemType != Type.ANY && !TypeResolver.isNumeric(elemType)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'sum' requires numeric collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    // Preserve the concrete numeric element type (FLOAT stays FLOAT, not INTEGER)
    Type resultType = Type.singleton(TypeResolver.isNumeric(elemType) ? elemType : Type.INTEGER);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Visits a 'max' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitAggregateOp} to perform type checking and result type
   * computation for the 'max' aggregate operation over a collection.
   *
   * @param ctx the context of the 'max' operation node in the AST
   * @return the resulting type of the aggregate operation
   */
  @Override
  public Type visitMaxOp(VitruvOCLParser.MaxOpContext ctx) {
    return visitAggregateOp(ctx, "max");
  }

  /**
   * Visits a 'min' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitAggregateOp} to perform type checking and result type
   * computation for the 'min' aggregate operation over a collection.
   *
   * @param ctx the context of the 'min' operation node in the AST
   * @return the resulting type of the aggregate operation
   */
  @Override
  public Type visitMinOp(VitruvOCLParser.MinOpContext ctx) {
    return visitAggregateOp(ctx, "min");
  }

  /**
   * Visits an 'avg' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitAggregateOp} to perform type checking and result type
   * computation for the 'avg' aggregate operation over a collection.
   *
   * @param ctx the context of the 'avg' operation node in the AST
   * @return the resulting type of the aggregate operation
   */
  @Override
  public Type visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    return visitAggregateOp(ctx, "avg");
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

  /**
   * Visits a 'floor' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitNumericOp} to perform type checking and result type
   * computation for the 'floor' numeric operation.
   *
   * @param ctx the context of the 'floor' operation node in the AST
   * @return the resulting numeric type of the operation
   */
  @Override
  public Type visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    return visitNumericOp(ctx, OP_FLOOR);
  }

  /**
   * Visits a 'ceil' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitNumericOp} to perform type checking and result type
   * computation for the 'ceil' numeric operation.
   *
   * @param ctx the context of the 'ceil' operation node in the AST
   * @return the resulting numeric type of the operation
   */
  @Override
  public Type visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    return visitNumericOp(ctx, "ceil");
  }

  /**
   * Visits a 'round' operation node in the AST.
   *
   * <p>This method delegates to {@link #visitNumericOp} to perform type checking and result type
   * computation for the 'round' numeric operation.
   *
   * @param ctx the context of the 'round' operation node in the AST
   * @return the resulting numeric type of the operation
   */
  @Override
  public Type visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    return visitNumericOp(ctx, OP_ROUND);
  }

  /**
   * Helper for aggregate operation type checking ({@code min}, {@code max}, {@code avg}).
   *
   * <p>Accepts both multi-valued collections and singleton ¡T! receivers. A singleton is a
   * degenerate collection of one element, so min/max/avg are valid on it.
   *
   * @param ctx the operation node
   * @param opName operation name for error messages
   * @return singleton of the element type, or {@link Type#ERROR}
   */
  private Type visitAggregateOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    // Reject singletons — max/min/avg require a collection, not a scalar
    if (receiverType.isSingleton() || !receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (elemType.isSingleton()) {
      elemType = elemType.getElementType();
    }

    if (elemType != Type.ANY && !TypeResolver.isNumeric(elemType)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires numeric collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    Type resultType = Type.singleton(TypeResolver.isNumeric(elemType) ? elemType : Type.INTEGER);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type-checks the {@code length()} operation on a String receiver.
   *
   * <p>Verifies that the receiver type is conformant to {@link Type#STRING}, then registers and
   * returns {@link Type#INTEGER} as the result type.
   *
   * @param ctx the parse tree node for the {@code length()} operation
   * @return {@link Type#INTEGER}; or {@link Type#ERROR} if the receiver is not conformant to {@code
   *     STRING}
   */
  @Override
  public Type visitLengthOp(VitruvOCLParser.LengthOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'length' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Type-checks the {@code size()} operation on a collection receiver.
   *
   * <p>Verifies that the receiver is a collection type, then registers and returns {@link
   * Type#INTEGER} as the result type.
   *
   * @param ctx the parse tree node for the {@code size()} operation
   * @return {@link Type#INTEGER}; or {@link Type#ERROR} if the receiver is not a collection type
   */
  @Override
  public Type visitSizeOp(VitruvOCLParser.SizeOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "size() requires a collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Helper for numeric element operations ({@code abs}, {@code floor}, {@code ceil}, {@code
   * round}).
   *
   * <p>Accepts both collections and singletons ¡T! — a scalar Real/Float/Integer is ¡T! and numeric
   * operations on it are valid.
   *
   * @param ctx the operation node
   * @param opName operation name for error messages
   * @return the receiver's type unchanged, or {@link Type#ERROR}
   */
  private Type visitNumericOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    Type checkType = receiverType.isSingleton() ? receiverType.getElementType() : receiverType;

    // Reject collections — abs/floor/ceil/round require scalar receiver
    if (checkType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires scalar numeric receiver, not a collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (TypeResolver.isNumeric(checkType)) {
      nodeTypes.put(ctx, receiverType);
      return receiverType;
    }

    if (checkType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires numeric receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    return Type.ERROR;
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'lift' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
   * <p>Iterator variable receives type {@code ¡T!} (singleton — each element drawn from a
   * collection is a singleton value.
   *
   * @param ctx The select operation node
   * @return Same collection type as receiver
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitSelectOp(VitruvOCLParser.SelectOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'select' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
          "select requires at least one iterator variable",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // each iterated element is a singleton ¡T! of the receiver's element type
    Type elemType = receiverType.getElementType();
    Type iterVarType = normalizeToSingleton(elemType);

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(iterVarType);
        }
      }

      if (!requireBody(ctx, ctx.body, OP_SELECT)) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      Type checkType = bodyType.isCollection() ? bodyType.getElementType() : bodyType;

      if (!checkType.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "select body must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }

      // select on ¡T! may filter out the element → result is ¿T?
      Type resultType = receiverType.isSingleton() ? Type.optional(elemType) : receiverType;
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code reject()} iterator.
   *
   * <p>Iterator variable receives type {@code ¡T!} (singleton)
   *
   * @param ctx The reject operation node
   * @return Same collection type as receiver
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitRejectOp(VitruvOCLParser.RejectOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'reject' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
          "reject requires at least one iterator variable",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // each iterated element is a singleton ¡T! of the receiver's element type
    Type elemType = receiverType.getElementType();
    Type iterVarType = normalizeToSingleton(elemType);

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(iterVarType);
        }
      }

      if (!requireBody(ctx, ctx.body, OP_REJECT)) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "reject predicate must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
        return Type.ERROR;
      }

      // reject on ¡T! may filter out the element → result is ¿T?
      Type resultType = receiverType.isSingleton() ? Type.optional(elemType) : receiverType;
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code collect()} iterator.
   *
   * <p>Iterator variable receives type {@code ¡T!} (singleton)
   *
   * @param ctx The collect operation node
   * @return Collection of the body expression type
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitCollectOp(VitruvOCLParser.CollectOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'collect' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
          PHASE_TAG);
      return Type.ERROR;
    }

    // each iterated element is a singleton ¡T! of the collection's element type
    Type elemType = receiverType.getElementType();
    Type iterVarType = normalizeToSingleton(elemType);

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(iterVarType);
        }
      }

      if (!requireBody(ctx, ctx.body, "collect")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;

      if (bodyType == Type.ERROR) {
        return Type.ERROR;
      }

      // collect result: preserve collection kind, element type from body
      // If body returns ¡T!, the element type is T (unwrap singleton)
      Type resultElemType = bodyType.isSingleton() ? bodyType.getElementType() : bodyType;
      Type resultType;
      if (receiverType.isSingleton()) {
        resultType = Type.singleton(resultElemType); // ¡T! → ¡U!
      } else if (receiverType.isOptional()) {
        resultType = Type.optional(resultElemType);  // ¿T? → ¿U?
      } else {
        resultType = preserveCollectionKind(receiverType, resultElemType);
      }
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code forAll()} iterator.
   *
   * <p>Iterator variable receives type {@code ¡T!} (singleton) — each element drawn from a
   * collection is a singleton value.
   *
   * @param ctx the context of the 'forAll' operation node in the AST
   * @return {@link Type#BOOLEAN} if successful; otherwise {@link Type#ERROR}
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitForAllOp(VitruvOCLParser.ForAllOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'forAll' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // each iterated element is a singleton ¡T! of the receiver's element type
    Type elemType = receiverType.getElementType();
    Type iterVarType = normalizeToSingleton(elemType);

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
          PHASE_TAG);
      return Type.ERROR;
    }

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(iterVarType);
        }
      }

      if (!requireBody(ctx, ctx.body, "forAll")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "forAll predicate must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
        return Type.ERROR;
      }

      nodeTypes.put(ctx, Type.BOOLEAN);
      return Type.BOOLEAN;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code exists()} iterator.
   *
   * <p>Iterator variable receives type {@code ¡T!} (singleton) per.
   *
   * @param ctx the context of the 'exists' operation node in the AST
   * @return {@link Type#BOOLEAN} if successful; otherwise {@link Type#ERROR}
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitExistsOp(VitruvOCLParser.ExistsOpContext ctx) {
    LOG.fine("[DBG] visitExistsOp: iteratorVars=" + ctx.iteratorVars
        + " body=" + ctx.body
        + " bodyChildCount=" + (ctx.body == null ? "null" : ctx.body.getChildCount())
        + " bodyException=" + (ctx.body == null ? "null" : ctx.body.exception));
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'exists' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // each iterated element is a singleton ¡T! of the receiver's element type
    Type elemType = receiverType.getElementType();
    Type iterVarType = normalizeToSingleton(elemType);

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
          PHASE_TAG);
      return Type.ERROR;
    }

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);

    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) {
          symbol.setType(iterVarType);
        }
      }

      if (!requireBody(ctx, ctx.body, OP_EXISTS)) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;

      if (!bodyType.getElementType().equals(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "exists predicate must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
        return Type.ERROR;
      }

      nodeTypes.put(ctx, Type.BOOLEAN);
      return Type.BOOLEAN;
    } finally {
      symbolTable.exitScope();
    }
  }

  @Override
  public Type visitIteratorMissingBody(VitruvOCLParser.IteratorMissingBodyContext ctx) {
    String opName = ctx.op.getText();
    org.antlr.v4.runtime.Token start = ctx.getStart();
    org.antlr.v4.runtime.Token stop = ctx.getStop(); // closing ')'
    int endLine = stop != null ? stop.getLine() : start.getLine();
    int endCol = stop != null
        ? stop.getCharPositionInLine() + stop.getText().length()
        : start.getCharPositionInLine() + ctx.getText().length();
    errors.add(new CompileError(
        start.getLine(), start.getCharPositionInLine(),
        endLine, endCol,
        "'" + opName + "' requires '| <body>' after the iterator variable(s)",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return Type.ERROR;
  }

  @Override
  public Type visitNoArgOpWithArgs(VitruvOCLParser.NoArgOpWithArgsContext ctx) {
    // Visit args so their own errors surface, then report the operation error.
    for (VitruvOCLParser.ExpCSContext arg : ctx.args) visit(arg);
    String opName = ctx.op.getText();
    org.antlr.v4.runtime.Token start = ctx.getStart();
    org.antlr.v4.runtime.Token stop = ctx.getStop();
    int endLine = stop != null ? stop.getLine() : start.getLine();
    int endCol = stop != null
        ? stop.getCharPositionInLine() + stop.getText().length()
        : start.getCharPositionInLine() + ctx.getText().length();
    errors.add(new CompileError(
        start.getLine(), start.getCharPositionInLine(),
        endLine, endCol,
        "'" + opName + "' takes no arguments",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return Type.ERROR;
  }

  private static final java.util.Set<String> NO_ARG_OPS = java.util.Set.of(
      "size", "isEmpty", "notEmpty", "first", "last", "reverse",
      OP_AS_SET, OP_AS_BAG, OP_AS_SEQUENCE, OP_AS_ORDERED_SET, "lift",
      "abs", OP_FLOOR, OP_CEILING, OP_ROUND, "allInstances");

  @Override
  public Type visitOpMissingParens(VitruvOCLParser.OpMissingParensContext ctx) {
    org.antlr.v4.runtime.Token op = ctx.op;
    String name = op.getText();
    String suggestion;
    String message;
    if (NO_ARG_OPS.contains(name)) {
      suggestion = name + "()";
      message = "'" + name + "' is an operation — add parentheses: '" + suggestion + "'";
    } else {
      suggestion = name + "(...)";
      message = "'" + name + "' is an operation that requires arguments — use '" + suggestion + "'";
    }
    errors.add(new CompileError(
        op.getLine(), op.getCharPositionInLine(),
        op.getLine(), op.getCharPositionInLine() + op.getText().length(),
        message, ErrorSeverity.ERROR, PHASE_TAG, null, suggestion));
    return Type.ERROR;
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
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "concat requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "concat argument must be String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  /**
   * Visits a 'substring' operation node in the AST.
   *
   * <p>This operation extracts a substring from a String receiver using start and end indices.
   *
   * <p>Steps:
   *
   * <ul>
   *   <li>Check that the receiver is of type {@link Type#STRING}.
   *   <li>Visit and check that both start and end indices are of type {@link Type#INTEGER}.
   *   <li>If type checks pass, the resulting type is {@link Type#STRING}.
   * </ul>
   *
   * @param ctx the context of the 'substring' operation node in the AST
   * @return {@link Type#STRING} if the receiver and indices are valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitSubstringOp(VitruvOCLParser.SubstringOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'substring' requires String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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
          PHASE_TAG);
    }

    if (!endType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "End index must be Integer",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  /**
   * Visits a 'toUpper' operation node in the AST.
   *
   * <p>Delegates to {@link #visitStringNoArgOp} for type checking and ensures the receiver is a
   * String. The result type is {@link Type#STRING}.
   *
   * @param ctx the context of the 'toUpper' operation node in the AST
   * @return {@link Type#STRING} if the receiver is a String; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitToUpperOp(VitruvOCLParser.ToUpperOpContext ctx) {
    return visitStringNoArgOp(ctx, "toUpper");
  }

  /**
   * Visits a 'toLower' operation node in the AST.
   *
   * <p>Delegates to {@link #visitStringNoArgOp} for type checking and ensures the receiver is a
   * String. The result type is {@link Type#STRING}.
   *
   * @param ctx the context of the 'toLower' operation node in the AST
   * @return {@link Type#STRING} if the receiver is a String; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitToLowerOp(VitruvOCLParser.ToLowerOpContext ctx) {
    return visitStringNoArgOp(ctx, "toLower");
  }

  /**
   * Visits an 'indexOf' operation node in the AST.
   *
   * <p>This operation returns the position of a substring within a String receiver.
   *
   * <ul>
   *   <li>Check that the receiver is {@link Type#STRING}.
   *   <li>Check that the argument is {@link Type#STRING}.
   *   <li>If valid, the resulting type is {@link Type#INTEGER}.
   * </ul>
   *
   * @param ctx the context of the 'indexOf' operation node in the AST
   * @return {@link Type#INTEGER} if the receiver and argument are valid; otherwise {@link
   *     Type#ERROR}
   */
  @Override
  public Type visitIndexOfOp(VitruvOCLParser.IndexOfOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'indexOf' requires String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Argument must be String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Visits an 'equalsIgnoreCase' operation node in the AST.
   *
   * <p>This operation compares the receiver String with another String argument, ignoring case.
   *
   * <ul>
   *   <li>Check that the receiver is {@link Type#STRING}.
   *   <li>Check that the argument is {@link Type#STRING}.
   *   <li>If valid, the resulting type is {@link Type#BOOLEAN}.
   * </ul>
   *
   * @param ctx the context of the 'equalsIgnoreCase' operation node in the AST
   * @return {@link Type#BOOLEAN} if the receiver and argument are valid; otherwise {@link
   *     Type#ERROR}
   */
  @Override
  public Type visitEqualsIgnoreCaseOp(VitruvOCLParser.EqualsIgnoreCaseOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'equalsIgnoreCase' requires String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Argument must be String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /** Helper for string operations with no arguments. */
  private Type visitStringNoArgOp(ParserRuleContext ctx, String opName) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires String",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  // ==================== Type Operations ====================

  /**
   * Type checks the {@code oclIsKindOf()} operation.
   *
   * <p>For a singleton {@code !T!} or bare metaclass receiver, returns {@code !Boolean!}. For a
   * multi-valued collection receiver, returns a collection of Boolean with the same kind.
   *
   * @param ctx The oclIsKindOf operation node
   * @return {@code !Boolean!} for singleton/bare receivers; {@code Collection(Boolean)} for
   *     multi-valued receivers
   */
  @Override
  public Type visitOclIsKindOfOp(VitruvOCLParser.OclIsKindOfOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    Type targetType = visit(ctx.type);
    if (targetType == null && ctx.type != null) {
      targetType = resolveTypeExpression(ctx.type);
    }

    // Mirror the logic of visitOclAsTypeOp: Singleton !T! must not be wrapped in Set first,
    // otherwise preserveCollectionKind would return Set{Boolean} = {Boolean} instead of !Boolean!
    Type resultType;
    if (receiverType.isCollection() && !receiverType.isSingleton()) {
      // Multi-valued collection: one Boolean per element, preserve collection kind
      resultType = preserveCollectionKind(receiverType, Type.BOOLEAN);
    } else {
      // Singleton !T! or bare metaclass T → oclIsKindOf returns a single !Boolean!
      resultType = Type.singleton(Type.BOOLEAN);
    }

    nodeTypes.put(ctx, resultType);
    nodeTypes.put(ctx.type, targetType);
    return resultType;
  }

  /** Helper to resolve type expressions for type operations. */
  private Type resolveTypeExpression(VitruvOCLParser.TypeExpCSContext ctx) {
    String text = ctx.getText();
    return switch (text) {
      case TYPE_INTEGER -> Type.INTEGER;
      case TYPE_STRING -> Type.STRING;
      case TYPE_BOOLEAN -> Type.BOOLEAN;
      default -> Type.ANY;
    };
  }

  /**
   * Visits an 'oclIsTypeOf' operation node in the AST.
   *
   * <p>This operation checks whether each element of a collection is exactly of a given type.
   *
   * <ul>
   *   <li>Ensures that the receiver is a collection; otherwise reports an error and returns {@link
   *       Type#ERROR}.
   *   <li>The resulting type preserves the collection kind of the receiver, but with {@link
   *       Type#BOOLEAN} as element type.
   *   <li>The resulting type is stored in {@code nodeTypes}.
   * </ul>
   *
   * @param ctx the context of the 'oclIsTypeOf' operation node in the AST
   * @return the collection of Boolean type if receiver is valid; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitOclIsTypeOfOp(VitruvOCLParser.OclIsTypeOfOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    Type targetType = visit(ctx.type);
    if (targetType == null && ctx.type != null) {
      targetType = resolveTypeExpression(ctx.type);
    }

    // Mirror visitOclIsKindOfOp: Singleton !T! must not be wrapped in Set first
    Type resultType;
    if (receiverType.isCollection() && !receiverType.isSingleton()) {
      // Multi-valued collection: one Boolean per element, preserve collection kind
      resultType = preserveCollectionKind(receiverType, Type.BOOLEAN);
    } else {
      // Singleton !T! or bare metaclass T → oclIsTypeOf returns a single !Boolean!
      resultType = Type.singleton(Type.BOOLEAN);
    }
    nodeTypes.put(ctx, resultType);
    nodeTypes.put(ctx.type, targetType);
    return resultType;
  }

  /**
   * Visits an 'oclAsType' operation node in the AST.
   *
   * <p>Casts the receiver to the target type while preserving the multiplicity. The receiver's
   * ctype multiplicity is preserved — only the member type τ is replaced by the target type:
   *
   * <ul>
   *   <li>{@code ¡Shape! .oclAsType(Box)} → {@code ¡Box!}
   *   <li>{@code {Shape} .oclAsType(Box)} → {@code {Box}}
   * </ul>
   *
   * @param ctx the context of the 'oclAsType' operation node in the AST
   * @return the cast type with preserved multiplicity, or {@link Type#ERROR}
   */
  @Override
  public Type visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    Type targetType = visit(ctx.type);
    if (targetType == null && ctx.type != null) {
      targetType = resolveTypeExpression(ctx.type);
    }

    // preserve the receiver's multiplicity, replace only the
    // member type τ. Bare metaclass types are implicitly ¡T! (singleton).
    Type resultType;
    if (receiverType.isCollection() && !receiverType.isSingleton()) {
      // Multi-valued collection {T}, [T], etc. — preserve collection kind
      resultType = preserveCollectionKind(receiverType, targetType);
    } else {
      // Singleton ¡T! or bare metaclass T → result is ¡targetType!
      resultType = Type.singleton(targetType);
    }

    nodeTypes.put(ctx, resultType);
    nodeTypes.put(ctx.type, targetType);
    return resultType;
  }

  // ==================== Metamodel Operations ====================
  /**
   * Type checks the {@code allInstances()} operation.
   *
   * <p>Requires metaclass receiver, returns Set of that metaclass type.
   *
   * <p><b>Example:</b> {@code Person.allInstances()} → OrderedSet(Person)
   *
   * @param ctx The allInstances operation node
   * @return OrderedSet(MetaclassType)
   */
  @Override
  public Type visitAllInstancesOp(VitruvOCLParser.AllInstancesOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream

    if (!receiverType.isMetaclassType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "allInstances() requires metaclass receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type resultType = Type.orderedSet(receiverType);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Not Yet Implemented ====================
  /**
   * Type checks correspondence operator (~).
   *
   * <p>The correspondence operator is a binary predicate that checks if two objects are related via
   * a Correspondence model instance. Both operands must be single object instances (not
   * collections, not primitives).
   *
   * <p><b>Syntax:</b> {@code obj1 ~ obj2}
   *
   * <p><b>Type:</b> {@code Boolean}
   *
   * <p><b>Examples:</b>
   *
   * <pre>
   * self ~ sat                                    // Boolean
   * Satellite.allInstances().select(s | self ~ s) // Filtered collection
   * self ~ sat implies sat.active                 // Consistency check
   * </pre>
   *
   * @param ctx Correspondence comparison operation node
   * @return Type.BOOLEAN if operands are valid, Type.ERROR otherwise
   */
  @Override
  public Type visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    // Unwrap singletons to get base types
    Type baseLeftType = leftType.isSingleton() ? leftType.getElementType() : leftType;
    Type baseRightType = rightType.isSingleton() ? rightType.getElementType() : rightType;

    // Both sides must be single objects (singleton or object type, not multi-valued collection)
    if (leftType.isCollection() && leftType.getMultiplicity() != Multiplicity.SINGLETON) {
      errors.add(
          ctx.left.start.getLine(),
          ctx.left.start.getCharPositionInLine(),
          "Correspondence operator ~ requires single object on left side, got collection: "
              + leftType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (rightType.isCollection() && rightType.getMultiplicity() != Multiplicity.SINGLETON) {
      errors.add(
          ctx.right.start.getLine(),
          ctx.right.start.getCharPositionInLine(),
          "Correspondence operator ~ requires single object on right side, got collection: "
              + rightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // Both must be metaclass types (not primitives)
    if (!baseLeftType.isMetaclassType() && baseLeftType != Type.ANY) {
      errors.add(
          ctx.left.start.getLine(),
          ctx.left.start.getCharPositionInLine(),
          "Correspondence operator ~ requires object type on left side, got: " + baseLeftType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    if (!baseRightType.isMetaclassType() && baseRightType != Type.ANY) {
      errors.add(
          ctx.right.start.getLine(),
          ctx.right.start.getCharPositionInLine(),
          "Correspondence operator ~ requires object type on right side, got: " + baseRightType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // Result is always Boolean
    Type resultType = Type.BOOLEAN;
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks select(~) / select(~, Type=T, Tag='x') shorthand.
   *
   * <p>Without a Type filter: returns the receiver collection type unchanged. With a Type filter:
   * returns a collection of that metaclass type (same collection kind, narrowed element type).
   *
   * @param ctx The selectCorrespondence node
   * @return Refined or unchanged receiver collection type, or Type.ERROR
   */
  @Override
  public Type visitSelectCorrespondence(VitruvOCLParser.SelectCorrespondenceContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == null || receiverType == Type.ERROR) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "Cannot determine receiver type for select(~)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "select(~) requires 'self' to be bound in current context",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type baseSelfType = selfSymbol.getType();
    if (baseSelfType.isSingleton()) baseSelfType = baseSelfType.getElementType();

    if (!baseSelfType.isMetaclassType() && baseSelfType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "select(~) requires 'self' to be an object type, got: " + baseSelfType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!elemType.isMetaclassType() && elemType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "select(~) requires collection of object types, got: " + elemType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // Visit filter to check options and extract optional Type refinement
    int corrErrsBefore = errors.getErrors().size();
    Type filterType = visit(ctx.corrFilter);
    if (reportCorrFilterParseError(ctx.corrFilter, ctx, OP_SELECT, corrErrsBefore))
      return Type.ERROR;

    // If a Type filter was given, narrow the result collection element type
    Type resultType =
        (filterType != Type.ANY && filterType != Type.ERROR)
            ? preserveCollectionKind(receiverType, filterType)
            : receiverType;

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks reject(~) / reject(~, Type=T, Tag='x') shorthand.
   *
   * <p>Type filter narrows the element type of the returned collection.
   *
   * @param ctx The rejectCorrespondence node
   * @return Refined or unchanged receiver collection type, or Type.ERROR
   */
  @Override
  public Type visitRejectCorrespondence(VitruvOCLParser.RejectCorrespondenceContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == null || receiverType == Type.ERROR) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "Cannot determine receiver type for reject(~)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "reject(~) requires 'self' to be bound in current context",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type baseSelfType = selfSymbol.getType();
    if (baseSelfType.isSingleton()) baseSelfType = baseSelfType.getElementType();

    if (!baseSelfType.isMetaclassType() && baseSelfType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "reject(~) requires 'self' to be an object type, got: " + baseSelfType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!elemType.isMetaclassType() && elemType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "reject(~) requires collection of object types, got: " + elemType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    int corrErrsBefore = errors.getErrors().size();
    Type filterType = visit(ctx.corrFilter);
    if (reportCorrFilterParseError(ctx.corrFilter, ctx, OP_REJECT, corrErrsBefore))
      return Type.ERROR;

    Type resultType =
        (filterType != Type.ANY && filterType != Type.ERROR)
            ? preserveCollectionKind(receiverType, filterType)
            : receiverType;

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks exists(~) / exists(~, Type=T, Tag='x') shorthand.
   *
   * <p>Always returns Boolean. Type/Tag filters are validated but don't change the return type.
   *
   * @param ctx The existsCorrespondence node
   * @return Type.BOOLEAN if validation succeeds, Type.ERROR otherwise
   */
  @Override
  public Type visitExistsCorrespondence(VitruvOCLParser.ExistsCorrespondenceContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == null || receiverType == Type.ERROR) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "Cannot determine receiver type for exists(~)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "exists(~) requires 'self' to be bound in current context",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type baseSelfType = selfSymbol.getType();
    if (baseSelfType.isSingleton()) baseSelfType = baseSelfType.getElementType();

    if (!baseSelfType.isMetaclassType() && baseSelfType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "exists(~) requires 'self' to be an object type, got: " + baseSelfType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Type elemType = receiverType.getElementType();
    if (!elemType.isMetaclassType() && elemType != Type.ANY) {
      errors.add(
          ctx.start.getLine(),
          ctx.start.getCharPositionInLine(),
          "exists(~) requires collection of object types, got: " + elemType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    // Validate filter options (type-checks the Type= expression if present)
    int corrErrsBefore = errors.getErrors().size();
    visit(ctx.corrFilter);
    if (reportCorrFilterParseError(ctx.corrFilter, ctx, OP_EXISTS, corrErrsBefore))
      return Type.ERROR;

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Checks whether a correspondenceFilterCS subtree had ANTLR parse errors (silent recovery from
   * e.g. extra commas like {@code reject(~, , , Type=T)}). When errors are detected and no
   * TypeChecker error was already reported for the filter, adds a targeted error spanning the full
   * operation call (e.g. the whole {@code reject(~, , , ...)}) and returns {@code true}.
   *
   * @param corrFilter  the parsed correspondenceFilterCS context
   * @param callCtx     the parent iterator/correspondence call context (for span)
   * @param opName      operation name for the message (e.g. "reject")
   * @param errsBefore  error count before visiting the filter
   * @return {@code true} if a parse error was detected
   */
  private boolean reportCorrFilterParseError(
      org.antlr.v4.runtime.ParserRuleContext corrFilter,
      org.antlr.v4.runtime.ParserRuleContext callCtx,
      String opName,
      int errsBefore) {
    boolean tcErr = errors.getErrors().size() > errsBefore;
    boolean parseErr = hasErrorNode(corrFilter) || hasExtraCommasInRange(corrFilter);
    if (!parseErr && !tcErr) return false;
    if (!tcErr) {
      // ANTLR recovered silently — no TypeChecker error was emitted yet → report one
      org.antlr.v4.runtime.Token start = callCtx.getStart();
      org.antlr.v4.runtime.Token stop  = callCtx.getStop();
      int endLine = stop != null ? stop.getLine() : start.getLine();
      int endCol  = stop != null
          ? stop.getCharPositionInLine() + stop.getText().length()
          : start.getCharPositionInLine() + callCtx.getText().length();
      errors.add(new CompileError(
          start.getLine(), start.getCharPositionInLine(), endLine, endCol,
          "'" + opName + "(~, ...)' has invalid arguments — check for extra or misplaced commas",
          ErrorSeverity.ERROR, PHASE_TAG, null));
    }
    return true;
  }

  /** Tokens that can start a new expression — if one follows a complete spec, an operator is missing. */
  private static final java.util.Set<String> EXPR_START_KEYWORDS =
      java.util.Set.of("self", "true", "false", "not", "if", "let");

  /**
   * Checks whether there are expression-starting tokens immediately after {@code spec.getStop()}
   * that were silently dropped by ANTLR error recovery. This catches patterns like
   * {@code self.a == "x"  persons::Foo.allInstances()...} where the user forgot
   * {@code and}/{@code or}/{@code implies} between two sub-expressions.
   *
   * @param spec  the parsed specification context
   * @return {@code true} if a missing-operator error was reported
   */
  private boolean reportMissingOperatorBetweenExpressions(
      org.antlr.v4.runtime.ParserRuleContext spec) {
    if (tokens == null || spec.getStop() == null) return false;
    int idx = nextDefault(spec.getStop().getTokenIndex() + 1);
    if (idx < 0) return false;
    org.antlr.v4.runtime.Token next = tokens.get(idx);
    // Token types that terminate an invCS (not expression starters)
    String txt = next.getText();
    if (",".equals(txt) || ")".equals(txt) || "context".equals(txt)
        || "inv".equals(txt) || "<EOF>".equals(txt)) return false;
    // Is it an expression-starting token?
    boolean isExprStart = EXPR_START_KEYWORDS.contains(txt)
        // ID followed by '::' (qualified name) or '.' (navigation) or '(' (call)
        || (txt.matches("[a-zA-Z_]\\w*") && isFollowedByNavOrScope(idx))
        // string literal
        || (txt.startsWith("\"") || txt.startsWith("'"))
        // numeric literal
        || txt.matches("-?\\d+(\\.\\d+)?");
    if (!isExprStart) return false;
    errors.add(new CompileError(
        next.getLine(), next.getCharPositionInLine(),
        next.getLine(), next.getCharPositionInLine() + next.getText().length(),
        "Missing logical operator before this expression — did you mean 'and', 'or', or 'implies'?",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return true;
  }

  private boolean isFollowedByNavOrScope(int idIdx) {
    int next = nextDefault(idIdx + 1);
    if (next < 0) return false;
    String t = tokens.get(next).getText();
    return "::".equals(t) || ".".equals(t) || "(".equals(t);
  }

  private static final java.util.Set<String> COMPARISON_OPS =
      java.util.Set.of("==", "!=", "<", "<=", ">", ">=");

  /**
   * Checks whether the token immediately after {@code ctx.getStop()} in the stream is a
   * comparison or arithmetic operator that implies a missing right-hand operand. If so,
   * reports the error ON that operator token and returns {@code true}.
   */
  private boolean reportMissingOperandAfter(org.antlr.v4.runtime.ParserRuleContext ctx) {
    if (tokens == null || ctx.getStop() == null) return false;
    int idx = nextDefault(ctx.getStop().getTokenIndex() + 1);
    if (idx < 0) return false;
    org.antlr.v4.runtime.Token opTok = tokens.get(idx);
    if (!COMPARISON_OPS.contains(opTok.getText())) return false;
    errors.add(new CompileError(
        opTok.getLine(), opTok.getCharPositionInLine(),
        opTok.getLine(), opTok.getCharPositionInLine() + opTok.getText().length(),
        "Missing right-hand operand for '" + opTok.getText() + "'",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return true;
  }

  private static final java.util.Set<String> CORR_OPS_SET =
      java.util.Set.of(OP_REJECT, OP_SELECT, OP_EXISTS);

  /**
   * Looks in the token stream immediately after {@code parsedExpr.getStop()} for an unparsed
   * navigation step of the form {@code .reject(~…)/select(~…)/exists(~…)} that ANTLR failed to
   * match (typically due to extra commas inside the correspondence filter).
   *
   * @return [opTokenIndex, closingParenTokenIndex] if found, or empty array if not found
   */
  private int[] findUnparsedCorrCallAfter(org.antlr.v4.runtime.ParserRuleContext parsedExpr) {
    if (tokens == null || parsedExpr.getStop() == null) return new int[0];
    int idx = parsedExpr.getStop().getTokenIndex() + 1;
    idx = nextDefault(idx);
    if (idx < 0) return new int[0];
    // expect '.'
    if (!".".equals(tokens.get(idx).getText())) return new int[0];
    idx = nextDefault(idx + 1);
    if (idx < 0) return new int[0];
    // expect reject / select / exists
    org.antlr.v4.runtime.Token opTok = tokens.get(idx);
    if (!CORR_OPS_SET.contains(opTok.getText())) return new int[0];
    int opIdx = idx;
    idx = nextDefault(idx + 1);
    if (idx < 0) return new int[0];
    // expect '('
    if (!"(".equals(tokens.get(idx).getText())) return new int[0];
    // find matching ')'
    int depth = 0;
    for (int i = idx; i < tokens.size(); i++) {
      String txt = tokens.get(i).getText();
      if ("(".equals(txt)) depth++;
      else if (")".equals(txt)) { depth--; if (depth == 0) return new int[]{opIdx, i}; }
    }
    return new int[0];
  }

  /** Returns the index of the next token on the default channel at or after {@code from}, or -1. */
  private int nextDefault(int from) {
    for (int i = from; i < tokens.size(); i++) {
      if (tokens.get(i).getChannel() == org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) return i;
    }
    return -1;
  }

  private boolean hasExtraCommasInRange(org.antlr.v4.runtime.ParserRuleContext ctx) {
    if (tokens == null || ctx.getStart() == null) return false;
    int from = ctx.getStart().getTokenIndex();
    int to = ctx.getStop() != null ? ctx.getStop().getTokenIndex() : from;
    boolean prevWasComma = false;
    for (int i = from; i <= to; i++) {
      org.antlr.v4.runtime.Token t = tokens.get(i);
      if (t.getChannel() != org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) continue;
      if (",".equals(t.getText())) {
        if (prevWasComma) return true; // two consecutive commas
        prevWasComma = true;
      } else {
        prevWasComma = false;
      }
    }
    return false;
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
        PHASE_TAG);

    nodeTypes.put(ctx, leftType);
    return leftType;
  }

  // ==================== Delegation & Miscellaneous ====================
  @Override
  public Type visitPrefixedExpr(VitruvOCLParser.PrefixedExprContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  /**
   * Visits a literal node in the AST.
   *
   * <p>Delegates type checking to the underlying literal expression.
   *
   * @param ctx the context of the literal node in the AST
   * @return the type of the literal expression
   */
  @Override
  public Type visitLiteral(VitruvOCLParser.LiteralContext ctx) {
    return visit(ctx.literalExpCS());
  }

  /**
   * Visits a conditional (if-then-else) node in the AST.
   *
   * <p>Delegates type checking to the underlying if-expression.
   *
   * @param ctx the context of the conditional node in the AST
   * @return the type of the if-expression
   */
  @Override
  public Type visitConditional(VitruvOCLParser.ConditionalContext ctx) {
    return visit(ctx.ifExpCS());
  }

  /**
   * Visits a let-binding node in the AST.
   *
   * <p>Delegates type checking to the underlying let-expression.
   *
   * @param ctx the context of the let-binding node in the AST
   * @return the type of the let-expression
   */
  @Override
  public Type visitLetBinding(VitruvOCLParser.LetBindingContext ctx) {
    return visit(ctx.letExpCS());
  }

  /**
   * Type-checks a {@code let} expression.
   *
   * <p>Retrieves the {@link Scope} pre-annotated by Pass 1, enters it, then type-checks all
   * variable declarations in order before type-checking each body expression in sequence. The type
   * of the last body expression is registered as the result type of the {@code let} expression.
   *
   * @param ctx the parse tree node for the {@code let} expression, containing the variable
   *     declaration list and one or more body expressions
   * @return the type of the last body expression; or {@link Type#ERROR} if no scope annotation is
   *     found, or the body contains no expressions
   */
  @Override
  public Type visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    Scope letScope = scopeAnnotator.getScope(ctx);
    if (letScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);

      return Type.ERROR;
    }

    symbolTable.enterScope(letScope);

    try {
      visit(ctx.variableDeclarations());

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
            PHASE_TAG);
        bodyType = Type.ERROR;
      }

      nodeTypes.put(ctx, bodyType);
      return bodyType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Visits a collection literal node in the AST.
   *
   * <p>Delegates type checking to the underlying collection literal expression.
   *
   * @param ctx the context of the collection literal node in the AST
   * @return the type of the collection literal expression
   */
  @Override
  public Type visitCollectionLiteral(VitruvOCLParser.CollectionLiteralContext ctx) {
    return visit(ctx.collectionLiteralExpCS());
  }

  /**
   * Visits a type literal node in the AST.
   *
   * <p>Delegates type checking to the underlying type literal expression.
   *
   * @param ctx the context of the type literal node in the AST
   * @return the type of the type literal expression
   */
  @Override
  public Type visitTypeLiteral(VitruvOCLParser.TypeLiteralContext ctx) {
    return visit(ctx.typeLiteralExpCS());
  }

  /**
   * Visits a nested expression node in the AST.
   *
   * <p>Delegates type checking to the underlying nested expression.
   *
   * @param ctx the context of the nested expression node in the AST
   * @return the type of the nested expression
   */
  @Override
  public Type visitNested(VitruvOCLParser.NestedContext ctx) {
    return visit(ctx.nestedExpCS());
  }

  /**
   * Visits a 'self' expression node in the AST.
   *
   * <p>Delegates type checking to the underlying self expression.
   *
   * @param ctx the context of the self expression node in the AST
   * @return the type of the self expression
   */
  @Override
  public Type visitSelf(VitruvOCLParser.SelfContext ctx) {
    return visit(ctx.selfExpCS());
  }

  /**
   * Visits a variable reference node in the AST.
   *
   * <p>Delegates type checking to the underlying variable expression.
   *
   * @param ctx the context of the variable reference node in the AST
   * @return the type of the variable expression
   */
  @Override
  public Type visitVariable(VitruvOCLParser.VariableContext ctx) {
    return visit(ctx.variableExpCS());
  }

  /**
   * Visits a nested expression node in the AST.
   *
   * <p>A nested expression may contain one or more sub-expressions. This method performs the
   * following steps:
   *
   * <ul>
   *   <li>Checks whether the nested expression contains any sub-expressions; if empty, an error is
   *       reported and {@link Type#ERROR} is returned.
   *   <li>Iterates over all sub-expressions, visiting each one and updating the resulting type to
   *       the type of the last sub-expression.
   *   <li>Stores the resulting type in {@code nodeTypes}.
   * </ul>
   *
   * @param ctx the context of the nested expression node in the AST
   * @return the type of the last sub-expression if any exist; otherwise {@link Type#ERROR}
   */
  @Override
  public Type visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    List<VitruvOCLParser.ExpCSContext> exps = ctx.expCS();
    if (exps.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Empty nested expression",
          ErrorSeverity.ERROR,
          PHASE_TAG);
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

  // ==================== Correspondence Filter ====================

  /**
   * Type checks a correspondence filter expression.
   *
   * <p>A correspondence filter consists of the '~' marker and optional Type/Tag options. This
   * method validates the options and returns the target metaclass type if a Type filter is present,
   * or Type.ANY otherwise.
   *
   * <p><b>Examples:</b>
   *
   * <pre>
   *   ~                                    // no filter
   *   ~, Tag = 'Brother'                  // tag filter only
   *   ~, Type = MM::Person               // type filter only
   *   ~, Type = MM::Person, Tag = 'Sibling'  // both
   * </pre>
   *
   * @param ctx The correspondence filter node
   * @return The resolved target metaclass type, or Type.ANY if no Type filter
   */
  @Override
  public Type visitCorrespondenceFilterCS(VitruvOCLParser.CorrespondenceFilterCSContext ctx) {
    if (ctx.correspondenceOptions() == null) {
      nodeTypes.put(ctx, Type.ANY);
      return Type.ANY;
    }
    Type result = visit(ctx.correspondenceOptions());
    nodeTypes.put(ctx, result);
    return result;
  }

  // ==================== New Iterator Operations ====================

  /**
   * Type checks the {@code one()} iterator.
   *
   * <p>Returns true iff exactly one element satisfies the predicate.
   *
   * @param ctx The one operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitOneOp(VitruvOCLParser.OneOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'one' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }

    Type iterVarType = normalizeToSingleton(receiverType.getElementType());
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) symbol.setType(iterVarType);
      }
      if (!requireBody(ctx, ctx.body, "one")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      Type checkType = bodyType.isCollection() ? bodyType.getElementType() : bodyType;
      if (!checkType.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "one() predicate must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }
      nodeTypes.put(ctx, Type.BOOLEAN);
      return Type.BOOLEAN;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code any()} iterator.
   *
   * <p>Returns an arbitrary element satisfying the predicate — result is a singleton of the element
   * type.
   *
   * @param ctx The any operation node
   * @return Singleton of element type
   */
  @Override
  public Type visitAnyOp(VitruvOCLParser.AnyOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isExplicitCollectionType()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'any' requires a collection, ¡T!, or ¿T? receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }

    Type iterVarType = normalizeToSingleton(receiverType.getElementType());
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) symbol.setType(iterVarType);
      }
      if (!requireBody(ctx, ctx.body, "any")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      Type checkType = bodyType.isCollection() ? bodyType.getElementType() : bodyType;
      if (!checkType.isConformantTo(Type.BOOLEAN)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "any() predicate must return Boolean",
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }
      // any() returns ¿T? — may find no matching element
      Type resultType = Type.optional(receiverType.getElementType());
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code isUnique()} iterator.
   *
   * <p>Returns true iff all body results are distinct.
   *
   * @param ctx The isUnique operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitIsUniqueOp(VitruvOCLParser.IsUniqueOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'isUnique' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }

    Type iterVarType = normalizeToSingleton(receiverType.getElementType());
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) symbol.setType(iterVarType);
      }
      visit(ctx.body); // body type unconstrained — just check it parses
      nodeTypes.put(ctx, Type.BOOLEAN);
      return Type.BOOLEAN;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code sortedBy()} iterator.
   *
   * <p>Body must return a comparable type. Result is an OrderedSet of the element type.
   *
   * @param ctx The sortedBy operation node
   * @return OrderedSet of element type
   */
  @Override
  public Type visitSortedByOp(VitruvOCLParser.SortedByOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'sortedBy' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }

    Type iterVarType = normalizeToSingleton(receiverType.getElementType());
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) symbol.setType(iterVarType);
      }
      if (!requireBody(ctx, ctx.body, "sortedBy")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      // body should return something comparable (numeric or string)
      Type checkType = bodyType.isSingleton() ? bodyType.getElementType() : bodyType;
      if (checkType != Type.ANY
          && !TypeResolver.isNumeric(checkType)
          && !checkType.isConformantTo(Type.STRING)
          && !checkType.isEnumType()
          && checkType != Type.ERROR) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "sortedBy() body must return a comparable type (numeric or String), got: " + checkType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }
      // result is always OrderedSet of the original element type
      Type resultType = Type.orderedSet(receiverType.getElementType());
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code collectNested()} iterator.
   *
   * <p>Like collect() but does NOT flatten — returns a collection of collections.
   *
   * @param ctx The collectNested operation node
   * @return Collection of body types (not flattened)
   */
  @Override
  public Type visitCollectNestedOp(VitruvOCLParser.CollectNestedOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'collectNested' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }

    Type iterVarType = normalizeToSingleton(receiverType.getElementType());
    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      for (String iterVar : iterVars) {
        VariableSymbol symbol = symbolTable.resolveVariable(iterVar);
        if (symbol != null && symbol.getType() == Type.ANY) symbol.setType(iterVarType);
      }
      if (!requireBody(ctx, ctx.body, "collectNested")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      // collectNested: preserve receiver kind, element type is the raw body type (no unwrap)
      Type resultType = preserveCollectionKind(receiverType, bodyType);
      nodeTypes.put(ctx, resultType);
      return resultType;
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Type checks the {@code iterate()} operation.
   *
   * <p>Syntax: {@code coll.iterate(elem; acc : T = init | body)} The accumulator type is the result
   * type.
   *
   * @param ctx The iterate operation node
   * @return The accumulator type
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Type visitIterateOp(VitruvOCLParser.IterateOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'iterate' requires collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    Scope iterScope = scopeAnnotator.getScope(ctx);
    if (iterScope == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          ERR_NO_SCOPE,
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }

    symbolTable.enterScope(iterScope);
    try {
      VitruvOCLParser.IterateVarSpecContext spec = ctx.iterateVarSpec();

      // Iterator variable — gets element type of receiver
      String iterVarName = spec.iterVar.getText();
      Type iterVarType = normalizeToSingleton(receiverType.getElementType());
      VariableSymbol iterSym = symbolTable.resolveVariable(iterVarName);
      if (iterSym != null && iterSym.getType() == Type.ANY) iterSym.setType(iterVarType);

      // Accumulator type — either explicit or inferred from init expression
      Type accType;
      if (spec.accType != null) {
        accType = visit(spec.accType);
      } else {
        accType = visit(spec.accInit);
      }
      String accVarName = spec.accVar.getText();
      VariableSymbol accSym = symbolTable.resolveVariable(accVarName);
      if (accSym != null && accSym.getType() == Type.ANY) accSym.setType(accType);

      // Init expression must conform to accumulator type
      Type initType = visit(spec.accInit);
      if (!initType.isConformantTo(accType) && !conformsWithUnwrapping(initType, accType)) {
        errors.add(
            spec.getStart().getLine(),
            spec.getStart().getCharPositionInLine(),
            "iterate() accumulator init type "
                + initType
                + " does not conform to declared type "
                + accType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }

      // Body must conform to accumulator type
      if (!requireBody(ctx, ctx.body, "iterate")) return Type.ERROR;
      Type bodyType = visit(ctx.body);
      if (bodyType == null || bodyType == Type.ERROR) return Type.ERROR;
      if (!bodyType.isConformantTo(accType)
          && !conformsWithUnwrapping(bodyType, accType)
          && bodyType != Type.ERROR) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "iterate() body type " + bodyType + " does not conform to accumulator type " + accType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }

      nodeTypes.put(ctx, accType);
      return accType;
    } finally {
      symbolTable.exitScope();
    }
  }

  // ==================== New Collection Conversion Operations ====================

  /**
   * Type checks the {@code asSet()} operation.
   *
   * @param ctx The asSet operation node
   * @return Set of element type
   */
  @Override
  public Type visitAsSetOp(VitruvOCLParser.AsSetOpContext ctx) {
    return visitCollectionConversionOp(ctx, OP_AS_SET, Type::set);
  }

  /**
   * Type checks the {@code asBag()} operation.
   *
   * @param ctx The asBag operation node
   * @return Bag of element type
   */
  @Override
  public Type visitAsBagOp(VitruvOCLParser.AsBagOpContext ctx) {
    return visitCollectionConversionOp(ctx, OP_AS_BAG, Type::bag);
  }

  /**
   * Type checks the {@code asSequence()} operation.
   *
   * @param ctx The asSequence operation node
   * @return Sequence of element type
   */
  @Override
  public Type visitAsSequenceOp(VitruvOCLParser.AsSequenceOpContext ctx) {
    return visitCollectionConversionOp(ctx, OP_AS_SEQUENCE, Type::sequence);
  }

  /**
   * Type checks the {@code asOrderedSet()} operation.
   *
   * @param ctx The asOrderedSet operation node
   * @return OrderedSet of element type
   */
  @Override
  public Type visitAsOrderedSetOp(VitruvOCLParser.AsOrderedSetOpContext ctx) {
    return visitCollectionConversionOp(ctx, OP_AS_ORDERED_SET, Type::orderedSet);
  }

  /** Helper for asSet/asBag/asSequence/asOrderedSet — all follow the same pattern. */
  private Type visitCollectionConversionOp(
      ParserRuleContext ctx, String opName, java.util.function.Function<Type, Type> factory) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type resultType = factory.apply(receiverType.getElementType());
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== New Collection Operations ====================

  /**
   * Type checks the {@code includesAll()} operation.
   *
   * @param ctx The includesAll operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitIncludesAllOp(VitruvOCLParser.IncludesAllOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'includesAll' requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    if (!argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'includesAll' argument must be a collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Type checks the {@code excludesAll()} operation.
   *
   * @param ctx The excludesAll operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitExcludesAllOp(VitruvOCLParser.ExcludesAllOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excludesAll' requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    if (!argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'excludesAll' argument must be a collection",
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Type checks the {@code count()} operation.
   *
   * <p>Returns how many times an element occurs in the collection.
   *
   * @param ctx The count operation node
   * @return Type.INTEGER
   */
  @Override
  public Type visitCountOp(VitruvOCLParser.CountOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'count' requires collection receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type argType = visit(ctx.arg);
    Type elemType = receiverType.getElementType();
    if (!argType.isConformantTo(elemType)
        && !conformsWithUnwrapping(argType, elemType)
        && argType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "count() argument type "
              + argType
              + " incompatible with collection element type "
              + elemType,
          ErrorSeverity.WARNING,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Type checks the {@code intersection()} operation.
   *
   * <p>Both operands must be collections with compatible element types. Result preserves the more
   * restrictive kind (Set beats Bag, OrderedSet beats Sequence).
   *
   * @param ctx The intersection operation node
   * @return Collection of common element type
   */
  @Override
  public Type visitIntersectionOp(VitruvOCLParser.IntersectionOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);
    if (!receiverType.isCollection() || !argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'intersection' requires collection operands",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type commonElem = Type.commonSuperType(receiverType.getElementType(), argType.getElementType());
    // intersection result: unique if either operand is unique; ordered only if both are ordered
    boolean eitherUnique = receiverType.isUnique() || argType.isUnique();
    boolean bothOrdered = receiverType.isOrdered() && argType.isOrdered();
    Type resultType;
    if (eitherUnique && bothOrdered) {
      resultType = Type.orderedSet(commonElem);
    } else if (eitherUnique) {
      resultType = Type.set(commonElem);
    } else if (bothOrdered) {
      resultType = Type.sequence(commonElem);
    } else {
      resultType = Type.bag(commonElem);
    }
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code symmetricDifference()} operation.
   *
   * <p>Both operands must be Sets. Result is a Set of common element type.
   *
   * @param ctx The symmetricDifference operation node
   * @return Set of element type
   */
  @Override
  public Type visitSymmetricDifferenceOp(VitruvOCLParser.SymmetricDifferenceOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);
    if (!receiverType.isCollection() || !argType.isCollection()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'symmetricDifference' requires collection operands",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    if (!receiverType.isUnique() || !argType.isUnique()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'symmetricDifference' requires Set operands (unique collections)",
          ErrorSeverity.ERROR, // WARNING → ERROR
          PHASE_TAG);
      return Type.ERROR;
    }

    Type commonElem = Type.commonSuperType(receiverType.getElementType(), argType.getElementType());
    Type resultType = Type.set(commonElem);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code prepend()} operation.
   *
   * <p>Requires Sequence or OrderedSet receiver. Returns same collection type.
   *
   * @param ctx The prepend operation node
   * @return Same ordered collection type
   */
  @Override
  public Type visitPrependOp(VitruvOCLParser.PrependOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'prepend' requires ordered collection (Sequence or OrderedSet)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type argType = visit(ctx.arg);
    Type elemType = receiverType.getElementType();
    if (!argType.isConformantTo(elemType)
        && !conformsWithUnwrapping(argType, elemType)
        && argType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "prepend() argument type " + argType + " incompatible with element type " + elemType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Type checks the {@code insertAt()} operation.
   *
   * <p>Requires ordered collection. Index must be Integer.
   *
   * @param ctx The insertAt operation node
   * @return Same ordered collection type
   */
  @Override
  public Type visitInsertAtOp(VitruvOCLParser.InsertAtOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'insertAt' requires ordered collection (Sequence or OrderedSet)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type indexType = visit(ctx.index);
    if (!indexType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "insertAt() index must be Integer, got " + indexType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    Type argType = visit(ctx.arg);
    Type elemType = receiverType.getElementType();
    if (!argType.isConformantTo(elemType)
        && !conformsWithUnwrapping(argType, elemType)
        && argType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "insertAt() element type "
              + argType
              + " incompatible with collection element type "
              + elemType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Type checks the {@code subSequence()} operation.
   *
   * <p>Requires ordered collection. Both bounds must be Integer.
   *
   * @param ctx The subSequence operation node
   * @return Same ordered collection type
   */
  @Override
  public Type visitSubSequenceOp(VitruvOCLParser.SubSequenceOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'subSequence' requires ordered collection (Sequence or OrderedSet)",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type startType = visit(ctx.start);
    Type endType = visit(ctx.end);
    if (!startType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "subSequence() start index must be Integer, got " + startType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    if (!endType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "subSequence() end index must be Integer, got " + endType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, receiverType);
    return receiverType;
  }

  /**
   * Type checks the {@code at()} operation.
   *
   * <p>Requires ordered collection. Index must be Integer. Returns singleton of element type.
   *
   * @param ctx The at operation node
   * @return Singleton of element type
   */
  @Override
  public Type visitAtOp(VitruvOCLParser.AtOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    // at() also valid on String (returns a character as String)
    if (receiverType.isConformantTo(Type.STRING)) {
      Type indexType = visit(ctx.index);
      if (!indexType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "at() index must be Integer, got " + indexType,
            ErrorSeverity.ERROR,
            PHASE_TAG);
      }
      nodeTypes.put(ctx, Type.STRING);
      return Type.STRING;
    }
    if (!receiverType.isCollection() || !receiverType.isOrdered()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'at' requires ordered collection or String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type indexType = visit(ctx.index);
    if (!indexType.isConformantTo(Type.INTEGER)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "at() index must be Integer, got " + indexType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    Type resultType = Type.singleton(receiverType.getElementType());
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code ceiling()} operation (alias for ceil).
   *
   * @param ctx The ceiling operation node
   * @return Receiver type unchanged
   */
  @Override
  public Type visitCeilingOp(VitruvOCLParser.CeilingOpContext ctx) {
    return visitNumericOp(ctx, OP_CEILING);
  }

  /**
   * Type checks the {@code div()} operation (integer division).
   *
   * <p>Both receiver and argument must be Integer.
   *
   * @param ctx The div operation node
   * @return Type.INTEGER
   */
  @Override
  public Type visitDivOp(VitruvOCLParser.DivOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    Type baseReceiver = receiverType.isSingleton() ? receiverType.getElementType() : receiverType;
    Type baseArg = argType.isSingleton() ? argType.getElementType() : argType;

    if (baseReceiver != Type.INTEGER && baseReceiver != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "div() requires Integer receiver, got " + receiverType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    if (baseArg != Type.INTEGER && baseArg != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "div() requires Integer argument, got " + argType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Type checks the {@code mod()} operation (integer remainder).
   *
   * <p>Both receiver and argument must be Integer.
   *
   * @param ctx The mod operation node
   * @return Type.INTEGER
   */
  @Override
  public Type visitModOp(VitruvOCLParser.ModOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    Type argType = visit(ctx.arg);

    Type baseReceiver = receiverType.isSingleton() ? receiverType.getElementType() : receiverType;
    Type baseArg = argType.isSingleton() ? argType.getElementType() : argType;

    if (baseReceiver != Type.INTEGER && baseReceiver != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "mod() requires Integer receiver, got " + receiverType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    if (baseArg != Type.INTEGER && baseArg != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "mod() requires Integer argument, got " + argType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  // ==================== New String Operations ====================

  /**
   * Type checks the {@code toInteger()} operation.
   *
   * @param ctx The toInteger operation node
   * @return Type.INTEGER
   */
  @Override
  public Type visitToIntegerOp(VitruvOCLParser.ToIntegerOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'toInteger' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    nodeTypes.put(ctx, Type.INTEGER);
    return Type.INTEGER;
  }

  /**
   * Type checks the {@code toReal()} operation.
   *
   * @param ctx The toReal operation node
   * @return Type.DOUBLE
   */
  @Override
  public Type visitToRealOp(VitruvOCLParser.ToRealOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'toReal' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    nodeTypes.put(ctx, Type.DOUBLE);
    return Type.DOUBLE;
  }

  /**
   * Type checks the {@code characters()} operation.
   *
   * <p>Splits a String into a Sequence of single-character Strings.
   *
   * @param ctx The characters operation node
   * @return Sequence(String)
   */
  @Override
  public Type visitCharactersOp(VitruvOCLParser.CharactersOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'characters' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type resultType = Type.sequence(Type.STRING);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the {@code matches()} operation.
   *
   * <p>Tests whether the receiver matches a regex pattern.
   *
   * @param ctx The matches operation node
   * @return Type.BOOLEAN
   */
  @Override
  public Type visitMatchesOp(VitruvOCLParser.MatchesOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'matches' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "matches() pattern argument must be String, got " + argType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Type checks the {@code substituteAll()} operation.
   *
   * @param ctx The substituteAll operation node
   * @return Type.STRING
   */
  @Override
  public Type visitSubstituteAllOp(VitruvOCLParser.SubstituteAllOpContext ctx) {
    return visitSubstituteOp(ctx, ctx.pattern, ctx.replacement, "substituteAll");
  }

  /**
   * Type checks the {@code substituteFirst()} operation.
   *
   * @param ctx The substituteFirst operation node
   * @return Type.STRING
   */
  @Override
  public Type visitSubstituteFirstOp(VitruvOCLParser.SubstituteFirstOpContext ctx) {
    return visitSubstituteOp(ctx, ctx.pattern, ctx.replacement, "substituteFirst");
  }

  /** Helper for substituteAll / substituteFirst — identical type rules. */
  private Type visitSubstituteOp(
      ParserRuleContext ctx,
      VitruvOCLParser.ExpCSContext patternCtx,
      VitruvOCLParser.ExpCSContext replacementCtx,
      String opName) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'" + opName + "' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type patternType = visit(patternCtx);
    if (!patternType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          opName + "() pattern must be String, got " + patternType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    Type replacementType = visit(replacementCtx);
    if (!replacementType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          opName + "() replacement must be String, got " + replacementType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    nodeTypes.put(ctx, Type.STRING);
    return Type.STRING;
  }

  /**
   * Type checks the {@code tokenize()} operation.
   *
   * <p>Splits the receiver String by a delimiter, returning a Sequence of Strings.
   *
   * @param ctx The tokenize operation node
   * @return Sequence(String)
   */
  @Override
  public Type visitTokenizeOp(VitruvOCLParser.TokenizeOpContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'tokenize' requires String receiver",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Type.ERROR;
    }
    Type argType = visit(ctx.arg);
    if (!argType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "tokenize() delimiter must be String, got " + argType,
          ErrorSeverity.ERROR,
          PHASE_TAG);
    }
    Type resultType = Type.sequence(Type.STRING);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type checks the list of correspondence filter options.
   *
   * <p>Iterates all options, visits each one, and returns the metaclass type found in a Type filter
   * (if any). Tag filters return Type.ANY. If multiple Type filters are present the last one wins
   * (grammar does not forbid it; a warning is issued).
   *
   * @param ctx The correspondence options node
   * @return The resolved target metaclass type, or Type.ANY if only Tag options present
   */
  @Override
  public Type visitCorrespondenceOptions(VitruvOCLParser.CorrespondenceOptionsContext ctx) {
    Type resolvedType = Type.ANY;
    boolean hadTypeFilter = false;

    for (VitruvOCLParser.CorrespondenceOptionContext option : ctx.correspondenceOption()) {
      Type optType = visit(option);
      if (optType != Type.ANY) {
        if (hadTypeFilter) {
          errors.add(
              option.getStart().getLine(),
              option.getStart().getCharPositionInLine(),
              "Duplicate Type filter in correspondence — only one Type filter allowed",
              ErrorSeverity.WARNING,
              PHASE_TAG);
        }
        resolvedType = optType;
        hadTypeFilter = true;
      }
    }

    nodeTypes.put(ctx, resolvedType);
    return resolvedType;
  }

  /**
   * Type checks a 'Type = ...' correspondence filter option.
   *
   * <p>Resolves the given type expression to a metaclass type and validates that it is indeed a
   * metaclass (not a primitive). Returns the resolved metaclass type so the evaluator can use it
   * for instanceof filtering.
   *
   * @param ctx The corrTypeFilter node
   * @return The resolved metaclass type, or Type.ERROR if invalid
   */
  @Override
  public Type visitCorrTypeFilter(VitruvOCLParser.CorrTypeFilterContext ctx) {
    Type targetType = visit(ctx.type);

    if (targetType == Type.ERROR) {
      return Type.ERROR;
    }

    // Unwrap singleton if needed (e.g. !Person! → Person)
    Type baseType = targetType.isSingleton() ? targetType.getElementType() : targetType;

    if (!baseType.isMetaclassType() && baseType != Type.ANY) {
      org.antlr.v4.runtime.Token start = ctx.getStart();
      org.antlr.v4.runtime.Token stop  = ctx.getStop();
      int endCol = (stop != null ? stop.getCharPositionInLine() + stop.getText().length()
                                 : start.getCharPositionInLine() + start.getText().length());
      errors.add(new CompileError(
          start.getLine(), start.getCharPositionInLine(),
          (stop != null ? stop.getLine() : start.getLine()), endCol,
          "Type filter must be a metaclass (e.g. persons::Person), got: " + baseType,
          ErrorSeverity.ERROR, PHASE_TAG, null));
      return Type.ERROR;
    }

    nodeTypes.put(ctx, baseType);
    return baseType;
  }

  /**
   * Type checks a 'Tag = ...' correspondence filter option.
   *
   * <p>Tags are plain string literals — no further type validation needed. Returns Type.ANY to
   * signal to the caller that no metaclass type was produced.
   *
   * @param ctx The corrTagFilter node
   * @return Type.ANY (tag carries no type information)
   */
  @Override
  public Type visitUnknownFilterOption(VitruvOCLParser.UnknownFilterOptionContext ctx) {
    visit(ctx.badVal); // visit value so its own errors surface

    String typed = ctx.badKey.getText();
    java.util.List<String> validKeys = java.util.List.of("Tag", "Type");

    String best = validKeys.stream()
        .min(java.util.Comparator.comparingInt(k ->
            levenshtein(typed.toLowerCase(java.util.Locale.ROOT),
                        k.toLowerCase(java.util.Locale.ROOT))))
        .orElse(null);

    int dist = best == null ? Integer.MAX_VALUE
        : levenshtein(typed.toLowerCase(java.util.Locale.ROOT),
                      best.toLowerCase(java.util.Locale.ROOT));

    String message = dist <= 2
        ? "Unknown filter option '" + typed + ERR_DID_YOU_MEAN + best + "'?"
        : "Unknown filter option '" + typed + "' — valid options are 'Tag' and 'Type'";

    org.antlr.v4.runtime.Token tok = ctx.badKey;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        message, ErrorSeverity.ERROR, PHASE_TAG, null, best));

    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  @Override
  public Type visitInvalidFilterArg(VitruvOCLParser.InvalidFilterArgContext ctx) {
    org.antlr.v4.runtime.Token tok = ctx.badArg;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        "Invalid filter argument '" + tok.getText() + "' — expected 'Type = <type>' or 'Tag = <string>'",
        ErrorSeverity.ERROR, PHASE_TAG, null));
    return Type.ERROR;
  }

  @Override
  public Type visitCorrTagFilter(VitruvOCLParser.CorrTagFilterContext ctx) {
    Type tagType = visit(ctx.tag);
    // Unwrap singleton wrapper to compare bare primitive type
    Type bare = (tagType != null && tagType.isSingleton()) ? tagType.getElementType() : tagType;
    if (bare != null && bare != Type.STRING && bare != Type.ANY && bare != Type.ERROR) {
      org.antlr.v4.runtime.Token start = ctx.getStart();
      org.antlr.v4.runtime.Token stop  = ctx.getStop();
      int endCol = (stop != null ? stop.getCharPositionInLine() + stop.getText().length()
                                 : start.getCharPositionInLine() + start.getText().length());
      errors.add(new CompileError(
          start.getLine(), start.getCharPositionInLine(),
          (stop != null ? stop.getLine() : start.getLine()), endCol,
          "Tag filter value must be String, got " + bare,
          ErrorSeverity.ERROR, PHASE_TAG, null));
    }
    nodeTypes.put(ctx, Type.ANY);
    return Type.ANY;
  }

  /**
   * Visits a type literal expression node in the AST.
   *
   * <p>This method delegates type checking to the underlying type literal node. The type literal
   * represents a type as an expression (e.g., a class or primitive type).
   *
   * @param ctx the context of the type literal expression node in the AST
   * @return the type of the underlying type literal
   */
  @Override
  public Type visitTypeLiteralExpCS(VitruvOCLParser.TypeLiteralExpCSContext ctx) {
    return visit(ctx.typeLiteralCS());
  }

  /**
   * Visits a property access node in the AST.
   *
   * <p>This method performs type checking for property access expressions, e.g., accessing a field
   * or attribute of an object or element of a collection.
   *
   * @param ctx the context of the property access node in the AST
   * @return the type of the accessed property
   */
  @Override
  public Type visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    Type receiverType = receiverStack.peek();
    if (receiverType == Type.ERROR) return Type.ERROR; // already reported upstream
    String propertyName = ctx.propertyName.getText();
    Type resultType = typeCheckPropertyAccess(receiverType, propertyName, ctx.propertyName);
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Visits a collection operation node in the AST.
   *
   * <p>Delegates type checking to the underlying collection operation expression.
   *
   * @param ctx the context of the collection operation node in the AST
   * @return the type of the collection operation expression
   */
  @Override
  public Type visitCollectionOperation(VitruvOCLParser.CollectionOperationContext ctx) {
    return visit(ctx.collectionOpCS());
  }

  /**
   * Visits a string operation node in the AST.
   *
   * <p>Delegates type checking to the underlying string operation expression.
   *
   * @param ctx the context of the string operation node in the AST
   * @return the type of the string operation expression
   */
  @Override
  public Type visitStringOperation(VitruvOCLParser.StringOperationContext ctx) {
    return visit(ctx.stringOpCS());
  }

  /**
   * Visits an iterator operation node in the AST.
   *
   * <p>Delegates type checking to the underlying iterator operation expression.
   *
   * @param ctx the context of the iterator operation node in the AST
   * @return the type of the iterator operation expression
   */
  @Override
  public Type visitIteratorOperation(VitruvOCLParser.IteratorOperationContext ctx) {
    return visit(ctx.iteratorOpCS());
  }

  /**
   * Visits a type operation node in the AST.
   *
   * <p>Delegates type checking to the underlying type operation expression.
   *
   * @param ctx the context of the type operation node in the AST
   * @return the type of the type operation expression
   */
  @Override
  public Type visitTypeOperation(VitruvOCLParser.TypeOperationContext ctx) {
    return visit(ctx.typeOpCS());
  }

  // ==================== Unknown Operator Catch-All ====================

  /** Correspondence iterator operations — candidates for unknownCorrOp suggestions. */
  private static final java.util.List<String> CORR_OPS =
      java.util.List.of(OP_SELECT, OP_REJECT, OP_EXISTS);

  /**
   * Reports a precise error when a correspondence-style call like {@code selcft(~, ...)} is used
   * but the operation name is not one of the known correspondence iterators.
   *
   * <p>Without this rule the grammar would try to parse the {@code ~} as an expression argument
   * inside {@code unknownOpCS} and produce a cryptic "mismatched input '~' expecting {...}"
   * message. This alternative catches the pattern before that happens.
   */
  @Override
  public Type visitUnknownCorrOp(VitruvOCLParser.UnknownCorrOpContext ctx) {
    // Still validate the filter so that Tag/Type errors are reported correctly.
    visit(ctx.corrFilter);

    String typed = ctx.opName.getText();
    String typedLow = typed.toLowerCase(java.util.Locale.ROOT);

    String best = CORR_OPS.stream()
        .min(java.util.Comparator.comparingInt(k ->
            levenshtein(typedLow, k.toLowerCase(java.util.Locale.ROOT))))
        .orElse(null);
    int dist = best == null ? Integer.MAX_VALUE
        : levenshtein(typedLow, best.toLowerCase(java.util.Locale.ROOT));

    int threshold = editThreshold(typed.length());
    String message = dist <= threshold
        ? ERR_UNKNOWN_OP + typed + ERR_DID_YOU_MEAN + best + "'?"
        : ERR_UNKNOWN_OP + typed + ERR_OP_DOES_NOT_EXIST
          + " (valid correspondence ops: select, reject, exists)";

    org.antlr.v4.runtime.Token tok = ctx.opName;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        message, ErrorSeverity.ERROR, PHASE_TAG, null, best));

    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  /** Keyword binary operators — candidates for "did you mean?" suggestions. */
  private static final java.util.List<String> KNOWN_BINARY_OPS =
      java.util.List.of("and", "or", "xor", "implies");

  @Override
  public Type visitUnknownBinaryOp(VitruvOCLParser.UnknownBinaryOpContext ctx) {
    visit(ctx.left);
    visit(ctx.right);

    String typed = ctx.op.getText();
    String typedLow = typed.toLowerCase(java.util.Locale.ROOT);

    // Find the closest keyword operator (case-insensitive Levenshtein)
    String best = KNOWN_BINARY_OPS.stream()
        .min(java.util.Comparator.comparingInt(k ->
            levenshtein(typedLow, k.toLowerCase(java.util.Locale.ROOT))))
        .orElse(null);
    int dist = best == null ? Integer.MAX_VALUE
        : levenshtein(typedLow, best.toLowerCase(java.util.Locale.ROOT));

    // Use "did you mean" when the name is close enough; threshold scales with length
    int threshold = editThreshold(typed.length());
    String message;
    String suggestion;
    if (dist <= threshold) {
      message    = "Unknown operator '" + typed + ERR_DID_YOU_MEAN + best + "'?";
      suggestion = best;
    } else {
      message    = "Unknown operator '" + typed + "' — this operator does not exist";
      suggestion = best; // still offer a Quick Fix to the closest keyword op
    }

    org.antlr.v4.runtime.Token tok = ctx.op;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        message, ErrorSeverity.ERROR, PHASE_TAG, null, suggestion));

    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  @Override
  public Type visitUnknownOperation(VitruvOCLParser.UnknownOperationContext ctx) {
    return visit(ctx.unknownOpCS());
  }

  @Override
  public Type visitUnknownOpCS(VitruvOCLParser.UnknownOpCSContext ctx) {
    for (VitruvOCLParser.ExpCSContext arg : ctx.args) {
      visit(arg);
    }
    String typed = ctx.opName.getText();
    java.util.Optional<String> close = suggestOperation(typed);   // within threshold
    String best  = bestOperation(typed);                           // unconditional best

    String message;
    String suggestion; // stored for Quick Fix — always the best candidate
    if (close.isPresent()) {
      // Close enough: "did you mean?"
      message    = ERR_UNKNOWN_OP + typed + ERR_DID_YOU_MEAN + close.get() + "'?";
      suggestion = close.get();
    } else if (best != null) {
      // Too far for a confident suggestion, but we still offer a fix in the tooltip
      message    = ERR_UNKNOWN_OP + typed + ERR_OP_DOES_NOT_EXIST;
      suggestion = best;
    } else {
      message    = ERR_UNKNOWN_OP + typed + ERR_OP_DOES_NOT_EXIST;
      suggestion = null;
    }

    org.antlr.v4.runtime.Token tok = ctx.opName;
    int endCol = tok.getCharPositionInLine() + tok.getText().length();
    errors.add(new CompileError(
        tok.getLine(), tok.getCharPositionInLine(),
        tok.getLine(), endCol,
        message, ErrorSeverity.ERROR, PHASE_TAG, null, suggestion));

    nodeTypes.put(ctx, Type.ERROR);
    return Type.ERROR;
  }

  // ==================== Operation Name Suggestion ====================

  /**
   * All operation names known to VitruvOCL.
   *
   * <p>Used by {@link #suggestOperation} to propose the closest match when an unknown operation
   * name is encountered.
   */
  private static final java.util.List<String> KNOWN_OPERATIONS = java.util.List.of(
      // Collection query
      "isEmpty", "notEmpty", "size", "count", "includes", "excludes",
      "includesAll", "excludesAll", "first", "last", "at", "reverse",
      // Collection modification
      "including", "excluding", "union", "intersection", "symmetricDifference",
      "flatten", "append", "prepend", "insertAt", "subSequence",
      // Collection conversion
      OP_AS_SET, OP_AS_BAG, OP_AS_SEQUENCE, OP_AS_ORDERED_SET, "lift",
      // Aggregation
      "sum", "max", "min", "avg",
      // Numeric
      "abs", OP_FLOOR, "ceil", OP_CEILING, OP_ROUND,
      // Iterator
      OP_SELECT, OP_REJECT, "collect", "collectNested",
      "forAll", OP_EXISTS, "one", "any",
      "isUnique", "sortedBy", "iterate",
      // String
      "concat", "substring", "length", "toUpper", "toLower",
      "indexOf", "equalsIgnoreCase", "characters", "tokenize",
      "substituteAll", "substituteFirst", "matches",
      "toInteger", "toReal",
      // Type / meta
      "oclIsKindOf", "oclIsTypeOf", "oclAsType", "allInstances"
  );

  /**
   * Returns the single closest known operation name to {@code typed}, regardless of distance.
   *
   * <p>Used to populate the Quick Fix even when the name is too different for a confident "did you
   * mean?" message. Returns {@code null} only when {@code KNOWN_OPERATIONS} is empty.
   */
  private static String bestOperation(String typed) {
    String lower = typed.toLowerCase(java.util.Locale.ROOT);
    String best = null;
    int bestDist = Integer.MAX_VALUE;
    for (String known : KNOWN_OPERATIONS) {
      int dist = levenshtein(lower, known.toLowerCase(java.util.Locale.ROOT));
      if (dist < bestDist) {
        bestDist = dist;
        best = known;
      }
    }
    return best;
  }

  /**
   * Returns the closest known operation name to {@code typed} if it is within the edit-distance
   * threshold, otherwise returns empty.
   *
   * <p>Threshold scales with name length:
   *
   * <ul>
   *   <li>1–3 chars → max distance 1
   *   <li>4–6 chars → max distance 2
   *   <li>7+ chars  → max distance 3
   * </ul>
   *
   * <p>Comparison is case-insensitive so "Select" suggests "select".
   */
  private static java.util.Optional<String> suggestOperation(String typed) {
    int threshold = editThreshold(typed.length());
    String lower = typed.toLowerCase(java.util.Locale.ROOT);

    String best = null;
    int bestDist = threshold + 1;

    for (String known : KNOWN_OPERATIONS) {
      int dist = levenshtein(lower, known.toLowerCase(java.util.Locale.ROOT));
      if (dist < bestDist) {
        bestDist = dist;
        best = known;
      }
    }

    return java.util.Optional.ofNullable(best);
  }

  /**
   * Computes the Damerau-Levenshtein (Optimal String Alignment) distance between two strings.
   *
   * <p>Counts insertions, deletions, substitutions and <em>adjacent transpositions</em> (e.g.
   * {@code "adn"} → {@code "and"} = 1 move). This is better than plain Levenshtein for catching
   * real typing mistakes where two neighbouring characters are accidentally swapped.
   *
   * <p>Time O(|a|·|b|), space O(|a|·|b|).
   */
  private static int levenshtein(String a, String b) {
    if (a.equals(b)) return 0;
    if (a.isEmpty()) return b.length();
    if (b.isEmpty()) return a.length();

    int la = a.length(), lb = b.length();
    int[][] d = new int[la + 1][lb + 1];

    for (int i = 0; i <= la; i++) d[i][0] = i;
    for (int j = 0; j <= lb; j++) d[0][j] = j;

    for (int i = 1; i <= la; i++) {
      for (int j = 1; j <= lb; j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(d[i - 1][j] + 1,                 // deletion
                  Math.min(d[i][j - 1] + 1,                  // insertion
                           d[i - 1][j - 1] + cost));          // substitution
        // Adjacent transposition (Damerau extension)
        if (i > 1 && j > 1
            && a.charAt(i - 1) == b.charAt(j - 2)
            && a.charAt(i - 2) == b.charAt(j - 1)) {
          d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost);
        }
      }
    }

    return d[la][lb];
  }

  /** Recursively checks whether any node in the subtree is an ANTLR {@link ErrorNode}. */
  private static boolean hasErrorNode(ParseTree tree) {
    if (tree instanceof ErrorNode) return true;
    if (tree instanceof org.antlr.v4.runtime.ParserRuleContext
        && ((org.antlr.v4.runtime.ParserRuleContext) tree).exception != null) return true;
    for (int i = 0; i < tree.getChildCount(); i++) {
      if (hasErrorNode(tree.getChild(i))) return true;
    }
    return false;
  }

  /**
   * Visits a 'onespace' lexical token in the AST.
   *
   * <p>This node represents a whitespace token and has type {@link Type#ANY}.
   *
   * @param ctx the context of the onespace token in the AST
   * @return {@link Type#ANY} since it represents a lexical token
   */
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
    // Eagerly fill the token buffer so all tokens are accessible by index,
    // even tokens that ANTLR's error recovery consumed (or skipped) during parsing.
    if (tokens instanceof org.antlr.v4.runtime.BufferedTokenStream) {
      ((org.antlr.v4.runtime.BufferedTokenStream) tokens).fill();
    }
  }

  /**
   * Normalizes a bare type to it singleton ctype ¡T!.
   *
   * <p>every expression has a ctype χ = τ[l,r](μ,ω). Bare primitive types (INTEGER, STRING, etc.)
   * and bare metaclass types are implicitly ¡T![1,1]. This method makes that wrapping explicit so
   * all downstream operations can rely on it. Multi-valued collections ({T}, [T], etc.) are
   * returned unchanged since they are already proper ctypes.
   *
   * @param t the type to normalize
   * @return ¡t! if t is a bare scalar or metaclass, t unchanged if already a proper ctype
   */
  private Type normalizeToSingleton(Type t) {
    if (t == Type.ERROR || t == Type.ANY) {
      return t;
    }
    if (t.isCollection()) {
      return t;
    } // {T}, [T], <T>, {{T}} — already proper ctype
    if (t.isSingleton()) {
      return t;
    } // !T! — already wrapped
    if (t.isOptional()) {
      return t;
    } // ?T? — already wrapped
    return Type.singleton(t); // bare INTEGER, STRING, cad::Sphere → !T!
  }

  /**
   * Checks conformance with singleton-unwrapping rules at let-binding sites.
   *
   * <p>A singleton {@code !T!} can be bound to a variable declared as T, and vice versa. Also
   * handles optional {@code ?T?} binding to T.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code !cad::Box!} conforms to {@code cad::Box} (oclAsType result in let)
   *   <li>{@code cad::Box} conforms to {@code !cad::Box!} (bare type to singleton decl)
   *   <li>{@code ?Any?} conforms to {@code cad::Coordinate} (null comparison context)
   * </ul>
   *
   * @param initType the type of the initializer expression
   * @param declared the declared type of the variable
   * @return true if initType conforms to declared after unwrapping rules
   */
  private boolean conformsWithUnwrapping(Type initType, Type declared) {
    // !T! conforms to T  (e.g. oclAsType returns !Box!, declared as cad::Box)
    if (initType.isSingleton() && initType.getElementType().isConformantTo(declared)) {
      return true;
    }
    // T conforms to !T!  (bare type assigned to singleton-declared variable)
    if (declared.isSingleton() && initType.isConformantTo(declared.getElementType())) {
      return true;
    }
    // ?T? conforms to T  (optional binding, e.g. null comparison context)
    if (initType.isOptional() && initType.getElementType().isConformantTo(declared)) {
      return true;
    }
    return false;
  }
}

