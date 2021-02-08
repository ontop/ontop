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

import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.protege.core.DuplicateMappingException;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.dialogs.MappingValidationDialog;
import it.unibz.inf.ontop.protege.gui.models.*;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.validation.SQLSourceQueryValidator;
import it.unibz.inf.ontop.utils.IDGenerator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class MappingManagerPanel extends JPanel {

	private static final long serialVersionUID = -486013653814714526L;

	private SQLSourceQueryValidator validator;

	private final OBDAModelManager obdaModelManager;

	private boolean canceled;

    private final JList<SQLPPTriplesMap> mappingList;

    private final JCheckBox chkFilter;
    private final JTextField txtFilter;

    /**
	 * Creates a new panel.
	 *
	 * @param obdaModelManager
	 */
	public MappingManagerPanel(OBDAModelManager obdaModelManager) {
        this.obdaModelManager = obdaModelManager;

        setLayout(new BorderLayout());

        mappingList = new JList<>();
        // Setting up the mappings tree
        mappingList.setCellRenderer(new MappingListRenderer(obdaModelManager));
        mappingList.setFixedCellWidth(-1);
        mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(mappingList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        Action addMappingAction = new AbstractAction("Create mapping...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createMapping();
            }
        };

        Action removeMappingAction = new AbstractAction("Remove mapping(s)...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeMapping(mappingList.getSelectedValuesList());
                mappingList.clearSelection();
            }
        };
        removeMappingAction.setEnabled(false);

        Action copyMappingAction = new AbstractAction("Copy mapping(s)...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyMapping(mappingList.getSelectedValuesList());
            }
        };
        copyMappingAction.setEnabled(false);

        Action editMappingAction = new AbstractAction("Edit mapping...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editMapping(mappingList.getSelectedValue());
            }
        };
        editMappingAction.setEnabled(false);

        Action validateSQLAction = new AbstractAction("Validate SQL") {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateMapping(mappingList.getSelectedValuesList());
            }
        };
        validateSQLAction.setEnabled(false);

        Action executeSQLAction = new AbstractAction("Execute SQL") {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeMappingSourceQuery(mappingList.getSelectedValue());
            }
        };
        executeSQLAction.setEnabled(false);

        JPanel pnlMappingButtons = new JPanel(new GridBagLayout());

        JButton createMappingButton = DialogUtils.getButton("Create", "plus.png", "Create a new mapping");
        createMappingButton.addActionListener(addMappingAction);
        pnlMappingButtons.add(createMappingButton,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton removeMappingButton = DialogUtils.getButton("Remove", "minus.png", "Remove the selected mapping(s)");
        removeMappingButton.addActionListener(removeMappingAction);
        removeMappingButton.setEnabled(false);
        pnlMappingButtons.add(removeMappingButton,
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton copyMappingButton = DialogUtils.getButton("Copy", "copy.png", "Make a duplicate of the selected mapping");
        copyMappingButton.addActionListener(copyMappingAction);
        copyMappingButton.setEnabled(false);
        pnlMappingButtons.add(copyMappingButton,
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlMappingButtons.add(new JPanel(),
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton selectAllButton = DialogUtils.getButton("Select all", "select-all.png", "Select all");
        selectAllButton.addActionListener(evt -> mappingList.setSelectionInterval(0, mappingList.getModel().getSize() - 1));
        pnlMappingButtons.add(selectAllButton,
                new GridBagConstraints(7, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton selectNoneButton = DialogUtils.getButton("Select none", "select-none.png", "Select none");
        selectNoneButton.addActionListener(evt -> mappingList.clearSelection());
        pnlMappingButtons.add(selectNoneButton,
                new GridBagConstraints(8, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        add(pnlMappingButtons, BorderLayout.NORTH);

        JPanel pnlExtraButtons = new JPanel(new GridBagLayout());

        JLabel mappingStatusLabel = new JLabel();
        pnlExtraButtons.add(mappingStatusLabel,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlExtraButtons.add(new JLabel("Search:"),
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 30, 2, 2), 0, 0));

        txtFilter = new JTextField();
        pnlExtraButtons.add(txtFilter,
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { processFilterAction(); }
            @Override public void removeUpdate(DocumentEvent e) { processFilterAction(); }
            @Override public void changedUpdate(DocumentEvent e) { processFilterAction(); }
        });

        chkFilter = new JCheckBox("Enable filter");
        chkFilter.setEnabled(false);
        chkFilter.addItemListener(evt -> processFilterAction());
        pnlExtraButtons.add(chkFilter,
                new GridBagConstraints(4, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 20), 0, 0));

        add(pnlExtraButtons, BorderLayout.SOUTH);

        JPopupMenu menuMappings = new JPopupMenu();
        menuMappings.add(new JMenuItem(addMappingAction));
        menuMappings.add(new JMenuItem(removeMappingAction));
        menuMappings.add(new JMenuItem(copyMappingAction));
        menuMappings.add(new JMenuItem(editMappingAction));
        menuMappings.addSeparator();
        menuMappings.add(new JMenuItem(validateSQLAction));
        menuMappings.add(new JMenuItem(executeSQLAction));
        mappingList.setComponentPopupMenu(menuMappings);

        mappingList.addListSelectionListener(evt -> {
            List<SQLPPTriplesMap> selectionList = mappingList.getSelectedValuesList();
            removeMappingAction.setEnabled(!selectionList.isEmpty());
            removeMappingButton.setEnabled(!selectionList.isEmpty());
            copyMappingAction.setEnabled(!selectionList.isEmpty());
            copyMappingButton.setEnabled(!selectionList.isEmpty());
            validateSQLAction.setEnabled(!selectionList.isEmpty());

            editMappingAction.setEnabled(selectionList.size() == 1);
            executeSQLAction.setEnabled(selectionList.size() == 1);
        });

        InputMap inputMap = mappingList.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "remove");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "add");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "edit");

        ActionMap actionMap = mappingList.getActionMap();
        actionMap.put("remove", removeMappingAction);
        actionMap.put("add", addMappingAction);
        actionMap.put("edit", editMappingAction);

        MappingFilteredListModel model = new MappingFilteredListModel(obdaModelManager.getActiveOBDAModel());
        model.addListDataListener(new ListDataListener() {
            @Override public void intervalRemoved(ListDataEvent e) { updateMappingSize(); }
            @Override public void intervalAdded(ListDataEvent e) { updateMappingSize(); }
            @Override public void contentsChanged(ListDataEvent e) { updateMappingSize(); }
            private void updateMappingSize() {
                mappingStatusLabel.setText("<html>Mapping size: <b>" +
                        obdaModelManager.getActiveOBDAModel().getMapping().size() + "</b></html>");
            }
        });
        mappingList.setModel(model);
    }

    public void setFilter(String filter) {
        txtFilter.setText(filter);
    }

    public void datasourceChanged() {
        // Update the mapping tree.
        MappingFilteredListModel model = (MappingFilteredListModel) mappingList.getModel();
        model.setFocusedSource();

        mappingList.revalidate();
    }

    private void processFilterAction() {
        String filterText = txtFilter.getText().trim();
        chkFilter.setEnabled(!filterText.isEmpty());

        MappingFilteredListModel model = (MappingFilteredListModel) mappingList.getModel();
        model.setFilter(chkFilter.isSelected() && chkFilter.isEnabled() ? filterText : null);
    }



    public void editMapping(SQLPPTriplesMap mapping) {
		JDialog dialog = new JDialog();

		dialog.setTitle("Edit Mapping");
		dialog.setModal(true);

		NewMappingDialogPanel panel = new NewMappingDialogPanel(obdaModelManager, dialog);
		panel.setMapping(mapping);
		dialog.setContentPane(panel);
		dialog.setSize(600, 500);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

    private void validateMapping(List<SQLPPTriplesMap> selectionList) {
		MappingValidationDialog outputField = new MappingValidationDialog();
		outputField.setLocationRelativeTo(getParent());

		Thread validatorThread = new Thread(() -> {
            canceled = false;
            outputField.addText("Validating " + selectionList.size() + " SQL queries.\n", outputField.NORMAL);
            for (SQLPPTriplesMap mapping : selectionList) {
                String id = mapping.getId();
                outputField.addText("  id: '" + id + "'... ", outputField.NORMAL);
                OntopSQLCredentialSettings settings = obdaModelManager.getConfigurationConnectionSettings();
                validator = new SQLSourceQueryValidator(settings, mapping.getSourceQuery());
                long timestart = System.nanoTime();

                if (canceled) {
                    return;
                }
                if (validator.validate()) {
                    long timestop = System.nanoTime();
                    String output = " valid  \n";
                    outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ns. Result: ", outputField.NORMAL);
                    outputField.addText(output, outputField.VALID);
                }
                else {
                    long timestop = System.nanoTime();
                    String output = " invalid Reason: " + validator.getReason().getMessage() + " \n";
                    outputField.addText("Time to query: " + ((timestop - timestart) / 1000) + " ns. Result: ", outputField.NORMAL);
                    outputField.addText(output, outputField.CRITICAL_ERROR);
                }

                if (canceled) {
                    return;
                }
            }
            outputField.setVisible(true);
        });
		validatorThread.start();

		Thread cancelThread = new Thread(() -> {
            while (!outputField.closed) {
                try {
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (validatorThread.isAlive()) {
                try {
                    Thread.currentThread();
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    canceled = true;
                    if (validator !=null) {
                        validator.cancelValidation();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
		cancelThread.start();
	}

	private void executeMappingSourceQuery(SQLPPTriplesMap mapping) {
		String sqlQuery = mapping.getSourceQuery().getSQL();

		SQLQueryPanel pnlQueryResult = new SQLQueryPanel(obdaModelManager.getDatasource(), sqlQuery);

		JDialog dlgQueryResult = new JDialog();
		DialogUtils.installEscapeCloseOperation(dlgQueryResult);
		dlgQueryResult.setContentPane(pnlQueryResult);
		dlgQueryResult.pack();
		dlgQueryResult.setLocationRelativeTo(this);
		dlgQueryResult.setVisible(true);
		dlgQueryResult.setTitle("SQL Query Result");
	}

	private void copyMapping(List<SQLPPTriplesMap> selection) {
		if (JOptionPane.showConfirmDialog(
                this,
                "<html>This will create a <b>copy</b> of the selected <b>" + selection.size() +
                        " mapping" + (selection.size() == 1 ? "" : "s") + "</b>.<br><br>" +
                        "Do you wish to <b>continue</b>?<br></html>",
                "Copy mapping confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                IconLoader.getOntopIcon()) != JOptionPane.YES_OPTION)
			return;

        try {
            OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
            for (SQLPPTriplesMap mapping : selection) {
                String id = mapping.getId();
                // find the next available ID
                String newId = id;
                for (int index = 0; index < 999999999; index++) {
                    newId = id + "(" + index + ")";
                    if (!obdaModel.containsMappingId(newId))
                        break;
                }
                obdaModel.add(newId, mapping.getSourceQuery().getSQL(), mapping.getTargetAtoms());
            }
        }
        catch (DuplicateMappingException e) {
            // SHOULD NEVER HAPPEN
            JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + e.getMessage());
            return;
        }
	}

    private void removeMapping(List<SQLPPTriplesMap> selection) {
		if (JOptionPane.showConfirmDialog(
                this,
                "<html>This will <b>remove</b> the selected <b>" + selection.size() +
                        " mapping" + (selection.size() == 1 ? "" : "s") + "</b>.<br><br>" +
                        "Do you wish to <b>continue</b>?.<br></html>",
                "Remove mapping confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                IconLoader.getOntopIcon()) != JOptionPane.YES_OPTION)
			return;

        OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
        for (SQLPPTriplesMap mapping : selection)
            obdaModel.remove(mapping.getId());
	}

    private void createMapping() {
        String id = IDGenerator.getNextUniqueID("MAPID-");

        JDialog dialog = new JDialog();
        dialog.setTitle("New Mapping");
        dialog.setModal(true);

        NewMappingDialogPanel panel = new NewMappingDialogPanel(obdaModelManager, dialog);
        panel.setID(id);
        dialog.setContentPane(panel);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
