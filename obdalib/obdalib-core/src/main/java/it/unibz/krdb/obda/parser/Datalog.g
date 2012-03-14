/*
 * Datalog.g
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
grammar Datalog;

@header {
package it.unibz.krdb.obda.parser;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
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
private static final String OBDA_DEFAULT_URI = "obda-default";
private static final String OBDA_BASE_URI = "obda-base";
private static final String OBDA_SELECT_ALL = "obda-select-all";

/** Map of directives */
private HashMap<String, String> directives = new HashMap<String, String>();

/** Set of variable terms */
private HashSet<Variable> variables = new HashSet<Variable>();

/** A factory to construct the predicates and terms */
private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

/** Select all flag */
private boolean isSelectAll = false;

public HashMap<String, String> getDirectives() {
    return directives;
}
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
parse returns [DatalogProgram value]
  : prog EOF {
      $value = $prog.value;
    }
  ; catch [RecognitionException e] { 
      throw e;
    }
  
prog returns [DatalogProgram value]
@init {
  $value = dfac.getDatalogProgram();
  CQIE rule = null;
}
  : base?
    directive* 
    (rule {
      rule = $rule.value;
      if (isSelectAll) {
        List<Term> variableList = new Vector<Term>();
        variableList.addAll(variables); // Import all the data from the Set to a Vector.
         
        // Get the head atom
        Atom head = rule.getHead();
        URI name = head.getPredicate().getName();
        int size = variableList.size(); 
        
        // Get the predicate atom
        Predicate predicate = dfac.getPredicate(name, size);
        Atom newhead = dfac.getAtom(predicate, variableList);
        rule.updateHead(newhead);
        
        isSelectAll = false;  
      }
        
      $value.appendRule(rule);
    })+
  ;
  
directive
@init {
  String prefix = "";
  String uriref = "";
}
  : PREFIX prefix LESS uriref GREATER {
      prefix = $prefix.text;
      prefix = prefix.substring(0, prefix.length()-1);  
      if (prefix == null || prefix == "") // if there is no prefix id put the default name.
        prefix = OBDA_DEFAULT_URI;

      uriref = $uriref.text;
      
      directives.put(prefix, uriref);
    }
  ;

base
@init {
  String prefix = OBDA_BASE_URI; // prefix name for the base
  String uriref = "";
}
  : BASE LESS uriref GREATER {
      uriref = $uriref.text;      
      directives.put(prefix, uriref);
    }
  ;
  
rule returns [CQIE value]
  : (head? INV_IMPLIES)=> datalog_syntax_rule { 
      $value = $datalog_syntax_rule.value;
    }
  | (body? IMPLIES)=> swirl_syntax_rule {
      $value = $swirl_syntax_rule.value;
    }
  ;

datalog_syntax_rule returns [CQIE value]
  : INV_IMPLIES body {
      $value = dfac.getCQIE(null, $body.value);
    }
  | datalog_syntax_alt {
      $value = $datalog_syntax_alt.value;
    }
  ;

datalog_syntax_alt returns [CQIE value]
  : (head INV_IMPLIES body)=> head INV_IMPLIES body {
      $value = dfac.getCQIE($head.value, $body.value);
    }
  | head INV_IMPLIES {
      $value = dfac.getCQIE($head.value, new LinkedList<Atom>());
    }
  ;

swirl_syntax_rule returns [CQIE value]
  : IMPLIES head {
      $value = dfac.getCQIE($head.value, new LinkedList<Atom>());
    }
  | swirl_syntax_alt {
      $value = $swirl_syntax_alt.value;
    }
  ;

swirl_syntax_alt returns [CQIE value]
  : (body IMPLIES head)=> body IMPLIES head {
      $value = dfac.getCQIE($head.value, $body.value);
    }
  | body IMPLIES {
      $value = dfac.getCQIE(null, $body.value);
    }
  ;

head returns [Atom value]
@init {
  $value = null;
}
  : atom {
      $value = $atom.value;
    }
  ;

body returns [List<Atom> value]
@init {
  $value = new LinkedList<Atom>();
}
  : a1=atom { $value.add($a1.value); } ((COMMA|CARET) a2=atom { $value.add($a2.value); })*
  ;

atom returns [Atom value]
  : predicate LPAREN terms? RPAREN  {
      URI uri = URI.create($predicate.value);
      
      Vector<Term> elements = $terms.elements;
      if (elements == null)
        elements = new Vector<Term>();
      Predicate predicate = dfac.getPredicate(uri, elements.size());
      
      Vector<Term> terms = $terms.elements;
      if (terms == null)
        terms = new Vector<Term>();
        
      $value = dfac.getAtom(predicate, terms);
    }
  ;

predicate returns [String value]
  : full_name      { $value = $full_name.value; }
  | plain_name     { $value = $plain_name.value; }
  | qualified_name { $value = $qualified_name.value; }
  ;

terms returns [Vector<Term> elements]
@init {
  $elements = new Vector<Term>();
}
  : t1=term { $elements.add($t1.value); } (COMMA t2=term { $elements.add($t2.value); })*
  ;
  
term returns [Term value]
  : variable_term { $value = $variable_term.value; }
  | literal_term  { $value = $literal_term.value; }
  | object_term   { $value = $object_term.value; }
  | uri_term      { $value = $uri_term.value; }
  ;

variable_term returns [Variable value] 
  : (DOLLAR|QUESTION) id { 
      $value = dfac.getVariable($id.text);
      variables.add($value); // collect the variable terms.
    }
  | ASTERISK {
      $value = dfac.getVariable(OBDA_SELECT_ALL);
      isSelectAll = true;
    }
  ;

literal_term returns [ValueConstant value]
@init {
  String literal = "";
}
  : string {
      literal = $string.text;
      literal = literal.substring(1, literal.length()-1); // removes the quote signs.
      
      if (literal.charAt(0) == '<' && literal.charAt(literal.length()-1) == '>') {
      	literal = directives.get("") + literal.substring(1,literal.length()-1);
      } else {
      for (String prefix: directives.keySet()) {
      	literal = literal.replaceAll("&" + prefix + ";", directives.get(prefix));
      	}
      }
      
      $value = dfac.getValueConstant(literal);
    }
  ; 
  
object_term returns [Function value]
  : function LPAREN terms RPAREN {
      String functionName = $function.value;
      int arity = $terms.elements.size();
    
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
      $value = dfac.getFunctionalTerm(functionSymbol, $terms.elements);
    }
  ;
  
uri_term returns [URIConstant value]
@init {
  String uriText = "";
}
  : uri { 
      uriText = $uri.text;      
      URI uri = URI.create(uriText);
      $value = dfac.getURIConstant(uri);
    }
  ;
  
function returns [String value]
  : full_name      { $value = $full_name.value; }
  | plain_name     { $value = $plain_name.value; }
  | qualified_name { $value = $qualified_name.value; }
  ;

qualified_name returns [String value]
@init {
  String prefix = "";
  String uriref = "";
}
  : prefix id {
      prefix = $prefix.text;
      prefix = prefix.substring(0, prefix.length()-1);
      if (prefix != null)
        uriref = directives.get(prefix);
      else
        uriref = directives.get(OBDA_DEFAULT_URI);
      if (uriref == null) {
      	  System.out.println("Unknown prefix");          
          throw new RecognitionException();
      }
      
      String uri = uriref + $id.text; // creates the complete Uri string
      $value = uri;
    }
  ;

plain_name returns [String value]
  : id {
      String uriref = directives.get(OBDA_BASE_URI);      
      String uri = uriref + $id.text; // creates the complete Uri string
      $value = uri;
    }
  ;

full_name returns [String value]
  : uri {
      $value = $uri.text;
    }
  ;

uri
  : STRING_URI
  ;

uriref
  : STRING_URI
  ;

id
  : ID_PLAIN
  ;

prefix
  : STRING_PREFIX
  | COLON
  ;
  
string
  : STRING_LITERAL
  | STRING_LITERAL2
  ;
  
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PREFIX: ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

BASE: ('B'|'b')('A'|'a')('S'|'s')('E'|'e');

IMPLIES:       '->';
INV_IMPLIES:   ':-';
REFERENCE:     '^^';
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

fragment ID_START: (ALPHA|UNDERSCORE);

fragment ID_CORE: (ID_START|DIGIT);

fragment SCHEMA: ALPHA (ALPHA|DIGIT|PLUS|DASH|DOT)*;

fragment URI_PATH: (ALPHANUM|UNDERSCORE|DASH|COLON|DOT|HASH|QUESTION|SLASH);

fragment ID: ID_START (ID_CORE)*;

ID_PLAIN: ID_START (ID_CORE)*;

STRING_LITERAL
  : QUOTE_DOUBLE (options {greedy=false;} : .)* QUOTE_DOUBLE
  ;

STRING_LITERAL2
  : QUOTE_SINGLE (options {greedy=false;} : .)* QUOTE_SINGLE
  ;

STRING_URI
  : SCHEMA COLON DOUBLE_SLASH (URI_PATH)*
  ;

STRING_PREFIX
  : ID COLON
  ; 
  
WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
  