package inf.unibz.it.obda.gui.swing.treemodel;

import inf.unibz.it.obda.model.OBDAMappingAxiom;

import java.util.List;

/**
 * Interface that implements a set of functions to add and remove filters of the TreeModel
 *
 */
public interface FilteredTreeModel {

	/**
	 * @param T
	 *            Adds a new filter
	 */
	public void addFilter(TreeModelFilter<OBDAMappingAxiom> filter);

	/**
	 * @param T
	 *            Adds a list of filters
	 */
	public void addFilters(List<TreeModelFilter<OBDAMappingAxiom>> filters);

	/**
	 * @param T
	 *            Remove a filter of the list of filters
	 */
	public void removeFilter(TreeModelFilter<OBDAMappingAxiom> filter);

	/**
	 * @param T
	 *            Remove a list of filters
	 */
	public void removeFilter(List<TreeModelFilter<OBDAMappingAxiom>> filters);

	/**
	 * Remove all the current filters
	 */
	public void removeAllFilters();

}
