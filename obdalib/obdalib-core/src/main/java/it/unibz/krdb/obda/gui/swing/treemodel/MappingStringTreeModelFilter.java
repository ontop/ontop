package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.SQLQuery;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;


/**
 * @author This filter receives a string in the constructor and returns true if
 *         accepts any mapping containing the string in the head or body
 *
 */
public class MappingStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {
	private final String srtModelFilter;

	/**
	 * @param strModeFilter
	 *            Constructor of the filter that receives a mapping
	 */
	public MappingStringTreeModelFilter(String strModeFilter) {
		this.srtModelFilter = strModeFilter;
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
		if (mapping.getId().indexOf(srtModelFilter) != -1)
			filterValue = true;
		CQIE headquery = (CQIEImpl) mapping.getTargetQuery();
		SQLQuery bodyquery = (SQLQuery) mapping.getSourceQuery();

		List<Atom> atoms = headquery.getBody();

		for (int i = 0; i < atoms.size(); i++) {
			Atom atom = atoms.get(i);
			if (atom.getPredicate().getName().toString().indexOf(srtModelFilter) != -1) {
				filterValue = true;
			}
			List<Term> queryTerms = atom.getTerms();

			for (int j = 0; j < queryTerms.size(); j++) {
				Term term = queryTerms.get(j);
				if (term.toString().indexOf(srtModelFilter) != -1)
					filterValue = true;

			}
		}
		if (bodyquery.toString().indexOf(srtModelFilter) != -1)
			filterValue = true;

		return filterValue;
	}

}
