package it.unibz.inf.ontop.protege.mapping;

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

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.mapping.worker.ExecuteSQLQuerySwingWorker;
import it.unibz.inf.ontop.protege.utils.*;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.*;

public class EditMappingDialog extends JDialog {

	private static final long serialVersionUID = 4351696247473906680L;

	private static final int MAX_ROWS = 100;

	@Nullable
	private final String id; // null means creating a new mapping

	private final OBDAModel obdaModel;

	private final TargetQueryStyledDocument targetQueryDocument;

	private final JTable sqlQueryResultTable;
	private final JTextField mappingIdField;
	private final JTextPane targetQueryTextPane;
	private final JTextPane sourceQueryTextPane;

	private final Border defaultBorder;
	private final Border errorBorder;

	private static final int DEFAULT_TOOLTIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOLTIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOLTIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOLTIP_DISMISS_DELAY = 9000;

	private boolean allComponentsNonEmpty = false, isValid = false;

	public EditMappingDialog(OBDAModel obdaModel, ColorSettings colorSettings, String id) {
		this(obdaModel, colorSettings, null, "New Triples Map", "Create");

		mappingIdField.setText(id);
	}

	public EditMappingDialog(OBDAModel obdaModel, ColorSettings colorSettings, TriplesMap triplesMap) {
		this(obdaModel, colorSettings, triplesMap.getId(), "Edit Triples Map", "Update");

		mappingIdField.setText(triplesMap.getId());
		sourceQueryTextPane.setText(triplesMap.getSqlQuery());
		if (triplesMap.getSqlErrorMessage() != null) {
			sourceQueryTextPane.setToolTipText(triplesMap.getSqlErrorMessage());
			sourceQueryTextPane.setBorder(errorBorder);
			sourceQueryTextPane.getDocument().addDocumentListener((SimpleDocumentListener)
					e -> sourceQueryTextPane.setBorder(defaultBorder));
		}
		targetQueryDocument.setInvalidPlaceholders(triplesMap.getInvalidPlaceholders());
		targetQueryTextPane.setText(triplesMap.getTargetRendering());
		targetValidation();
	}

	private EditMappingDialog(OBDAModel obdaModel, ColorSettings colorSettings, String id, String title, String buttonText) {
		this.obdaModel = obdaModel;
		this.id = id;

		saveAction = new OntopAbstractAction(buttonText, null, null,
				getKeyStrokeWithCtrlMask(VK_ENTER)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveTriplesMap();
			}
		};

		setTitle(title);
		setModal(true);

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		JPanel mainPanel = new JPanel(new GridBagLayout());

		mainPanel.add(new JLabel("Mapping ID:"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(8, 10, 8, 10), 0, 0));

		mappingIdField = new JTextField();
		mappingIdField.setFont(TargetQueryStyledDocument.TARGET_QUERY_FONT);
		setKeyboardShortcuts(mappingIdField);
		mainPanel.add(mappingIdField,
				new GridBagConstraints(1, 0, 1, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(8, 10, 8, 10), 0, 0));

		JPanel targetQueryPanel = new JPanel(new BorderLayout());
		targetQueryPanel.add(new JLabel("Target (Triples Template):"), BorderLayout.NORTH);
		targetQueryTextPane = new JTextPane();
		targetQueryTextPane.setBorder(defaultBorder);
		Timer timer = new Timer(1000, e -> targetValidation());
		timer.setRepeats(false);
		targetQueryDocument = new TargetQueryStyledDocument(obdaModel.getObdaModelManager(), colorSettings, d -> timer.restart());
		targetQueryTextPane.setDocument(targetQueryDocument);
		setKeyboardShortcuts(targetQueryTextPane);
		targetQueryPanel.add(new JScrollPane(targetQueryTextPane), BorderLayout.CENTER);

		JPanel sourceQueryPanel = new JPanel(new BorderLayout());
		sourceQueryPanel.add(new JLabel("Source (SQL Query):"), BorderLayout.NORTH);
		sourceQueryTextPane = new JTextPane();
		sourceQueryTextPane.setBorder(defaultBorder);
		sourceQueryTextPane.setDocument(new SQLQueryStyledDocument());
		setKeyboardShortcuts(sourceQueryTextPane);
		sourceQueryPanel.add(new JScrollPane(sourceQueryTextPane), BorderLayout.CENTER);

		JPanel sqlQueryResultPanel = new JPanel(new BorderLayout());
		sqlQueryResultPanel.add(new JLabel("SQL Query results:"), BorderLayout.NORTH);
		sqlQueryResultTable = new JTable();
		sqlQueryResultTable.setFocusable(false);
		sqlQueryResultPanel.add(new JScrollPane(sqlQueryResultTable), BorderLayout.CENTER);

		JSplitPane sqlSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourceQueryPanel, sqlQueryResultPanel);
		sqlSplitPane.setResizeWeight(0.6);
		sqlSplitPane.setOneTouchExpandable(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, targetQueryPanel, sqlSplitPane);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		mainPanel.add(splitPane,
				new GridBagConstraints(0, 1, 2, 1, 1, 1,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
						new Insets(0, 10, 0, 10), 0, 0));

		JPanel testSqlQueryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		testSqlQueryPanel.add(getButton(executeSqlQueryAction));
		testSqlQueryPanel.add(new JLabel("(" + MAX_ROWS + " rows)"));
		mainPanel.add(testSqlQueryPanel,
				new GridBagConstraints(0, 2, 2, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(4, 10, 0, 0), 0, 0));

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton saveButton = getButton(saveAction);
		controlPanel.add(saveButton);
		controlPanel.add(getButton(cancelAction));
		mainPanel.add(controlPanel,
				new GridBagConstraints(0, 3, 2, 1, 0, 0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 10, 4), 0, 0));

		setContentPane(mainPanel);

		setUpAccelerator(mainPanel, saveAction);
		setUpAccelerator(mainPanel, cancelAction);
		setUpAccelerator(mainPanel, executeSqlQueryAction);
		getRootPane().setDefaultButton(saveButton);

		setPreferredSize(new Dimension(700, 600));
	}

	private final OntopAbstractAction saveAction;

	private final OntopAbstractAction cancelAction = getStandardCloseWindowAction(CANCEL_BUTTON_TEXT, EditMappingDialog.this);

	private final OntopAbstractAction executeSqlQueryAction = new OntopAbstractAction("Execute the SQL query",
			"execute.png",
			"Execute the SQL query in the SQL query text pane\nand display the first " + MAX_ROWS + " results in the table",
			getKeyStrokeWithCtrlMask(VK_E)) {
		@Override
		public void actionPerformed(ActionEvent e) {
			ExecuteSQLQuerySwingWorker worker = new ExecuteSQLQuerySwingWorker(
					EditMappingDialog.this,
					obdaModel.getDataSource(),
					sourceQueryTextPane.getText().trim(),
					MAX_ROWS,
					sqlQueryResultTable::setModel);
			worker.execute();
		}
	};

	private void setKeyboardShortcuts(JTextComponent component) {
		component.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, 0)));
		component.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				ImmutableSet.of(KeyStroke.getKeyStroke(VK_TAB, SHIFT_DOWN_MASK)));

		component.getDocument().addDocumentListener((SimpleDocumentListener)
				e -> updateSaveMappingActionStatus());
	}

	private void updateSaveMappingActionStatus() {
		allComponentsNonEmpty = !mappingIdField.getText().trim().isEmpty()
				&& !targetQueryTextPane.getText().trim().isEmpty()
				&& !sourceQueryTextPane.getText().trim().isEmpty();

		saveAction.setEnabled(isValid && allComponentsNonEmpty);
	}

	private void targetValidation() {
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		String error;
		try {
			ImmutableSet<IRI> iris = targetQueryDocument.validate();
			if (iris.isEmpty()) {
				if (!isValid) {
					isValid = true;
					saveAction.setEnabled(allComponentsNonEmpty);
					targetQueryTextPane.setToolTipText(null);
					targetQueryTextPane.setBorder(defaultBorder);
					toolTipManager.setInitialDelay(DEFAULT_TOOLTIP_INITIAL_DELAY);
					toolTipManager.setDismissDelay(DEFAULT_TOOLTIP_DISMISS_DELAY);
				}
				return;
			}
			OntologyPrefixManager prefixManager = obdaModel.getMutablePrefixManager();
			error = iris.stream()
					.map(IRI::getIRIString)
					.map(prefixManager::getShortForm)
					.map(iri -> "\t- " + iri)
					.collect(Collectors.joining(
							",\n",
							"The following predicates are not declared in the ontology:\n",
							"."));
		}
		catch (TargetQueryParserException e) {
			error = (e.getMessage() == null)
					? "Unknown syntax error, check Protege log file."
					: e.getMessage().replace("'<EOF>'", "the end");
		}
		targetQueryTextPane.setBorder(errorBorder);
		toolTipManager.setInitialDelay(ERROR_TOOLTIP_INITIAL_DELAY);
		toolTipManager.setDismissDelay(ERROR_TOOLTIP_DISMISS_DELAY);
		targetQueryTextPane.setToolTipText("<html>" + htmlEscape(error) + "</html>");
		isValid = false;
		saveAction.setEnabled(false);
	}

	private void saveTriplesMap() {
		try {
			String newId = mappingIdField.getText().trim();
			String target = targetQueryTextPane.getText();
			String source = sourceQueryTextPane.getText().trim();

			if (id == null)
				obdaModel.getTriplesMapManager().add(newId, source, target);
			else
				obdaModel.getTriplesMapManager().update(id, newId, source, target);

			dispatchEvent(new WindowEvent(EditMappingDialog.this, WindowEvent.WINDOW_CLOSING));
		}
		catch (DuplicateTriplesMapException e) {
			JOptionPane.showMessageDialog(this, "Error while inserting triples map: ID " + e.getMessage() + " is already taken");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error while inserting triples map: " + e.getMessage());
		}
	}
}
