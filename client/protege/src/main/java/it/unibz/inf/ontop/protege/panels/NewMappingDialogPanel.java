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
import it.unibz.inf.ontop.protege.gui.models.ResultSetTableModel;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.getButton;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class NewMappingDialogPanel extends JPanel {

	private static final long serialVersionUID = 4351696247473906680L;

	private static final Logger LOGGER = LoggerFactory.getLogger(NewMappingDialogPanel.class);

	@Nullable
	private final String id; // null means creating a new mapping

	private final OBDAModelManager obdaModelManager;
	private final JDialog parent;

	private final TargetQueryStyledDocument targetDocument;

	private final JButton cmdInsertMapping;
	private final JTable tblQueryResult;
	private final JTextField txtMappingID;
	private final JTextPane txtTargetQuery;
	private final JTextPane txtSourceQuery;

	private final Border defaultBorder;
	private final Border errorBorder;

	private static final int DEFAULT_TOOLTIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOLTIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOLTIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOLTIP_DISMISS_DELAY = 9000;

	private final Action testSqlAction;
	private final Action storeMappingAction;
	private final Action cancelAction;

	private boolean allComponentsNonEmpty = false, isValid = false;

	public NewMappingDialogPanel(OBDAModelManager obdaModelManager, String id) {
		this(obdaModelManager,null, "New Mapping", "Create",
				"Add the triples map to the current mapping");

		txtMappingID.setText(id);
	}

	public NewMappingDialogPanel(OBDAModelManager obdaModelManager, SQLPPTriplesMap mapping) {
		this(obdaModelManager, mapping.getId(), "Edit Mapping", "Update",
				"Update the triples map in the current mapping");

		txtMappingID.setText(mapping.getId());

		txtSourceQuery.setText(mapping.getSourceQuery().getSQL());

		String trgQuery = obdaModelManager.getActiveOBDAModel().getTargetRendering(mapping);
		txtTargetQuery.setText(trgQuery);
		targetValidation();
	}

	private NewMappingDialogPanel(OBDAModelManager obdaModelManager, String id, String title, String buttonText, String buttonTooltip) {
		this.obdaModelManager = obdaModelManager;
		this.id = id;

		testSqlAction = new AbstractAction("Test SQL Query") {
			@Override
			public void actionPerformed(ActionEvent e) {
				testSqlQuery();
			}
		};

		storeMappingAction = new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				storeMapping();
			}
		};

		cancelAction = new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		};

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		setLayout(new GridBagLayout());

		add(new JLabel("Mapping ID:"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(8, 10, 8, 0), 0, 0));

		txtMappingID = new JTextField();
		txtMappingID.setFont(new Font("Dialog", Font.BOLD, 12));
		setKeyboardShortcuts(txtMappingID);
		add(txtMappingID, new GridBagConstraints(1, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(8, 0, 8, 10), 0, 0));

		JPanel pnlTargetQueryEditor = new JPanel(new BorderLayout());
		pnlTargetQueryEditor.add(new JLabel("Target (Triples Template):"), BorderLayout.NORTH);
		txtTargetQuery = new JTextPane();
		txtTargetQuery.setBorder(defaultBorder);
		Timer timer = new Timer(1000, e -> targetValidation());
		timer.setRepeats(false);
		targetDocument = new TargetQueryStyledDocument(obdaModelManager, d -> timer.restart());
		txtTargetQuery.setDocument(targetDocument);
		setKeyboardShortcuts(txtTargetQuery);
		pnlTargetQueryEditor.add(new JScrollPane(txtTargetQuery), BorderLayout.CENTER);

		JPanel pnlSourceQueryEditor = new JPanel(new BorderLayout());
		pnlSourceQueryEditor.add(new JLabel("Source (SQL Query):"), BorderLayout.NORTH);
		txtSourceQuery = new JTextPane();
		txtSourceQuery.setBorder(defaultBorder);
		txtSourceQuery.setDocument(new SQLQueryStyledDocument());
		setKeyboardShortcuts(txtSourceQuery);
		pnlSourceQueryEditor.add(new JScrollPane(txtSourceQuery), BorderLayout.CENTER);

		JPanel pnlQueryResult = new JPanel(new BorderLayout());
		pnlQueryResult.add(new JLabel("SQL Query results:"), BorderLayout.NORTH);
		tblQueryResult = new JTable();
		tblQueryResult.setFocusable(false);
		pnlQueryResult.add(new JScrollPane(tblQueryResult), BorderLayout.CENTER);

		JSplitPane splitSQL = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				pnlSourceQueryEditor,
				pnlQueryResult);
		splitSQL.setResizeWeight(0.6);
		splitSQL.setOneTouchExpandable(true);

		JSplitPane splitTargetSource = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				pnlTargetQueryEditor,
				splitSQL);
		splitTargetSource.setResizeWeight(0.5);
		splitTargetSource.setOneTouchExpandable(true);

		add(splitTargetSource, new GridBagConstraints(0, 2, 2, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(0, 10, 0, 10), 0, 0));

		JPanel pnlTestButton = new JPanel();
		JButton cmdTestQuery = getButton("<html><u>T</u>est SQL Query</html>", "execute.png", "Execute the SQL query in the SQL query text pane\nand display the first 100 results in the table.");
		cmdTestQuery.addActionListener(testSqlAction);
		pnlTestButton.add(cmdTestQuery);
		pnlTestButton.add(new JLabel("(100 rows)"));
		add(pnlTestButton,
				new GridBagConstraints(0, 5, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(4, 10, 0, 0), 0, 0));

		JPanel pnlCommandButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		cmdInsertMapping = getButton(buttonText, "accept.png", buttonTooltip);
		cmdInsertMapping.setEnabled(false);
		cmdInsertMapping.addActionListener(storeMappingAction);
		pnlCommandButton.add(cmdInsertMapping);

		JButton cmdCancel = getButton("Cancel", "cancel.png", null);
		cmdCancel.addActionListener(cancelAction);
		pnlCommandButton.add(cmdCancel);

		add(pnlCommandButton, new GridBagConstraints(0, 7, 2, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 10, 4), 0, 0));

		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(VK_T, CTRL_DOWN_MASK), "test");
		inputMap.put(KeyStroke.getKeyStroke(VK_ENTER, CTRL_DOWN_MASK), "store");
		ActionMap actionMap = getActionMap();
		actionMap.put("test", testSqlAction);
		actionMap.put("store", storeMappingAction);

		this.parent = new JDialog();
		parent.setTitle(title);
		parent.setModal(true);
		parent.setContentPane(this);
		parent.setSize(700, 600);
		DialogUtils.installEscapeCloseOperation(parent);
	}


	private void setKeyboardShortcuts(JTextComponent component) {
		component.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, 0)));
		component.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, SHIFT_DOWN_MASK)));

		component.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { updateStoreActionStatus(); }
			@Override public void removeUpdate(DocumentEvent e) { updateStoreActionStatus(); }
			@Override public void changedUpdate(DocumentEvent e) { updateStoreActionStatus(); }
		});
	}

	private void updateStoreActionStatus() {
		allComponentsNonEmpty = !txtMappingID.getText().trim().isEmpty()
				&& !txtTargetQuery.getText().trim().isEmpty()
				&& !txtSourceQuery.getText().trim().isEmpty();

		storeMappingAction.setEnabled(isValid && allComponentsNonEmpty);
		cmdInsertMapping.setEnabled(isValid && allComponentsNonEmpty);
	}

	private void targetValidation() {
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		String error;
		try {
			ImmutableSet<IRI> iris = targetDocument.validate();
			if (iris.isEmpty()) {
				if (!isValid) {
					isValid = true;
					storeMappingAction.setEnabled(allComponentsNonEmpty);
					cmdInsertMapping.setEnabled(allComponentsNonEmpty);
					txtTargetQuery.setToolTipText(null);
					txtTargetQuery.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));
					toolTipManager.setInitialDelay(DEFAULT_TOOLTIP_INITIAL_DELAY);
					toolTipManager.setDismissDelay(DEFAULT_TOOLTIP_DISMISS_DELAY);
				}
				return;
			}
			MutablePrefixManager prefixManager = obdaModelManager.getActiveOBDAModel().getMutablePrefixManager();
			error = "The following predicates are not declared in the ontology:\n "
					+ iris.stream()
					.map(IRI::getIRIString)
					.map(prefixManager::getShortForm)
					.map(iri -> "\t- " + iri)
					.collect(Collectors.joining(",\n")) + ".";
		}
		catch (TargetQueryParserException e) {
			error = (e.getMessage() == null)
					? "Syntax error, check LOGGER"
					: e.getMessage().replace("'<EOF>'", "the end");
		}
		txtTargetQuery.setBorder(BorderFactory.createCompoundBorder(null, errorBorder));
		toolTipManager.setInitialDelay(ERROR_TOOLTIP_INITIAL_DELAY);
		toolTipManager.setDismissDelay(ERROR_TOOLTIP_DISMISS_DELAY);
		txtTargetQuery.setToolTipText("<html><body>" +
				error.replace("<", "&lt;")
						.replace(">", "&gt;")
						.replace("\n", "<br>")
						.replace("\t", HTML_TAB)
				+ "</body></html>");
		isValid = false;
		cmdInsertMapping.setEnabled(false);
		storeMappingAction.setEnabled(false);
	}



	private void testSqlQuery() {
		ExecuteSQLQuerySwingWorker worker = new ExecuteSQLQuerySwingWorker(parent);
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
				statement.setMaxRows(100);
				try (ResultSet rs = statement.executeQuery(txtSourceQuery.getText().trim())) {
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
				tblQueryResult.setModel(tableModel);
			}
			catch (CancellationException | InterruptedException ignore) {
			}
			catch (ExecutionException e) {
				DialogUtils.showErrorDialog(parent, "Edit Mapping", "Executing SQL query error.", LOGGER, e,
						obdaModelManager.getDatasource());
			}
		}
	}

	private void storeMapping() {
		try {
			String newId = txtMappingID.getText().trim();
			String target = txtTargetQuery.getText();
			String source = txtSourceQuery.getText().trim();
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

	public void openDialog(JComponent window) {
		parent.setLocationRelativeTo(window);
		parent.setVisible(true);
	}

	private void closeDialog() {
		parent.setVisible(false);
		parent.dispose();
	}
}
