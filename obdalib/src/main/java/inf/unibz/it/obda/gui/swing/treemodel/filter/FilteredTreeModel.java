package inf.unibz.it.obda.gui.swing.treemodel.filter;

import java.util.*;

/**
 * Interface that implements a set of functions to add and remove filters of the TreeModel
 * 
 */
public interface FilteredTreeModel {

	/**
	 * @param T
	 *            Adds a new filter
	 */
	public void addFilter(TreeModelFilter T);

	/**
	 * @param T
	 *            Adds a list of filters
	 */
	public void addFilters(List<TreeModelFilter> T);

	/**
	 * @param T
	 *            Remove a filter of the list of filters
	 */
	public void removeFilter(TreeModelFilter T);

	/**
	 * @param T
	 *            Remove a list of filters
	 */
	public void removeFilter(List<TreeModelFilter> T);

	/**
	 * Remove all the current filters
	 */
	public void removeAllFilters();

}
