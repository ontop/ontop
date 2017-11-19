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
   $ java -jar /path/to/antlr-3.5.2-complete.jar -visitor TurtleOBDA_visitor.g4
  */


grammar TurtleOBDA_tmp;

options {
  superClass = AbstractTurtleOBDAParser ;
}

@header {
import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
}

//@lexer::header {
//package it.unibz.inf.ontop.spec.mapping.parser.impl;
//
//import java.util.Vector;
//}

//@lexer::members {
//private String error = "";
//
//public String getError() {
//   return error;
//}
//
//@Override
//public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
//   throw e;
//}
//
//@Override
//public void recover(IntStream input, RecognitionException re) {
//   throw new RuntimeException(error);
//}
//
//@Override
//public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
//   String hdr = getErrorHeader(e);
//   String msg = getErrorMessage(e, tokenNames);
//   emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
//}
//
//@Override
//public void emitErrorMessage(String msg) {
//   error = msg;
//   throw new RuntimeException(error);
//}
//
//@Override
//public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
//   throw new RecognitionException(input);
//}
//}

//@members {
///** Map of directives */
//private HashMap<String, String> directives = new HashMap<String, String>();
//
///** The current subject term */
//private Term currentSubject;
//
///** All variables */
//private Set<Term> variableSet = new HashSet<Term>();
//
//private String error = "";
//
//public String getError() {
//   return error;
//}
//
//protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
//   throw new MismatchedTokenException(ttype, input);
//}
//
//public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
//   throw e;
//}
//
//@Override
//public void recover(IntStream input, RecognitionException re) {
//   throw new RuntimeException(error);
//}
//
//@Override
//public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
//   String hdr = getErrorHeader(e);
//   String msg = getErrorMessage(e, tokenNames);
//   emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
//}
//
//@Override
//public void emitErrorMessage(String msg) {
//   error = msg;
//}
//
//@Override
//public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
//   throw new RecognitionException(input);
//}
//
//private String removeBrackets(String text) {
//   return text.substring(1, text.length()-1);
//}
//
//	private Term construct(String text) {
//	   Term toReturn = null;
//	   final String PLACEHOLDER = "{}";
//	   List<Term> terms = new LinkedList<Term>();
//	   List<FormatString> tokens = parse(text);
//	   int size = tokens.size();
//	   if (size == 1) {
//	      FormatString token = tokens.get(0);
//	      if (token instanceof FixedString) {
//	          ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(token.toString()); // a single URI template
//	          toReturn = TERM_FACTORY.getUriTemplate(uriTemplate);
//	      }
//	      else if (token instanceof ColumnString) {
//	         // a single URI template
//	         Variable column = TERM_FACTORY.getVariable(token.toString());
//	         toReturn = TERM_FACTORY.getUriTemplate(column);
//	      }
//	   }
//	   else {
//	      StringBuilder sb = new StringBuilder();
//	      for(FormatString token : tokens) {
//	         if (token instanceof FixedString) { // if part of URI template
//	            sb.append(token.toString());
//	         }
//	         else if (token instanceof ColumnString) {
//	            sb.append(PLACEHOLDER);
//	            Variable column = TERM_FACTORY.getVariable(token.toString());
//	            terms.add(column);
//	         }
//	      }
//	      ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(sb.toString()); // complete URI template
//	      terms.add(0, uriTemplate);
//	      toReturn = TERM_FACTORY.getUriTemplate(terms);
//	   }
//	   return toReturn;
//	}
//
//// Column placeholder pattern
//private static final String formatSpecifier = "\\{([^\\}]+)?\\}";
//private static Pattern chPattern = Pattern.compile(formatSpecifier);
//
//private List<FormatString> parse(String text) {
//   List<FormatString> toReturn = new ArrayList<FormatString>();
//   Matcher m = chPattern.matcher(text);
//   int i = 0;
//   while (i < text.length()) {
//      if (m.find(i)) {
//         if (m.start() != i) {
//            toReturn.add(new FixedString(text.substring(i, m.start())));
//         }
//         String value = m.group(1);
//         if(value.contains(".")){
//         	throw new IllegalArgumentException("Fully qualified columns are not accepted.");
//         }
//         toReturn.add(new ColumnString(value));
//         i = m.end();
//      }
//      else {
//         toReturn.add(new FixedString(text.substring(i)));
//         break;
//      }
//   }
//   return toReturn;
//}
//
//private interface FormatString {
//   int index();
//   String toString();
//}
//
//private class FixedString implements FormatString {
//   private String s;
//   FixedString(String s) { this.s = s; }
//   @Override public int index() { return -1; }  // flag code for fixed string
//   @Override public String toString() { return s; }
//}
//
//private class ColumnString implements FormatString {
//   private String s;
//   ColumnString(String s) { this.s = s; }
//   @Override public int index() { return 0; }  // flag code for column string
//   @Override public String toString() { return s; }
//}
//
//	//this function distinguishes curly bracket with
//	//back slash "\{" from curly bracket "{"
//	private int getIndexOfCurlyB(String str){
//	   int i;
//	   int j;
//	   i = str.indexOf("{");
//	   j = str.indexOf("\\{");
//	      while((i-1 == j) &&(j != -1)){
//		i = str.indexOf("{",i+1);
//		j = str.indexOf("\\{",j+1);
//	      }
//	  return i;
//	}
//
//	//in case of concat this function parses the literal
//	//and adds parsed constant literals and template literal to terms list
//	private ArrayList<Term> addToTermsList(String str){
//	   ArrayList<Term> terms = new ArrayList<Term>();
//	   int i,j;
//	   String st;
//	   str = str.substring(1, str.length()-1);
//	   while(str.contains("{")){
//	      i = getIndexOfCurlyB(str);
//	      if (i > 0){
//	    	 st = str.substring(0,i);
//	    	 st = st.replace("\\\\", "");
//	         terms.add(TERM_FACTORY.getConstantLiteral(st));
//	         str = str.substring(str.indexOf("{", i), str.length());
//	      }else if (i == 0){
//	         j = str.indexOf("}");
//	         terms.add(TERM_FACTORY.getVariable(str.substring(1,j)));
//	         str = str.substring(j+1,str.length());
//	      } else {
//	    	  break;
//	      }
//	   }
//	   if(!str.equals("")){
//	      str = str.replace("\\\\", "");
//	      terms.add(TERM_FACTORY.getConstantLiteral(str));
//	   }
//	   return terms;
//	}
//
//	//this function returns nested concats
//	//in case of more than two terms need to be concatted
//	private Term getNestedConcat(String str){
//	   ArrayList<Term> terms = new ArrayList<Term>();
//	   terms = addToTermsList(str);
//	   if(terms.size() == 1){
//	      Variable v = (Variable) terms.get(0);
//          variableSet.add(v);
//          return v;
//	   }
//
//       Function f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
//       for(int j=2;j<terms.size();j++) {
//          f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, f, terms.get(j));
//       }
//       return f;
//	}
//
///**
// * This methods construct an atom from a triple
// *
// * For the input (subject, pred, object), the result is
// * <ul>
// *  <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
// *  <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
// *  <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
// * </ul>
// */
//	private Function makeAtom(Term subject, Term pred, Term object) {
//	     Function atom = null;
//
//	        if (isRDFType(pred)) {
//		             if (object instanceof  Function) {
//		                  if(QueryUtils.isGrounded(object)) {
//		                      ValueConstant c = ((ValueConstant) ((Function) object).getTerm(0));  // it has to be a URI constant
//		                      Predicate predicate = TERM_FACTORY.getClassPredicate(c.getValue());
//		                      atom = TERM_FACTORY.getFunction(predicate, subject);
//		                  } else {
//		                       atom = ATOM_FACTORY.getTripleAtom(subject, pred, object);
//		                  }
//		             }
//		             else if (object instanceof  Variable){
//		                  Term uriOfPred = TERM_FACTORY.getUriTemplate(pred);
//		                  Term uriOfObject = TERM_FACTORY.getUriTemplate(object);
//		                  atom = ATOM_FACTORY.getTripleAtom(subject, uriOfPred,  uriOfObject);
//		              }
//		             else {
//		                  throw new IllegalArgumentException("parser cannot handle object " + object);
//		              }
//		        } else if( ! QueryUtils.isGrounded(pred) ){
//		             atom = ATOM_FACTORY.getTripleAtom(subject, pred,  object);
//		        } else {
//                			             //Predicate predicate = TERM_FACTORY.getPredicate(pred.toString(), 2); // the data type cannot be determined here!
//                			             Predicate predicate;
//                			             if(pred instanceof Function) {
//                							 ValueConstant pr = (ValueConstant) ((Function) pred).getTerm(0);
//                							 if (object instanceof Variable) {
//                								 predicate = TERM_FACTORY.getDataPropertyPredicate(pr.getValue());
//                							 } else {
//                								 if (object instanceof Function) {
//                									 if (((Function) object).getFunctionSymbol() instanceof URITemplatePredicate) {
//
//                										 predicate = TERM_FACTORY.getObjectPropertyPredicate(pr.getValue());
//                									 } else {
//                										 predicate = TERM_FACTORY.getDataPropertyPredicate(pr.getValue());
//                									 }
//                								 }
//                									 else {
//                										 throw new IllegalArgumentException("parser cannot handle object " + object);
//                									 }
//                							 }
//                						 }else {
//                			                  throw new IllegalArgumentException("predicate should be a URI Function");
//                			             }
//                			             atom = TERM_FACTORY.getFunction(predicate, subject, object);
//                			       }
//                			       return atom;
//	  }
//
//
//private static boolean isRDFType(Term pred) {
////		if (pred instanceof Constant && ((Constant) pred).getValue().equals(RDF_TYPE)) {
////			return true;
////		}
//		if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant ) {
//			String c= ((Constant) ((Function) pred).getTerm(0)).getValue();
//			return c.equals(RDF_TYPE);
//		}
//		return false;
//	}
//
//}


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
  : AT PREFIX namespace uriref  # prefixID_1
   | defaultNamespace uriref # prefixID_2
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
//  | blank
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
  : nodeID
  | BLANK
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

