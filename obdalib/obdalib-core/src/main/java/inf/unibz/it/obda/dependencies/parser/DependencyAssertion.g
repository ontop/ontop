grammar DependencyAssertion;

@header {
package inf.unibz.it.obda.dependencies.parser;

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
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.Variable;
}

@lexer::header {
package inf.unibz.it.obda.dependencies.parser;

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
/** The API controller */
private APIController apic = null;

/** A factory to construct the subject and object terms */
private TermFactoryImpl termFactory = TermFactoryImpl.getInstance();

public void setController(APIController apic) {
  this.apic = apic;
}

public class MappingException extends RecognitionException {
  private String msg = "";
  
  public MappingException(String msg) {
    this.msg = msg;
  }
  
  public String getErrorMessage() {
    return msg;
  }
}
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
parse returns [Vector<AbstractDependencyAssertion> assertions]
  : prog EOF {
      $assertions = $prog.values;
    }
  ; 
  
prog returns [Vector<AbstractDependencyAssertion> values]
@init {
  values = new Vector<AbstractDependencyAssertion>();
}
  : e1=expression { $values.add($e1.value); } 
    (SEMI e2=expression { $values.add($e2.value); })* 
  ;

expression returns [AbstractDependencyAssertion value]
  : inclusion_dependency    { $value = $inclusion_dependency.value; }
  | disjointness_dependency { $value = $disjointness_dependency.value; }
  | functional_dependency   { $value = $functional_dependency.value; }
  ;
  
inclusion_dependency returns [RDBMSInclusionDependency value]
  : 'includedIn' LPAREN p1=parameter COMMA p2=parameter RPAREN {
      String mapId1 = (String) $p1.values.get(0);
      String mapId2 = (String) $p2.values.get(0);
      Vector<Variable> mapTerms1 = (Vector<Variable>) $p1.values.get(1);
      Vector<Variable> mapTerms2 = (Vector<Variable>) $p2.values.get(1);     
      
      if (mapTerms1.size() != mapTerms2.size()) {
        throw new MappingException("Both mappings in the assertion must " +
            "have the same number of terms!");
      }
      else {
        MappingController mc = apic.getMappingController();
        DatasourcesController dc = apic.getDatasourcesController();
        URI sourceUri = dc.getCurrentDataSource().getSourceID();
        OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
        OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
        
        if (mapAxiom1 != null && mapAxiom2 != null) {
          $value = new RDBMSInclusionDependency(sourceUri, mapId1, mapId2,
            (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
            (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
            mapTerms1, mapTerms2);
        }
        else {
          throw new MappingException("Invalid mapping id!");
        }
      }
    }
  ;
  
disjointness_dependency returns [RDBMSDisjointnessDependency value]
  : 'disjoint' LPAREN p1=parameter COMMA p2=parameter RPAREN {
      String mapId1 = (String) $p1.values.get(0);
      String mapId2 = (String) $p2.values.get(0);
      Vector<Variable> mapTerms1 = (Vector<Variable>) $p1.values.get(1);
      Vector<Variable> mapTerms2 = (Vector<Variable>) $p2.values.get(1);
      
      if (mapTerms1.size() != mapTerms2.size()) {
        throw new MappingException("Both mappings in the assertion must " +
            "have the same number of terms!");
      }
      else {
        MappingController mc = apic.getMappingController();
        DatasourcesController dc = apic.getDatasourcesController();
        URI sourceUri = dc.getCurrentDataSource().getSourceID();
        OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
        OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
        
        if (mapAxiom1 != null && mapAxiom2 != null) {
          $value = new RDBMSDisjointnessDependency(sourceUri, mapId1, mapId2,
            (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
            (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
            mapTerms1, mapTerms2);
        }
        else {
          throw new MappingException("Invalid mapping id!");
        }
      }
    }
  ;
  
functional_dependency returns [RDBMSFunctionalDependency value]
  : 'functionOf' LPAREN p1=parameter COMMA p2=parameter RPAREN {
      String mapId1 = (String) $p1.values.get(0);
      String mapId2 = (String) $p2.values.get(0);
      Vector<Variable> mapTerms1 = (Vector<Variable>) $p1.values.get(1);
      Vector<Variable> mapTerms2 = (Vector<Variable>) $p2.values.get(1);
      
      if (mapTerms1.size() != mapTerms2.size()) {
        throw new MappingException("Both mappings in the assertion must " +
            "have the same number of terms!");
      }
      else {
        MappingController mc = apic.getMappingController();
        DatasourcesController dc = apic.getDatasourcesController();
        URI sourceUri = dc.getCurrentDataSource().getSourceID();
        OBDAMappingAxiom mapAxiom1 = mc.getMapping(sourceUri, mapId1);
        OBDAMappingAxiom mapAxiom2 = mc.getMapping(sourceUri, mapId2);
        
        if (mapAxiom1 != null && mapAxiom2 != null) {
          $value = new RDBMSFunctionalDependency(sourceUri, mapId1, mapId2,
            (RDBMSSQLQuery) mapAxiom1.getSourceQuery(),
            (RDBMSSQLQuery) mapAxiom2.getSourceQuery(), 
            mapTerms1, mapTerms2);
        }
        else {
          throw new MappingException("Invalid mapping id!");
        }
      }
    }
  ;

parameter returns [ArrayList<Object> values]
@init {
  values = new ArrayList<Object>();
  Vector<Variable> terms = new Vector<Variable>();
}
  : rule { $values.add($rule.value); } LSQ_BRACKET 
      v1=variable { terms.add($v1.value); } 
      (COMMA v2=variable { terms.add($v2.value); })* RSQ_BRACKET {
      $values.add(terms);  
    }
  ;
  
rule returns [String value] 
  : (BODY|HEAD) DOT mapping_id {
      $value = $mapping_id.text;
    }
  ;

variable returns [Variable value] 
  : DOLLAR var_name {
      $value = termFactory.createVariable($var_name.text);
    }
  ;

mapping_id
  : STRING
  ; 

var_name
  : STRING
  ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

BODY: ('B'|'b')('O'|'o')('D'|'d')('Y'|'y');

HEAD: ('H'|'h')('E'|'e')('A'|'a')('D'|'d');

DOT:           '.';
SEMI:          ';';
COMMA:         ',';
LPAREN:        '(';
RPAREN:        ')';
LSQ_BRACKET:   '[';
RSQ_BRACKET:   ']';
DOLLAR:        '$';
UNDERSCORE:    '_';
DASH:          '-';

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

fragment ALPHANUM: (ALPHA|DIGIT);

STRING: (ALPHANUM|UNDERSCORE|DASH)+;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};