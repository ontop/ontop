grammar SQL99;

@header {
package it.unibz.krdb.obda.parser;
}

@lexer::header {
package it.unibz.krdb.obda.parser;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

parse
  : query EOF
  ;

query 	
  : SELECT set_quantifier? select_list table_expression
  ;
  
set_quantifier
  : DISTINCT 
  | ALL
  ;
  
select_list
  : ASTERISK
  | select_sublist (COMMA select_sublist)*
  ;
  
select_sublist
  : qualified_asterisk
  | column_reference
  ;
  
qualified_asterisk
  : table_identifier DOT ASTERISK
  ;
  
column_reference
  : (table_identifier DOT)? column_name
  ;
    
table_expression
  : from_clause (where_clause)?
  ;
  
from_clause
  : FROM table_reference_list
  ;  
  
table_reference_list
  : table_reference (COMMA table_reference)*
  ;
  
table_reference
  : table_primary 
  | joined_table
  ;

where_clause
  : WHERE search_condition
  ;

search_condition
  : boolean_value_expression
  ;
  
boolean_value_expression
  : boolean_term ((OR|AND) boolean_term)*
  ;

boolean_term
  : predicate
  ;
  
predicate
  : comparison_predicate
  | null_predicate
  ;
  
comparison_predicate
  : column_reference comp_op value
  | column_reference comp_op column_reference
  ;

comp_op
  : equals_operator
  | not_equals_operator
  | less_than_operator
  | greater_than_operator
  | less_than_or_equals_operator
  | greater_than_or_equals_operator
  ;

null_predicate
  : column_reference IS (NOT)? NULL
  ;

joined_table
  : JOIN
  ;

table_primary
  : table_name (AS alias_name)?
  ; 
 
table_name
  : (schema_name DOT)? table_identifier
  ;  

alias_name
  : identifier
  ;
    
table_identifier
  : identifier
  ;
  
schema_name
  : identifier
  ;
    
column_name
  : identifier
  ;
  
identifier
  : STRING 
  | STRING_WITH_QUOTE
  ;

value
  : NUMERIC 
  | STRING_WITH_QUOTE
  ;

equals_operator
  : EQUALS
  ;

not_equals_operator
  : LESS GREATER
  ;
  
less_than_operator
  : LESS
  ;
  
greater_than_operator
  : GREATER
  ;
 
less_than_or_equals_operator
  : LESS EQUALS
  ;
 
greater_than_or_equals_operator
  : GREATER EQUALS
  ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

SELECT: ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

DISTINCT:	('D'|'d')('I'|'i')('S'|'s')('T'|'t')('I'|'i')('N'|'n')('C'|'c')('T'|'t');

ALL: ('A'|'a')('L'|'l')('L'|'l');

FROM:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');

WHERE: ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

AND: ('A'|'a')('N'|'n')('D'|'d');

OR:	('O'|'o')('R'|'r');

NOT: ('N'|'n')('O'|'o')('T'|'t');

ORDER: ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');

BY:	('B'|'b')('Y'|'y');

AS:	('A'|'a')('S'|'s');

JOIN: ('J'|'j')('O'|'o')('I'|'i')('N'|'n');

ON:	('O'|'o')('N'|'n');

LEFT:	('L'|'l')('E'|'e')('F'|'f')('T'|'t');

RIGHT: ('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t');

IS: ('I'|'i')('S'|'s');

NULL: ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

SEMI:          ';';
DOT:           '.';
COMMA:         ',';
LSQ_BRACKET:   '[';
RSQ_BRACKET:   ']';
LPAREN:        '(';
RPAREN:        ')';
QUESTION:      '?';
DOLLAR:        '$';
QUOTE_DOUBLE:  '"';
QUOTE_SINGLE:  '\'';
APOSTROPHE:    '`';
UNDERSCORE:    '_';
DASH:          '-';
ASTERISK:      '*';
AMPERSAND:     '&';
AT:            '@';
EXCLAMATION:   '!';
HASH:          '#';
PERCENT:       '%';
PLUS:          '+';
EQUALS:        '=';
COLON:         ':';
LESS:          '<';
GREATER:       '>';
SLASH:         '/';
DOUBLE_SLASH:  '//';
BACKSLASH:     '\\';
TILDE:         '~';
CARET:         '^';

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

fragment ALPHANUM: (ALPHA|DIGIT);

fragment CHAR: (ALPHANUM|UNDERSCORE|DASH);

NUMERIC: DIGIT+;

STRING: CHAR*;

STRING_WITH_QUOTE: QUOTE_DOUBLE CHAR* QUOTE_DOUBLE;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};