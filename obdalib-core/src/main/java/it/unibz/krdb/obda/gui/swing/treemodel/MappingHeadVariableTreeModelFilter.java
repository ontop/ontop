package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;

/**
 * This Filter receives a string and returns true if any mapping contains the
 * string given in any of its head atoms.
 */
public class MappingHeadVariableTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

	public MappingHeadVariableTreeModelFilter() {
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
		// no match found!
		return (bNegation ? !isMatch : isMatch);
	}

	/** A helper method to check a match */
	public static boolean match(String keyword, Function predicate) {
		if (predicate.getFunctionSymbol().toString().indexOf(keyword) != -1) { // match found!
			return true;
		}

		// If the predicate name is mismatch, perhaps the terms.
		final List<NewLiteral> queryTerms = predicate.getTerms();
		for (int j = 0; j < queryTerms.size(); j++) {
			NewLiteral term = queryTerms.get(j);
			if (term.toString().indexOf(keyword) != -1) { // match found!
				return true;
			}
		}
		return false;
	}
}
