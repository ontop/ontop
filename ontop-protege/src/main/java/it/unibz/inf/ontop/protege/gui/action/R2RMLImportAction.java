package it.unibz.inf.ontop.protege.gui.action;

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
import it.unibz.inf.ontop.protege.core.OBDAModelWrapper;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDACoreModule;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.mapping.MappingParser;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.owlrefplatform.questdb.R2RMLQuestPreferences;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
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
		OBDAProperties r2rmlProperties = new R2RMLQuestPreferences();

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

		if (obdaModelController.getSources().isEmpty()) {
			JOptionPane.showMessageDialog(workspace, "The data source is missing. Create one in ontop Mappings. ");
		} else {
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
							if (mapping.getTargetQuery().toString().contains("BNODE")) {
								JOptionPane.showMessageDialog(workspace, "The mapping " + mapping.getId() + " contains BNode. -ontoPro- does not support it yet.");
							} else {
								obdaModelController.addMapping(sourceID, mapping);
							}
						}
					} catch (DuplicateMappingException dm) {
						JOptionPane.showMessageDialog(workspace, "Duplicate mapping id found. Please correct the Resource node name: " + dm.getLocalizedMessage());
						throw new RuntimeException("Duplicate mapping found: " + dm.getMessage());
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
}
