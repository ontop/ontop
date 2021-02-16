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

import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.QueryManager;
import it.unibz.inf.ontop.protege.gui.dialogs.SelectPrefixDialog;
import it.unibz.inf.ontop.protege.gui.dialogs.TextQueryResultsDialog;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.utils.OWL2TurtleTranslator;
import it.unibz.inf.ontop.protege.workers.ExportResultsToCSVSwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorkerFactory;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;

/**
 * Creates a new panel to execute queries. Remember to execute the
 * setResultsPanel function to indicate where to display the results.
 */
public class QueryInterfacePanel extends JPanel {

	private static final long serialVersionUID = -5902798157183352944L;

	private final OBDAModelManager obdaModelManager;

	private String groupId = "", queryId = "";

	private final JCheckBox showAllCheckBox;
	private final JCheckBox showShortIriCheckBox;
	private final JTextPane queryTextPane;
	private final JFormattedTextField fetchSizeTextField;
	public final JButton executeButton;


	private final JLabel executionInfoLabel;
	private final JTable queryResultTable;
	private final JTextArea txtSqlTranslation;
	private final JButton exportButton;
	private final JButton stopButton;

	private final OWLEditorKit editorKit;

	public QueryInterfacePanel(OWLEditorKit editorKit) {
		this.editorKit = editorKit;
		this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

		JPopupMenu sparqlPopupMenu = new JPopupMenu();
		JMenuItem getIqMenuItem = new JMenuItem("View Intermediate Query...");
		getIqMenuItem.addActionListener(this::getIqActionPerformed);
		sparqlPopupMenu.add(getIqMenuItem);

		JMenuItem getSqlMenuItem = new JMenuItem("View SQL translation...");
		getSqlMenuItem.addActionListener(this::getSqlActionPerformed);
		sparqlPopupMenu.add(getSqlMenuItem);

		JPanel queryPanel = new JPanel(new BorderLayout());
		queryPanel.setPreferredSize(new Dimension(400, 250));
		queryPanel.setMinimumSize(new Dimension(400, 250));

		queryTextPane = new JTextPane();
		queryTextPane.setFont(new Font("Lucida Console", Font.PLAIN, 14)); // NOI18N
		queryTextPane.setComponentPopupMenu(sparqlPopupMenu);
		queryPanel.add(new JScrollPane(queryTextPane), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new GridBagLayout());

		JPanel executionLimitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		executionLimitPanel.setBorder(BorderFactory.createEtchedBorder());
		executionLimitPanel.add(new JLabel("Show"));

		fetchSizeTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fetchSizeTextField.setValue(100);
		fetchSizeTextField.setColumns(4);
		fetchSizeTextField.setHorizontalAlignment(JTextField.RIGHT);
		executionLimitPanel.add(fetchSizeTextField);

		executionLimitPanel.add(new JLabel("or"));

		showAllCheckBox = new JCheckBox("all results.");
		showAllCheckBox.addActionListener(new ActionListener() {
			private int fetchSizeSaved = 100;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showAllCheckBox.isSelected()) {
					fetchSizeSaved = getFetchSize();
					fetchSizeTextField.setValue(0);
				}
				else 
					fetchSizeTextField.setValue(fetchSizeSaved);
				
				fetchSizeTextField.setEnabled(!showAllCheckBox.isSelected());
			}
		});
		executionLimitPanel.add(showAllCheckBox);
		controlPanel.add(executionLimitPanel,
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		controlPanel.add(new Panel(), // gobbler
				new GridBagConstraints(1, 0, 1, 1, 0.3, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));

		JPanel showShortIriPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		showShortIriPanel.setBorder(BorderFactory.createEtchedBorder());
		showShortIriCheckBox = new JCheckBox("Use short IRIs");
		showShortIriPanel.add(showShortIriCheckBox);
		controlPanel.add(showShortIriPanel,
				new GridBagConstraints(2, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		controlPanel.add(new Panel(), // gobbler
				new GridBagConstraints(3, 0, 1, 1, 1, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));

		OntopAbstractAction prefixesAction = new OntopAbstractAction(
				"Prefixes...",
				"attach.png",
				"Select prefixes to insert into the query") {
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectPrefixDialog dialog = new SelectPrefixDialog(obdaModelManager.getTriplesMapCollection().getMutablePrefixManager(), queryTextPane);
				dialog.setVisible(true);
			}
		};

		OntopAbstractAction executeAction = new OntopAbstractAction(
				"Execute",
				"execute.png",
				"Execute the query and display the results") {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeActionPerformed(e);
			}
		};

		OntopAbstractAction updateAction = new OntopAbstractAction(
				"Update",
				"save.png",
				"Update the query in the catalog") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (queryId.isEmpty()) {
					JOptionPane.showMessageDialog(QueryInterfacePanel.this,
							"Please select first the query you would like to update",
							"Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				QueryManager.Query query = obdaModelManager.getQueryController().getQuery(groupId, queryId);
				query.setQuery(queryTextPane.getText());
			}
		};

		stopButton = DialogUtils.getButton(
				"Stop",
				"stop.png",
				"Stop running the current query",
				null);
		stopButton.setEnabled(false);
		controlPanel.add(stopButton,
				new GridBagConstraints(4, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		executeButton = DialogUtils.getButton(executeAction);
		controlPanel.add(executeButton,
				new GridBagConstraints(5, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));


		queryPanel.add(controlPanel, BorderLayout.SOUTH);

		JPanel topControlPanel = new JPanel(new GridBagLayout());
		topControlPanel.add(new JLabel("SPARQL Query"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		topControlPanel.add(new JPanel(), // gobbler
				new GridBagConstraints(1, 0, 1, 1, 1, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));

		JButton prefixesButton = DialogUtils.getButton(prefixesAction);
		topControlPanel.add(prefixesButton,
				new GridBagConstraints(2, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		JButton updateButton = DialogUtils.getButton(updateAction);
		topControlPanel.add(updateButton,
				new GridBagConstraints(3, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		queryPanel.add(topControlPanel, BorderLayout.NORTH);

		JPanel resultsPanel = new JPanel(new BorderLayout());

		executionInfoLabel = new JLabel("<html>&nbsp;</html>");
		executionInfoLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		resultsPanel.add(executionInfoLabel, BorderLayout.NORTH);

		JTabbedPane resultTabbedPane = new JTabbedPane();
		resultTabbedPane.setMinimumSize(new Dimension(400, 250));
		resultTabbedPane.setPreferredSize(new Dimension(400, 250));

		JPanel sparqlResultPanel = new JPanel(new BorderLayout());
		resultTabbedPane.addTab("SPARQL results", sparqlResultPanel);

		queryResultTable = new JTable(new DefaultTableModel(new String[] {"Results"}, 0));
		sparqlResultPanel.add(new JScrollPane(queryResultTable), BorderLayout.CENTER);

		JPanel controlBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		exportButton = DialogUtils.getButton(
				"Export to CSV...",
				"export.png",
				"Export the results to a CSV file",
				this::exportActionPerformed);
		exportButton.setEnabled(false);
		controlBottomPanel.add(exportButton);

		sparqlResultPanel.add(controlBottomPanel, BorderLayout.SOUTH);

		txtSqlTranslation = new JTextArea();
		resultTabbedPane.addTab("SQL translation", new JScrollPane(txtSqlTranslation));

		JPopupMenu menu = new JPopupMenu();
		JMenuItem countResultsMenuItem = new JMenuItem("Count tuples");
		countResultsMenuItem.addActionListener(this::countResultsActionPerformed);
		menu.add(countResultsMenuItem);
		sparqlResultPanel.setComponentPopupMenu(menu);

		resultsPanel.add(resultTabbedPane, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel, resultsPanel);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);

		InputMap inputMap = queryTextPane.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "execute");
		ActionMap actionMap = queryTextPane.getActionMap();
		actionMap.put("execute", executeAction);
	}

	private void executeActionPerformed(ActionEvent evt) {
		String query = getQuery();
		if (query.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Query editor cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery parsedQuery = parser.parseQuery(query, "http://example.org");
			if (parsedQuery instanceof ParsedTupleQuery) {
				OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), getSelectQueryExecutor());
			}
			else if (parsedQuery instanceof ParsedBooleanQuery) {
				OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), getAskQueryExecutor());
			}
			else if (parsedQuery instanceof ParsedGraphQuery) {
				OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), getGraphQueryExecutor());
			}
			else {
				JOptionPane.showMessageDialog(this, "This type of SPARQL expression is not handled. Please use SELECT, ASK, DESCRIBE, or CONSTRUCT.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error parsing SPARQL query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void selectedQueryChanged(String groupId, String queryId, String query) {
//		if (!this.queryId.isEmpty()) {
//			QueryManager.Query previous = queryManager.getQuery(this.groupId, this.queryId);
//			previous.setQuery(queryTextPane.getText());
//		}
		queryTextPane.setText(query);
		this.groupId = groupId;
		this.queryId = queryId;
		setTableModel(new DefaultTableModel());
		txtSqlTranslation.setText("");
		//executeButton.setEnabled(true);
		//stopButton.setEnabled(false);
		executionInfoLabel.setText("<html>&nbsp</html>");
	}


	private void showActionResult(String s) {
		executionInfoLabel.setText(s);
		executionInfoLabel.setOpaque(false);
	}


	private String getQuery() {
		return queryTextPane.getText();
	}

	private int getFetchSize() {
		return ((Number) fetchSizeTextField.getValue()).intValue();
	}


	private void exportActionPerformed(ActionEvent evt) {
		JFileChooser fileChooser = DialogUtils.getFileChooser(editorKit,
				DialogUtils.getExtensionReplacer(".csv"));

		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fileChooser.getSelectedFile();
		if (!DialogUtils.confirmCanWrite(file, this, "Export to CSV"))
			return;

		ExportResultsToCSVSwingWorker worker = new ExportResultsToCSVSwingWorker(
				this, file, (DefaultTableModel) queryResultTable.getModel());
		worker.execute();
	}


	public void setTableModel(TableModel tableModel) {
		queryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		queryResultTable.setModel(tableModel);
		queryResultTable.invalidate();
		queryResultTable.repaint();

		exportButton.setEnabled(false);
	}

	private OntopQuerySwingWorkerFactory<Boolean, Void> getAskQueryExecutor() {
		setTableModel(new DefaultTableModel());

		return (ontop, query) -> new OntopQuerySwingWorker<Boolean, Void>(ontop, query, getParent(),
						"Execute ASK Query", executeButton, stopButton, executionInfoLabel) {
			@Override
			protected Boolean runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.executeAskQuery(query).getValue();
			}

			@Override
			protected void onCompletion(Boolean result, String sqlQuery) {
				executionInfoLabel.setBackground(result ? Color.GREEN : Color.RED);
				showActionResult("Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ". " +
										"Result: " + result);
				setSQLTranslation(sqlQuery);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<Void, String> getGraphQueryExecutor() {
		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(new String[] {"RDF triples"});
		setTableModel(tableModel);

		return (ontop, query) -> new OntopQuerySwingWorker<Void, String>(ontop, query, getParent(),
						"Execute CONSTRUCT/DESCRIBE Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				OWL2TurtleTranslator owlTranslator = getOWL2TurtleTranslator();
				owlTranslator.getPrefixMap().entrySet().stream()
						.map(e -> "@prefix " + e.getKey() + " " + e.getValue() + ".\n")
						.forEach(this::publish);
				publish("\n");

				setFetchSize(statement);
				try (GraphOWLResultSet rs = statement.executeGraphQuery(query)) {
					while (rs.hasNext()) {
						owlTranslator.render(rs.next())
								.ifPresent(this::publish);
						tick();
					}
				}
				return null;
			}

			@Override
			protected void process(java.util.List<String> chunks) {
				for (String row : chunks)
					tableModel.addRow(new String[] { row });
			}

			@Override
			protected void onCompletion(Void result, String sqlQuery) {
				showActionResult(
						"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ".\n" +
								"OWL Axioms produced: " + getCount());
				setSQLTranslation(sqlQuery);
				exportButton.setEnabled(tableModel.getRowCount() > 0);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<Void, String[]> getSelectQueryExecutor() {
		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(new String[0]);
		setTableModel(tableModel);

		return (ontop, query) -> new OntopQuerySwingWorker<Void, String[]>(ontop, query, getParent(),
						"Execute SELECT Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				OWL2TurtleTranslator owlTranslator = getOWL2TurtleTranslator();
				setFetchSize(statement);
				try (TupleOWLResultSet rs = statement.executeSelectQuery(query)) {
					List<String> signature = rs.getSignature();
					SwingUtilities.invokeLater(() -> {
						tableModel.setColumnIdentifiers(signature.toArray());
						queryResultTable.revalidate();
					});
					int columns = signature.size();
					while (rs.hasNext()) {
						String[] row = new String[columns];
						OWLBindingSet bs = rs.next();
						for (int j = 0; j < columns; j++) {
							String variableName = signature.get(j);
							OWLObject constant = bs.getOWLPropertyAssertionObject(variableName);
							row[j] = owlTranslator.render(constant);
						}
						publish(row);
						tick();
					}
				}
				return null;
			}

			@Override
			protected void process(java.util.List<String[]> chunks) {
				for (String[] row : chunks)
					tableModel.addRow(row);
			}

			@Override
			protected void onCompletion(Void result, String sqlQuery) {
				showActionResult(
						"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ".\n" +
								"Solution mappings returned: " + getCount());
				setSQLTranslation(sqlQuery);
				exportButton.setEnabled(tableModel.getRowCount() > 0);
			}
		};
	}

	private void setFetchSize(OntopOWLStatement statement) throws OntopOWLException {
		if (!showAllCheckBox.isSelected()) {
			DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) statement;
			defaultOntopOWLStatement.setMaxRows(getFetchSize());
		}
	}

	private OWL2TurtleTranslator getOWL2TurtleTranslator() {
		return new OWL2TurtleTranslator(
				obdaModelManager.getTriplesMapCollection().getMutablePrefixManager(),
				showShortIriCheckBox.isSelected());
	}

	public void setSQLTranslation(String sql) {
		txtSqlTranslation.setText(sql);
	}

	private void getIqActionPerformed(ActionEvent evt) {
		OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, this, "Rewriting query") {

			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getRewritingRendering(query);
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				setSQLTranslation(sqlQuery);
				TextQueryResultsDialog dialog = new TextQueryResultsDialog(editorKit.getWorkspace(),
						"Intermediate Query",
						result,
						"Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
				dialog.setVisible(true);
			}
		});
	}

	private void getSqlActionPerformed(ActionEvent evt) {
		OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, this, "Rewriting query") {

			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				// TODO: should we show the SQL query only?
				return statement.getExecutableQuery(query).toString();
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				setSQLTranslation(sqlQuery);
				TextQueryResultsDialog dialog = new TextQueryResultsDialog(editorKit.getWorkspace(),
						"SQL Translation",
						result,
						"Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
				dialog.setVisible(true);
			}
		});
	}

	private void countResultsActionPerformed(ActionEvent evt) {
		OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(),
				(ontop, query) -> new OntopQuerySwingWorker<Long, Void>(
						ontop, query, getParent(), "Counting tuples") {

					@Override
					protected Long runQuery(OntopOWLStatement statement, String query) throws Exception {
						return statement.getTupleCount(query);
					}

					@Override
					protected void onCompletion(Long result, String sqlQuery) {
						showActionResult(
								"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) +
										" - Number of rows retrieved: " + result);
						setSQLTranslation(sqlQuery);
					}
				});
	}

}
