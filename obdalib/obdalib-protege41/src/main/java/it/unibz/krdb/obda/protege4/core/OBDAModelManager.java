package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAMappingListener;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelRefactorer;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerListener;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;
import it.unibz.krdb.obda.utils.VersionInfo;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAModelManager implements Disposable {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	OWLEditorKit owlEditorKit = null;

	OWLOntologyManager mmgr = null;

	// OBDAModel obdacontroller = null;

	Map<URI, OBDAModel> obdamodels = null;

	private List<OBDAModelManagerListener> obdaManagerListeners = null;

	private JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener modelManagerListener = new OBDAPLuginOWLModelManagerListener();

	private ProtegeQueryControllerListener qlistener = new ProtegeQueryControllerListener();
	private ProtegeMappingControllerListener mlistener = new ProtegeMappingControllerListener();
	private ProtegeDatasourcesControllerListener dlistener = new ProtegeDatasourcesControllerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	private boolean loadingData;

	public OBDAModelManager(EditorKit editorKit) {
		super();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA PLugin only works with OWLEditorKit instances.");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;
		mmgr = ((OWLModelManager) owlEditorKit.getModelManager()).getOWLOntologyManager();
		OWLModelManager owlmmgr = (OWLModelManager) editorKit.getModelManager();
		owlmmgr.addListener(modelManagerListener);
		obdaManagerListeners = new LinkedList<OBDAModelManagerListener>();
		obdamodels = new HashMap<URI, OBDAModel>();

		// Adding ontology change listeners to synchronize with the mappings
		mmgr.addOntologyChangeListener(new OntologyRefactoringListener());

		// Printing the version information to the console
		System.out.println("Using " + VersionInfo.getVersionInfo().toString() + "\n");
	}

	/***
	 * This ontology change listener has some euristics that determine if the
	 * user is refactoring his ontology. In particular, this listener will try
	 * to determine if some add/remove axioms are in fact a "renaming"
	 * operation. This happens when a list of axioms has a
	 * remove(DeclarationAxiom(x)) immediatly followed by an
	 * add(DeclarationAxiom(y)), in this case, y is a renaming for x.
	 * 
	 * @author mariano
	 * 
	 */
	public class OntologyRefactoringListener implements OWLOntologyChangeListener {

		OWLAPI3Translator translator = new OWLAPI3Translator();

		@Override
		public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
			Map<OWLEntity, OWLEntity> renamings = new HashMap<OWLEntity, OWLEntity>();
			Set<OWLEntity> removals = new HashSet<OWLEntity>();

			for (int idx = 0; idx < changes.size(); idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					IRI newiri = ((SetOntologyID) change).getNewOntologyID().getOntologyIRI();
					IRI oldiri = ((SetOntologyID) change).getOriginalOntologyID().getOntologyIRI();
					OBDAModel model = obdamodels.get(oldiri.toURI());
					PrefixManager prefixManager = model.getPrefixManager();
					prefixManager.setDefaultNamespace(newiri.toURI().toString());
					
					obdamodels.remove(oldiri.toURI());
					obdamodels.put(newiri.toURI(), model);
					continue;
				}

				if (idx + 1 >= changes.size())
					continue;
				if (change instanceof RemoveAxiom && changes.get(idx + 1) instanceof AddAxiom) {
					/***
					 * Found the pattern of a renaming refactoring
					 */
					RemoveAxiom remove = (RemoveAxiom) change;
					AddAxiom add = (AddAxiom) changes.get(idx + 1);

					if (!(remove.getAxiom() instanceof OWLDeclarationAxiom && add.getAxiom() instanceof OWLDeclarationAxiom))
						continue;

					/*
					 * we found the patter we are looking for, a remove and add
					 * of declaration axioms
					 */
					OWLEntity olde = ((OWLDeclarationAxiom) remove.getAxiom()).getEntity();
					OWLEntity newe = ((OWLDeclarationAxiom) add.getAxiom()).getEntity();
					renamings.put(olde, newe);
				} else if (change instanceof RemoveAxiom && ((RemoveAxiom) change).getAxiom() instanceof OWLDeclarationAxiom) {
					/***
					 * Found the pattern of a deletion
					 */
					OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) ((RemoveAxiom) change).getAxiom();
					OWLEntity removedEntity = declaration.getEntity();
					removals.add(removedEntity);

				}
			}

			/***
			 * Applying the renamings to the OBDA model
			 */
			OBDAModel obdamodel = getActiveOBDAModel();
			for (OWLEntity olde : renamings.keySet()) {
				OWLEntity removedEntity = olde;
				OWLEntity newEntity = renamings.get(removedEntity);
				/*
				 * This set of changes appears to be a "renaming" operation,
				 * hence we will modify the OBDA model acordingly
				 */
				Predicate removedPredicate = translator.getPredicate(removedEntity);
				Predicate newPredicate = translator.getPredicate(newEntity);

				obdamodel.renamePredicate(removedPredicate, newPredicate);
			}

			/***
			 * Applying the deletions to the obda model
			 */
			for (OWLEntity removede : removals) {
				Predicate removedPredicate = translator.getPredicate(removede);
				obdamodel.deletePredicate(removedPredicate);

			}

		}

	}

	public void addListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.remove(listener);
	}

	public OBDAModel getActiveOBDAModel() {
		OWLOntology ontology = owlEditorKit.getOWLModelManager().getActiveOntology();
		if (ontology != null) {
			OWLOntologyID ontologyID = ontology.getOntologyID();
			IRI ontologyIRI = ontologyID.getOntologyIRI();
			URI uri = null;
			if (ontologyIRI != null)
				uri = ontologyIRI.toURI();
			else
				uri = URI.create(ontologyID.toString());

			return obdamodels.get(uri);
		}
		return null;
	}

	/***
	 * This method makes sure is used to setup a new/fresh OBDA model. This is
	 * done by replacing the instance this.obdacontroller (the OBDA model) with
	 * a new object. On creation listeners for the datasources, mappings and
	 * queries are setup so that changes in these trigger and ontology change.
	 * 
	 * Additionally, this method configures all available OBDAOWLReasonerFacotry
	 * objects to have a reference to the newly created OBDA model and to the
	 * global preference object. This is necessary so that the factories are
	 * able to pass the OBDA model to the reasoner instances when they are
	 * created.
	 */
	private void setupNewOBDAModel() {
		OBDAModel activeOBDAModel = getActiveOBDAModel();

		if (activeOBDAModel != null) {
			return;
		}
		OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
		activeOBDAModel = obdafac.getOBDAModel();

		activeOBDAModel.addSourcesListener(dlistener);
		activeOBDAModel.addMappingsListener(mlistener);
		activeOBDAModel.getQueryController().addListener(qlistener);

		OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();
		PrefixOWLOntologyFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(mmgr.getActiveOntology());
		addOBDADefaultPrefixes(prefixManager);

		// PrefixManager mapper =mmgr.getOWLOntologyManager();

		OWLOntologyID ontologyID = this.owlEditorKit.getModelManager().getActiveOntology().getOntologyID();
		URI uri = null;
		if (ontologyID.isAnonymous()) {
			uri = URI.create(ontologyID.toString());
		} else {
			uri = ontologyID.getOntologyIRI().toURI();
		}
		obdamodels.put(uri, activeOBDAModel);

		PrefixManagerWrapper prefixwrapper = new PrefixManagerWrapper(prefixManager);
		activeOBDAModel.setPrefixManager(prefixwrapper);

		String defaultPrefix = prefixManager.getDefaultPrefix();
		if (defaultPrefix != null)
			activeOBDAModel.getPrefixManager().setDefaultNamespace(defaultPrefix);
		else
			activeOBDAModel.getPrefixManager().setDefaultNamespace(ontologyID.getOntologyIRI().toURI().toString());

		// triggerOBDAModelChangeEvent(oldcontroller, obdacontroller);
	}

	/**
	 * Append here all default prefixes used by the system.
	 */
	private void addOBDADefaultPrefixes(PrefixOWLOntologyFormat prefixManager) {
		if (!prefixManager.containsPrefixMapping("quest")) {
			prefixManager.setPrefix("quest", "http://obda.org/quest#");
		}
	}

	/***
	 * Internal class responsible for coordinating actions related to updates in
	 * the ontology environment.
	 */
	private class OBDAPLuginOWLModelManagerListener implements OWLModelManagerListener {

		public boolean inititializing = false;

		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			EventType type = event.getType();
			OWLModelManager source = event.getSource();

			switch (type) {
			case ABOUT_TO_CLASSIFY:
				log.debug("ABOUT TO CLASSIFY");
				// if ((!inititializing) && (obdamodels != null) &&
				// (owlEditorKit != null) && (getActiveOBDAModel() != null)) {
				// OWLReasoner reasoner =
				// owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasoner();
				// if (reasoner instanceof QuestOWL) {
				// QuestOWL quest = (QuestOWL) reasoner;
				// ProtegeReformulationPlatformPreferences reasonerPreference =
				// (ProtegeReformulationPlatformPreferences) owlEditorKit
				// .get(QuestPreferences.class.getName());
				// quest.setPreferences(reasonerPreference);
				// quest.loadOBDAModel(getActiveOBDAModel());
				// }
				// }
				break;

			case ENTITY_RENDERER_CHANGED:
				log.debug("RENDERER CHANGED");
				break;

			case ONTOLOGY_CLASSIFIED:
				break;

			case ACTIVE_ONTOLOGY_CHANGED:
				log.debug("ACTIVE ONTOLOGY CHANGED");
				inititializing = true;
				setupNewOBDAModel();

				OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

				PrefixOWLOntologyFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(mmgr.getActiveOntology());
				String defaultPrefix = prefixManager.getDefaultPrefix();
				OBDAModel activeOBDAModel = getActiveOBDAModel();
				OWLOntology ontology = owlEditorKit.getOWLModelManager().getActiveOntology();
				OWLOntologyID ontologyID = ontology.getOntologyID();
				if (defaultPrefix != null)
					activeOBDAModel.getPrefixManager().setDefaultNamespace(defaultPrefix);
				else
					activeOBDAModel.getPrefixManager().setDefaultNamespace(ontologyID.getOntologyIRI().toURI().toString());

				ProtegeOWLReasonerInfo factory = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
				if (factory instanceof ProtegeOBDAOWLReformulationPlatformFactory) {
					ProtegeOBDAOWLReformulationPlatformFactory questfactory = (ProtegeOBDAOWLReformulationPlatformFactory) factory;
					ProtegeReformulationPlatformPreferences reasonerPreference = (ProtegeReformulationPlatformPreferences) owlEditorKit
							.get(QuestPreferences.class.getName());
					questfactory.setPreferences(reasonerPreference);
					questfactory.setOBDAModel(getActiveOBDAModel());
				}

				fireActiveOBDAModelChange();

				inititializing = false;
				break;

			case ENTITY_RENDERING_CHANGED:
				break;

			case ONTOLOGY_CREATED:
				log.debug("ONTOLOGY CREATED");
				break;

			case ONTOLOGY_LOADED:
				log.debug("ONTOLOGY LOADED");
				loadingData = true;
				try {
					OWLOntology activeonto = source.getActiveOntology();

					URI owlfile = source.getOWLOntologyManager().getOntologyDocumentIRI(activeonto).toURI();
					URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");
					getActiveOBDAModel().getPrefixManager().setDefaultNamespace(
							activeonto.getOntologyID().getOntologyIRI().toURI().toString());
					DataManager ioManager = new DataManager(getActiveOBDAModel());
					File file = new File(obdafile);
					if (file.canRead()) {
						final OBDAModel obdaModel = getActiveOBDAModel();
						ioManager.loadOBDADataFromURI(obdafile, activeonto.getOntologyID().getOntologyIRI().toURI(),
								obdaModel.getPrefixManager());
						// connectionManager.setupConnection(obdaModel); // fill
						// in
						// the
						// connection
						// pool.
						OBDAModelRefactorer refactorer = new OBDAModelRefactorer(obdaModel, activeonto);
						refactorer.run(); // adding type information to the
											// mapping predicates.
					}
				} catch (Exception e) {
					log.error(e.getMessage());
				} finally {
					loadingData = false;
				}
				break;

			case ONTOLOGY_RELOADED:
				log.debug("ONTOLOGY RELOADED");
				loadingData = true;
				try {
					OWLOntology activeonto = source.getActiveOntology();
					URI owlfile = source.getOWLOntologyManager().getOntologyDocumentIRI(activeonto).toURI();
					URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");
					getActiveOBDAModel().getPrefixManager().setDefaultNamespace(
							activeonto.getOntologyID().getOntologyIRI().toURI().toString());
					DataManager ioManager = new DataManager(getActiveOBDAModel());
					File file = new File(obdafile);
					if (file.canRead()) {
						final OBDAModel obdaModel = getActiveOBDAModel();
						ioManager.loadOBDADataFromURI(obdafile, activeonto.getOntologyID().getOntologyIRI().toURI(),
								obdaModel.getPrefixManager());
						// connectionManager.setupConnection(obdaModel); // fill
						// in
						// the
						// connection
						// pool.
						OBDAModelRefactorer refactorer = new OBDAModelRefactorer(obdaModel, activeonto);
						refactorer.run(); // adding type information to the
											// mapping predicates.
					}

				} catch (Exception e) {
					log.error(e.getMessage());
				} finally {
					loadingData = false;
				}
				break;

			case ONTOLOGY_SAVED:
				log.debug("ACTIVE ONTOLOGY SAVED");
				OWLOntology activeonto = source.getActiveOntology();
				URI owlfile = source.getOWLOntologyManager().getOntologyDocumentIRI(activeonto).toURI();
				URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");
				File file = new File(obdafile);
				try {
					if (!file.exists()) {
						file.createNewFile();
					}
					if (file.canWrite()) {
						DataManager ioManager = new DataManager(getActiveOBDAModel());
						ioManager.saveOBDAData(obdafile, true, getActiveOBDAModel().getPrefixManager());
					} else {
						log.warn("WARNING: Cannot perform write operation");
					}
				} catch (IOException e) {
					log.error("Error while saving OBDA file: {}", obdafile.toString());
					log.error(e.getMessage());
				}
				break;

			case ONTOLOGY_VISIBILITY_CHANGED:
				log.debug("VISIBILITY CHANGED");
				break;

			case REASONER_CHANGED:
				log.info("REASONER CHANGED");
				if ((!inititializing) && (obdamodels != null) && (owlEditorKit != null) && (getActiveOBDAModel() != null)) {

					ProtegeOWLReasonerInfo fac = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
					if (fac instanceof ProtegeOBDAOWLReformulationPlatformFactory) {
						ProtegeOBDAOWLReformulationPlatformFactory questfactory = (ProtegeOBDAOWLReformulationPlatformFactory) fac;
						ProtegeReformulationPlatformPreferences reasonerPreference = (ProtegeReformulationPlatformPreferences) owlEditorKit
								.get(QuestPreferences.class.getName());
						questfactory.setPreferences(reasonerPreference);
						questfactory.setOBDAModel(getActiveOBDAModel());
					}
					break;
				}
			}
		}

	}

	public void fireActiveOBDAModelChange() {
		for (OBDAModelManagerListener listener : obdaManagerListeners) {
			try {
				listener.activeOntologyChanged();
			} catch (Exception e) {
				log.debug("Badly behaved listener: {}", listener.getClass().toString());
				log.debug(e.getMessage(), e);
			}
		}
	}

	/***
	 * Protege wont trigger a save action unless it detects that the OWLOntology
	 * currently opened has suffered a change. The OBDA plugin requires that
	 * protege triggers a save action also in the case when only the OBDA model
	 * has suffered chagnes. To acomplish this, this method will "fake" an
	 * ontology change by inserting and removing a class into the OWLModel.
	 * 
	 */
	private void triggerOntologyChanged() {
		if (loadingData)
			return;
		OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
		OWLOntology ontology = owlmm.getActiveOntology();

		if (ontology == null)
			return;
		
		owlmm.setDirty(ontology);
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	public void dispose() throws Exception {
		try {
			owlEditorKit.getModelManager().removeListener(getModelManagerListener());
			connectionManager.dispose();
		} catch (Exception e) {
			log.warn(e.getMessage());
		}

	}

	protected OWLModelManagerListener getModelManagerListener() {
		return modelManagerListener;
	}

	/***
	 * The following are internal helpers that dispatch "needs save" messages to
	 * the OWL ontology model when OBDA model changes.
	 */

	private class ProtegeDatasourcesControllerListener implements OBDAModelListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1633463551656406417L;

		public void datasourceUpdated(String oldname, OBDADataSource currendata) {
			triggerOntologyChanged();
		}

		public void datasourceDeleted(OBDADataSource source) {
			triggerOntologyChanged();
		}

		public void datasourceAdded(OBDADataSource source) {
			triggerOntologyChanged();
		}

		public void alldatasourcesDeleted() {
			triggerOntologyChanged();
		}

		public void datasourcParametersUpdated() {
			triggerOntologyChanged();
		}
	}

	private class ProtegeMappingControllerListener implements OBDAMappingListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5794145688669702879L;

		public void allMappingsRemoved() {
			triggerOntologyChanged();
		}

		public void currentSourceChanged(URI oldsrcuri, URI newsrcuri) {
			// Do nothing!
		}

		public void mappingDeleted(URI srcuri, String mapping_id) {
			triggerOntologyChanged();
		}

		public void mappingInserted(URI srcuri, String mapping_id) {
			triggerOntologyChanged();
		}

		public void mappingUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
			triggerOntologyChanged();
		}
	}

	private class ProtegeQueryControllerListener implements QueryControllerListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4536639410306364312L;

		public void elementAdded(QueryControllerEntity element) {
			triggerOntologyChanged();
		}

		public void elementAdded(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}

		public void elementRemoved(QueryControllerEntity element) {
			triggerOntologyChanged();
		}

		public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}

		public void elementChanged(QueryControllerQuery query) {
			triggerOntologyChanged();
		}

		public void elementChanged(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}
	}

}
