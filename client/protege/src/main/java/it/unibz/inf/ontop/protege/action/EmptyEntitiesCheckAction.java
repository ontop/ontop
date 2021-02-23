package it.unibz.inf.ontop.protege.action;

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

import it.unibz.inf.ontop.owlapi.validation.OntopOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.apache.commons.rdf.api.IRI;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;

public class EmptyEntitiesCheckAction extends ProtegeAction {

	private static final long serialVersionUID = 3322509244957306932L;

	private static final Logger LOGGER = LoggerFactory.getLogger(EmptyEntitiesCheckAction.class);

	private static final String DIALOG_TITLE = "Empty Entities";

	private static final class EmptyEntityInfo {
		private final String id;
		private final boolean isClass;

		EmptyEntityInfo(String id, boolean isClass) {
			this.id = id;
			this.isClass = isClass;
		}

		Object[] asRow() {
			return new Object[] { id };
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		Optional<OntopProtegeReasoner> reasoner = DialogUtils.getOntopProtegeReasoner(getEditorKit());
		if (!reasoner.isPresent())
			return;

		JDialog dialog = new JDialog((JFrame)null, DIALOG_TITLE, true);

		JPanel emptiesPanel = new JPanel(new BorderLayout());

		JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		summaryPanel.add(new JLabel("<html><b>Empty classes and properties:</b></html>"));
		JLabel summaryLabel = new JLabel("<html><i>retrieving...</i></html>");
		summaryPanel.add(summaryLabel);
		emptiesPanel.add(summaryPanel, BorderLayout.NORTH);

		JPanel listsPanel = new JPanel();
		listsPanel.setLayout(new BoxLayout(listsPanel, BoxLayout.PAGE_AXIS));
		DefaultTableModel classModel = DialogUtils.createNonEditableTableModel(new String[] {"Classes"});
		JTable tblConceptCount = new JTable(classModel);
		listsPanel.add(new JScrollPane(tblConceptCount));
		DefaultTableModel propertyModel = DialogUtils.createNonEditableTableModel(new String[] {"Properties"});
		JTable tblPropertyCount = new JTable(propertyModel);
		listsPanel.add(new JScrollPane(tblPropertyCount));
		emptiesPanel.add(listsPanel, BorderLayout.CENTER);

		OBDAModel obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());

		SwingWorker<String, EmptyEntityInfo> worker = new SwingWorker<String, EmptyEntityInfo>() {

			@Override
			protected String doInBackground() {
				OntopOWLEmptyEntitiesChecker checker = reasoner.get().getEmptyEntitiesChecker();
				PrefixManager prefixManager = obdaModel.getMutablePrefixManager();
				int emptyClasses = 0;
				for (IRI iri : checker.emptyClasses()) {
					if (isCancelled())
						break;

					publish(new EmptyEntityInfo(
							prefixManager.getShortForm(iri.getIRIString()),
							true));
					emptyClasses++;
				}

				int emptyProperties = 0;
				for (IRI iri : checker.emptyProperties()) {
					if (isCancelled())
						break;

					publish(new EmptyEntityInfo(
							prefixManager.getShortForm(iri.getIRIString()),
							false));
					emptyProperties++;
				}

				return String.format("%s empty %s,  %s empty %s",
						emptyClasses,
						emptyClasses == 1 ? "class" : "classes",
						emptyProperties,
						emptyProperties == 1 ? "property" : "properties");
			}

			@Override
			protected void process(java.util.List<EmptyEntityInfo> list) {
				for (EmptyEntityInfo info : list)
					(info.isClass ? classModel : propertyModel).addRow(info.asRow());
			}

			@Override
			protected void done() {
				try {
					summaryLabel.setText(get());
				}
				catch (CancellationException e) {
					/* NO-OP */
				}
				catch (InterruptedException e) {
					summaryLabel.setText("An error occurred: " + e.getMessage());
				}
				catch (ExecutionException e) {
					dialog.dispose();
					DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, DIALOG_TITLE + "error.", LOGGER, e, obdaModel.getDataSource());
				}
			}
		};
		worker.execute();

		JPanel commandPanel = new JPanel(new FlowLayout());
		OntopAbstractAction closeAction = getStandardCloseWindowAction("Close", dialog);
		JButton closeButton = getButton(closeAction);
		commandPanel.add(closeButton);

		setUpAccelerator(dialog.getRootPane(), closeAction);
		dialog.getRootPane().setDefaultButton(closeButton);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(false);
			}
		});

		dialog.setLayout(new BorderLayout());
		dialog.add(emptiesPanel, BorderLayout.CENTER);
		dialog.add(commandPanel, BorderLayout.SOUTH);

		dialog.setPreferredSize(new Dimension(520, 400));
		DialogUtils.setLocationRelativeToProtegeAndOpen(getEditorKit(), dialog);
	}

	@Override
	public void initialise() { /* NO-OP */ }

	@Override
	public void dispose() {  /* NO-OP */ }
}
