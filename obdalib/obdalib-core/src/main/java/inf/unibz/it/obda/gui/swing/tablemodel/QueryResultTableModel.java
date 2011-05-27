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
package inf.unibz.it.obda.gui.swing.tablemodel;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public class QueryResultTableModel extends DefaultTableModel {
	//public ArrayList<ArrayList<String>> results = null;
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1299206623412108413L;

	public QueryResultTableModel(Vector data, Vector columnnames) {
		super(data, columnnames);
		//this.results = results;
	}


}
