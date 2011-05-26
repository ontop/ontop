package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;
import inf.unibz.it.obda.api.controller.MappingControllerListener;
import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.api.controller.QueryControllerListener;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.model.DataSource;
import inf.unibz.it.obda.model.OBDAMappingAxiom;
import inf.unibz.it.obda.owlapi.OBDAOWLReasonerFactory;
import inf.unibz.it.obda.owlapi.OWLAPIController;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.obda.reformulation.protege4.ProtegeReformulationPlatformPreferences;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
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

	private Logger									log						= LoggerFactory.getLogger(this.getClass());

	OWLEditorKit									owlEditorKit			= null;

	OWLOntologyManager								mmgr					= null;

	OWLAPIController								obdacontroller			= null;

//	List<OBDAPluginControllerListener>				controllerListeners;

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener			modelManagerListener	= new OBDAPLuginOWLModelManagerListener();

	private ProtegeQueryControllerListener			qlistener				= new ProtegeQueryControllerListener();
	private ProtegeMappingControllerListener		mlistener				= new ProtegeMappingControllerListener();
	private ProtegeDatasourcesControllerListener	dlistener				= new ProtegeDatasourcesControllerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	private boolean									loadingData;

	public OBDAPluginController(EditorKit editorKit) {
		super();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA PLugin only works with OWLEditorKit instances.");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;
		mmgr = ((OWLModelManager) owlEditorKit.getModelManager()).getOWLOntologyManager();

//		controllerListeners = new LinkedList<OBDAPluginControllerListener>();
//		OWLModelManager owlmm = owlEditorKit.getModelManager();
//		owlmm.addListener(getModelManagerListener());
	}

	public OWLAPIController getOBDAManager() {
		return obdacontroller;

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
		APIController oldcontroller = obdacontroller;
		
		if (obdacontroller != null) {

			log.warn("WARNING: At the moment, the obda plugin doesn't support editing two ontologies in the same window");
			log.warn("Open ontologies ONLY IN A NEW WINDOWS, otherwise, the OBDA information and the behaivor of the plugin will be inconsistent");
			
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
			}
		}
		
//		triggerOBDAModelChangeEvent(oldcontroller, obdacontroller);
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
					log.debug("ABOUT TO CLASSIFY");
					break;
				case ENTITY_RENDERER_CHANGED:
					log.debug("RENDERER CHANGED");
					break;
				case ONTOLOGY_CLASSIFIED:
					break;
				case ACTIVE_ONTOLOGY_CHANGED:

					log.debug("ACTIVE ONTOLOGY CHANGED");
					if (obdacontroller != null)
						obdacontroller.getPrefixManager().setDefaultNamespace(source.getActiveOntology().getURI().toString());
					break;
				case ENTITY_RENDERING_CHANGED:
					break;

				case ONTOLOGY_CREATED:
					log.debug("ONTOLOGY CREATED");
					setupNewOBDAModel();
					break;

				case ONTOLOGY_LOADED:
					log.debug("ACTIVE ONTOLOGY LOADED");
					loadingData = true;
					try {
						setupNewOBDAModel();

						OWLOntology activeonto = source.getActiveOntology();
						URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
						URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");

						obdacontroller.getPrefixManager().setDefaultNamespace(activeonto.getURI().toString());
						obdacontroller.getIOManager().loadOBDADataFromURI(obdafile, activeonto.getURI(), obdacontroller.getPrefixManager());
					} catch (Exception e) {
						log.warn(e.getMessage());
						log.debug(e.getMessage(), e);
					} finally {
						loadingData = false;
					}
					break;

				case ONTOLOGY_SAVED:
					log.debug("ACTIVE ONTOLOGY SAVED");

					OWLOntology activeonto = source.getActiveOntology();
					URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
					URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");

					try {
						obdacontroller.getIOManager().saveOBDAData(obdafile, true, obdacontroller.getPrefixManager());
					} catch (IOException e) {
						log.error("ERROR saving OBDA data to file: {}", obdafile.toString());
						log.error("ERROR message: {}", e.getMessage());
						log.error(e.getMessage(), e);
					}
					break;
				case ONTOLOGY_VISIBILITY_CHANGED:
					log.debug("VISIBILITY CHANGED");
					break;
				case REASONER_CHANGED:
					log.debug("REASONER CHANGED");
					break;
			}
		}

	}

//	public void addListener(OBDAPluginControllerListener listener) {
//		controllerListeners.add(listener);
//	}
//
//	public void removeListener(OBDAPluginControllerListener listener) {
//		controllerListeners.remove(listener);
//	}

//	private void triggerOBDAModelChangeEvent(APIController oldmodel, APIController newmodel) {
//		for (OBDAPluginControllerListener listener : controllerListeners) {
//			try {
//				listener.obdaModelChanged(oldmodel, newmodel);
//			} catch (Exception e) {
//				log.warn("Eror notifying listeners of obda model change: {}", e.getMessage());
//				log.debug(e.getMessage(), e);
//			}
//		}
//	}

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

		OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(URI.create("http://www.unibz.it/krdb/obdaplugin#RandomClass6677841155"));
		OWLAxiom axiom = owlmm.getOWLDataFactory().getOWLDeclarationAxiom(newClass);

		try {
			AddAxiom addChange = new AddAxiom(ontology, axiom);
			owlmm.applyChange(addChange);
			RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
			owlmm.applyChange(removeChange);
		} catch (Exception e) {
			log.warn("Exception forcing an ontology change. Your OWL model might contain a new class that you need to remove manually: {}",
					newClass.getURI());
			log.warn(e.getMessage());
			log.debug(e.getMessage(), e);
		}
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	public void dispose() throws Exception {
//		try {
//			
//			owlEditorKit.getModelManager().removeListener(getModelManagerListener());
//		} catch (Exception e) {
//			log.warn(e.getMessage());
//		}

	}

	protected OWLModelManagerListener getModelManagerListener() {
		return modelManagerListener;
	}

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
