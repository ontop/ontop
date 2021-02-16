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
import it.unibz.inf.ontop.protege.gui.models.OWLResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDADataQueryAction;
import it.unibz.inf.ontop.protege.workers.ExportResultsToCSVSwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorker;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

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
			OntopQuerySwingWorker.getOntopAndExecute(editorKit, querypanel.getQuery(), (ontop, query) -> new OntopQuerySwingWorker<Long>(
					this.getParent(), ontop, "Counting tuples", query) {

				@Override
				protected Long runQuery(OntopOWLStatement statement, String query) throws Exception {
					return statement.getTupleCount(query);
				}

				@Override
				protected void onCompletion(Long result, String sqlQuery) {
					querypanel.updateStatus(result);
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
				this.getParent(), file, ((OWLResultSetTableModel) queryResultTable.getModel()));
		worker.execute();
	}


	public void setTableModel(TableModel tableModel) {
		SwingUtilities.invokeLater(() -> {
			queryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			ToolTipManager.sharedInstance().unregisterComponent(queryResultTable);
			ToolTipManager.sharedInstance().unregisterComponent(queryResultTable.getTableHeader());

			TableModel oldmodel = queryResultTable.getModel();
			if (oldmodel != null) {
				oldmodel.removeTableModelListener(queryResultTable);
			}
			queryResultTable.setModel(tableModel);

			addNotify();

			queryResultTable.invalidate();
			queryResultTable.repaint();
			exportButton.setEnabled(tableModel instanceof OWLResultSetTableModel);
		});

		// Write a hint in the comment panel for user information
		String msg = (querypanel.isFetchAllSelect() || querypanel.canGetMoreTuples())
			? "Try to continue scrolling down the table to retrieve more results."
        	: "--";
		commentLabel.setText(msg);
	}

	public void runAskQuery(String askQuery) {
		OntopQuerySwingWorker.getOntopAndExecute(editorKit, askQuery,
				(ontop, query) -> new OntopQuerySwingWorker<Boolean>(getParent(), ontop, "Execute ASK Query", query, querypanel.executeButton, stopButton, commentLabel) {
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


	public void setSQLTranslation(String sql){
		txtSqlTranslation.setText(sql);
	}
}
