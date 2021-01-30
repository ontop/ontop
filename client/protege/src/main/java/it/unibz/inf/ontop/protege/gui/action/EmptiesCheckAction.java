package it.unibz.inf.ontop.protege.gui.action;

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

import it.unibz.inf.ontop.owlapi.validation.QuestOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.panels.EmptiesCheckPanel;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class EmptiesCheckAction extends ProtegeAction {

	private static final long serialVersionUID = 3322509244957306932L;

	private final Logger log = LoggerFactory.getLogger(EmptiesCheckAction.class);

	@Override
	public void actionPerformed(ActionEvent evt) {
		Optional<OntopProtegeReasoner> reasoner = DialogUtils.getOntopProtegeReasoner(getEditorKit());
		if (!reasoner.isPresent())
			return;

		try {
			OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
			QuestOWLEmptyEntitiesChecker checker = reasoner.get().getEmptyEntitiesChecker();

			JDialog dialog = new JDialog();
			dialog.setModal(true);
			dialog.setSize(520, 400);
			dialog.setLocationRelativeTo(null);
			dialog.setTitle("Emptiness Check");

			EmptiesCheckPanel emptiesPanel = new EmptiesCheckPanel();
			Thread thread = new Thread("EmptyEntitiesCheck Thread") {
				@Override
				public void run() {
					OBDAProgressMonitor monitor = new OBDAProgressMonitor("Finding empty entities...", getWorkspace());
					monitor.addProgressListener(emptiesPanel);
					monitor.start();
					SwingUtilities.invokeLater(() -> emptiesPanel.initContent(checker, obdaModelManager.getActiveOBDAModel().getMutablePrefixManager()));
					monitor.stop();
					if (!emptiesPanel.isCancelled() && !emptiesPanel.isErrorShown()) {
						SwingUtilities.invokeLater(() -> dialog.setVisible(true));
					}
				}
			};
			thread.start();

			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			JButton cmdCloseInformation = new JButton("Close");
			cmdCloseInformation.addActionListener(e -> {
				dialog.setVisible(false);
				dialog.removeAll();
				dialog.dispose();
			});
			panel.add(cmdCloseInformation);

			dialog.setLayout(new BorderLayout());
			dialog.add(emptiesPanel, BorderLayout.CENTER);
			dialog.add(panel, BorderLayout.SOUTH);
			DialogUtils.installEscapeCloseOperation(dialog);

			dialog.pack();
		}
		catch (Throwable e) {
			DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Error during emptiness checking.", log, e);
		}
	}

	@Override
	public void initialise() { /* NO-OP */ }

	@Override
	public void dispose() {  /* NO-OP */ }
}
