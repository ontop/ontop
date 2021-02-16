package it.unibz.inf.ontop.protege.panels;

/*
 * #%L
 * ontop-protege4
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
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.MutablePrefixManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.views.OWLAxiomToTurtleTranslator;
import it.unibz.inf.ontop.protege.workers.ExportResultsToCSVSwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorker;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class ResultViewTablePanel extends JPanel {

	private static final long serialVersionUID = -8494558136315031084L;

	private final QueryInterfacePanel querypanel;

	private final JLabel commentLabel;
	private final JTable queryResultTable;
	private final JTextArea txtSqlTranslation;
	private final JButton exportButton;
	private final JButton stopButton;

	private final OWLEditorKit editorKit;

	public ResultViewTablePanel(OWLEditorKit editorKit, QueryInterfacePanel panel) {
		this.editorKit = editorKit;
		querypanel = panel;

		setMinimumSize(new Dimension(400, 250));
		setPreferredSize(new Dimension(400, 250));
		setLayout(new BorderLayout(0, 5));

		JTabbedPane resultTabbedPanel = new JTabbedPane();

		JPanel sparqlResultPanel = new JPanel(new BorderLayout());

		queryResultTable = new JTable(new DefaultTableModel(
            new Object [][] {},
            new String [] {"Results"} ));

		sparqlResultPanel.add(new JScrollPane(queryResultTable), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new GridBagLayout());

		JLabel hintLabel = new JLabel("Hint:");
		hintLabel.setFont(new Font("Tahoma", Font.BOLD, 11)); // NOI18N
		controlPanel.add(hintLabel,
				new GridBagConstraints(0, 0, 1, 1, 1, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));

		commentLabel = new JLabel("--");
		controlPanel.add(commentLabel,
				new GridBagConstraints(0, 1, 1, 1, 1, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));


		stopButton = DialogUtils.getButton(
				"Stop",
				"stop.png",
				"Stop running the current query",
				null);
		stopButton.setEnabled(false);
		controlPanel.add(stopButton,
				new GridBagConstraints(1, 0, 1, 2, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));


		exportButton = DialogUtils.getButton(
				"Export to CSV...",
				"export.png",
				"Export the results to a CSV file",
				this::cmdExportResultActionPerformed);
		exportButton.setEnabled(false);
		controlPanel.add(exportButton,
				new GridBagConstraints(2, 0, 1, 2, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));


		sparqlResultPanel.add(controlPanel, BorderLayout.SOUTH);

		resultTabbedPanel.addTab("SPARQL results", sparqlResultPanel);

		txtSqlTranslation = new JTextArea();
		resultTabbedPanel.addTab("SQL Translation", new JScrollPane(txtSqlTranslation));

		add(resultTabbedPanel, BorderLayout.CENTER);

		JPopupMenu menu = new JPopupMenu();
		JMenuItem countAll = new JMenuItem("Count tuples");
		countAll.addActionListener(e -> {
			OntopQuerySwingWorker.getOntopAndExecute(editorKit, querypanel.getQuery(),
					(ontop, query) -> new OntopQuerySwingWorker<Long, Void>(
								ontop, query, getParent(), "Counting tuples") {

				@Override
				protected Long runQuery(OntopOWLStatement statement, String query) throws Exception {
					return statement.getTupleCount(query);
				}

				@Override
				protected void onCompletion(Long result, String sqlQuery) {
					querypanel.showActionResult(
							"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) +
							" - Number of rows retrieved: " + result);
					setSQLTranslation(sqlQuery);
				}
			});
		});
		menu.add(countAll);
		sparqlResultPanel.setComponentPopupMenu(menu);
	}

	// TODO: remove .getParent thrice
	private void cmdExportResultActionPerformed(ActionEvent evt) {
		JFileChooser fileChooser = DialogUtils.getFileChooser(editorKit,
				DialogUtils.getExtensionReplacer(".csv"));

		if (fileChooser.showSaveDialog(this.getParent()) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fileChooser.getSelectedFile();
		if (!DialogUtils.confirmCanWrite(file, this.getParent(), "Export to CSV"))
			return;

		ExportResultsToCSVSwingWorker worker = new ExportResultsToCSVSwingWorker(
				this.getParent(), file, (DefaultTableModel) queryResultTable.getModel());
		worker.execute();
	}


	public void setTableModel(TableModel tableModel) {
		queryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		queryResultTable.setModel(tableModel);
		queryResultTable.invalidate();
		queryResultTable.repaint();

		exportButton.setEnabled(false);
	}

	public void runAskQuery(String askQuery) {
		setTableModel(new DefaultTableModel());

		OntopQuerySwingWorker.getOntopAndExecute(editorKit, askQuery,
				(ontop, query) -> new OntopQuerySwingWorker<Boolean, Void>(ontop, query, getParent(),
						"Execute ASK Query", querypanel.executeButton, stopButton, commentLabel) {
			@Override
			protected Boolean runQuery(OntopOWLStatement statement, String query) throws Exception {
				return statement.executeAskQuery(query).getValue();
			}

			@Override
			protected void onCompletion(Boolean result, String sqlQuery) {
				querypanel.showBooleanActionResultInTextInfo(
						"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ". " +
								"Result: " + result,
						result);
				setSQLTranslation(sqlQuery);
			}
		});
	}

	public void runGraphQuery(OBDAModelManager obdaModelManager, String graphQuery) {
		boolean fetchAll = querypanel.isFetchAllSelect();
		int fetchSize = querypanel.getFetchSize();
		boolean shortIris = querypanel.isShortIriSelected();
		MutablePrefixManager prefixManager = obdaModelManager.getTriplesMapCollection().getMutablePrefixManager();

		DefaultTableModel tableModel = DialogUtils.createNonEditableTableModel(new String[] {"RDF triples"});
		setTableModel(tableModel);

		OntopQuerySwingWorker.getOntopAndExecute(editorKit, graphQuery,
				(ontop, query) -> new OntopQuerySwingWorker<Void, String>(ontop, query, getParent(),
						"Execute CONSTRUCT/DESCRIBE Query", querypanel.executeButton, stopButton, commentLabel) {

					@Override
					protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
						OWLAxiomToTurtleTranslator owlTranslator = new OWLAxiomToTurtleTranslator(prefixManager, shortIris);
						prefixManager.getPrefixMap().entrySet().stream()
								.map(e -> "@prefix " + e.getKey() + " " + e.getValue() + ".\n")
								.forEach(this::publish);
						publish("\n");

						if (!fetchAll) {
							DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) statement;
							defaultOntopOWLStatement.setMaxRows(fetchSize);
						}
						try (GraphOWLResultSet rs = statement.executeGraphQuery(query)) {
							if (rs != null)
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
						querypanel.showActionResult(
								"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ".\n" +
										"OWL Axioms produced: " + getCount());
						setSQLTranslation(sqlQuery);
						exportButton.setEnabled(tableModel.getRowCount() > 0);
					}
				});

	}

	public void runSelectQuery(OBDAModelManager obdaModelManager, String selectQuery) {
		boolean fetchAll = querypanel.isFetchAllSelect();
		int fetchSize = querypanel.getFetchSize();
		boolean shortIris = querypanel.isShortIriSelected();
		MutablePrefixManager prefixManager = obdaModelManager.getTriplesMapCollection().getMutablePrefixManager();

		DefaultTableModel tableModel = new DefaultTableModel();
		setTableModel(tableModel);

		OntopQuerySwingWorker.getOntopAndExecute(editorKit, selectQuery,
				(ontop, query) -> new OntopQuerySwingWorker<Void, String[]>(ontop, query, getParent(),
						"Execute SELECT Query", querypanel.executeButton, stopButton, commentLabel) {

					@Override
					protected Void runQuery(OntopOWLStatement statement, String query) throws Exception {
						OWLAxiomToTurtleTranslator owlTranslator = new OWLAxiomToTurtleTranslator(prefixManager, shortIris);
						if (!fetchAll) {
							DefaultOntopOWLStatement defaultOntopOWLStatement = (DefaultOntopOWLStatement) statement;
							defaultOntopOWLStatement.setMaxRows(fetchSize);
						}
						try (TupleOWLResultSet rs = statement.executeSelectQuery(query)) {
							if (rs != null) {
								List<String> signature = rs.getSignature();
								SwingUtilities.invokeLater(() -> {
										tableModel.setColumnIdentifiers(signature.toArray());
										queryResultTable.revalidate();
								});
								int columns = signature.size();
								while (rs.hasNext()) {
									String[] row = new String[columns];
									OWLBindingSet bindingSet = rs.next();
									for (int j = 0; j < columns; j++) {
										String variableName = signature.get(j);
										OWLPropertyAssertionObject constant = bindingSet.getOWLPropertyAssertionObject(variableName);
										row[j] = owlTranslator.render(constant);
									}
									publish(row);
									tick();
								}
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
						querypanel.showActionResult(
								"Execution time: " + DialogUtils.renderElapsedTime(elapsedTimeMillis()) + ".\n" +
										"Solution mappings returned: " + getCount());
						setSQLTranslation(sqlQuery);
						exportButton.setEnabled(tableModel.getRowCount() > 0);
					}
				});

	}



	public void setSQLTranslation(String sql){
		txtSqlTranslation.setText(sql);
	}
}
