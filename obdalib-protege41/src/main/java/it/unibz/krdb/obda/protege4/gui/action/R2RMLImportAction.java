package it.unibz.krdb.obda.protege4.gui.action;

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

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.r2rml.R2RMLReader;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(R2RMLImportAction.class);

	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit) getEditorKit();
		obdaModel = ((OBDAModelManager) editorKit.get(OBDAModelImpl.class
				.getName())).getActiveOBDAModel();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		final OWLWorkspace workspace = editorKit.getWorkspace();

		if (obdaModel.getSources().isEmpty()) 
		{
			JOptionPane.showMessageDialog(workspace, "The data source is missing. Create one in ontop Mappings. ");
		}
		else {
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
					R2RMLReader reader = null;
					try {
						reader = new R2RMLReader(file);


					URI sourceID = obdaModel.getSources().get(0).getSourceID();

					try {
						for (OBDAMappingAxiom mapping : reader.readMappings()) {
							if (mapping.getTargetQuery().toString().contains("BNODE")) {
								JOptionPane.showMessageDialog(workspace, "The mapping " + mapping.getId() + " contains BNode. -ontoPro- does not support it yet.");
							} else {
								obdaModel.addMapping(sourceID, mapping);

							}
						}
						JOptionPane.showMessageDialog(workspace, "R2rml Import completed. " );
					} catch (DuplicateMappingException dm) {
						JOptionPane.showMessageDialog(workspace, "Duplicate mapping id found. Please correct the Resource node name: " + dm.getLocalizedMessage());
						throw new RuntimeException("Duplicate mapping found: " + dm.getMessage());
					}

					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "An error occurred. For more info, see the logs.");
						log.error("Error during r2rml import. \n");
						e.printStackTrace();
					}

				}
			}

		}
	}
}
