package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;
import java.util.List;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

/**
 * @author This Filter receives a string and returns true if any mapping
 *         contains the string given in any of its head atoms
 */
public class MappingHeadVariableTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtHeadVariableFilter;

	/**
	 * @param srtHeadVariableFilter
	 *            Constructor of the MappingHeadVariableTreeModelFilter
	 */
	public MappingHeadVariableTreeModelFilter(String srtHeadVariableFilter) {
		this.srtHeadVariableFilter = srtHeadVariableFilter;
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
		// TODO Auto-generated method stub
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();

		ArrayList<QueryAtom> headAtom = headquery.getAtoms(); // atoms

		for (int i = 0; i < headAtom.size(); i++) {
			QueryAtom atom = headAtom.get(i);
			if (atom.getNamedPredicate().getUri().toString().indexOf(srtHeadVariableFilter) != -1) {
				filterValue = true;
			}
			ArrayList<QueryTerm> terms = atom.getTerms();
			for (int j = 0; j < terms.size(); j++) {
				if ((terms.get(j).toString()).indexOf(srtHeadVariableFilter) != -1) {
					filterValue = true;
				}
			}

		}
		return filterValue;
	}

}
