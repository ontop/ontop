package it.unibz.inf.ontop.protege.action;

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
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.mapping.TriplesMap;
import it.unibz.inf.ontop.protege.mapping.TriplesMapFactory;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private static final String DIALOG_TITLE = "R2RML Import";

	private static final Logger LOGGER = LoggerFactory.getLogger(R2RMLImportAction.class);

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (!DialogUtils.confirmation(getWorkspace(),
				"<html>The imported mappings will be appended to the existing data source.<br><br>Do you wish to <b>continue</b>?<br></html>",
				DIALOG_TITLE))
			return;

		JFileChooser fc = DialogUtils.getFileChooser(getEditorKit(), null);
		if (fc.showOpenDialog(getWorkspace()) != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();
		R2RMLImportWorker worker = new R2RMLImportWorker(file);
		worker.execute();
	}

	private class R2RMLImportWorker extends SwingWorkerWithMonitor<ImmutableList<SQLPPTriplesMap>, Void> {

		private final File file;
		private final OBDAModel obdaModel;

		R2RMLImportWorker(File file) {
			super(getWorkspace(),
					"<html><h3>Importing R2RML mapping:</h3></html>", true);
			this.file = file;
			this.obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());
		}

		@Override
		protected ImmutableList<SQLPPTriplesMap> doInBackground() throws Exception {
			start("initializing...");
			SQLPPMapping parsedModel = obdaModel.parseR2RMLMapping(file);

			// convert R2RML into Native triples maps
			TriplesMapFactory triplesMapFactory = obdaModel.getTriplesMapFactory();
			ImmutableList.Builder<SQLPPTriplesMap> builder = ImmutableList.builder();
			for (SQLPPTriplesMap m : parsedModel.getTripleMaps()) {
				String target = triplesMapFactory.getTargetRendering(m.getTargetAtoms());
				SQLPPTriplesMap nativeTriplesMap = new OntopNativeSQLPPTriplesMap(m.getId(), m.getSourceQuery(), triplesMapFactory.getTargetQuery(target));
				builder.add(nativeTriplesMap);
			}

			ImmutableList<SQLPPTriplesMap> triplesMaps = builder.build();
			endLoop("");
			end();
			return triplesMaps;
		}

		@Override
		public void done() {
			try {
				ImmutableList<SQLPPTriplesMap> triplesMaps = complete();
				// TODO: move back to doInBackground?
				Set<OWLDeclarationAxiom> axioms = obdaModel.insertTriplesMaps(triplesMaps, false);
				DialogUtils.showInfoDialog(getWorkspace(),
						"<html><h3>Import of R2RML mapping is complete.</h3><br>" +
								HTML_TAB + "<b>" + triplesMaps.size() + "</b> triples maps inserted into the mapping.<br>" +
								HTML_TAB + "<b>" + axioms.size() + "</b> declaration axioms (re)inserted into the ontology.<br></html>",
						DIALOG_TITLE);
			}
			catch (DuplicateTriplesMapException e) {
				LOGGER.error("Internal error:", e);
			}
			catch (CancellationException | InterruptedException e) {
				DialogUtils.showCancelledActionDialog(getWorkspace(), DIALOG_TITLE);
			}
			catch (ExecutionException e) {
				DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, DIALOG_TITLE + " error.", LOGGER, e, obdaModel.getDataSource());
			}
		}
	}

	@Override
	public void initialise() {/* NO-OP */ }

	@Override
	public void dispose()  {/* NO-OP */ }
}
