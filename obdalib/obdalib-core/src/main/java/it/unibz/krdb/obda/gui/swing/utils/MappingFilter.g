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

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
parse returns [ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList]
@init {
  $filterList = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();
}
  : f1=filter { $filterList.add($f1.value); } (SEMI f2=filter { $filterList.add($f2.value); })* EOF
  ; catch [RecognitionException e] { 
      throw e;
    }

filter returns [TreeModelFilter<OBDAMappingAxiom> value]
  : (not=NOT? (type COLON)? keyword) {
      $value = $type.value;
      if ($value == null) {
        $value = new MappingStringTreeModelFilter();
      }
      
      // Register the keyword.
      $value.addStringFilter($keyword.text);
      
      // Register the negation.
      if (not != null) {
        $value.putNegation();
      }
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
  : STRING (COMMA STRING)*
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
SEMI:          ';';
UNDERSCORE:    '_';
DASH:          '-';

fragment ALPHA: ('a'..'z'|'A'..'Z');

fragment DIGIT: '0'..'9'; 

fragment ALPHANUM: (ALPHA|DIGIT);

fragment CHAR: (ALPHANUM|UNDERSCORE|DASH);

STRING: CHAR*;

WS: (' '|'\t'|('\n'|'\r'('\n')))+ {$channel=HIDDEN;};
