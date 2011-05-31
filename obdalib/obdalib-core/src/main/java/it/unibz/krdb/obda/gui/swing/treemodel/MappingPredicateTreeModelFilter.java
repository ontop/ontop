package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;



/**
 * @author This filter receives a string like parameter in the constructor and returns true if any mapping contains an atom in the head whose
 *         predicate matches predicate
 *
 */
public class MappingPredicateTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private final String srtPredicateFilter;

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
		OBDAMappingAxiom mapping = object;
		CQIE headquery = (CQIEImpl) mapping.getTargetQuery();
		List<Atom> atoms = headquery.getBody();
		int atomscount = atoms.size();
		for (int i = 0; i < atomscount; i++) {
			Atom atom = atoms.get(i);

			if (atom.getPredicate().getName().toString().indexOf(srtPredicateFilter) != -1)
				filterValue = true	;

		}
		return filterValue;
	}

}
