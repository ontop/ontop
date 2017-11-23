grammar test;

//variable
//  : BRAC AWITHB
//  ;
//
//
//
////BASE:  ;
//
//BRAC: '{' ;
//
//AWITHB: 'a}';
//
//otherRule
//: BRAC AWITHB
////: '{a}'
//;


top : w+
  ;

w : TOKEN | kw;

kw : BASE | TOKEN | BASED;
//kw : TOKEN | 'base' ;

BASE : 'base' ;
TOKEN : [A-z]+ ;
BASED : 'based d' ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ -> channel(HIDDEN);

//
//PREFIX: ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');
//
//FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');
//
//TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');
//
//REFERENCE:     '^^';
//LTSIGN:        '<"';
//RTSIGN:        '">';
//SEMI:          ';';
//PERIOD:        '.';
//COMMA:         ',';
//LSQ_BRACKET:   '[';
//RSQ_BRACKET:   ']';
//LCR_BRACKET:   '{';
//RCR_BRACKET:   '}';
//LPAREN:        '(';
//RPAREN:        ')';
//QUESTION:      '?';
//DOLLAR:        '$';
//QUOTE_DOUBLE:  '"';
//QUOTE_SINGLE:  '\'';
//APOSTROPHE:    '`';
//UNDERSCORE:    '_';
//MINUS:         '-';
//ASTERISK:      '*';
//AMPERSAND:     '&';
//AT:            '@';
//EXCLAMATION:   '!';
//HASH:          '#';
//PERCENT:       '%';
//PLUS:          '+';
//EQUALS:        '=';
//COLON:         ':';
//LESS:          '<';
//GREATER:       '>';
//SLASH:         '/';
//DOUBLE_SLASH:  '//';
//BACKSLASH:     '\\';
//BLANK:	       '[]';
//BLANK_PREFIX:  '_:';
//TILDE:         '~';
//CARET:         '^';
//
//fragment ALPHA
//  : 'a'..'z'
//  | 'A'..'Z'
//  | '\u00C0'..'\u00D6'
//  | '\u00D8'..'\u00F6'
//  | '\u00F8'..'\u02FF'
//  | '\u0370'..'\u037D'
//  | '\u037F'..'\u1FFF'
//  | '\u200C'..'\u200D'
//  | '\u2070'..'\u218F'
//  | '\u2C00'..'\u2FEF'
//  | '\u3001'..'\uD7FF'
//  | '\uF900'..'\uFDCF'
//  | '\uFDF0'..'\uFFFD'
//  ;
//
//fragment DIGIT
//  : '0'..'9'
//  ;
//
//fragment ALPHANUM
//  : ALPHA
//  | DIGIT
//  ;
//
//fragment CHAR
//  : ALPHANUM
//  | UNDERSCORE
//  | MINUS
//  | PERIOD
//  ;
//
//INTEGER
//  : DIGIT+
//  ;
//
//DOUBLE
//  : DIGIT+ PERIOD DIGIT* ('e'|'E') ('-'|'+')? DIGIT*
//  | PERIOD DIGIT+ ('e'|'E') ('-'|'+')? DIGIT*
//  | DIGIT+ ('e'|'E') ('-'|'+')? DIGIT*
//  ;
//
//DECIMAL
//  : DIGIT+ PERIOD DIGIT+
//  | PERIOD DIGIT+
//  ;
//
//INTEGER_POSITIVE
//  : PLUS INTEGER
//  ;
//
//INTEGER_NEGATIVE
//  : MINUS INTEGER
//  ;
//
//DOUBLE_POSITIVE
//  : PLUS DOUBLE
//  ;
//
//DOUBLE_NEGATIVE
//  : MINUS DOUBLE
//  ;
//
//DECIMAL_POSITIVE
//  : PLUS DECIMAL
//  ;
//
//DECIMAL_NEGATIVE
//  : MINUS DECIMAL
//  ;
//
//VARNAME
//  : ALPHA CHAR*
//  ;
//
//fragment ECHAR
//  : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'')
//  ;
//
//fragment SCHEMA: ALPHA (ALPHANUM|PLUS|MINUS|PERIOD)*;
//
//fragment URI_PATH: (ALPHANUM|UNDERSCORE|MINUS|COLON|PERIOD|HASH|QUESTION|SLASH);
//
//fragment ID_START: (ALPHA|UNDERSCORE);
//
//fragment ID_CORE: (ID_START|DIGIT);
//
//fragment ID: ID_START (ID_CORE)*;
//
//fragment NAME_START_CHAR: (ALPHA|UNDERSCORE);
//
//fragment NAME_CHAR: (NAME_START_CHAR|DIGIT|MINUS|PERIOD|HASH|QUESTION|SLASH|PERCENT|EQUALS|SEMI);
//
//NCNAME
//  : NAME_START_CHAR (NAME_CHAR)*
//  ;
//
//NAMESPACE
//  : NAME_START_CHAR (NAME_CHAR)* COLON
//  ;
//
//PREFIXED_NAME
//  : (NCNAME)? COLON NCNAME_EXT
//  ;
//
//NCNAME_EXT
//  : (NAME_CHAR|LCR_BRACKET|RCR_BRACKET)*
//  ;
////NCNAME_EXT
////  : (NAME_CHAR|LCR_BRACKET|RCR_BRACKET|HASH|SLASH)*
////  ;
//
//STRING_WITH_QUOTE
//  : '\'' ( ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '\''
//  ;
//
//STRING_WITH_QUOTE_DOUBLE
//  : '"'  ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '"'
//  ;
//
//STRING_WITH_BRACKET
//  : '<' ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '>'
//  ;
//
//STRING_WITH_CURLY_BRACKET
//  : '{' ( ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )*? '}'
//  ;
//
//STRING_URI
//  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
//  ;
//
//WS: (' '|'\t'|('\n'|'\r'('\n')))+ -> channel(HIDDEN);
//
////NCNAME_EXT
////  : (NAME_CHAR|LCR_BRACKET|RCR_BRACKET)*
////  ;
