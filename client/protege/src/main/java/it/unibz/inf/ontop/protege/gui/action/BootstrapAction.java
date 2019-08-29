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

import it.unibz.inf.ontop.protege.core.MutablePrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.utils.BootstrapGenerator;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BootstrapAction extends ProtegeAction {

	private static final long serialVersionUID = 8671527155950905524L;

	private OWLEditorKit editorKit = null;
	private OWLWorkspace workspace;
	private OWLModelManager owlManager;
	private OBDAModelManager modelManager;
	private String baseUri = "";

	private OBDAModel currentModel;
	private OBDADataSource currentSource;

	private Logger log = LoggerFactory.getLogger(BootstrapAction.class);

	@Override
	public void initialise() {
		editorKit = (OWLEditorKit) getEditorKit();
		workspace = editorKit.getWorkspace();
		owlManager = editorKit.getOWLModelManager();
		modelManager = ((OBDAModelManager) editorKit.get(SQLPPMappingImpl.class.getName()));
	}

	@Override
	public void dispose() {
		// NOP
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		currentModel = modelManager.getActiveOBDAModel();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JLabel ouri = new JLabel(
				"Base URI - the prefix to be used for all generated classes and properties: ");
		ouri.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(ouri);
		Dimension minsize1 = new Dimension(10, 10);
		panel.add(new Box.Filler(minsize1, minsize1, minsize1));
		JTextField base_uri = new JTextField();
		base_uri.setText(currentModel.getMutablePrefixManager().getDefaultPrefix()
				.replace("#", "/"));
		base_uri.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(base_uri);
		Dimension minsize2 = new Dimension(20, 20);
		panel.add(new Box.Filler(minsize2, minsize2, minsize2));
		int res = JOptionPane.showOptionDialog(workspace, panel,
				"Bootstrapping", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (res == JOptionPane.OK_OPTION) {

			currentSource = currentModel.getDatasource();
			if (currentSource != null) {
				this.baseUri = base_uri.getText().trim();
				if (baseUri.contains("#")) {
					JOptionPane.showMessageDialog(workspace,
							"Base Uri cannot contain the character '#'");
					throw new RuntimeException("Base URI " + baseUri
							+ " contains '#' character!");
				} else {
					String bootstrapPrefix = "g:";
					MutablePrefixManager prefixManager = currentModel.getMutablePrefixManager();
					while(prefixManager.contains(bootstrapPrefix)){
						bootstrapPrefix = "g"+bootstrapPrefix;
					}
					currentModel.addPrefix(bootstrapPrefix, baseUri);
					Thread th = new Thread("Bootstrapper Action Thread"){
						@Override
						public void run() {
							try {
								OBDAProgressMonitor monitor = new OBDAProgressMonitor(
										"Bootstrapping ontology and mappings...", workspace);
								BootstrapperThread t = new BootstrapperThread();
								monitor.addProgressListener(t);
								monitor.start();
								t.run(baseUri);
								monitor.stop();
								JOptionPane.showMessageDialog(workspace,
										"Task is completed.", "Done",
										JOptionPane.INFORMATION_MESSAGE);

								// FORCE REPAINT!
								// TODO(xiao): it is not clear whether the following really fixed the issue of the panel being blank
								editorKit.getWorkspace().getSelectedTab().revalidate();
								editorKit.getWorkspace().getSelectedTab().repaint();

							} catch (Exception e) {
								log.error(e.getMessage(), e);
								JOptionPane
										.showMessageDialog(workspace,
												"Error occured during bootstrapping data source.");
							}
						}
					};
					th.start();
				}

			}
		}
	}

	private class BootstrapperThread implements OBDAProgressListener {

		@Override
		public void actionCanceled() throws Exception {

		}

		public void run(String baseUri) throws Exception {
			OBDAModelManager obdaModelManager = (OBDAModelManager) editorKit.get(SQLPPMappingImpl.class.getName());
			new BootstrapGenerator(obdaModelManager, baseUri, owlManager);
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isErrorShown() {
			return false;
		}

	}

}
