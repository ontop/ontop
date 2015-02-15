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
import java.io.IOException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelWrapper;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.r2rml.R2RMLMappingParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModelWrapper obdaModelController = null;

	private Logger log = LoggerFactory.getLogger(R2RMLImportAction.class);
    private NativeQueryLanguageComponentFactory nativeQLFactory;

    @Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit) getEditorKit();
		obdaModelController = ((OBDAModelManager) editorKit.get(OBDAModelImpl.class
				.getName())).getActiveOBDAModelWrapper();

        /**
         * OBDA properties for building a R2RML mapping parser
		 *
		 * Data source parameters are missing. --> We use the dataSource object instead.
         */
        OBDAProperties r2rmlProperties = new OBDAProperties();
        r2rmlProperties.setProperty(MappingParser.class.getCanonicalName(),
                R2RMLMappingParser.class.getCanonicalName());

        Injector injector = Guice.createInjector(new OBDACoreModule(r2rmlProperties));
        nativeQLFactory = injector.getInstance(
                NativeQueryLanguageComponentFactory.class);
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
                Disposable d = editorKit.get(NativeQueryLanguageComponentFactory.class.getName());

				/**
				 * Uses the predefined data source for creating the OBDAModel.
				 */
				OBDADataSource dataSource = obdaModelController.getSources().get(0);
                MappingParser parser = nativeQLFactory.create(file, dataSource);
				URI sourceID = dataSource.getSourceID();

				try {
                    OBDAModel parsedModel = parser.getOBDAModel();

                    /**
                     * TODO: improve this inefficient method (batch processing, not one by one)
                     */
					for (OBDAMappingAxiom mapping : parsedModel.getMappings(sourceID)) {
						if (mapping.getTargetQuery().toString().contains("BNODE")){
							JOptionPane.showMessageDialog(workspace, "The mapping "+mapping.getId()+" contains BNode. -ontoPro- does not support it yet.");
						} else{
							obdaModelController.addMapping(sourceID, mapping);
						}
					}
				} catch (DuplicateMappingException dm) {
					JOptionPane.showMessageDialog(workspace, "Duplicate mapping id found. Please correct the Resource node name: "+dm.getLocalizedMessage());
					throw new RuntimeException("Duplicate mapping found: "+dm.getMessage());
				} catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidMappingException e) {
                    e.printStackTrace();
                } catch (InvalidDataSourceException e) {
                    e.printStackTrace();
                }
            }

	}
	}
}
