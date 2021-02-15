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

import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.QueryManager;
import it.unibz.inf.ontop.protege.gui.dialogs.SelectPrefixDialog;
import it.unibz.inf.ontop.protege.utils.OBDADataQueryAction;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

/**
 * Creates a new panel to execute queries. Remember to execute the
 * setResultsPanel function to indicate where to display the results.
 */
public class QueryInterfacePanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = -5902798157183352944L;

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterfacePanel.class);

	private OBDADataQueryAction<TupleOWLResultSet> executeSelectAction;
	private OBDADataQueryAction<BooleanOWLResultSet> executeAskAction;
	private OBDADataQueryAction<?> executeGraphQueryAction;
	private OBDADataQueryAction<String> retrieveUCQExpansionAction;
	private OBDADataQueryAction<String> retrieveUCQUnfoldingAction;

	private final QueryManager queryManager;

	private double execTime = 0;

	private String groupId = "", queryId = "";

	private final JCheckBox showAllCheckBox;
	private final JCheckBox showShortIriCheckBox;
	private final JTextPane queryTextPane;
	private final JFormattedTextField fetchSizeTextField;

	// TODO: move to the other panel
	private final JLabel executionInfoLabel;


	public QueryInterfacePanel(OBDAModelManager obdaModelManager) {
		this.queryManager = obdaModelManager.getQueryController();


		JPopupMenu sparqlPopupMenu = new JPopupMenu();
		JMenuItem getSPARQLExpansion = new JMenuItem("View Intermediate Query...");
		getSPARQLExpansion.addActionListener(this::getSPARQLExpansionActionPerformed);
		sparqlPopupMenu.add(getSPARQLExpansion);

		JMenuItem getSPARQLSQLExpansion = new JMenuItem("View SQL translation...");
		getSPARQLSQLExpansion.addActionListener(this::getSPARQLSQLExpansionActionPerformed);
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

		JButton executeButton = DialogUtils.getButton(executeAction);
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

	private void getSPARQLExpansionActionPerformed(ActionEvent evt) {
		Thread queryRunnerThread = new Thread(() ->
				retrieveUCQExpansionAction.run(queryTextPane.getText()));
		queryRunnerThread.start();
	}

	private void getSPARQLSQLExpansionActionPerformed(ActionEvent evt) {
		Thread queryRunnerThread = new Thread(() ->
				retrieveUCQUnfoldingAction.run(queryTextPane.getText()));
		queryRunnerThread.start();
	}

	private void cmdExecuteQueryActionPerformed(ActionEvent evt) {
		try {
			// TODO Handle this such that there is a listener checking the progress of the execution
			Thread queryRunnerThread = new Thread(() -> {
				String queryString = queryTextPane.getText();
				if (queryString.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Query editor cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					OBDADataQueryAction<?> action;
					SPARQLParser parser = new SPARQLParser();
					ParsedQuery parsedQuery = parser.parseQuery(queryString, "http://example.org");
					if (parsedQuery instanceof ParsedTupleQuery) {
						action = executeSelectAction;
					}
					else if (parsedQuery instanceof ParsedBooleanQuery) {
						action = executeAskAction;
					}
					else if (parsedQuery instanceof ParsedGraphQuery) {
						action = executeGraphQueryAction;
					}
					else {
						JOptionPane.showMessageDialog(this, "This type of SPARQL expression is not handled. Please use SELECT, ASK, DESCRIBE, or CONSTRUCT.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					action.run(queryString);
					execTime = action.getExecutionTime();
					do {
						int rows = action.getNumberOfRows();
						updateStatus(rows);
						try {
							Thread.sleep(100);
						}
						catch (InterruptedException e) {
							break;
						}
					} while (action.isRunning());
					int rows = action.getNumberOfRows();
					updateStatus(rows);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Error parsing SPARQL query: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
            });
			queryRunnerThread.start();
		}
		catch (Exception e) {
			DialogUtils.showSeeLogErrorDialog(this, "", LOGGER, e);
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

	public void setExecuteSelectAction(OBDADataQueryAction<TupleOWLResultSet> executeUCQAction) {
		this.executeSelectAction = executeUCQAction;
	}

	public void setExecuteAskAction(OBDADataQueryAction<BooleanOWLResultSet> executeUCQAction) {
		this.executeAskAction = executeUCQAction;
	}

	public void setExecuteGraphQueryAction(OBDADataQueryAction<?> action) {
		this.executeGraphQueryAction = action;
	}


	public void setRetrieveUCQExpansionAction(OBDADataQueryAction<String> retrieveUCQExpansionAction) {
		this.retrieveUCQExpansionAction = retrieveUCQExpansionAction;
	}

	public OBDADataQueryAction<?> getRetrieveUCQExpansionAction() {
		return retrieveUCQExpansionAction;
	}

	public void setRetrieveUCQUnfoldingAction(OBDADataQueryAction<String> retrieveUCQUnfoldingAction) {
		this.retrieveUCQUnfoldingAction = retrieveUCQUnfoldingAction;
	}


	//get and update the info box with  the actual time in seconds of the execution of the query
	public void updateStatus(long result) {
		if (result != - 1) {
			Double time = execTime / 1000;
			String s = String.format("Execution time: %s sec - Number of rows retrieved: %,d ", time, result);
			SwingUtilities.invokeLater(() -> {
				executionInfoLabel.setText(s);
				executionInfoLabel.setOpaque(false);
			});
		}
	}

	//set the boolean result in the info box of the ask query
	//show the result for ask query
	public  void showBooleanActionResultInTextInfo(String title, BooleanOWLResultSet result) {
		Double time = execTime / 1000;
		String s = String.format("Execution time: %s sec - ", time);
		SwingUtilities.invokeLater(() -> {
			try {
				boolean value = result.getValue();
				executionInfoLabel.setBackground(value ? Color.GREEN : Color.RED);
				executionInfoLabel.setOpaque(true);
				executionInfoLabel.setText(s + title + " " + value);
			}
			catch (OWLException e) {
				DialogUtils.showSeeLogErrorDialog(this, "", LOGGER, e);
			}
		});
	}

	//update the number of rows when the table change
	@Override
	public void tableChanged(TableModelEvent e) {
		int rows = ((TableModel) e.getSource()).getRowCount();
		updateStatus(rows);
	}

	public boolean isShortURISelect() {
		return showShortIriCheckBox.isSelected();
	}

	public boolean isFetchAllSelect() {
		return showAllCheckBox.isSelected();
	}

	// TODO Remove this method after moving the GUI package to protege41 module.
	// The constant 100 is the same as the NEXT_FETCH_SIZE in OWLResultSetTableModel
	public boolean canGetMoreTuples() {
		return getFetchSize() > 100;
	}

	public String getQuery() {
		return queryTextPane.getText();
	}

	public int getFetchSize() {
		return ((Number) fetchSizeTextField.getValue()).intValue();
	}

}
