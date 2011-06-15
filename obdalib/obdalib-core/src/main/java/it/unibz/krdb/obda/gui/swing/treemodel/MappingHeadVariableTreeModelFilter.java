package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.CQIEImpl;

import java.util.List;



/**
 * @author This Filter receives a string and returns true if any mapping
 *         contains the string given in any of its head atoms
 */
public class MappingHeadVariableTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private final String srtHeadVariableFilter;

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
		OBDAMappingAxiom mapping = object;
		CQIE headquery = (CQIEImpl) mapping.getTargetQuery();

		List<Atom> headAtom = headquery.getBody(); // atoms

		for (int i = 0; i < headAtom.size(); i++) {
			PredicateAtom atom = (PredicateAtom) headAtom.get(i);
			if (atom.getPredicate().getName().toString().indexOf(srtHeadVariableFilter) != -1) {
				filterValue = true;
			}
			List<Term> terms = atom.getTerms();
			for (int j = 0; j < terms.size(); j++) {
				if ((terms.get(j).toString()).indexOf(srtHeadVariableFilter) != -1) {
					filterValue = true;
				}
			}

		}
		return filterValue;
	}

}
