package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;

import java.util.List;

/**
 * Interface that implements a set of functions to add and remove filters of the
 * TreeModel
 */
public interface FilteredModel {

	/**
	 * @param filter
	 *            Adds a new filter
	 */
	public void addFilter(TreeModelFilter<OBDAMappingAxiom> filter);

	/**
	 * @param filters
	 *            Adds a list of filters
	 */
	public void addFilters(List<TreeModelFilter<OBDAMappingAxiom>> filters);

	/**
	 * @param filter
	 *            Remove a filter of the list of filters
	 */
	public void removeFilter(TreeModelFilter<OBDAMappingAxiom> filter);

	/**
	 * @param filters
	 *            Remove a list of filters
	 */
	public void removeFilter(List<TreeModelFilter<OBDAMappingAxiom>> filters);

	/**
	 * Remove all the current filters
	 */
	public void removeAllFilters();
}
