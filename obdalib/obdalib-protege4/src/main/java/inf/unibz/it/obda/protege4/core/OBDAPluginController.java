package inf.unibz.it.obda.protege4.core;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
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
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPlugin;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginLoader;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.protege.editor.owl.ui.prefix.PrefixMapperManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

public class OBDAPluginController extends APIController implements Disposable {

	OWLOntology currentOntology = null;
	URI currentOntologyPhysicalURI = null;

	ProtegeManager pmanager = null;
	EditorKitManager ekmanager = null;
	WorkspaceManager wsmanager = null;
	OWLEditorKit owlEditorKit = null;
	PrefixMapperManager prefixmanager = null;

	public OBDAPluginController(EditorKit editorKit) {
		super();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("Received non OWLEditorKit editor kit");
		}		
		this.owlEditorKit = (OWLEditorKit) editorKit;
		
		initMapController();
		initIOManager();
		initAPICoupler();
		
		setupReasonerFactory();
		
	  // Adding standard listeners
	  addOntologyChangeListener();
	  addDatasourceControllerListener();
	  addMappingControllerListener();
	  addQueryControllerListener();
	  		
    // Looking for instances of AssertionControllerFactory Plugins
    loadAssertionControllerFactoryPlugins();
	}
	
	private void initMapController() {
		mapcontroller = new SynchronizedMappingController(dscontroller, this);
	}
	
	private void initIOManager() {
		ioManager = new OBDAPluginDataManager(this, new ProtegePrefixManager());
	}
	
	private void initAPICoupler() {
		final OWLOntologyManager mmgr = ((OWLModelManagerImpl) owlEditorKit.getModelManager()).getOWLOntologyManager();
		final OWLOntology root = owlEditorKit.getOWLModelManager().getActiveOntology();
		apicoupler = new OWLAPICoupler(this, mmgr, root);
		setCoupler(apicoupler);
	}
	
	 /**
   * Setting up the current reasoner factories to have a reference to this
   * OBDA plugin controller.
   */
	private void setupReasonerFactory() {
		Set<ProtegeOWLReasonerFactory> factories = owlEditorKit
				.getOWLWorkspace().getOWLModelManager().getOWLReasonerManager()
				.getInstalledReasonerFactories();
		for (ProtegeOWLReasonerFactory protegeOWLReasonerFactory : factories) {
			if (protegeOWLReasonerFactory instanceof OBDAOWLReasonerFactory) {
				OBDAOWLReasonerFactory obdaFactory = (OBDAOWLReasonerFactory) protegeOWLReasonerFactory;
				obdaFactory.setOBDAController(this);
				
				// Each reasoner factory has its own preference instance.
				ProtegeReformulationPlatformPreferences reasonerPreference = 
						new ProtegeReformulationPlatformPreferences();
				obdaFactory.setPreferenceHolder(reasonerPreference);
				owlEditorKit.put(ReformulationPlatformPreferences.class.getName(), 
						reasonerPreference);
			}
		}
	}
	
  private void addOntologyChangeListener() {
    final SynchronizedMappingController controller = (SynchronizedMappingController) mapcontroller;
    owlEditorKit.getOWLModelManager().addOntologyChangeListener(controller);
  }
	 
	private void addDatasourceControllerListener() {
		dscontroller.addDatasourceControllerListener(new DatasourcesControllerListener() {			
		  public void datasourceUpdated(String oldname, DataSource currendata) {
				triggerOntologyChanged();				
			}			
			public void datasourceDeleted(DataSource source) {
				triggerOntologyChanged();
			}			
			public void datasourceAdded(DataSource source) {
				triggerOntologyChanged();				
			}
			public void currentDatasourceChange(DataSource previousdatasource, DataSource currentsource) {
			  // Do nothing!
			}
			public void alldatasourcesDeleted() {
				triggerOntologyChanged();
			}
			public void datasourcParametersUpdated() {
				triggerOntologyChanged();
			}
		});
	}
	
  private void addMappingControllerListener() {
    mapcontroller.addMappingControllerListener(new MappingControllerListener() {
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
      public void ontologyChanged() {
        triggerOntologyChanged();
      }
    });
  }

	private void addQueryControllerListener() {
		queryController.addListener(new QueryControllerListener() {
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
		});
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
				AssertionControllerFactoryPluginInstance instance = pl
						.newInstance();
				Class assertionClass = instance.getAssertionClass();
				AssertionController controller = instance
						.getControllerInstance();
				AssertionXMLCodec xmlCodec = instance.getXMLCodec();
				boolean triggerUpddate = instance.triggersOntologyChanged();
				if (triggerUpddate) {
					controller.addControllerListener(defaultAssertionControllerListener);
				}
				addAssertionController(assertionClass, controller, xmlCodec);
			} catch (Throwable e) {
				ProtegeApplication.getErrorLog().logError(e);
			}
		}
	}

	@Override
	public File getCurrentOntologyFile() {
		currentOntologyPhysicalURI = owlEditorKit.getOWLModelManager()
				.getOntologyPhysicalURI(
						owlEditorKit.getOWLModelManager().getActiveOntology());

		if (currentOntologyPhysicalURI == null)
			return null;
		File owlfile = new File(currentOntologyPhysicalURI);
		File obdafile = new File(ioManager.getOBDAFile(owlfile.toURI()));
		return obdafile;
	}

	private final OWLModelManagerListener modelManagerListener = new OWLModelManagerListener() {

		public void handleChange(OWLModelManagerChangeEvent event) {
			EventType type = event.getType();
			OWLModelManager source = event.getSource();
			OWLOntology ontology = owlEditorKit.getOWLModelManager().getActiveOntology();

      switch (type) {
        case ABOUT_TO_CLASSIFY :
          break;
        case ENTITY_RENDERER_CHANGED :
          break;
        case ONTOLOGY_CLASSIFIED :
          break;
        case ACTIVE_ONTOLOGY_CHANGED :
          if (currentOntology != ontology) {
            OBDAPluginController.this.currentOntology = ontology;
            OBDAPluginController.this.currentOntologyURI = ontology.getURI();
            String uri = ontology.getURI().toString();
            if (loadedOntologies.add(uri)) {
              apicoupler.addNewOntologyInfo(ontology);
              loadData(source.getOntologyPhysicalURI(ontology));
            }
            try {
              mapcontroller.activeOntologyChanged();

            }
            catch (Exception e) {
              log.warn("Error changing the active ontology.");
            }
          }
          apicoupler.updateOntologies();
          break;
        case ENTITY_RENDERING_CHANGED :
          break;
        case ONTOLOGY_CREATED :
          break;
        case ONTOLOGY_LOADED :
          break;
        case ONTOLOGY_SAVED :
          break;
        case ONTOLOGY_VISIBILITY_CHANGED :
          break;
        case REASONER_CHANGED :
          break;
      }
		}
	};

	private OWLAPICoupler apicoupler;
	private boolean loadingData;

	private void triggerOntologyChanged() {
		if (!this.loadingData) {
			OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
			OWLOntology ontology = owlmm.getActiveOntology();

			if (ontology != null) {
				OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(
						URI.create(ontology.getURI()
								+ "#RandomClass6677841155"));
				OWLAxiom axiom = owlmm.getOWLDataFactory()
						.getOWLDeclarationAxiom(newClass);

				try {
					AddAxiom addChange = new AddAxiom(ontology, axiom);
					owlmm.applyChange(addChange);
	
					RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
					owlmm.applyChange(removeChange);
				} catch (Exception e) {
					log.warn("Exception while faking an ontology change. Your OBDA data might have new data that has not been noted and you must force an ontology save operation OR your ontology could have an extra declaration for a temporary class with URI: {}", newClass.getURI() );
					log.debug(e.getMessage(), e);
				}
			}
		}
	}

	public void setupModelManagerListener() {
		OWLModelManager owlmm = owlEditorKit.getModelManager();
		owlmm.addListener(modelManagerListener);
	}

	public void removeModelManagerListener() {
		owlEditorKit.getModelManager().removeListener(modelManagerListener);
	}
	
	public void addOntologyToCoupler(URI uri){
		OWLOntologyManager mmgr = ((OWLModelManagerImpl)owlEditorKit.getModelManager()).getOWLOntologyManager();
		apicoupler.addNewOntologyInfo(mmgr.getOntology(uri));
	}

	@Override
	public URI getPhysicalURIOfOntology(URI onto){
		
		Set<OWLOntology> set =owlEditorKit.getModelManager().getOntologies();
		Iterator<OWLOntology> it = set.iterator();
		while(it.hasNext()){
			OWLOntology o = it.next();
			if(o.getURI().equals(onto)){
				return owlEditorKit.getModelManager().getOntologyPhysicalURI(o);
			}
		}
		return null;
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	public void dispose() throws Exception {
		owlEditorKit.getModelManager().removeListener(modelManagerListener);

	}

	public void loadData(URI owlFile) {
		loadingData = true;
		try {
			apicoupler.addNewOntologyInfo(currentOntology);
			URI obdafile = getIOManager().getOBDAFile(owlFile);
			getIOManager().loadOBDADataFromURI(obdafile);
			String uri = currentOntology.getURI().toString();
			loadedOntologies.add(uri);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			loadingData = false;
		}
	}

	public void saveData(URI owlFile) {
		// TODO fix exception handling
		Logger.getLogger(OBDAPluginController.class).info(
				"Saving OBDA data for " + owlFile.toString());
		URI obdafile = getIOManager().getOBDAFile(owlFile);
		try {
			getIOManager().saveOBDAData(obdafile);
		} catch (Exception e) {
			e.printStackTrace();
			triggerOntologyChanged();
			Logger.getLogger(OBDAPluginController.class).error(e);
		}
	}

	@Override
	public Set<URI> getOntologyURIs() {
		HashSet<URI> uris = new HashSet<URI>();
		Set<OWLOntology> ontologies = owlEditorKit.getModelManager()
				.getOntologies();
		for (OWLOntology owlOntology : ontologies) {
			uris.add(owlOntology.getURI());
		}
		return uris;
	}

	@Override
	public void setCurrentOntologyURI(URI uri) {
		currentOntologyURI = uri;
		apicoupler.updateOntology(uri);
		mapcontroller.activeOntologyChanged();
	}

}
