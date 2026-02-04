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
    CONTEXT
    (ID ':')?
    (metamodel=ID '::' className=ID | contextName=ID)
    invCS*
;

invCS
:
    'inv' (ID ('(' specificationCS ')')?)? ':' specificationCS
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

COLONCOLON : '::' ;

typeNameExpCS
:
    metamodel=ID COLONCOLON className=ID
    | unqualified=ID
;
// ============================================================================
// EXPRESSIONS
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
    base=primaryExpCS navigation=navigationChainCS*                         # primaryWithNav
    | '-' operand=prefixedExpCS                                             # unaryMinus
    | 'not' operand=prefixedExpCS                                           # logicalNot
    | metamodel=ID '::' className=ID navigation=navigationChainCS+         # prefixedQualified
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
    collectionOpCS   # collectionOperation
    | stringOpCS     # stringOperation
    | iteratorOpCS   # iteratorOperation
    | typeOpCS       # typeOperation
;

// ============================================================================
// COLLECTION OPERATIONS
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
// ITERATOR OPERATIONS
// ============================================================================

iteratorOpCS
:
    'select' '(' iteratorVars=iteratorVarList '|' body=expCS ')'    # selectOp
    | 'reject' '(' iteratorVars=iteratorVarList '|' body=expCS ')'  # rejectOp
    | 'collect' '(' iteratorVars=iteratorVarList '|' body=expCS ')' # collectOp
    | 'forAll' '(' iteratorVars=iteratorVarList '|' body=expCS ')'  # forAllOp
    | 'exists' '(' iteratorVars=iteratorVarList '|' body=expCS ')'  # existsOp
;

iteratorVarList
:
    ID (',' ID)*
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