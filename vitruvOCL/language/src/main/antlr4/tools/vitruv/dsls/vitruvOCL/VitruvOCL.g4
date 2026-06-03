/*******************************************************************************
 * Copyright (c) 2012, 2016 University of Mannheim: Chair for Software Engineering
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ralph Gerbig - initial API and implementation and initial documentation
 *    Arne Lange - ocl2 implementation
 *    Max Oesterle - OCL implementation, extensive refactoring and extensions
 *******************************************************************************/
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
    | left=infixedExpCS op='~' right=infixedExpCS                          # correspondence
    | left=infixedExpCS op='==' right=infixedExpCS                         # equalityComparison
    | left=infixedExpCS op='!=' right=infixedExpCS                         # inequalityComparison
    | left=infixedExpCS op='<' right=infixedExpCS                          # lessThanComparison
    | left=infixedExpCS op='<=' right=infixedExpCS                         # lessThanOrEqualComparison
    | left=infixedExpCS op='>' right=infixedExpCS                          # greaterThanComparison
    | left=infixedExpCS op='>=' right=infixedExpCS                         # greaterThanOrEqualComparison
    | left=infixedExpCS op='^' right=infixedExpCS                          # message
    | left=infixedExpCS op='and' right=infixedExpCS                        # logicalAnd
    | left=infixedExpCS op='or' right=infixedExpCS                         # logicalOr
    | left=infixedExpCS op='xor' right=infixedExpCS                        # logicalXor
    | left=infixedExpCS op='implies' right=infixedExpCS                    # implication
    | left=infixedExpCS op=ID right=infixedExpCS                           # unknownBinaryOp
;

// Prefix operations and navigation
prefixedExpCS
:
    base=primaryExpCS navigation=navigationChainCS*                         # primaryWithNav
    | '-' operand=prefixedExpCS                                             # unaryMinus
    | 'not' operand=prefixedExpCS                                           # logicalNot
    | metamodel=ID '::' className=ID navigation=navigationChainCS*         # prefixedQualified
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
    | unknownOpCS    # unknownOperation
;

// Catch-all for unknown operation calls — produces a precise "Unknown operation" diagnostic
// instead of a cryptic syntax error on the closing parenthesis.
unknownOpCS
:
    opName=ID '(' (args+=expCS (',' args+=expCS)*)? ')'
;

// ============================================================================
// COLLECTION OPERATIONS
// ============================================================================

collectionOpCS
:
    'including' '(' arg=expCS ')'                                          # includingOp
    | 'excluding' '(' arg=expCS ')'                                        # excludingOp
    | 'includes' '(' arg=expCS ')'                                         # includesOp
    | 'excludes' '(' arg=expCS ')'                                         # excludesOp
    | 'includesAll' '(' arg=expCS ')'                                      # includesAllOp
    | 'excludesAll' '(' arg=expCS ')'                                      # excludesAllOp
    | 'count' '(' arg=expCS ')'                                            # countOp
    | 'flatten' '(' ')'                                                    # flattenOp
    | 'union' '(' arg=expCS ')'                                            # unionOp
    | 'intersection' '(' arg=expCS ')'                                     # intersectionOp
    | 'symmetricDifference' '(' arg=expCS ')'                              # symmetricDifferenceOp
    | 'append' '(' arg=expCS ')'                                           # appendOp
    | 'prepend' '(' arg=expCS ')'                                          # prependOp
    | 'insertAt' '(' index=expCS ',' arg=expCS ')'                        # insertAtOp
    | 'subSequence' '(' start=expCS ',' end=expCS ')'                     # subSequenceOp
    | 'at' '(' index=expCS ')'                                             # atOp
    | 'sum' '(' ')'                                                        # sumOp
    | 'max' '(' ')'                                                        # maxOp
    | 'min' '(' ')'                                                        # minOp
    | 'avg' '(' ')'                                                        # avgOp
    | 'abs' '(' ')'                                                        # absOp
    | 'floor' '(' ')'                                                      # floorOp
    | 'ceil' '(' ')'                                                       # ceilOp
    | 'ceiling' '(' ')'                                                    # ceilingOp
    | 'round' '(' ')'                                                      # roundOp
    | 'div' '(' arg=expCS ')'                                              # divOp
    | 'mod' '(' arg=expCS ')'                                              # modOp
    | 'lift' '(' ')'                                                       # liftOp
    | 'size' '(' ')'                                                       # sizeOp
    | 'isEmpty' '(' ')'                                                    # isEmptyOp
    | 'notEmpty' '(' ')'                                                   # notEmptyOp
    | 'first' '(' ')'                                                      # firstOp
    | 'last' '(' ')'                                                       # lastOp
    | 'reverse' '(' ')'                                                    # reverseOp
    | 'allInstances' '(' ')'                                               # allInstancesOp
    | 'asSet' '(' ')'                                                      # asSetOp
    | 'asBag' '(' ')'                                                      # asBagOp
    | 'asSequence' '(' ')'                                                 # asSequenceOp
    | 'asOrderedSet' '(' ')'                                               # asOrderedSetOp
;

// ============================================================================
// ITERATOR OPERATIONS
// ============================================================================

iteratorOpCS
:
    'select' '(' corrFilter=correspondenceFilterCS ')'                               # selectCorrespondence
    | 'reject' '(' corrFilter=correspondenceFilterCS ')'                             # rejectCorrespondence
    | 'exists' '(' corrFilter=correspondenceFilterCS ')'                             # existsCorrespondence
    | 'select' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                  # selectOp
    | 'reject' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                  # rejectOp
    | 'collect' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                 # collectOp
    | 'forAll' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                  # forAllOp
    | 'exists' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                  # existsOp
    | 'one' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                     # oneOp
    | 'any' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                     # anyOp
    | 'isUnique' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                # isUniqueOp
    | 'sortedBy' '(' iteratorVars=iteratorVarList '|' body=expCS ')'                # sortedByOp
    | 'collectNested' '(' iteratorVars=iteratorVarList '|' body=expCS ')'           # collectNestedOp
    | 'iterate' '(' iterateVarSpec '|' body=expCS ')'                               # iterateOp
;

// iterate(elem; acc : Type = initExpr | body)
iterateVarSpec
:
    iterVar=ID ';' accVar=ID (':' accType=typeExpCS)? '=' accInit=expCS
;

// ============================================================================
// CORRESPONDENCE FILTER
// ============================================================================

correspondenceFilterCS
:
    '~' (',' correspondenceOptions)?
;

correspondenceOptions
:
    correspondenceOption (',' correspondenceOption)*
;

correspondenceOption
:
    'Type' '=' type=typeExpCS    # corrTypeFilter
    | 'Tag' '='  tag=STRING      # corrTagFilter
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
    'concat' '(' arg=expCS ')'                                             # concatOp
    | 'length' '(' ')'   # lengthOp
    | 'substring' '(' start=expCS ',' end=expCS ')'                       # substringOp
    | 'toUpper' '(' ')'                                                    # toUpperOp
    | 'toLower' '(' ')'                                                    # toLowerOp
    | 'indexOf' '(' arg=expCS ')'                                          # indexOfOp
    | 'equalsIgnoreCase' '(' arg=expCS ')'                                 # equalsIgnoreCaseOp
    | 'toInteger' '(' ')'                                                  # toIntegerOp
    | 'toReal' '(' ')'                                                     # toRealOp
    | 'characters' '(' ')'                                                 # charactersOp
    | 'matches' '(' arg=expCS ')'                                          # matchesOp
    | 'substituteAll' '(' pattern=expCS ',' replacement=expCS ')'         # substituteAllOp
    | 'substituteFirst' '(' pattern=expCS ',' replacement=expCS ')'       # substituteFirstOp
    | 'tokenize' '(' arg=expCS ')'                                         # tokenizeOp
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
    [a-zA-Z_][a-zA-Z0-9_]*
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