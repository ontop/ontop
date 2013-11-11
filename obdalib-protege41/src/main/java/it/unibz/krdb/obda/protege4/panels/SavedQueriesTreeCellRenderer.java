/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.panels;

import it.unibz.krdb.obda.protege4.gui.IconLoader;
import it.unibz.krdb.obda.protege4.gui.treemodels.QueryGroupTreeElement;
import it.unibz.krdb.obda.protege4.gui.treemodels.QueryTreeElement;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SavedQueriesTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1404770661541936062L;
	Icon saved_query_icon = null;
	Icon query_group_icon = null;
	Icon root_node_icon = null;
	final String PATH_SAVEDQUERY_ICON = "images/query_icon.png";
	final String PATH_QUERYGROUP_ICON = "images/group_icon.png";

	public SavedQueriesTreeCellRenderer() {
		saved_query_icon = IconLoader.getImageIcon(PATH_SAVEDQUERY_ICON);
		query_group_icon = IconLoader.getImageIcon(PATH_QUERYGROUP_ICON);
		root_node_icon = IconLoader.getImageIcon("images/metadata.gif");
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof QueryTreeElement) {
			setIcon(saved_query_icon);
		} else if (value instanceof QueryGroupTreeElement) {
			setIcon(query_group_icon);
		} else {
			setIcon(root_node_icon);
		}
		return this;
	}
}
