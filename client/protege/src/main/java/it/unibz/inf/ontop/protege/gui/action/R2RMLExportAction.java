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

import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.R2RMLMappingSerializer;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
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

    private static final Logger log = LoggerFactory.getLogger(R2RMLExportAction.class);

	@Override
	public void actionPerformed(ActionEvent evt) {
        // Get the path of the file of the active OWL model
        OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
        OWLModelManager modelManager = editorKit.getOWLModelManager();
        OWLOntology activeOntology = modelManager.getActiveOntology();
        IRI documentIRI = modelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);

        JFileChooser fc = DialogUtils.getFileChooser(getEditorKit());

        String shortForm = documentIRI.getShortForm();
        int i = shortForm.lastIndexOf(".");
        String ontologyName = (i < 1)?
                shortForm:
                shortForm.substring(0, i);
        fc.setSelectedFile(new File(ontologyName + "-mapping.ttl"));

        if (fc.showSaveDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
            return;
        File file = fc.getSelectedFile();
        Thread thread = new Thread("R2RML Export Action Thread") {
            @Override
            public void run() {
                try {
                    OBDAProgressMonitor monitor = new OBDAProgressMonitor(
                            "Exporting the mapping to R2RML...", getWorkspace());
                    R2RMLExportThread t = new R2RMLExportThread();
                    monitor.addProgressListener(t);
                    monitor.start();
                    t.run(file);
                    monitor.stop();
                    JOptionPane.showMessageDialog(getWorkspace(),
                            "R2RML Export completed.",
                            "Done",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Throwable e) {
                    DialogUtils.showSeeLogErrorDialog(getWorkspace(),"Error during R2RML export.", log, e);
                }
            }
        };
        thread.start();
	}

	// TODO: NOT A THREAD!
    private class R2RMLExportThread implements OBDAProgressListener {

        public void run(File file) throws IOException {
            OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
            R2RMLMappingSerializer writer = new R2RMLMappingSerializer(obdaModelManager.getRdfFactory());
            writer.write(file, obdaModelManager.getActiveOBDAModel().generatePPMapping());
        }

        @Override
        public void actionCanceled() {  }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isErrorShown() {
            return false;
        }
    }

    @Override
    public void initialise()  { /* NO-OP */ }

    @Override
    public void dispose()  { /* NO-OP */ }

}
