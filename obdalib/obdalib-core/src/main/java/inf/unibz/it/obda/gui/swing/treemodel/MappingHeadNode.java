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
package inf.unibz.it.obda.gui.swing.treemodel;

import javax.swing.tree.DefaultMutableTreeNode;

public class MappingHeadNode extends DefaultMutableTreeNode {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -172088331390892183L;

	public MappingHeadNode(String name) {
		super(name);
	}
	
	public String getQuery() {
		return (String)getUserObject();
	}
	
	public void setQuery(String query) {
		setUserObject(query);
	}
}
