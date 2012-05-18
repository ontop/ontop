package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;

/**
 * This filter receives a string in the constructor and returns true if accepts
 * any mapping containing the string in the head or body
 */
public class MappingStringTreeModelFilter extends TreeModelFilter<OBDAMappingAxiom> {

	public MappingStringTreeModelFilter() {
		super.bNegation = false;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean isMatch = false;
		for (String keyword : vecKeyword) {
			// Check in the Mapping ID
			final String mappingId = object.getId();
			isMatch = MappingIDTreeModelFilter.match(keyword.trim(), mappingId);
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Target Query
			final CQIE headquery = (CQIEImpl) object.getTargetQuery();
			final List<Atom> atoms = headquery.getBody();
			for (int i = 0; i < atoms.size(); i++) {
				Atom predicate = (Atom) atoms.get(i);
				isMatch = isMatch || MappingHeadVariableTreeModelFilter.match(keyword.trim(), predicate);
			}
			if (isMatch) {
				break; // end loop if a match is found!
			}

			// Check in the Mapping Source Query
			final OBDASQLQuery query = (OBDASQLQuery) object.getSourceQuery();
			isMatch = MappingSQLStringTreeModelFilter.match(keyword.trim(), query.toString());
			if (isMatch) {
				break; // end loop if a match is found!
			}
		}
		// no match found!
		return (bNegation ? !isMatch : isMatch);
	}
}
