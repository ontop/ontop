/*
 * Turtle.g
 * Copyright (C) 2010 Obdalib Team
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For information on how to redistribute this software under
 * the terms of a license other than GNU General Public License
 * contact TMate Software at support@sqljet.com
 *
 * @author Josef Hardi (josef.hardi@unibz.it)
 */
grammar Turtle;

@header {
package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.Atom;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;
}

@lexer::header {
package it.unibz.krdb.obda.parser;

import java.util.List;
import java.util.Vector;
}

@lexer::members {
private List<String> errors = new Vector<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    String hdr = getErrorHeader(e);
    String msg = getErrorMessage(e, tokenNames);
    errors.add(hdr + " " + msg);
}

public List<String> getErrors() {
    return errors;
}   
}

@members {
/** Constants */
private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
private static final URI RDF_TYPE_URI = URI.create(RDF_TYPE);

/** Map of directives */
private HashMap<String, String> directives = new HashMap<String, String>();

/** The current subject term */
private Term subject;

private Set<Term> variableSet = new HashSet<Term>();

/** A factory to construct the predicates and terms */
private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
parse returns [CQIE value]
@init {
  List<Atom> body = new LinkedList<Atom>();
}
  : statement* EOF {
      int arity = variableSet.size();
      List<Term> distinguishVariables = new ArrayList<Term>(variableSet);
      Atom head = dfac.getAtom(dfac.getPredicate(OBDALibConstants.QUERY_HEAD_URI, arity, null), distinguishVariables);
      if ($statement.value != null) {
        body.addAll($statement.value); 
      }
      $value = dfac.getCQIE(head, body); 
    }
  ;
  
statement returns [List<Atom> value]
  : directive PERIOD
  | triples PERIOD { $value = $triples.value; }
  ;

directive
  : base
  | prefixID
  ;

base
  : AT BASE uriref {
      String uriref = $uriref.value;
      directives.put("", uriref);
    }
  ;

prefixID
  : AT PREFIX prefix uriref {
      String prefix = $prefix.text;
      String uriref = $uriref.value;
      directives.put(prefix.substring(0, prefix.length()-1), uriref); // remove the end colon
    }
  ;
  
triples returns [List<Atom> value]
  : subject { subject = $subject.value; } predicateObjectList {
      $value = $predicateObjectList.value;
    }
  ;
    
predicateObjectList returns [List<Atom> value]
@init {
   $value = new LinkedList<Atom>();
}
  : v1=verb l1=objectList {
      for (Term object : $l1.value) {
        Atom atom = null;
        if ($v1.value.equals(RDF_TYPE_URI)) {
          URIConstant c = (URIConstant) object;  // it has to be a URI constant
          Predicate predicate = dfac.getClassPredicate(c.getURI());
          atom = dfac.getAtom(predicate, subject);
        } else {
          Predicate predicate = dfac.getPredicate($v1.value, 2, null); // the data type cannot be determined here!
          atom = dfac.getAtom(predicate, subject, object);
        }        
        $value.add(atom);
      }
    } 
    (SEMI v2=verb l2=objectList {
      for (Term object : $l2.value) {
        Atom atom = null;
        if ($v2.value.equals(RDF_TYPE_URI)) {
          URIConstant c = (URIConstant) object;  // it has to be a URI constant
          Predicate predicate = dfac.getClassPredicate(c.getURI());
          atom = dfac.getAtom(predicate, subject);
        } else {
          Predicate predicate = dfac.getPredicate($v2.value, 2, null); // the data type cannot be determined here!
          atom = dfac.getAtom(predicate, subject, object);
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
//  : resource
//  | blank
  : variable { $value = $variable.value; }
  | function { $value = $function.value; }
  ;
  
predicate returns [URI value]
  : resource { $value = $resource.value; }
  ;
  
object returns [Term value]
  : resource { $value = dfac.getURIConstant($resource.value); } // if the object is a resource then it has to be an rdf:type object!
  | function { $value = $function.value; }
  | literal  { $value = $literal.value; }
  | variable { $value = $variable.value; }
//  | blank
  ;
  
resource returns [URI value]
  : uriref { $value = URI.create($uriref.value); }
  | qname { $value = URI.create($qname.value); }
  ;
  
uriref returns [String value]
  : LESS relativeURI GREATER { $value = $relativeURI.text; } 
  ;
  
qname returns [String value]
  : (prefix|COLON) name? {
      String prefix = "";
      if ($prefix.text != null) {
        prefix = $prefix.text.substring(0, $prefix.text.length()-1); // remove the colon!
      }
      String uri = directives.get(prefix);
      $value = uri + $name.text; 
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
      Predicate functionSymbol = null;  	
      if (functionName.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
    	functionSymbol = dfac.getDataTypePredicateLiteral();
      } else if (functionName.equals(OBDAVocabulary.XSD_STRING_URI)) {
    	functionSymbol = dfac.getDataTypePredicateString();
      } else if (functionName.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
     	functionSymbol = dfac.getDataTypePredicateInteger();
      } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDecimal();
      } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDouble();
      } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDateTime();
      } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
    	functionSymbol = dfac.getDataTypePredicateBoolean();
      } else {
        functionSymbol = dfac.getPredicate(URI.create(functionName), arity);
      }
      $value = dfac.getFunctionalTerm(functionSymbol, $terms.value);
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

literal returns [ValueConstant value]
//  : STRING_WITH_QUOTE_DOUBLE (AT language)?
//  | dataTypeString { $value = $numericPositive.value; }
  : stringLiteral  { $value = $stringLiteral.value; }
  | numericLiteral { $value = $numericLiteral.value; }
  | booleanLiteral { $value = $booleanLiteral.value; }
  ;

stringLiteral returns [ValueConstant value]
  : STRING_WITH_QUOTE_DOUBLE { 
      String str = $STRING_WITH_QUOTE_DOUBLE.text;
      $value = dfac.getValueConstant(str.substring(1, str.length()-1), COL_TYPE.LITERAL); // without the double quotes
    }
  ;
   
dataTypeString
  : STRING_WITH_QUOTE_DOUBLE REFERENCE resource
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

prefix
  : STRING_PREFIX
  ;

name
  : VARNAME
  ;
 
language
  : CHAR
  ;

booleanLiteral returns [ValueConstant value]
  : TRUE  { $value = dfac.getValueConstant($TRUE.text, COL_TYPE.BOOLEAN); }
  | FALSE { $value = dfac.getValueConstant($FALSE.text, COL_TYPE.BOOLEAN); }
  ;
  
numericUnsigned returns [ValueConstant value]
  : INTEGER { $value = dfac.getValueConstant($INTEGER.text, COL_TYPE.INTEGER); }
  | DOUBLE  { $value = dfac.getValueConstant($DOUBLE.text, COL_TYPE.DOUBLE); }
  | DECIMAL { $value = dfac.getValueConstant($DECIMAL.text, COL_TYPE.DECIMAL); }
  ;
  
numericPositive returns [ValueConstant value]
  : INTEGER_POSITIVE { $value = dfac.getValueConstant($INTEGER_POSITIVE.text, COL_TYPE.INTEGER); }
  | DOUBLE_POSITIVE  { $value = dfac.getValueConstant($DOUBLE_POSITIVE.text, COL_TYPE.DOUBLE); }
  | DECIMAL_POSITIVE { $value = dfac.getValueConstant($DECIMAL_POSITIVE.text, COL_TYPE.DECIMAL); }
  ;
  
numericNegative returns [ValueConstant value]
  : INTEGER_NEGATIVE { $value = dfac.getValueConstant($INTEGER_NEGATIVE.text, COL_TYPE.INTEGER); }
  | DOUBLE_NEGATIVE  { $value = dfac.getValueConstant($DOUBLE_NEGATIVE.text, COL_TYPE.DOUBLE); }
  | DECIMAL_NEGATIVE { $value = dfac.getValueConstant($DECIMAL_NEGATIVE.text, COL_TYPE.DECIMAL); }
  ;
    
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

BASE: ('B'|'b')('A'|'a')('S'|'s')('E'|'e');

PREFIX: ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

REFERENCE:     '^^';
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

ID_PLAIN: ID_START (ID_CORE)*;

STRING_WITH_QUOTE
  : '\'' ( options {greedy=false  ;} : ~('\u0027' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '\''
  ;

STRING_WITH_QUOTE_DOUBLE
  : '"'  ( options {greedy=false  ;} : ~('\u0022' | '\u005C' | '\u000A' | '\u000D') | ECHAR )* '"'
  ;

STRING_URI
  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
  ;

STRING_PREFIX
  : ID COLON
  ; 
  
WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
  