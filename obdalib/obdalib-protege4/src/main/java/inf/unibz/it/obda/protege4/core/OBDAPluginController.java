package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.AssertionControllerListener;
import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;
import inf.unibz.it.obda.api.controller.MappingControllerListener;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.QueryControllerListener;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.owlapi.OBDAOWLReasonerFactory;
import inf.unibz.it.obda.owlapi.OWLAPIController;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPlugin;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginLoader;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.obda.reformulation.protege4.ProtegeReformulationPlatformPreferences;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitManager;
import org.protege.editor.core.ui.workspace.WorkspaceManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerFactory;
import org.protege.editor.owl.ui.prefix.PrefixMapper;
import org.protege.editor.owl.ui.prefix.PrefixMapperManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAPluginController implements Disposable {

	// OWLOntology currentOntology = null;
	// URI currentOntologyPhysicalURI = null;

	ProtegeManager									pmanager				= null;
	EditorKitManager								ekmanager				= null;
	WorkspaceManager								wsmanager				= null;
	OWLEditorKit									owlEditorKit			= null;
	PrefixMapperManager								prefixmanager			= null;

	OWLOntologyManager								mmgr					= null;
	OWLOntology										root					= null;

	OWLAPIController								obdacontroller			= null;

	private Logger									log						= LoggerFactory.getLogger(this.getClass());

	private final OWLModelManagerListener			modelManagerListener	= new OBDAPLuginOWLModelManagerListener();

	private ProtegeQueryControllerListener			qlistener				= new ProtegeQueryControllerListener();
	private ProtegeMappingControllerListener		mlistener				= new ProtegeMappingControllerListener();
	private ProtegeDatasourcesControllerListener	dlistener				= new ProtegeDatasourcesControllerListener();

	public OBDAPluginController(EditorKit editorKit) {
		super();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("Received non OWLEditorKit editor kit");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;
		mmgr = ((OWLModelManagerImpl) owlEditorKit.getModelManager()).getOWLOntologyManager();

		// obdacontroller = new OWLAPIController();

		// setupReasonerFactory();

		OWLModelManager owlmm = owlEditorKit.getModelManager();
		owlmm.addListener(modelManagerListener);

		// Looking for instances of AssertionControllerFactory Plugins
		loadAssertionControllerFactoryPlugins();

	}

	public OWLAPIController getOBDAManager() {
		return obdacontroller;

	}

	private void setupNewOBDAModel() {
		
		
		
		if (obdacontroller != null) {
			obdacontroller.getDatasourcesController().removeDatasourceControllerListener(dlistener);
			obdacontroller.getMappingController().removeMappingControllerListener(mlistener);
			obdacontroller.getQueryController().removeListener(qlistener);
		}
		obdacontroller = new OWLAPIController();
		
		
		obdacontroller.getDatasourcesController().addDatasourceControllerListener(dlistener);
		obdacontroller.getMappingController().addMappingControllerListener(mlistener);
		obdacontroller.getQueryController().addListener(qlistener);
		
		
		PrefixMapper mapper = PrefixMapperManager.getInstance().getMapper();
		PrefixManagerWrapper prefixwrapper = new PrefixManagerWrapper(mapper);
		obdacontroller.setPrefixManager(prefixwrapper);
		OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();
		obdacontroller.getPrefixManager().setDefaultNamespace(mmgr.getActiveOntology().getURI().toString());
		
		
		Set<ProtegeOWLReasonerFactory> factories = owlEditorKit.getOWLWorkspace().getOWLModelManager().getOWLReasonerManager()
				.getInstalledReasonerFactories();
		for (ProtegeOWLReasonerFactory protegeOWLReasonerFactory : factories) {
			if (protegeOWLReasonerFactory instanceof OBDAOWLReasonerFactory) {
				OBDAOWLReasonerFactory obdaFactory = (OBDAOWLReasonerFactory) protegeOWLReasonerFactory;
				obdaFactory.setOBDAController(obdacontroller);

				// Each reasoner factory has its own preference instance.
				ProtegeReformulationPlatformPreferences reasonerPreference = new ProtegeReformulationPlatformPreferences();
				obdaFactory.setPreferenceHolder(reasonerPreference);
				// owlEditorKit.put(ReformulationPlatformPreferences.class.getName(),
				// reasonerPreference);
			}
		}

	}

	private void loadAssertionControllerFactoryPlugins() {
		AssertionControllerListener<Assertion> defaultAssertionControllerListener = new AssertionControllerListener<Assertion>() {
			public void assertionAdded(Assertion assertion) {
				triggerOntologyChanged();
			}

			public void assertionChanged(Assertion oldAssertion, Assertion newAssertion) {
				triggerOntologyChanged();
			}

			public void assertionRemoved(Assertion assertion) {
				triggerOntologyChanged();
			}

			public void assertionsCleared() {
				triggerOntologyChanged();
			}
		};

		AssertionControllerFactoryPluginLoader loader = new AssertionControllerFactoryPluginLoader();
		for (AssertionControllerFactoryPlugin pl : loader.getPlugins()) {
			try {
				AssertionControllerFactoryPluginInstance instance = pl.newInstance();
				Class assertionClass = instance.getAssertionClass();
				AssertionController controller = instance.getControllerInstance();
				AssertionXMLCodec xmlCodec = instance.getXMLCodec();
				boolean triggerUpddate = instance.triggersOntologyChanged();
				if (triggerUpddate) {
					controller.addControllerListener(defaultAssertionControllerListener);
				}
				obdacontroller.addAssertionController(assertionClass, controller, xmlCodec);
			} catch (Throwable e) {
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	/***
	 * Internal class responsible for coordinating actions related to updates in
	 * the ontology environment.
	 */
	private class OBDAPLuginOWLModelManagerListener implements OWLModelManagerListener {
		public void handleChange(OWLModelManagerChangeEvent event) {
			EventType type = event.getType();
			OWLModelManager source = event.getSource();
			

			switch (type) {
				case ABOUT_TO_CLASSIFY:
					log.info("ABOUT TO CLASSIFY");
					break;
				case ENTITY_RENDERER_CHANGED:
					log.info("RENDERER CHANGED");
					break;
				case ONTOLOGY_CLASSIFIED:
					break;
				case ACTIVE_ONTOLOGY_CHANGED:

					// OWLOntology ontology = owlEditorKit
					// .getOWLModelManager().getActiveOntology();
					//
					// obdacontroller.setCurrentOntologyURI(ontology.getURI());
					//
					// String uri = ontology.getURI().toString();
					// if (obdacontroller.getLoadedOntologies().add(uri)) {
					// apicoupler.addNewOntologyInfo(ontology);
					// loadData(source.getOntologyPhysicalURI(ontology));
					// }
					// try {
					// obdacontroller.getMapcontroller()
					// .activeOntologyChanged();
					//
					// } catch (Exception e) {
					// log.warn("Error changing the active ontology.");
					// }
					//
					// apicoupler.updateOntologies();
					log.info("ACTIVE ONTOLOGY CHANGED");
					if (obdacontroller != null)
						obdacontroller.getPrefixManager().setDefaultNamespace(source.getActiveOntology().getURI().toString());
					break;
				case ENTITY_RENDERING_CHANGED:
					break;

				case ONTOLOGY_CREATED:
					log.info("ONTOLOGY CREATED");
					setupNewOBDAModel();
					break;

				case ONTOLOGY_LOADED:
					log.info("ACTIVE ONTOLOGY LOADED");
					loadingData = true;
					try {
						setupNewOBDAModel();

						OWLOntology activeonto = source.getActiveOntology();
						URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
						URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");

						obdacontroller.getPrefixManager().setDefaultNamespace(activeonto.getURI().toString());
						obdacontroller.getIOManager().loadOBDADataFromURI(obdafile, activeonto.getURI(),obdacontroller.getPrefixManager());
					} catch (Exception e) {
						log.warn(e.getMessage());
						log.debug(e.getMessage(), e);
					} finally {
						loadingData = false;
					}
					break;

				case ONTOLOGY_SAVED:
					log.info("ACTIVE ONTOLOGY SAVED");
					
					OWLOntology activeonto = source.getActiveOntology();
					URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
					URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");

					try {
						obdacontroller.getIOManager().saveOBDAData(obdafile, true, obdacontroller.getPrefixManager());
					} catch (IOException e) {
						log.error("ERROR saving OBDA data to file: {}", obdafile.toString());
						log.error("ERROR message: {}", e.getMessage());
						log.error(e.getMessage(),e);
					}
					break;
				case ONTOLOGY_VISIBILITY_CHANGED:
					log.info("VISIBILITY CHANGED");
					break;
				case REASONER_CHANGED:
					log.info("REASONER CHANGED");
					break;
			}
		}

	}

	// private OWLAPICoupler apicoupler;

	private boolean	loadingData;

	private void triggerOntologyChanged() {
		if (!this.loadingData) {
			OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
			OWLOntology ontology = owlmm.getActiveOntology();

			if (ontology != null) {
				OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(URI.create(ontology.getURI() + "#RandomClass6677841155"));
				OWLAxiom axiom = owlmm.getOWLDataFactory().getOWLDeclarationAxiom(newClass);

				try {
					AddAxiom addChange = new AddAxiom(ontology, axiom);
					owlmm.applyChange(addChange);

					RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
					owlmm.applyChange(removeChange);
				} catch (Exception e) {
					log
							.warn(
									"Exception while faking an ontology change. Your OBDA data might have new data that has not been noted and you must force an ontology save operation OR your ontology could have an extra declaration for a temporary class with URI: {}",
									newClass.getURI());
					log.debug(e.getMessage(), e);
				}
			}
		}
	}

	// public void removeModelManagerListener() {
	// owlEditorKit.getModelManager().removeListener(modelManagerListener);
	// }

	// public void addOntologyToCoupler(URI uri) {
	// OWLOntologyManager mmgr = ((OWLModelManagerImpl)
	// owlEditorKit.getModelManager()).getOWLOntologyManager();
	// apicoupler.addNewOntologyInfo(mmgr.getOntology(uri));
	// }

	// @Override
	// public URI getPhysicalURIOfOntology(URI onto){
	//		
	// Set<OWLOntology> set =owlEditorKit.getModelManager().getOntologies();
	// Iterator<OWLOntology> it = set.iterator();
	// while(it.hasNext()){
	// OWLOntology o = it.next();
	// if(o.getURI().equals(onto)){
	// return owlEditorKit.getModelManager().getOntologyPhysicalURI(o);
	// }
	// }
	// return null;
	// }

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	public void dispose() throws Exception {
		owlEditorKit.getModelManager().removeListener(modelManagerListener);

	}

	public void loadData(URI owlFile) {
		// loadingData = true;
		// try {
		// apicoupler.addNewOntologyInfo(currentOntology);
		// URI obdafile = obdacontroller.getIOManager().getOBDAFile(owlFile);
		// obdacontroller.getIOManager().loadOBDADataFromURI(obdafile);
		// String uri = currentOntology.getURI().toString();
		// obdacontroller.getLoadedOntologies().add(uri);
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		// loadingData = false;
		// }
	}

	public void saveData(URI owlFile) {
		// log.info("Saving OBDA data for " + owlFile.toString());
		// URI obdafile = obdacontroller.getIOManager().getOBDAFile(owlFile);
		// try {
		// obdacontroller.getIOManager().saveOBDAData(obdafile);
		// } catch (Exception e) {
		// e.printStackTrace();
		// triggerOntologyChanged();
		// log.error(e.getMessage(), e);
		// }
	}

	// @Override
	// public Set<URI> getOntologyURIs() {
	// HashSet<URI> uris = new HashSet<URI>();
	// Set<OWLOntology> ontologies =
	// owlEditorKit.getModelManager().getOntologies();
	// for (OWLOntology owlOntology : ontologies) {
	// uris.add(owlOntology.getURI());
	// }
	// return uris;
	// }

	// public void setCurrentOntologyURI(URI uri) {
	// obdacontroller.setCurrentOntologyURI(uri);
	// apicoupler.updateOntology(uri);
	// obdacontroller.getMapcontroller().activeOntologyChanged();
	// }

	/***
	 * The following are internal helpers that dispatch "needs save" messages to
	 * the OWL ontology model when OBDA model changes.
	 */

	private class ProtegeDatasourcesControllerListener implements DatasourcesControllerListener {
		public void datasourceUpdated(String oldname, DataSource currendata) {
			triggerOntologyChanged();
		}

		public void datasourceDeleted(DataSource source) {
			triggerOntologyChanged();
		}

		public void datasourceAdded(DataSource source) {
			triggerOntologyChanged();
		}

		public void alldatasourcesDeleted() {
			triggerOntologyChanged();
		}

		public void datasourcParametersUpdated() {
			triggerOntologyChanged();
		}
	}

	private class ProtegeMappingControllerListener implements MappingControllerListener {
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
