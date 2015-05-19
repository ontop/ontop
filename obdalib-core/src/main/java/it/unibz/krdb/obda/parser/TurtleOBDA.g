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
 
grammar TurtleOBDA;

@header {
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.utils.QueryUtils;
import it.unibz.krdb.obda.model.URITemplatePredicate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

}

@lexer::header {
package it.unibz.krdb.obda.parser;

import java.util.List;
import java.util.Vector;
}

@lexer::members {
private String error = "";
    
public String getError() {
   return error;
}

@Override
public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
   throw e;
}

@Override
public void recover(IntStream input, RecognitionException re) {
   throw new RuntimeException(error);
}
    
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
   String hdr = getErrorHeader(e);
   String msg = getErrorMessage(e, tokenNames);
   emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
}

@Override
public void emitErrorMessage(String msg) {
   error = msg;
   throw new RuntimeException(error);
}
    
@Override
public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
   throw new RecognitionException(input);
}
}

@members {
/** Map of directives */
private HashMap<String, String> directives = new HashMap<String, String>();

/** The current subject term */
private Term currentSubject;

/** All variables */
private Set<Term> variableSet = new HashSet<Term>();

/** A factory to construct the predicates and terms */
private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

private String error = "";

public String getError() {
   return error;
}

protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
   throw new MismatchedTokenException(ttype, input);
}

public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
   throw e;
}

@Override
public void recover(IntStream input, RecognitionException re) {
   throw new RuntimeException(error);
}

@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
   String hdr = getErrorHeader(e);
   String msg = getErrorMessage(e, tokenNames);
   emitErrorMessage("Syntax error: " + msg + " Location: " + hdr);
}

@Override
public void emitErrorMessage(String msg) {
   error = msg;
}
    
public Object recoverFromMismatchedTokenrecoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
   throw new RecognitionException(input);
}

private String removeBrackets(String text) {
   return text.substring(1, text.length()-1);
}

	private Term construct(String text) {
	   Term toReturn = null;
	   final String PLACEHOLDER = "{}";
	   List<Term> terms = new LinkedList<Term>();
	   List<FormatString> tokens = parse(text);
	   int size = tokens.size();
	   if (size == 1) {
	      FormatString token = tokens.get(0);
	      if (token instanceof FixedString) {
	          ValueConstant uriTemplate = dfac.getConstantLiteral(token.toString()); // a single URI template
	          toReturn = dfac.getUriTemplate(uriTemplate);
	      }
	      else if (token instanceof ColumnString) {
	         ValueConstant uriTemplate = dfac.getConstantLiteral(PLACEHOLDER); // a single URI template
	         Variable column = dfac.getVariable(token.toString());
	         terms.add(0, uriTemplate);
	         terms.add(column);
	         toReturn = dfac.getUriTemplate(terms);
	      }
	   }
	   else {
	      StringBuilder sb = new StringBuilder();
	      for(FormatString token : tokens) {
	         if (token instanceof FixedString) { // if part of URI template
	            sb.append(token.toString());
	         }
	         else if (token instanceof ColumnString) {
	            sb.append(PLACEHOLDER);
	            Variable column = dfac.getVariable(token.toString());
	            terms.add(column);
	         }
	      }
	      ValueConstant uriTemplate = dfac.getConstantLiteral(sb.toString()); // complete URI template
	      terms.add(0, uriTemplate);
	      toReturn = dfac.getUriTemplate(terms);
	   }
	   return toReturn;
	}
	
// Column placeholder pattern
private static final String formatSpecifier = "\\{([^\\}]+)?\\}";
private static Pattern chPattern = Pattern.compile(formatSpecifier);

private List<FormatString> parse(String text) {
   List<FormatString> toReturn = new ArrayList<FormatString>();
   Matcher m = chPattern.matcher(text);
   int i = 0;
   while (i < text.length()) {
      if (m.find(i)) {
         if (m.start() != i) {
            toReturn.add(new FixedString(text.substring(i, m.start())));
         }
         String value = m.group(1);
         toReturn.add(new ColumnString(value));
         i = m.end();
      }
      else {
         toReturn.add(new FixedString(text.substring(i)));
         break;
      }
   }
   return toReturn;
}

private interface FormatString {
   int index();
   String toString();
}

private class FixedString implements FormatString {
   private String s;
   FixedString(String s) { this.s = s; }
   @Override public int index() { return -1; }  // flag code for fixed string
   @Override public String toString() { return s; }
}

private class ColumnString implements FormatString {
   private String s;
   ColumnString(String s) { this.s = s; }
   @Override public int index() { return 0; }  // flag code for column string
   @Override public String toString() { return s; }
}

	//this function distinguishes curly bracket with 
	//back slash "\{" from curly bracket "{" 
	private int getIndexOfCurlyB(String str){
	   int i;
	   int j;
	   i = str.indexOf("{");
	   j = str.indexOf("\\{");
	      while((i-1 == j) &&(j != -1)){		
		i = str.indexOf("{",i+1);
		j = str.indexOf("\\{",j+1);		
	      }	
	  return i;
	}
	
	//in case of concat this function parses the literal 
	//and adds parsed constant literals and template literal to terms list
	private ArrayList<Term> addToTermsList(String str){
	   ArrayList<Term> terms = new ArrayList<Term>();
	   int i,j;
	   String st;
	   str = str.substring(1, str.length()-1);
	   while(str.contains("{")){
	      i = getIndexOfCurlyB(str);
	      if (i > 0){
	    	 st = str.substring(0,i);
	    	 st = st.replace("\\\\", "");
	         terms.add(dfac.getConstantLiteral(st));
	         str = str.substring(str.indexOf("{", i), str.length());
	      }else if (i == 0){
	         j = str.indexOf("}");
	         terms.add(dfac.getVariable(str.substring(1,j)));
	         str = str.substring(j+1,str.length());
	      } else {
	    	  break;
	      }
	   }
	   if(!str.equals("")){
	      str = str.replace("\\\\", "");
	      terms.add(dfac.getConstantLiteral(str));
	   }
	   return terms;
	}
	
	//this function returns nested concats 
	//in case of more than two terms need to be concatted
	private Function getNestedConcat(String str){
	   ArrayList<Term> terms = new ArrayList<Term>();
	   terms = addToTermsList(str);
	   Function f = dfac.getFunctionConcat(terms.get(0),terms.get(1));
           for(int j=2;j<terms.size();j++){
              f = dfac.getFunctionConcat(f,terms.get(j));
           }
	   return f;
	}

/**
 * This methods construct an atom from a triple 
 * 
 * For the input (subject, pred, object), the result is 
 * <ul>
 *  <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
 *  <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
 *  <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
 * </ul>
 */
	private Function makeAtom(Term subject, Term pred, Term object) {
	     Function atom = null;

	        if (isRDFType(pred)) {
		             if (object instanceof  Function) {
		                  if(QueryUtils.isGrounded(object)) {
		                      ValueConstant c = ((ValueConstant) ((Function) object).getTerm(0));  // it has to be a URI constant
		                      Predicate predicate = dfac.getClassPredicate(c.getValue());
		                      atom = dfac.getFunction(predicate, subject);
		                  } else {
		                       atom = dfac.getTripleAtom(subject, pred, object);
		                  }
		             }
		             else if (object instanceof  Variable){
		                  Term uriOfPred = dfac.getUriTemplate(pred);
		                  Term uriOfObject = dfac.getUriTemplate(object);
		                  atom = dfac.getTripleAtom(subject, uriOfPred,  uriOfObject);
		              }
		             else {
		                  throw new IllegalArgumentException("parser cannot handle object " + object);
		              }
		        } else if( ! QueryUtils.isGrounded(pred) ){
		             atom = dfac.getTripleAtom(subject, pred,  object);
		        } else {
                			             //Predicate predicate = dfac.getPredicate(pred.toString(), 2); // the data type cannot be determined here!
                			             Predicate predicate;
                			             if(pred instanceof Function) {
                							 ValueConstant pr = (ValueConstant) ((Function) pred).getTerm(0);
                							 if (object instanceof Variable) {
                								 predicate = dfac.getPredicate(pr.getValue(), 2);
                							 } else {
                								 if (object instanceof Function) {
                									 if (((Function) object).getFunctionSymbol() instanceof URITemplatePredicate) {

                										 predicate = dfac.getObjectPropertyPredicate(pr.getValue());
                									 } else {
                										 predicate = dfac.getDataPropertyPredicate(pr.getValue());
                									 }
                								 }
                									 else {
                										 throw new IllegalArgumentException("parser cannot handle object " + object);
                									 }
                							 }
                						 }else {
                			                  throw new IllegalArgumentException("predicate should be a URI Function");
                			             }
                			             atom = dfac.getFunction(predicate, subject, object);
                			       }
                			       return atom;
	  }


private static boolean isRDFType(Term pred) {
//		if (pred instanceof Constant && ((Constant) pred).getValue().equals(OBDAVocabulary.RDF_TYPE)) {
//			return true;
//		}
		if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant ) {
			String c= ((Constant) ((Function) pred).getTerm(0)).getValue();
			return c.equals(OBDAVocabulary.RDF_TYPE);
		}	
		return false;
	}

}


/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

parse returns [CQIE value]
  : directiveStatement*
    t1=triplesStatement {
      int arity = variableSet.size();
      List<Term> distinguishVariables = new ArrayList<Term>(variableSet);
      Function head = dfac.getFunction(dfac.getPredicate(OBDALibConstants.QUERY_HEAD, arity), distinguishVariables);
      
      // Create a new rule
      List<Function> triples = $t1.value;
      $value = dfac.getCQIE(head, triples);
    }
    (t2=triplesStatement)* EOF {
      List<Function> additionalTriples = $t2.value;
      if (additionalTriples != null) {
        // If there are additional triple statements then just add to the existing body
        List<Function> existingBody = $value.getBody();
        existingBody.addAll(additionalTriples);
      }
    }
  ;

directiveStatement
  : directive PERIOD
  ;

triplesStatement returns [List<Function> value]
  : triples WS* PERIOD { $value = $triples.value; }
  ;

directive
  : base
  | prefixID
  ;

base
  : AT BASE uriref
  ;

prefixID
@init {
  String prefix = "";
}
  : AT PREFIX (namespace { prefix = $namespace.text; } | defaultNamespace { prefix = $defaultNamespace.text; }) uriref {
      String uriref = $uriref.value;
      directives.put(prefix.substring(0, prefix.length()-1), uriref); // remove the end colon
    }
  ;

triples returns [List<Function> value]
  : subject { currentSubject = $subject.value; } predicateObjectList {
      $value = $predicateObjectList.value;
    }
  ;

predicateObjectList returns [List<Function> value]
@init {
   $value = new LinkedList<Function>();
}
  : v1=verb  l1= objectList{
      for (Term object : $l1.value) {
        Function atom = makeAtom(currentSubject, $v1.value, object);
        $value.add(atom);
      }
    } 
    (SEMI v2=verb l2=objectList {
      for (Term object : $l2.value) {
        Function atom = makeAtom(currentSubject, $v2.value, object);
        $value.add(atom);
      }
    })*
  ;
  
//verb returns [String value]
verb returns [Term value]
  : predicate { $value = $predicate.value; }
  | 'a' {
  Term constant = dfac.getConstantLiteral(OBDAVocabulary.RDF_TYPE);
  $value = dfac.getUriTemplate(constant);
  }
  ;

objectList returns [List<Term> value]
@init {
  $value = new ArrayList<Term>();
}
  : o1=object { $value.add($o1.value); } (COMMA o2=object { $value.add($o2.value); })* 
  ;

subject returns [Term value]
  : resource { $value = $resource.value; }
  | variable { $value = $variable.value; }
//  | blank
  ;

//predicate returns [String value]
predicate returns [Term value]
  : resource {
  	$value = $resource.value; 
//      Term nl = $resource.value;
//      if (nl instanceof URIConstant) {
//        URIConstant c = (URIConstant) nl;
//        $value = c.getValue();
//      } else {
//        throw new RuntimeException("Unsupported predicate syntax: " + nl.toString());
//      }
    }
  ;

object returns [Term value]
  : resource { $value = $resource.value; }
  | literal  { $value = $literal.value; }
  | typedLiteral { $value = $typedLiteral.value; }
  | variable { $value = $variable.value; }
//  | blank
  ;

resource returns [Term value]
   : uriref { $value = construct($uriref.value); }
   | qname { $value = construct($qname.value); }
  //: uriref { $value = dfac.getConstantURI($uriref.value);}
  // | qname { $value = dfac.getConstantURI($qname.value); }
  ;

uriref returns [String value]
  : STRING_WITH_BRACKET { $value = removeBrackets($STRING_WITH_BRACKET.text); }
  ;

qname returns [String value]
  : PREFIXED_NAME {
      String[] tokens = $PREFIXED_NAME.text.split(":", 2);
      String uri = directives.get(tokens[0]);  // the first token is the prefix
      $value = uri + tokens[1];  // the second token is the local name
    }
  ;

blank
  : nodeID
  | BLANK
  ;

variable returns [Variable value]
  : STRING_WITH_CURLY_BRACKET {
      $value = dfac.getVariable(removeBrackets($STRING_WITH_CURLY_BRACKET.text));
      variableSet.add($value);
    }
  ;
  
function returns [Function value]
  : resource LPAREN terms RPAREN {
      String functionName = $resource.value.toString();
      int arity = $terms.value.size();
      Predicate functionSymbol = dfac.getPredicate(functionName, arity);
      $value = dfac.getFunction(functionSymbol, $terms.value);
    }
  ;

typedLiteral returns [Function value]
  : variable AT language {
      Variable var = $variable.value;
      Term lang = $language.value;   
      $value = dfac.getTypedTerm(var, lang);

    }
  | variable REFERENCE resource {
      Variable var = $variable.value;
      //String functionName = $resource.value.toString();
      // $resource.value must be a URIConstant
    String functionName = null;
    if ($resource.value instanceof Function){
       functionName = ((ValueConstant) ((Function)$resource.value).getTerm(0)).getValue();
    } else {
        throw new IllegalArgumentException("$resource.value should be an URI");
    }
    Predicate.COL_TYPE type = dtfac.getDatatype(functionName);
    if (type == null)  
 	  throw new RuntimeException("ERROR. A mapping involves an unsupported datatype. \nOffending datatype:" + functionName);
    
      $value = dfac.getTypedTerm(var, type);

	
     }
  ;

language returns [Term value]
  : languageTag {
    	$value = dfac.getConstantLiteral($languageTag.text.toLowerCase(), COL_TYPE.STRING);
    }
  | variable {
    	$value = $variable.value;
    }
  ;

terms returns [Vector<Term> value]
@init {
  $value = new Vector<Term>();
}
  : t1=term { $value.add($t1.value); } (COMMA t2=term { $value.add($t2.value); })*
  ;

term returns [Term value]
  : function { $value = $function.value; }
  | variable { $value = $variable.value; }
  | literal { $value = $literal.value; }
  ;
/*
concat returns [Function value]
: QUOTE_DOUBLE t1=term t2=term QUOTE_DOUBLE{
        $value = dfac.getFunctionConcat($t1.value, $t2.value);
}
;
*/
literal returns [Term value]
  : stringLiteral (AT language)? {
       Term lang = $language.value;
       if (($stringLiteral.value) instanceof Function){
          Function f = (Function)$stringLiteral.value;
          if (lang != null){
             value = dfac.getTypedTerm(f,lang);
          }else{
             value = dfac.getTypedTerm(f, COL_TYPE.LITERAL);
          }       
       }else{
          ValueConstant constant = (ValueConstant)$stringLiteral.value;
          if (lang != null) {
	     value = dfac.getTypedTerm(constant, lang);
          } else {
      	     value = dfac.getTypedTerm(constant, COL_TYPE.LITERAL);
          }
       }
    }
  | dataTypeString { $value = $dataTypeString.value; }
  | numericLiteral { $value = $numericLiteral.value; }
  | booleanLiteral { $value = $booleanLiteral.value; }
  ;

stringLiteral returns [Term value]
  : STRING_WITH_QUOTE_DOUBLE {
      String str = $STRING_WITH_QUOTE_DOUBLE.text;
      if (str.contains("{")){
      	$value = getNestedConcat(str);
      }else{
      	$value = dfac.getConstantLiteral(str.substring(1, str.length()-1), COL_TYPE.LITERAL); // without the double quotes
      }
    }
  ;

dataTypeString returns [Term value]
  :  stringLiteral REFERENCE resource {
      if (($stringLiteral.value) instanceof Function){
          Function f = (Function)$stringLiteral.value;
          value = dfac.getTypedTerm(f, COL_TYPE.LITERAL);
      }else{
          ValueConstant constant = (ValueConstant)$stringLiteral.value;
          String functionName = $resource.value.toString();
          Predicate functionSymbol = null;
          if ($resource.value instanceof Function){
	    functionName = ( (ValueConstant) ((Function)$resource.value).getTerm(0) ).getValue();
          }
          Predicate.COL_TYPE type = dtfac.getDatatype(functionName);
          if (type == null) {
            throw new RuntimeException("Unsupported datatype: " + functionName);
          }
          $value = dfac.getTypedTerm(constant, type);
      }
  };

numericLiteral returns [ValueConstant value]
  : numericUnsigned { $value = $numericUnsigned.value; }
  | numericPositive { $value = $numericPositive.value; }
  | numericNegative { $value = $numericNegative.value; }
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

booleanLiteral returns [ValueConstant value]
  : TRUE  { $value = dfac.getConstantLiteral($TRUE.text, COL_TYPE.BOOLEAN); }
  | FALSE { $value = dfac.getConstantLiteral($FALSE.text, COL_TYPE.BOOLEAN); }
  ;

numericUnsigned returns [ValueConstant value]
  : INTEGER { $value = dfac.getConstantLiteral($INTEGER.text, COL_TYPE.INTEGER); }
  | DOUBLE  { $value = dfac.getConstantLiteral($DOUBLE.text, COL_TYPE.DOUBLE); }
  | DECIMAL { $value = dfac.getConstantLiteral($DECIMAL.text, COL_TYPE.DECIMAL); }
  ;

numericPositive returns [ValueConstant value]
  : INTEGER_POSITIVE { $value = dfac.getConstantLiteral($INTEGER_POSITIVE.text, COL_TYPE.INTEGER); }
  | DOUBLE_POSITIVE  { $value = dfac.getConstantLiteral($DOUBLE_POSITIVE.text, COL_TYPE.DOUBLE); }
  | DECIMAL_POSITIVE { $value = dfac.getConstantLiteral($DECIMAL_POSITIVE.text, COL_TYPE.DECIMAL); }
  ;

numericNegative returns [ValueConstant value]
  : INTEGER_NEGATIVE { $value = dfac.getConstantLiteral($INTEGER_NEGATIVE.text, COL_TYPE.INTEGER); }
  | DOUBLE_NEGATIVE  { $value = dfac.getConstantLiteral($DOUBLE_NEGATIVE.text, COL_TYPE.DOUBLE); }
  | DECIMAL_NEGATIVE { $value = dfac.getConstantLiteral($DECIMAL_NEGATIVE.text, COL_TYPE.DECIMAL); }
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
  : DIGIT+ PERIOD DIGIT* ('e'|'E') ('-'|'+')?
  | PERIOD DIGIT+ ('e'|'E') ('-'|'+')?
  | DIGIT+ ('e'|'E') ('-'|'+')?
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
  : '\'' ( options {greedy=false  ;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
  ;
/*
STRING_WITH_QUOTE_DOUBLE_CURLY_BRACKET
  : '"' (STRING_CONSTANT_LITERAL)*(STRING_WITH_CURLY_BRACKET)+((STRING_CONSTANT_LITERAL)*|(STRING_WITH_CURLY_BRACKET)*) '"'
  ;

STRING_CONSTANT_LITERAL
: ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D' | LCR_BRACKET) | ECHAR)*
;
*/
STRING_WITH_QUOTE_DOUBLE
  : '"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
  ;

STRING_WITH_BRACKET
  : '<' ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '>'
  ;

STRING_WITH_CURLY_BRACKET
  : '{' ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '}'
  ;

STRING_URI
  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
  ;
  
WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
  