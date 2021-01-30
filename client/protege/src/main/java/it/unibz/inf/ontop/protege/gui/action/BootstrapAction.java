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
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.protege.utils.OBDAProgressListener;
import it.unibz.inf.ontop.protege.utils.OBDAProgressMonitor;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BootstrapAction extends ProtegeAction {

	private static final long serialVersionUID = 8671527155950905524L;

	private final Logger log = LoggerFactory.getLogger(BootstrapAction.class);

	@Override
	public void actionPerformed(ActionEvent evt) {

		OBDAModelManager modelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
		MutablePrefixManager prefixManager = modelManager.getActiveOBDAModel().getMutablePrefixManager();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JLabel baseIriLabel = new JLabel(
				"Base IRI - the prefix to be used for all generated classes and properties: ");
		baseIriLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriLabel);
		Dimension minsize1 = new Dimension(10, 10);
		panel.add(new Box.Filler(minsize1, minsize1, minsize1));
		JTextField baseIriField = new JTextField();
		baseIriField.setText(prefixManager.getDefaultIriPrefix()
				.replace("#", "/"));
		baseIriField.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(baseIriField);
		Dimension minsize2 = new Dimension(20, 20);
		panel.add(new Box.Filler(minsize2, minsize2, minsize2));

		if (JOptionPane.showOptionDialog(getWorkspace(),
				panel,
				"Bootstrapping base IRI",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null) != JOptionPane.OK_OPTION)
			return;

		String baseIri = baseIriField.getText().trim();
		if (baseIri.contains("#")) {
			JOptionPane.showMessageDialog(getWorkspace(),
					"Base IRI cannot contain '#'",
					"Bootstrapping error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String bootstrapPrefix = "g:";
		Map<String, String> map = prefixManager.getPrefixMap();
		while (map.containsKey(bootstrapPrefix)) {
			bootstrapPrefix = "g" + bootstrapPrefix;
		}
		prefixManager.addPrefix(bootstrapPrefix, baseIri);

		Thread thread = new Thread("Bootstrapper Action Thread") {
			@Override
			public void run() {
				try {
					OBDAProgressMonitor monitor = new OBDAProgressMonitor(
							"Bootstrapping ontology and mappings...", getWorkspace());
					BootstrapperThread t = new BootstrapperThread();
					monitor.addProgressListener(t);
					monitor.start();
					t.run(baseIri);
					monitor.stop();
					JOptionPane.showMessageDialog(getWorkspace(),
							"Bootstrapping completed.",
							"Done",
							JOptionPane.INFORMATION_MESSAGE);

					SwingUtilities.invokeLater(() -> {
						getWorkspace().getTopLevelAncestor().repaint();
					});
				}
				catch (Throwable e) {
					DialogUtils.showSeeLogErrorDialog(getWorkspace(), "Bootstrapping error.", log, e);
				}
			}
		};
		thread.start();
	}

	// TODO: not a thread
	private class BootstrapperThread implements OBDAProgressListener {

		public void run(String baseIri0) throws Exception {
			OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getEditorKit());
			OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
			String baseIri = (baseIri0 == null || baseIri0.isEmpty())
					? obdaModel.getMutablePrefixManager().getDefaultIriPrefix()
					: DirectMappingEngine.fixBaseURI(baseIri0);

			OWLModelManager modelManager = ((OWLEditorKit)getEditorKit()).getModelManager();
			OntopSQLOWLAPIConfiguration configuration = obdaModelManager
					.getConfigurationManager()
					.buildOntopSQLOWLAPIConfiguration(modelManager.getActiveOntology());
			Injector injector = configuration.getInjector();

			JDBCConnectionManager connManager = JDBCConnectionManager.getJDBCConnectionManager();
			try (Connection conn = connManager.getConnection(configuration.getSettings())) {
				JDBCMetadataProviderFactory metadataProviderFactory = injector.getInstance(JDBCMetadataProviderFactory.class);
				MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
				// this operation is EXPENSIVE
				ImmutableList<NamedRelationDefinition> relations = ImmutableMetadata.extractImmutableMetadata(metadataProvider).getAllRelations();

				Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
				AtomicInteger currentMappingIndex = new AtomicInteger(obdaModel.getMapping().size() + 1);

				DirectMappingEngine directMappingEngine = injector.getInstance(DirectMappingEngine.class);
				ImmutableList<SQLPPTriplesMap> sqlppTriplesMaps = relations.stream()
						.flatMap(td -> directMappingEngine.getMapping(td, baseIri, bnodeTemplateMap, currentMappingIndex).stream())
						.collect(ImmutableCollectors.toList());

				try {
					obdaModel.add(sqlppTriplesMaps);
				}
				catch (DuplicateMappingException e) {
					JOptionPane.showMessageDialog(getWorkspace(), "Duplicate mapping id found: " + e.getLocalizedMessage());
				}

				// update protege ontology
				OWLOntologyManager manager = modelManager.getActiveOntology().getOWLOntologyManager();
				TypeFactory typeFactory = obdaModelManager.getTypeFactory();
				Set<OWLDeclarationAxiom> declarationAxioms = MappingOntologyUtils.extractDeclarationAxioms(
						manager,
						sqlppTriplesMaps.stream()
								.flatMap(ax -> ax.getTargetAtoms().stream()),
						typeFactory,
						true);

				List<AddAxiom> addAxioms = declarationAxioms.stream()
						.map(ax -> new AddAxiom(modelManager.getActiveOntology(), ax))
						.collect(Collectors.toList());

				modelManager.applyChanges(addAxioms);
			}
			catch (SQLException e) {
				throw new RuntimeException("JDBC connection is missing, have you setup Ontop Mapping properties?\n" +
						"Message: " + e.getMessage());
			}
		}

		@Override
		public void actionCanceled()  { }

		@Override
		public boolean isCancelled() { return false; }

		@Override
		public boolean isErrorShown() { return false; }
	}


	@Override
	public void initialise() { /* NO-OP */ }

	@Override
	public void dispose() {/* NO-OP */ }

}
