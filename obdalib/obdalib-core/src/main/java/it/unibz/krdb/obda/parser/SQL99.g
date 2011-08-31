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
  : query_specification (UNION (set_quantifier)? query_specification)*
  ;
  
query_specification
  : select_clause table_expression
  ;

select_clause
  : SELECT set_quantifier? (
      ASTERISK
      | select_list)
  ; 

set_quantifier
  : DISTINCT 
  | ALL
  ;
  
select_list
  : select_sublist (COMMA select_sublist)*
  ;
  
select_sublist
  : qualified_asterisk
  | derived_column
  ;
  
qualified_asterisk
  : table_identifier PERIOD ASTERISK
  ;
  
derived_column
  : value_expression (AS? alias_name)?
  ;  
 
value_expression
  : numeric_value_expression
  | string_value_expression
  | reference_value_expression
  | collection_value_expression
  ;

numeric_value_expression
  : LPAREN numeric_operation RPAREN
  ;

numeric_operation
  : term ((PLUS|MINUS) term)*
  ;

term
  : factor ((ASTERISK|SOLIDUS) factor)*
  ;
  
factor
  : column_reference
  | numeric_literal
  ;

sign
  : PLUS
  | MINUS
  ;

string_value_expression
  : LPAREN concatenation RPAREN
  ;
  
concatenation
  : concatenation_value (concatenation_operator concatenation_value)+
  ;

concatenation_value
  : column_reference
  | general_literal
  ;

reference_value_expression
  : column_reference
  ;

column_reference
  : (table_identifier PERIOD)? column_name
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

row_value_expression
  : literal
  | value_expression
  ;

literal
  : numeric_literal
  | general_literal
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
  : table_primary (joined_table)?
  ;

where_clause
  : WHERE search_condition
  ;

search_condition
  : boolean_value_expression
  ;
  
boolean_value_expression
  : boolean_term (OR boolean_term)*
  ;
  
boolean_term
  : boolean_factor (AND boolean_factor)*
  ;
  
boolean_factor
  : (NOT)? boolean_test
  ;

boolean_test
  : boolean_primary (IS (NOT)? truth_value)?
  ;

boolean_primary
  : predicate
  | parenthesized_boolean_value_expression
  ;

parenthesized_boolean_value_expression
  : LPAREN boolean_value_expression RPAREN
  ;
 
predicate
  : comparison_predicate
  | null_predicate
  | in_predicate
  ;
  
comparison_predicate
  : row_value_expression comp_op (row_value_expression)
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
  | LPAREN in_value_list RPAREN
  ;

table_subquery
  : subquery
  ;

subquery
  : LPAREN query RPAREN
  ;
  
in_value_list
  : row_value_expression (COMMA row_value_expression)*
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
  : ((join_type)? JOIN table_reference join_specification)+
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

join_specification
  : join_condition
  | named_columns_join
  ;

join_condition
  : ON search_condition
  ;

named_columns_join
  : USING LPAREN join_column_list RPAREN
  ;

join_column_list
  : column_name (COMMA column_name)*
  ;

table_primary
  : table_name (AS? alias_name)?
  | derived_table AS? alias_name
  ; 
 
table_name
  : (schema_name PERIOD)? table_identifier
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
  : VARNAME
  ;

delimited_identifier
  : STRING_WITH_QUOTE_DOUBLE
  ;

general_literal
  : TRUE
  | FALSE
  | STRING_WITH_QUOTE
  ;

numeric_literal
  : INTEGER
  | DECIMAL
  | INTEGER_POSITIVE
  | DECIMAL_POSITIVE
  | INTEGER_NEGATIVE
  | DECIMAL_NEGATIVE
  ;

truth_value
  : TRUE
  | FALSE
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

USING: ('U'|'u')('S'|'s')('I'|'i')('N'|'n')('G'|'g');

ON:	('O'|'o')('N'|'n');

IN: ('I'|'i')('N'|'n');

IS: ('I'|'i')('S'|'s');

NULL: ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

SEMI:          ';';
PERIOD:        '.';
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
MINUS:         '-';
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
SOLIDUS:       '/';
DOUBLE_SLASH:  '//';
BACKSLASH:     '\\';
TILDE:         '~';
CARET:         '^';
CONCATENATION: '||';

fragment ALPHA
  : 'a'..'z'
  | 'A'..'Z'
  ;

fragment DIGIT
  : '0'..'9'
  ; 

fragment ALPHANUM
  : ALPHA
  | DIGIT
  ;

fragment CHAR
  : ALPHANUM
  | UNDERSCORE
  | MINUS
  ;

fragment ECHAR
  : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'')
  ;

INTEGER
  : DIGIT+
  ;

DECIMAL
  : DIGIT+ PERIOD DIGIT+
  | PERIOD DIGIT+
  ;
  
INTEGER_POSITIVE
  : PLUS INTEGER
  ;

INTEGER_NEGATIVE
  : MINUS INTEGER
  ;	  

DECIMAL_POSITIVE
  : PLUS DECIMAL
  ;
  
DECIMAL_NEGATIVE
  : MINUS DECIMAL
  ;

VARNAME
  : ALPHA CHAR*
  ;

STRING_WITH_QUOTE
  : '\'' ( options {greedy=false  ;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
  ;

STRING_WITH_QUOTE_DOUBLE
  : '"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
  ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};