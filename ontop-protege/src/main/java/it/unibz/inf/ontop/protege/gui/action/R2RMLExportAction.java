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

import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelWrapper;
import it.unibz.inf.ontop.r2rml.R2RMLWriter;
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
import java.net.URI;

public class R2RMLExportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModelWrapper obdaModel = null;
	private OWLModelManager modelManager= null;
	
	private Logger log = LoggerFactory.getLogger(R2RMLExportAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();		
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModelWrapper();
		modelManager = editorKit.getOWLModelManager();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

        // Assumes initialise() has been run and has set modelManager to active OWLModelManager
	@Override
	public void actionPerformed(ActionEvent arg0) {

        try {
		final OWLWorkspace workspace = editorKit.getWorkspace();
            if (obdaModel.getSources().isEmpty())
            {
                JOptionPane.showMessageDialog(workspace, "The data source is missing. Create one in ontop Mappings. ");
            }
            else {
                URI sourceID = obdaModel.getSources().get(0).getSourceID();

		// Get the path of the file of the active OWL model
		OWLOntology activeOntology = modelManager.getActiveOntology();
                IRI documentIRI = modelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
		File ontologyDir = new File(documentIRI.toURI().getPath());

		final JFileChooser fc = new JFileChooser(ontologyDir);
                fc.setSelectedFile(new File(sourceID + "-mappings.ttl"));
                int approve = fc.showSaveDialog(workspace);

                if(approve == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

					/**
					 * TODO: improve this
					 */
					NativeQueryLanguageComponentFactory nativeQLFactory = OntopModelConfiguration.defaultBuilder()
							.build().getInjector().getInstance(NativeQueryLanguageComponentFactory.class);

					R2RMLWriter writer = new R2RMLWriter(obdaModel.getCurrentImmutableOBDAModel(),
							modelManager.getActiveOntology(),
							nativeQLFactory);
                writer.write(file);
                    JOptionPane.showMessageDialog(workspace, "R2RML Export completed.");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "An error occurred. For more info, see the logs.");
            log.error("Error during R2RML export. \n");
            ex.printStackTrace();
        }

	}
}
