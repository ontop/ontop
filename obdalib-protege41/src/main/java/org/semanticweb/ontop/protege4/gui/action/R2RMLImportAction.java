package org.semanticweb.ontop.protege4.gui.action;

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

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.SQLOBDAModel;
import org.semanticweb.ontop.model.impl.SQLOBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.r2rml.R2RMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private SQLOBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(R2RMLImportAction.class);

	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit) getEditorKit();
		obdaModel = ((OBDAModelManager) editorKit.get(SQLOBDAModelImpl.class
				.getName())).getActiveOBDAModel();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		final OWLWorkspace workspace = editorKit.getWorkspace();

		String message = "The imported mappings will be appended to the existing data source. Continue?";
		int response = JOptionPane.showConfirmDialog(workspace, message,
				"Confirmation", JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {

			final JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(workspace);
			File file = null;
			try {

				file = (fc.getSelectedFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (file != null) {
				R2RMLReader reader = new R2RMLReader(file);

				URI sourceID = obdaModel.getSources().get(0).getSourceID();

				try {
					for (OBDAMappingAxiom mapping : reader.readMappings()) {
						if (mapping.getTargetQuery().toString().contains("BNODE")){
							JOptionPane.showMessageDialog(workspace, "The mapping "+mapping.getId()+" contains BNode. -ontoPro- does not support it yet.");
						} else{
							obdaModel.addMapping(sourceID, mapping);
						}
					}
				} catch (DuplicateMappingException dm) {
					JOptionPane.showMessageDialog(workspace, "Duplicate mapping id found. Please correct the Resource node name: "+dm.getLocalizedMessage());
					throw new RuntimeException("Duplicate mapping found: "+dm.getMessage());
				}
		}

	}
	}
}
