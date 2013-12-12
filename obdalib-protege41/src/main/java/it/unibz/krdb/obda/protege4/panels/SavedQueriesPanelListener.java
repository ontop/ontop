package it.unibz.krdb.obda.protege4.panels;

public interface SavedQueriesPanelListener {
	/**
	 * The parameters new_group and new_id were added to initialize the
	 * SaveQueryPanel with this values.
	 */
	public void selectedQuerychanged(String new_group, String new_query, String new_id);
}
