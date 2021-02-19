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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
        treSavedQuery.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                QueryManager.Item item = (QueryManager.Item)value;
                setText(item.getID());
                setIcon(item.isQuery() ? saved_query_icon : query_group_icon);

                return this;
            }
        });
        treSavedQuery.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treSavedQuery.getSelectionModel().addTreeSelectionListener(evt -> {
            QueryManager.Item entity = (QueryManager.Item) evt.getPath().getLastPathComponent();
            listeners.forEach(l -> l.selectedQueryChanged(entity.isQuery() ? entity : null));
        });

        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }
            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                selectAndScrollTo(e.getTreePath().pathByAddingChild(e.getChildren()[0]));
            }
            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                selectAndScrollTo(e.getTreePath());
            }
            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                selectAndScrollTo(e.getTreePath());
            }
            private void selectAndScrollTo(TreePath treePath) {
                treSavedQuery.setSelectionPath(treePath);
                treSavedQuery.scrollPathToVisible(treePath);
            }
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
                "Remove the selected group or query",
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


    private void cmdAddActionPerformed(ActionEvent evt) {
		NewQueryDialog dialog = new NewQueryDialog(this, queryManager);
		dialog.setVisible(true);
    }

	private void cmdRemoveActionPerformed(ActionEvent evt) {
		TreePath path = treSavedQuery.getSelectionPath();
		if (path == null)
			return;

        QueryManager.Item item = (QueryManager.Item) path.getLastPathComponent();
        if (item.getParent() == null) // root cannot be removed
            return;

        if (JOptionPane.showConfirmDialog(this,
                item.isQuery()
                        ? "This will delete query " + item.getID() +  ".\nContinue? "
                        : "This will delete group " + item.getID() +  " (with all its queries).\nContinue?",
                "Delete confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;

        item.getParent().removeChild(item);
	}

}
