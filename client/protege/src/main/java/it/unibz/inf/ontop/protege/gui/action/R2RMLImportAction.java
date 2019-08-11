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

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.parser.DataSource2PropertiesConvertor;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModelController = null;
	private OWLModelManager modelManager;

	private Logger log = LoggerFactory.getLogger(R2RMLImportAction.class);

	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit) getEditorKit();
		obdaModelController = ((OBDAModelManager) editorKit.get(SQLPPMappingImpl.class
				.getName())).getActiveOBDAModel();
		modelManager = editorKit.getOWLWorkspace().getOWLModelManager();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		final OWLWorkspace workspace = editorKit.getWorkspace();

		if (obdaModelController.getSources().isEmpty()) {
			JOptionPane.showMessageDialog(workspace, "The data source is missing. Create one in ontop Mappings. ");
		} else {
			String message = "The imported mappings will be appended to the existing data source. Continue?";
			int response = JOptionPane.showConfirmDialog(workspace, message,
					"Confirmation", JOptionPane.YES_NO_OPTION);

			if (response == JOptionPane.YES_OPTION) {
				// Get the path of the file of the active OWL model
				OWLOntology activeOntology = modelManager.getActiveOntology();
				IRI documentIRI = modelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
				File ontologyDir = new File(documentIRI.toURI().getPath());
				final JFileChooser fc = new JFileChooser(ontologyDir);

				fc.showOpenDialog(workspace);
				File file = null;
				try {

					file = (fc.getSelectedFile());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (file != null) {


					File finalFile = file;
					Thread th = new Thread("Bootstrapper Action Thread"){
						@Override
						public void run() {
							try {
								OBDAProgressMonitor monitor = new OBDAProgressMonitor(
										"Bootstrapping ontology and mappings...", workspace);
								R2RMLImportThread t = new R2RMLImportThread();
								monitor.addProgressListener(t);
								monitor.start();
								t.run(finalFile);
								monitor.stop();
								JOptionPane.showMessageDialog(workspace,
										"R2RML Import completed.", "Done",
										JOptionPane.INFORMATION_MESSAGE);
							}catch (Exception e) {
								JOptionPane.showMessageDialog(workspace, "An error occurred. For more info, see the logs.");
								log.error("Error during R2RML import. \n", e.getMessage());
								e.printStackTrace();
							}
						}
					};
					th.start();


				}

			}
		}
	}

	private class R2RMLImportThread implements OBDAProgressListener {

		@Override
		public void actionCanceled() throws Exception {

		}

		public void run(File file)
				throws Exception {

			/**
			 * Uses the predefined data source for creating the OBDAModel.
			 */
			OBDADataSource dataSource = obdaModelController.getSources().get(0);

			OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
					.properties(DataSource2PropertiesConvertor.convert(dataSource))
					.r2rmlMappingFile(file)
					.build();


			SQLPPMapping parsedModel = configuration.loadProvidedPPMapping();

			try{
			/**
			 * TODO: improve this inefficient method (batch processing, not one by one)
			 */
			for (SQLPPTriplesMap mapping : parsedModel.getTripleMaps()) {
//				if (mapping.getTargetAtoms().toString().contains("BNODE")) {
//					JOptionPane.showMessageDialog(getWorkspace(), "The mapping " + mapping.getId() + " contains BNode. -ontoPro- does not support it yet.");
//				} else {
					obdaModelController.addTriplesMap(mapping, false);
//				}
			}
			} catch (DuplicateMappingException dm) {
				JOptionPane.showMessageDialog(getWorkspace(), "Duplicate mapping id found. Please correct the Resource node name: " + dm.getLocalizedMessage());
				throw new RuntimeException("Duplicate mapping found: " + dm.getMessage());
			}
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
