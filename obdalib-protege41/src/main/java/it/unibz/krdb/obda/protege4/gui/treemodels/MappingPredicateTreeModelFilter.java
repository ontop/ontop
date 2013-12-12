package it.unibz.krdb.obda.protege4.gui.treemodels;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.protege4.gui.treemodels.TreeModelFilter;

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
		final List<Function> atoms = headquery.getBody();

		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			for (int i = 0; i < atoms.size(); i++) {
				Function predicate = (Function) atoms.get(i);
				isMatch = isMatch || match(keyword.trim(), predicate);
			}
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		return (bNegation ? !isMatch : isMatch);
	}

	/** A helper method to check a match */
	private boolean match(String keyword, Function predicate) {
		if (predicate.getFunctionSymbol().toString().indexOf(keyword) != -1) {
			return true;
		}
		return false;
	}
}
