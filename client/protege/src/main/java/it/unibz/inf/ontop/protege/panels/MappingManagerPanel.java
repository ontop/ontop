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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.gui.dialogs.EditMappingDialog;
import it.unibz.inf.ontop.protege.gui.dialogs.SQLQueryDialog;
import it.unibz.inf.ontop.protege.gui.models.*;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class MappingManagerPanel extends JPanel {

	private static final long serialVersionUID = -486013653814714526L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MappingManagerPanel.class);

	private final OBDAModelManager obdaModelManager;

    private final JList<TriplesMap> mappingList;

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

        Action addMappingAction = new AbstractAction("New triples map") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createMapping();
            }
        };

        Action removeMappingAction = new AbstractAction("Remove triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeMapping(mappingList.getSelectedValuesList());
                mappingList.clearSelection();
            }
        };
        removeMappingAction.setEnabled(false);

        Action copyMappingAction = new AbstractAction("Copy triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyMapping(mappingList.getSelectedValuesList());
            }
        };
        copyMappingAction.setEnabled(false);

        Action editMappingAction = new AbstractAction("Edit triples map") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editMapping(mappingList.getSelectedValue());
            }
        };
        editMappingAction.setEnabled(false);

        Action validateSQLAction = new AbstractAction("Validate triples maps") {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateMapping(mappingList.getSelectedValuesList());
            }
        };
        validateSQLAction.setEnabled(false);

        Action executeSQLAction = new AbstractAction("Execute source SQL") {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeMappingSourceQuery(mappingList.getSelectedValue());
            }
        };
        executeSQLAction.setEnabled(false);

        JPanel pnlMappingButtons = new JPanel(new GridBagLayout());

        pnlMappingButtons.add(
                DialogUtils.getButton(
                        "Create",
                        "plus.png",
                        "Create a new triples map",
                        addMappingAction),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton removeMappingButton = DialogUtils.getButton(
                "Remove",
                "minus.png",
                "Remove the selected triples maps",
                removeMappingAction);
        removeMappingButton.setEnabled(false);
        pnlMappingButtons.add(removeMappingButton,
                new GridBagConstraints(1, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton copyMappingButton = DialogUtils.getButton(
                "Copy",
                "copy.png",
                "Make a duplicate copy of the selected triples maps (with fresh IDs)",
                copyMappingAction);
        copyMappingButton.setEnabled(false);
        pnlMappingButtons.add(copyMappingButton,
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlMappingButtons.add(new JPanel(),
                new GridBagConstraints(3, 0, 1, 1, 1, 0,
                        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlMappingButtons.add(
                DialogUtils.getButton(
                        "Select all",
                        "select-all.png",
                        null,
                        evt -> mappingList.setSelectionInterval(0, mappingList.getModel().getSize() - 1)),
                new GridBagConstraints(7, 0, 1, 1, 0, 0,
                        GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pnlMappingButtons.add(
                DialogUtils.getButton(
                        "Select none",
                        "select-none.png",
                        null,
                        evt -> mappingList.clearSelection()),
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
        txtFilter.getDocument().addDocumentListener((SimpleDocumentListener)
                            e ->  processFilterAction());

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
            List<TriplesMap> selectionList = mappingList.getSelectedValuesList();
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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");

        ActionMap actionMap = mappingList.getActionMap();
        actionMap.put("remove", removeMappingAction);
        actionMap.put("add", addMappingAction);
        actionMap.put("edit", editMappingAction);

        mappingList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    editMappingAction.actionPerformed(
                            new ActionEvent(mappingList, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        MappingFilteredListModel model = new MappingFilteredListModel(obdaModelManager.getTriplesMapCollection());
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


    public void editMapping(TriplesMap triplesMap) {
		EditMappingDialog dialog = new EditMappingDialog(obdaModelManager, triplesMap);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
	}

    private void validateMapping(List<TriplesMap> selectionList) {
        ValidationSwingWorker worker = new ValidationSwingWorker(this, selectionList, obdaModelManager.getDatasource());
        worker.execute();
    }

	private static final class ValidationReport {
	    private final String id;
	    private final TriplesMap.Status status;
	    private final String sqlErrorMessage;
	    private final ImmutableList<String> invalidPlaceholders;

        ValidationReport(String id, TriplesMap.Status status)  {
            this.id = id;
            this.status = status;
            this.sqlErrorMessage = null;
            this.invalidPlaceholders = ImmutableList.of();
        }
        ValidationReport(String id, String sqlErrorMessage)  {
            this.id = id;
            this.status = TriplesMap.Status.INVALID;
            this.sqlErrorMessage = sqlErrorMessage;
            this.invalidPlaceholders = ImmutableList.of();
        }
        ValidationReport(String id, ImmutableList<String> invalidPlaceholders)  {
            this.id = id;
            this.status = TriplesMap.Status.INVALID;
            this.sqlErrorMessage = null;
            this.invalidPlaceholders = invalidPlaceholders;
        }
    }

	private class ValidationSwingWorker extends SwingWorkerWithCompletionPercentageMonitor<Void, ValidationReport> {
        private final Component parent;
        private final List<TriplesMap> triplesMapList;
        private final OBDADataSource dataSource;
        private int invalidTriplesMapCount;

        private static final String DIALOG_TITLE = "Triples Maps Validation";

	    ValidationSwingWorker(Component parent, List<TriplesMap> triplesMapList, OBDADataSource dataSource) {
            super(parent, "<html><h3>Validating Triples Maps:</h3></html>");
            this.parent = parent;
            this.dataSource = dataSource;
	        this.triplesMapList = triplesMapList;
        }

        @Override
        protected Void doInBackground() throws Exception {
            start("initializing...");

            setMaxTicks(triplesMapList.size());
            startLoop(this::getCompletionPercentage, () -> String.format("%d%% completed.", getCompletionPercentage()));

            try (Connection conn = dataSource.getConnection();
                 Statement statement = conn.createStatement()) {
                statement.setMaxRows(1);
                for (TriplesMap triplesMap : triplesMapList) {
                    ImmutableList.Builder<String> builder = ImmutableList.builder();
                    try (ResultSet rs = statement.executeQuery(triplesMap.getSqlQuery())) {
                        if (rs.next()) {
                            ImmutableSet<String> vars = triplesMap.getTargetAtoms().stream()
                                    .flatMap(a -> a.getSubstitution().getImmutableMap().values().stream())
                                    .flatMap(ImmutableTerm::getVariableStream)
                                    .map(Variable::getName)
                                    .collect(ImmutableCollectors.toSet());
                            for (String var : vars) {
                                try {
                                    String s = rs.getString(var);
                                }
                                catch (SQLException e) {
                                    builder.add(var);
                                }
                            }
                        }
                        ImmutableList<String> invalidPlaceholders = builder.build();
                        if (invalidPlaceholders.isEmpty())
                            publish(new ValidationReport(triplesMap.getId(), TriplesMap.Status.VALID));
                        else
                            publish(new ValidationReport(triplesMap.getId(), invalidPlaceholders));
                    }
                    catch (SQLException e) {
                        publish(new ValidationReport(triplesMap.getId(), e.getMessage()));
                    }
                    tick();
                }
            }
            endLoop("");
            end();
            return null;
        }

        @Override
        protected void process(List<ValidationReport> reports) {
	        for (ValidationReport report : reports) {
	            if (report.status == TriplesMap.Status.INVALID)
                    invalidTriplesMapCount++;

                obdaModelManager.getTriplesMapCollection().setStatus(
                        report.id,
                        report.status,
                        report.sqlErrorMessage,
                        report.invalidPlaceholders);
            }

        }

        @Override
        protected void done() {
            try {
                complete();
                String message = invalidTriplesMapCount == 0
                        ? (triplesMapList.size() == 1
                            ? "The only triples map has been found valid."
                            : "All <b>" + triplesMapList.size() + "</b> triples map have been found valid.")
                        : "<b>" + invalidTriplesMapCount + "</b> triples map" + (invalidTriplesMapCount > 1 ? "s" : "") + " (out of <b>" +
                                triplesMapList.size() + "</b>) have been found invalid.";

                JOptionPane.showMessageDialog(parent,
                        "<html><h3>Validation of the triples maps is complete.</h3><br>" +
                                HTML_TAB + message + "<br></html>",
                        DIALOG_TITLE,
                        JOptionPane.INFORMATION_MESSAGE,
                        IconLoader.getOntopIcon());
            }
            catch (CancellationException | InterruptedException ignore) {
            }
            catch (ExecutionException e) {
                DialogUtils.showErrorDialog(parent, DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, dataSource);
            }
        }

    }

	private void executeMappingSourceQuery(TriplesMap triplesMap) {
		SQLQueryDialog dialog = new SQLQueryDialog(
		        obdaModelManager.getDatasource(),
                triplesMap.getSqlQuery());
        dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	private void copyMapping(List<TriplesMap> selection) {
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

        TriplesMapCollection triplesMapCollection = obdaModelManager.getTriplesMapCollection();
        for (TriplesMap triplesMap : selection)
            triplesMapCollection.duplicate(triplesMap.getId());
	}

    private void removeMapping(List<TriplesMap> selection) {
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

        TriplesMapCollection triplesMapCollection = obdaModelManager.getTriplesMapCollection();
        for (TriplesMap triplesMap : selection)
            triplesMapCollection.remove(triplesMap.getId());
	}

    private void createMapping() {
        EditMappingDialog dialog = new EditMappingDialog(
                obdaModelManager,
                IDGenerator.getNextUniqueID("MAPID-"));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
