package inf.unibz.it.obda.gui.swing.treemodel.filter;

import inf.unibz.it.obda.model.OBDAMappingAxiom;
import inf.unibz.it.obda.model.impl.RDBMSSQLQuery;

/*
 * @author This filter receives a string in the constructor and returns true if accepts any query that contains the given text and
 *         implements the interface TreeModelFilter
 */

public class QueryStringTreeModelFilter implements
		TreeModelFilter<OBDAMappingAxiom> {

	private final String srtQueryTreeFilter;

	/**
	 * @param srtQueryTreeFilter
	 *            Constructor of the Filter
	 */
	public QueryStringTreeModelFilter(String srtQueryTreeFilter) {
		this.srtQueryTreeFilter = srtQueryTreeFilter;
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
		try {
			OBDAMappingAxiom mapping = object;
			RDBMSSQLQuery q = new RDBMSSQLQuery();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filterValue;
	}
}
