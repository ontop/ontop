package it.unibz.inf.ontop.protege.core;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.protege.mapping.TriplesMapManager;
import it.unibz.inf.ontop.protege.mapping.TriplesMapManagerListener;
import it.unibz.inf.ontop.protege.query.QueryManager;
import it.unibz.inf.ontop.protege.query.QueryManagerListener;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.EventListenerList;
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

	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();
	private final OWLOntologyChangeListener ontologyManagerListener = new OntologyRefactoringListener();

	private final Map<OWLOntologyID, OBDAModel> obdaModels = new HashMap<>();

	@Nullable
	private OBDAModel currentObdaModel;

	private final RDF rdfFactory;

	public OBDAModelManager(OWLEditorKit editorKit) {
		this.owlEditorKit = editorKit;

		getModelManager().addListener(modelManagerListener);
		getModelManager().getOWLOntologyManager().addOntologyChangeListener(ontologyManagerListener);

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
		getModelManager().getOWLOntologyManager().removeOntologyChangeListener(ontologyManagerListener);
		getModelManager().removeListener(modelManagerListener);
		obdaModels.values().forEach(OBDAModel::dispose);
	}

	@Nonnull
	public OBDAModel getCurrentOBDAModel() {
		if (currentObdaModel == null)
			throw new MinorOntopInternalBugException("No current OBDA Model");

		return currentObdaModel;
	}

	// QueryManagerListener
	private final EventListenerList<QueryManagerListener> queryManagerEventListeners = new EventListenerList<>();

	public void addQueryManagerListener(QueryManagerListener listener) {
		queryManagerEventListeners.add(listener);
	}

	public void removeQueryManagerListener(QueryManagerListener listener) {
		queryManagerEventListeners.remove(listener);
	}

	// TriplesMapManagerListener
	private final EventListenerList<TriplesMapManagerListener> triplesMapCollectionListeners = new EventListenerList<>();

	public void addMappingListener(TriplesMapManagerListener listener) {
		triplesMapCollectionListeners.add(listener);
	}

	public void removeMappingListener(TriplesMapManagerListener listener) {
		triplesMapCollectionListeners.remove(listener);
	}

	// OBDAModelManagerListener
	private final EventListenerList<OBDAModelManagerListener> listeners = new EventListenerList<>();

	public void addListener(OBDAModelManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		listeners.remove(listener);
	}




	OWLModelManager getModelManager() {
		return owlEditorKit.getModelManager();
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
				TriplesMapManager tiplesMaps = obdaModels.get(o.getKey()).getTriplesMapManager();
				for (Map.Entry<OWLDeclarationAxiom, OWLDeclarationAxiom> e : o.getValue().entrySet())
					tiplesMaps.renamePredicate(getIRI(e.getKey()), getIRI(e.getValue()));
			}

			for (Map.Entry<OWLOntologyID, Set<OWLDeclarationAxiom>> o : remove.entrySet()) {
				TriplesMapManager tiplesMaps = obdaModels.get(o.getKey()).getTriplesMapManager();
				for (OWLDeclarationAxiom axiom : o.getValue())
					tiplesMaps.removePredicate(getIRI(axiom));
			}
		}

		private org.apache.commons.rdf.api.IRI getIRI(OWLDeclarationAxiom axiom) {
			return rdfFactory.createIRI(axiom.getEntity().getIRI().toString());
		}
	}



	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			try {
				LOGGER.debug(event.getType().name());
				setUpReasonerInfo();
				switch (event.getType()) {
					case ONTOLOGY_CREATED: // fired before ACTIVE_ONTOLOGY_CHANGED
						ontologyCreated();
						break;
	
					case ACTIVE_ONTOLOGY_CHANGED:
						activeOntologyChanged();
						break;
	
					case ONTOLOGY_LOADED: // fired after ACTIVE_ONTOLOGY_CHANGED
					case ONTOLOGY_RELOADED:
						ontologyLoadedReloaded();
						break;
	
					case ONTOLOGY_SAVED:
						ontologySaved();
						break;
	
					case REASONER_CHANGED:
					case ABOUT_TO_CLASSIFY:
					case ONTOLOGY_CLASSIFIED:
					case ENTITY_RENDERER_CHANGED:
					case ONTOLOGY_VISIBILITY_CHANGED:
					case ENTITY_RENDERING_CHANGED:
						break;
				}
			} catch (Throwable ex) {
				// Report the error, either using the logging system or by directly writing on
				// STDERR (an error here will crash the Ontop Protégé plugin, so it's important to
				// report it as otherwise there will be no debugging info to work on)
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("", ex);
				} else {
					ex.printStackTrace();
				}
				
				// Propagate the error
				Throwables.throwIfUnchecked(ex);
				throw new RuntimeException(ex);
			}
		}
	}

	private void setUpReasonerInfo() {
		ProtegeOWLReasonerInfo protegeOWLReasonerInfo = getModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
		if (protegeOWLReasonerInfo instanceof OntopReasonerInfo) {
			OntopReasonerInfo reasonerInfo = (OntopReasonerInfo) protegeOWLReasonerInfo;
			reasonerInfo.setOBDAModelManager(this);
		}
	}

	private void ontologyCreated() {
		OWLOntology ontology = getModelManager().getActiveOntology();
		createObdaModel(ontology);
	}

	private OBDAModel createObdaModel(OWLOntology ontology) {
		OBDAModel obdaModel = new OBDAModel(ontology, this);
		//obdaModel.getDataSource().addListener(s -> dataSourceListeners.fire(l -> l.dataSourceChanged(s)));
		obdaModel.getTriplesMapManager().addListener(s -> triplesMapCollectionListeners.fire(l -> l.changed(s)));
		obdaModel.getQueryManager().addListener(new QueryManagerListener() {
			@Override
			public void inserted(QueryManager.Item entity, int indexInParent) {
				queryManagerEventListeners.fire(l -> l.inserted(entity, indexInParent));
			}
			@Override
			public void removed(QueryManager.Item entity, int indexInParent) {
				queryManagerEventListeners.fire(l -> l.removed(entity, indexInParent));
			}
			@Override
			public void renamed(QueryManager.Item entity, int indexInParent) {
				queryManagerEventListeners.fire(l -> l.renamed(entity, indexInParent));
			}
			@Override
			public void changed(QueryManager.Item query, int indexInParent) {
				queryManagerEventListeners.fire(l -> l.changed(query, indexInParent));
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

		listeners.fire(l -> l.activeOntologyChanged(getCurrentOBDAModel()));
	}

	private void ontologyLoadedReloaded() {
		try {
			getCurrentOBDAModel().clear();
			getCurrentOBDAModel().load();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
			DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
		}
		finally {
			listeners.fire(l -> l.activeOntologyChanged(getCurrentOBDAModel()));
		}
	}

	private void ontologySaved() {
		try {
			getCurrentOBDAModel().store();
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
}
