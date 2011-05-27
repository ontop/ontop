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
package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.treemodel.QueryGroupTreeElement;
import it.unibz.krdb.obda.gui.swing.treemodel.QueryTreeElement;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SavedQueriesTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1404770661541936062L;
	Icon			saved_query_icon		= null;
	Icon			query_group_icon		= null;
	Icon			root_node_icon			= null;
	final String	PATH_SAVEDQUERY_ICON	= "images/query_icon.png";
	final String	PATH_QUERYGROUP_ICON	= "images/group_icon.png";

	public SavedQueriesTreeCellRenderer() {
		saved_query_icon = IconLoader.getImageIcon(PATH_SAVEDQUERY_ICON);
		query_group_icon = IconLoader.getImageIcon(PATH_QUERYGROUP_ICON);
		root_node_icon = IconLoader.getImageIcon("images/metadata.gif");
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if (value instanceof QueryTreeElement) {
			setIcon(saved_query_icon);
			//setText(value.toString());
			//setFont(new Font("Helvetica", Font.PLAIN, 11));
		} else if (value instanceof QueryGroupTreeElement) {
			setIcon(query_group_icon);
			//setFont(new Font("Helvetica", Font.BOLD, 11));
			//setText(value.toString());
		} else {
			setIcon(root_node_icon);
			//setText(value.toString());
			///setFont(new Font("Helvetica", Font.PLAIN, 11));
		}
		return this;
	}

}
