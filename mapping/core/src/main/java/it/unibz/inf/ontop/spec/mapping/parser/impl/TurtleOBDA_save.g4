/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

 /*
  HOW TO GENERATE JAVA FILES:

   $ cd CURRENT_DIRECTORY
   $ java -jar /path/to/antlr-3.5.2-complete.jar -visitor TurtleOBDA.g4
  */


grammar TurtleOBDA_save;

options {
  superClass = AbstractTurtleOBDAParser ;
}

@header {
import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
}



/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

parse
  : directiveStatement* triplesStatement+ EOF
  ;

directiveStatement
  : directive PERIOD
  ;

triplesStatement
  : triples WS* PERIOD
  ;

directive
  : base | prefixID
  ;

base
  : AT BASE uriref
  ;

prefixID
  : AT PREFIX (namespace | defaultNamespace) uriref
  ;

triples
  : subject  predicateObjectList
  ;

predicateObjectList
  : predicateObject (SEMI predicateObject)*
  ;

predicateObject
: verb objectList
;

verb
  : resource
  | 'a'
  ;

objectList
  : object (COMMA object)*
  ;

subject
  : resource | variable | blank
  ;

object
  : resource | literal | typedLiteral | variable
  ;

resource
   : uriref | qname
   ;

uriref
  : STRING_WITH_BRACKET
  ;

qname
  : PREFIXED_NAME
  ;

blank
  : nodeID | BLANK
  ;

variable
  : STRING_WITH_CURLY_BRACKET
  ;

function
  : resource LPAREN terms RPAREN
  ;

typedLiteral
  : variable AT language        # typedLiteral_1
  | variable REFERENCE resource # typedLiteral_2
  ;

language
  : languageTag | variable
  ;

terms
  : term (COMMA term)*
  ;

term
  : function | variable | literal
  ;

literal
  : stringLiteral (AT language)? | dataTypeString | numericLiteral | booleanLiteral
  ;

stringLiteral
  : STRING_WITH_QUOTE_DOUBLE
  ;

dataTypeString
  :  stringLiteral REFERENCE resource
  ;

numericLiteral
  : numericUnsigned | numericPositive | numericNegative
  ;

nodeID
  : BLANK_PREFIX name
  ;

relativeURI // Not used
  : STRING_URI
  ;

namespace
  : NAMESPACE
  ;

defaultNamespace
  : COLON
  ;

name
  : VARNAME
  ;

languageTag
  : VARNAME
  ;

booleanLiteral
  : TRUE | FALSE
  ;

numericUnsigned
  : INTEGER | DOUBLE | DECIMAL
  ;

numericPositive
  : INTEGER_POSITIVE | DOUBLE_POSITIVE | DECIMAL_POSITIVE
  ;

numericNegative
  : INTEGER_NEGATIVE | DOUBLE_NEGATIVE  | DECIMAL_NEGATIVE
  ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

BASE: ('B'|'b')('A'|'a')('S'|'s')('E'|'e');

PREFIX: ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

REFERENCE:     '^^';
LTSIGN:        '<"';
RTSIGN:        '">';
SEMI:          ';';
PERIOD:        '.';
COMMA:         ',';
LSQ_BRACKET:   '[';
RSQ_BRACKET:   ']';
LCR_BRACKET:   '{';
RCR_BRACKET:   '}';
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
SLASH:         '/';
DOUBLE_SLASH:  '//';
BACKSLASH:     '\\';
BLANK:	       '[]';
BLANK_PREFIX:  '_:';
TILDE:         '~';
CARET:         '^';

fragment ALPHA
  : 'a'..'z'
  | 'A'..'Z'
  | '\u00C0'..'\u00D6'
  | '\u00D8'..'\u00F6'
  | '\u00F8'..'\u02FF'
  | '\u0370'..'\u037D'
  | '\u037F'..'\u1FFF'
  | '\u200C'..'\u200D'
  | '\u2070'..'\u218F'
  | '\u2C00'..'\u2FEF'
  | '\u3001'..'\uD7FF'
  | '\uF900'..'\uFDCF'
  | '\uFDF0'..'\uFFFD'
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
  | PERIOD
  ;

INTEGER
  : DIGIT+
  ;

DOUBLE
  : DIGIT+ PERIOD DIGIT* ('e'|'E') ('-'|'+')? DIGIT*
  | PERIOD DIGIT+ ('e'|'E') ('-'|'+')? DIGIT*
  | DIGIT+ ('e'|'E') ('-'|'+')? DIGIT*
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

DOUBLE_POSITIVE
  : PLUS DOUBLE
  ;

DOUBLE_NEGATIVE
  : MINUS DOUBLE
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

fragment ECHAR
  : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'')
  ;

fragment SCHEMA: ALPHA (ALPHANUM|PLUS|MINUS|PERIOD)*;

fragment URI_PATH: (ALPHANUM|UNDERSCORE|MINUS|COLON|PERIOD|HASH|QUESTION|SLASH);

fragment ID_START: (ALPHA|UNDERSCORE);

fragment ID_CORE: (ID_START|DIGIT);

fragment ID: ID_START (ID_CORE)*;

fragment NAME_START_CHAR: (ALPHA|UNDERSCORE);

fragment NAME_CHAR: (NAME_START_CHAR|DIGIT|UNDERSCORE|MINUS|PERIOD|HASH|QUESTION|SLASH|PERCENT|EQUALS|SEMI);

NCNAME
  : NAME_START_CHAR (NAME_CHAR)*
  ;

NCNAME_EXT
  : (NAME_CHAR|LCR_BRACKET|RCR_BRACKET|HASH|SLASH)*
  ;

NAMESPACE
  : NAME_START_CHAR (NAME_CHAR)* COLON
  ;

PREFIXED_NAME
  : NCNAME? COLON NCNAME_EXT
  ;

STRING_WITH_QUOTE
  : '\'' ( ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '\''
  ;

STRING_WITH_QUOTE_DOUBLE
  : '"'  ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '"'
  ;

STRING_WITH_BRACKET
  : '<' ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '>'
  ;

STRING_WITH_CURLY_BRACKET
  : '{' ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '}'
  ;

STRING_URI
  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
  ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ -> channel(HIDDEN);

