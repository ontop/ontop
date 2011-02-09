grammar Constraints;

@header {
package inf.unibz.it.obda.constraints.parser;

import java.net.URI;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSCheckConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import inf.unibz.it.ucq.typing.CheckOperationTerm;
import inf.unibz.it.ucq.typing.UnknownXSDTypeException;
import inf.unibz.it.ucq.typing.XSDTypingController;

import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.Variable;
import org.obda.query.domain.Term;
import org.obda.query.domain.ValueConstant;

import com.sun.msv.datatype.xsd.XSDatatype;
}

@lexer::header {
package inf.unibz.it.obda.constraints.parser;

import java.util.Vector;
}

@lexer::members {
private Vector<String> errors = new Vector<String>();

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
/** The API controller */
private APIController apic = null;

/** A factory to construct the subject and object terms */
private TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

public void setController(APIController apic) {
  this.apic = apic;
}
}


/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
parse returns [AbstractConstraintAssertion constraint] 
  : prog EOF {
      $constraint = $prog.value;
    }
  ;
  
prog returns [AbstractConstraintAssertion value]
  : expression {
      $value = $expression.value;
    }
  ;
  
expression returns [AbstractConstraintAssertion value]
  : check_constraint        { $value = $check_constraint.value; }
  | unique_constraint       { $value = $unique_constraint.value; }
  | foreign_key_constraint  { $value = $foreign_key_constraint.value; }
  | primary_key_constraint  { $value = $primary_key_constraint.value; }
  ;
  
check_constraint returns [RDBMSCheckConstraint value]
@init {
  Vector<CheckOperationTerm> conditions = new Vector<CheckOperationTerm>();
  MappingController mc = apic.getMappingController();
  DatasourcesController dc = apic.getDatasourcesController();
  URI sourceUri = dc.getCurrentDataSource().getSourceID();
  OBDAMappingAxiom axiom = null;
}
  : table_name CHECK LPAREN c1=condition { conditions.add($c1.value); } RPAREN 
        (COMMA LPAREN c2=condition { conditions.add($c2.value); } RPAREN)* {

      axiom = mc.getMapping(sourceUri, $table_name.text);
      $value = new RDBMSCheckConstraint(
          $table_name.text, (RDBMSSQLQuery)axiom.getSourceQuery(), conditions);
    }
  ;
  
unique_constraint returns [RDBMSUniquenessConstraint value]
@init {
  MappingController mc = apic.getMappingController();
  DatasourcesController dc = apic.getDatasourcesController();
  URI sourceUri = dc.getCurrentDataSource().getSourceID();
  OBDAMappingAxiom axiom = null;
}
  : table_name UNIQUE LPAREN parameter RPAREN {
      
      axiom = mc.getMapping(sourceUri, $table_name.text);
      $value = new RDBMSUniquenessConstraint(
          $table_name.text, (RDBMSSQLQuery)axiom.getSourceQuery(), 
          $parameter.values);
    }
  ;
  
foreign_key_constraint returns [RDBMSForeignKeyConstraint value]
@init {
  MappingController mc = apic.getMappingController();
  DatasourcesController dc = apic.getDatasourcesController();
  URI sourceUri = dc.getCurrentDataSource().getSourceID();
  OBDAMappingAxiom axiom1 = null;
  OBDAMappingAxiom axiom2 = null;
}
  : t1=table_name LPAREN p1=parameter RPAREN REFERENCES t2=table_name (LPAREN p2=parameter RPAREN)? {
  
      axiom1 = mc.getMapping(sourceUri, $t1.text);
      axiom2 = mc.getMapping(sourceUri, $t2.text);
      $value = new RDBMSForeignKeyConstraint(
        $t1.text, $t2.text, 
        (RDBMSSQLQuery)axiom1.getSourceQuery(), 
        (RDBMSSQLQuery)axiom2.getSourceQuery(), 
        $p1.values, $p2.values);
    }
  ;
  
primary_key_constraint returns [RDBMSPrimaryKeyConstraint value]
@init {
  MappingController mc = apic.getMappingController();
  DatasourcesController dc = apic.getDatasourcesController();
  URI sourceUri = dc.getCurrentDataSource().getSourceID();
  OBDAMappingAxiom axiom = null;
}
  : table_name PRIMARY_KEY LPAREN parameter RPAREN {
  
      axiom = mc.getMapping(sourceUri, $table_name.text);
      $value = new RDBMSPrimaryKeyConstraint(
          $table_name.text, (RDBMSSQLQuery)axiom.getSourceQuery(), 
          $parameter.values);
    }
  ;
  
condition returns [CheckOperationTerm value]
  : variable op range {
      $value = new CheckOperationTerm($variable.value, $op.text, $range.value);
    }
  ;
  
parameter returns [Vector<Variable> values]
@init {
  $values = new Vector<Variable>();
}
  : v1=variable { $values.add($v1.value); } (COMMA v2=variable { $values.add($v2.value); })*
  ;
  
range returns [Term value]
  : variable         { $value = $variable.value; }
  | string_constant  { $value = $string_constant.value; }
  | numeric_constant { $value = $numeric_constant.value; }
  | boolean_constant { $value = $boolean_constant.value; }
  ;
  
string_constant returns [ValueConstant value]
@init {
  XSDatatype type = null;
  try {
    type = XSDTypingController.getInstance().getType("xsd:string");
  }
  catch (UnknownXSDTypeException e) {
    // Do nothing.
  }  
}
  : STRING_LITERAL {
      $value = termFactory.createValueConstant($STRING_LITERAL.text, type);
    }
  | STRING_LITERAL2 {
      $value = termFactory.createValueConstant($STRING_LITERAL2.text, type);
    }
  ;
  
numeric_constant returns [ValueConstant value]
@init {
  String constant = "";
  XSDatatype type = null;
  try {
	  type = (constant.contains(".")) ? 
	     XSDTypingController.getInstance().getType("xsd:double") :
		   XSDTypingController.getInstance().getType("xsd:int");
  }
  catch (UnknownXSDTypeException e) {
    // Do nothing.
  }  
}
  : NUMBER {
      constant = $NUMBER.text;
      $value = termFactory.createValueConstant(constant, type);
    }
  ;
  
boolean_constant returns [ValueConstant value]
@init {
  XSDatatype type = null;
  try {
    type = XSDTypingController.getInstance().getType("xsd:boolean");
  }
  catch (UnknownXSDTypeException e) {
    // Do nothing.
  }  
}
  : TRUE {
      $value = termFactory.createValueConstant("true", type);
    }
  | FALSE {
      $value = termFactory.createValueConstant("false", type);
    }
  ;
  
variable returns [Variable value] 
  : DOLLAR var_name {
      $value = termFactory.createVariable($var_name.text);
    }
  ;

var_name
  : STRING
  ;

table_name
  : STRING 
  ;

op 
  : LESS
  | GREATER
  | EQUALS
  | LESS_OR_EQUAL
  | GREATER_OR_EQUAL
  ;
     
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

CHECK: ('C'|'c')('H'|'h')('E'|'e')('C'|'c')('K'|'k');

UNIQUE: ('U'|'u')('N'|'n')('I'|'i')('Q'|'q')('U'|'u')('E'|'e');

REFERENCES: ('R'|'r')('E'|'e')('F'|'f')('E'|'e')('R'|'r')('E'|'e')('N'|'n')('C'|'c')('E'|'e')('S'|'s');

PRIMARY_KEY: ('P'|'p')('R'|'r')('I'|'i')('M'|'m')('A'|'a')('R'|'r')('Y'|'y')(' ')('K'|'k')('E'|'e')('Y'|'y');

TRUE: ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

FALSE: ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

DOT:             '.';
COMMA:           ',';
LPAREN:          '(';
RPAREN:          ')';
DOLLAR:          '$';
QUOTE_DOUBLE:    '"';
QUOTE_SINGLE:    '\'';
UNDERSCORE:      '_';
DASH:            '-';
LESS:            '<';
GREATER:         '>';
EQUALS:          '=';
LESS_OR_EQUAL:   '<=';
GREATER_OR_EQUAL:'>=';

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

NUMBER: DIGIT+ (DOT DIGIT+)?;

fragment ALPHANUM: (ALPHA|DIGIT);

STRING: (ALPHANUM|UNDERSCORE|DASH)+;

STRING_LITERAL
  : QUOTE_DOUBLE (options {greedy=false;} : .)* QUOTE_DOUBLE
  ;

STRING_LITERAL2
  : QUOTE_SINGLE (options {greedy=false;} : .)* QUOTE_SINGLE
  ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};