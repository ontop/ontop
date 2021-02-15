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
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.IconLoader;
import it.unibz.inf.ontop.protege.gui.models.QueryControllerTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the display of stored queries using a tree structure.
 */
public class SavedQueriesPanel extends JPanel implements QueryManager.EventListener {

	private static final long serialVersionUID = 6920100822784727963L;

    static private final String PATH_SAVEDQUERY_ICON = "images/query_icon.png";
    static private final String PATH_QUERYGROUP_ICON = "images/group_icon.png";

    private final Icon saved_query_icon;
    private final Icon query_group_icon;

    private final List<SavedQueriesPanelListener> listeners = new ArrayList<>();
	
	private final QueryControllerTreeModel queryControllerModel = new QueryControllerTreeModel();

	private final QueryManager queryManager;
		
	private QueryControllerTreeModel.QueryNode currentId;
	private QueryControllerTreeModel.QueryNode previousId;

    private final JTree treSavedQuery;

	public SavedQueriesPanel(QueryManager queryManager) {
        this.queryManager = queryManager;

        saved_query_icon = IconLoader.getImageIcon(PATH_SAVEDQUERY_ICON);
        query_group_icon = IconLoader.getImageIcon(PATH_QUERYGROUP_ICON);

        setLayout(new BorderLayout());

        treSavedQuery = new JTree(queryControllerModel);
        treSavedQuery.setRootVisible(false);
        treSavedQuery.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                if (value instanceof QueryControllerTreeModel.QueryNode)
                    setIcon(saved_query_icon);
                else if (value instanceof QueryControllerTreeModel.GroupNode)
                    setIcon(query_group_icon);

                return this;
            }
        });
        treSavedQuery.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                reselectQueryNode(evt);
            }
        });
        treSavedQuery.addTreeSelectionListener(this::selectQueryNode);
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

		queryManager.addListener(queryControllerModel);
		queryManager.addListener(this);
		
		// Fill the tree model with existing elements from the controller
		queryControllerModel.synchronize(queryManager);
		queryControllerModel.reload();
	}

	public void addQueryManagerListener(SavedQueriesPanelListener listener) {
		if (listener != null && !listeners.contains(listener))
		    listeners.add(listener);
	}

	public void removeQueryManagerListener(SavedQueriesPanelListener listener) {
		if (listener != null)
		    listeners.remove(listener);
	}


    private void selectQueryNode(TreeSelectionEvent evt) {

    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        if (node instanceof QueryControllerTreeModel.QueryNode) {
            currentId = (QueryControllerTreeModel.QueryNode)node;
            listeners.forEach(l -> l.selectedQueryChanged(currentId.getGroupID(), currentId.getQueryID(), currentId.getQuery()));
        }
        else if (node instanceof QueryControllerTreeModel.GroupNode) {
            QueryControllerTreeModel.GroupNode groupElement = (QueryControllerTreeModel.GroupNode)node;
            currentId = null;
            listeners.forEach(l -> l.selectedQueryChanged(groupElement.getGroupID(), "", ""));
        }
        else if (node == null) {
            currentId = null;
        }
    }

	private void reselectQueryNode(MouseEvent evt) {
		if (currentId == null) {
			return;
		}
		if (previousId == currentId) {
            listeners.forEach(l -> l.selectedQueryChanged(currentId.getGroupID(), currentId.getQueryID(), currentId.getQuery()));
        }
		else { // register the selected node
			previousId = currentId;
		}
	}

    private void cmdAddActionPerformed(ActionEvent evt) {
		NewQueryDialog dialog = new NewQueryDialog(this, queryManager);
		dialog.setVisible(true);
    }

	private void cmdRemoveActionPerformed(ActionEvent evt) {
		TreePath selected_path = treSavedQuery.getSelectionPath();
		if (selected_path == null)
			return;

		if (JOptionPane.showConfirmDialog(this,
                "This will delete the selected query. \n Continue? ",
                "Delete confirmation",
				JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selected_path.getLastPathComponent();
		if (node instanceof QueryControllerTreeModel.QueryNode) {
            QueryControllerTreeModel.QueryNode queryTreeElement = (QueryControllerTreeModel.QueryNode)node;
			queryManager.removeQuery(queryTreeElement.getGroupID(), queryTreeElement.getQueryID());
		}
		else if (node instanceof QueryControllerTreeModel.GroupNode) {
            QueryControllerTreeModel.GroupNode groupTreeElement = (QueryControllerTreeModel.GroupNode)node;
			queryManager.removeGroup(groupTreeElement.getGroupID());
		}
	}


    @Override
	public void added(QueryManager.Group group) {
		DefaultMutableTreeNode node = queryControllerModel.getGroupNode(group);
		// Select the new node in the JTree
		treSavedQuery.setSelectionPath(new TreePath(node.getPath()));
		treSavedQuery.scrollPathToVisible(new TreePath(node.getPath()));
	}

	@Override
	public void added(QueryManager.Query query) {
		DefaultMutableTreeNode node = queryControllerModel.getQueryNode(query);
		// Select the new node in the JTree
		treSavedQuery.setSelectionPath(new TreePath(node.getPath()));
		treSavedQuery.scrollPathToVisible(new TreePath(node.getPath()));
	}

    @Override
    public void changed(QueryManager.Query query) {
        DefaultMutableTreeNode node = queryControllerModel.getQueryNode(query);
        // Select the modified node in the JTree
        treSavedQuery.setSelectionPath(new TreePath(node.getPath()));
        treSavedQuery.scrollPathToVisible(new TreePath(node.getPath()));
    }

    @Override
	public void removed(QueryManager.Query query) {
        listeners.forEach(l -> l.selectedQueryChanged("", "", ""));
    }

	@Override
	public void removed(QueryManager.Group group) {
        listeners.forEach(l -> l.selectedQueryChanged("", "", ""));
    }
}
