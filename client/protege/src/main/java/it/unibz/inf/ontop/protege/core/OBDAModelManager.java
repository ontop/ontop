package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
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

import com.google.inject.Injector;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.converter.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.utils.querymanager.*;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class OBDAModelManager implements Disposable {
    private static final Logger log = LoggerFactory.getLogger(OBDAModelManager.class);

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.
	private static final String PROPERTY_EXT = ".properties"; // The default property file extension.
	private static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.

	OWLEditorKit owlEditorKit;

	TermFactory termFactory;
	TypeFactory typeFactory;
	JdbcTypeMapper jdbcTypeMapper;

	private final QueryController queryController = new QueryController(new ProtegeQueryControllerListener());

	private OBDAModel obdaModel;

	private final List<OBDAModelManagerListener> obdaManagerListeners = new ArrayList<>();

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private OntopConfigurationManager configurationManager;

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private OWLModelManagerListener modelManagerListener;

	private final ProtegeMappingControllerListener mlistener = new ProtegeMappingControllerListener();
	private final ProtegeDatasourcesControllerListener dlistener = new ProtegeDatasourcesControllerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	boolean loadingData;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private java.util.Optional<OWLOntologyID> lastKnownOntologyId = java.util.Optional.empty();
	AtomFactory atomFactory;
	Relation2Predicate relation2Predicate;
	DatalogFactory datalogFactory;

	public OBDAModelManager(){

    }

	public OBDAModelManager(EditorKit editorKit) {

		/*
		 * TODO: avoid this use
		 */
		// Default injector
		Injector defaultInjector = OntopMappingSQLAllConfiguration.defaultBuilder()
				.jdbcDriver("")
				.jdbcUrl("")
				.jdbcUser("")
				.jdbcPassword("")
				.build().getInjector();

		SpecificationFactory specificationFactory = defaultInjector.getInstance(SpecificationFactory.class);
		SQLPPMappingFactory ppMappingFactory = defaultInjector.getInstance(SQLPPMappingFactory.class);
		atomFactory = defaultInjector.getInstance(AtomFactory.class);
		termFactory = defaultInjector.getInstance(TermFactory.class);
		typeFactory = defaultInjector.getInstance(TypeFactory.class);
		datalogFactory = defaultInjector.getInstance(DatalogFactory.class);
		relation2Predicate = defaultInjector.getInstance(Relation2Predicate.class);
		jdbcTypeMapper = defaultInjector.getInstance(JdbcTypeMapper.class);

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA Plugin only works with OWLEditorKit instances.");
		}
		owlEditorKit = (OWLEditorKit) editorKit;
		updateModelManagerListener(new OBDAPluginOWLModelManagerListener());

		// Adding ontology change listeners to synchronize with the mappings
        owlEditorKit.getModelManager().getOWLOntologyManager().addOntologyChangeListener(new OntologyRefactoringListener(owlEditorKit, this));

        setActiveModel(new OBDAModel(specificationFactory, ppMappingFactory,
                PrefixUtilities.getPrefixOWLOntologyFormat(owlEditorKit.getModelManager().getActiveOntology()),
                atomFactory, termFactory, typeFactory, datalogFactory, relation2Predicate, jdbcTypeMapper));

		// Printing the version information to the console
		//System.out.println("Using " + VersionInfo.getVersionInfo().toString() + "\n");

		setConfigurationManager(new OntopConfigurationManager(getActiveOBDAModel(),
				(DisposableProperties) owlEditorKit.get(DisposableProperties.class.getName())));
	}

	void updateModelManagerListener(OWLModelManagerListener listener){
		if(listener!=null) {
			if (modelManagerListener != null) {
				owlEditorKit.getModelManager().removeListener(modelManagerListener);
			}
			modelManagerListener = listener;
			owlEditorKit.getModelManager().addListener(modelManagerListener);
		}
	}

	void setConfigurationManager(OntopConfigurationManager configurationManager){
		this.configurationManager = configurationManager;
	}

	void setActiveModel(OBDAModel obdaModel){
		this.obdaModel = obdaModel;
		this.obdaModel.addSourceListener(dlistener);
		this.obdaModel.addMappingsListener(mlistener);
	}

	public OntopConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public AtomFactory getAtomFactory() {
		return atomFactory;
	}

	public TermFactory getTermFactory() {
		return termFactory;
	}

	public DatalogFactory getDatalogFactory() {
		return datalogFactory;
	}

	public JdbcTypeMapper getJdbcTypeMapper() {
		return jdbcTypeMapper;
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}


	public void addListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.add(listener);
	}

	public void removeListener(OBDAModelManagerListener listener) {
		obdaManagerListeners.remove(listener);
	}

	public OBDAModel getActiveOBDAModel() {
		return obdaModel;
	}

	public QueryController getQueryController() {
		return queryController;
	}

    void loadOBDAFile(String owlName) throws Exception {
        File obdaFile = new File(URI.create(owlName + OBDA_EXT));
        File queryFile = new File(URI.create(owlName + QUERY_EXT));
        if (obdaFile.exists()) {
            try {
                //convert old syntax OBDA file
                Reader mappingReader = new FileReader(obdaFile);
                OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(new FileReader(obdaFile), obdaFile.getName());
                java.util.Optional<Properties> optionalDataSourceProperties = converter.getOBDADataSourceProperties();

                if (optionalDataSourceProperties.isPresent()) {
                    configurationManager.loadProperties(optionalDataSourceProperties.get());
                    mappingReader = converter.getOutputReader();
                }
                // Load the OBDA model
                getActiveOBDAModel().parseMapping(mappingReader, configurationManager.snapshotProperties());

            } catch (Exception ex) {
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
        } else {
            log.warn("OBDA model couldn't be loaded because no .obda file exists in the same location as the .owl file");
        }
        // adding type information to the mapping predicates
        for (SQLPPTriplesMap mapping : getActiveOBDAModel().generatePPMapping().getTripleMaps()) {
            List<? extends Function> tq = mapping.getTargetAtoms();
            if (!TargetQueryValidator.validate(tq, getActiveOBDAModel().getCurrentVocabulary()).isEmpty()) {
                throw new Exception("Found an invalid target query: " + tq.toString());
            }
        }
    }

    void loadProperties(String owlName) throws IOException {
        // Loads the properties (and the data source)
        File propertyFile = new File(URI.create(owlName + PROPERTY_EXT));
        if (propertyFile.exists()) {
            configurationManager.loadPropertyFile(propertyFile);
        }
    }

	/**
	 * Internal class responsible for coordinating actions related to updates in
	 * the ontology environment.
	 */
	public class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {

		public boolean initializing = false;

		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {

			// Get the active ontology
			OWLModelManager source = event.getSource();
			OWLOntology activeOntology = source.getActiveOntology();

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
					OWLOntologyID id = activeOntology.getOntologyID();

					if (!lastKnownOntologyId
							.filter(last -> last.equals(id))
							.isPresent()) {
						lastKnownOntologyId = java.util.Optional.of(id);
						handleNewActiveOntology();
					}
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
					break;
			}
		}

		/**
		 * When the active ontology is new (first one or differs from the last one)
		 */
		void handleNewActiveOntology() {
			initializing = true; // flag on

			OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

			OWLOntology ontology = mmgr.getActiveOntology();
			PrefixDocumentFormat owlPrefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);

			// Resets the OBDA and loads the vocabulary and the prefixes
			getActiveOBDAModel().reset(owlPrefixManager);
			loadVocabularyAndDefaultPrefix(getActiveOBDAModel(), mmgr.getOntologies(), ontology);

			configurationManager.clearImplicitDBConstraintFile();
			DisposableProperties settings = (DisposableProperties) owlEditorKit.get(DisposableProperties.class.getName());
			configurationManager.resetProperties(settings.clone());


			ProtegeOWLReasonerInfo factory = owlEditorKit.getOWLModelManager().getOWLReasonerManager().getCurrentReasonerFactory();
			if (factory instanceof OntopReasonerInfo) {
				OntopReasonerInfo questfactory = (OntopReasonerInfo) factory;
				questfactory.setConfigurationGenerator(configurationManager);
			}
			fireActiveOBDAModelChange();

			initializing = false; // flag off
		}

		void handleOntologyLoadedAndReLoaded(OWLModelManager owlModelManager, OWLOntology activeOntology) {
			loadingData = true; // flag on
			try {
				IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);

				if (!UIUtil.isLocalFile(documentIRI.toURI())) {
					return;
				}

				String owlDocumentIriString = documentIRI.toString();
                String owlName = owlDocumentIriString.substring(0, owlDocumentIriString.lastIndexOf("."));

                File implicitDBConstraintFile = new File(URI.create(owlName + DBPREFS_EXT));
				if(implicitDBConstraintFile.exists())
					configurationManager.setImplicitDBConstraintFile(implicitDBConstraintFile);

				loadProperties(owlName);
                loadOBDAFile(owlName);
			}
			catch (Exception e) {
				InvalidOntopConfigurationException ex = new InvalidOntopConfigurationException("An exception has occurred when loading input file.\nMessage: " + e.getMessage());
				DialogUtils.showQuickErrorDialog(null, ex, "Open file error");
				log.error(e.getMessage());
			}
			finally {
				loadingData = false; // flag off
				fireActiveOBDAModelChange();
			}
		}

		void handleOntologySaved(OWLModelManager owlModelManager, OWLOntology activeOntology) {
			try {
				IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);
				String owlDocumentIriString = documentIRI.toString();

				if(!UIUtil.isLocalFile(documentIRI.toURI())){
					return;
				}

				//String owlName = Files.getNameWithoutExtension(owlDocumentIriString);

				int i = owlDocumentIriString.lastIndexOf(".");
				String owlName = owlDocumentIriString.substring(0,i);

				String obdaDocumentIri = owlName + OBDA_EXT;
				String queryDocumentIri = owlName + QUERY_EXT;

				// Save the OBDA model
				File obdaFile = new File(URI.create(obdaDocumentIri));
				SQLPPMapping ppMapping = getActiveOBDAModel().generatePPMapping();
				OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(ppMapping);
				writer.save(obdaFile);

				log.info("mapping file saved to {}", obdaFile);

				if (!queryController.getElements().isEmpty()) {
					// Save the queries
					File queryFile = new File(URI.create(queryDocumentIri));
					QueryIOManager queryIO = new QueryIOManager(queryController);
					queryIO.save(queryFile);
					log.info("query file saved to {}", queryFile);
				}


				Properties properties = configurationManager.snapshotUserProperties();
				if (!properties.isEmpty()) {
					String propertyFilePath = owlName + PROPERTY_EXT;
					File propertyFile = new File(URI.create(propertyFilePath));
					FileOutputStream outputStream = new FileOutputStream(propertyFile);
					properties.store(outputStream, null);
					outputStream.flush();
					outputStream.close();
					log.info("Property file saved to {}", propertyFilePath);
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

	private static void loadVocabularyAndDefaultPrefix(OBDAModel obdaModel, Set<OWLOntology> ontologies,
													   OWLOntology activeOntology) {
		for (OWLOntology ontology : ontologies) {
			// Setup the entity declarations
			for (OWLClass c : ontology.getClassesInSignature())
				obdaModel.getCurrentVocabulary().classes().declare(c.getIRI());

			for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature())
				obdaModel.getCurrentVocabulary().objectProperties().declare(r.getIRI());

			for (OWLDataProperty p : ontology.getDataPropertiesInSignature())
				obdaModel.getCurrentVocabulary().dataProperties().declare(p.getIRI());

			for (OWLAnnotationProperty p : ontology.getAnnotationPropertiesInSignature())
				obdaModel.getCurrentVocabulary().annotationProperties().declare(p.getIRI());
		}

		String unsafeDefaultPrefix = activeOntology.getOntologyID().getOntologyIRI()
				.transform(IRI::toString)
				.or("");
		obdaModel.addPrefix(PrefixManager.DEFAULT_PREFIX, OBDAModelManager.getProperPrefixURI(unsafeDefaultPrefix));
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
	 * has suffered changes. To accomplish this, this method will "fake" an
	 * ontology change by inserting and removing a class into the OWLModel.
	 *
	 */
	void triggerOntologyChanged() {
		if (loadingData) {
			return;
		}
		OWLModelManager owlmm = owlEditorKit.getOWLModelManager();
		OWLOntology ontology = owlmm.getActiveOntology();

		if (ontology == null) {
			return;
		}

		OWLClass newClass = owlmm.getOWLDataFactory().getOWLClass(IRI.create("http://www.unibz.it/inf/obdaplugin#RandomClass6677841155"));
		OWLAxiom axiom = owlmm.getOWLDataFactory().getOWLDeclarationAxiom(newClass);

		try {
			AddAxiom addChange = new AddAxiom(ontology, axiom);
			owlmm.applyChange(addChange);
			RemoveAxiom removeChange = new RemoveAxiom(ontology, axiom);
			owlmm.applyChange(removeChange);
//			owlmm.fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
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
			owlEditorKit.getModelManager().removeListener(modelManagerListener);
			connectionManager.dispose();
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
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
		public void mappingDeleted(URI srcuri ) {
			triggerOntologyChanged();
		}

		@Override
		public void mappingInserted(URI srcuri ) {
			triggerOntologyChanged();
		}

		@Override
		public void mappingUpdated() {  triggerOntologyChanged(); }
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

	/**
	 * A utility method to ensure a proper naming for prefix URI
	 */
    static String getProperPrefixURI(String prefixUri) {
		if (!prefixUri.endsWith("#")) {
			if (!prefixUri.endsWith("/")) {
				String defaultSeparator = EntityCreationPreferences.getDefaultSeparator();
				if (!prefixUri.endsWith(defaultSeparator))  {
					prefixUri += defaultSeparator;
				}
			}
		}
		return prefixUri;
	}
}
