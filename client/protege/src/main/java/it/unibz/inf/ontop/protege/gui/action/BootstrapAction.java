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
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.MetadataProvider;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.protege.utils.LinearTickerSwingWorker;
import it.unibz.inf.ontop.protege.utils.ProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.semanticweb.owlapi.model.AddAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class BootstrapAction extends ProtegeAction {

	private static final long serialVersionUID = 8671527155950905524L;

	private final Logger log = LoggerFactory.getLogger(BootstrapAction.class);

	private static final String DIALOG_TITLE = "Bootstrapping ontology and mapping";

	@Override
	public void actionPerformed(ActionEvent evt) {

		OBDAModelManager modelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
		MutablePrefixManager prefixManager = modelManager.getActiveOBDAModel().getMutablePrefixManager();

		String defaultBaseIRI = prefixManager.getDefaultIriPrefix()
				.replace("#", "/");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JLabel baseIriLabel = new JLabel("Base IRI - the prefix " +
				"to be used for all generated classes and properties: ");
		baseIriLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriLabel);

		Dimension minsize1 = new Dimension(10, 10);
		panel.add(new Box.Filler(minsize1, minsize1, minsize1));

		JTextField baseIriField = new JTextField(defaultBaseIRI);
		baseIriField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriField);

		Dimension minsize2 = new Dimension(20, 20);
		panel.add(new Box.Filler(minsize2, minsize2, minsize2));

		if (JOptionPane.showOptionDialog(getWorkspace(),
				panel,
				DIALOG_TITLE,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null) != JOptionPane.OK_OPTION)
			return;

		String baseIri0 = baseIriField.getText().trim();
		if (baseIri0.contains("#")) {
			DialogUtils.showPrettyMessageDialog(getWorkspace(),
					"Base IRIs cannot contain '#':\n" +
							baseIri0 + " is not a valid base IRI.",
					DIALOG_TITLE,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String baseIri = DirectMappingEngine.fixBaseURI(
				baseIri0.isEmpty() ? defaultBaseIRI : baseIri0);
		prefixManager.generateUniquePrefixForBootstrapper(baseIri);

		BootstrapWorker worker = new BootstrapWorker(baseIri);
		worker.execute();
	}

	private class BootstrapWorker extends LinearTickerSwingWorker<ImmutableList<SQLPPTriplesMap>, Void> {

		private final String baseIri;
		private final JDBCMetadataProviderFactory metadataProviderFactory;
		private final DirectMappingEngine directMappingEngine;
		private final OntopStandaloneSQLSettings settings;

		private final AtomicInteger currentMappingIndex;

		BootstrapWorker(String baseIri) {
			super(new ProgressMonitor(getWorkspace(),
					"<html><h3>Bootstrapping ontology and mapping:</h3></html>",
					false));

			this.baseIri = baseIri;

			OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
			OntopSQLOWLAPIConfiguration configuration = obdaModelManager.getConfigurationForOntology();
			this.settings = configuration.getSettings();

			Injector injector = configuration.getInjector();
			this.metadataProviderFactory = injector.getInstance(JDBCMetadataProviderFactory.class);
			this.directMappingEngine = injector.getInstance(DirectMappingEngine.class);

			OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
			this.currentMappingIndex = new AtomicInteger(obdaModel.getMapping().size() + 1);
		}

		@Override
		protected ImmutableList<SQLPPTriplesMap> doInBackground() throws Exception {

			start("initializing...");

			final ImmutableMetadata metadata;

			JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();
			try (Connection conn = connectionManager.getConnection(settings)) {

				MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
				ImmutableList<RelationID> relationIds = metadataProvider.getRelationIDs();

				setMaxTicks(relationIds.size() * 2);
				startLoop(() -> String.format("%d%% completed.", getCompletionPercentage()));

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
			end();
			return builder.build();
		}

		@Override
		public void done() {
			OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
			OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
			try {
				ImmutableList<SQLPPTriplesMap> triplesMaps = complete();
				try {
					obdaModel.add(triplesMaps);
				}
				catch (DuplicateMappingException e) {
					JOptionPane.showMessageDialog(getWorkspace(),
							"<html><b>Duplicate mapping IDs:</b>" +
									HTML_TAB + e.getMessage() + "</html>",
							DIALOG_TITLE,
							JOptionPane.ERROR_MESSAGE);
				}

				List<AddAxiom> addAxioms = obdaModelManager
						.insertOntologyDeclarations(triplesMaps, true);

				JOptionPane.showMessageDialog(getWorkspace(),
						"<html><h3>Bootstrapping the ontology and mapping is complete.</h3><br>" +
								HTML_TAB + "<b>" + triplesMaps.size() + "</b> triple maps inserted into the mapping.<br>" +
								HTML_TAB + "<b>" + addAxioms.size() + "</b> declaration axioms (re)inserted into the ontology.<br></html>",
						DIALOG_TITLE,
						JOptionPane.INFORMATION_MESSAGE);
			}
			catch (CancellationException | InterruptedException e) {
				DialogUtils.showCancelledActionDialog(getWorkspace(), DIALOG_TITLE);
			}
			catch (ExecutionException e) {
				DialogUtils.showErrorDialog(getWorkspace(), DIALOG_TITLE, "Bootstrapper error.", log, e, settings);
			}
		}
	}


	@Override
	public void initialise() { /* NO-OP */ }

	@Override
	public void dispose() {/* NO-OP */ }

}
