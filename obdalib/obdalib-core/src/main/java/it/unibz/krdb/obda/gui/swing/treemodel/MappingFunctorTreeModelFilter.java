package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.VariableImpl;

import java.util.List;


/**
 * This filter receives a string and returns true if any mapping contains
 * the functor in some of the atoms in the head
 */

public class MappingFunctorTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

  public MappingFunctorTreeModelFilter() {
    super.bNegation = false;
  }

	@Override
	public boolean match(OBDAMappingAxiom object) {

	  final CQIE headquery = (CQIEImpl) object.getTargetQuery();
    final List<Atom> atoms = headquery.getBody();

    boolean isMatch = false;

	  String[] vecKeyword = strFilter.split(KEYWORD_DELIM);
    for (String keyword : vecKeyword) {
  		for (int i = 0; i < atoms.size(); i++) {
  			Atom predicate = (Atom) atoms.get(i);
  			isMatch = isMatch || match(keyword.trim(), predicate);
  		}
  		if (isMatch) {
  		  break;  // end loop if a match is found!
  		}
    }
		return (bNegation ? !isMatch : isMatch);
	}

	/** A helper method to check a match */
	public static boolean match(String keyword, Atom predicate) {

	  List<Term> queryTerms = predicate.getTerms();
    for (int j = 0; j < queryTerms.size(); j++) {
      Term term = queryTerms.get(j);
      if (term instanceof FunctionalTermImpl) {
        FunctionalTermImpl functionTerm = (FunctionalTermImpl) term;
        if (functionTerm.getFunctionSymbol().toString().indexOf(keyword)!= -1) { // match found!
          return true;
        }
      }
      if (term instanceof VariableImpl) {
        VariableImpl variableTerm = (VariableImpl) term;
        if(variableTerm.getName().indexOf(keyword)!= -1) { // match found!
          return true;
        }
      }
    }
    return false;
	}
}
