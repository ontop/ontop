package it.unibz.inf.ontop.protege.mapping;

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

import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.protege.mapping.worker.ExecuteSQLQuerySwingWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

public class SQLQueryDialog extends JDialog {

	private static final long serialVersionUID = 7600557919206933923L;

	private static final int MAX_ROWS = 100;

	private final JTable sqlQueryResultTable;
	private final JTextPane sourceQueryTextPane;

	private final OBDADataSource datasource;

	OntopAbstractAction executeSqlQueryAction = new OntopAbstractAction(
			"Execute SQL Query",
			"execute.png",
			"Execute the SQL query",
			getKeyStrokeWithCtrlMask(VK_ENTER)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			executeSqlQuery();
		}
	};

	OntopAbstractAction closeDialogAction = new OntopAbstractAction(OK_BUTTON_TEXT, null, null,
			KeyStroke.getKeyStroke(VK_ESCAPE, 0)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispatchEvent(new WindowEvent(SQLQueryDialog.this, WindowEvent.WINDOW_CLOSING));
		}
	};

	public SQLQueryDialog(OBDADataSource datasource, String query) {
		this.datasource = datasource;

		setTitle("SQL Query Result");
		setModal(true);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel sourceQueryPanel = new JPanel(new GridBagLayout());

		sourceQueryPanel.add(new JLabel("Source (SQL Query):"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0,0,4,0), 0, 0));

		sourceQueryTextPane = new JTextPane();
		sourceQueryTextPane.setDocument(new SQLQueryStyledDocument());
		sourceQueryTextPane.setText(query);
		sourceQueryTextPane.setPreferredSize(new Dimension(650, 250));

		sourceQueryPanel.add(new JScrollPane(sourceQueryTextPane),
				new GridBagConstraints(0, 1, 2, 1, 1, 1,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(0,0,0,0), 0, 0));

		sourceQueryPanel.add(
				getButton(executeSqlQueryAction),
				new GridBagConstraints(1, 2, 1, 1, 0, 0,
						GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(4,0,4,0), 0, 0));

		sqlQueryResultTable = new JTable();
		sqlQueryResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		sourceQueryTextPane.setPreferredSize(new Dimension(650, 250));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				sourceQueryPanel,
				new JScrollPane(sqlQueryResultTable));
		splitPane.setResizeWeight(0.6);

		mainPanel.add(splitPane, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add(getButton(closeDialogAction));
		mainPanel.add(controlPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);

		setUpAccelerator(sourceQueryTextPane, executeSqlQueryAction);
		setUpAccelerator(mainPanel, executeSqlQueryAction);
		setUpAccelerator(mainPanel, closeDialogAction);

		setSize(700, 600);

		executeSqlQuery();
	}

	private void executeSqlQuery() {
		ExecuteSQLQuerySwingWorker worker = new ExecuteSQLQuerySwingWorker(
				this,
				datasource,
				sourceQueryTextPane.getText().trim(),
				MAX_ROWS,
				sqlQueryResultTable::setModel);
		worker.execute();
	}
}
