/*******************************************************************************
 * Copyright (c) 2012, 2016 University of Mannheim: Chair for Software Engineering
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralph Gerbig - initial API and implementation and initial documentation
 *    Arne Lange - ocl2 implementation
 *******************************************************************************/
 grammar VitruvOCL;



 contextDeclCS
 :
     classifierContextCS+
 ;


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

 CONTEXT
 :
     'context'
 ;


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
 ;

 typeNameExpCS
 :
     ID '::'
     (
         ID '::'
     )* ID
     | ID
 ;

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
         | '!='
         | '<'
         | '>'
         | '=='
     ) right = infixedExpCS # equalOperations
     | left = infixedExpCS op = '~' right = infixedExpCS # coextension
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
 ;

prefixedExpCS
:
    ('-' | 'not')* primaryExpCS (navigationOperatorCS primaryExpCS)*
;

 primaryExpCS
 :
     ifExpCS
     | primitiveLiteralExpCS
     | navigatingExpCS
     | selfExpCS
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


 selfExpCS
 :
     'self'
 ;

 primitiveLiteralExpCS
 :
     NumberLiteralExpCS # number
     | STRING # string
     | BooleanLiteralExpCS # boolean
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
            metamodelQualifiedName = ID '::' 
            (
                ID '::'
            )* ID
        )
        | (
            ID '::'
            (
                ID '::'
            )* ID
        )
        | variableName = ID
        | collectionOperationName
        | stringOperationName
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


// String Operations
stringOperationName
:
    'concat'
    | 'substring'
    | 'toUpper'
    | 'toLower'
    | 'indexOf'
    | 'equalsIgnoreCase'
;


collectionOperationName
:
    'including'
    | 'excluding'
    | 'includes'
    | 'excludes'
    | 'flatten'
    | 'union'
    | 'append'
    | 'sum'      
    | 'max'      
    | 'min'     
    | 'avg'      
    | 'abs'     
    | 'floor'    
    | 'ceil'     
    | 'round'   
    | 'lift' 
    | 'oclIsKindOf'
    | 'oclIsTypeOf'    
    | 'oclAsType' 
;


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

 classifierContextCS
 :
     CONTEXT levelSpecificationCS?
     (
         ID ':'
     )?
     (
         (
             metamodelName = ID '::'
             (
                 ID '::'
             )* ID
         )
         | (
             ID '::'
             (
                 ID '::'
             )* ID
         )
         | ID
     )
     invCS*
 ;


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