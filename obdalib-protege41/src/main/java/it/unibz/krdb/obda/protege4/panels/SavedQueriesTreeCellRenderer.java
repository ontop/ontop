package it.unibz.krdb.obda.protege4.panels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
