package it.unibz.krdb.obda.protege4.core;

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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAMappingListener;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.PropertyExpression;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelValidator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.protege4.utils.DialogUtils;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerEntity;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerListener;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.ImplicitDBConstraints;

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
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObjectProperty;
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

	private static final String OBDA_EXT = "obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = "q"; // The default query file extension.
	private static final String DBPREFS_EXT = "db_prefs"; // The default db_prefs (currently only user constraints) file extension.

	private OWLEditorKit owlEditorKit;

	private OWLOntologyManager mmgr;

	private QueryController queryController;

	private Map<URI, OBDAModel> obdamodels;

	private List<OBDAModelManagerListener> obdaManagerListeners;

	private JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private boolean applyUserConstraints = false;
	private ImplicitDBConstraints userConstraints;
	
	private static Logger log = LoggerFactory.getLogger(OBDAModelManager.class);

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

		// Initialize the query controller
		queryController = new QueryController();

		// Printing the version information to the console
		//	System.out.println("Using " + VersionInfo.getVersionInfo().toString() + "\n");
	}

	/***
	 * This ontology change listener has some euristics that determine if the
	 * user is refactoring his ontology. In particular, this listener will try
	 * to determine if some add/remove axioms are in fact a "renaming"
	 * operation. This happens when a list of axioms has a
	 * remove(DeclarationAxiom(x)) immediatly followed by an
	 * add(DeclarationAxiom(y)), in this case, y is a renaming for x.
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

					if (newiri == null)
						continue;

					IRI oldiri = ((SetOntologyID) change).getOriginalOntologyID().getOntologyIRI();

					log.debug("Ontology ID changed");
					log.debug("Old ID: {}", oldiri);
					log.debug("New ID: {}", newiri);

					OBDAModel model = obdamodels.get(oldiri.toURI());

					if (model == null) {
						setupNewOBDAModel();
						model = getActiveOBDAModel();
					}

					PrefixManager prefixManager = model.getPrefixManager();
					prefixManager.addPrefix(PrefixManager.DEFAULT_PREFIX, newiri.toURI().toString());

					obdamodels.remove(oldiri.toURI());
					obdamodels.put(newiri.toURI(), model);
					continue;

				} else if (change instanceof AddAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						OBDAModel activeOBDAModel = getActiveOBDAModel();
						if (entity instanceof OWLClass) {
							OWLClass oc = (OWLClass) entity;
							OClass c = ofac.createClass(oc.getIRI().toString());
							activeOBDAModel.declareClass(c);
						} else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							PropertyExpression r = ofac.createObjectProperty(or.getIRI().toString());
							activeOBDAModel.declareObjectProperty(r);
						} else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							PropertyExpression p = ofac.createDataProperty(op.getIRI().toString());
							activeOBDAModel.declareDataProperty(p);
						}
					}

				} else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						OBDAModel activeOBDAModel = getActiveOBDAModel();
						if (entity instanceof OWLClass) {
							OWLClass oc = (OWLClass) entity;
							OClass c = ofac.createClass(oc.getIRI().toString());
							activeOBDAModel.unDeclareClass(c);
						} else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							PropertyExpression r = ofac.createObjectProperty(or.getIRI().toString());
							activeOBDAModel.unDeclareObjectProperty(r);
						} else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							PropertyExpression p = ofac.createDataProperty(op.getIRI().toString());
							activeOBDAModel.unDeclareDataProperty(p);
						}
					}
				}

				if (idx + 1 >= changes.size()) {
					continue;
				}

				if (change instanceof RemoveAxiom && changes.get(idx + 1) instanceof AddAxiom) {
					// Found the pattern of a renaming refactoring
					RemoveAxiom remove = (RemoveAxiom) change;
					AddAxiom add = (AddAxiom) changes.get(idx + 1);

					if (!(remove.getAxiom() instanceof OWLDeclarationAxiom && add.getAxiom() instanceof OWLDeclarationAxiom)) {
						continue;
					}
					// Found the patter we are looking for, a remove and add of
					// declaration axioms
					OWLEntity olde = ((OWLDeclarationAxiom) remove.getAxiom()).getEntity();
					OWLEntity newe = ((OWLDeclarationAxiom) add.getAxiom()).getEntity();
					renamings.put(olde, newe);

				} else if (change instanceof RemoveAxiom && ((RemoveAxiom) change).getAxiom() instanceof OWLDeclarationAxiom) {
					// Found the pattern of a deletion
					OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) ((RemoveAxiom) change).getAxiom();
					OWLEntity removedEntity = declaration.getEntity();
					removals.add(removedEntity);
				}
			}

			// Applying the renaming to the OBDA model
			OBDAModel obdamodel = getActiveOBDAModel();
			for (OWLEntity olde : renamings.keySet()) {
				OWLEntity removedEntity = olde;
				OWLEntity newEntity = renamings.get(removedEntity);

				// This set of changes appears to be a "renaming" operation,
				// hence we will modify the OBDA model accordingly
				Predicate removedPredicate = translator.getPredicate(removedEntity);
				Predicate newPredicate = translator.getPredicate(newEntity);

				obdamodel.renamePredicate(removedPredicate, newPredicate);
			}

			// Applying the deletions to the obda model
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
			if (ontologyIRI != null) {
				uri = ontologyIRI.toURI();
			} else {
				uri = URI.create(ontologyID.toString());
			}
			return obdamodels.get(uri);
		}
		return null;
	}

	/**
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
		activeOBDAModel = dfac.getOBDAModel();

		activeOBDAModel.addSourcesListener(dlistener);
		activeOBDAModel.addMappingsListener(mlistener);
		queryController.addListener(qlistener);

		OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

		Set<OWLOntology> ontologies = mmgr.getOntologies();
		for (OWLOntology ontology : ontologies) {
			// Setup the entity declarations
			for (OWLClass c : ontology.getClassesInSignature()) {
				OClass pred = ofac.createClass(c.getIRI().toString());
				activeOBDAModel.declareClass(pred);
			}
			for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature()) {
				PropertyExpression pred = ofac.createObjectProperty(r.getIRI().toString());
				activeOBDAModel.declareObjectProperty(pred);
			}
			for (OWLDataProperty p : ontology.getDataPropertiesInSignature()) {
				PropertyExpression pred = ofac.createDataProperty(p.getIRI().toString());
				activeOBDAModel.declareDataProperty(pred);
			}
		}

		// Setup the prefixes
		PrefixOWLOntologyFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(mmgr.getActiveOntology());
		//		addOBDACommonPrefixes(prefixManager);

		PrefixManagerWrapper prefixwrapper = new PrefixManagerWrapper(prefixManager);
		activeOBDAModel.setPrefixManager(prefixwrapper);

		OWLOntology activeOntology = mmgr.getActiveOntology();

		String defaultPrefix = prefixManager.getDefaultPrefix();
		if (defaultPrefix == null) {
			OWLOntologyID ontologyID = activeOntology.getOntologyID();
			defaultPrefix = ontologyID.getOntologyIRI().toURI().toString();
		}
		activeOBDAModel.getPrefixManager().addPrefix(PrefixManager.DEFAULT_PREFIX, defaultPrefix);

		// Add the model
		URI modelUri = activeOntology.getOntologyID().getOntologyIRI().toURI();
		obdamodels.put(modelUri, activeOBDAModel);
	}

	//	/**
	//	 * Append here all default prefixes used by the system.
	//	 */
	//	private void addOBDACommonPrefixes(PrefixOWLOntologyFormat prefixManager) {
	//		if (!prefixManager.containsPrefixMapping("quest")) {
	////			sb.append("@PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> .\n");
	////			sb.append("@PREFIX rdfs: <http:  //www.w3.org/2000/01/rdf-schema#> .\n");
	////			sb.append("@PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
	////			sb.append("@PREFIX owl: <http://www.w3.org/2002/07/owl#> .\n");
	//
	//			prefixManager.setPrefix("quest", OBDAVocabulary.QUEST_NS);
	//		}
	//	}

	public QueryController getQueryController() {
		if (queryController == null) {
			queryController = new QueryController();
		}
		return queryController;
	}

	/**
	 * Internal class responsible for coordinating actions related to updates in
	 * the ontology environment.
	 */
	private class OBDAPLuginOWLModelManagerListener implements OWLModelManagerListener {

		public boolean inititializing = false;

		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {

			// Get the active ontology
			OWLModelManager source = event.getSource();
			OWLOntology activeOntology = source.getActiveOntology();

			// Initialize the active OBDA model
			OBDAModel activeOBDAModel = null;

			// Perform a proper handling for each type of event
			final EventType eventType = event.getType();
			switch (eventType) {
			case ABOUT_TO_CLASSIFY:
				log.debug("ABOUT TO CLASSIFY");
				loadingData = true;
				break;

			case ENTITY_RENDERER_CHANGED:
				log.debug("RENDERER CHANGED");
				break;

			case ONTOLOGY_CLASSIFIED:
				loadingData = false;
				break;

			case ACTIVE_ONTOLOGY_CHANGED:
				log.debug("ACTIVE ONTOLOGY CHANGED");
				inititializing = true; // flag on

				// Setting up a new OBDA model and retrieve the object.
				setupNewOBDAModel();
				activeOBDAModel = getActiveOBDAModel();

				OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

				OWLOntology ontology = mmgr.getActiveOntology();
				PrefixOWLOntologyFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);

				String defaultPrefix = prefixManager.getDefaultPrefix();
				if (defaultPrefix == null) {
					OWLOntologyID ontologyID = ontology.getOntologyID();
					defaultPrefix = ontologyID.getOntologyIRI().toURI().toString();
				}
				activeOBDAModel.getPrefixManager().addPrefix(PrefixManager.DEFAULT_PREFIX, defaultPrefix);

				ProtegeOWLReasonerInfo factory = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
				if (factory instanceof ProtegeOBDAOWLReformulationPlatformFactory) {
					ProtegeOBDAOWLReformulationPlatformFactory questfactory = (ProtegeOBDAOWLReformulationPlatformFactory) factory;
					ProtegeReformulationPlatformPreferences reasonerPreference = (ProtegeReformulationPlatformPreferences) owlEditorKit.get(QuestPreferences.class.getName());
					questfactory.setPreferences(reasonerPreference);
					questfactory.setOBDAModel(getActiveOBDAModel());
					if(applyUserConstraints)
						questfactory.setImplicitDBConstraints(userConstraints);
				}
				fireActiveOBDAModelChange();

				inititializing = false; // flag off
				break;

			case ENTITY_RENDERING_CHANGED:
				break;

			case ONTOLOGY_CREATED:
				log.debug("ONTOLOGY CREATED");
				break;

			case ONTOLOGY_LOADED:
			case ONTOLOGY_RELOADED:
				log.debug("ONTOLOGY LOADED/RELOADED");
				loadingData = true; // flag on
				try {
					// Get the active OBDA model
					activeOBDAModel = getActiveOBDAModel();

					String owlDocumentIri = source.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology).toString();
					String obdaDocumentIri = owlDocumentIri.substring(0, owlDocumentIri.length() - 3) + OBDA_EXT;
					String queryDocumentIri = owlDocumentIri.substring(0, owlDocumentIri.length() - 3) + QUERY_EXT;
					String dbprefsDocumentIri = owlDocumentIri.substring(0, owlDocumentIri.length() - 3) + DBPREFS_EXT;

					File obdaFile = new File(URI.create(obdaDocumentIri));
					File queryFile = new File(URI.create(queryDocumentIri));
					File dbprefsFile = new File(URI.create(dbprefsDocumentIri));
					IRI ontologyIRI = activeOntology.getOntologyID().getOntologyIRI();

					activeOBDAModel.getPrefixManager().addPrefix(PrefixManager.DEFAULT_PREFIX, ontologyIRI.toString());
					if (obdaFile.exists()) {
						try {
							// Load the OBDA model
							ModelIOManager modelIO = new ModelIOManager(activeOBDAModel);
							modelIO.load(obdaFile);
						} catch (Exception ex) {
							activeOBDAModel.reset();
							throw new Exception("Exception occurred while loading OBDA document: " + obdaFile + "\n\n" + ex.getMessage());
						}
						try {
							// Load the saved queries
							QueryIOManager queryIO = new QueryIOManager(queryController);
							queryIO.load(queryFile);
						} catch (Exception ex) {
							queryController.reset();
							throw new Exception("Exception occurred while loading Query document: " + queryFile + "\n\n" + ex.getMessage());
						}	
						applyUserConstraints = false;
						if (dbprefsFile.exists()){
							try {
								// Load user-supplied constraints
								userConstraints = new ImplicitDBConstraints(dbprefsFile);
								applyUserConstraints = true;
							} catch (Exception ex) {
								throw new Exception("Exception occurred while loading database preference file : " + dbprefsFile + "\n\n" + ex.getMessage());
							}
						}
					} else {
						log.warn("OBDA model couldn't be loaded because no .obda file exists in the same location as the .owl file");
					}
					OBDAModelValidator refactorer = new OBDAModelValidator(activeOBDAModel, activeOntology);
					refactorer.run(); // adding type information to the mapping predicates.
				} catch (Exception e) {
					OBDAException ex = new OBDAException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
					DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
					log.error(e.getMessage());
				} finally {
					loadingData = false; // flag off
				}
				break;

			case ONTOLOGY_SAVED:
				log.debug("ACTIVE ONTOLOGY SAVED");
				try {
					// Get the active OBDA model
					activeOBDAModel = getActiveOBDAModel();

					String owlDocumentIri = source.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology).toString();
					String obdaDocumentIri = owlDocumentIri.substring(0, owlDocumentIri.length() - 3) + OBDA_EXT;
					String queryDocumentIri = owlDocumentIri.substring(0, owlDocumentIri.length() - 3) + QUERY_EXT;

					// Save the OBDA model
					File obdaFile = new File(URI.create(obdaDocumentIri));
					ModelIOManager ModelIO = new ModelIOManager(activeOBDAModel);
					ModelIO.save(obdaFile);

					// Save the queries
					File queryFile = new File(URI.create(queryDocumentIri));
					QueryIOManager queryIO = new QueryIOManager(queryController);
					queryIO.save(queryFile);

				} catch (IOException e) {
					log.error(e.getMessage());
					Exception newException = new Exception(
							"Error saving the OBDA file. Closing Protege now can result in losing changes in your data sources or mappings. Please resolve the issue that prevents saving in the current location, or do \"Save as..\" to save in an alternative location. \n\nThe error message was: \n"
									+ e.getMessage());
					DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
					triggerOntologyChanged();
				}
				break;

			case ONTOLOGY_VISIBILITY_CHANGED:
				log.debug("VISIBILITY CHANGED");
				break;

			case REASONER_CHANGED:
				log.info("REASONER CHANGED");

				// Get the active OBDA model
				activeOBDAModel = getActiveOBDAModel();

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
		if (loadingData) {
			return;
		}
		OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
		OWLOntology ontology = owlmm.getActiveOntology();

		if (ontology == null) {
			return;
		}

		OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(IRI.create("http://www.unibz.it/krdb/obdaplugin#RandomClass6677841155"));
		OWLAxiom axiom = owlmm.getOWLDataFactory().getOWLDeclarationAxiom(newClass);

		try {
			AddAxiom addChange = new AddAxiom(ontology, axiom);
			owlmm.applyChange(addChange);
			RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
			owlmm.applyChange(removeChange);
		} catch (Exception e) {
			log.warn("Exception forcing an ontology change. Your OWL model might contain a new class that you need to remove manually: {}",
					newClass.getIRI());
			log.warn(e.getMessage());
			log.debug(e.getMessage(), e);
		}
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

	/*
	 * The following are internal helpers that dispatch "needs save" messages to
	 * the OWL ontology model when OBDA model changes.
	 */

	private class ProtegeDatasourcesControllerListener implements OBDAModelListener {

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
