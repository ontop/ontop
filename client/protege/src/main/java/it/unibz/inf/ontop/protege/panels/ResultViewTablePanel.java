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

import it.unibz.inf.ontop.protege.gui.models.OWLResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDADataQueryAction;
import it.unibz.inf.ontop.protege.workers.ExportResultsToCSVSwingWorker;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ResultViewTablePanel extends JPanel {

	private static final long serialVersionUID = -8494558136315031084L;

	private OBDADataQueryAction<Long> countAllTuplesAction;
	private final QueryInterfacePanel querypanel;

	private final JLabel commentLabel;
	private final JTable queryResultTable;
	private final JTextArea txtSqlTranslation;
	private final JButton exportButton;

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

		JPanel controlPanel = new JPanel(new BorderLayout(0, 5));

		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 5));
		statusPanel.setPreferredSize(new Dimension(64, 36));

		JLabel hintLabel = new JLabel("Hint:");
		hintLabel.setFont(new Font("Tahoma", Font.BOLD, 11)); // NOI18N
		statusPanel.add(hintLabel);

		commentLabel = new JLabel("--");
		statusPanel.add(commentLabel);

		controlPanel.add(statusPanel, BorderLayout.CENTER);

		exportButton = DialogUtils.getButton(
				"Export to CSV...",
				"export.png",
				"Export the results to a CSV file",
				this::cmdExportResultActionPerformed);
		exportButton.setEnabled(false);
		controlPanel.add(exportButton, BorderLayout.EAST);

		sparqlResultPanel.add(controlPanel, BorderLayout.SOUTH);

		resultTabbedPanel.addTab("SPARQL results", sparqlResultPanel);

		txtSqlTranslation = new JTextArea();
		resultTabbedPanel.addTab("SQL Translation", new JScrollPane(txtSqlTranslation));

		add(resultTabbedPanel, BorderLayout.CENTER);

		JPopupMenu menu = new JPopupMenu();
		JMenuItem countAll = new JMenuItem("count all tuples");
		countAll.addActionListener(e -> {
			Thread thread = new Thread(() -> {
				String query = querypanel.getQuery();
				countAllTuplesAction.run(query);
			});
			thread.start();
		});
		menu.add(countAll);
		queryResultTable.setComponentPopupMenu(menu);
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


	public void setCountAllTuplesActionForUCQ(OBDADataQueryAction<Long> countAllTuples) {
		this.countAllTuplesAction = countAllTuples;
	}


	public void setSQLTranslation(String sql){
		txtSqlTranslation.setText(sql);
	}
}
