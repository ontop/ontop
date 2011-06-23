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
  : query_expression (UNION (set_quantifier)? query_expression)*
  ;
  
query_expression
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
  | derived_column
  ;
  
qualified_asterisk
  : table_identifier DOT ASTERISK
  ;
  
derived_column
  : value_expression (AS? alias_name)?
  ;  
 
value_expression
  : string_value_expression
  | reference_value_expression
  | collection_value_expression
  ;

string_value_expression
  : LPAREN concatenation RPAREN
  ;
  
concatenation
  : concatenation_value (concatenation_operator concatenation_value)+
  ;

concatenation_value
  : column_reference
  | value
  ;

reference_value_expression
  : column_reference
  ;

column_reference
  : (table_identifier DOT)? column_name
  ;  
  
collection_value_expression
  : set_function_specification
  ;

set_function_specification
  : COUNT LPAREN ASTERISK RPAREN
  | general_set_function
  ;
  
general_set_function
  : set_function_op LPAREN (set_quantifier)? value_expression RPAREN
  ;
  
set_function_op
  : AVG
  | MAX
  | MIN
  | SUM
  | EVERY
  | ANY
  | SOME
  | COUNT
  ;  
    
table_expression
  : from_clause (where_clause)? (group_by_clause)?
  ;
  
from_clause
  : FROM table_reference_list
  ;  
  
table_reference_list
  : table_reference (COMMA table_reference)*
  ;
  
table_reference
options {
  backtrack=true;
}
  : table_primary (joined_table)?
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
  | in_predicate
  ;
  
comparison_predicate
  : value_expression comp_op (value|value_expression)
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

in_predicate
  : column_reference (NOT)? IN in_predicate_value
  ;
  
in_predicate_value
  : table_subquery
  | LPAREN value_list RPAREN
  ;

table_subquery
  : subquery
  ;

subquery
  : LPAREN query RPAREN
  ;
  
value_list
  : value (COMMA value)*
  ;

group_by_clause
  : GROUP BY grouping_element_list
  ;

grouping_element_list
  : grouping_element (COMMA grouping_element)*
  ;
  
grouping_element
  : grouping_column_reference
  | LPAREN grouping_column_reference_list RPAREN 
  ;
  
grouping_column_reference
  : column_reference
  ;  

grouping_column_reference_list
  : column_reference (COMMA column_reference)*
  ;  

joined_table
  : qualified_join
  ;

qualified_join
  : (join_type)? JOIN table_primary join_condition
  ;

join_type
  : INNER
  | outer_join_type (OUTER)?
  ;
  
outer_join_type
  : LEFT 
  | RIGHT 
  | FULL
  ;

join_condition
  : ON search_condition
  ;

table_primary
  : table_name (AS? alias_name)?
  | derived_table AS? alias_name
  ; 
 
table_name
  : (schema_name DOT)? table_identifier
  ;  

alias_name
  : identifier
  ;

derived_table
  : table_subquery
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
  : regular_identifier 
  | delimited_identifier
  ;

regular_identifier
  : STRING
  ;

delimited_identifier
  : STRING_WITH_QUOTE_DOUBLE
  ;

value
  : TRUE
  | FALSE
  | NUMERIC 
  | STRING_WITH_QUOTE
  ;

concatenation_operator
  : CONCATENATION
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

AVG: ('A'|'a')('V'|'v')('G'|'g');

MAX: ('M'|'m')('A'|'a')('X'|'x');

MIN: ('M'|'m')('I'|'i')('N'|'n');

SUM: ('S'|'s')('U'|'u')('M'|'m');

EVERY: ('E'|'e')('V'|'v')('E'|'e')('R'|'r')('Y'|'y');

ANY: ('A'|'a')('N'|'n')('Y'|'y');

SOME: ('S'|'s')('O'|'o')('M'|'m')('E'|'e');

COUNT: ('C'|'c')('O'|'o')('U'|'u')('N'|'n')('T'|'t');

FROM:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');

WHERE: ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

AND: ('A'|'a')('N'|'n')('D'|'d');

OR:	('O'|'o')('R'|'r');

NOT: ('N'|'n')('O'|'o')('T'|'t');

ORDER: ('O'|'o')('R'|'r')('D'|'d')('E'|'e')('R'|'r');

GROUP: ('G'|'g')('R'|'r')('O'|'o')('U'|'u')('P'|'p');

BY:	('B'|'b')('Y'|'y');

AS:	('A'|'a')('S'|'s');

JOIN: ('J'|'j')('O'|'o')('I'|'i')('N'|'n');

INNER: ('I'|'i')('N'|'n')('N'|'n')('E'|'e')('R'|'r');

OUTER: ('O'|'o')('U'|'u')('T'|'t')('E'|'e')('R'|'r');

LEFT: ('L'|'l')('E'|'e')('F'|'f')('T'|'t');

RIGHT: ('R'|'r')('I'|'i')('G'|'g')('H'|'h')('T'|'t');

FULL: ('F'|'f')('U'|'u')('L'|'l')('L'|'l');

UNION: ('U'|'u')('N'|'n')('I'|'i')('O'|'o')('N'|'n');

ON:	('O'|'o')('N'|'n');

IN: ('I'|'i')('N'|'n');

IS: ('I'|'i')('S'|'s');

NULL: ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

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
CONCATENATION: '||';

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

fragment ALPHANUM: (ALPHA|DIGIT);

fragment CHAR: (ALPHANUM|UNDERSCORE|DASH);

NUMERIC: DIGIT+;

STRING: CHAR*;

STRING_WITH_QUOTE_DOUBLE: QUOTE_DOUBLE CHAR* QUOTE_DOUBLE;

STRING_WITH_QUOTE: (QUOTE_SINGLE|QUOTE_DOUBLE) CHAR* (QUOTE_SINGLE|QUOTE_DOUBLE);

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};