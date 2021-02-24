package it.unibz.inf.ontop.protege.core;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.protege.connection.DataSourceListener;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollection;
import it.unibz.inf.ontop.protege.mapping.TriplesMapCollectionListener;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerEventListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class OBDAModelManager implements Disposable {

	private static final Logger LOGGER = LoggerFactory.getLogger(OBDAModelManager.class);

	private final OWLEditorKit owlEditorKit;

	private final List<OBDAModelManagerListener> obdaManagerListeners = new ArrayList<>();

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();

	private final Map<OWLOntologyID, OBDAModel> obdaModels = new HashMap<>();

	@Nullable
	private OBDAModel currentObdaModel;

	private final RDF rdfFactory;

	public OBDAModelManager(OWLEditorKit editorKit) {
		this.owlEditorKit = editorKit;

		getModelManager().addListener(modelManagerListener);
		getOntologyManager().addOntologyChangeListener(new OntologyRefactoringListener());


		/*
		 * TODO: avoid using Default injector
		 */
		OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
				.jdbcDriver("")
				.jdbcUrl("")
				.jdbcUser("")
				.jdbcPassword("")
				.build();

		Injector defaultInjector = configuration.getInjector();

		rdfFactory = defaultInjector.getInstance(RDF.class);
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	@Override
	public void dispose() {
		try {
			getModelManager().removeListener(modelManagerListener);
			connectionManager.dispose();
		}
		catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
	}

	public OBDAModel getCurrentOBDAModel() {
		return currentObdaModel;
	}

	private final List<QueryManagerEventListener> queryManagerEventListeners = new ArrayList<>();

	public void addQueryManagerListener(QueryManagerEventListener listener) {
		if (listener != null && !queryManagerEventListeners.contains(listener))
			queryManagerEventListeners.add(listener);
	}

	private final List<TriplesMapCollectionListener> triplesMapCollectionListeners = new ArrayList<>();

	public void addMappingsListener(TriplesMapCollectionListener listener) {
		if (listener != null && !triplesMapCollectionListeners.contains(listener))
			triplesMapCollectionListeners.add(listener);
	}

	private final List<DataSourceListener> dataSourceListeners = new ArrayList<>();

	public void addDataSourceListener(DataSourceListener listener) {
		if (listener != null && !dataSourceListeners.contains(listener))
			dataSourceListeners.add(listener);
	}

	public void removeDataSourceListener(DataSourceListener listener) {
		dataSourceListeners.remove(listener);
	}


	OWLModelManager getModelManager() {
		return owlEditorKit.getModelManager();
	}

	OWLOntologyManager getOntologyManager() {
		return getModelManager().getOWLOntologyManager();
	}

	OBDAModel getOBDAModel(OWLOntology ontology) {
		return obdaModels.get(ontology.getOntologyID());
	}

	/***
	 * This ontology change listener has some heuristics to determine if the
	 * user is refactoring the ontology. In particular, this listener will try
	 * to determine if some add/remove axioms are in fact a "renaming"
	 * operation. This happens when a list of axioms has a
	 * remove(DeclarationAxiom(x)) immediately followed by an
	 * add(DeclarationAxiom(y)). In this case, y is a new name for x.
	 */
	private class OntologyRefactoringListener implements OWLOntologyChangeListener {

		@Override
		public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
			Map<OWLOntologyID, Map<OWLDeclarationAxiom, OWLDeclarationAxiom>> rename = new HashMap<>();
			Map<OWLOntologyID, Set<OWLDeclarationAxiom>> remove = new HashMap<>();

			for (int idx = 0; idx < changes.size() ; idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					SetOntologyID set = (SetOntologyID)change;
					OWLOntologyID oldId = set.getOriginalOntologyID();
					OBDAModel obdaModel = obdaModels.get(oldId);
					obdaModels.remove(oldId);
					OWLOntologyID newId = set.getNewOntologyID();
					obdaModel.getMutablePrefixManager().updateOntologyID(newId);
					obdaModels.put(newId, obdaModel);
					LOGGER.debug("Ontology ID changed\nOld ID: {}\nNew ID: {}", oldId, newId);
				}
				else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (idx + 1 < changes.size() && changes.get(idx + 1) instanceof AddAxiom) {
						// renaming
						OWLAxiom nextAxiom = changes.get(idx + 1).getAxiom();
						if (axiom instanceof OWLDeclarationAxiom && nextAxiom instanceof OWLDeclarationAxiom) {
							rename.computeIfAbsent(change.getOntology().getOntologyID(), id -> new HashMap<>())
									.put((OWLDeclarationAxiom) axiom, (OWLDeclarationAxiom) nextAxiom);
						}
					}
					else if (axiom instanceof OWLDeclarationAxiom) {
						remove.computeIfAbsent(change.getOntology().getOntologyID(), id -> new HashSet<>())
								.add((OWLDeclarationAxiom) axiom);
					}
				}
			}

			for (Map.Entry<OWLOntologyID, Map<OWLDeclarationAxiom, OWLDeclarationAxiom>> o : rename.entrySet()) {
				TriplesMapCollection tiplesMaps = obdaModels.get(o.getKey()).getTriplesMapCollection();
				for (Map.Entry<OWLDeclarationAxiom, OWLDeclarationAxiom> e : o.getValue().entrySet())
					tiplesMaps.renamePredicate(getIRI(e.getKey()), getIRI(e.getValue()));
			}

			for (Map.Entry<OWLOntologyID, Set<OWLDeclarationAxiom>> o : remove.entrySet()) {
				TriplesMapCollection tiplesMaps = obdaModels.get(o.getKey()).getTriplesMapCollection();
				for (OWLDeclarationAxiom axiom : o.getValue())
					tiplesMaps.removePredicate(getIRI(axiom));
			}
		}

		private org.apache.commons.rdf.api.IRI getIRI(OWLDeclarationAxiom axiom) {
			return rdfFactory.createIRI(axiom.getEntity().getIRI().toString());
		}
	}

	public DisposableProperties getStandardProperties() {
		return OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit);
	}


	public void addListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.remove(listener);
	}




	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			LOGGER.debug(event.getType().name());
			switch (event.getType()) {
				case ONTOLOGY_CREATED: // fired before ACTIVE_ONTOLOGY_CHANGED
					ontologyCreated();
					break;

				case ACTIVE_ONTOLOGY_CHANGED:
					activeOntologyChanged();
					break;

				case ONTOLOGY_LOADED: // fired after ACTIVE_ONTOLOGY_CHANGED
					ontologyLoaded();
					break;

				case ONTOLOGY_SAVED:
					ontologySaved();
					break;

				case ABOUT_TO_CLASSIFY:
				case ONTOLOGY_CLASSIFIED:
				case ONTOLOGY_RELOADED:
				case ENTITY_RENDERER_CHANGED:
				case REASONER_CHANGED:
				case ONTOLOGY_VISIBILITY_CHANGED:
				case ENTITY_RENDERING_CHANGED:
					break;
			}
		}
	}

	private void ontologyCreated() {
		OWLOntology ontology = getModelManager().getActiveOntology();
		createObdaModel(ontology);
	}

	private OBDAModel createObdaModel(OWLOntology ontology) {
		OBDAModel obdaModel = new OBDAModel(ontology, this);
		obdaModel.getDataSource().addListener(s -> dataSourceListeners.forEach(l -> l.dataSourceChanged(s)));
		obdaModel.getTriplesMapCollection().addMappingsListener(s -> triplesMapCollectionListeners.forEach(l -> l.triplesMapCollectionChanged(s)));
		obdaModel.getQueryManager().addListener(new QueryManagerEventListener() {
			@Override
			public void inserted(QueryManager.Item entity, int indexInParent) {
				queryManagerEventListeners.forEach(l -> l.inserted(entity, indexInParent));
			}
			@Override
			public void removed(QueryManager.Item entity, int indexInParent) {
				queryManagerEventListeners.forEach(l -> l.removed(entity, indexInParent));
			}
			@Override
			public void renamed(QueryManager.Item query, int indexInParent) {
				queryManagerEventListeners.forEach(l -> l.renamed(query, indexInParent));
			}
			@Override
			public void changed(QueryManager.Item query, int indexInParent) {
				queryManagerEventListeners.forEach(l -> l.changed(query, indexInParent));
			}
		});
		obdaModels.put(ontology.getOntologyID(), obdaModel);
		return obdaModel;
	}

	private void activeOntologyChanged() {
		OWLOntology ontology = getModelManager().getActiveOntology();
		OBDAModel obdaModel = obdaModels.computeIfAbsent(ontology.getOntologyID(),
				i -> createObdaModel(ontology));

		if (obdaModel == currentObdaModel)
			return;

		currentObdaModel = obdaModel;

		ProtegeOWLReasonerInfo protegeOWLReasonerInfo = getModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
		if (protegeOWLReasonerInfo instanceof OntopReasonerInfo) {
			OntopReasonerInfo reasonerInfo = (OntopReasonerInfo) protegeOWLReasonerInfo;
			reasonerInfo.setConfigurationGenerator(currentObdaModel.getConfigurationManager());
		}

		fireActiveOntologyChanged();
	}

	private void ontologyLoaded() {
		try {
			currentObdaModel.load();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
		}
		finally {
			fireActiveOntologyChanged();
		}
	}

	private void ontologySaved() {
		try {
			currentObdaModel.store();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			Exception newException = new Exception(
					"Error saving the OBDA file. Closing Protege now can result in losing changes " +
							"in your data sources or mappings. Please resolve the issue that prevents " +
							"saving in the current location, or do \"Save as..\" to save in an alternative location. \n\n" +
							"The error message was: \n"  + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
		}
	}

	private void fireActiveOntologyChanged() {
		for (OBDAModelManagerListener listener : obdaManagerListeners) {
			try {
				listener.activeOntologyChanged();
			}
			catch (Exception e) {
				LOGGER.debug("Badly behaved listener: {}", listener.getClass().toString());
				LOGGER.debug(e.getMessage(), e);
			}
		}
	}
}
