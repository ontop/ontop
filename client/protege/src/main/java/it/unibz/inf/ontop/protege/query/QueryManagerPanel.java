package it.unibz.inf.ontop.protege.query;

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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

public class QueryManagerPanel extends JPanel {

	private static final long serialVersionUID = 6920100822784727963L;

    static private final String QUERY_ICON_PATH = "images/query_icon.png";
    static private final String GROUP_ICON_PATH = "images/group_icon.png";

    private final List<QueryManagerPanelSelectionListener> listeners = new ArrayList<>();

    private final JTree queryManagerTree;
    private final QueryManagerTreeModel model;

	public QueryManagerPanel(OWLEditorKit editorKit) {
        OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        deleteAction.setEnabled(false);
        renameAction.setEnabled(false);

        setLayout(new BorderLayout());

        model = new QueryManagerTreeModel(obdaModelManager);
        queryManagerTree = new JTree(model);
        queryManagerTree.setCellRenderer(new DefaultTreeCellRenderer() {
            private final Icon queryIcon = DialogUtils.getImageIcon(QUERY_ICON_PATH);
            private final Icon groupIcon = DialogUtils.getImageIcon(GROUP_ICON_PATH);

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                QueryManager.Item item = (QueryManager.Item)value;
                setText(item.getID());
                setIcon(item.isQuery() ? queryIcon : groupIcon);
                return this;
            }
        });
        queryManagerTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        queryManagerTree.getSelectionModel().addTreeSelectionListener(evt -> {
            QueryManager.Item entity = (QueryManager.Item) evt.getPath().getLastPathComponent();
            listeners.forEach(l -> l.selectionChanged(entity.isQuery() ? entity : null));
        });

        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) { /* NO-OP */ }
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
                queryManagerTree.setSelectionPath(treePath);
                queryManagerTree.scrollPathToVisible(treePath);
            }
        });
        add(new JScrollPane(queryManagerTree), BorderLayout.CENTER);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(getMenuItem("Add Query...", addQueryAction));
        popupMenu.add(getMenuItem("Add Group...", addGroupAction));
        popupMenu.add(getMenuItem("Rename Query/Group", renameAction));
        popupMenu.add(getMenuItem("Delete Query/Group", deleteAction));
        setUpPopUpMenu(queryManagerTree, popupMenu);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
        controlPanel.add(getButton(addQueryAction));
        controlPanel.add(getButton(addGroupAction));
        controlPanel.add(getButton(deleteAction));
        add(controlPanel, BorderLayout.NORTH);

        queryManagerTree.getSelectionModel().addTreeSelectionListener(evt -> {
            boolean nonEmptySelection = queryManagerTree.getSelectionPaths() != null;
            renameAction.setEnabled(nonEmptySelection);
            deleteAction.setEnabled(nonEmptySelection);
        });
	}

	OBDAModelManagerListener getOBDAModelManagerListener() { return model; }

    QueryManagerListener getQueryManagerListener() { return model; }

    public void addQueryManagerSelectionListener(QueryManagerPanelSelectionListener listener) {
		if (listener != null && !listeners.contains(listener))
		    listeners.add(listener);
	}

	public void removeQueryManagerSelectionListener(QueryManagerPanelSelectionListener listener) {
		if (listener != null)
		    listeners.remove(listener);
	}

    private final OntopAbstractAction addQueryAction = new OntopAbstractAction(
            "Query",
            "plus.png",
            "Create a new query",
            getKeyStrokeWithCtrlMask(VK_E)) {
        @Override
        public void actionPerformed(ActionEvent evt) {
            QueryManager.Item group = getTargetForInsertion();
            showNewItemDialog("New query ID:", "Create New Query", getUsedIDs(group))
                    .ifPresent(id -> group.addQueryChild(id, ""));
        }
    };

    private final OntopAbstractAction addGroupAction = new OntopAbstractAction(
            "Group",
            "plus.png",
            "Create a new group",
            getKeyStrokeWithCtrlMask(VK_G)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            QueryManager.Item group = getTargetForInsertion();
            showNewItemDialog("New group ID:", "Create New Group", getUsedIDs(group))
                    .ifPresent(group::addGroupChild);
        }
    };

    private final OntopAbstractAction renameAction = new OntopAbstractAction(
            "Rename",
            null,
            null,
            getKeyStrokeWithCtrlMask(VK_U)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath path = queryManagerTree.getSelectionPath();
            if (path == null)
                return;

            QueryManager.Item item = (QueryManager.Item)path.getLastPathComponent();
            if (item.getParent() == null)
                return;

            showNewItemDialog("<html><b>New</b> ID for " + (item.isQuery() ? "query" : "group") +
                            " \"" + htmlEscape(item.getID()) + "\":</html>",
                    "Rename " + (item.isQuery() ? "Query" : "Group") + " \"" + item.getID() + "\"",
                    getUsedIDs(item.getParent()))
                    .ifPresent(item::setID);
        }
    };

    private final OntopAbstractAction deleteAction = new OntopAbstractAction(
            "Delete",
            "minus.png",
            "Delete selected group or query",
            getKeyStrokeWithCtrlMask(VK_BACK_SPACE)) {
        @Override
        public void actionPerformed(ActionEvent evt) {
            TreePath path = queryManagerTree.getSelectionPath();
            if (path == null)
                return;

            QueryManager.Item item = (QueryManager.Item) path.getLastPathComponent();
            if (item.getParent() == null) // root cannot be removed
                return;

            if (!confirmDelete(item))
                return;

            item.getParent().removeChild(item);
        }
    };


    private QueryManager.Item getTargetForInsertion() {
        TreePath path = queryManagerTree.getSelectionPath();
        if (path == null)
            return (QueryManager.Item)queryManagerTree.getModel().getRoot();

        QueryManager.Item item = (QueryManager.Item) path.getLastPathComponent();
        return item.isQuery() ? item.getParent() : item;
    }

    private ImmutableSet<String> getUsedIDs(QueryManager.Item item) {
	    return item.getChildren().stream()
                .map(QueryManager.Item::getID)
                .collect(ImmutableCollectors.toSet());
    }

    private Optional<String> showNewItemDialog(String labelString, String title, ImmutableSet<String> usedIDs) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(new JLabel(labelString));

        panel.add(Box.createVerticalStrut(10));

        JTextField idField = new JTextField("");
        idField.setColumns(40);
        Border normalBorder = idField.getBorder();
        Border errorBorder = BorderFactory.createLineBorder(Color.RED, 1);
        panel.add(idField);

        panel.add(Box.createVerticalStrut(10));

        JLabel errorLabel = new JLabel("<html>&nbsp;</html>");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(errorLabel.getFont().deriveFont(AffineTransform.getScaleInstance(0.9, 0.9)));
        panel.add(errorLabel);

        panel.add(Box.createVerticalStrut(20));

        // manual construction of the buttons is required to control their enabled status
        JButton okButton = createStandardButton(OK_BUTTON_TEXT, false);
        JButton cancelButton = createStandardButton(CANCEL_BUTTON_TEXT, true);

        idField.getDocument().addDocumentListener((SimpleDocumentListener) evt -> {
            String id = idField.getText().trim();
            if (id.isEmpty() || usedIDs.contains(id)) {
                idField.setBorder(errorBorder);
                errorLabel.setText(id.isEmpty()
                        ? "ID cannot be empty."
                        : "A query or a group with this ID already exists.");
                okButton.setEnabled(false);
            }
            else {
                idField.setBorder(normalBorder);
                errorLabel.setText("<html>&nbsp;</html>");
                okButton.setEnabled(true);
            }
        });

        if (JOptionPane.showOptionDialog(null,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                DialogUtils.getOntopIcon(),
                new Object[] { okButton, cancelButton },
                okButton) != JOptionPane.OK_OPTION)
            return Optional.empty();

        return Optional.of(idField.getText().trim());
    }

	private boolean confirmDelete(QueryManager.Item item) {
        return DialogUtils.confirmation(null,
                "<html>This will delete " + (item.isQuery() ?  "query" : "group") +
                        " \""  + htmlEscape(item.getID()) + "\"" +
                        (item.getChildCount() == 0 ? "" : " along with all its queries and groups") + ".<br><br>" +
                        "Do you wish to <b>continue</b>?<br></html>",
                "Delete confirmation");
    }
}
