grammar MappingFilter;

@header {
package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.treemodel.TreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingIDTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingStringTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingHeadVariableTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingSQLStringTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingFunctorTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingPredicateTreeModelFilter;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
}

@lexer::header {
package it.unibz.krdb.obda.gui.swing.utils;

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
private static String stripLeadingAndTrailingQuotes(String str)
{
  if (str.startsWith("\"")) {
    str = str.substring(1, str.length());
  }
  if (str.endsWith("\"")) {
    str = str.substring(0, str.length() - 1);
  }
  return str;
}
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
parse returns [ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList]
@init {
  $filterList = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();
}
  : f1=filter { $filterList.add($f1.value); } (COMMA f2=filter { $filterList.add($f2.value); })* EOF
  ; catch [RecognitionException e] { 
      throw e;
    }

filter returns [TreeModelFilter<OBDAMappingAxiom> value]
  : (not=NOT? type COLON keyword) {
      $value = $type.value;
      String keyword = stripLeadingAndTrailingQuotes($keyword.text);    
      if (not != null) {
        $value.putNegation();
      }
      $value.addStringFilter(keyword);
    }
  ;

type returns [TreeModelFilter<OBDAMappingAxiom> value]
  : ID      { $value = new MappingIDTreeModelFilter(); }
  | TEXT    { $value = new MappingStringTreeModelFilter(); }
  | TARGET  { $value = new MappingHeadVariableTreeModelFilter(); }
  | SOURCE  { $value = new MappingSQLStringTreeModelFilter(); }
  | FUNCT   { $value = new MappingFunctorTreeModelFilter(); }
  | PRED    { $value = new MappingPredicateTreeModelFilter(); }
  ;
  
keyword
  : STRING_LITERAL
  ; 
 
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

NOT: ('N'|'n')('O'|'o')('T'|'t');

ID: ('I'|'i')('D'|'d');

TEXT: ('T'|'t')('E'|'e')('X'|'x')('T'|'t');

TARGET: ('T'|'t')('A'|'a')('R'|'r')('G'|'g')('E'|'e')('T'|'t');

SOURCE: ('S'|'s')('O'|'o')('U'|'u')('R'|'r')('C'|'c')('E'|'e');

FUNCT: ('F'|'f')('U'|'u')('N'|'n')('C'|'c')('T'|'t');

PRED: ('P'|'p')('R'|'r')('E'|'e')('D'|'d');

COMMA:         ',';
COLON:         ':';
QUOTE_DOUBLE:  '"';

STRING_LITERAL
  : QUOTE_DOUBLE (options {greedy=false;} : .)* QUOTE_DOUBLE
  ;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
