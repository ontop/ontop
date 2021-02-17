package it.unibz.inf.ontop.protege.panels;

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

import it.unibz.inf.ontop.protege.core.QueryManager;
import it.unibz.inf.ontop.protege.gui.dialogs.NewQueryDialog;
import it.unibz.inf.ontop.protege.gui.models.QueryManagerTreeModel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.IconLoader;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryManagerPanel extends JPanel {

	private static final long serialVersionUID = 6920100822784727963L;

    static private final String PATH_SAVEDQUERY_ICON = "images/query_icon.png";
    static private final String PATH_QUERYGROUP_ICON = "images/group_icon.png";

    private final Icon saved_query_icon;
    private final Icon query_group_icon;

    private final List<QueryManagerSelectionListener> listeners = new ArrayList<>();

    private final QueryManager queryManager;

    private final JTree treSavedQuery;

	public QueryManagerPanel(QueryManager queryManager) {
        this.queryManager = queryManager;

        saved_query_icon = IconLoader.getImageIcon(PATH_SAVEDQUERY_ICON);
        query_group_icon = IconLoader.getImageIcon(PATH_QUERYGROUP_ICON);

        setLayout(new BorderLayout());

        QueryManagerTreeModel model = new QueryManagerTreeModel(queryManager);
        treSavedQuery = new JTree(model);
        treSavedQuery.setRootVisible(false);
        treSavedQuery.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                if (value instanceof QueryManager.Query) {
                    setIcon(saved_query_icon);
                    setText(((QueryManager.Query) value).getID());
                }
                else if (value instanceof QueryManager.Group) {
                    setIcon(query_group_icon);
                    setText(((QueryManager.Group) value).getID());
                }

                return this;
            }
        });
        treSavedQuery.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treSavedQuery.getSelectionModel().addTreeSelectionListener(this::selectQueryNode);

        model.addTreeModelListener(new TreeModelListener() {
            @Override public void treeNodesChanged(TreeModelEvent e) { selectAndScrollTo(getExtendedTreePath(e)); }
            @Override public void treeNodesInserted(TreeModelEvent e) { selectAndScrollTo(getExtendedTreePath(e)); }
            @Override public void treeNodesRemoved(TreeModelEvent e) { selectAndScrollTo(e.getTreePath()); }
            @Override public void treeStructureChanged(TreeModelEvent e) { selectAndScrollTo(e.getTreePath()); }
        });
        add(new JScrollPane(treSavedQuery), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        JButton addButton = DialogUtils.getButton(
                "Add",
                "plus.png",
                "Add a new query",
                this::cmdAddActionPerformed);
        controlPanel.add(addButton);

        JButton removeButton = DialogUtils.getButton(
                "Remove",
                "minus.png",
                "Remove the selected query",
                this::cmdRemoveActionPerformed);
        controlPanel.add(removeButton);

        add(controlPanel, BorderLayout.NORTH);
	}

	public void addQueryManagerSelectionListener(QueryManagerSelectionListener listener) {
		if (listener != null && !listeners.contains(listener))
		    listeners.add(listener);
	}

	public void removeQueryManagerSelectionListener(QueryManagerSelectionListener listener) {
		if (listener != null)
		    listeners.remove(listener);
	}


    private void selectQueryNode(TreeSelectionEvent evt) {
    	Object node = evt.getPath().getLastPathComponent();

        if (node instanceof QueryManager.Query) {
            QueryManager.Query query =  (QueryManager.Query)node;
            listeners.forEach(l -> l.selectedQueryChanged(query.getGroup().getID(), query.getID(), query.getQuery()));
        }
        else if (node instanceof QueryManager.Group) {
            QueryManager.Group group = (QueryManager.Group)node;
            listeners.forEach(l -> l.selectedQueryChanged(group.getID(), "", ""));
        }
        else {
            listeners.forEach(l -> l.selectedQueryChanged("", "", ""));
        }
    }

    private void cmdAddActionPerformed(ActionEvent evt) {
		NewQueryDialog dialog = new NewQueryDialog(this, queryManager);
		dialog.setVisible(true);
    }

	private void cmdRemoveActionPerformed(ActionEvent evt) {
		TreePath path = treSavedQuery.getSelectionPath();
		if (path == null)
			return;

		Object node = path.getLastPathComponent();
		if (node instanceof QueryManager.Query) {
            QueryManager.Query query = (QueryManager.Query)node;
            if (JOptionPane.showConfirmDialog(this,
                    "This will delete query " + query.getID() +  ".\nContinue?",
                    "Delete confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                return;

            queryManager.removeQuery(query.getGroup().getID(), query.getID());
		}
		else if (node instanceof QueryManager.Group) {
            QueryManager.Group group = (QueryManager.Group)node;
            if (JOptionPane.showConfirmDialog(this,
                    "This will delete group " + group.getID() +  " (with all its queries).\nContinue?",
                    "Delete confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                return;

			queryManager.removeGroup(group.getID());
		}
	}

    private TreePath getExtendedTreePath(TreeModelEvent e) {
        Object[] path = e.getPath();
        Object[] extendedPath = Arrays.copyOf(path, path.length + 1);
        extendedPath[path.length] = e.getChildren()[0];
        return new TreePath(extendedPath);
    }

    private void selectAndScrollTo(TreePath treePath) {
        treSavedQuery.setSelectionPath(treePath);
        treSavedQuery.scrollPathToVisible(treePath);
    }
}
