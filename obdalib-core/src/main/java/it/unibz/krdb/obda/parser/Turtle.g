/*
 * Turtle.g
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
grammar Turtle;

@header {
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

//import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

//import com.hp.hpl.jena.iri.URIImpl;
//import com.hp.hpl.jena.iri.URIImplFactory;

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
public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
    throws RecognitionException
{
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
public void emitErrorMessage(String msg) 	{
	error = msg;
	throw new RuntimeException(error);
}
    
@Override
public Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
    throws RecognitionException {
    throw new RecognitionException(input);
}
}

@members {
/** Constants */
private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
private static final URIImpl RDF_TYPE_URI = new URIImpl(RDF_TYPE);

/** Map of directives */
private HashMap<String, String> directives = new HashMap<String, String>();

/** The current subject term */
private Term subject;

private Set<Term> variableSet = new HashSet<Term>();

/** A factory to construct the predicates and terms */
private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

private String error = "";

public String getError() {
	return error;
}

protected void mismatch(IntStream input, int ttype, BitSet follow)
    throws RecognitionException
{
    throw new MismatchedTokenException(ttype, input);
}

public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
    throws RecognitionException
{
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
public void emitErrorMessage(	String 	msg	 ) 	{
	error = msg;
}
    
public Object recoverFromMismatchedTokenrecoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
	throws RecognitionException {
    throw new RecognitionException(input);
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
      Function head = dfac.getFunction(dfac.getPredicate(OBDALibConstants.QUERY_HEAD, arity, null), distinguishVariables);
      
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
  : subject { subject = $subject.value; } predicateObjectList {
      $value = $predicateObjectList.value;
    }
  ;

predicateObjectList returns [List<Function> value]
@init {
   $value = new LinkedList<Function>();
}
  : v1=verb l1=objectList {
      for (Term object : $l1.value) {
        Function atom = null;
        if ($v1.value.equals(RDF_TYPE_URI)) {
          URIConstant c = (URIConstant) object;  // it has to be a URI constant
          Predicate predicate = dfac.getClassPredicate(c.getURI());
          atom = dfac.getFunction(predicate, subject);
        } else {
          Predicate predicate = dfac.getPredicate($v1.value.toString(), 2, null); // the data type cannot be determined here!
          atom = dfac.getFunction(predicate, subject, object);
        }
        $value.add(atom);
      }
    } 
    (SEMI v2=verb l2=objectList {
      for (Term object : $l2.value) {
        Function atom = null;
        if ($v2.value.equals(RDF_TYPE_URI)) {
          URIConstant c = (URIConstant) object;  // it has to be a URI constant
          Predicate predicate = dfac.getClassPredicate(c.getURI());
          atom = dfac.getFunction(predicate, subject);
        } else {
          Predicate predicate = dfac.getPredicate($v2.value.toString(), 2, null); // the data type cannot be determined here!
          atom = dfac.getFunction(predicate, subject, object);
        }
        $value.add(atom);
      }
    })*
  ;
  
verb returns [URI value]
  : predicate { $value = $predicate.value; }
  | 'a' { $value = RDF_TYPE_URI; }
  ;

objectList returns [List<Term> value]
@init {
  $value = new ArrayList<Term>();
}
  : o1=object { $value.add($o1.value); } (COMMA o2=object { $value.add($o2.value); })* 
  ;

subject returns [Term value]
  : resource { $value = dfac.getConstantURI($resource.value.toString()); }
  | variable { $value = $variable.value; }
  | function { $value = $function.value; }
  | uriTemplateFunction { $value = $uriTemplateFunction.value; }
//  | blank
  ;

predicate returns [URI value]
  : resource { $value = $resource.value; }
  ;

object returns [Term value]
  : resource { $value = dfac.getConstantURI($resource.value.toString()); }
  | function { $value = $function.value; }
  | literal  { $value = $literal.value; }
  | variable { $value = $variable.value; }
  | dataTypeFunction { $value = $dataTypeFunction.value; }
  | uriTemplateFunction { $value = $uriTemplateFunction.value; }
//  | blank
  ;

resource returns [URI value]
  : uriref { $value = new URIImpl($uriref.value); }
  | qname { $value = new URIImpl($qname.value); }
  ;

uriref returns [String value]
  : LESS relativeURI GREATER { $value = $relativeURI.text; }
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
  : (QUESTION|DOLLAR) name {
       $value = dfac.getVariable($name.text);
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

dataTypeFunction returns [Function value]
  : variable AT language {
      Predicate functionSymbol = dfac.getDatatypePredicateLiteralLang();
      Variable var = $variable.value;
      Term lang = $language.value;   
      $value = dfac.getFunction(functionSymbol, var, lang);
    }
  | variable REFERENCE resource {
      Variable var = $variable.value;
      String functionName = $resource.value.toString();
      Predicate functionSymbol = null;
      if (functionName.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
    	functionSymbol = dfac.getDatatypePredicateLiteral();
      } else if (functionName.equals(OBDAVocabulary.XSD_STRING_URI)) {
    	functionSymbol = dfac.getDatatypePredicateString();
      } else if (functionName.equals(OBDAVocabulary.XSD_INTEGER_URI) || functionName.equals(OBDAVocabulary.XSD_INT_URI)) {
     	functionSymbol = dfac.getDatatypePredicateInteger();
      } else if (functionName.equals(OBDAVocabulary.XSD_LONG)) {
            	functionSymbol = dfac.getDatatypePredicateLong();
      } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDecimal();
      } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDouble();
      } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDateTime();
      } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
    	functionSymbol = dfac.getDatatypePredicateBoolean();
      } else {
        throw new RecognitionException();
      }
      $value = dfac.getFunction(functionSymbol, var);
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

uriTemplateFunction returns [Function value]
@init {
  List<Term> terms = new ArrayList<Term>();
}
  : STRING_WITH_TEMPLATE_SIGN {
      String template = $STRING_WITH_TEMPLATE_SIGN.text;
      
      // cleanup the template string, e.g., <"&ex;student-{pid}"> --> &ex;student-{pid}
      template = template.substring(2, template.length()-2);
      
      if (template.contains("&") && template.contains(";")) {
        // scan the input string if it contains "&...;"
        int start = template.indexOf("&");
        int end = template.indexOf(";");
        
        // extract the whole prefix placeholder, e.g., "&ex;"
        String prefixPlaceHolder = template.substring(start, end+1);
        
        // extract the prefix name, e.g., "&ex;" --> "ex"
        String prefix = prefixPlaceHolder.substring(1, prefixPlaceHolder.length()-1);
        
        // replace any colon sign
        prefix = prefix.replace(":", "");
        
        String uri = directives.get(prefix);
        if (uri == null) {
          throw new RuntimeException("The prefix name is unknown: " + prefix); // the prefix is unknown.
        }
        template = template.replaceFirst(prefixPlaceHolder, uri);
      }
      
      while (template.contains("{") && template.contains("}")) {
        // scan the input string if it contains "{" ... "}"
        int start = template.indexOf("{");
        int end = template.indexOf("}");
        
        // extract the whole placeholder, e.g., "{?var}"
        String placeHolder = Pattern.quote(template.substring(start, end+1));
        template = template.replaceFirst(placeHolder, "[]"); // change the placeholder string temporarly
        
        // extract the variable name only, e.g., "{?var}" --> "var"
        try {
       	  String variableName = placeHolder.substring(4, placeHolder.length()-3);
       	  if (variableName.equals("")) {
       	    throw new RuntimeException("Variable name must have at least 1 character");
       	  }
          terms.add(dfac.getVariable(variableName));
        } catch (IndexOutOfBoundsException e) {
       	  throw new RuntimeException("Variable name must have at least 1 character");
        }
      }
      // replace the placeholder string to the original. The current string becomes the template
      template = template.replace("[]", "{}");
      ValueConstant uriTemplate = dfac.getConstantLiteral(template);
      
      // the URI template is always on the first position in the term list
      terms.add(0, uriTemplate);
      $value = dfac.getFunction(dfac.getUriTemplatePredicate(terms.size()), terms);
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

literal returns [Term value]
  : stringLiteral (AT language)? {
       ValueConstant constant = $stringLiteral.value;
       Term lang = $language.value;
       if (lang != null) {
         $value = dfac.getFunction(dfac.getDatatypePredicateLiteralLang(), constant, lang);
       } else {
       	 $value = dfac.getFunction(dfac.getDatatypePredicateLiteral(), constant);
       }
    }
  | dataTypeString { $value = $dataTypeString.value; }
  | numericLiteral { $value = $numericLiteral.value; }
  | booleanLiteral { $value = $booleanLiteral.value; }
  ;

stringLiteral returns [ValueConstant value]
  : STRING_WITH_QUOTE_DOUBLE {
      String str = $STRING_WITH_QUOTE_DOUBLE.text;
      $value = dfac.getConstantLiteral(str.substring(1, str.length()-1), COL_TYPE.LITERAL); // without the double quotes
    }
  ;

dataTypeString returns [Term value]
  :  stringLiteral REFERENCE resource {
      ValueConstant constant = $stringLiteral.value;
      String functionName = $resource.value.toString();
      Predicate functionSymbol = null;
      if (functionName.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
    	functionSymbol = dfac.getDatatypePredicateLiteral();
      } else if (functionName.equals(OBDAVocabulary.XSD_STRING_URI)) {
    	functionSymbol = dfac.getDatatypePredicateString();
      } else if (functionName.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
     	functionSymbol = dfac.getDatatypePredicateInteger();
      } else if (functionName.equals(OBDAVocabulary.XSD_LONG_URI)) {
     	functionSymbol = dfac.getDatatypePredicateLong();
      } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDecimal();
      } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDouble();
      } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
    	functionSymbol = dfac.getDatatypePredicateDateTime();
      } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
    	functionSymbol = dfac.getDatatypePredicateBoolean();
      } else {
        throw new RuntimeException("Unknown datatype: " + functionName);
      }
      $value = dfac.getFunction(functionSymbol, constant);
    }
  ;

numericLiteral returns [ValueConstant value]
  : numericUnsigned { $value = $numericUnsigned.value; }
  | numericPositive { $value = $numericPositive.value; }
  | numericNegative { $value = $numericNegative.value; }
  ;

nodeID
  : BLANK_PREFIX name
  ;

relativeURI
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

fragment NAME_CHAR: (NAME_START_CHAR|DIGIT|UNDERSCORE|MINUS|PERIOD|HASH|QUESTION|SLASH);	 

NCNAME
  : NAME_START_CHAR (NAME_CHAR)*;

NAMESPACE
  : NAME_START_CHAR (NAME_CHAR)* COLON
  ;

PREFIXED_NAME
  : NCNAME? COLON NCNAME
  ;

STRING_WITH_QUOTE
  : '\'' ( options {greedy=false  ;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
  ;

STRING_WITH_QUOTE_DOUBLE
  : '"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
  ;

STRING_WITH_TEMPLATE_SIGN
  : '<"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '">'
  ;

STRING_URI
  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
  ;
  
WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
  