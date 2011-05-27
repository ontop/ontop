package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;
import inf.unibz.it.obda.model.OBDAMappingAxiom;
import inf.unibz.it.obda.model.SQLQuery;
import inf.unibz.it.obda.model.Term;
import inf.unibz.it.obda.model.impl.CQIEImpl;

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
