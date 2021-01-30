package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.converter.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.util.UIUtil;
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
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class OBDAModelManager implements Disposable {

	// Mutable
	private final OBDADataSource source = new OBDADataSource();
	// Mutable and replaced after reset
	private final OntologySignature currentMutableVocabulary = new OntologySignature();

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.

	private final OWLEditorKit owlEditorKit;

	private final OBDAModel obdaModel;

	private final QueryManager queryController = new QueryManager();

	private final List<OBDAModelManagerListener> obdaManagerListeners = new ArrayList<>();

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private final OntopConfigurationManager configurationManager;

	private final RDF rdfFactory;
	private final TypeFactory typeFactory;

	private static final Logger log = LoggerFactory.getLogger(OBDAModelManager.class);

	/***
	 * This is the instance responsible for listening for Protege ontology
	 * events (loading/saving/changing ontology)
	 */
	private final OWLModelManagerListener modelManagerListener = new OBDAPluginOWLModelManagerListener();

	/***
	 * This flag is used to avoid triggering a "Ontology Changed" event when new
	 * mappings/sources/queries are inserted into the model not by the user, but
	 * by a ontology load call.
	 */
	private boolean loadingData;

	@Nullable
	private OWLOntologyID lastKnownOntologyId;

	private static final IRI ONTOLOGY_UPDATE_TRIGGER = IRI.create("http://www.unibz.it/inf/obdaplugin#RandomClass6677841155");

	public OBDAModelManager(OWLEditorKit editorKit) {
		this.owlEditorKit = editorKit;

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

		SQLPPMappingFactory ppMappingFactory = defaultInjector.getInstance(SQLPPMappingFactory.class);
		TermFactory termFactory = defaultInjector.getInstance(TermFactory.class);
		typeFactory = defaultInjector.getInstance(TypeFactory.class);
		rdfFactory = defaultInjector.getInstance(RDF.class);
		TargetAtomFactory targetAtomFactory = defaultInjector.getInstance(TargetAtomFactory.class);
		SubstitutionFactory substitutionFactory = defaultInjector.getInstance(SubstitutionFactory.class);
		TargetQueryParserFactory targetQueryParserFactory = defaultInjector.getInstance(TargetQueryParserFactory.class);
		SQLPPSourceQueryFactory sourceQueryFactory = defaultInjector.getInstance(SQLPPSourceQueryFactory.class);

		OWLModelManager modelManager = owlEditorKit.getModelManager();
		modelManager.addListener(modelManagerListener);

		OWLOntologyManager mmgr = modelManager.getOWLOntologyManager();
		mmgr.addOntologyChangeListener(new OntologyRefactoringListener());

		obdaModel = new OBDAModel(modelManager.getActiveOntology(), ppMappingFactory, termFactory,
				targetAtomFactory, substitutionFactory, targetQueryParserFactory, sourceQueryFactory);

		obdaModel.addMappingsListener(new ProtegeMappingControllerListener());

		queryController.addListener(new ProtegeQueryControllerListener());

		source.addListener(this::triggerOntologyChanged);

		DisposableProperties settings = OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit);
		configurationManager = new OntopConfigurationManager(this, settings);
	}

	public OntopConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

	public RDF getRdfFactory() {
		return rdfFactory;
	}

	public OBDADataSource getDatasource() {
		return source;
	}


	/***
	 * This ontology change listener has some heuristics to determine if the
	 * user is refactoring the ontology. In particular, this listener will try
	 * to determine if some add/remove axioms are in fact a "renaming"
	 * operation. This happens when a list of axioms has a
	 * remove(DeclarationAxiom(x)) immediately followed by an
	 * add(DeclarationAxiom(y)). In this case, y is a new name for x.
	 */
	public class OntologyRefactoringListener implements OWLOntologyChangeListener {

		@Override
		public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
			Map<OWLEntity, OWLEntity> renamings = new HashMap<>();
			Set<OWLEntity> removals = new HashSet<>();

			for (int idx = 0; idx < changes.size() ; idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					OWLOntologyID newID = ((SetOntologyID) change).getNewOntologyID();
					log.debug("Ontology ID changed\nOld ID: {}\nNew ID: {}", ((SetOntologyID) change).getOriginalOntologyID(), newID);

					obdaModel.getMutablePrefixManager().updateOntologyID(newID);
				}
				else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					// renaming
					if (idx + 1 < changes.size() && changes.get(idx + 1) instanceof AddAxiom) {
						OWLAxiom nextAxiom = changes.get(idx + 1).getAxiom();
						if (axiom instanceof OWLDeclarationAxiom && nextAxiom instanceof OWLDeclarationAxiom) {
							OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
							OWLEntity nextEntity = ((OWLDeclarationAxiom) nextAxiom).getEntity();
							renamings.put(entity, nextEntity);
						}
					}
					else if (axiom instanceof OWLDeclarationAxiom) {
						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						// Hack: this has been done just to trigger a change in the ontology
						if (!entity.getIRI().equals(ONTOLOGY_UPDATE_TRIGGER))
							removals.add(entity);
					}
				}
			}

			for (Map.Entry<OWLEntity, OWLEntity> e : renamings.entrySet()) {
				obdaModel.renamePredicateInMapping(getIRI(e.getKey()), getIRI(e.getValue()));
			}

			for (OWLEntity removede : removals) {
				obdaModel.removePredicateFromMapping(getIRI(removede));
			}
		}
	}

	private org.apache.commons.rdf.api.IRI getIRI(OWLEntity entity) {
		return rdfFactory.createIRI(entity.getIRI().toString());
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

	public QueryManager getQueryController() {
		return queryController;
	}

	public OntologySignature getCurrentVocabulary() { return currentMutableVocabulary; }


	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			log.debug(event.getType().name());
			switch (event.getType()) {
				case ABOUT_TO_CLASSIFY:
					loadingData = true;
					break;

				case ONTOLOGY_CLASSIFIED:
					loadingData = false;
					break;

				case ACTIVE_ONTOLOGY_CHANGED:
					handleNewActiveOntology();
					break;

				case ONTOLOGY_LOADED:
				case ONTOLOGY_RELOADED:
					handleOntologyLoadedAndReLoaded();
					break;

				case ONTOLOGY_SAVED:
					handleOntologySaved();
					break;

				case ENTITY_RENDERER_CHANGED:
				case REASONER_CHANGED:
				case ONTOLOGY_VISIBILITY_CHANGED:
				case ONTOLOGY_CREATED:
				case ENTITY_RENDERING_CHANGED:
					break;
			}
		}
	}

	/**
	 * When the active ontology is new (first one or differs from the last one)
	 */
	private void handleNewActiveOntology() {
		OWLModelManager owlModelManager = owlEditorKit.getModelManager();
		OWLOntology ontology = owlModelManager.getActiveOntology();
		OWLOntologyID id = ontology.getOntologyID();
		if (id.equals(lastKnownOntologyId))
			return;

		lastKnownOntologyId = id;

		obdaModel.reset(ontology);
		currentMutableVocabulary.setOntology(ontology);

		DisposableProperties settings = OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit);
		configurationManager.reset(settings);

		ProtegeOWLReasonerInfo factory = owlModelManager.getOWLReasonerManager().getCurrentReasonerFactory();
		if (factory instanceof OntopReasonerInfo) {
			OntopReasonerInfo questfactory = (OntopReasonerInfo) factory;
			questfactory.setConfigurationGenerator(configurationManager);
		}
		fireActiveOBDAModelChange();
	}

	private void handleOntologyLoadedAndReLoaded() {
		loadingData = true; // flag on
		try {
			OWLModelManager owlModelManager = owlEditorKit.getModelManager();
			IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(owlModelManager.getActiveOntology());
			String owlName = getOwlName(documentIRI);
			if (owlName == null)
				return;

			File obdaFile = new File(URI.create(owlName + OBDA_EXT));
			if (obdaFile.exists()) {
				configurationManager.loadNewConfiguration(owlName);

				try (Reader mappingReader = new FileReader(obdaFile)) {
					OldSyntaxMappingConverter converter = new OldSyntaxMappingConverter(mappingReader, obdaFile.getName());
					java.util.Optional<Properties> optionalDataSourceProperties = converter.getOBDADataSourceProperties();

					optionalDataSourceProperties.ifPresent(configurationManager::loadProperties);
					obdaModel.parseMapping(new StringReader(converter.getRestOfFile()), configurationManager.snapshotProperties());
				}
				catch (Exception ex) {
					throw new Exception("Exception occurred while loading OBDA document: " + obdaFile + "\n\n" + ex.getMessage());
				}

				File queriesFile = new File(URI.create(owlName + QUERY_EXT));
				if (queriesFile.exists()) {
					try (FileReader reader = new FileReader(queriesFile)) {
						queryController.load(reader);
					}
					catch (Exception ex) {
						throw new Exception("Exception occurred while loading query document: " + queriesFile + "\n\n" + ex.getMessage());
					}
				}
			}
			else {
				log.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
			}
			// adding type information to the mapping predicates
			for (SQLPPTriplesMap mapping : obdaModel.getMapping()) {
				ImmutableList<TargetAtom> tq = mapping.getTargetAtoms();
				ImmutableList<org.apache.commons.rdf.api.IRI> invalidIRIs = currentMutableVocabulary.validate(tq);
				if (!invalidIRIs.isEmpty()) {
					throw new Exception("Found an invalid target query: \n" +
							"  mappingId:\t" + mapping.getId() + "\n" +
							(mapping.getOptionalTargetString().map(s -> "  target:\t" + s + "\n").orElse("")) +
							"  predicates not declared in the ontology: " + invalidIRIs);
				}
			}
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

	private void handleOntologySaved() {
		try {
			OWLModelManager owlModelManager = owlEditorKit.getModelManager();
			IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(owlModelManager.getActiveOntology());
			String owlName = getOwlName(documentIRI);
			if (owlName == null)
				return;

			File obdaFile = new File(URI.create(owlName + OBDA_EXT));
			if (!obdaModel.getMapping().isEmpty()) {
				OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
				writer.write(obdaFile, obdaModel.generatePPMapping());
				log.info("mapping file saved to {}", obdaFile);
			}
			else {
				Files.deleteIfExists(obdaFile.toPath());
			}

			File queriesFile = new File(URI.create(owlName + QUERY_EXT));
			if (!queryController.getGroups().isEmpty()) {
				try (FileWriter writer = new FileWriter(queriesFile)) {
					writer.write(queryController.renderQueries());
				}
				catch (IOException e) {
					throw new IOException(String.format("Error while saving the queries to file located at %s.\n" +
							"Make sure you have the write permission at the location specified.", queriesFile.getAbsolutePath()));
				}
				log.info("query file saved to {}", queriesFile);
			}
			else {
				Files.deleteIfExists(queriesFile.toPath());
			}

			File propertyFile = new File(URI.create(owlName + OntopConfigurationManager.PROPERTY_EXT));
			Properties properties = configurationManager.snapshotUserProperties();
			// Generate a property file iff there is at least one property that is not "jdbc.name"
			if (properties.entrySet().stream()
					.anyMatch(e -> !e.getKey().equals(OntopSQLCoreSettings.JDBC_NAME) &&
							!e.getValue().equals(""))){
				try (FileOutputStream outputStream = new FileOutputStream(propertyFile)) {
					properties.store(outputStream, null);
				}
				log.info("Property file saved to {}", propertyFile.toPath());
			}
			else {
				Files.deleteIfExists(propertyFile.toPath());
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
			Exception newException = new Exception(
					"Error saving the OBDA file. Closing Protege now can result in losing changes in your data sources or mappings. Please resolve the issue that prevents saving in the current location, or do \"Save as..\" to save in an alternative location. \n\nThe error message was: \n"
							+ e.getMessage());
			DialogUtils.showQuickErrorDialog(null, newException, "Error saving OBDA file");
			triggerOntologyChanged();
		}
	}

	private static String getOwlName(IRI documentIRI) {
		if (!UIUtil.isLocalFile(documentIRI.toURI())) {
			return null;
		}

		String owlDocumentIriString = documentIRI.toString();
		int i = owlDocumentIriString.lastIndexOf(".");
		return owlDocumentIriString.substring(0, i);
	}

	public void fireActiveOBDAModelChange() {
		for (OBDAModelManagerListener listener : obdaManagerListeners) {
			try {
				listener.activeOntologyChanged();
			}
			catch (Exception e) {
				log.debug("Badly behaved listener: {}", listener.getClass().toString());
				log.debug(e.getMessage(), e);
			}
		}
	}

	/***
	 * Protege won't trigger a save action unless it detects that the
	 * currently open OWLOntology has changed. The OBDA plugin requires that
	 * protege triggers a save action also in the case when only the OBDA model
	 * has changed. To accomplish this, this method will "fake" an
	 * ontology change by inserting and removing a class into the OWLModel.
	 */
	private void triggerOntologyChanged() {
		if (loadingData) {
			return;
		}
		OWLModelManager owlModelManager = owlEditorKit.getModelManager();
		OWLOntology ontology = owlModelManager.getActiveOntology();
		if (ontology == null) {
			return;
		}

		try {
			OWLDataFactory owlDatafactory = owlModelManager.getOWLDataFactory();
			OWLClass newClass = owlDatafactory.getOWLClass(ONTOLOGY_UPDATE_TRIGGER);
			OWLAxiom axiom = owlDatafactory.getOWLDeclarationAxiom(newClass);

			owlModelManager.applyChange(new AddAxiom(ontology, axiom));
			owlModelManager.applyChange(new RemoveAxiom(ontology, axiom));
		}
		catch (Exception e) {
			log.warn("Exception forcing an ontology change. " +
							"Your OWL model might contain a new class that " +
							"you need to remove manually: {}", ONTOLOGY_UPDATE_TRIGGER);
			log.warn(e.getMessage());
			log.debug(e.getMessage(), e);
		}
	}

	/***
	 * Called from ModelManager dispose method since this object is setup as the
	 * APIController.class.getName() property with the put method.
	 */
	@Override
	public void dispose() {
		try {
			owlEditorKit.getModelManager().removeListener(modelManagerListener);
			connectionManager.dispose();
		}
		catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	public List<AddAxiom> insertOntologyDeclarations(ImmutableList<SQLPPTriplesMap> triplesMaps, boolean bootstraped) {
		OWLModelManager modelManager = owlEditorKit.getModelManager();
		OWLOntologyManager manager = modelManager.getActiveOntology().getOWLOntologyManager();
		Set<OWLDeclarationAxiom> declarationAxioms = MappingOntologyUtils.extractDeclarationAxioms(
				manager,
				triplesMaps.stream().flatMap(ax -> ax.getTargetAtoms().stream()),
				typeFactory,
				bootstraped);

		List<AddAxiom> addAxioms = declarationAxioms.stream()
				.map(ax -> new AddAxiom(modelManager.getActiveOntology(), ax))
				.collect(Collectors.toList());

		modelManager.applyChanges(addAxioms);

		return addAxioms;
	}

	public OntopSQLOWLAPIConfiguration getConfigurationForOntology() {
		OWLModelManager modelManager = owlEditorKit.getModelManager();
		OntopSQLOWLAPIConfiguration configuration = getConfigurationManager()
				.buildOntopSQLOWLAPIConfiguration(modelManager.getActiveOntology());

		return configuration;
	}

	/*
	 * The following are internal helpers that dispatch "needs save" messages to
	 * the OWL ontology model when OBDA model changes.
	 */


	private class ProtegeMappingControllerListener implements OBDAMappingListener {
		@Override
		public void mappingDeleted() {
			triggerOntologyChanged();
		}

		@Override
		public void mappingInserted() {
			triggerOntologyChanged();
		}

		@Override
		public void mappingUpdated() {  triggerOntologyChanged(); }
	}

	private class ProtegeQueryControllerListener implements QueryManager.EventListener {
		@Override
		public void added(QueryManager.Group group) {
			triggerOntologyChanged();
		}

		@Override
		public void added(QueryManager.Query query) {
			triggerOntologyChanged();
		}

		@Override
		public void removed(QueryManager.Group group) {
			triggerOntologyChanged();
		}

		@Override
		public void removed(QueryManager.Query query) {
			triggerOntologyChanged();
		}

		@Override
		public void changed(QueryManager.Query query) {
			triggerOntologyChanged();
		}
	}




}
