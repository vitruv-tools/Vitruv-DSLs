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
     (
         propertyContextDeclCS
         | classifierContextCS
         | operationContextCS
     )+
 ;

 operationContextCS
 :
     CONTEXT levelSpecificationCS?
     (
         ID ':'
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
     (
         preCS
         | postCS
         | bodyCS
     )*
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

 bodyCS
 :
     'body' ID? ':' specificationCS
 ;

 postCS
 :
     'post' ID? ':' specificationCS
 ;

 preCS
 :
     'pre' ID? ':' specificationCS
 ;
 
  filterCS
 :
     'filter' ID? ':' specificationCS
 ;
 

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

 typeExpCS
 :
     typeNameExpCS
     | typeLiteralCS
 ;

 typeLiteralCS
 :
     primitiveTypeCS
     | collectionTypeCS
     | tupleTypeCS
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

 tuplePartCS
 :
     ID ':' typeExpCS
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
     | 'OclInvalID'
     | 'OclVoID'
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
     | '->' # arrow
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

 primaryExpCS
 :
     letExpCS
     | ifExpCS
     | primitiveLiteralExpCS
     | navigatingExpCS
     | selfExpCS
     | tupleLiteralExpCS
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

 selfExpCS
 :
     'self'
 ;

 primitiveLiteralExpCS
 :
     NumberLiteralExpCS # number
     | STRING # string
     | BooleanLiteralExpCS # boolean
     | InvalIDLiteralExpCS # invalid
     | NullLiteralExpCS # null
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
             ID '::'
             (
                 ID '::'
             )* ID
         )
         | ID
     )
     (
         invCS
         | defCS
         | filterCS
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
     (
         (
             initCS derCS?
         )?
         | derCS initCS?
     )
 ;

 derCS
 :
     'derive' ':' specificationCS
 ;

 initCS
 :
     'init' ':' specificationCS
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