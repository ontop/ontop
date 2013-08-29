/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.panels;

public interface SavedQueriesPanelListener {
	/**
	 * The parameters new_group and new_id were added to initialize the
	 * SaveQueryPanel with this values.
	 */
	public void selectedQuerychanged(String new_group, String new_query, String new_id);
}
