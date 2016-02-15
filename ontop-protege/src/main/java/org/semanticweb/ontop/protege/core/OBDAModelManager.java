package org.semanticweb.ontop.protege.core;

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

import com.google.common.base.Optional;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelValidator;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.querymanager.*;
import it.unibz.krdb.sql.ImplicitDBConstraintsReader;
import it.unibz.krdb.sql.JDBCConnectionManager;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.ontop.protege.utils.DialogUtils;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URI;
import java.util.*;

public class OBDAModelManager implements Disposable {

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.
	private static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.

	private final OWLEditorKit owlEditorKit;

	private final OWLOntologyManager mmgr;

	private QueryController queryController;

	private final Map<URI, OBDAModel> obdamodels;

	private final List<OBDAModelManagerListener> obdaManagerListeners;

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

    private boolean applyUserConstraints = false;
	private ImplicitDBConstraintsReader userConstraints;
	
	private static final Logger log = LoggerFactory.getLogger(OBDAModelManager.class);

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();

	private final ProtegeQueryControllerListener qlistener = new ProtegeQueryControllerListener();
	private final ProtegeMappingControllerListener mlistener = new ProtegeMappingControllerListener();
	private final ProtegeDatasourcesControllerListener dlistener = new ProtegeDatasourcesControllerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	private boolean loadingData;

	public OBDAModelManager(EditorKit editorKit) {
		super();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA Plugin only works with OWLEditorKit instances.");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;
		mmgr = owlEditorKit.getModelManager().getOWLOntologyManager();
		OWLModelManager owlmmgr = (OWLModelManager) editorKit.getModelManager();
		owlmmgr.addListener(modelManagerListener);
		obdaManagerListeners = new ArrayList<>();
		obdamodels = new HashMap<>();

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


		@Override
		public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) throws OWLException {
			Map<OWLEntity, OWLEntity> renamings = new HashMap<OWLEntity, OWLEntity>();
			Set<OWLEntity> removals = new HashSet<OWLEntity>();

			for (int idx = 0; idx < changes.size(); idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					Optional<IRI> optionalNewIRI = ((SetOntologyID) change).getNewOntologyID().getOntologyIRI();

                    if(!optionalNewIRI.isPresent())
                        return;

                    IRI newiri = optionalNewIRI.get();

					IRI oldiri = ((SetOntologyID) change).getOriginalOntologyID().getOntologyIRI().get();

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
							activeOBDAModel.getOntologyVocabulary().createClass(oc.getIRI().toString());
						} 
						else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							activeOBDAModel.getOntologyVocabulary().createObjectProperty(or.getIRI().toString());
						} 
						else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							activeOBDAModel.getOntologyVocabulary().createDataProperty(op.getIRI().toString());
						}
						else if (entity instanceof OWLAnnotationProperty){
							OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
							activeOBDAModel.getOntologyVocabulary().createAnnotationProperty(ap.getIRI().toString());
						}
					}

				} else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						OBDAModel activeOBDAModel = getActiveOBDAModel();
						if (entity instanceof OWLClass) {
							OWLClass oc = (OWLClass) entity;
							activeOBDAModel.getOntologyVocabulary().removeClass(oc.getIRI().toString());
						} 
						else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							activeOBDAModel.getOntologyVocabulary().removeObjectProperty(or.getIRI().toString());
						} 
						else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							activeOBDAModel.getOntologyVocabulary().removeDataProperty(op.getIRI().toString());
						}

						else if (entity instanceof  OWLAnnotationProperty ){
							OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
							activeOBDAModel.getOntologyVocabulary().removeAnnotationProperty(ap.getIRI().toString());
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

				} else if (change instanceof RemoveAxiom && change.getAxiom() instanceof OWLDeclarationAxiom) {
					// Found the pattern of a deletion
					OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) change.getAxiom();
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
				Predicate removedPredicate = getPredicate(removedEntity);
				Predicate newPredicate = getPredicate(newEntity);

				obdamodel.renamePredicate(removedPredicate, newPredicate);
			}

			// Applying the deletions to the obda model
			for (OWLEntity removede : removals) {
				Predicate removedPredicate = getPredicate(removede);
				obdamodel.deletePredicate(removedPredicate);
			}
		}
	}
	
	private static Predicate getPredicate(OWLEntity entity) {
		Predicate p = null;
		if (entity instanceof OWLClass) {
			/* We ignore TOP and BOTTOM (Thing and Nothing) */
			if (((OWLClass) entity).isOWLThing() || ((OWLClass) entity).isOWLNothing()) {
				return null;
			}
			String uri = entity.getIRI().toString();

			p = dfac.getClassPredicate(uri);
		} else if (entity instanceof OWLObjectProperty) {
			String uri = entity.getIRI().toString();

			p = dfac.getObjectPropertyPredicate(uri);
		} else if (entity instanceof OWLDataProperty) {
			String uri = entity.getIRI().toString();

			p = dfac.getDataPropertyPredicate(uri);

		} else if (entity instanceof OWLAnnotationProperty) {
			String uri = entity.getIRI().toString();

			p = dfac.getDataPropertyPredicate(uri);
        }
		return p;
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

            Optional<IRI> optionalOntologyIRI = ontologyID.getOntologyIRI();

            URI uri;

            if(optionalOntologyIRI.isPresent()){
                uri = optionalOntologyIRI.get().toURI();
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
			for (OWLClass c : ontology.getClassesInSignature()) 
				activeOBDAModel.getOntologyVocabulary().createClass(c.getIRI().toString());
			
			for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature()) 
				activeOBDAModel.getOntologyVocabulary().createObjectProperty(r.getIRI().toString());
			
			for (OWLDataProperty p : ontology.getDataPropertiesInSignature()) 
				activeOBDAModel.getOntologyVocabulary().createDataProperty(p.getIRI().toString());

			for (OWLAnnotationProperty p : ontology.getAnnotationPropertiesInSignature())
				activeOBDAModel.getOntologyVocabulary().createAnnotationProperty(p.getIRI().toString());
		}

		// Setup the prefixes
		PrefixDocumentFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(mmgr.getActiveOntology());
		//		addOBDACommonPrefixes(prefixManager);

		PrefixManagerWrapper prefixwrapper = new PrefixManagerWrapper(prefixManager);
		activeOBDAModel.setPrefixManager(prefixwrapper);

		OWLOntology activeOntology = mmgr.getActiveOntology();

		String defaultPrefix = prefixManager.getDefaultPrefix();
		if (defaultPrefix == null) {
			OWLOntologyID ontologyID = activeOntology.getOntologyID();
			defaultPrefix = ontologyID.getOntologyIRI().get().toURI().toString();
		}
		activeOBDAModel.getPrefixManager().addPrefix(PrefixManager.DEFAULT_PREFIX, defaultPrefix);

		// Add the model
		URI modelUri = activeOntology.getOntologyID().getOntologyIRI().get().toURI();
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
	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {

		public boolean initializing = false;

		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {

			// Get the active ontology
			OWLModelManager source = event.getSource();
			OWLOntology activeOntology = source.getActiveOntology();

			// Initialize the active OBDA model
			OBDAModel activeOBDAModel;

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
				handleActiveOntologyChanged();
				break;

			case ENTITY_RENDERING_CHANGED:
				break;

			case ONTOLOGY_CREATED:
				log.debug("ONTOLOGY CREATED");
				break;

			case ONTOLOGY_LOADED:
			case ONTOLOGY_RELOADED:
				log.debug("ONTOLOGY LOADED/RELOADED");
				handleOntologyLoadedAndReLoaded(source, activeOntology);
				break;

			case ONTOLOGY_SAVED:
				log.debug("ONTOLOGY SAVED");
				handleOntologySaved(source, activeOntology);
				break;

			case ONTOLOGY_VISIBILITY_CHANGED:
				log.debug("ONTOLOGY VISIBILITY CHANGED");
				break;

			case REASONER_CHANGED:
				log.info("REASONER CHANGED");
				handleReasonerChanged();
				break;
			}
		}

		private void handleReasonerChanged() {
			OBDAModel activeOBDAModel;// Get the active OBDA model
			activeOBDAModel = getActiveOBDAModel();

			if ((!initializing) && (owlEditorKit != null) && (activeOBDAModel != null)) {
                ProtegeOWLReasonerInfo fac = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
                if (fac instanceof OntopReasonerInfo) {
                    OntopReasonerInfo questfactory = (OntopReasonerInfo) fac;
                    DisposableQuestPreferences reasonerPreference = (DisposableQuestPreferences) owlEditorKit
                            .get(QuestPreferences.class.getName());
                    questfactory.setPreferences(reasonerPreference);
                    questfactory.setOBDAModel(activeOBDAModel);
                }
				return;
            }
		}

		private void handleActiveOntologyChanged() {
			OBDAModel activeOBDAModel;
			initializing = true; // flag on

			// Setting up a new OBDA model and retrieve the object.
			setupNewOBDAModel();
			activeOBDAModel = getActiveOBDAModel();

			OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

			OWLOntology ontology = mmgr.getActiveOntology();
			PrefixDocumentFormat prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);

			String defaultPrefix = prefixManager.getDefaultPrefix();
			if (defaultPrefix == null) {
                OWLOntologyID ontologyID = ontology.getOntologyID();
                defaultPrefix = ontologyID.getOntologyIRI().get().toURI().toString();
            }
			activeOBDAModel.getPrefixManager().addPrefix(PrefixManager.DEFAULT_PREFIX, defaultPrefix);

			ProtegeOWLReasonerInfo factory = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
			if (factory instanceof OntopReasonerInfo) {
                OntopReasonerInfo questfactory = (OntopReasonerInfo) factory;
                DisposableQuestPreferences reasonerPreference = (DisposableQuestPreferences) owlEditorKit.get(QuestPreferences.class.getName());
                questfactory.setPreferences(reasonerPreference);
                questfactory.setOBDAModel(getActiveOBDAModel());
                if(applyUserConstraints)
                    questfactory.setImplicitDBConstraints(userConstraints);
            }
			fireActiveOBDAModelChange();

			initializing = false; // flag off
		}

		private void handleOntologyLoadedAndReLoaded(OWLModelManager owlModelManager, OWLOntology activeOntology) {
			OBDAModel activeOBDAModel;
			loadingData = true; // flag on
			try {
                // Get the active OBDA model
                activeOBDAModel = getActiveOBDAModel();

				IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
				String owlDocumentIriString = documentIRI.toString();
				int i = owlDocumentIriString.lastIndexOf(".");
				String owlName = owlDocumentIriString.substring(0,i);

				String obdaDocumentIri = owlName + OBDA_EXT;
                String queryDocumentIri = owlName + QUERY_EXT;
                String dbprefsDocumentIri = owlName + DBPREFS_EXT;

                File obdaFile = new File(URI.create(obdaDocumentIri));
                File queryFile = new File(URI.create(queryDocumentIri));
                File dbprefsFile = new File(URI.create(dbprefsDocumentIri));
                IRI ontologyIRI = activeOntology.getOntologyID().getOntologyIRI().get();

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
                            userConstraints = new ImplicitDBConstraintsReader(dbprefsFile);
                            applyUserConstraints = true;
                        } catch (Exception ex) {
                            throw new Exception("Exception occurred while loading database preference file : " + dbprefsFile + "\n\n" + ex.getMessage());
                        }
                    }
                } else {
                    log.warn("OBDA model couldn't be loaded because no .obda file exists in the same location as the .owl file");
                }
                // adding type information to the mapping predicates
                OBDAModelValidator.validate(activeOBDAModel);
            }
            catch (Exception e) {
                OBDAException ex = new OBDAException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
                DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
                log.error(e.getMessage());
            } finally {
                loadingData = false; // flag off
            }
		}

		private void handleOntologySaved(OWLModelManager owlModelManager, OWLOntology activeOntology) {
			OBDAModel activeOBDAModel;
			try {
                // copied from org.protege.editor.core.ui.action.SaveAction
                //ProtegeManager.getInstance().saveEditorKit(owlEditorKit);
//                owlEditorKit.handleSave();

                // Get the active OBDA model
                activeOBDAModel = getActiveOBDAModel();

                IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
                String owlDocumentIriString = documentIRI.toString();

                if(!UIUtil.isLocalFile(documentIRI.toURI())){
                    return;
                }

                if(!activeOBDAModel.getSources().isEmpty()) {

                    //String owlName = Files.getNameWithoutExtension(owlDocumentIriString);

					int i = owlDocumentIriString.lastIndexOf(".");
					String owlName = owlDocumentIriString.substring(0,i);

					String obdaDocumentIri = owlName + OBDA_EXT;
                    String queryDocumentIri = owlName + QUERY_EXT;

                    // Save the OBDA model
                    File obdaFile = new File(URI.create(obdaDocumentIri));
					//File obdaFile = new File(obdaDocumentIri);
                    ModelIOManager ModelIO = new ModelIOManager(activeOBDAModel);
                    ModelIO.save(obdaFile);

                    log.info("mapping file saved to {}", obdaFile);

                    if (!queryController.getElements().isEmpty()) {
                        // Save the queries
                        File queryFile = new File(URI.create(queryDocumentIri));
                        QueryIOManager queryIO = new QueryIOManager(queryController);
                        queryIO.save(queryFile);
						log.info("query file saved to {}", queryFile);
					}
                }

            } catch (Exception e) {
                log.error(e.getMessage());
                Exception newException = new Exception(
                        "Error saving the OBDA file. Closing Protege now can result in losing changes in your data sources or mappings. Please resolve the issue that prevents saving in the current location, or do \"Save as..\" to save in an alternative location. \n\nThe error message was: \n"
                                + e.getMessage());
                DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
                triggerOntologyChanged();
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
	@Override
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

		@Override
        public void datasourceUpdated(String oldname, OBDADataSource currendata) {
			triggerOntologyChanged();
		}

		@Override
        public void datasourceDeleted(OBDADataSource source) {
			triggerOntologyChanged();
		}

		@Override
        public void datasourceAdded(OBDADataSource source) {
			triggerOntologyChanged();
		}

		@Override
        public void alldatasourcesDeleted() {
			triggerOntologyChanged();
		}

		@Override
        public void datasourceParametersUpdated() {
			triggerOntologyChanged();
		}
	}

	private class ProtegeMappingControllerListener implements OBDAMappingListener {

		private static final long serialVersionUID = -5794145688669702879L;

		@Override
        public void allMappingsRemoved() {
			triggerOntologyChanged();
		}

		@Override
        public void currentSourceChanged(URI oldsrcuri, URI newsrcuri) {
			// Do nothing!
		}

		@Override
        public void mappingDeleted(URI srcuri, String mapping_id) {
			triggerOntologyChanged();
		}

		@Override
        public void mappingInserted(URI srcuri, String mapping_id) {
			triggerOntologyChanged();
		}

		@Override
        public void mappingUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
			triggerOntologyChanged();
		}
	}

	private class ProtegeQueryControllerListener implements QueryControllerListener {

		private static final long serialVersionUID = 4536639410306364312L;

		@Override
        public void elementAdded(QueryControllerEntity element) {
			triggerOntologyChanged();
		}

		@Override
        public void elementAdded(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}

		@Override
        public void elementRemoved(QueryControllerEntity element) {
			triggerOntologyChanged();
		}

		@Override
        public void elementRemoved(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}

		@Override
        public void elementChanged(QueryControllerQuery query) {
			triggerOntologyChanged();
		}

		@Override
        public void elementChanged(QueryControllerQuery query, QueryControllerGroup group) {
			triggerOntologyChanged();
		}
	}
}
