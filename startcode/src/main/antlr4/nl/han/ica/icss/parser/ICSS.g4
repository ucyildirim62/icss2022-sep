grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: variableAssignment* styleRule* EOF;

selector
: classSelector
| idSelector
| tagSelector
;

classSelector: CLASS_IDENT;
idSelector: ID_IDENT;
tagSelector: LOWER_IDENT;

literal
: PIXELSIZE
| PERCENTAGE
| COLOR
| SCALAR
| boolLiteral
;

boolLiteral: TRUE | FALSE;

expresion
: expresion MUL expresion     #MulOperation
| expresion MIN expresion     #MinOperation
| expresion PLUS expresion    #AddOperation
| literal                 #LiteralExpression
| variableReference       #VariableReferenceExpression
;

variableReference: CAPITAL_IDENT;
variableAssignment: variableReference ASSIGNMENT_OPERATOR expresion SEMICOLON;

property: LOWER_IDENT;
declaration: (property COLON expresion SEMICOLON) | ifClause | variableAssignment;

ifClause: IF BOX_BRACKET_OPEN (variableReference | boolLiteral) BOX_BRACKET_CLOSE body elseClause?;
elseClause: ELSE body;

styleRule: selector body;

body: OPEN_BRACE declaration* CLOSE_BRACE;
