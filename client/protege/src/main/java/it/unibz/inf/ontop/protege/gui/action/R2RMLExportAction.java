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

import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.serializer.SQLPPMappingToR2RMLConverter;
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
import java.io.IOException;

public class R2RMLExportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	private OWLModelManager modelManager= null;
	
	private Logger log = LoggerFactory.getLogger(R2RMLExportAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();		
		obdaModel = ((OBDAModelManager)editorKit.get(SQLPPMappingImpl.class.getName())).getActiveOBDAModel();
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
                // Get the path of the file of the active OWL model
                OWLOntology activeOntology = modelManager.getActiveOntology();
                IRI documentIRI = modelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);

                File ontologyDir = new File(documentIRI.toURI().getPath());

                final JFileChooser fc = new JFileChooser(ontologyDir);
                final String shortForm = documentIRI.getShortForm();
                int i = shortForm.lastIndexOf(".");
                String ontologyName = (i < 1)?
                        shortForm:
                        shortForm.substring(0, i);
                fc.setSelectedFile(new File(ontologyName + "-mapping.ttl"));
                //fc.setSelectedFile(new File(sourceID + "-mapping.ttl"));

                int approve = fc.showSaveDialog(workspace);

                if(approve == JFileChooser.APPROVE_OPTION) {


                    final File file = fc.getSelectedFile();

                    Thread th = new Thread("R2RML Export Action Thread"){
                        @Override
                        public void run() {
                            try {
                                OBDAProgressMonitor monitor = new OBDAProgressMonitor(
                                        "Exporting the mapping to R2RML...", workspace);
                                R2RMLExportThread t = new R2RMLExportThread();
                                monitor.addProgressListener(t);
                                monitor.start();
                                t.run(file);
                                monitor.stop();
                                JOptionPane.showMessageDialog(workspace,
                                        "R2RML Export completed.", "Done",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(workspace, "An error occurred. For more info, see the logs.");
                                log.error("Error during R2RML export. \n", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    };
                    th.start();


                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getWorkspace(), "An error occurred. For more info, see the logs.");
            log.error("Error during R2RML export. \n");
            ex.printStackTrace();
        }

	}

    private class R2RMLExportThread implements OBDAProgressListener {

        @Override
        public void actionCanceled() {

        }

        public void run(File file) throws IOException {
            SQLPPMappingToR2RMLConverter writer = new SQLPPMappingToR2RMLConverter(obdaModel.generatePPMapping(),
                    obdaModel.getRdfFactory(), obdaModel.getTermFactory());
            writer.write(file);
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
