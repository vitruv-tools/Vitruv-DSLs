grammar VitruvOCL;

// ============================================================================
// CONTEXT & CONSTRAINTS
// ============================================================================

contextDeclCS
:
    classifierContextCS+
;

classifierContextCS
:
    CONTEXT levelSpecificationCS?
    (ID ':')?
    (metamodel=ID '::' className=ID | contextName=ID)
    invCS*
;

invCS
:
    'inv' (ID ('(' specificationCS ')')?)? ':' specificationCS
;

levelSpecificationCS
:
    '(' NumberLiteralExpCS (',' ('_' | NumberLiteralExpCS))? ')'
;

CONTEXT: 'context';

// ============================================================================
// TYPE SYSTEM
// ============================================================================

typeExpCS
:
    typeNameExpCS
    | typeLiteralCS
;

typeLiteralCS
:
    primitiveTypeCS
    | collectionTypeCS
;

collectionTypeCS
:
    collectionKind=collectionTypeIdentifier ('(' typeExpCS ')' | '<' typeExpCS '>')?
;

collectionTypeIdentifier
:
    'Collection' | 'Bag' | 'OrderedSet' | 'Sequence' | 'Set'
;

primitiveTypeCS
:
    'Boolean' | 'Integer' | 'Real' | 'String' | 'ID' | 'UnlimitedNatural' | 'OclAny'
;

typeNameExpCS
:
    metamodel=ID '::' className=ID
    | unqualified=ID
;

// ============================================================================
// EXPRESSIONS (all evaluate to collections internally)
// ============================================================================

specificationCS
:
    expCS*
;

expCS
:
    infixedExpCS
;

// Infix operations with precedence
infixedExpCS
:
    prefixedExpCS                                                           # prefixedExpr
    | left=infixedExpCS op=('*'|'/') right=infixedExpCS                    # multiplicative
    | left=infixedExpCS op=('+'|'-') right=infixedExpCS                    # additive
    | left=infixedExpCS op=('<='|'>='|'!='|'<'|'>'|'==') right=infixedExpCS # comparison
    | left=infixedExpCS op='~' right=infixedExpCS                          # correspondence
    | left=infixedExpCS op='^' right=infixedExpCS                          # message
    | left=infixedExpCS op=('and'|'or'|'xor') right=infixedExpCS           # logical
    | left=infixedExpCS op='implies' right=infixedExpCS                    # implication
;

// Prefix operations and navigation
prefixedExpCS
:
    op=('-'|'not')* base=primaryExpCS navigation=navigationChainCS*        # prefixedPrimary
    | op=('-'|'not')* metamodel=ID '::' className=ID navigation=navigationChainCS+ # prefixedQualified
;

navigationChainCS
:
    '.' navigationTargetCS
;

navigationTargetCS
:
    propertyAccess             # propertyNav
    | operationCall            # operationNav
;

// ============================================================================
// PRIMARY EXPRESSIONS (atomic expressions)
// ============================================================================

primaryExpCS
:
    literalExpCS               # literal
    | ifExpCS                  # conditional
    | letExpCS                 # letBinding
    | collectionLiteralExpCS   # collectionLiteral
    | typeLiteralExpCS         # typeLiteral
    | nestedExpCS              # nested
    | selfExpCS                # self
    | variableExpCS            # variable
;

// Literals: User writes "1", internally becomes ¡int! = [1]
literalExpCS
:
    NumberLiteralExpCS         # numberLit
    | STRING                   # stringLit
    | BooleanLiteralExpCS      # booleanLit
;

variableExpCS
:
    varName=ID
;

selfExpCS
:
    'self'
;

nestedExpCS
:
    '(' expCS+ ')'
;

// ============================================================================
// CONTROL FLOW
// ============================================================================

ifExpCS
:
    'if' condition=expCS+ 'then' thenBranch=expCS+ 'else' elseBranch=expCS+ 'endif'
;

letExpCS
:
    'let' variableDeclarations 'in' body=expCS+
;

variableDeclarations
:
    variableDeclaration (',' variableDeclaration)*
;

variableDeclaration
:
    varName=ID (':' varType=typeExpCS)? '=' varInit=expCS
;

// ============================================================================
// COLLECTION LITERALS
// ============================================================================

collectionLiteralExpCS
:
    collectionKind=collectionTypeCS '{' (arguments=collectionArguments)? '}'
;

collectionArguments
:
    collectionLiteralPartCS (',' collectionLiteralPartCS)*
;

collectionLiteralPartCS
:
    expCS ('..' expCS)?
;

typeLiteralExpCS
:
    typeLiteralCS
;

// ============================================================================
// PROPERTY ACCESS & OPERATIONS
// ============================================================================

propertyAccess
:
    propertyName=ID
;

operationCall
:
    collectionOpCS
    | stringOpCS
    | iteratorOpCS
    | typeOpCS
;

// ============================================================================
// COLLECTION OPERATIONS (each is a labeled alternative)
// ============================================================================

collectionOpCS
:
    'including' '(' arg=expCS ')'      # includingOp
    | 'excluding' '(' arg=expCS ')'    # excludingOp
    | 'includes' '(' arg=expCS ')'     # includesOp
    | 'excludes' '(' arg=expCS ')'     # excludesOp
    | 'flatten' '(' ')'                # flattenOp
    | 'union' '(' arg=expCS ')'        # unionOp
    | 'append' '(' arg=expCS ')'       # appendOp
    | 'sum' '(' ')'                    # sumOp
    | 'max' '(' ')'                    # maxOp
    | 'min' '(' ')'                    # minOp
    | 'avg' '(' ')'                    # avgOp
    | 'abs' '(' ')'                    # absOp
    | 'floor' '(' ')'                  # floorOp
    | 'ceil' '(' ')'                   # ceilOp
    | 'round' '(' ')'                  # roundOp
    | 'lift' '(' ')'                   # liftOp
    | 'size' '(' ')'                   # sizeOp
    | 'isEmpty' '(' ')'                # isEmptyOp
    | 'notEmpty' '(' ')'               # notEmptyOp
    | 'first' '(' ')'                  # firstOp
    | 'last' '(' ')'                   # lastOp
    | 'reverse' '(' ')'                # reverseOp
    | 'allInstances' '(' ')'           # allInstancesOp
;

// ============================================================================
// ITERATOR OPERATIONS (with implicit lowering)
// ============================================================================

iteratorOpCS
:
    'select' '(' iteratorVar=ID '|' body=expCS ')'    # selectOp
    | 'reject' '(' iteratorVar=ID '|' body=expCS ')'  # rejectOp
    | 'collect' '(' iteratorVar=ID '|' body=expCS ')' # collectOp
    | 'forAll' '(' iteratorVar=ID '|' body=expCS ')'  # forAllOp
    | 'exists' '(' iteratorVar=ID '|' body=expCS ')'  # existsOp
;

// ============================================================================
// STRING OPERATIONS
// ============================================================================

stringOpCS
:
    'concat' '(' arg=expCS ')'                              # concatOp
    | 'substring' '(' start=expCS ',' end=expCS ')'        # substringOp
    | 'toUpper' '(' ')'                                     # toUpperOp
    | 'toLower' '(' ')'                                     # toLowerOp
    | 'indexOf' '(' arg=expCS ')'                          # indexOfOp
    | 'equalsIgnoreCase' '(' arg=expCS ')'                 # equalsIgnoreCaseOp
;

// ============================================================================
// TYPE OPERATIONS
// ============================================================================

typeOpCS
:
    'oclIsKindOf' '(' type=typeExpCS ')'    # oclIsKindOfOp
    | 'oclIsTypeOf' '(' type=typeExpCS ')'  # oclIsTypeOfOp
    | 'oclAsType' '(' type=typeExpCS ')'    # oclAsTypeOp
;

// ============================================================================
// LEXER
// ============================================================================

NumberLiteralExpCS
:
    INT ('.' INT)? (('e'|'E') ('+'|'-')? INT)?
;

BooleanLiteralExpCS
:
    'true' | 'false'
;

ID
:
    [a-zA-Z][a-zA-Z0-9]*
;

STRING
:
    UnterminatedStringLiteral '"'
;

UnterminatedStringLiteral
:
    '"' (~["\\\r\n] | '\\' (. | EOF))*
;

fragment DIGIT: [0-9];
INT: DIGIT+;

WS: [ \t\n\r]+ -> skip;
COMMENT: '--' .*? '\n' -> skip;

onespace: ONESPACE;
ONESPACE: ' ';