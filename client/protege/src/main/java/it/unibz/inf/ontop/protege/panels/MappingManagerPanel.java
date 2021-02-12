package it.unibz.inf.ontop.protege.panels;

/*
 * #%L
 * ontop-protege
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

import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.gui.dialogs.EditMappingDialog;
import it.unibz.inf.ontop.protege.gui.dialogs.SQLQueryDialog;
import it.unibz.inf.ontop.protege.gui.models.*;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.protege.workers.ValidationSwingWorker;
import it.unibz.inf.ontop.utils.IDGenerator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MappingManagerPanel extends JPanel {

	private static final long serialVersionUID = -486013653814714526L;

    private final MappingFilteredListModel model;
    private final JList<TriplesMap> mappingList;

    private final JCheckBox filterCheckbox;
    private final JTextField filterField;

    private static final int GAP = 2;

    /**
	 * Creates a new panel.
	 *
	 * @param obdaModelManager
	 */
	public MappingManagerPanel(OBDAModelManager obdaModelManager) {

        setLayout(new BorderLayout());

        mappingList = new JList<>();
        // Setting up the mappings tree
        mappingList.setCellRenderer(new MappingListRenderer(obdaModelManager));
        mappingList.setFixedCellWidth(-1);
        mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(mappingList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        Action newAction = new AbstractAction("New triples map...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditMappingDialog dialog = new EditMappingDialog(
                        obdaModelManager,
                        IDGenerator.getNextUniqueID("MAPID-"));
                dialog.setLocationRelativeTo(MappingManagerPanel.this);
                dialog.setVisible(true);
            }
        };

        Action removeAction = new AbstractAction("Remove triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!removeConfirm(mappingList.getSelectedValuesList()))
                    return;

                TriplesMapCollection triplesMapCollection = obdaModelManager.getTriplesMapCollection();
                for (TriplesMap triplesMap : mappingList.getSelectedValuesList())
                    triplesMapCollection.remove(triplesMap.getId());

                mappingList.clearSelection();
            }
        };
        removeAction.setEnabled(false);

        Action copyAction = new AbstractAction("Copy triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!copyConfirm(mappingList.getSelectedValuesList()))
                    return;

                TriplesMapCollection triplesMapCollection = obdaModelManager.getTriplesMapCollection();
                for (TriplesMap triplesMap : mappingList.getSelectedValuesList())
                    triplesMapCollection.duplicate(triplesMap.getId());
            }
        };
        copyAction.setEnabled(false);

        Action editAction = new AbstractAction("Edit triples map...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditMappingDialog dialog = new EditMappingDialog(
                        obdaModelManager,
                        mappingList.getSelectedValue());
                dialog.setLocationRelativeTo(MappingManagerPanel.this);
                dialog.setVisible(true);
            }
        };
        editAction.setEnabled(false);

        Action validateAction = new AbstractAction("Validate triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ValidationSwingWorker worker = new ValidationSwingWorker(
                        MappingManagerPanel.this,
                        mappingList.getSelectedValuesList(),
                        obdaModelManager);
                worker.execute();
            }
        };
        validateAction.setEnabled(false);

        Action executeSQLAction = new AbstractAction("Execute source SQL...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SQLQueryDialog dialog = new SQLQueryDialog(
                        obdaModelManager.getDatasource(),
                        mappingList.getSelectedValue().getSqlQuery());
                dialog.setLocationRelativeTo(MappingManagerPanel.this);
                dialog.setVisible(true);
            }
        };
        executeSQLAction.setEnabled(false);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        buttonsPanel.add(
                DialogUtils.getButton(
                        "New...",
                        "plus.png",
                        "Create a new triples map",
                        newAction),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        JButton removeButton = DialogUtils.getButton(
                "Remove",
                "minus.png",
                "Remove the selected triples maps",
                removeAction);
        removeButton.setEnabled(false);
        buttonsPanel.add(removeButton,
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        JButton copyButton = DialogUtils.getButton(
                "Copy",
                "copy.png",
                "Make a duplicate copy of the selected triples maps (with fresh IDs)",
                copyAction);
        copyButton.setEnabled(false);
        buttonsPanel.add(copyButton,
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(new JPanel(), // stretchable panel
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(
                DialogUtils.getButton(
                        "Select all",
                        "select-all.png",
                        null,
                        evt -> mappingList.setSelectionInterval(0, mappingList.getModel().getSize() - 1)),
                new GridBagConstraints(7, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(
                DialogUtils.getButton(
                        "Select none",
                        "select-none.png",
                        null,
                        evt -> mappingList.clearSelection()),
                new GridBagConstraints(8, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        add(buttonsPanel, BorderLayout.NORTH);

        JPanel extraButtonsPanel = new JPanel(new GridBagLayout());

        JLabel mappingStatusLabel = new JLabel();
        extraButtonsPanel.add(mappingStatusLabel,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        extraButtonsPanel.add(new JLabel("Search:"),
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, 30, GAP, GAP), 0, 0));

        filterField = new JTextField();
        extraButtonsPanel.add(filterField,
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));
        filterField.getDocument().addDocumentListener((SimpleDocumentListener)
                            e ->  processFilterAction());

        filterCheckbox = new JCheckBox("Enable filter");
        filterCheckbox.setEnabled(false);
        filterCheckbox.addItemListener(evt -> processFilterAction());
        extraButtonsPanel.add(filterCheckbox,
                new GridBagConstraints(4, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, 20), 0, 0));

        add(extraButtonsPanel, BorderLayout.SOUTH);

        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(newAction));
        menu.add(new JMenuItem(removeAction));
        menu.add(new JMenuItem(copyAction));
        menu.add(new JMenuItem(editAction));
        menu.addSeparator();
        menu.add(new JMenuItem(validateAction));
        menu.add(new JMenuItem(executeSQLAction));
        mappingList.setComponentPopupMenu(menu);

        mappingList.addListSelectionListener(evt -> {
            List<TriplesMap> selectionList = mappingList.getSelectedValuesList();
            removeAction.setEnabled(!selectionList.isEmpty());
            removeButton.setEnabled(!selectionList.isEmpty());
            copyAction.setEnabled(!selectionList.isEmpty());
            copyButton.setEnabled(!selectionList.isEmpty());
            validateAction.setEnabled(!selectionList.isEmpty());

            editAction.setEnabled(selectionList.size() == 1);
            executeSQLAction.setEnabled(selectionList.size() == 1);
        });

        InputMap inputMap = mappingList.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "add");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "edit");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");

        ActionMap actionMap = mappingList.getActionMap();
        actionMap.put("remove", removeAction);
        actionMap.put("add", newAction);
        actionMap.put("edit", editAction);

        mappingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    editAction.actionPerformed(
                            new ActionEvent(mappingList, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        model = new MappingFilteredListModel(obdaModelManager.getTriplesMapCollection());
        model.addListDataListener(new ListDataListener() {
            @Override public void intervalRemoved(ListDataEvent e) { updateMappingSize(); }
            @Override public void intervalAdded(ListDataEvent e) { updateMappingSize(); }
            @Override public void contentsChanged(ListDataEvent e) { updateMappingSize(); }
            private void updateMappingSize() {
                mappingStatusLabel.setText("<html>Mapping size: <b>" +
                        obdaModelManager.getTriplesMapCollection().size() + "</b></html>");
            }
        });
        mappingList.setModel(model);
    }

    /**
     * any change of the filter will trigger the model update via processFilterAction()
     * @param filter
     */
    public void setFilter(String filter) {
        filterField.setText(filter);
    }

    private void processFilterAction() {
        String filterText = filterField.getText().trim();
        filterCheckbox.setEnabled(!filterText.isEmpty());

        model.setFilter(filterCheckbox.isSelected() && filterCheckbox.isEnabled() ? filterText : null);
    }

    private boolean copyConfirm(List<TriplesMap> selection) {
        return DialogUtils.confirmation(this,
                "<html>This will create a <b>copy</b> of the selected <b>" + selection.size() +
                        " triples map" + (selection.size() == 1 ? "" : "s") + "</b>.<br><br>" +
                        "Do you wish to <b>continue</b>?<br></html>",
                "Copy triples maps confirmation");
    }

    private boolean removeConfirm(List<TriplesMap> selection) {
        return DialogUtils.confirmation(this,
                "<html>This will <b>remove</b> the selected <b>" + selection.size() +
                        " triples map" + (selection.size() == 1 ? "" : "s") + "</b>.<br><br>" +
                        "Do you wish to <b>continue</b>?.<br></html>",
                "Remove triples maps confirmation");
    }
}
