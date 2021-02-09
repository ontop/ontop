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
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.getButton;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class NewMappingDialogPanel extends JPanel {

	private static final long serialVersionUID = 4351696247473906680L;

	private static final Logger LOGGER = LoggerFactory.getLogger(NewMappingDialogPanel.class);

	private static final int MAX_ROWS = 100;

	@Nullable
	private final String id; // null means creating a new mapping

	private final OBDAModelManager obdaModelManager;
	private final JDialog dialog;

	private final TargetQueryStyledDocument targetQueryDocument;

	private final JButton saveMappingButton;
	private final JTable sqlQueryResultTable;
	private final JTextField mappingIdField;
	private final JTextPane targetQueryTextPane;
	private final JTextPane sourceQueryTextPane;

	private final Border defaultBorder;
	private final Border errorBorder;

	private static final int DEFAULT_TOOLTIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOLTIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOLTIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOLTIP_DISMISS_DELAY = 9000;

	private final Action saveMappingAction;

	private boolean allComponentsNonEmpty = false, isValid = false;

	public NewMappingDialogPanel(OBDAModelManager obdaModelManager, String id) {
		this(obdaModelManager,null, "New Mapping", "Create",
				"Add the triples map to the current mapping");

		mappingIdField.setText(id);
	}

	public NewMappingDialogPanel(OBDAModelManager obdaModelManager, SQLPPTriplesMap mapping) {
		this(obdaModelManager, mapping.getId(), "Edit Mapping", "Update",
				"Update the triples map in the current mapping");

		mappingIdField.setText(mapping.getId());

		sourceQueryTextPane.setText(mapping.getSourceQuery().getSQL());

		String trgQuery = obdaModelManager.getActiveOBDAModel().getTargetRendering(mapping);
		targetQueryTextPane.setText(trgQuery);
		targetValidation();
	}

	private NewMappingDialogPanel(OBDAModelManager obdaModelManager, String id, String title, String buttonText, String buttonTooltip) {
		this.obdaModelManager = obdaModelManager;
		this.id = id;

		saveMappingAction = new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveMapping();
			}
		};

		Action cancelAction = new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		};

		Action testSqlQueryAction = new AbstractAction("Test SQL") {
			@Override
			public void actionPerformed(ActionEvent e) {
				testSqlQuery();
			}
		};

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		setLayout(new GridBagLayout());

		add(new JLabel("Mapping ID:"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(8, 10, 8, 0), 0, 0));

		mappingIdField = new JTextField();
		mappingIdField.setFont(new Font("Dialog", Font.BOLD, 12));
		setKeyboardShortcuts(mappingIdField);
		add(mappingIdField, new GridBagConstraints(1, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(8, 0, 8, 10), 0, 0));

		JPanel targetQueryPanel = new JPanel(new BorderLayout());
		targetQueryPanel.add(new JLabel("Target (Triples Template):"), BorderLayout.NORTH);
		targetQueryTextPane = new JTextPane();
		targetQueryTextPane.setBorder(defaultBorder);
		Timer timer = new Timer(1000, e -> targetValidation());
		timer.setRepeats(false);
		targetQueryDocument = new TargetQueryStyledDocument(obdaModelManager, d -> timer.restart());
		targetQueryTextPane.setDocument(targetQueryDocument);
		setKeyboardShortcuts(targetQueryTextPane);
		targetQueryPanel.add(new JScrollPane(targetQueryTextPane), BorderLayout.CENTER);

		JPanel sourceQueryPanel = new JPanel(new BorderLayout());
		sourceQueryPanel.add(new JLabel("Source (SQL Query):"), BorderLayout.NORTH);
		sourceQueryTextPane = new JTextPane();
		sourceQueryTextPane.setBorder(defaultBorder);
		sourceQueryTextPane.setDocument(new SQLQueryStyledDocument());
		setKeyboardShortcuts(sourceQueryTextPane);
		sourceQueryPanel.add(new JScrollPane(sourceQueryTextPane), BorderLayout.CENTER);

		JPanel sqlQueryResultPanel = new JPanel(new BorderLayout());
		sqlQueryResultPanel.add(new JLabel("SQL Query results:"), BorderLayout.NORTH);
		sqlQueryResultTable = new JTable();
		sqlQueryResultTable.setFocusable(false);
		sqlQueryResultPanel.add(new JScrollPane(sqlQueryResultTable), BorderLayout.CENTER);

		JSplitPane sqlSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				sourceQueryPanel,
				sqlQueryResultPanel);
		sqlSplitPane.setResizeWeight(0.6);
		sqlSplitPane.setOneTouchExpandable(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				targetQueryPanel,
				sqlSplitPane);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		add(splitPane, new GridBagConstraints(0, 2, 2, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 10, 0, 10), 0, 0));

		JPanel testSqlQueryPanel = new JPanel(new FlowLayout());
		JButton testSqlQueryButton = getButton("<html><u>T</u>est SQL Query</html>", "execute.png", "Execute the SQL query in the SQL query text pane\nand display the first 100 results in the table.");
		testSqlQueryButton.addActionListener(testSqlQueryAction);
		testSqlQueryPanel.add(testSqlQueryButton);
		testSqlQueryPanel.add(new JLabel("(" + MAX_ROWS + " rows)"));
		add(testSqlQueryPanel,
				new GridBagConstraints(0, 5, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(4, 10, 0, 0), 0, 0));

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		saveMappingButton = getButton(buttonText, "accept.png", buttonTooltip);
		saveMappingButton.setEnabled(false);
		saveMappingButton.addActionListener(saveMappingAction);
		buttonsPanel.add(saveMappingButton);

		JButton cancelButton = getButton("Cancel", "cancel.png", null);
		cancelButton.addActionListener(cancelAction);
		buttonsPanel.add(cancelButton);

		add(buttonsPanel, new GridBagConstraints(0, 7, 2, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 10, 4), 0, 0));

		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(VK_T, CTRL_DOWN_MASK), "test");
		inputMap.put(KeyStroke.getKeyStroke(VK_ENTER, CTRL_DOWN_MASK), "save");
		ActionMap actionMap = getActionMap();
		actionMap.put("test", testSqlQueryAction);
		actionMap.put("save", saveMappingAction);

		this.dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setContentPane(this);
		dialog.setSize(700, 600);
		DialogUtils.installEscapeCloseOperation(dialog);
	}

	public void openDialog(JComponent window) {
		dialog.setLocationRelativeTo(window);
		dialog.setVisible(true);
	}

	private void closeDialog() {
		dialog.setVisible(false);
		dialog.dispose();
	}

	private void setKeyboardShortcuts(JTextComponent component) {
		component.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, 0)));
		component.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, SHIFT_DOWN_MASK)));

		component.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { updateSaveMappingActionStatus(); }
			@Override public void removeUpdate(DocumentEvent e) { updateSaveMappingActionStatus(); }
			@Override public void changedUpdate(DocumentEvent e) { updateSaveMappingActionStatus(); }
		});
	}

	private void updateSaveMappingActionStatus() {
		allComponentsNonEmpty = !mappingIdField.getText().trim().isEmpty()
				&& !targetQueryTextPane.getText().trim().isEmpty()
				&& !sourceQueryTextPane.getText().trim().isEmpty();

		saveMappingAction.setEnabled(isValid && allComponentsNonEmpty);
		saveMappingButton.setEnabled(isValid && allComponentsNonEmpty);
	}

	private void targetValidation() {
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		String error;
		try {
			ImmutableSet<IRI> iris = targetQueryDocument.validate();
			if (iris.isEmpty()) {
				if (!isValid) {
					isValid = true;
					saveMappingAction.setEnabled(allComponentsNonEmpty);
					saveMappingButton.setEnabled(allComponentsNonEmpty);
					targetQueryTextPane.setToolTipText(null);
					targetQueryTextPane.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));
					toolTipManager.setInitialDelay(DEFAULT_TOOLTIP_INITIAL_DELAY);
					toolTipManager.setDismissDelay(DEFAULT_TOOLTIP_DISMISS_DELAY);
				}
				return;
			}
			MutablePrefixManager prefixManager = obdaModelManager.getActiveOBDAModel().getMutablePrefixManager();
			error = iris.stream()
					.map(IRI::getIRIString)
					.map(prefixManager::getShortForm)
					.map(iri -> "\t- " + iri)
					.collect(Collectors.joining(
							",\n",
							"The following predicates are not declared in the ontology:\n",
							"."));
		}
		catch (TargetQueryParserException e) {
			error = (e.getMessage() == null)
					? "Unknown syntax error, check Protege log file."
					: e.getMessage().replace("'<EOF>'", "the end");
		}
		targetQueryTextPane.setBorder(BorderFactory.createCompoundBorder(null, errorBorder));
		toolTipManager.setInitialDelay(ERROR_TOOLTIP_INITIAL_DELAY);
		toolTipManager.setDismissDelay(ERROR_TOOLTIP_DISMISS_DELAY);
		targetQueryTextPane.setToolTipText("<html><body>" +
				error.replace("<", "&lt;")
						.replace(">", "&gt;")
						.replace("\n", "<br>")
						.replace("\t", HTML_TAB)
				+ "</body></html>");
		isValid = false;
		saveMappingButton.setEnabled(false);
		saveMappingAction.setEnabled(false);
	}

	private void saveMapping() {
		try {
			String newId = mappingIdField.getText().trim();
			String target = targetQueryTextPane.getText();
			String source = sourceQueryTextPane.getText().trim();
			OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();

			ImmutableList<TargetAtom> targetQuery = obdaModel.parseTargetQuery(target);
			LOGGER.info("Insert Mapping: \n"+ target + "\n" + source);

			if (id == null)
				obdaModel.add(newId, source, targetQuery);
			else
				obdaModel.update(id, newId, source, targetQuery);

			closeDialog();
		}
		catch (DuplicateMappingException e) {
			JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e.getMessage() + " is already taken");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e.getMessage());
		}
	}

	private void testSqlQuery() {
		ExecuteSQLQuerySwingWorker worker = new ExecuteSQLQuerySwingWorker(dialog);
		worker.execute();
	}

	private class ExecuteSQLQuerySwingWorker extends SwingWorkerWithCompletionPercentageMonitor<DefaultTableModel, Void> {

		protected ExecuteSQLQuerySwingWorker(Component parent) {
			super(parent, "<html><h3>Executing SQL query:</h3></html>");
			progressMonitor.setCancelAction(this::doCancel);
		}

		private Statement statement;

		private void doCancel() {
			JDBCConnectionManager.cancelQuietly(statement);
		}

		@Override
		protected DefaultTableModel doInBackground() throws Exception {
			start("initializing...");
			setMaxTicks(100);
			JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
			OBDADataSource dataSource = obdaModelManager.getDatasource();
			try (Connection conn = man.getConnection(dataSource.getURL(), dataSource.getUsername(), dataSource.getPassword())) {
				statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				statement.setMaxRows(MAX_ROWS);
				try (ResultSet rs = statement.executeQuery(sourceQueryTextPane.getText().trim())) {
					ResultSetMetaData metadata = rs.getMetaData();
					int numcols = metadata.getColumnCount();
					String[] columns = new String[numcols];
					for (int i = 1; i <= numcols; i++)
						columns[i - 1] = metadata.getColumnLabel(i);
					DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(columns);
					startLoop(this::getCompletionPercentage, () -> String.format("%d%% rows retrieved...", getCompletionPercentage()));
					while (rs.next()) {
						String[] values = new String[numcols];
						for (int i = 1; i <= numcols; i++)
							values[i - 1] = rs.getString(i);
						tableModel.addRow(values);
						tick();
					}
					endLoop("generating table...");
					end();
					return tableModel;
				}
			}
			finally {
				JDBCConnectionManager.closeQuietly(statement);
			}
		}

		@Override
		public void done() {
			try {
				DefaultTableModel tableModel = complete();
				sqlQueryResultTable.setModel(tableModel);
			}
			catch (CancellationException | InterruptedException ignore) {
			}
			catch (ExecutionException e) {
				DialogUtils.showErrorDialog(dialog, dialog.getTitle(), "Executing SQL query error.", LOGGER, e,
						obdaModelManager.getDatasource());
			}
		}
	}
}
