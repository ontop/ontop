package it.unibz.inf.ontop.protege.core;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.*;
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

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();

	private final Map<OWLOntologyID, OBDAModel> obdaModels = new HashMap<>();
	private OBDAModel currentObdaModel;

	@Nullable
	private OWLOntologyID lastKnownOntologyId;

	public OBDAModelManager(OWLEditorKit editorKit) {
		this.owlEditorKit = editorKit;

		getModelManager().addListener(modelManagerListener);
		getOntologyManager().addOntologyChangeListener(new OntologyRefactoringListener());
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

	private final ArrayList<QueryManagerEventListener> queryManagerEventListeners = new ArrayList<>();

	public void addQueryManagerListener(QueryManagerEventListener listener) {
		if (listener != null && !queryManagerEventListeners.contains(listener))
			queryManagerEventListeners.add(listener);
	}

	private final ArrayList<TriplesMapCollectionListener> triplesMapCollectionListeners = new ArrayList<>();

	public void addMappingsListener(TriplesMapCollectionListener listener) {
		if (listener != null && !triplesMapCollectionListeners.contains(listener))
			triplesMapCollectionListeners.add(listener);
	}

	private final ArrayList<DataSourceListener> dataSourceListeners = new ArrayList<>();

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

	public OBDAModel getOBDAModel(OWLOntology ontology) {
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
			return getCurrentOBDAModel().getRdfFactory().createIRI(axiom.getEntity().getIRI().toString());
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
		// TODO: clean up code - this one is called from the event dispatch thread
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			LOGGER.debug(event.getType().name());
			LOGGER.debug("CURRENT ACTiVE: " + getModelManager().getActiveOntologies());
			switch (event.getType()) {
				case ABOUT_TO_CLASSIFY: // starting reasoner
					break;

				case ONTOLOGY_CLASSIFIED: // reasoner started
					break;

				case ACTIVE_ONTOLOGY_CHANGED:
					handleNewActiveOntology();
					break;

				case ONTOLOGY_LOADED: // first, ACTIVE_ONT_CHANGED
					handleOntologyLoaded();
					break;

				case ONTOLOGY_SAVED:
					handleOntologySaved();
					break;

				case ONTOLOGY_CREATED: // when a new one is created, followed by ACTIVE_ONT_CHANGED
					handleOntologyCreated();
					break;

				case ONTOLOGY_RELOADED:
				case ENTITY_RENDERER_CHANGED:
				case REASONER_CHANGED:
				case ONTOLOGY_VISIBILITY_CHANGED:
				case ENTITY_RENDERING_CHANGED:
					break;
			}
		}
	}

	private void handleOntologyCreated() {
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

	/**
	 * When the active ontology is new (first one or differs from the last one)
	 */
	private void handleNewActiveOntology() {
		OWLOntology ontology = getModelManager().getActiveOntology();
		OWLOntologyID id = ontology.getOntologyID();
		System.out.println("ACTiVE " + id + " FROM " + obdaModels.keySet());
		currentObdaModel = obdaModels.computeIfAbsent(id, i -> createObdaModel(ontology));

		if (id.equals(lastKnownOntologyId))
			return;

		lastKnownOntologyId = id;

		ProtegeOWLReasonerInfo factory = getModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
		if (factory instanceof OntopReasonerInfo) {
			OntopReasonerInfo questfactory = (OntopReasonerInfo) factory;
			questfactory.setConfigurationGenerator(currentObdaModel.getConfigurationManager());
		}

		fireActiveOBDAModelChange();
	}

	private void handleOntologyLoaded() {
		try {
			currentObdaModel.load();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
		}
		finally {
			fireActiveOBDAModelChange();
		}
	}

	private void handleOntologySaved() {
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

	private void fireActiveOBDAModelChange() {
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
