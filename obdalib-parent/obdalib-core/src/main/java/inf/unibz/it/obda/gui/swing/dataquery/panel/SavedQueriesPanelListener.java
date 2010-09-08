/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.dataquery.panel;

public interface SavedQueriesPanelListener {
	/**
	 * The parameters new_group and new_id were added to initialize the SaveQueryPanel with this values
	 * @param new_group
	 * @param new_query
	 * @param new_id
	 */
	public void selectedQuerychanged(String new_group,String new_query,String new_id);
}
