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
package inf.unibz.it.obda.gui.swing.queryhistory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class QueryhistoryTreeModel extends DefaultTreeModel implements QueryhistoryControllerListener{

	private static final long serialVersionUID = -5181390800513619513L;

	public QueryhistoryTreeModel() {
		super(new DefaultMutableTreeNode(""));
	}
	
	public void queryAdded(DefaultMutableTreeNode node) {
		
			insertNodeInto(node, (DefaultMutableTreeNode) root, root.getChildCount());

	}

	public void queryRemoved(DefaultMutableTreeNode node) {
		
		super.removeNodeFromParent(node);
	}

}
