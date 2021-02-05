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
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLImportAction.class);

	private static final String DIALOG_TITLE = "R2RML Import";

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (JOptionPane.showConfirmDialog(getWorkspace(),
				"<html>The imported mappings will be appended to the existing data source.<br><br>Do you wish to <b>continue</b>?<br></html>",
				DIALOG_TITLE,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				IconLoader.getOntopIcon()) != JOptionPane.YES_OPTION)
			return;

		JFileChooser fc = DialogUtils.getFileChooser(getEditorKit(), null);
		if (fc.showOpenDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();
		Thread thread = new Thread("R2RML Import Thread") {
			@Override
			public void run() {
				try {
					OBDAProgressMonitor monitor = new OBDAProgressMonitor(
							"Importing R2RML mapping ...", getWorkspace());
					R2RMLImportThread t = new R2RMLImportThread();
					monitor.addProgressListener(t);
					monitor.start();
					t.run(file);
					monitor.stop();
					JOptionPane.showMessageDialog(getWorkspace(),
							"R2RML Import completed.",
							"Done",
							JOptionPane.INFORMATION_MESSAGE);
				}
				catch (Throwable e) {
					DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Error during R2RML import.", LOGGER, e);
				}
			}
		};
		thread.start();
	}

	// TODO: NOT A THREAD
	private class R2RMLImportThread implements OBDAProgressListener {

		public void run(File file) throws Exception {

			OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());

			OBDADataSource dataSource = obdaModelManager.getDatasource();
			OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
					.properties(dataSource.asProperties())
					.r2rmlMappingFile(file)
					.build();

			SQLPPMapping parsedModel = configuration.loadProvidedPPMapping();

			OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
			OWLModelManager modelManager = editorKit.getOWLWorkspace().getOWLModelManager();
			OWLOntologyManager manager = modelManager.getOWLOntologyManager();

			OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
			ImmutableList<SQLPPTriplesMap> tripleMaps = parsedModel.getTripleMaps();
			try {
				obdaModel.add(tripleMaps);
			}
			catch (DuplicateMappingException dm) {
				JOptionPane.showMessageDialog(getWorkspace(), "Duplicate mapping id found. Please correct the Resource node name: " + dm.getLocalizedMessage());
				throw new RuntimeException("Duplicate mapping found: " + dm.getMessage());
			}

			ImmutableList<AddAxiom> addAxioms = MappingOntologyUtils.extractDeclarationAxioms(
					manager,
					tripleMaps.stream()
							.flatMap(tm -> tm.getTargetAtoms().stream()),
					obdaModelManager.getTypeFactory(),
					false)
					.stream()
					.map(ax -> new AddAxiom(modelManager.getActiveOntology(), ax))
					.collect(ImmutableCollectors.toList());

			modelManager.applyChanges(addAxioms);
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
	public void initialise() {
		/* NO-OP */
	}

	@Override
	public void dispose()  {
		/* NO-OP */
	}
}
