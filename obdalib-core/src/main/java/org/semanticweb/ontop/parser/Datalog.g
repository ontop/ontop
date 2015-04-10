 /*
 * #%L
 * Datalog.g
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

grammar Datalog;

@header {
package org.semanticweb.ontop.parser;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;

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
}

@lexer::header {
package org.semanticweb.ontop.parser;

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
        Function head = rule.getHead();
        String name = head.getPredicate().getName();
        int size = variableList.size(); 
        
        // Get the predicate atom
        Predicate predicate = dfac.getPredicate(name, size);
        Function newhead = dfac.getFunction(predicate, variableList);
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
      $value = dfac.getCQIE($head.value, new LinkedList<Function>());
    }
  ;

swirl_syntax_rule returns [CQIE value]
  : IMPLIES head {
      $value = dfac.getCQIE($head.value, new LinkedList<Function>());
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

head returns [Function value]
@init {
  $value = null;
}
  : atom {
      $value = $atom.value;
    }
  ;

body returns [List<Function> value]
@init {
  $value = new LinkedList<Function>();
}
  : a1=atom { $value.add($a1.value); } ((COMMA|CARET) a2=atom { $value.add($a2.value); })*
  ;

atom returns [Function value]
  : predicate LPAREN terms? RPAREN  {
      String uri = $predicate.value;
      
      Vector<Term> elements = $terms.elements;
      if (elements == null)
        elements = new Vector<Term>();
      Predicate predicate = dfac.getPredicate(uri, elements.size());
      
      Vector<Term> terms = $terms.elements;
      if (terms == null)
        terms = new Vector<Term>();
        
      $value = dfac.getFunction(predicate, terms);
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
      
      $value = dfac.getConstantLiteral(literal);
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
      } else if (functionName.equals(OBDAVocabulary.XSD_LONG_URI)) {
     	functionSymbol = dfac.getDataTypePredicateLong();
      } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDecimal();
      } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDouble();
      } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
    	functionSymbol = dfac.getDataTypePredicateDateTime();
      } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
    	functionSymbol = dfac.getDataTypePredicateBoolean();
      } else if (functionName.equals(OBDAVocabulary.QUEST_URI)) {
        functionSymbol = dfac.getUriTemplatePredicate(arity);
      } else {
        functionSymbol = dfac.getPredicate(functionName, arity);
      }
      $value = dfac.getFunction(functionSymbol, $terms.elements);
    }
  ;
  
uri_term returns [URIConstant value]
@init {
  String uriText = "";
}
  : uri { 
      uriText = $uri.text;      
      $value = dfac.getConstantURI(uriText);
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
      	  //System.out.println("Unknown prefix");          
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
  