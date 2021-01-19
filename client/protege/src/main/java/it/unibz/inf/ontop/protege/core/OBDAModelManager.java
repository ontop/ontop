package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.querymanager.*;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.converter.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import org.apache.commons.rdf.api.RDF;
import org.protege.editor.core.Disposable;
import org.protege.editor.core.ui.util.UIUtil;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.EntityCreationPreferences;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

public class OBDAModelManager implements Disposable {

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.

	private final OWLEditorKit owlEditorKit;

	private final OBDAModel obdaModel;

	private QueryController queryController;

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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<OWLOntologyID> lastKnownOntologyId = Optional.empty();

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

		PrefixDocumentFormat prefixFormat = PrefixUtilities.getPrefixOWLOntologyFormat(modelManager.getActiveOntology());
		obdaModel = new OBDAModel(ppMappingFactory, prefixFormat, termFactory,
				typeFactory, targetAtomFactory, substitutionFactory, rdfFactory, targetQueryParserFactory, sourceQueryFactory);

		ProtegeDatasourcesControllerListener dlistener = new ProtegeDatasourcesControllerListener();
		obdaModel.addSourceListener(dlistener);
		ProtegeMappingControllerListener mlistener = new ProtegeMappingControllerListener();
		obdaModel.addMappingsListener(mlistener);

		queryController = new QueryController();
		ProtegeQueryControllerListener qlistener = new ProtegeQueryControllerListener();
		queryController.addListener(qlistener);

		DisposableProperties settings = OBDAEditorKitSynchronizerPlugin.getProperties(owlEditorKit);
		configurationManager = new OntopConfigurationManager(obdaModel, settings);
	}

	public OntopConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
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

			OWLOntologyManager owlOntologyManager = owlEditorKit.getModelManager().getOWLOntologyManager();

			for (int idx = 0; idx < changes.size() ; idx++) {
				OWLOntologyChange change = changes.get(idx);
				if (change instanceof SetOntologyID) {
					OWLOntologyID newID = ((SetOntologyID) change).getNewOntologyID();
					log.debug("Ontology ID changed\nOld ID: {}\nNew ID: {}", ((SetOntologyID) change).getOriginalOntologyID(), newID);

					// if the OBDA model does not have an explicit namespace associated to the default prefix (":")
					if (!obdaModel.getExplicitDefaultPrefixNamespace().isPresent()) {
						generateDefaultPrefixNamespaceIfPossible(newID);
					}
				}
				else if (change instanceof AddImport) {
					AddImportData addedImport = ((AddImport) change).getChangeData();
					OWLOntology onto = owlOntologyManager.getOntology(addedImport.getDeclaration().getIRI());
					if (onto != null)
						for (OWLEntity entity : onto.getSignature())
							processEntity(entity,
									MutableOntologyVocabularyCategory::declare);
				}
				else if (change instanceof RemoveImport) {
					RemoveImportData removedImport = ((RemoveImport) change).getChangeData();
					OWLOntology onto = owlOntologyManager.getOntology(removedImport.getDeclaration().getIRI());
					if (onto != null)
						for (OWLEntity entity : onto.getSignature())
							processEntity(entity,
									MutableOntologyVocabularyCategory::remove);
				}
				else if (change instanceof AddAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						processEntity(((OWLDeclarationAxiom) axiom).getEntity(),
								MutableOntologyVocabularyCategory::declare);
					}
				}
				else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						processEntity(((OWLDeclarationAxiom) axiom).getEntity(),
								MutableOntologyVocabularyCategory::remove);
					}

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
				obdaModel.changePredicateIri(getIRI(e.getKey()), getIRI(e.getValue()));
			}

			for (OWLEntity removede : removals) {
				obdaModel.deletePredicateIRI(getIRI(removede));
			}
		}
	}

	private void processEntity(OWLEntity entity, BiConsumer<MutableOntologyVocabularyCategory, org.apache.commons.rdf.api.IRI> consumer) {
		MutableOntologyVocabulary vocabulary = obdaModel.getCurrentVocabulary();

		if (entity instanceof OWLClass) {
			OWLClass oc = (OWLClass) entity;
			consumer.accept(vocabulary.classes(), getIRI(oc));
		}
		else if (entity instanceof OWLObjectProperty) {
			OWLObjectProperty or = (OWLObjectProperty) entity;
			consumer.accept(vocabulary.objectProperties(), getIRI(or));
		}
		else if (entity instanceof OWLDataProperty) {
			OWLDataProperty op = (OWLDataProperty) entity;
			consumer.accept(vocabulary.dataProperties(), getIRI(op));
		}
		else if (entity instanceof  OWLAnnotationProperty ){
			OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
			consumer.accept(vocabulary.annotationProperties(), getIRI(ap));
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

	public QueryController getQueryController() {
		if (queryController == null) {
			queryController = new QueryController();
		}
		return queryController;
	}


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
		if (lastKnownOntologyId.filter(last -> last.equals(id)).isPresent())
			return;

		lastKnownOntologyId = Optional.of(id);

		PrefixDocumentFormat owlPrefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);
		obdaModel.reset(owlPrefixManager);

		for (OWLOntology onto : owlModelManager.getActiveOntologies()) {
			for (OWLEntity entity : onto.getSignature())
				processEntity(entity, MutableOntologyVocabularyCategory::declare);
		}

		Optional<String> ns = getDeclaredDefaultPrefixNamespace(ontology);
		if (ns.isPresent()) {
			obdaModel.setExplicitDefaultPrefixNamespace(ns.get());
		}
		else {
			generateDefaultPrefixNamespaceIfPossible(ontology.getOntologyID());
		}

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

				File queryFile = new File(URI.create(owlName + QUERY_EXT));
				try {
					// Load the saved queries
					QueryIOManager queryIO = new QueryIOManager(queryController);
					queryIO.load(queryFile);
				}
				catch (Exception ex) {
					throw new Exception("Exception occurred while loading Query document: " + queryFile + "\n\n" + ex.getMessage());
				}
			}
			else {
				log.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
			}
			// adding type information to the mapping predicates
			for (SQLPPTriplesMap mapping : obdaModel.generatePPMapping().getTripleMaps()) {
				ImmutableList<TargetAtom> tq = mapping.getTargetAtoms();
				ImmutableList<org.apache.commons.rdf.api.IRI> invalidIRIs = TargetQueryValidator.validate(tq, obdaModel.getCurrentVocabulary());
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
			if (obdaModel.hasTripleMaps()) {
				SQLPPMapping ppMapping = obdaModel.generatePPMapping();
				OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
				writer.write(obdaFile, ppMapping);
				log.info("mapping file saved to {}", obdaFile);
			}
			else {
				Files.deleteIfExists(obdaFile.toPath());
			}

			if (!queryController.getElements().isEmpty()) {
				File queryFile = new File(URI.create(owlName + QUERY_EXT));
				QueryIOManager queryIO = new QueryIOManager(queryController);
				queryIO.save(queryFile);
				log.info("query file saved to {}", queryFile);
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

	/*
	 * The following are internal helpers that dispatch "needs save" messages to
	 * the OWL ontology model when OBDA model changes.
	 */

	private class ProtegeDatasourcesControllerListener implements OBDAModelListener {
		@Override
		public void datasourceParametersUpdated() {
			triggerOntologyChanged();
		}
	}

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

	private class ProtegeQueryControllerListener implements QueryControllerListener {

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


	private void generateDefaultPrefixNamespaceIfPossible(OWLOntologyID ontologyID) {
		com.google.common.base.Optional<org.semanticweb.owlapi.model.IRI> ontologyIRI = ontologyID.getOntologyIRI();
		if (ontologyIRI.isPresent()) {
			String prefixUri = ontologyIRI.get().toString();
			if (!prefixUri.endsWith("#") && !prefixUri.endsWith("/")) {
				String defaultSeparator = EntityCreationPreferences.getDefaultSeparator();
				if (!prefixUri.endsWith(defaultSeparator))  {
					prefixUri += defaultSeparator;
				}
			}
			obdaModel.addPrefix(it.unibz.inf.ontop.spec.mapping.PrefixManager.DEFAULT_PREFIX, prefixUri);
		}
	}

	/**
	 *  Returns the namespace declared in the ontology for the default prefix.
	 */
	private static Optional<String> getDeclaredDefaultPrefixNamespace(OWLOntology ontology) {
		OWLOntologyXMLNamespaceManager nsm = new OWLOntologyXMLNamespaceManager(
				ontology,
				ontology.getOWLOntologyManager().getOntologyFormat(ontology));

		if (StreamSupport.stream(nsm.getPrefixes().spliterator(), false)
				.anyMatch(p ->  p.equals(""))) {
			return Optional.ofNullable(nsm.getNamespaceForPrefix(""));
		}
		return Optional.empty();
	}

}
