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

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.r2rml.R2RMLWriter;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;

public class R2RMLExportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	private OWLModelManager modelManager= null;
	
	private Logger log = LoggerFactory.getLogger(R2RMLExportAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();		
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();
		modelManager = editorKit.getOWLModelManager();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

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

                final JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File(sourceID + "-mappings.ttl"));
                int approve = fc.showSaveDialog(workspace);

                if(approve == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();


                    R2RMLWriter writer = new R2RMLWriter(obdaModel, sourceID, modelManager.getActiveOntology());
                    writer.write(file);
                    JOptionPane.showMessageDialog(workspace, "R2rml Export completed.");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "An error occured. For more info, see the logs.");
            log.error("Error during r2rml export. \n");
            ex.printStackTrace();
        }

	}
}
