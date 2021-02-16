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

import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.QueryManager;
import it.unibz.inf.ontop.protege.gui.dialogs.SelectPrefixDialog;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorker;
import it.unibz.inf.ontop.protege.workers.OntopQuerySwingWorkerFactory;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.protege.editor.owl.OWLEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

/**
 * Creates a new panel to execute queries. Remember to execute the
 * setResultsPanel function to indicate where to display the results.
 */
public class QueryInterfacePanel extends JPanel {

	private static final long serialVersionUID = -5902798157183352944L;

	private Runnable executeSelectAction;
	private Runnable executeGraphQuery;
	private Runnable executeAsk;

	private final QueryManager queryManager;

	private String groupId = "", queryId = "";

	private final JCheckBox showAllCheckBox;
	private final JCheckBox showShortIriCheckBox;
	private final JTextPane queryTextPane;
	private final JFormattedTextField fetchSizeTextField;
	public final JButton executeButton;

	// TODO: move to the other panel
	private final JLabel executionInfoLabel;

	public QueryInterfacePanel(OWLEditorKit editorKit,
							   OBDAModelManager obdaModelManager,
							   OntopQuerySwingWorkerFactory<String, Void> retrieveUCQExpansionAction,
							   OntopQuerySwingWorkerFactory<String, Void> retrieveUCQUnfoldingAction) {

		this.queryManager = obdaModelManager.getQueryController();

		JPopupMenu sparqlPopupMenu = new JPopupMenu();
		JMenuItem getSPARQLExpansion = new JMenuItem("View Intermediate Query...");
		getSPARQLExpansion.addActionListener(evt ->
				OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), retrieveUCQExpansionAction));
		sparqlPopupMenu.add(getSPARQLExpansion);

		JMenuItem getSPARQLSQLExpansion = new JMenuItem("View SQL translation...");
		getSPARQLSQLExpansion.addActionListener(evt ->
				OntopQuerySwingWorker.getOntopAndExecute(editorKit, getQuery(), retrieveUCQUnfoldingAction));
		sparqlPopupMenu.add(getSPARQLSQLExpansion);

		setLayout(new BorderLayout());

		queryTextPane = new JTextPane();
		queryTextPane.setFont(new Font("Lucida Console", Font.PLAIN, 14)); // NOI18N
		queryTextPane.setComponentPopupMenu(sparqlPopupMenu);
		add(new JScrollPane(queryTextPane), BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		JPanel executionInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		executionInfoLabel = new JLabel();
		executionInfoPanel.add(executionInfoLabel);

		executionInfoPanel.add(new JLabel("Show"));

		fetchSizeTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fetchSizeTextField.setValue(100);
		fetchSizeTextField.setColumns(4);
		fetchSizeTextField.setHorizontalAlignment(JTextField.RIGHT);
		executionInfoPanel.add(fetchSizeTextField);

		executionInfoPanel.add(new JLabel("or"));

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
		executionInfoPanel.add(showAllCheckBox);

		showShortIriCheckBox = new JCheckBox("Use short IRIs");
		executionInfoPanel.add(showShortIriCheckBox);

		controlPanel.add(executionInfoPanel);

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
				cmdExecuteQueryActionPerformed(e);
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

				QueryManager.Query query = queryManager.getQuery(groupId, queryId);
				query.setQuery(queryTextPane.getText());
			}
		};

		executeButton = DialogUtils.getButton(executeAction);
		controlPanel.add(executeButton);

		add(controlPanel, BorderLayout.SOUTH);

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

		add(topControlPanel, BorderLayout.NORTH);


		InputMap inputMap = queryTextPane.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "execute");
		ActionMap actionMap = queryTextPane.getActionMap();
		actionMap.put("execute", executeAction);
	}

	private void cmdExecuteQueryActionPerformed(ActionEvent evt) {
		String queryString = queryTextPane.getText();
		if (queryString.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Query editor cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			SPARQLParser parser = new SPARQLParser();
			ParsedQuery parsedQuery = parser.parseQuery(queryString, "http://example.org");
			if (parsedQuery instanceof ParsedTupleQuery) {
				executeSelectAction.run();
			}
			else if (parsedQuery instanceof ParsedBooleanQuery) {
				executeAsk.run();
			}
			else if (parsedQuery instanceof ParsedGraphQuery) {
				executeGraphQuery.run();
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
	}

	public void setExecuteSelectAction(Runnable executeUCQAction) {
		this.executeSelectAction = executeUCQAction;
	}

	public void setExecuteGraphQueryAction(Runnable action) {
		this.executeGraphQuery = action;
	}

	public void setExecuteAskAction(Runnable action) {
		this.executeAsk = action;
	}


	public void showActionResult(String s) {
		executionInfoLabel.setText(s);
		executionInfoLabel.setOpaque(false);
	}

	public  void showBooleanActionResultInTextInfo(String title, boolean result) {
		executionInfoLabel.setBackground(result ? Color.GREEN : Color.RED);
		executionInfoLabel.setOpaque(true);
		executionInfoLabel.setText(title);
	}


	public boolean isShortIriSelected() {
		return showShortIriCheckBox.isSelected();
	}

	public boolean isFetchAllSelect() {
		return showAllCheckBox.isSelected();
	}

	public String getQuery() {
		return queryTextPane.getText();
	}

	public int getFetchSize() {
		return ((Number) fetchSizeTextField.getValue()).intValue();
	}
}
