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
package org.obda.query.tools.parser;

import java.net.URI;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.FunctionSymbol;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.FunctionSymbolImpl;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.query.domain.imp.VariableImpl;
import org.obda.reformulation.domain.Predicate;
import org.obda.reformulation.domain.PredicateFactory;
import org.obda.reformulation.domain.imp.BasicPredicateFactoryImpl;
}

@lexer::header {
package org.obda.query.tools.parser;

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

/** A factory to construct the subject and object terms */
private TermFactory termFactory = TermFactory.getInstance();

/** A factory to construct the predicates */
private PredicateFactory predicateFactory = BasicPredicateFactoryImpl.getInstance();

/** Select all flag */
private boolean isSelectAll = false;

public HashMap<String, String> getDirectives() {
    return directives;
}

private CQIE identifyAllVariables(CQIE rule) {
    List<Term> variableTerms = new Vector<Term>();
    List<Atom> body = rule.getBody();
    for (Atom atom : body) {
        List<Term> terms = atom.getTerms();
        for (Term term : terms) {
            if (term instanceof VariableImpl)
                variableTerms.add(term);
        }
    }
    
    Atom head = rule.getHead();
    head.updateTerms(variableTerms);
    rule.updateHead(head);
     
    return rule; 
}
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
parse returns [DatalogProgramImpl value]
  : prog EOF {
      $value = $prog.value;
    }
  ; catch [RecognitionException e] { 
      throw e;
    }
  
prog returns [DatalogProgramImpl value]
@init {
  $value = new DatalogProgramImpl();
  CQIE rule = null;
}
  : base?
    directive* 
    (rule {
      rule = $rule.value;
      if (isSelectAll) {
        rule = identifyAllVariables(rule);
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
      $value = new CQIEImpl(null, $body.value, true);
    }
  | datalog_syntax_alt {
      $value = $datalog_syntax_alt.value;
    }
  ;

datalog_syntax_alt returns [CQIE value]
  : (head INV_IMPLIES body)=> head INV_IMPLIES body {
      $value = new CQIEImpl($head.value, $body.value, true);
    }
  | head INV_IMPLIES {
      $value = new CQIEImpl($head.value, null, true);
    }
  ;

swirl_syntax_rule returns [CQIE value]
  : IMPLIES head {
      $value = new CQIEImpl($head.value, null, true);
    }
  | swirl_syntax_alt {
      $value = $swirl_syntax_alt.value;
    }
  ;

swirl_syntax_alt returns [CQIE value]
  : (body IMPLIES head)=> body IMPLIES head {
      $value = new CQIEImpl($head.value, $body.value, true);
    }
  | body IMPLIES {
      $value = new CQIEImpl(null, $body.value, true);
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

body returns [Vector<Atom> value]
@init {
  $value = new Vector<Atom>();
}
  : a1=atom { $value.add($a1.value); } ((COMMA|CARET) a2=atom { $value.add($a2.value); })*
  ;

atom returns [Atom value]
  : predicate LPAREN terms? RPAREN  {
      URI uri = URI.create($predicate.value);
      
      Vector<Term> elements = $terms.elements;
      if (elements == null)
        elements = new Vector<Term>();
      Predicate predicate = predicateFactory.getPredicate(uri, elements.size());
      
      Vector<Term> terms = $terms.elements;
      if (terms == null)
        terms = new Vector<Term>();
        
      $value = new AtomImpl(predicate, terms);
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

variable_term returns [Term value] 
  : (DOLLAR|QUESTION) id { 
      $value = termFactory.createVariable($id.text);
    }
  | ASTERISK {
      $value = termFactory.createVariable(OBDA_SELECT_ALL);
      isSelectAll = true;
    }
  ;

literal_term returns [Term value]
@init {
  String literal = "";
}
  : string {
      literal = $string.text;
      literal = literal.substring(1, literal.length()-1); // removes the quote signs.
      $value = termFactory.createValueConstant(literal);
    }
  ; 
  
object_term returns [Term value]
  : function LPAREN terms RPAREN {
      FunctionSymbol function = new FunctionSymbolImpl($function.value, $terms.elements.size());
      $value = termFactory.createObjectTerm(function, $terms.elements);
    }
  ;
  
uri_term returns [Term value]
@init {
  String uriText = "";
}
  : uri { 
      uriText = $uri.text;      
      URI uri = URI.create(uriText);
      $value = termFactory.createURIConstant(uri);
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
  