grammar SqlBase;

singleStatement
    : statement EOF
    ;

statement
    : selectStatement    #QueryStatement
    | createStatement    #createTableStatement
    | showTable          #showTableStatement
    ;

showTable
    : SHOW TABLES
    ;

createStatement
    : CREATE TABLE tablenName=tableIdentifier '('identifier dataType (',' identifier dataType)* ')' fileMetaClause
    ;

dataType
    : IDENTIFIER
    ;

fileMetaClause
    : filePathClause separatorClause?
    ;

filePathClause
    : PATH path=STRING
    ;

separatorClause
    : SEPARATOR separator=STRING
    ;

selectStatement
    : selectClause fromCluse whereCluse?
    ;

selectClause
    : SELECT expression (',' expression)*
    ;    

fromCluse
    : FROM unresolvedRelation
    ;

unresolvedRelation
    : relationPrimary joinRelation*
    ;

relationPrimary
    : tableIdentifier (',' tableIdentifier)*
    | '(' selectStatement ')' identifier
    ;

joinRelation
    : JOIN right=unresolvedRelation joinCriteria?
    ;

joinCriteria
    : ON booleanExpression
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
    | primaryExpression 'AS' identifier                                               #alias
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
    : EQ | NEQ | LT | LTE | GT | GTE
    ;

booleanValue
    : TRUE_ | FALSE_
    ;

number
    : MINUS? INTEGER_LITERAL          #integerLiteral
    | MINUS? DOUBLE_LITERAL            #doubleLiteral
    ;

// key words
SEPARATOR: 'SEPARATOR';
PATH: 'PATH';
WITH: 'WITH';
SELECT: 'SELECT';
FROM  : 'FROM';
WHERE : 'WHERE';
JOIN : 'JOIN';
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
NOT : 'NOT' | '!';

TRUE_  : 'TRUE';
FALSE_ : 'FALSE';

EQ  : '=' | '==';
NEQ : '<>' | '!=';
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

DOUBLE_LITERAL
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