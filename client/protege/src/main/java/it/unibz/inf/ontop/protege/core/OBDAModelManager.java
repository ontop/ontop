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

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.reformulation.generation.impl.Relation2Predicate;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.converter.OldSyntaxMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.querymanager.*;
import org.apache.commons.rdf.api.RDF;
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
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;

public class OBDAModelManager implements Disposable {

	private static final String OBDA_EXT = ".obda"; // The default OBDA file extension.
	private static final String QUERY_EXT = ".q"; // The default query file extension.
	private static final String PROPERTY_EXT = ".properties"; // The default property file extension.
	private static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.

	private final OWLEditorKit owlEditorKit;

	private final OWLOntologyManager mmgr;
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final JdbcTypeMapper jdbcTypeMapper;

	private QueryController queryController;

	private final OBDAModel obdaModel;

	private final List<OBDAModelManagerListener> obdaManagerListeners;

	private final JDBCConnectionManager connectionManager = JDBCConnectionManager.getJDBCConnectionManager();

	private final OntopConfigurationManager configurationManager;

	private static final Logger log = LoggerFactory.getLogger(OBDAModelManager.class);
	private final RDF rdfFactory;

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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private java.util.Optional<OWLOntologyID> lastKnownOntologyId;
	private final AtomFactory atomFactory;
	private final Relation2Predicate relation2Predicate;
	private final DatalogFactory datalogFactory;
	private final TargetAtomFactory targetAtomFactory;

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
		targetAtomFactory = defaultInjector.getInstance(TargetAtomFactory.class);
		rdfFactory = defaultInjector.getInstance(RDF.class);
		TargetAtomFactory targetAtomFactory = defaultInjector.getInstance(TargetAtomFactory.class);
		SubstitutionFactory substitutionFactory = defaultInjector.getInstance(SubstitutionFactory.class);

		lastKnownOntologyId = java.util.Optional.empty();

		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA Plugin only works with OWLEditorKit instances.");
		}
		this.owlEditorKit = (OWLEditorKit) editorKit;
		OWLModelManager modelManager = owlEditorKit.getModelManager();
		mmgr = modelManager.getOWLOntologyManager();
		OWLModelManager owlmmgr = (OWLModelManager) editorKit.getModelManager();
		owlmmgr.addListener(modelManagerListener);

		obdaManagerListeners = new ArrayList<>();

		// Adding ontology change listeners to synchronize with the mappings
		mmgr.addOntologyChangeListener(new OntologyRefactoringListener());

		// Initialize the query controller
		queryController = new QueryController();

		PrefixDocumentFormat prefixFormat = PrefixUtilities.getPrefixOWLOntologyFormat(modelManager.getActiveOntology());
		obdaModel = new OBDAModel(specificationFactory, ppMappingFactory, prefixFormat, atomFactory, termFactory,
				typeFactory, datalogFactory, targetAtomFactory, substitutionFactory, jdbcTypeMapper, rdfFactory);
		obdaModel.addSourceListener(dlistener);
		obdaModel.addMappingsListener(mlistener);
		queryController.addListener(qlistener);

		// Printing the version information to the console
		//	System.out.println("Using " + VersionInfo.getVersionInfo().toString() + "\n");

		DisposableProperties settings = (DisposableProperties) owlEditorKit.get(DisposableProperties.class.getName());
		configurationManager = new OntopConfigurationManager(obdaModel, settings);
	}

	public OntopConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public AtomFactory getAtomFactory() {
		return atomFactory;
	}

	public Relation2Predicate getRelation2Predicate() {
		return relation2Predicate;
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

	public TargetAtomFactory getTargetAtomFactory() {
		return targetAtomFactory;
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
					log.debug("Ontology ID changed");
					log.debug("Old ID: {}", ((SetOntologyID) change).getOriginalOntologyID());
					OWLOntologyID newID = ((SetOntologyID) change).getNewOntologyID();
					log.debug("New ID: {}", newID);

					// if the OBDA model does not have an explicit namespace associated to the default prefix (":")
					if(!obdaModel.getExplicitDefaultPrefixNamespace().isPresent()){
						MutablePrefixManager.generateDefaultPrefixNamespaceFromID(newID).ifPresent(
								id -> obdaModel.addPrefix(
										MutablePrefixManager.DEFAULT_PREFIX,
										id
								));
					}
					continue;
				}
				else if (change instanceof AddImport) {

					AddImportData addedImport = ((AddImport) change).getChangeData();
					IRI addedOntoIRI = addedImport.getDeclaration().getIRI();

					OWLOntology addedOnto = mmgr.getOntology(addedOntoIRI);
					OBDAModel activeOBDAModel = getActiveOBDAModel();

					// Setup the entity declarations
					for (OWLClass c : addedOnto.getClassesInSignature())
						activeOBDAModel.getCurrentVocabulary().classes().declare(getIRI(c));

					for (OWLObjectProperty r : addedOnto.getObjectPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().objectProperties().declare(getIRI(r));

					for (OWLDataProperty p : addedOnto.getDataPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().dataProperties().declare(getIRI(p));

					for (OWLAnnotationProperty p : addedOnto.getAnnotationPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().annotationProperties().declare(getIRI(p));

					continue;
				}
				else if (change instanceof RemoveImport) {

					RemoveImportData removedImport = ((RemoveImport) change).getChangeData();
					IRI removedOntoIRI = removedImport.getDeclaration().getIRI();

					OWLOntology removedOnto = mmgr.getOntology(removedOntoIRI);
					OBDAModel activeOBDAModel = getActiveOBDAModel();

					for (OWLClass c : removedOnto.getClassesInSignature())
						activeOBDAModel.getCurrentVocabulary().classes().remove(getIRI(c));

					for (OWLObjectProperty r : removedOnto.getObjectPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().objectProperties().remove(getIRI(r));

					for (OWLDataProperty p : removedOnto.getDataPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().dataProperties().remove(getIRI(p));

					for (OWLAnnotationProperty p : removedOnto.getAnnotationPropertiesInSignature())
						activeOBDAModel.getCurrentVocabulary().annotationProperties().remove(getIRI(p));

					continue;
				}
				else if (change instanceof AddAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {

						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						OBDAModel activeOBDAModel = getActiveOBDAModel();
						if (entity instanceof OWLClass) {
							OWLClass oc = (OWLClass) entity;
							activeOBDAModel.getCurrentVocabulary().classes().declare(getIRI(oc));
						}
						else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							activeOBDAModel.getCurrentVocabulary().objectProperties().declare(getIRI(or));
						}
						else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							activeOBDAModel.getCurrentVocabulary().dataProperties().declare(getIRI(op));
						}
						else if (entity instanceof OWLAnnotationProperty){
							OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
							activeOBDAModel.getCurrentVocabulary().annotationProperties().declare(getIRI(ap));
						}
					}
				}
				else if (change instanceof RemoveAxiom) {
					OWLAxiom axiom = change.getAxiom();
					if (axiom instanceof OWLDeclarationAxiom) {
						OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
						OBDAModel activeOBDAModel = getActiveOBDAModel();
						if (entity instanceof OWLClass) {
							OWLClass oc = (OWLClass) entity;
							activeOBDAModel.getCurrentVocabulary().classes().remove(getIRI(oc));
						}
						else if (entity instanceof OWLObjectProperty) {
							OWLObjectProperty or = (OWLObjectProperty) entity;
							activeOBDAModel.getCurrentVocabulary().objectProperties().remove(getIRI(or));
						}
						else if (entity instanceof OWLDataProperty) {
							OWLDataProperty op = (OWLDataProperty) entity;
							activeOBDAModel.getCurrentVocabulary().dataProperties().remove(getIRI(op));
						}

						else if (entity instanceof  OWLAnnotationProperty ){
							OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
							activeOBDAModel.getCurrentVocabulary().annotationProperties().remove(getIRI(ap));
						}

					}
				}

				 if (idx + 1 < changes.size() && change instanceof RemoveAxiom && changes.get(idx + 1) instanceof AddAxiom) {

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

				}
				else if (change instanceof RemoveAxiom && change.getAxiom() instanceof OWLDeclarationAxiom) {
					// Found the pattern of a deletion
					OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) change.getAxiom();
					OWLEntity removedEntity = declaration.getEntity();

					if(removedEntity.getIRI().toQuotedString().equals("<http://www.unibz.it/inf/obdaplugin#RandomClass6677841155>")){
						//Hack this has been done just to trigger a change int the ontology
						continue;
					 }
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
				org.apache.commons.rdf.api.IRI removedIRI = getIRI(removedEntity);
				org.apache.commons.rdf.api.IRI newIRI = getIRI(newEntity);

				obdamodel.changePredicateIri(removedIRI, newIRI);
			}

			// Applying the deletions to the obda model
			for (OWLEntity removede : removals) {
				org.apache.commons.rdf.api.IRI removedIRI = getIRI(removede);
				obdamodel.deletePredicateIRI(removedIRI);
			}
		}

//		private void updateOntologyID(SetOntologyID change) {
//			// original ontology id
//			OWLOntologyID originalOntologyID = change.getOriginalOntologyID();
//			Optional<IRI> oldOntologyIRI = originalOntologyID.getOntologyIRI();
//
//			URI oldiri = null;
//			if(oldOntologyIRI.isPresent()) {
//				oldiri = oldOntologyIRI.get().toURI();
//			}
//			else {
//				oldiri = URI.create(originalOntologyID.toString());
//			}
//
//			log.debug("Ontology ID changed");
//			log.debug("Old ID: {}", oldiri);
//
//			// new ontology id
//			OWLOntologyID newOntologyID = change.getNewOntologyID();
//			Optional<IRI> optionalNewIRI = newOntologyID.getOntologyIRI();
//
//			URI newiri = null;
//			if(optionalNewIRI.isPresent()) {
//				newiri = optionalNewIRI.get().toURI();
//				obdaModel.addPrefix(PrefixManager.DEFAULT_PREFIX, MutablePrefixManager.getProperPrefixURI(newiri.toString()));
//			}
//			else {
//				newiri = URI.create(newOntologyID.toString());
//				obdaModel.addPrefix(PrefixManager.DEFAULT_PREFIX, "");
//			}
//
//			log.debug("New ID: {}", newiri);
//		}
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

	/**
	 * Internal class responsible for coordinating actions related to updates in
	 * the ontology environment.
	 */
	private class OBDAPluginOWLModelManagerListener implements OWLModelManagerListener {

		boolean initializing = false;

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
		private void handleNewActiveOntology() {
			initializing = true; // flag on

			OWLModelManager mmgr = owlEditorKit.getOWLWorkspace().getOWLModelManager();

			OWLOntology ontology = mmgr.getActiveOntology();
			PrefixDocumentFormat owlPrefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(ontology);

			// Resets the OBDA and loads the vocabulary and the prefixes
			obdaModel.reset(owlPrefixManager);
			loadVocabularyAndDefaultPrefix(obdaModel, mmgr.getOntologies(), ontology);

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

		private void handleOntologyLoadedAndReLoaded(OWLModelManager owlModelManager, OWLOntology activeOntology) {
			loadingData = true; // flag on
			try {
				IRI documentIRI = owlModelManager.getOWLOntologyManager().getOntologyDocumentIRI(activeOntology);

				if (!UIUtil.isLocalFile(documentIRI.toURI())) {
					return;
				}

				String owlDocumentIriString = documentIRI.toString();
				int i = owlDocumentIriString.lastIndexOf(".");
				String owlName = owlDocumentIriString.substring(0,i);

				String obdaDocumentIri = owlName + OBDA_EXT;
				String queryDocumentIri = owlName + QUERY_EXT;
				String propertyFilePath = owlName + PROPERTY_EXT;
				String implicitDBConstraintFilePath = owlName + DBPREFS_EXT;

				File obdaFile = new File(URI.create(obdaDocumentIri));
				File queryFile = new File(URI.create(queryDocumentIri));
				File propertyFile = new File(URI.create(propertyFilePath));
				File implicitDBConstraintFile = new File(URI.create(implicitDBConstraintFilePath));

				if(implicitDBConstraintFile.exists())
					configurationManager.setImplicitDBConstraintFile(implicitDBConstraintFile);

				/*cd
				 * Loads the properties (and the data source)
				 */
				if (propertyFile.exists()) {
					configurationManager.loadPropertyFile(propertyFile);
				}

				if (obdaFile.exists()) {
					try {
						//convert old syntax OBDA file
						Reader mappingReader = new FileReader(obdaFile);
						OldSyntaxMappingConverter converter =  new OldSyntaxMappingConverter(new FileReader(obdaFile), obdaFile.getName());
						java.util.Optional<Properties> optionalDataSourceProperties = converter.getOBDADataSourceProperties();

						if (optionalDataSourceProperties.isPresent()) {
							configurationManager.loadProperties(optionalDataSourceProperties.get());
							mappingReader = converter.getOutputReader();
						}
						// Load the OBDA model
						obdaModel.parseMapping(mappingReader, configurationManager.snapshotProperties());

					}
					catch (Exception ex) {
						throw new Exception("Exception occurred while loading OBDA document: " + obdaFile + "\n\n" + ex.getMessage());
					}

					try {
						// Load the saved queries
						QueryIOManager queryIO = new QueryIOManager(queryController);
						queryIO.load(queryFile);
					}
					catch (Exception ex) {
						queryController.reset();
						throw new Exception("Exception occurred while loading Query document: " + queryFile + "\n\n" + ex.getMessage());
					}
				}
				else {
					log.warn("No OBDA model was loaded because no .obda file exists in the same location as the .owl file");
				}
				// adding type information to the mapping predicates
				for (SQLPPTriplesMap mapping : obdaModel.generatePPMapping().getTripleMaps()) {
					ImmutableList<TargetAtom> tq = mapping.getTargetAtoms();
					if (!TargetQueryValidator.validate(tq, obdaModel.getCurrentVocabulary()).isEmpty()) {
						throw new Exception("Found an invalid target query: " + tq.toString());
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

		private void handleOntologySaved(OWLModelManager owlModelManager, OWLOntology activeOntology) {
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

				// Save the mapping
				File obdaFile = new File(URI.create(obdaDocumentIri));
				if(obdaModel.hasTripleMaps()) {
					SQLPPMapping ppMapping = obdaModel.generatePPMapping();
					OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer(ppMapping);
					writer.save(obdaFile);
					log.info("mapping file saved to {}", obdaFile);
				} else {
					Files.deleteIfExists(obdaFile.toPath());
				}

				if (!queryController.getElements().isEmpty()) {
					// Save the queries
					File queryFile = new File(URI.create(queryDocumentIri));
					QueryIOManager queryIO = new QueryIOManager(queryController);
					queryIO.save(queryFile);
					log.info("query file saved to {}", queryFile);
				}

				String propertyFilePath = owlName + PROPERTY_EXT;
				File propertyFile = new File(URI.create(propertyFilePath));
				Properties properties = configurationManager.snapshotUserProperties();
				// Generate a property file iff there is at least one property that is not "jdbc.name"
				if (properties.entrySet().stream()
						.anyMatch(
								e -> !e.getKey().equals(OntopSQLCoreSettings.JDBC_NAME) &&
										!e.getValue().equals(""))
				){
					FileOutputStream outputStream = new FileOutputStream(propertyFile);
					properties.store(outputStream, null);
					outputStream.flush();
					outputStream.close();
					log.info("Property file saved to {}", propertyFilePath);
				}else {
					Files.deleteIfExists(propertyFile.toPath());
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

	private void loadVocabularyAndDefaultPrefix(OBDAModel obdaModel, Set<OWLOntology> ontologies,
													   OWLOntology activeOntology) {
		for (OWLOntology ontology : ontologies) {
			// Setup the entity declarations
			for (OWLClass c : ontology.getClassesInSignature())
				obdaModel.getCurrentVocabulary().classes().declare(getIRI(c));

			for (OWLObjectProperty r : ontology.getObjectPropertiesInSignature())
				obdaModel.getCurrentVocabulary().objectProperties().declare(getIRI(r));

			for (OWLDataProperty p : ontology.getDataPropertiesInSignature())
				obdaModel.getCurrentVocabulary().dataProperties().declare(getIRI(p));

			for (OWLAnnotationProperty p : ontology.getAnnotationPropertiesInSignature())
				obdaModel.getCurrentVocabulary().annotationProperties().declare(getIRI(p));
		}
		updateDefaultPrefixNamespace(obdaModel, activeOntology);
	}

	/**
	 * Modifies the OBDA model
	 */
	private void updateDefaultPrefixNamespace(OBDAModel obdaModel, OWLOntology ontology) {
		java.util.Optional<String> ns = MutablePrefixManager.getDeclaredDefaultPrefixNamespace(ontology);
		if(ns.isPresent()) {
			obdaModel.setExplicitDefaultPrefixNamespace(ns.get());
		} else{
			MutablePrefixManager.generateDefaultPrefixNamespaceFromID(ontology.getOntologyID()).ifPresent(
					id -> obdaModel.addPrefix(
							MutablePrefixManager.DEFAULT_PREFIX,
							id
					));
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
	 * has suffered changes. To accomplish this, this method will "fake" an
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
	public void dispose() {
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

}
