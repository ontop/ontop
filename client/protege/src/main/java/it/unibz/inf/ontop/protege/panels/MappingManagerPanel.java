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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.protege.core.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialConfiguration;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.dialogs.MappingValidationDialog;
import it.unibz.inf.ontop.protege.gui.treemodels.*;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.validation.SQLSourceQueryValidator;
import it.unibz.inf.ontop.utils.IDGenerator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MappingManagerPanel extends JPanel {

	private static final long serialVersionUID = -486013653814714526L;

	private SQLSourceQueryValidator validator;

	private final OBDAModelManager obdaModelManager;

	private boolean canceled;


    private final JCheckBox chkFilter;
    private final JButton cmdAddMapping;
    private final JButton cmdDeselectAll;
    private final JButton cmdDuplicateMapping;
    private final JButton cmdRemoveMapping;
    private final JButton cmdSelectAll;
    private final JLabel mappingStatusLabel;
    private final JList<SQLPPTriplesMap> mappingList;
    private final JPopupMenu menuMappings;
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
        mappingList.setCellRenderer(new OBDAMappingListRenderer(obdaModelManager));
        mappingList.setFixedCellWidth(-1);
        mappingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        SynchronizedMappingListModel model = new SynchronizedMappingListModel(obdaModelManager.getActiveOBDAModel());
        model.addListDataListener(new ListDataListener() {
            @Override public void intervalRemoved(ListDataEvent e) { updateMappingSize(); }
            @Override public void intervalAdded(ListDataEvent e) { updateMappingSize(); }
            @Override public void contentsChanged(ListDataEvent e) { updateMappingSize(); }
        });
        mappingList.setModel(model);
        add(new JScrollPane(mappingList), BorderLayout.CENTER);

        JPanel pnlMappingButtons = new JPanel(new GridBagLayout());

        cmdAddMapping = DialogUtils.getButton("Create", "plus.png", "Create a new mapping");
        cmdAddMapping.addActionListener(evt -> addMapping());
        pnlMappingButtons.add(cmdAddMapping,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        cmdRemoveMapping = DialogUtils.getButton("Remove", "minus.png", "Remove the selected mapping(s)");
        cmdRemoveMapping.addActionListener(evt -> removeMapping());
        pnlMappingButtons.add(cmdRemoveMapping,
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        cmdDuplicateMapping = DialogUtils.getButton("Copy", "copy.png", "Make a duplicate of the selected mapping");
        cmdDuplicateMapping.addActionListener(this::cmdDuplicateMappingActionPerformed);
        pnlMappingButtons.add(cmdDuplicateMapping,
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlMappingButtons.add(new JPanel(),
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));

        cmdSelectAll = DialogUtils.getButton("Select all", "select-all.png", "Select all");
        cmdSelectAll.addActionListener(evt -> mappingList.setSelectionInterval(0, mappingList.getModel().getSize() - 1));
        pnlMappingButtons.add(cmdSelectAll,
                new GridBagConstraints(7, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        cmdDeselectAll = DialogUtils.getButton("Select none", "select-none.png", "Select none");
        cmdDeselectAll.addActionListener(evt -> mappingList.clearSelection());
        pnlMappingButtons.add(cmdDeselectAll,
                new GridBagConstraints(8, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        add(pnlMappingButtons, BorderLayout.NORTH);

        JPanel pnlExtraButtons = new JPanel(new GridBagLayout());

        mappingStatusLabel = new JLabel();
        updateMappingSize();
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


        menuMappings = new JPopupMenu();

        JMenuItem add = new JMenuItem("Create mapping...");
        add.addActionListener(evt -> addMapping());
        menuMappings.add(add);

        JMenuItem delete = new JMenuItem("Remove mapping(s)...");
        delete.addActionListener(evt -> removeMapping());
        menuMappings.add(delete);

        JMenuItem editMapping = new JMenuItem("Edit mapping...");
        editMapping.addActionListener(evt -> editMapping());
        menuMappings.add(editMapping);

        menuMappings.addSeparator();

        JMenuItem menuValidateBody = new JMenuItem("Validate SQL");
        menuValidateBody.addActionListener(this::menuValidateBodyActionPerformed);
        menuMappings.add(menuValidateBody);

        JMenuItem menuExecuteBody = new JMenuItem("Execute SQL");
        menuExecuteBody.addActionListener(this::menuExecuteBodyActionPerformed);
        menuMappings.add(menuExecuteBody);


        mappingList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DELETE:
                    case KeyEvent.VK_BACK_SPACE:
                        removeMapping();
                        break;
                    case KeyEvent.VK_INSERT:
                        addMapping();
                        break;
                    case KeyEvent.VK_SPACE:
                        editMapping();
                        break;
                    default:
                        break;
                }
            }
        });

		mappingList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					editMapping();
			}
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    menuMappings.show(e.getComponent(), e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    menuMappings.show(e.getComponent(), e.getX(), e.getY());
            }
		});
	}

	private void updateMappingSize() {
        mappingStatusLabel.setText("<html>Mapping size: <b>" + obdaModelManager.getActiveOBDAModel().getMapping().size() + "</b></html>");
    }

    public void editMapping() {
		SQLPPTriplesMap mapping = mappingList.getSelectedValue();
		if (mapping == null) {
			return;
		}
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

    /***
	 * The action for the search field and the search checkbox. If the checkbox
	 * is not selected it cleans the filters. If it is selected it updates to
	 * the current search string.
	 */
	private void processFilterAction() {
	    String filterText = txtFilter.getText().trim();
	    chkFilter.setEnabled(!filterText.isEmpty());

        SynchronizedMappingListModel model = (SynchronizedMappingListModel) mappingList.getModel();
        model.setFilter(chkFilter.isSelected() && chkFilter.isEnabled() ? filterText : null);
	}

    private void menuValidateBodyActionPerformed(java.awt.event.ActionEvent evt) {
		MappingValidationDialog outputField = new MappingValidationDialog();
		outputField.setLocationRelativeTo(getParent());

		Thread validatorThread = new Thread(() -> {
            canceled = false;
            List<SQLPPTriplesMap> path = mappingList.getSelectedValuesList();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No mappings have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            outputField.addText("Validating " + path.size() + " SQL queries.\n", outputField.NORMAL);
            for (SQLPPTriplesMap mapping : path) {
                String id = mapping.getId();
                outputField.addText("  id: '" + id + "'... ", outputField.NORMAL);
                OntopSQLCredentialConfiguration config = OntopSQLCredentialConfiguration.defaultBuilder()
                        .properties(obdaModelManager.getDatasource().asProperties())
                        .build();
                validator = new SQLSourceQueryValidator(config.getSettings(), mapping.getSourceQuery());
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

	private void menuExecuteBodyActionPerformed(ActionEvent evt) {
		SQLPPTriplesMap mapping = mappingList.getSelectedValue();
		if (mapping == null) {
			return;
		}
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

	private void cmdDuplicateMappingActionPerformed(ActionEvent evt) {
		List<SQLPPTriplesMap> selection = mappingList.getSelectedValuesList();
		if (selection.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No mappings have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}

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

            try {
                obdaModel.add(newId, mapping.getSourceQuery().getSQL(), mapping.getTargetAtoms());
            }
            catch (DuplicateMappingException e) {
                JOptionPane.showMessageDialog(this, "Duplicate Mapping: " + newId);
                return;
            }
        }
	}

    private void removeMapping() {
		List<SQLPPTriplesMap> selection = mappingList.getSelectedValuesList();
		if (selection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No mappings have been selected", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}

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

        for (SQLPPTriplesMap mapping : selection)
            obdaModelManager.getActiveOBDAModel().remove(mapping.getId());

		mappingList.clearSelection();
	}

    private void addMapping() {
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

	public void setFilter(String filter) {
		txtFilter.setText(filter);
		processFilterAction();
	}


    public void datasourceChanged() {
		// Update the mapping tree.
		SynchronizedMappingListModel model = (SynchronizedMappingListModel) mappingList.getModel();
		model.setFocusedSource();

		mappingList.revalidate();
	}
}
