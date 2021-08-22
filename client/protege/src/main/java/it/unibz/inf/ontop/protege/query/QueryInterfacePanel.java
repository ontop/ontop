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
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.query.worker.TurtleRendererForOWL;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.utils.OntopReasonerAction;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;
import it.unibz.inf.ontop.protege.query.worker.ExportResultsToCSVSwingWorker;
import it.unibz.inf.ontop.protege.utils.OntopQuerySwingWorker;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

/*
    TODO: apply shorten IRI on the fly to the result
    TODO: stop / ask if stopping is required when switching between queries
    TODO: parse SPARQL while editing and highlight error location
 */

public class QueryInterfacePanel extends JPanel implements QueryManagerPanelSelectionListener, OWLOntologyChangeListener {

	private static final long serialVersionUID = -5902798157183352944L;

	private final OWLEditorKit editorKit;
	private final OBDAModelManager obdaModelManager;

	private final QueryInterfaceLimitPanel limitPanel;
	private final JCheckBox showShortIriCheckBox;
	private final JTextPane queryTextPane;
	private final JButton executeButton;

	private final JTabbedPane resultTabbedPane;
	private final JLabel executionInfoLabel;
	private final JTable queryResultTable;
	private final JTextArea txtSqlTranslation;
	private final JButton stopButton;

	private QueryManager.Item query;

	public QueryInterfacePanel(OWLEditorKit editorKit) {
		this.editorKit = editorKit;
		this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

		OntopReasonerAction viewIqAction = new OntopReasonerAction(
				"View Intermediate Query...", null, null, null,
				editorKit, this::getViewIqWorker);

		OntopReasonerAction viewSqlAction = new OntopReasonerAction(
				"View SQL translation...", null, null, null,
				editorKit, this::getViewSqlWorker);

		JPopupMenu sparqlPopupMenu = new JPopupMenu();
		sparqlPopupMenu.add(getMenuItem(viewIqAction));
		sparqlPopupMenu.add(getMenuItem(viewSqlAction));

		JPanel queryPanel = new JPanel(new BorderLayout());
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

		OntopReasonerAction executeAction = new OntopReasonerAction(
				"Execute",
				"execute.png",
				"Execute the query and display the results",
				getKeyStrokeWithCtrlMask(VK_ENTER),
				editorKit,
				this::getExecuteWorker);

		stopButton = getButton(new OntopAbstractAction(
				"Stop",
				"stop.png",
				"Stop running the current query",
				null) {
			@Override public void actionPerformed(ActionEvent e) {  /* NO-OP */ }
		});
		stopButton.setEnabled(false);
		controlPanel.add(stopButton,
				new GridBagConstraints(4, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		executeButton = getButton(executeAction);
		controlPanel.add(executeButton,
				new GridBagConstraints(5, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));


		queryPanel.add(controlPanel, BorderLayout.SOUTH);

		JPanel topControlPanel = new JPanel();
		topControlPanel.setLayout(new BoxLayout(topControlPanel, BoxLayout.LINE_AXIS));
		topControlPanel.add(new JLabel("SPARQL Query"));
		topControlPanel.add(Box.createHorizontalGlue());
		topControlPanel.add(getButton(prefixesAction));
		queryPanel.add(topControlPanel, BorderLayout.NORTH);

		JPanel resultsPanel = new JPanel(new BorderLayout());

		executionInfoLabel = new JLabel("<html>&nbsp;</html>");
		executionInfoLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		resultsPanel.add(executionInfoLabel, BorderLayout.NORTH);

		resultTabbedPane = new JTabbedPane();
		resultTabbedPane.setMinimumSize(new Dimension(400, 250));

		JPanel sparqlResultPanel = new JPanel(new BorderLayout());
		resultTabbedPane.addTab("SPARQL results", sparqlResultPanel);

		queryResultTable = new JTable(new DefaultTableModel(new String[]{ "Results" }, 0));
		JScrollPane scrollPane = new JScrollPane(queryResultTable);
		sparqlResultPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel controlBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		exportAction.setEnabled(false);
		controlBottomPanel.add(getButton(exportAction));

		sparqlResultPanel.add(controlBottomPanel, BorderLayout.SOUTH);

		txtSqlTranslation = new JTextArea();
		resultTabbedPane.addTab("SQL translation", new JScrollPane(txtSqlTranslation));

		OntopReasonerAction countResultsAction = new OntopReasonerAction(
				"Count tuples", null, null, null,
				editorKit, this::getCountResultsWorker);

		JPopupMenu resultsPopupMenu = new JPopupMenu();
		resultsPopupMenu.add(getMenuItem(countResultsAction));

		resultsPanel.setComponentPopupMenu(resultsPopupMenu);
		queryResultTable.setComponentPopupMenu(resultsPopupMenu);
		resultTabbedPane.setComponentPopupMenu(resultsPopupMenu);
		scrollPane.setComponentPopupMenu(resultsPopupMenu);

		resultsPanel.add(resultTabbedPane, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queryPanel, resultsPanel);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);

		setUpAccelerator(queryTextPane, executeAction);
		setUpAccelerator(queryTextPane, prefixesAction);
		setUpAccelerator(queryResultTable, exportAction);
	}

	@Override
	public void selectionChanged(QueryManager.Item query) {
		if (this.query == query)
			return;

		this.query = query;
		queryTextPane.setText(query != null ? query.getQueryString() : "");
		resetTableModel(new String[0]);
		//executeButton.setEnabled(true);
		//stopButton.setEnabled(false);
		showStatus("<html>&nbsp</html>", "");
	}

	private void showStatus(String status, String sqlQuery) {
		executionInfoLabel.setText(status);
		executionInfoLabel.setOpaque(false);
		txtSqlTranslation.setText(sqlQuery);
		exportAction.setEnabled(queryResultTable.getRowCount() > 0);
	}

	@Override // see QueryManagerView
	public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
		resetTableModel(new String[0]);
	}

	private final OntopAbstractAction prefixesAction = new OntopAbstractAction(
			"Prefixes...",
			"attach.png",
			"Select prefixes to insert into the query",
			getKeyStrokeWithCtrlMask(VK_P)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			SelectPrefixesDialog dialog = new SelectPrefixesDialog(obdaModelManager.getCurrentOBDAModel().getMutablePrefixManager(), queryTextPane.getText());
			setLocationRelativeToProtegeAndOpen(editorKit, dialog);
			dialog.getPrefixDirectives()
					.ifPresent(s -> queryTextPane.setText(s + "\n" + queryTextPane.getText()));
		}
	};

	private final OntopAbstractAction exportAction = new OntopAbstractAction(
			"Export to CSV...",
			"export.png",
			"Export the results to a CSV file",
			getKeyStrokeWithCtrlMask(VK_T)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = DialogUtils.getFileChooser(editorKit,
					DialogUtils.getExtensionReplacer(".csv"));

			if (fileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;

			File file = fileChooser.getSelectedFile();
			if (!DialogUtils.confirmCanWrite(file, null, "Export to CSV"))
				return;

			ExportResultsToCSVSwingWorker worker = new ExportResultsToCSVSwingWorker(
					QueryInterfacePanel.this, file, (DefaultTableModel) queryResultTable.getModel());
			worker.execute();
		}
	};


	private OntopQuerySwingWorker<?, ?> getExecuteWorker(OntopProtegeReasoner ontop) {
		String query = queryTextPane.getText();
		if (query.isEmpty()) {
			showPrettyMessageDialog(this, "Query editor cannot be empty.", "Error");
			return null;
		}
		try {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery parsedQuery = parser.parseQuery(query, "http://example.org");
			if (parsedQuery instanceof ParsedTupleQuery)
				return getSelectQueryWorker(ontop);

			if (parsedQuery instanceof ParsedGraphQuery)
				return getGraphQueryWorker(ontop);

			if (parsedQuery instanceof ParsedBooleanQuery)
				return getAskQueryWorker(ontop);

			showPrettyMessageDialog(this, "This type of SPARQL expression is not handled. Please use SELECT, ASK, DESCRIBE, or CONSTRUCT.", "Error");
		}
		catch (Exception e) {
			showPrettyMessageDialog(this, "Error parsing SPARQL query: " + e.getMessage(), "Error");
		}
		return null;
	}

	private OntopQuerySwingWorker<Void, String[]> getSelectQueryWorker(OntopProtegeReasoner ontop) {
		DefaultTableModel tableModel = resetTableModel(new String[0]);

		return new OntopQuerySwingWorker<Void, String[]>(ontop, queryTextPane.getText(), getParent(),
				"Execute SELECT Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				TurtleRendererForOWL renderer = setFetchSizeAndGetTurtleRenderer(statement);
				try (TupleOWLResultSet rs = statement.executeSelectQuery(query)) {
					String[] signature = rs.getSignature().toArray(new String[0]);
					SwingUtilities.invokeLater(() -> {
						tableModel.setColumnIdentifiers(signature);
						queryResultTable.revalidate();
					});
					while (rs.hasNext()) {
						publish(renderer.render(rs.next(), signature));
						tick();
					}
				}
				return null;
			}

			@Override
			protected void process(java.util.List<String[]> chunks) {
				chunks.forEach(tableModel::addRow);
			}

			@Override
			protected void onCompletion(Void result, String sqlQuery) {
				showStatus(formatStatus(elapsedTimeMillis(), "Solution mappings returned: <b>" + getCount() + "</b>."), sqlQuery);
			}
		};
	}

	private OntopQuerySwingWorker<Void, String> getGraphQueryWorker(OntopProtegeReasoner ontop) {
		DefaultTableModel tableModel = resetTableModel(new String[] { "RDF triples" });
		return new OntopQuerySwingWorker<Void, String>(ontop, queryTextPane.getText(), getParent(),
				"Execute CONSTRUCT/DESCRIBE Query", executeButton, stopButton, executionInfoLabel) {

			@Override
			protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
				TurtleRendererForOWL renderer = setFetchSizeAndGetTurtleRenderer(statement);
				publish(renderer.renderPrefixMap());
				publish("");
				try (GraphOWLResultSet rs = statement.executeGraphQuery(query)) {
					while (rs.hasNext()) {
						renderer.render(rs.next())
								.ifPresent(this::publish);
						tick();
					}
				}
				return null;
			}

			@Override
			protected void process(java.util.List<String> chunks) {
				chunks.forEach(c -> tableModel.addRow(new String[] { c }));
			}

			@Override
			protected void onCompletion(Void result, String sqlQuery) {
				showStatus(formatStatus(elapsedTimeMillis(), "OWL axioms produced: <b>" + getCount() + "</b>."), sqlQuery);
			}
		};
	}

	private OntopQuerySwingWorker<Boolean, Void> getAskQueryWorker(OntopProtegeReasoner ontop) {
		resetTableModel(new String[0]);

		return new OntopQuerySwingWorker<Boolean, Void>(ontop, queryTextPane.getText(), getParent(),
						"Execute ASK Query", executeButton, stopButton, executionInfoLabel) {
			@Override
			protected Boolean runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.executeAskQuery(query).getValue();
			}

			@Override
			protected void onCompletion(Boolean result, String sqlQuery) {
				showStatus(formatStatus(elapsedTimeMillis(), "Result: <b>" + result + "</b>."), sqlQuery);
				executionInfoLabel.setBackground(result ? Color.GREEN : Color.RED);
				executionInfoLabel.setOpaque(true);
			}
		};
	}

	private DefaultTableModel resetTableModel(String[] columnNames) {
		queryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(columnNames);
		queryResultTable.setModel(tableModel);
		queryResultTable.invalidate();
		queryResultTable.repaint();

		exportAction.setEnabled(false);
		return tableModel;
	}

	private TurtleRendererForOWL setFetchSizeAndGetTurtleRenderer(OntopOWLStatement statement) throws OntopOWLException {
		if (!limitPanel.isFetchAllSelected()) {
			DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) statement;
			defaultOntopOWLStatement.setMaxRows(limitPanel.getFetchSize());
		}
		return new TurtleRendererForOWL(
				obdaModelManager.getCurrentOBDAModel().getMutablePrefixManager(),
				showShortIriCheckBox.isSelected());
	}

	private static String formatStatus(long time, String message) {
		return"<html>Execution time: <b>" + DialogUtils.renderElapsedTime(time) + "</b>." +
				" " + message + "</html>";
	}

	private OntopQuerySwingWorker<String, Void> getViewIqWorker(OntopProtegeReasoner ontop) {
		return new OntopQuerySwingWorker<String, Void>(ontop, queryTextPane.getText(), getParent(), "Rewriting query") {
			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getRewritingRendering(query);
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				txtSqlTranslation.setText(sqlQuery);
				QueryResultsSimpleDialog dialog = new QueryResultsSimpleDialog(
						"Intermediate Query",
						result,
						"Processing time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()));
				setLocationRelativeToProtegeAndOpen(editorKit, dialog);
			}
		};
	}

	private OntopQuerySwingWorker<String, Void> getViewSqlWorker(OntopProtegeReasoner ontop) {
		return new OntopQuerySwingWorker<String, Void>(ontop, queryTextPane.getText(), getParent(), "Rewriting query") {

			@Override
			protected String runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getExecutableQuery(query).toString();
			}

			@Override
			protected void onCompletion(String result, String sqlQuery) {
				resultTabbedPane.setSelectedIndex(1);
				showStatus(formatStatus(elapsedTimeMillis(), "Translated into SQL."), sqlQuery);
			}
		};
	}

	private OntopQuerySwingWorker<Long, Void> getCountResultsWorker(OntopProtegeReasoner ontop) {
		return new OntopQuerySwingWorker<Long, Void>(ontop, queryTextPane.getText(), getParent(), "Counting results") {

			@Override
			protected Long runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.getTupleCount(query);
			}

			@Override
			protected void onCompletion(Long result, String sqlQuery) {
				showStatus(formatStatus(elapsedTimeMillis(),"The number of results: <b>" + result + "</b>."), sqlQuery);
			}
		};
	}
}
