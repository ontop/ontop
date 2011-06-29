package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.DatasourcesControllerListener;
import it.unibz.krdb.obda.model.MappingControllerListener;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.queryanswering.QueryControllerEntity;
import it.unibz.krdb.obda.queryanswering.QueryControllerGroup;
import it.unibz.krdb.obda.queryanswering.QueryControllerListener;
import it.unibz.krdb.obda.queryanswering.QueryControllerQuery;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
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

public class OBDAModelManager implements Disposable {

	private Logger									log						= LoggerFactory.getLogger(this.getClass());

	OWLEditorKit									owlEditorKit			= null;

	OWLOntologyManager								mmgr					= null;

	// OBDAModel obdacontroller = null;

	Map<URI, OBDAModel>								obdamodels				= null;

	private List<OBDAModelManagerListener>			obdaManagerListeners	= null;

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

	}

	public void addListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.remove(listener);
	}

	public OBDAModel getActiveOBDAModel() {
		return obdamodels.get(owlEditorKit.getOWLModelManager().getActiveOntology().getURI());
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

		activeOBDAModel.getDatasourcesController().addDatasourceControllerListener(dlistener);
		activeOBDAModel.getMappingController().addMappingControllerListener(mlistener);
		activeOBDAModel.getQueryController().addListener(qlistener);

		PrefixMapper mapper = PrefixMapperManager.getInstance().getMapper();
		PrefixManagerWrapper prefixwrapper = new PrefixManagerWrapper(mapper);
		activeOBDAModel.setPrefixManager(prefixwrapper);
		OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();
		activeOBDAModel.getPrefixManager().setDefaultNamespace(mmgr.getActiveOntology().getURI().toString());

		obdamodels.put(this.owlEditorKit.getModelManager().getActiveOntology().getURI(), activeOBDAModel);

		// triggerOBDAModelChangeEvent(oldcontroller, obdacontroller);
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
					setupNewOBDAModel();

					Set<ProtegeOWLReasonerFactory> factories = owlEditorKit.getOWLWorkspace().getOWLModelManager().getOWLReasonerManager()
							.getInstalledReasonerFactories();
					for (ProtegeOWLReasonerFactory protegeOWLReasonerFactory : factories) {
						if (protegeOWLReasonerFactory instanceof OBDAOWLReasonerFactory) {
							OBDAOWLReasonerFactory obdaFactory = (OBDAOWLReasonerFactory) protegeOWLReasonerFactory;
							obdaFactory.setOBDAController(getActiveOBDAModel());

							ProtegeReformulationPlatformPreferences reasonerPreference = owlEditorKit
									.get(ReformulationPlatformPreferences.class.getName());
							obdaFactory.setPreferenceHolder(reasonerPreference);
						}
					}
					getActiveOBDAModel().getPrefixManager().setDefaultNamespace(source.getActiveOntology().getURI().toString());
					fireActiveOBDAModelChange();
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
						URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
						URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");
						getActiveOBDAModel().getPrefixManager().setDefaultNamespace(activeonto.getURI().toString());
						DataManager ioManager = new DataManager(getActiveOBDAModel());
						File file = new File(obdafile);
						if (file.canRead()) {
							ioManager.loadOBDADataFromURI(obdafile, activeonto.getURI(), getActiveOBDAModel().getPrefixManager());
						} else {
							log.debug("Cannot read file: {}", obdafile.toString());
							log.debug("If this is a new model, or a model without a .obda file then ignore this message");
						}
					} catch (Exception e) {
						log.warn(e.getMessage());
						log.debug(e.getMessage(), e);
					} finally {
						loadingData = false;
					}
					break;

				case ONTOLOGY_RELOADED:
					log.debug("ONTOLOGY RELOADED");
					loadingData = true;
					try {

						OWLOntology activeonto = source.getActiveOntology();
						URI owlfile = source.getOWLOntologyManager().getPhysicalURIForOntology(activeonto);
						URI obdafile = URI.create(owlfile.toString().substring(0, owlfile.toString().length() - 3) + "obda");
						getActiveOBDAModel().getPrefixManager().setDefaultNamespace(activeonto.getURI().toString());
						DataManager ioManager = new DataManager(getActiveOBDAModel());
						File file = new File(obdafile);
						if (file.canRead()) {
							ioManager.loadOBDADataFromURI(obdafile, activeonto.getURI(), getActiveOBDAModel().getPrefixManager());
						} else {
							log.debug("Cannot read file: {}", obdafile.toString());
							log.debug("If this is a new model, or a model without a .obda file then ignore this message");
						}
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
					File file = new File(obdafile);
					if (file.canWrite()) {
						try {
							DataManager ioManager = new DataManager(getActiveOBDAModel());
							ioManager.saveOBDAData(obdafile, true, getActiveOBDAModel().getPrefixManager());
						} catch (IOException e) {
							log.error("ERROR saving OBDA data to file: {}", obdafile.toString());
							log.error("ERROR message: {}", e.getMessage());
							log.error(e.getMessage(), e);
						}
					} else {
						log.warn("WARNING: The location of the .obda file for this ontology doesnt allow write operations");
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
		try {
			owlEditorKit.getModelManager().removeListener(getModelManagerListener());
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
