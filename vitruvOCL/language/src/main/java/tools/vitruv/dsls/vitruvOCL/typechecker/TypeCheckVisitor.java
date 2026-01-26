package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionArgumentsContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionLiteralExpCSContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionLiteralPartCSContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionTypeCSContext;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintSpecification;
import tools.vitruv.dsls.vitruvOCL.symboltable.LocalScope;
import tools.vitruv.dsls.vitruvOCL.symboltable.Symbol;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.TypeSymbol;
import tools.vitruv.dsls.vitruvOCL.symboltable.VariableSymbol;

/**
 * Visitor for type checking
 *
 * <p>purpose: validate type rules and annotations from parse Tree
 *
 * <p>output: @link ParseTreeProperty<Type>
 *
 * <p>error handling: collects errors in ErrorCollector
 *
 * @see AbstractPhaseVisitor parent Visitor class
 * @see Type Type System
 * @see TypeResolver helper for Type Resolution
 */
public class TypeCheckVisitor extends AbstractPhaseVisitor<Type> {

  private final ParseTreeProperty<Type> nodeTypes = new ParseTreeProperty<>();
  private final SymbolTable symbolTable;
  private final ConstraintSpecification specification;
  private final TypeRegistry typeRegistry;

  private org.antlr.v4.runtime.TokenStream tokens;

  public TypeCheckVisitor(
      SymbolTable symbolTable, ConstraintSpecification specification, ErrorCollector errors) {
    super(symbolTable, specification, errors);
    this.typeRegistry = new TypeRegistry(specification);
    this.symbolTable = symbolTable;
    this.specification = specification;
  }

  public ParseTreeProperty<Type> getNodeTypes() {
    return nodeTypes;
  }

  public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
    this.tokens = tokens;
  }

  @Override
  protected void handleUndefinedSymbol(String name, org.antlr.v4.runtime.ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Undefined symbol: " + name,
        ErrorSeverity.ERROR,
        "type-checker");
  }

  @Override
  public Type visitBoolean(VitruvOCLParser.BooleanContext ctx) {
    Type type = Type.BOOLEAN;
    nodeTypes.put(ctx, type);
    return type;
  }

  @Override
  public Type visitString(VitruvOCLParser.StringContext ctx) {
    Type type = Type.STRING;
    nodeTypes.put(ctx, type);
    return type;
  }

  @Override
  public Type visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    // Evaluate the expression inside the parentheses
    // nestedExpCS can have multiple expressions, we take the last one
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

    // Type-check all expressions, return type of last one
    Type resultType = null;
    for (VitruvOCLParser.ExpCSContext exp : exps) {
      resultType = visit(exp);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Binary Operators ====================

  @Override
  public Type visitNumber(VitruvOCLParser.NumberContext ctx) {
    Type type = Type.INTEGER;
    nodeTypes.put(ctx, type);
    return type;
  }

  @Override
  public Type visitPlusMinus(VitruvOCLParser.PlusMinusContext ctx) {
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

  @Override
  public Type visitTimesDivide(VitruvOCLParser.TimesDivideContext ctx) {
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

  @Override
  public Type visitPrefixedExp(VitruvOCLParser.PrefixedExpContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  @Override
  public Type visitAndOrXor(VitruvOCLParser.AndOrXorContext ctx) {
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

  /** Type-checks 'implies' operator. */
  @Override
  public Type visitImplies(VitruvOCLParser.ImpliesContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    Type resultType = Type.BOOLEAN;

    // Both operands must be Boolean
    if (!leftType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Left operand of 'implies' must be Boolean, got " + leftType,
          ErrorSeverity.ERROR,
          "type-checker");
      resultType = Type.ERROR;
    }

    if (!rightType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Right operand of 'implies' must be Boolean, got " + rightType,
          ErrorSeverity.ERROR,
          "type-checker");
      resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  // ==================== Comparison Operators ====================

  /** Type-checks equality and ordering operations: =, <>, <, <=, >, >= */
  @Override
  public Type visitEqualOperations(VitruvOCLParser.EqualOperationsContext ctx) {
    Type leftType = visit(ctx.left);
    Type rightType = visit(ctx.right);

    String operator = ctx.op.getText();

    // All comparison operators return Boolean
    Type resultType = Type.BOOLEAN;

    // Type check: operands must be comparable
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

  /** Checks if two types are comparable for equality/ordering. */
  private boolean areComparable(Type t1, Type t2) {
    // ERROR types are always compatible (error recovery)
    if (t1 == Type.ERROR || t2 == Type.ERROR) return true;

    // Same types are always comparable
    if (t1.equals(t2)) return true;

    // Check conformance in both directions
    if (t1.isConformantTo(t2) || t2.isConformantTo(t1)) return true;

    return false;
  }

  // ==================== Collections ====================

  /** Type-checks collection type declarations. Example: Set, Sequence, Bag, OrderedSet */
  @Override
  public Type visitCollectionTypeCS(CollectionTypeCSContext ctx) {
    // Extract collection kind
    String kind = null;
    if (ctx.collectionTypeIDentifier() != null) {
      kind = ctx.collectionTypeIDentifier().getText();
    } else if (ctx.collectionType != null) {
      kind = ctx.collectionType.getText();
    }

    if (kind == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unable to determine collection type",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    // Create the collection type **with ANY as placeholder for elementType**
    Type collectionType =
        switch (kind) {
          case "Set" -> Type.set(Type.ANY);
          case "Sequence" -> Type.sequence(Type.ANY);
          case "Bag" -> Type.bag(Type.ANY);
          case "OrderedSet" -> Type.orderedSet(Type.ANY);
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

    // Store type in nodeTypes (optional: placeholder)
    nodeTypes.put(ctx, collectionType);
    return collectionType;
  }

  /** Type-checks collection literals. Example: Set{1, 2, 3}, Sequence{1..10} */
  @Override
  public Type visitCollectionLiteralExpCS(CollectionLiteralExpCSContext ctx) {
    // Visit the collection type to get the collection kind (Set, Sequence, etc.)
    Type collectionType = visit(ctx.collectionTypeCS());

    if (collectionType == null || collectionType == Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Unable to determine collection type",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Empty collection: Set{} - keep ANY as element type
    if (ctx.argument == null) {
      nodeTypes.put(ctx, collectionType);
      return collectionType;
    }

    // Type-check arguments and infer actual element type
    Type inferredElementType = visit(ctx.argument);

    if (inferredElementType == null || inferredElementType == Type.ERROR) {
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Create NEW collection type with INFERRED element type
    Type resultType;
    if (collectionType.isUnique() && collectionType.isOrdered()) {
      resultType = Type.orderedSet(inferredElementType);
    } else if (collectionType.isUnique()) {
      resultType = Type.set(inferredElementType);
    } else if (collectionType.isOrdered()) {
      resultType = Type.sequence(inferredElementType);
    } else {
      resultType = Type.bag(inferredElementType);
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /** Type-checks single collection literal part. Can be a single element or a range (1..10). */
  @Override
  public Type visitCollectionLiteralPartCS(CollectionLiteralPartCSContext ctx) {
    Type firstType = visit(ctx.expCS(0));

    // Check if it's a range (1..10)
    if (ctx.expCS().size() == 2) {
      Type secondType = visit(ctx.expCS(1));

      // Both must be integers for ranges
      if (!firstType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Range start must be Integer, got " + firstType,
            ErrorSeverity.ERROR,
            "type-checker");
      }
      if (!secondType.isConformantTo(Type.INTEGER)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Range end must be Integer, got " + secondType,
            ErrorSeverity.ERROR,
            "type-checker");
      }

      return Type.INTEGER;
    }

    // Single element
    return firstType;
  }

  /** Type-checks collection arguments. Validates that all elements have compatible types. */
  @Override
  public Type visitCollectionArguments(CollectionArgumentsContext ctx) {
    Type commonType = null;

    for (CollectionLiteralPartCSContext partCtx : ctx.collectionLiteralPartCS()) {
      Type partType = visit(partCtx);

      if (partType == Type.ERROR) {
        continue;
      }

      if (commonType == null) {
        commonType = partType;
      } else if (!partType.equals(commonType)) {
        // Types differ - find common supertype
        Type superType = Type.commonSuperType(commonType, partType);

        if (superType != null) {
          commonType = superType;
        } else {
          // No common type - fall back to ANY
          commonType = Type.ANY;
        }
      }
    }

    return commonType != null ? commonType : Type.ANY;
  }

  /**
   * Extracts the operation/property name from a navigatingArgExpCS. This is typically a simple
   * identifier or qualified name.
   */
  private String extractOperationName(VitruvOCLParser.NavigatingArgExpCSContext ctx) {
    if (ctx.nameExpCS() != null) {
      return ctx.nameExpCS().getText();
    }
    return ctx.getText();
  }

  /**
   * Type-checks a primary expression with an explicit receiver type. Used for metamodel navigation
   * like UML::Class.allInstances()
   */
  private Type visitPrimaryExpCSWithReceiver(
      VitruvOCLParser.PrimaryExpCSContext ctx, Type receiverType) {
    if (ctx.navigatingExpCS() != null) {
      return visitNavigatingExpCSWithReceiver(ctx.navigatingExpCS(), receiverType);
    }

    // Other primary expressions don't use receiver
    return visit(ctx);
  }

  /** Type-checks a navigating expression with an explicit receiver type. */
  private Type visitNavigatingExpCSWithReceiver(
      VitruvOCLParser.NavigatingExpCSContext ctx, Type receiverType) {
    String opName = ctx.indexExpCS().nameExpCS().getText();

    // Handle metaclass operations
    if (receiverType.isMetaclassType()) {
      switch (opName) {
        case "allInstances":
          // MetaClass.allInstances() : Set(MetaClass)
          Type resultType = Type.set(receiverType);
          nodeTypes.put(ctx, resultType);
          return resultType;

        default:
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Unknown operation '" + opName + "' on metaclass type",
              ErrorSeverity.ERROR,
              "type-checker");
          nodeTypes.put(ctx, Type.ERROR);
          return Type.ERROR;
      }
    }

    // Fall back to normal navigation
    return visit(ctx);
  }

  /**
   * Handles navigation chains and unary operators: -5, not true, Set{1,2}.size()
   *
   * <p>For Collection Operations like including/excluding, we need the receiver type which is only
   * available at this level of the parse tree.
   */
  @Override
  public Type visitPrefixedExpCS(VitruvOCLParser.PrefixedExpCSContext ctx) {
    // Handle Metamodel::Class.operation() pattern
    if (ctx.metamodel != null && ctx.className != null) {
      String qualifiedName = ctx.metamodel.getText() + "::" + ctx.className.getText();
      Type metaclassType = symbolTable.lookupType(qualifiedName);

      if (metaclassType == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown metamodel type: " + qualifiedName,
            ErrorSeverity.ERROR,
            "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
      }

      // Store the metaclass type as receiver
      Type currentType = metaclassType;

      // Type-check navigation operations
      for (VitruvOCLParser.PrimaryExpCSContext primary : ctx.primaryExpCS()) {
        currentType = visitPrimaryExpCSWithReceiver(primary, currentType);
      }

      nodeTypes.put(ctx, currentType);
      return currentType;
    }
    // Count how many unary operators we have at the start
    int unaryOpCount = 0;
    for (int i = 0; i < ctx.getChildCount(); i++) {
      String text = ctx.getChild(i).getText();
      if (text.equals("-") || text.equals("not")) {
        unaryOpCount++;
      } else {
        break; // Stop at first non-operator
      }
    }

    // Get all primaries
    List<VitruvOCLParser.PrimaryExpCSContext> primaries = ctx.primaryExpCS();

    if (primaries.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Empty expression",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Start with first primary
    Type currentType = visit(primaries.get(0));

    // Type-check unary operators
    for (int i = 0; i < unaryOpCount; i++) {
      String op = ctx.getChild(i).getText();

      if (op.equals("-")) {
        if (!currentType.isConformantTo(Type.INTEGER)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Unary minus requires numeric type, got " + currentType,
              ErrorSeverity.ERROR,
              "type-checker");
          currentType = Type.ERROR;
        }
        // Result type stays INTEGER
      } else if (op.equals("not")) {
        if (!currentType.isConformantTo(Type.BOOLEAN)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Unary not requires boolean type, got " + currentType,
              ErrorSeverity.ERROR,
              "type-checker");
          currentType = Type.ERROR;
        }
        // Result type stays BOOLEAN
      }
    }

    // Handle navigation chain (if there are more primaries)
    for (int i = 1; i < primaries.size(); i++) {
      VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);

      // Check if this is a navigatingExpCS (method call)
      if (operationCtx.navigatingExpCS() != null) {
        VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();

        // Check if operation name is a collectionOperationName OR stringOperationName
        if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
          VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();

          // CRITICAL: Cast to NameContext to access collectionOperationName()
          if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
            // Collection operations
            if (nameContext.collectionOperationName() != null) {
              String opName = nameContext.collectionOperationName().getText();
              currentType = typeCheckCollectionOperation(navCtx, opName, currentType);
              continue;
            }

            // String operations
            if (nameContext.stringOperationName() != null) {
              String opName = nameContext.stringOperationName().getText();
              currentType = typeCheckStringOperation(navCtx, opName, currentType);
              continue;
            }
          }
        }

        // Check for no-arg operations (size, isEmpty, etc.)
        if (navCtx.navigatingArgCS().isEmpty() && navCtx.opName != null) {
          String opName = navCtx.opName.nameExpCS().getText();

          switch (opName) {
            case "size":
              currentType = Type.INTEGER;
              break;
            case "isEmpty":
            case "notEmpty":
              currentType = Type.BOOLEAN;
              break;
            case "first":
            case "last":
              if (currentType.isCollection()) {
                currentType = currentType.getElementType();
              } else {
                errors.add(
                    navCtx.getStart().getLine(),
                    navCtx.getStart().getCharPositionInLine(),
                    "Operation '" + opName + "' requires collection type",
                    ErrorSeverity.ERROR,
                    "type-checker");
                currentType = Type.ERROR;
              }
              break;
            case "reverse":
              if (!currentType.isCollection() || !currentType.isOrdered()) {
                errors.add(
                    navCtx.getStart().getLine(),
                    navCtx.getStart().getCharPositionInLine(),
                    "Operation 'reverse' requires ordered collection",
                    ErrorSeverity.ERROR,
                    "type-checker");
                currentType = Type.ERROR;
              }
              // Type stays the same
              break;
            case "flatten":
              if (!currentType.isCollection()) {
                errors.add(
                    navCtx.getStart().getLine(),
                    navCtx.getStart().getCharPositionInLine(),
                    "Operation 'flatten' requires collection",
                    ErrorSeverity.ERROR,
                    "type-checker");
                currentType = Type.ERROR;
              } else {
                Type innerType = currentType.getElementType();
                if (innerType.isCollection()) {
                  currentType = innerType; // Flatten one level
                }
              }
              break;
            default:
              // Normal navigation - delegate to existing logic
              currentType = visit(operationCtx);
          }
        } else {
          // Has arguments or not a special operation - delegate
          currentType = visit(operationCtx);
        }
      } else {
        // Other primary expression types
        currentType = visit(operationCtx);
      }
    }

    nodeTypes.put(ctx, currentType);
    return currentType;
  }

  @Override
  public Type visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    // Resolve context type (Metamodel::Class or unqualified Class)
    Type contextType;
    if (ctx.metamodel != null && ctx.className != null) {
      String qualifiedName = ctx.metamodel.getText() + "::" + ctx.className.getText();
      contextType = symbolTable.lookupType(qualifiedName);
    } else if (ctx.contextName != null) {
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

    // Store context type for invariants
    nodeTypes.put(ctx, contextType);

    // Type-check all invariants with this context
    for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
      visit(inv);
    }

    return contextType;
  }

  @Override
  public Type visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    // Get context type from parent
    Type contextType = nodeTypes.get(ctx.getParent());

    // Type-check the invariant expression (should be Boolean)
    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Type invType = Type.BOOLEAN;

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      invType = visit(spec);
    }

    if (!invType.isConformantTo(Type.BOOLEAN) && invType != Type.ERROR) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "Invariant must be Boolean expression, got " + invType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    nodeTypes.put(ctx, Type.BOOLEAN);
    return Type.BOOLEAN;
  }

  /**
   * Type-checks collection operations: including, excluding, flatten, select, reject, etc.
   *
   * @param ctx Navigation context containing the operation
   * @param opName Operation name (including, excluding, etc.)
   * @param receiverType Type of the receiver collection
   * @return Result type of the operation
   */
  private Type typeCheckCollectionOperation(
      VitruvOCLParser.NavigatingExpCSContext ctx, String opName, Type receiverType) {
    Type resultType;

    switch (opName) {
      case "including":
      case "excluding":
        // including(x) / excluding(x): Collection(T) → Collection(T)
        // Argument must be compatible with element type

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
        if (args.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires 1 argument",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check argument
        Type argType = visit(args.get(0).navigatingArgExpCS());
        Type elemType = receiverType.getElementType();

        if (argType != null && argType != Type.ERROR && !argType.isConformantTo(elemType)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Argument type "
                  + argType
                  + " not compatible with collection element type "
                  + elemType,
              ErrorSeverity.ERROR,
              "type-checker");
        }

        // Result has same collection type as receiver
        resultType = receiverType;
        break;

      case "includes":
      case "excludes":
        // includes(x) / excludes(x): Collection(T) → Boolean

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        args = ctx.navigatingArgCS();
        if (args.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires 1 argument",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check argument
        visit(args.get(0).navigatingArgExpCS());

        resultType = Type.BOOLEAN;
        break;

      case "flatten":
        // flatten(): Collection(Collection(T)) → Collection(T)

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'flatten' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        Type innerElemType = receiverType.getElementType();
        if (!innerElemType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'flatten' requires Collection(Collection(T)), got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Preserve collection kind: Set{Set{1}} → Set{1}
        Type flatElementType = innerElemType.getElementType();
        if (receiverType.isUnique() && receiverType.isOrdered()) {
          resultType = Type.orderedSet(flatElementType);
        } else if (receiverType.isUnique()) {
          resultType = Type.set(flatElementType);
        } else if (receiverType.isOrdered()) {
          resultType = Type.sequence(flatElementType);
        } else {
          resultType = Type.bag(flatElementType);
        }
        break;

      case "union":
      case "append":
        // union(collection) / append(collection): Collection(T) × Collection(T) →
        // Collection(T)

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        args = ctx.navigatingArgCS();
        if (args.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires 1 argument",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check argument (must be collection)
        Type argCollType = visit(args.get(0).navigatingArgExpCS());

        if (argCollType != null && argCollType != Type.ERROR && !argCollType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Argument must be a collection, got " + argCollType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Compute common element type
        Type commonElemType =
            Type.commonSuperType(receiverType.getElementType(), argCollType.getElementType());

        // Determine result collection type based on uniqueness/ordering
        // union: Set ∪ Set = Set, Sequence ∪ Sequence = Sequence
        boolean bothUnique = receiverType.isUnique() && argCollType.isUnique();
        boolean anyOrdered = receiverType.isOrdered() || argCollType.isOrdered();

        if (bothUnique && anyOrdered) {
          resultType = Type.orderedSet(commonElemType);
        } else if (bothUnique) {
          resultType = Type.set(commonElemType);
        } else if (anyOrdered) {
          resultType = Type.sequence(commonElemType);
        } else {
          resultType = Type.bag(commonElemType);
        }
        break;

      case "sum":
      case "max":
      case "min":
      case "avg":
        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        Type elemTypes = receiverType.getElementType();

        // Allow ANY for empty collections
        if (elemTypes != Type.ANY && !elemTypes.isConformantTo(Type.INTEGER)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires numeric collection, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        resultType = Type.set(Type.INTEGER);
        break;

      case "abs":
      case "floor":
      case "ceil":
      case "round":
        // Per-element numeric operations: Collection(Number) → Collection(Number)
        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        Type elem = receiverType.getElementType();
        if (!elem.isConformantTo(Type.INTEGER)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires numeric collection, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Preserve collection type
        resultType = receiverType;
        break;

      case "oclIsKindOf":
        // oclIsKindOf(TypeName) : Collection(T) → Collection(Boolean)

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'oclIsKindOf' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        List<VitruvOCLParser.NavigatingArgCSContext> argsKindOF = ctx.navigatingArgCS();
        if (argsKindOF.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'oclIsKindOf' requires 1 type argument",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (argsKindOF.size() > 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'oclIsKindOf' expects exactly 1 argument, got " + argsKindOF.size(),
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Resolve the type argument (Integer, String, Boolean, UML::Class, etc.)
        VitruvOCLParser.NavigatingArgCSContext argCtx = argsKindOF.get(0);
        Type argumentType = visit(argCtx.navigatingArgExpCS());

        // For oclIsKindOf, we allow unknown types (they will resolve to false at runtime)
        // So we DON'T throw an error if argumentType is ERROR or null
        // We still store it for the evaluation phase to handle
        if (argumentType != null && argumentType != Type.ERROR) {
          nodeTypes.put(argCtx, argumentType);
        }
        // Note: If argumentType is ERROR, the evaluator will try to resolve it
        // and return false if it can't be resolved

        // Result: Collection(Boolean) - preserve collection kind
        if (receiverType.isUnique() && receiverType.isOrdered()) {
          resultType = Type.orderedSet(Type.BOOLEAN);
        } else if (receiverType.isUnique()) {
          resultType = Type.set(Type.BOOLEAN);
        } else if (receiverType.isOrdered()) {
          resultType = Type.sequence(Type.BOOLEAN);
        } else {
          resultType = Type.bag(Type.BOOLEAN);
        }
        break;
      case "lift":
        // lift(): Collection(T) → Collection(Collection(T))
        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'lift' requires collection receiver",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Wrap collection in another collection of same kind
        if (receiverType.isUnique() && receiverType.isOrdered()) {
          resultType = Type.orderedSet(receiverType);
        } else if (receiverType.isUnique()) {
          resultType = Type.set(receiverType);
        } else if (receiverType.isOrdered()) {
          resultType = Type.sequence(receiverType);
        } else {
          resultType = Type.bag(receiverType);
        }
        break;

      // ==================== ITERATOR OPERATIONS ====================

      case "select":
      case "reject":
        // select/reject(x | predicate) : Collection(T) -> Collection(T)
        // Filters elements, preserves collection type

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Check we have exactly 1 argument with iterator syntax
        List<VitruvOCLParser.NavigatingArgCSContext> selectArgs = ctx.navigatingArgCS();
        if (selectArgs.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires iterator argument (var | predicate)",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (selectArgs.size() > 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '"
                  + opName
                  + "' expects exactly 1 iterator argument, got "
                  + selectArgs.size(),
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check iterator with proper scoping
        resultType =
            typeCheckIteratorOperation(
                ctx,
                selectArgs.get(0),
                opName,
                receiverType,
                Type.BOOLEAN // Predicate must return Boolean
                );
        break;

      case "collect":
        // collect(x | expression) : Collection(T) -> Collection(U)
        // Transforms elements, result element type comes from body

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'collect' requires collection receiver, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Check we have exactly 1 argument with iterator syntax
        List<VitruvOCLParser.NavigatingArgCSContext> collectArgs = ctx.navigatingArgCS();
        if (collectArgs.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'collect' requires iterator argument (var | expression)",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (collectArgs.size() > 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'collect' expects exactly 1 iterator argument, got " + collectArgs.size(),
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check iterator with proper scoping
        // For collect, we don't constrain the body type - it can be anything
        resultType =
            typeCheckIteratorOperation(
                ctx, collectArgs.get(0), opName, receiverType, null // No constraint on body type
                );
        break;

      case "forAll":
      case "exists":
        // forAll/exists(x | predicate) : Collection(T) -> Set(Boolean)
        // Quantifiers always return Boolean

        if (!receiverType.isCollection()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires collection receiver, got " + receiverType,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Check we have exactly 1 argument with iterator syntax
        List<VitruvOCLParser.NavigatingArgCSContext> quantifierArgs = ctx.navigatingArgCS();
        if (quantifierArgs.isEmpty()) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' requires iterator argument (var | predicate)",
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (quantifierArgs.size() > 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '"
                  + opName
                  + "' expects exactly 1 iterator argument, got "
                  + quantifierArgs.size(),
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        // Type-check iterator with proper scoping
        Type quantifierResultType =
            typeCheckIteratorOperation(
                ctx,
                quantifierArgs.get(0),
                opName,
                receiverType,
                Type.BOOLEAN // Predicate must return Boolean
                );

        // Quantifiers always return Set(Boolean) regardless of receiver type
        if (quantifierResultType != Type.ERROR) {
          resultType = Type.set(Type.BOOLEAN);
        } else {
          resultType = Type.ERROR;
        }
        break;

      default:
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown collection operation: " + opName,
            ErrorSeverity.ERROR,
            "type-checker");
        resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type-checks String operations: concat, substring, toUpper, toLower, etc. Similar structure to
   * typeCheckCollectionOperation.
   *
   * @param ctx Navigation context containing the operation
   * @param opName Operation name (concat, substring, etc.)
   * @param receiverType Type of the receiver (should be String)
   * @return Result type of the operation
   */
  private Type typeCheckStringOperation(
      VitruvOCLParser.NavigatingExpCSContext ctx, String opName, Type receiverType) {
    // Verify receiver is String
    if (!receiverType.isConformantTo(Type.STRING)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "String operation '" + opName + "' requires String receiver, got " + receiverType,
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    Type resultType;

    // WICHTIG: Argumente sind in navigatingArgCS() + navigatingCommaArgCS()
    List<VitruvOCLParser.NavigatingArgCSContext> regularArgs = ctx.navigatingArgCS();
    List<VitruvOCLParser.NavigatingCommaArgCSContext> commaArgs = ctx.navigatingCommaArgCS();

    // Collect ALL argument types
    List<Type> argTypes = new ArrayList<>();

    // First argument (if exists)
    if (!regularArgs.isEmpty()) {
      Type argType = visit(regularArgs.get(0).navigatingArgExpCS());
      argTypes.add(argType);
    }

    // Additional comma arguments
    for (VitruvOCLParser.NavigatingCommaArgCSContext commaArg : commaArgs) {
      Type argType = visit(commaArg.navigatingArgExpCS());
      argTypes.add(argType);
    }

    int totalArgs = argTypes.size();

    switch (opName) {
      case "concat":
        // concat(String) : String
        if (totalArgs != 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'concat' requires 1 argument, got " + totalArgs,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (!argTypes.get(0).isConformantTo(Type.STRING)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "concat() requires String argument, got " + argTypes.get(0),
              ErrorSeverity.ERROR,
              "type-checker");
        }

        resultType = Type.STRING;
        break;

      case "substring":
        // substring(Integer, Integer) : String
        if (totalArgs != 2) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'substring' requires 2 arguments (start, end), got " + totalArgs,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (!argTypes.get(0).isConformantTo(Type.INTEGER)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "substring() start index must be Integer, got " + argTypes.get(0),
              ErrorSeverity.ERROR,
              "type-checker");
        }

        if (!argTypes.get(1).isConformantTo(Type.INTEGER)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "substring() end index must be Integer, got " + argTypes.get(1),
              ErrorSeverity.ERROR,
              "type-checker");
        }

        resultType = Type.STRING;
        break;

      case "toUpper":
      case "toLower":
        // toUpper() : String, toLower() : String
        if (totalArgs != 0) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation '" + opName + "' takes no arguments, got " + totalArgs,
              ErrorSeverity.ERROR,
              "type-checker");
        }

        resultType = Type.STRING;
        break;

      case "indexOf":
        // indexOf(String) : Integer
        if (totalArgs != 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'indexOf' requires 1 argument, got " + totalArgs,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (!argTypes.get(0).isConformantTo(Type.STRING)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "indexOf() requires String argument, got " + argTypes.get(0),
              ErrorSeverity.ERROR,
              "type-checker");
        }

        resultType = Type.INTEGER;
        break;

      case "equalsIgnoreCase":
        // equalsIgnoreCase(String) : Boolean
        if (totalArgs != 1) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Operation 'equalsIgnoreCase' requires 1 argument, got " + totalArgs,
              ErrorSeverity.ERROR,
              "type-checker");
          resultType = Type.ERROR;
          break;
        }

        if (!argTypes.get(0).isConformantTo(Type.STRING)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "equalsIgnoreCase() requires String argument, got " + argTypes.get(0),
              ErrorSeverity.ERROR,
              "type-checker");
        }

        resultType = Type.BOOLEAN;
        break;

      default:
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown string operation: " + opName,
            ErrorSeverity.ERROR,
            "type-checker");
        resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Type-checks iterator operations with proper variable scoping.
   *
   * @param navCtx Navigation context (for error reporting)
   * @param argCtx Iterator argument context (contains var | body)
   * @param opName Operation name (select, reject, collect, etc.)
   * @param receiverType Type of the collection being iterated over
   * @param expectedBodyType Expected type of body expression (null = any type OK)
   * @return Result type of the operation
   */
  private Type typeCheckIteratorOperation(
      VitruvOCLParser.NavigatingExpCSContext navCtx,
      VitruvOCLParser.NavigatingArgCSContext argCtx,
      String opName,
      Type receiverType,
      Type expectedBodyType) {
    // Extract iterator variable name and body from NavigatingArgCS
    VitruvOCLParser.NavigatingArgExpCSContext argExpCtx = argCtx.navigatingArgExpCS();

    // Must have iterator syntax: x | body
    if (argExpCtx.iteratorVar == null || argExpCtx.body == null) {
      errors.add(
          navCtx.getStart().getLine(),
          navCtx.getStart().getCharPositionInLine(),
          "Operation '" + opName + "' requires iterator syntax: variableName | expression",
          ErrorSeverity.ERROR,
          "type-checker");
      return Type.ERROR;
    }

    String iteratorVarName = argExpCtx.iteratorVar.getText();

    // Get element type from receiver collection
    Type elementType = receiverType.getElementType();

    // Create new local scope for iterator variable
    LocalScope iteratorScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iteratorScope);

    try {
      // Define iterator variable with element type
      VariableSymbol iteratorVar =
          new VariableSymbol(
              iteratorVarName, elementType, iteratorScope, true // Mark as iterator variable
              );

      symbolTable.define(iteratorVar);

      // Type-check body expression in iterator scope
      Type bodyType = visit(argExpCtx.body);

      if (bodyType == Type.ERROR) {
        return Type.ERROR;
      }

      // Check if body type matches expected type (if specified)
      if (expectedBodyType != null && !bodyType.isConformantTo(expectedBodyType)) {
        errors.add(
            argExpCtx.body.getStart().getLine(),
            argExpCtx.body.getStart().getCharPositionInLine(),
            "Iterator body must return " + expectedBodyType + ", got " + bodyType,
            ErrorSeverity.ERROR,
            "type-checker");
        return Type.ERROR;
      }

      // Compute result type based on operation
      return computeIteratorResultType(opName, receiverType, bodyType);

    } finally {
      // CRITICAL: Exit iterator scope
      symbolTable.exitScope();
    }
  }

  /**
   * Computes the result type of an iterator operation.
   *
   * @param opName Operation name (select, reject, collect, forAll, exists)
   * @param receiverType Type of the receiver collection
   * @param bodyType Type returned by the iterator body
   * @return Result type of the operation
   */
  private Type computeIteratorResultType(String opName, Type receiverType, Type bodyType) {
    return switch (opName) {
      case "select", "reject" ->
          // Preserve collection type: Set(T) -> Set(T)
          receiverType;

      case "collect" -> {
        // Transform with auto-flatten: Collection(T) -> Collection(U)
        // Body type becomes new element type
        // Preserve collection kind
        if (receiverType.isUnique() && receiverType.isOrdered()) {
          yield Type.orderedSet(bodyType);
        } else if (receiverType.isUnique()) {
          yield Type.set(bodyType);
        } else if (receiverType.isOrdered()) {
          yield Type.sequence(bodyType);
        } else {
          yield Type.bag(bodyType);
        }
      }

      case "forAll", "exists" ->
          // Quantifiers return Set(Boolean)
          Type.set(Type.BOOLEAN);

      default -> Type.ERROR;
    };
  }

  // ==================== Variables ====================

  /** Type-checks variable references: x, myVar, etc. */
  @Override
  public Type visitName(VitruvOCLParser.NameContext ctx) {
    // Check if it's a variable reference
    if (ctx.variableName != null) {
      String varName = ctx.variableName.getText();

      // Lookup in symbol table
      Symbol symbol = symbolTable.resolve(varName);

      if (symbol == null) {
        handleUndefinedSymbol(varName, ctx);
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
      }

      // Must be a variable, not a type or operation
      if (!(symbol instanceof VariableSymbol)) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "'" + varName + "' is not a variable",
            ErrorSeverity.ERROR,
            "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
      }

      Type varType = symbol.getType();
      nodeTypes.put(ctx, varType);
      return varType;
    }

    // Check for collection operation names
    if (ctx.collectionOperationName() != null) {
      // These are handled in prefixedExpCS navigation chains
      // Return placeholder type
      return Type.ANY;
    }

    // Check for string operation names
    if (ctx.stringOperationName() != null) {
      // These are handled in prefixedExpCS navigation chains
      return Type.ANY;
    }

    // Other cases (metamodel qualified names, etc.)
    return visitChildren(ctx);
  }

  /**
   * Type-checks navigating argument expressions. Handles three cases: 1. Iterator syntax: x | x > 2
   * (for select/reject/collect/forAll/exists) 2. Complex iterator syntax: expr | name . body
   * (legacy, backward compat) 3. Normal argument: expression
   */
  @Override
  public Type visitNavigatingArgExpCS(VitruvOCLParser.NavigatingArgExpCSContext ctx) {
    // Case 1: Simple Iterator syntax (x | body)
    if (ctx.iteratorVar != null && ctx.body != null) {
      // This is handled in typeCheckIteratorOperation
      // For now, just type-check the body expression
      Type bodyType = visit(ctx.body);
      nodeTypes.put(ctx, bodyType);
      return bodyType;
    }

    // Case 2: Complex iterator syntax (legacy - might be used elsewhere)
    if (ctx.iteratorVariable != null && ctx.iteratorBarExpCS() != null) {
      // Type-check iterator variable expression
      Type iterVarType = visit(ctx.iteratorVariable);

      // Type-check body expressions
      // WICHTIG: Die vollständige Liste kommt von infixedExpCS(), nicht von
      // iteratorBody
      // Das Label iteratorBody zeigt nur auf das ERSTE Element
      Type bodyType = null;
      List<VitruvOCLParser.InfixedExpCSContext> bodyExpressions = ctx.infixedExpCS();

      // Skip the first infixedExpCS if it's the iteratorVariable
      // (it appears twice: once as iteratorVariable, once in the list)
      int startIndex =
          (bodyExpressions.size() > 0 && bodyExpressions.get(0) == ctx.iteratorVariable) ? 1 : 0;

      for (int i = startIndex; i < bodyExpressions.size(); i++) {
        bodyType = visit(bodyExpressions.get(i));
      }

      nodeTypes.put(ctx, bodyType != null ? bodyType : Type.ERROR);
      return bodyType != null ? bodyType : Type.ERROR;
    }

    // Case 3: Normal argument expression
    Type resultType = null;
    for (VitruvOCLParser.InfixedExpCSContext exp : ctx.infixedExpCS()) {
      resultType = visit(exp);
    }

    // NEW: Check if this is a type argument (for oclIsKindOf, oclIsTypeOf,
    // oclAsType)
    // If the expression resolves to a typeNameExpCS, treat it as a Type instead of
    // evaluating it
    if (resultType == null && ctx.infixedExpCS().size() == 1) {
      VitruvOCLParser.InfixedExpCSContext infixed = ctx.infixedExpCS().get(0);
      // Check if this is actually a typeNameExpCS path
      Type typeArg = tryResolveAsType(infixed);
      if (typeArg != null) {
        nodeTypes.put(ctx, typeArg);
        return typeArg;
      }
    }

    if (resultType == null) {
      resultType = Type.ERROR;
    }

    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /**
   * Attempts to resolve an infixedExpCS as a type reference (e.g., UML::Class). Returns the Type if
   * successful, null otherwise.
   */
  private Type tryResolveAsType(VitruvOCLParser.InfixedExpCSContext ctx) {
    if (ctx instanceof VitruvOCLParser.PrefixedExpContext) {
      VitruvOCLParser.PrefixedExpCSContext prefixed =
          ((VitruvOCLParser.PrefixedExpContext) ctx).prefixedExpCS();

      // Check for Metamodel::Class pattern
      if (prefixed.metamodel != null && prefixed.className != null) {
        String qualifiedName = prefixed.metamodel.getText() + "::" + prefixed.className.getText();
        return symbolTable.lookupType(qualifiedName);
      }

      // Check for unqualified type name
      if (prefixed.primaryExpCS().size() == 1 && prefixed.navigationOperatorCS().isEmpty()) {

        VitruvOCLParser.PrimaryExpCSContext primary = prefixed.primaryExpCS(0);

        if (primary.navigatingExpCS() != null) {
          VitruvOCLParser.IndexExpCSContext indexExp = primary.navigatingExpCS().indexExpCS();

          if (indexExp != null && indexExp.nameExpCS() != null) {
            VitruvOCLParser.NameExpCSContext nameExp = indexExp.nameExpCS();

            if (nameExp instanceof VitruvOCLParser.NameContext) {
              VitruvOCLParser.NameContext name = (VitruvOCLParser.NameContext) nameExp;

              if (name.variableName != null) {
                return symbolTable.lookupType(name.variableName.getText());
              }
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Type-checks let expressions. Creates new local scope and binds variables.
   *
   * <p>Example: let x = 5, y = x * 2 in y + 3
   */
  @Override
  public Type visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    // Create new local scope for let variables
    LocalScope letScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(letScope);

    try {
      // Type-check and define all variables
      VitruvOCLParser.VariableDeclarationsContext varDecls = ctx.variableDeclarations();

      for (VitruvOCLParser.VariableDeclarationContext varDecl : varDecls.variableDeclaration()) {
        String varName = varDecl.varName.getText();

        // Check for duplicate in current scope
        Symbol existing = letScope.resolve(varName);
        if (existing != null && existing.getDefiningScope() == letScope) {
          errors.add(
              varDecl.getStart().getLine(),
              varDecl.getStart().getCharPositionInLine(),
              "Variable '" + varName + "' already defined in current scope",
              ErrorSeverity.ERROR,
              "type-checker");
          continue;
        }

        // Type-check initializer expression
        Type initType = visit(varDecl.varInit);

        if (initType == Type.ERROR) {
          continue;
        }

        // Check explicit type annotation if present
        Type declaredType = initType;
        if (varDecl.varType != null) {
          declaredType = visit(varDecl.varType);

          if (!initType.isConformantTo(declaredType)) {
            errors.add(
                varDecl.getStart().getLine(),
                varDecl.getStart().getCharPositionInLine(),
                "Type mismatch: initializer has type "
                    + initType
                    + " but variable declared as "
                    + declaredType,
                ErrorSeverity.ERROR,
                "type-checker");
            continue;
          }
        }

        // Create variable symbol and define
        VariableSymbol varSymbol = new VariableSymbol(varName, declaredType, letScope, false);

        symbolTable.define(varSymbol);
        nodeTypes.put(varDecl, declaredType);
      }

      // Type-check body expression (singular!)
      Type bodyType = visit(ctx.body);

      if (bodyType == null) {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Let expression body is empty",
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

  /** Type-checks 'self' references. */
  @Override
  public Type visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    // Lookup 'self' in symbol table
    Symbol selfSymbol = symbolTable.resolve("self");

    if (selfSymbol == null) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "'self' is not defined in current context",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    Type selfType = selfSymbol.getType();
    nodeTypes.put(ctx, selfType);
    return selfType;
  }

  /** Type-checks type expressions: Integer, String, Set(Integer), etc. */
  @Override
  public Type visitTypeExpCS(VitruvOCLParser.TypeExpCSContext ctx) {
    // typeExpCS can be either typeNameExpCS or typeLiteralCS
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

  /** Type-checks type names: Integer, String, Boolean, MyClass, etc. */
  @Override
  public Type visitTypeNameExpCS(VitruvOCLParser.TypeNameExpCSContext ctx) {
    String typeName = ctx.getText();

    // Handle primitive types
    return switch (typeName) {
      case "Integer" -> Type.INTEGER;
      case "String" -> Type.STRING;
      case "Boolean" -> Type.BOOLEAN;
      case "Double" -> Type.DOUBLE;
      case "OclAny" -> Type.ANY;
      default -> {
        // Try to resolve from symbol table (metamodel types)
        Symbol symbol = symbolTable.resolve(typeName);

        if (symbol == null) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "Unknown type: " + typeName,
              ErrorSeverity.ERROR,
              "type-checker");
          yield Type.ERROR;
        }

        if (!(symbol instanceof TypeSymbol)) {
          errors.add(
              ctx.getStart().getLine(),
              ctx.getStart().getCharPositionInLine(),
              "'" + typeName + "' is not a type",
              ErrorSeverity.ERROR,
              "type-checker");
          yield Type.ERROR;
        }

        yield symbol.getType();
      }
    };
  }

  /** Type-checks type literals: primitive types and collection types. */
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

  /** Type-checks primitive types: Integer, String, Boolean, etc. */
  @Override
  public Type visitPrimitiveTypeCS(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    String typeName = ctx.getText();

    return switch (typeName) {
      case "Integer" -> Type.INTEGER;
      case "String" -> Type.STRING;
      case "Boolean" -> Type.BOOLEAN;
      case "Double" -> Type.DOUBLE;
      case "OclAny" -> Type.ANY;
      case "UnlimitedNatural" -> Type.INTEGER; // Map to Integer for now
      case "ID" -> Type.STRING; // Map to String for now
      default -> {
        errors.add(
            ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine(),
            "Unknown primitive type: " + typeName,
            ErrorSeverity.ERROR,
            "type-checker");
        yield Type.ERROR;
      }
    };
  }

  // ==================== Navigation ====================

  @Override
  public Type visitNavigatingExpCS(VitruvOCLParser.NavigatingExpCSContext ctx) {
    // Start with the base expression (indexExpCS is the source)
    Type sourceType = visit(ctx.indexExpCS());

    // Process regular arguments (property access/method calls with .)
    for (VitruvOCLParser.NavigatingArgCSContext arg : ctx.navigatingArgCS()) {
      sourceType = handleNavigatingArg(arg, sourceType);
    }

    // Process comma arguments (additional parameters)
    for (VitruvOCLParser.NavigatingCommaArgCSContext commaArg : ctx.navigatingCommaArgCS()) {
      sourceType = handleNavigatingCommaArg(commaArg, sourceType);
    }

    // Process semi arguments (iterator variables for select/reject/collect)
    for (VitruvOCLParser.NavigatingSemiAgrsCSContext semiArg : ctx.navigatingSemiAgrsCS()) {
      sourceType = handleNavigatingSemiArgs(semiArg, sourceType);
    }

    nodeTypes.put(ctx, sourceType);
    return sourceType;
  }

  /**
   * Handles dot navigation (.) In OCL#, . is used for both object operations and collection
   * operations since everything is a collection with multiplicity.
   */
  private Type handleNavigatingArg(VitruvOCLParser.NavigatingArgCSContext ctx, Type sourceType) {
    String name = extractOperationName(ctx.navigatingArgExpCS());

    // Collect argument types
    Type[] argTypes = ctx.expCS().stream().map(this::visit).toArray(Type[]::new);

    // Try collection operation (works for all types since everything is a
    // collection)
    Type result = TypeResolver.resolveCollectionOperation(sourceType, name, argTypes);
    if (result != Type.ERROR) {
      return result;
    }

    // Try object operation (String methods, etc.)
    if (argTypes.length > 0) {
      result = TypeResolver.resolveObjectOperation(sourceType, name, argTypes);
      if (result != Type.ERROR) {
        return result;
      }
    }

    // Try property access (metamodel properties)
    if (argTypes.length == 0) {
      result = resolvePropertyAccess(sourceType, name, ctx);
      if (result != Type.ERROR) {
        return result;
      }
    }

    // Nothing worked - error
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Unknown operation or property '" + name + "' on type " + sourceType,
        ErrorSeverity.ERROR,
        "type-checker");
    return Type.ERROR;
  }

  /**
   * TODO wenn variablen Handles comma-separated additional arguments Example: operation(arg1, arg2,
   * arg3)
   *
   * <p>Structure: , arg : type = init
   */
  private Type handleNavigatingCommaArg(
      VitruvOCLParser.NavigatingCommaArgCSContext ctx, Type sourceType) {
    // Process additional arguments for multi-parameter operations
    // For now, we just validate the expressions exist
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      visit(exp); // Type check each expression
    }

    return sourceType;
  }

  /**
   * TODO wenn Variablen Handles semicolon-separated iterator variables Example:
   * collection->select(x | x > 5)
   *
   * <p>Structure: ; var : type = exp | body
   */
  private Type handleNavigatingSemiArgs(
      VitruvOCLParser.NavigatingSemiAgrsCSContext ctx, Type sourceType) {
    // Extract iterator variable name

    // TODO: Register iterator variable in symbol table with element type
    // Type varType = sourceType.getElementType();
    // symbolTable.define(varName, varType);

    // Type check the body expression if present
    if (ctx.infixedExpCS() != null) {
      visit(ctx.infixedExpCS());
    }

    // Type check the collection expression if present
    if (ctx.expCS() != null) {
      visit(ctx.expCS());
    }

    return sourceType;
  }

  /** Resolves property access on objects. TODO: Use TypeRegistry to resolve metamodel properties */
  private Type resolvePropertyAccess(
      Type sourceType, String propertyName, org.antlr.v4.runtime.ParserRuleContext ctx) {
    // For now, return ERROR - will be implemented with TypeRegistry
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Cannot resolve property '" + propertyName + "' on type " + sourceType,
        ErrorSeverity.ERROR,
        "type-checker");
    return Type.ERROR;
  }

  // ==================== If-Then-Else ====================

  @Override
  public Type visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    // Get ALL expressions
    List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();

    if (allExps.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "If expression requires condition, then branch, and else branch",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Find 'then', 'else', 'endif' token positions in the token stream
    List<VitruvOCLParser.ExpCSContext> condExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> thenExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> elseExps = new ArrayList<>();

    // Parse structure by finding keyword positions
    int thenTokenIndex = findKeywordToken(ctx, "then");
    int elseTokenIndex = findKeywordToken(ctx, "else");

    // Partition expressions based on token positions
    for (VitruvOCLParser.ExpCSContext exp : allExps) {
      int expStartIndex = exp.getStart().getTokenIndex();

      if (expStartIndex < thenTokenIndex) {
        condExps.add(exp);
      } else if (expStartIndex < elseTokenIndex) {
        thenExps.add(exp);
      } else {
        elseExps.add(exp);
      }
    }

    if (condExps.isEmpty() || thenExps.isEmpty() || elseExps.isEmpty()) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "If expression missing condition, then, or else branch",
          ErrorSeverity.ERROR,
          "type-checker");
      nodeTypes.put(ctx, Type.ERROR);
      return Type.ERROR;
    }

    // Type-check all expressions in each section, use last one for branch type
    Type condType = null;
    for (VitruvOCLParser.ExpCSContext exp : condExps) {
      condType = visit(exp);
    }

    Type thenType = null;
    for (VitruvOCLParser.ExpCSContext exp : thenExps) {
      thenType = visit(exp);
    }

    Type elseType = null;
    for (VitruvOCLParser.ExpCSContext exp : elseExps) {
      elseType = visit(exp);
    }

    // Condition must be Boolean
    if (condType != null && !condType.isConformantTo(Type.BOOLEAN)) {
      errors.add(
          ctx.getStart().getLine(),
          ctx.getStart().getCharPositionInLine(),
          "If condition must be Boolean, got: " + condType,
          ErrorSeverity.ERROR,
          "type-checker");
    }

    // Both branches must have compatible types
    if (thenType != null && elseType != null) {
      Type resultType;

      // Special case: if one branch is a collection with Any element type,
      // use the other branch's type (prefer more specific type)
      if (thenType.isCollection() && elseType.isCollection()) {
        Type thenElemType = thenType.getElementType();
        Type elseElemType = elseType.getElementType();

        if (thenElemType == Type.ANY && elseElemType != Type.ANY) {
          // Then branch is {Any}, else branch is specific → use else type
          resultType = elseType;
        } else if (elseElemType == Type.ANY && thenElemType != Type.ANY) {
          // Else branch is {Any}, then branch is specific → use then type
          resultType = thenType;
        } else {
          // Both have concrete element types (or both have Any)
          // Check if they're compatible
          if (!thenType.equals(elseType)) {
            // Try common supertype
            if (thenType.isConformantTo(elseType)) {
              resultType = elseType;
            } else if (elseType.isConformantTo(thenType)) {
              resultType = thenType;
            } else {
              errors.add(
                  ctx.getStart().getLine(),
                  ctx.getStart().getCharPositionInLine(),
                  "If branches have incompatible types: then=" + thenType + ", else=" + elseType,
                  ErrorSeverity.ERROR,
                  "type-checker");
              nodeTypes.put(ctx, Type.ERROR);
              return Type.ERROR;
            }
          } else {
            resultType = thenType;
          }
        }
      } else {
        // Non-collections or mixed types
        if (!thenType.equals(elseType)) {
          // Check if types are conformant
          if (thenType.isConformantTo(elseType)) {
            resultType = elseType;
          } else if (elseType.isConformantTo(thenType)) {
            resultType = thenType;
          } else {
            errors.add(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                "If branches have incompatible types: then=" + thenType + ", else=" + elseType,
                ErrorSeverity.ERROR,
                "type-checker");
            nodeTypes.put(ctx, Type.ERROR);
            return Type.ERROR;
          }
        } else {
          resultType = thenType;
        }
      }

      nodeTypes.put(ctx, resultType);
      return resultType;
    }

    Type resultType = thenType != null ? thenType : Type.ERROR;
    nodeTypes.put(ctx, resultType);
    return resultType;
  }

  /** Helper: Find token index of keyword in context, respecting nesting levels. */
  private int findKeywordToken(VitruvOCLParser.IfExpCSContext ctx, String keyword) {
    if (tokens == null) {
      return Integer.MAX_VALUE;
    }

    int startIdx = ctx.getStart().getTokenIndex();
    int stopIdx = ctx.getStop().getTokenIndex();

    // Track nesting level
    int nestingLevel = 0;

    for (int i = startIdx; i <= stopIdx; i++) {
      org.antlr.v4.runtime.Token token = tokens.get(i);
      String text = token.getText();

      // Skip the first 'if'
      if (i == startIdx && text.equals("if")) {
        continue;
      }

      // Track nesting
      if (text.equals("if")) {
        nestingLevel++;
      } else if (text.equals("endif")) {
        nestingLevel--;
      }

      // Only match keywords at nesting level 0
      if (nestingLevel == 0 && text.equals(keyword)) {
        return i;
      }
    }

    return Integer.MAX_VALUE;
  }
}