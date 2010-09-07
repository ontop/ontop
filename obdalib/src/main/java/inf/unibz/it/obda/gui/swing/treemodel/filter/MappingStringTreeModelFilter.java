package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.net.URI;
import java.util.ArrayList;

import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.ConceptQueryAtom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

/**
 * @author This filter receives a string in the constructor and returns true if
 *         accepts any mapping containing the string in the head or body
 * 
 */
public class MappingStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {
	private String srtModelFilter;

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
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		if (mapping.getId().indexOf(srtModelFilter) != -1)
			filterValue = true;
		ConjunctiveQuery headquery = (ConjunctiveQuery) mapping
				.getTargetQuery();
		RDBMSSQLQuery bodyquery = (RDBMSSQLQuery) mapping.getSourceQuery();

		ArrayList<QueryAtom> atoms = headquery.getAtoms();

		for (int i = 0; i < atoms.size(); i++) {
			QueryAtom atom = atoms.get(i);
			if (atom.getName().indexOf(srtModelFilter) != -1) {
				filterValue = true;
			}
			ArrayList<QueryTerm> queryTerms = atom.getTerms();

			for (int j = 0; j < queryTerms.size(); j++) {
				QueryTerm term = queryTerms.get(j);
				if (term.toString().indexOf(srtModelFilter) != -1)
					filterValue = true;

			}
		}
		if (bodyquery.getInputQuString().indexOf(srtModelFilter) != -1)
			filterValue = true;

		return filterValue;
	}

}
