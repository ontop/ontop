package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;

/*
 * @author This filter receives a string in the constructor and returns true if accepts any query that contains the given text and
 *         implements the interface TreeModelFilter
 */

//TODO This filter is probably wrong, fix. Josef

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
			
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filterValue;
	}
}
