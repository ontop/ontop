package it.unibz.inf.ontop.protege.mapping;

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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.mapping.worker.ValidationSwingWorker;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.utils.IDGenerator;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

public class MappingManagerPanel extends JPanel implements OBDAModelManagerListener {

	private static final long serialVersionUID = -486013653814714526L;

	private final OBDAModelManager obdaModelManager;
	private final ColorSettings colorSettings;
	private final OWLEditorKit editorKit;
    private final MappingFilteredListModel model;
    private final JList<TriplesMap> mappingList;

    private final JCheckBox filterCheckbox;
    private final JTextField filterField;

    private static final int GAP = 2;

	public MappingManagerPanel(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);
        this.colorSettings = OBDAEditorKitSynchronizerPlugin.getColorSettings(editorKit);

        setLayout(new BorderLayout());

        mappingList = new JList<>();
        mappingList.setCellRenderer(new MappingListRenderer(editorKit));
        mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(mappingList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        mappingList.addListSelectionListener(evt -> setActionEnabled());

        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        buttonsPanel.add(getButton(newAction),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(getButton(removeAction),
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(getButton(copyAction),
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(getButton(validateAction),
                new GridBagConstraints(3, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(new JPanel(), // stretchable panel
                new GridBagConstraints(4, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(getButton(selectAllAction),
                new GridBagConstraints(7, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        buttonsPanel.add(getButton(selectNoneAction),
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

        extraButtonsPanel.add(new JLabel("Search (any of):"),
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, 30, GAP, GAP), 0, 0));

        filterField = new JTextField();
        extraButtonsPanel.add(filterField,
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));
        filterField.getDocument().addDocumentListener((SimpleDocumentListener) e ->  applyFilter());

        filterCheckbox = new JCheckBox("Enable filter");
        filterCheckbox.setEnabled(false);
        filterCheckbox.addItemListener(evt -> applyFilter());
        extraButtonsPanel.add(filterCheckbox,
                new GridBagConstraints(4, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        filterField.addActionListener(evt -> filterCheckbox.setSelected(true));
        add(extraButtonsPanel, BorderLayout.SOUTH);

        JPopupMenu menu = new JPopupMenu();
        menu.add(getMenuItem("New triples map...", newAction));
        menu.add(getMenuItem("Remove triples maps", removeAction));
        menu.add(getMenuItem("Copy triples maps", copyAction));
        menu.add(getMenuItem(editAction));
        menu.addSeparator();
        menu.add(getMenuItem("Validate triples maps", validateAction));
        menu.add(getMenuItem(executeSQLAction));
        setUpPopUpMenu(mappingList, menu);
        // additional accelerators
        setUpAccelerator(mappingList, removeAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        setUpAccelerator(mappingList, editAction, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));

        setActionEnabled();

        mappingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    editAction.actionPerformed(
                            new ActionEvent(mappingList, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        model = new MappingFilteredListModel(obdaModelManager);
        model.addListDataListener(new ListDataListener() {
            @Override public void intervalRemoved(ListDataEvent e) { updateMappingSize(); }
            @Override public void intervalAdded(ListDataEvent e) { updateMappingSize(); }
            @Override public void contentsChanged(ListDataEvent e) { updateMappingSize(); }
            private void updateMappingSize() {
                mappingStatusLabel.setText("<html>Mapping size: <b>" +
                        obdaModelManager.getCurrentOBDAModel().getTriplesMapManager().size() + "</b></html>");
            }
        });
        mappingList.setModel(model);
        model.changed(obdaModelManager.getCurrentOBDAModel().getTriplesMapManager());
    }


    TriplesMapManagerListener getTriplesMapManagerListener() { return model; }


    private final OntopAbstractAction newAction = new OntopAbstractAction(
            "New...",
            "plus.png",
            "Create a new triples map",
            KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            EditMappingDialog dialog = new EditMappingDialog(
                    obdaModelManager.getCurrentOBDAModel(),
                    colorSettings,
                    IDGenerator.getNextUniqueID("MAPID-"));
            DialogUtils.setLocationRelativeToProtegeAndOpen(editorKit, dialog);
        }
    };

    private final OntopAbstractAction removeAction = new OntopAbstractAction(
            "Remove",
            "minus.png",
            "Remove selected triples maps",
            KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!removeConfirm(mappingList.getSelectedValuesList()))
                return;

            TriplesMapManager triplesMapCollection = obdaModelManager.getCurrentOBDAModel().getTriplesMapManager();
            for (TriplesMap triplesMap : mappingList.getSelectedValuesList())
                triplesMapCollection.remove(triplesMap.getId());

            mappingList.clearSelection();
        }
    };

    private final OntopAbstractAction copyAction = new OntopAbstractAction(
            "Copy",
            "copy.png",
            "Make a duplicate copy of the selected triples maps (with fresh IDs)",
            null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!copyConfirm(mappingList.getSelectedValuesList()))
                return;

            TriplesMapManager triplesMapCollection = obdaModelManager.getCurrentOBDAModel().getTriplesMapManager();
            for (TriplesMap triplesMap : mappingList.getSelectedValuesList())
                triplesMapCollection.duplicate(triplesMap.getId());
        }
    };

    private final OntopAbstractAction editAction = new OntopAbstractAction("Edit triples map...",
            null, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            EditMappingDialog dialog = new EditMappingDialog(
                    obdaModelManager.getCurrentOBDAModel(),
                    colorSettings,
                    mappingList.getSelectedValue());
            DialogUtils.setLocationRelativeToProtegeAndOpen(editorKit, dialog);
        }
    };

    private final OntopAbstractAction validateAction = new OntopAbstractAction(
            "Validate",
            "validate.png",
            "Validate selected triples maps",
            getKeyStrokeWithCtrlMask(VK_V)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            ValidationSwingWorker worker = new ValidationSwingWorker(
                    MappingManagerPanel.this,
                    mappingList.getSelectedValuesList(),
                    obdaModelManager.getCurrentOBDAModel());
            worker.execute();
        }
    };

    private final OntopAbstractAction executeSQLAction = new OntopAbstractAction("Execute source SQL...",
            null, null, null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SQLQueryDialog dialog = new SQLQueryDialog(
                    obdaModelManager.getCurrentOBDAModel().getDataSource(),
                    mappingList.getSelectedValue().getSqlQuery());
            DialogUtils.setLocationRelativeToProtegeAndOpen(editorKit, dialog);
        }
    };


    private final OntopAbstractAction selectAllAction = new OntopAbstractAction("Select all",
            "select-all.png",
            "Select all triples maps",
            getKeyStrokeWithCtrlMask(VK_A)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            mappingList.setSelectionInterval(0, mappingList.getModel().getSize() - 1);
        }
    };

    private final OntopAbstractAction selectNoneAction = new OntopAbstractAction("Select none",
            "select-none.png",
            "Deselect all triples maps",
            getKeyStrokeWithCtrlMask(VK_N)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            mappingList.clearSelection();
        }
    };

    private void setActionEnabled() {
        List<TriplesMap> selectionList = mappingList.getSelectedValuesList();
        removeAction.setEnabled(!selectionList.isEmpty());
        copyAction.setEnabled(!selectionList.isEmpty());
        validateAction.setEnabled(!selectionList.isEmpty());

        editAction.setEnabled(selectionList.size() == 1);
        executeSQLAction.setEnabled(selectionList.size() == 1);
    }

    @Override
    public void activeOntologyChanged(OBDAModel obdaModel) {
        setFilter("");
        applyFilter(); // forced update
    }
    /**
     * any change of the filter will trigger the model update via processFilterAction()
     * @param filter
     */
    public void setFilter(String filter) {
        filterField.setText(filter);
    }

    private void applyFilter() {
        String filterText = filterField.getText().trim();
        filterCheckbox.setEnabled(!filterText.isEmpty());

        model.setFilter(filterCheckbox.isSelected() && filterCheckbox.isEnabled()
                ? getFilter(filterText)
                : ImmutableList.of());
    }

    private ImmutableList<String> getFilter(String filterText) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String c : filterText.split("\\s+")) {
            String filter = c.endsWith(";") ? c.substring(0, c.length() - 1).trim() : c;
            builder.add(filter);
        }
        return builder.build();
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
