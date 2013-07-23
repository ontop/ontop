package it.unibz.krdb.obda.gui.swing.panel;

public interface SavedQueriesPanelListener {
	/**
	 * The parameters new_group and new_id were added to initialize the
	 * SaveQueryPanel with this values.
	 */
	public void selectedQuerychanged(String new_group, String new_query, String new_id);
}
