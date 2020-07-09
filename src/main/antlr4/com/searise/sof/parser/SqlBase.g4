grammar SqlBase;

singleStatement
    : statement EOF
    ;

statement
    : selectStatement    #QueryStatement
    ;
    
selectStatement
    : selectClause fromCluse whereCluse?
    ;

selectClause
    : SELECT expression (',' expression)*
    ;    

fromCluse
    : FROM tableIdentifier (',' tableIdentifier)*
    | FROM '(' selectStatement ')' identifier
    ;

whereCluse
    : WHERE expression
    ;

expression
    : booleanExpression
    ;

booleanExpression
    : valueExpression                                                          #booleanExpressionDefaule
    | NOT booleanExpression                                                    #logicalNot
    | left=booleanExpression opt=(AND | OR) right=booleanExpression            #logicalBinary
    ;

valueExpression
    : primaryExpression                                                               #valueExpressionDefault
    | MINUS valueExpression                                                           #arithmeticUnary  
    | left=valueExpression opt=(ASTERISK | SLASH | PERCENT) right=valueExpression     #arithmeticBinary
    | left=valueExpression opt=(PLUS | MINUS ) right=valueExpression                  #arithmeticBinary
    | left=valueExpression comparisonOperator right=valueExpression                   #comparison
    ;

primaryExpression
    : columnIdentifier      #columnReference
    | constant              #constantValue
    | '(' expression ')'    #parenthesizedExpression
    ;

constant
    : STRING            #stringLiteral
    | number            #numberLiteral
    | booleanValue      #booleanLiteral
    ;

columnIdentifier
    : tableIdentifier '.' identifier    #columnWithTable
    | identifier                        #columnWithoutTable
    ;

tableIdentifier
    : tableName=identifier                        #tableIdentifierDefault
    | tableName=identifier AS alias=identifier    #tableAlias
    ;

identifier
    : IDENTIFIER
    ;

comparisonOperator
    : EQ | NEQ | NEQJ | LT | LTE | GT | GTE 
    ;

booleanValue
    : TRUE_ | FALSE_
    ;

number
    : MINUS? INTEGER_LITERAL          #integerLiteral
    | MINUS? FLOAT_LITERAL            #floatLiteral
    ;

// key words
SELECT: 'SELECT';
FROM  : 'FROM';
WHERE : 'WHERE';
INSERT: 'INSERT';
INTO  : 'INTO';
VALUES: 'VALUES';
DELETE: 'DELETE';
CREATE: 'CREATE';
COPY  : 'COPY';  
DELIMITER : 'DELIMITER';
CSV   : 'CSV';
HEADER: 'HEADER';
TABLE : 'TABLE';
TABLES: 'TABLES';
SHOW  : 'SHOW';
SCHEMA: 'SCHEMA';
INDEX : 'INDEX';
ON    : 'ON';
AS    : 'AS';

OR  : 'OR';
AND : 'AND';
NOT : 'NOT';

// avoid conflict in C++
TRUE_  : 'TRUE';
FALSE_ : 'FALSE';

EQ  : '=' | '==';
NEQ : '<>';
NEQJ: '!=';
LT  : '<';
LTE : '<=' | '!>';
GT  : '>';
GTE : '>=' | '!<';

// Binary operators
PLUS: '+';
MINUS: '-';
ASTERISK: '*';
SLASH: '/';
PERCENT: '%';

STRING
    : '\'' ( ~('\''|'\\') | ('\\' .) )* '\''
    | '"' ( ~('"'|'\\') | ('\\' .) )* '"'
    ;

FLOAT_LITERAL
    : DIGIT+ '.' DIGIT+
    ; 


INTEGER_LITERAL
    : DIGIT+
    ;

IDENTIFIER
    : (LETTER | DIGIT | '_')+
    ;

fragment DIGIT
    : [0-9]
    ;
fragment LETTER
    : [A-Z]
    ;

WS
    : [ \r\n\t]+ -> channel(HIDDEN)
    ;