package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.ArrayList;
import java.util.List;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

public class MappingIDTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private String srtIDTreeModelFilter;

	public MappingIDTreeModelFilter(String srtIDTreeModelFilter) {
		this.srtIDTreeModelFilter = srtIDTreeModelFilter;
	}

	@Override
	public boolean match(OBDAMappingAxiom object) {
		// TODO Auto-generated method stub
		boolean filterValue = false;
		OBDAMappingAxiom mapping = (OBDAMappingAxiom) object;
		if (mapping.getId().indexOf(srtIDTreeModelFilter) != -1)
			filterValue = true;
		return filterValue;
	}

}
