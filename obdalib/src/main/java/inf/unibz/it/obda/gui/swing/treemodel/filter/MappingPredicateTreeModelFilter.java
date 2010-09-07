package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;

/**
 * @author This filter receives a string like parameter in the constructor and returns true if any mapping contains an atom in the head whose
 *         predicate matches predicate
 * 
 */
public class MappingPredicateTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtPredicateFilter;

	/**
	 * @param srtPredicateFilter
	 *            Constructor of the filter
	 */
	public MappingPredicateTreeModelFilter(String srtPredicateFilter) {
		this.srtPredicateFilter = srtPredicateFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter#match(java
	 * .lang.Object)
	 */
	@Override
	public boolean match(OBDAMappingAxiom object) {
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		ArrayList<QueryAtom> atoms = headquery.getAtoms();
		int atomscount = atoms.size();
		for (int i = 0; i < atomscount; i++) {
			QueryAtom atom = atoms.get(i);

			if (atom.getName().indexOf(srtPredicateFilter) != -1)
				filterValue = true;

		}
		return filterValue;
	}

}
