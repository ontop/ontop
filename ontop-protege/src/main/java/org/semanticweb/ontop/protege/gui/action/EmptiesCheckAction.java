package org.semanticweb.ontop.protege.gui.action;

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

import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLEmptyEntitiesChecker;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.ontop.protege.panels.EmptiesCheckPanel;
import org.semanticweb.ontop.protege.utils.DialogUtils;
import org.semanticweb.ontop.protege.utils.OBDAProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class EmptiesCheckAction extends ProtegeAction {

	private static final long serialVersionUID = 3322509244957306932L;

	private OWLEditorKit editorKit = null;
	private OWLModelManager owlManager = null;
	private QuestOWLEmptyEntitiesChecker check;

	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit) getEditorKit();
		owlManager = editorKit.getOWLModelManager();
//		currentOnto = owlManager.getActiveOntology();

	}

	@Override
	public void dispose() throws Exception {
		// Does nothing.
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		OWLReasoner reasoner = owlManager.getOWLReasonerManager().getCurrentReasoner();


		if (reasoner instanceof QuestOWL) {
			try {
				check = ((QuestOWL) reasoner).getEmptyEntitiesChecker();

				JDialog dialog = new JDialog();
				dialog.setModal(true);
				dialog.setSize(520, 400);
				dialog.setLocationRelativeTo(null);
				dialog.setTitle("Empties Check");

				EmptiesCheckPanel emptiesPanel = new EmptiesCheckPanel();
				Thread th = new Thread("EmptyEntitiesCheck") {
					public void run() {

						OBDAProgressMonitor monitor = new OBDAProgressMonitor("Finding empty entities...");
						monitor.addProgressListener(emptiesPanel);
						monitor.start();
						emptiesPanel.initContent(check);
						monitor.stop();


						if(!emptiesPanel.isCancelled() && !emptiesPanel.isErrorShown()) {
							dialog.setVisible(true);
						}

					}
				};
				th.start();

				JPanel pnlCommandButton = createButtonPanel(dialog);
				dialog.setLayout(new BorderLayout());
				dialog.add(emptiesPanel, BorderLayout.CENTER);
				dialog.add(pnlCommandButton, BorderLayout.SOUTH);
				DialogUtils.installEscapeCloseOperation(dialog);

				dialog.pack();


			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "An error occurred. For more info, see the logs.");
			}

		} else {
			JOptionPane.showMessageDialog(null, "You have to start ontop reasoner for this feature.");
		}

	}

	private JPanel createButtonPanel(final JDialog parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		JButton cmdCloseInformation = new JButton();
		cmdCloseInformation.setText("Close Information");
		cmdCloseInformation.addActionListener(evt -> {
            parent.setVisible(false);
            parent.removeAll();
            parent.dispose();
        });
		panel.add(cmdCloseInformation);

		return panel;
	}
}
