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
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.mapping.DuplicateTriplesMapException;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SwingWorkerWithCompletionPercentageMonitor;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class BootstrapAction extends ProtegeAction {

	private static final long serialVersionUID = 8671527155950905524L;

	private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAction.class);

	private static final String DIALOG_TITLE = "Bootstrapping ontology and mapping";

	@Override
	public void actionPerformed(ActionEvent evt) {

		OBDAModel obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());
		OntologyPrefixManager prefixManager = obdaModel.getMutablePrefixManager();

		String defaultBaseIRI = prefixManager.getDefaultIriPrefix()
				.replace("#", "/");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JLabel baseIriLabel = new JLabel("Base IRI - the prefix " +
				"to be used for all generated classes and properties: ");
		baseIriLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriLabel);

		panel.add(Box.createRigidArea(new Dimension(10, 10)));

		JTextField baseIriField = new JTextField(defaultBaseIRI);
		baseIriField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriField);

		panel.add(Box.createRigidArea(new Dimension(20, 20)));

		if (JOptionPane.showOptionDialog(getWorkspace(),
				panel,
				DIALOG_TITLE,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				DialogUtils.getOntopIcon(),
				null,
				null) != JOptionPane.OK_OPTION)
			return;

		String baseIri0 = baseIriField.getText().trim();
		if (baseIri0.contains("#")) {
			DialogUtils.showPrettyMessageDialog(getWorkspace(),
					"Base IRIs cannot contain '#':\n" +
							baseIri0 + " is not a valid base IRI.",
					DIALOG_TITLE);
			return;
		}

		String baseIri = DirectMappingEngine.fixBaseURI(
				baseIri0.isEmpty() ? defaultBaseIRI : baseIri0);
		prefixManager.generateUniquePrefixForBootstrapper(baseIri);

		BootstrapWorker worker = new BootstrapWorker(baseIri);
		worker.execute();
	}

	private class BootstrapWorker extends SwingWorkerWithCompletionPercentageMonitor<ImmutableList<SQLPPTriplesMap>, Void> {

		private final String baseIri;
		private final JDBCMetadataProviderFactory metadataProviderFactory;
		private final DirectMappingEngine directMappingEngine;
		private final OBDAModel obdaModel;

		private final AtomicInteger currentMappingIndex;

		BootstrapWorker(String baseIri) {
			super(getWorkspace(),
					"<html><h3>Bootstrapping ontology and mapping:</h3></html>");

			this.baseIri = baseIri;

			obdaModel = OBDAEditorKitSynchronizerPlugin.getCurrentOBDAModel(getEditorKit());

			Injector injector = obdaModel.getOntopConfiguration().getInjector();
			this.metadataProviderFactory = injector.getInstance(JDBCMetadataProviderFactory.class);
			this.directMappingEngine = injector.getInstance(DirectMappingEngine.class);

			this.currentMappingIndex = new AtomicInteger(obdaModel.getTriplesMapManager().size() + 1);
		}

		@Override
		protected ImmutableList<SQLPPTriplesMap> doInBackground() throws Exception {

			start("initializing...");

			final ImmutableMetadata metadata;

			try (Connection conn = obdaModel.getDataSource().getConnection()) {
				MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
				ImmutableList<RelationID> relationIds = metadataProvider.getRelationIDs();

				setMaxTicks(relationIds.size() * 2);
				startLoop(this::getCompletionPercentage, () -> String.format("%d%% completed.", getCompletionPercentage()));

				CachingMetadataLookup lookup = new CachingMetadataLookup(metadataProvider);
				for (RelationID id : relationIds) {
					lookup.getRelation(id);
					tick();
				}
				metadata = lookup.extractImmutableMetadata();
			}

			Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();

			ImmutableList.Builder<SQLPPTriplesMap> builder = ImmutableList.builder();
			for (NamedRelationDefinition relation : metadata.getAllRelations()) {
				builder.addAll(directMappingEngine
						.getMapping(relation, baseIri, bnodeTemplateMap, currentMappingIndex));
				tick();
			}

			endLoop("");
			ImmutableList<SQLPPTriplesMap> triplesMaps = builder.build();
			end();
			return triplesMaps;
		}

		@Override
		public void done() {
			try {
				ImmutableList<SQLPPTriplesMap> triplesMaps = complete();
				// TODO: move back to doInBackground?
				Set<OWLDeclarationAxiom> axioms = obdaModel.insertTriplesMaps(triplesMaps, true);
				DialogUtils.showInfoDialog(getWorkspace(),
						"<html><h3>Bootstrapping the ontology and mapping is complete.</h3><br>" +
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
	public void initialise() { /* NO-OP */ }

	@Override
	public void dispose() {/* NO-OP */ }

}
