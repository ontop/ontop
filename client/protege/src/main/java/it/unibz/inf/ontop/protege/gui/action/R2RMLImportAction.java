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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.protege.core.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
	public void initialise() {
		editorKit = (OWLEditorKit) getEditorKit();
		obdaModelController = ((OBDAModelManager) editorKit.get(SQLPPMappingImpl.class
				.getName())).getActiveOBDAModel();
		modelManager = editorKit.getOWLWorkspace().getOWLModelManager();
	}

	@Override
	public void dispose()  {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		final OWLWorkspace workspace = editorKit.getWorkspace();

		if (obdaModelController.getSource() == null) {
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
					Thread th = new Thread("R2RML Import Thread"){
						@Override
						public void run() {
							try {
								OBDAProgressMonitor monitor = new OBDAProgressMonitor(
										"Importing R2RML mapping ...", workspace);
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
			OBDADataSource dataSource = obdaModelController.getSource();
			OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
					.properties(dataSource.asProperties())
					.r2rmlMappingFile(file)
					.build();

			SQLPPMapping parsedModel = configuration.loadProvidedPPMapping();
			OWLOntologyManager manager = modelManager.getOWLOntologyManager();

			/**
			 * TODO: improve this inefficient method (batch processing, not one by one)
			 */
			ImmutableList<SQLPPTriplesMap> tripleMaps = parsedModel.getTripleMaps();
			tripleMaps.forEach(tm -> registerTripleMap(tm));

			ImmutableList<AddAxiom> addAxioms = MappingOntologyUtils.extractDeclarationAxioms(
					manager,
					tripleMaps.stream()
							.flatMap(tm -> tm.getTargetAtoms().stream()),
					obdaModelController.getTypeFactory(),
					false
			).stream()
					.map(ax -> new AddAxiom(
							modelManager.getActiveOntology(),
							ax
					)).collect(ImmutableCollectors.toList());
			modelManager.applyChanges(addAxioms);
		}

		private void registerTripleMap(SQLPPTriplesMap tm) {
			try{
				obdaModelController.addTriplesMap(tm, false);
			}
			catch (DuplicateMappingException dm) {
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
