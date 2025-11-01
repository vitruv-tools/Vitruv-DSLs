/******************************************************************************* 
 * Copyright (c) 2012, 2016 University of Mannheim: Chair for Software Engineering
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ralph Gerbig - initial API and implementation and initial documentation
 * Arne Lange - ocl2 implementation

* TODO Noch OCL# leute REFERENZIEREN
 *******************************************************************************/
grammar VitruvOCL; // Noch nur OCL#, noch nicht die Features von vitruvOCL hinzugefügt

// ==================== CONTEXT ====================

contextDeclCS
:
    (
        propertyContextDeclCS
        | classifierContextCS
        | operationContextCS
    )+
;

// ==================== OPERATION CONTEXT ====================

operationContextCS
:
    CONTEXT levelSpecificationCS?
    (
        ID ':' // Optionaler Name
    )?
    (
        ID '::'
        (
            ID '::'
        )* ID
        | ID
    ) '('
    (
        parameterCS
        (
            ',' parameterCS
        )*
    )? ')'
    (
        ':' typeExpCS
    )?
    // preCS und postCS fallen weg, da OCL# sie nicht unterstützt
    // (pre/post invariants removed because OCL# does not support them)
    // (
        // preCS
        // | postCS
        // | bodyCS
    // )*
;

// ==================== LEVEL SPECIFICATION ====================

levelSpecificationCS
:
    '(' NumberLiteralExpCS
    (
        ','
        (
            '_'
            | NumberLiteralExpCS
        )
    )? ')'
;

// ==================== KEYWORDS ====================

CONTEXT
:
    'context'
;

// ==================== BODY, PRE, POST, FILTER ====================

// bodyCS, preCS, postCS, filterCS are removed in OCL#
// because OCL# does not support these clauses
/*
bodyCS : 'body' ID? ':' specificationCS ;
postCS : 'post' ID? ':' specificationCS ;
preCS : 'pre' ID? ':' specificationCS ;
filterCS : 'filter' ID? ':' specificationCS ;
*/

// ==================== DEFINITIONS ====================

defCS
:
    'def' ID? ':' ID
    (
        (
            '(' parameterCS?
            (
                ',' parameterCS
            )* ')'
        )? ':' typeExpCS? '=' specificationCS
    )
;

// ==================== TYPES ====================

typeExpCS
:
    typeNameExpCS
    | typeLiteralCS
;

typeLiteralCS
:
    primitiveTypeCS
    | collectionTypeCS
    // tupleTypeCS removed because OCL# does not support tuple types
    // Tuple types are removed to simplify collections and iterators
    // | tupleTypeCS
;

tupleTypeCS
:
    'Tuple'
    (
        '(' tuplePartCS
        (
            ',' tuplePartCS
        )* ')'
        | '<' tuplePartCS
        (
            ',' tuplePartCS
        )* '>'
    )?
;

// tuple parts removed along with tupleTypeCS
/*
tuplePartCS
:
    ID ':' typeExpCS
;
*/

collectionTypeCS
:
    collectionType = collectionTypeIDentifier
    (
        '(' typeExpCS ')'
        | '<' typeExpCS '>'
    )?
;

collectionTypeIDentifier
:
    'Collection'
    | 'Bag'
    | 'OrderedSet'
    | 'Sequence'
    | 'Set'
;

primitiveTypeCS
:
    'Boolean'
    | 'Integer'
    | 'Real'
    | 'String'
    | 'ID'
    | 'UnlimitedNatural'
    | 'OclAny'
    // OCL# removes OclInvalid and OclVoid
    // | 'OclInvalID'
    // | 'OclVoID'
;

// typeNameExpCS remains unchanged
typeNameExpCS
:
    ID '::'
    (
        ID '::'
    )* ID
    | ID
;

// ==================== EXPRESSIONS ====================

specificationCS
:
    infixedExpCS*
;

expCS
:
    infixedExpCS
;

infixedExpCS
:
    prefixedExpCS # prefixedExp
    | iteratorBarExpCS # iteratorBar
    | left = infixedExpCS op =
    (
        '/'
        | '*'
    ) right = infixedExpCS # timesDivide
    | left = infixedExpCS op =
    (
        '+'
        | '-'
    ) right = infixedExpCS # plusMinus
    | left = infixedExpCS op =
    (
        '<='
        | '>='
        | '<>'
        | '<'
        | '>'
        | '='
    ) right = infixedExpCS # equalOperations
    | left = infixedExpCS op = '^' right = infixedExpCS # Message
    | left = infixedExpCS op =
    (
        'and'
        | 'or'
        | 'xor'
    ) right = infixedExpCS # andOrXor
    | left = infixedExpCS op = 'implies' right = infixedExpCS # implies
;

iteratorBarExpCS
:
    '|'
;

navigationOperatorCS
:
    '.' # dot
    // '->' removed, OCL# only uses point operator
    // | '->' # arrow
;

prefixedExpCS
:
    not = UnaryOperatorCS+ exp = primaryExpCS
    | primaryExpCS
    (
    navigationOperatorCS primaryExpCS
    )*
    | primaryExpCS
;

UnaryOperatorCS
:
    '-'
    | 'not'
;

// ==================== PRIMARY EXPRESSIONS ====================

primaryExpCS
:
    // letExpCS removed, OCL# does not support let
    // | letExpCS
    | ifExpCS
    | primitiveLiteralExpCS
    | navigatingExpCS
    | selfExpCS
    // tupleLiteralExpCS removed
    // | tupleLiteralExpCS
    | collectionLiteralExpCS
    | typeLiteralExpCS
    | nestedExpCS
;

nestedExpCS
:
    (
        '(' exp = expCS+ ')'
    )+
;

ifExpCS
:
    'if' ifexp = expCS+ 'then' thenexp = expCS+ 'else' elseexp = expCS+
    'endif'
;

// letExpCS removed
/*
letExpCS
:
    'let' letVariableCS
    (
        ',' letVariableCS
    )* 'in' in = expCS+
;

letVariableCS
:
    name = ID ':' type = typeExpCS '=' exp = expCS+
;
*/

typeLiteralExpCS
:
    typeLiteralCS
;

collectionLiteralExpCS
:
 type = collectionTypeCS '{'
 (
 argument = collectionArguments
 )? '}'
;

collectionArguments
:
    collectionLiteralPartCS
    (
        ',' collectionLiteralPartCS
    )*
;

collectionLiteralPartCS
:
    expCS
    (
        '..' expCS
    )?
;

// tupleLiteralExpCS removed
/*
tupleLiteralExpCS
:
    'Tuple' '{' tupleLiteralPartCS
    (
        ',' tupleLiteralPartCS
    )* '}'
;

tupleLiteralPartCS
:
    ID
    (
        ':' typeExpCS
    )? '=' expCS
;
*/

selfExpCS
:
    'self'
;

primitiveLiteralExpCS
:
    NumberLiteralExpCS # number
    | STRING # string
    | BooleanLiteralExpCS # boolean
    // invalid and null removed
    // | InvalIDLiteralExpCS # invalid
    // | NullLiteralExpCS # null
;

InvalIDLiteralExpCS
:
     'invalid'
;

NumberLiteralExpCS
:
    INT
    (
        '.' INT
    )?
    (
        (
            'e'
            | 'E'
        )
        (
            '+'
            | '-'
        )? INT
    )?
;

fragment
DIGIT
:
    [0-9]
;

INT
:
    DIGIT+
;

BooleanLiteralExpCS
:
    'true'
    | 'false'
;

NullLiteralExpCS
:
    'null'
;

// navigatingExpCS bleibt erhalten, weil OCL# erlaubt Collection-Navigation
navigatingExpCS
:
    opName = indexExpCS
    (
        '@' 'pre'
    )?
    (
        '(' '"'? onespace? barArg = navigatingBarAgrsCS* arg =
        navigatingArgCS* commaArg = navigatingCommaArgCS* semiArg =
        navigatingSemiAgrsCS* '"'? ')'
    )*
;

navigatingSemiAgrsCS
:
    ';' var = navigatingArgExpCS
    (
        ':' typeName = typeExpCS
    )?
    (
        '=' exp = infixedExpCS
    )?
    (
        '|' expCS
    )?
;

navigatingCommaArgCS
:
    ',' navigatingArgExpCS
    (
        ':' typeExpCS
    )?
    (
        '=' expCS+
    )?
;

navigatingArgExpCS
:
    iteratorVariable = infixedExpCS iteratorBarExpCS name = nameExpCS
    navigationOperatorCS body = infixedExpCS*
    | infixedExpCS+
;

navigatingBarAgrsCS
:
    '|' var = navigatingArgExpCS
    (
        ':' type = typeExpCS
    )?
    (
        '=' expCS+
    )?
;

navigatingArgCS
:
    navigatingArgExpCS
    (
        ':' typeExpCS
    )?
    (
        '=' expCS+
    )?
;

indexExpCS
:
    nameExpCS
    (
        '[' expCS
        (
            ',' expCS
        )* ']'
    )?
;

nameExpCS
:
    (
        (
            ID '::'
            (
                ID '::'
            )* ID
        )
        | variableName = ID
        | STRING
    ) # name
    | '$' clab = ID '$' # ontologicalName
    | '#' aspect = ID
    (
        '('
        (
            NumberLiteralExpCS
            | ID
        )?
        (
            ','
            (
                NumberLiteralExpCS
                | ID
            )
        )* ')'
    )? '#' # linguisticalName
;

parameterCS
:
    (
        ID ':' 
    )? typeExpCS
;

// invCS removed: OCL# does not support invariants
/*
invCS
:
    'inv'
        (
        ID
        (
            '(' specificationCS ')'
        )?
    )? ':' specificationCS
;
*/

classifierContextCS
:
    CONTEXT levelSpecificationCS?
    (
        ID ':' 
    )?
    (
        (
            ID '::'
            (
                ID '::'
            )* ID
        )
        | ID
    )
    (
        // invCS removed
        defCS
        // filterCS removed
    )*
;

propertyContextDeclCS
:
    CONTEXT levelSpecificationCS?
    (
        (
            ID '::'
            (
                ID '::'
            )* ID
        )
        | ID
    ) ':' typeExpCS
    /*
    (
         (
             initCS derCS?
         )?
         | derCS initCS?
     )
     */
;

// derCS und initCS removed
/*
    derCS : 'derive' ':' specificationCS ;
    initCS : 'init' ':' specificationCS ;
*/

ID
:
    [a-zA-Z] [a-zA-Z0-9]*
;

WS
:
    [ \t\n\r]+ -> skip
;

onespace
:
    ONESPACE
;

ONESPACE
:
    ' '
;

STRING
:
    UnterminatedStringLiteral '"'
;

UnterminatedStringLiteral
:
    '"'
    (
        ~["\\\r\n]
        | '\\'
        (
            .
            | EOF
        )
    )*
;

COMMENT
:
     '--' .*? '\n' -> skip
;
