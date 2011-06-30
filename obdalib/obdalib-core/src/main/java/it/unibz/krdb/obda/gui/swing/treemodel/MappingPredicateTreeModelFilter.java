package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;



/**
 * This filter receives a string like parameter in the constructor and returns
 * true if any mapping contains an atom in the head whose predicate matches
 * predicate.
 */
public class MappingPredicateTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

  public MappingPredicateTreeModelFilter() {
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
  			PredicateAtom predicate = (PredicateAtom) atoms.get(i);
  			isMatch = isMatch || match(keyword.trim(), predicate);
  		}
  		if (isMatch) {
  		  break;  // end loop if a match is found!
  		}
    }
		return (bNegation ? !isMatch : isMatch);
	}

  /** A helper method to check a match */
  private boolean match(String keyword, PredicateAtom predicate) {

    if (predicate.getPredicate().getName().toString().indexOf(keyword) != -1) {
      return true;
    }
    return false;
  }
}
