package it.unibz.inf.ontop.protege.query;

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
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;
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
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.getKeyStrokeWithCtrlMask;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_P;

public class QueryInterfacePanel extends JPanel implements QueryManagerPanelSelectionListener, OWLOntologyChangeListener {

	private static final long serialVersionUID = -5902798157183352944L;

	private final OBDAModelManager obdaModelManager;

	private QueryManager.Item query;

	private final QueryInterfaceLimitPanel limitPanel;
	private final JCheckBox showShortIriCheckBox;
	private final JTextPane queryTextPane;
	private final JButton executeButton;

	private final JTabbedPane resultTabbedPane;
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
		JMenuItem getIqMenuItem = DialogUtils.getMenuItem("View Intermediate Query...",
				evt -> getOntopAndExecute(getShowIqExecutor()));
		sparqlPopupMenu.add(getIqMenuItem);

		JMenuItem getSqlMenuItem = DialogUtils.getMenuItem("View SQL translation...",
				evt -> getOntopAndExecute(getShowSqlExecutor()));
		sparqlPopupMenu.add(getSqlMenuItem);

		JPanel queryPanel = new JPanel(new BorderLayout());
		//queryPanel.setPreferredSize(new Dimension(400, 250));
		queryPanel.setMinimumSize(new Dimension(400, 250));

		queryTextPane = new JTextPane();
		queryTextPane.setFont(new Font("Lucida Console", Font.PLAIN, 14)); // NOI18N
		queryTextPane.setComponentPopupMenu(sparqlPopupMenu);
		queryTextPane.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
			if (query != null)
				query.setQueryString(queryTextPane.getText());
		});
		queryPanel.add(new JScrollPane(queryTextPane), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new GridBagLayout());

		limitPanel = new QueryInterfaceLimitPanel();
		limitPanel.setBorder(BorderFactory.createEtchedBorder());
		controlPanel.add(limitPanel,
				new GridBagConstraints(0, 0, 1, 1, 0, 1,
						GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
						new Insets(3, 0, 3, 10), 0, 0));

		JPanel showShortIriPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		showShortIriPanel.setBorder(BorderFactory.createEtchedBorder());
		showShortIriCheckBox = new JCheckBox("Use short IRIs");
		showShortIriPanel.add(showShortIriCheckBox);
		controlPanel.add(showShortIriPanel,
				new GridBagConstraints(2, 0, 1, 1, 0, 1,
						GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
						new Insets(3, 10, 3, 0), 0, 0));

		controlPanel.add(new JPanel(), // gobbler
				new GridBagConstraints(3, 0, 1, 1, 1, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));

		OntopAbstractAction prefixesAction = new OntopAbstractAction(
				"Prefixes...",
				"attach.png",
				"Select prefixes to insert into the query",
				getKeyStrokeWithCtrlMask(VK_P)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectPrefixesDialog dialog = new SelectPrefixesDialog(obdaModelManager.getTriplesMapCollection().getMutablePrefixManager(), queryTextPane.getText());
				dialog.setLocationRelativeTo(QueryInterfacePanel.this);
				dialog.setVisible(true);
				dialog.getPrefixDirectives()
						.ifPresent(s -> queryTextPane.setText(s + "\n" + queryTextPane.getText()));
			}
		};

		OntopAbstractAction executeAction = new OntopAbstractAction(
				"Execute",
				"execute.png",
				"Execute the query and display the results",
				getKeyStrokeWithCtrlMask(VK_ENTER)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeActionPerformed(e);
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

		JPanel topControlPanel = new JPanel();
		topControlPanel.setLayout(new BoxLayout(topControlPanel, BoxLayout.LINE_AXIS));
		topControlPanel.add(new JLabel("SPARQL Query"));
		topControlPanel.add(Box.createHorizontalGlue());
		JButton prefixesButton = DialogUtils.getButton(prefixesAction);
		topControlPanel.add(prefixesButton);
		queryPanel.add(topControlPanel, BorderLayout.NORTH);

		JPanel resultsPanel = new JPanel(new BorderLayout());

		executionInfoLabel = new JLabel("<html>&nbsp;</html>");
		executionInfoLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		resultsPanel.add(executionInfoLabel, BorderLayout.NORTH);

		resultTabbedPane = new JTabbedPane();
		resultTabbedPane.setMinimumSize(new Dimension(400, 250));
		//resultTabbedPane.setPreferredSize(new Dimension(400, 250));

		JPanel sparqlResultPanel = new JPanel(new BorderLayout());
		resultTabbedPane.addTab("SPARQL results", sparqlResultPanel);

		queryResultTable = new JTable(new DefaultTableModel(new String[]{ "Results" }, 0));
		JScrollPane scrollPane = new JScrollPane(queryResultTable);
		sparqlResultPanel.add(scrollPane, BorderLayout.CENTER);

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
		countResultsMenuItem.addActionListener(evt -> getOntopAndExecute(getCountResultsExecutor()));
		menu.add(countResultsMenuItem);
		resultsPanel.setComponentPopupMenu(menu);
		queryResultTable.setComponentPopupMenu(menu);
		resultTabbedPane.setComponentPopupMenu(menu);
		scrollPane.setComponentPopupMenu(menu);

		resultsPanel.add(resultTabbedPane, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel, resultsPanel);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);

		setUpAccelerator(queryTextPane, executeAction);
		setUpAccelerator(queryTextPane, prefixesAction);
	}

	private void executeActionPerformed(ActionEvent evt) {
		String query = queryTextPane.getText();
		if (query.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Query editor cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery parsedQuery = parser.parseQuery(query, "http://example.org");
			if (parsedQuery instanceof ParsedTupleQuery) {
				getOntopAndExecute(getSelectQueryExecutor());
			}
			else if (parsedQuery instanceof ParsedBooleanQuery) {
				getOntopAndExecute(getAskQueryExecutor());
			}
			else if (parsedQuery instanceof ParsedGraphQuery) {
				getOntopAndExecute(getGraphQueryExecutor());
			}
			else {
				JOptionPane.showMessageDialog(this, "This type of SPARQL expression is not handled. Please use SELECT, ASK, DESCRIBE, or CONSTRUCT.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error parsing SPARQL query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void selectionChanged(QueryManager.Item query) {
		if (this.query == query)
			return;

		this.query = query;
		queryTextPane.setText(query != null ? query.getQueryString() : "");
		resetTableModel(new String[0]);
		txtSqlTranslation.setText("");
		//executeButton.setEnabled(true);
		//stopButton.setEnabled(false);
		executionInfoLabel.setText("<html>&nbsp</html>");
		executionInfoLabel.setOpaque(false);
	}

	@Override
	public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
		resetTableModel(new String[0]);
	}

	private void showActionResult(long time, String second) {
		executionInfoLabel.setText("<html>Execution time: <b>" + DialogUtils.renderElapsedTime(time) + "</b>. " + second + "</html>");
		executionInfoLabel.setOpaque(false);
	}

	private DefaultTableModel resetTableModel(String[] columnNames) {
		queryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(columnNames);
		queryResultTable.setModel(tableModel);
		queryResultTable.invalidate();
		queryResultTable.repaint();

		exportButton.setEnabled(false);
		return tableModel;
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


	private OntopQuerySwingWorkerFactory<Boolean, Void> getAskQueryExecutor() {
		resetTableModel(new String[0]);

		return (ontop, query) -> new OntopQuerySwingWorker<Boolean, Void>(ontop, query, getParent(),
						"Execute ASK Query", executeButton, stopButton, executionInfoLabel) {
			@Override
			protected Boolean runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.executeAskQuery(query).getValue();
			}

			@Override
			protected void onCompletion(Boolean result, String sqlQuery) {
				showActionResult(elapsedTimeMillis(), "Result: <b>" + result + "</b>.");
				executionInfoLabel.setBackground(result ? Color.GREEN : Color.RED);
				executionInfoLabel.setOpaque(true);
				setSQLTranslation(sqlQuery);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<Void, String> getGraphQueryExecutor() {
		DefaultTableModel tableModel = resetTableModel(new String[] {"RDF triples"});
		return (ontop, query) -> new OntopQuerySwingWorker<Void, String>(ontop, query, getParent(),
						"Execute CONSTRUCT/DESCRIBE Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				TurtleRendererForOWL owlTranslator = getOWL2TurtleTranslator();
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
				showActionResult(elapsedTimeMillis(), "OWL axioms produced: <b>" + getCount() + "</b>.");
				setSQLTranslation(sqlQuery);
				exportButton.setEnabled(tableModel.getRowCount() > 0);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<Void, String[]> getSelectQueryExecutor() {
		DefaultTableModel tableModel = resetTableModel(new String[0]);
		return (ontop, query) -> new OntopQuerySwingWorker<Void, String[]>(ontop, query, getParent(),
						"Execute SELECT Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				TurtleRendererForOWL owlTranslator = getOWL2TurtleTranslator();
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
				showActionResult(elapsedTimeMillis(), "Solution mappings returned: <b>" + getCount() + "</b>.");
				setSQLTranslation(sqlQuery);
				exportButton.setEnabled(tableModel.getRowCount() > 0);
			}
		};
	}

	private void setFetchSize(OntopOWLStatement statement) throws OntopOWLException {
		if (!limitPanel.isFetchAllSelected()) {
			DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) statement;
			defaultOntopOWLStatement.setMaxRows(limitPanel.getFetchSize());
		}
	}

	private TurtleRendererForOWL getOWL2TurtleTranslator() {
		return new TurtleRendererForOWL(
				obdaModelManager.getTriplesMapCollection().getMutablePrefixManager(),
				showShortIriCheckBox.isSelected());
	}

	public void setSQLTranslation(String sql) {
		txtSqlTranslation.setText(sql);
	}

	private void getOntopAndExecute(OntopQuerySwingWorkerFactory<?, ?> executor) {
		Optional<OntopProtegeReasoner> ontop = DialogUtils.getOntopProtegeReasoner(editorKit);
		if (!ontop.isPresent())
			return;

		OntopQuerySwingWorker<?, ?> worker = executor.apply(ontop.get(), queryTextPane.getText());
		worker.execute();
	}

	private OntopQuerySwingWorkerFactory<String, Void> getShowIqExecutor() {
		return (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, getParent(), "Rewriting query") {
			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getRewritingRendering(query);
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				setSQLTranslation(sqlQuery);
				QueryResultsSimpleDialog dialog = new QueryResultsSimpleDialog(editorKit.getWorkspace(),
						"Intermediate Query",
						result,
						"Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
				dialog.setVisible(true);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<String, Void> getShowSqlExecutor() {
		return (ontop, query) -> new OntopQuerySwingWorker<String, Void>(ontop, query, getParent(), "Rewriting query") {

			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getExecutableQuery(query).toString();
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				setSQLTranslation(sqlQuery);
				resultTabbedPane.setSelectedIndex(1);
				showActionResult(elapsedTimeMillis(), "Translated into SQL.");
//				QueryResultsSimpleDialog dialog = new QueryResultsSimpleDialog(editorKit.getWorkspace(),
//						"SQL Translation",
//						result,
//						"Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
//				dialog.setVisible(true);
			}
		};
	}

	private OntopQuerySwingWorkerFactory<Long, Void> getCountResultsExecutor() {
		return (ontop, query) -> new OntopQuerySwingWorker<Long, Void>(ontop, query, getParent(), "Counting results") {

			@Override
			protected Long runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getTupleCount(query);
			}

			@Override
			protected void onCompletion(Long result, String sqlQuery) {
				showActionResult(elapsedTimeMillis(),"The number of results: <b>" + result + "</b>.");
				setSQLTranslation(sqlQuery);
			}
		};
	}
}
