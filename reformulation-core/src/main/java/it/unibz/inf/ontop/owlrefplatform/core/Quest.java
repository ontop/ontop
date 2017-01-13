package it.unibz.inf.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RepositoryChangedListener;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.DummyReformulator;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.reformulation.TreeWitnessRewriter;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.owlrefplatform.core.translator.MappingVocabularyFixer;

import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import it.unibz.inf.ontop.utils.IMapping2DatalogConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.sql.*;
import java.util.*;

public class Quest implements Serializable, IQuest {

	private static final long serialVersionUID = -6074403119825754295L;

	// Whether to print primary and foreign keys to stdout.
	private boolean printKeys;

	/***
	 * Internal components
	 */

	/* The active ABox repository (is null if there is no Semantic Index, i.e., in Virtual Mode) */
	private RDBMSSIRepositoryManager dataRepository = null;

    /* The merge and translation of all loaded ontologies */
	private final Ontology inputOntology;

	/* The input OBDA model */
	private OBDAModel inputOBDAModel = null;

	private QuestQueryProcessor engine;

	/**
	 * This represents user-supplied constraints, i.e. primary
	 * and foreign keys not present in the database metadata
	 */
	private ImplicitDBConstraintsReader userConstraints = null;

	/*
	 * Whether to apply the user-supplied database constraints given above
	 * userConstraints must be initialized and non-null whenever this is true
	 */
	// private boolean applyUserConstraints;

	/** Davide> Exclude specific predicates from T-Mapping approach **/
	private final TMappingExclusionConfig excludeFromTMappings ;
	
	/** Davide> Whether to exclude the user-supplied predicates from the
	 *          TMapping procedure (that is, the mapping assertions for 
	 *          those predicates should not be extended according to the 
	 *          TBox hierarchies
	 */
	//private boolean applyExcludeFromTMappings;
	
	/***
	 * General flags and fields
	 */

	private final Logger log = LoggerFactory.getLogger(Quest.class);

	/***
	 * Configuration
	 */

	public boolean reformulate = false;

	private Optional<String> reformulationTechnique = Optional.of(QuestConstants.UCQBASED);

	private boolean bOptimizeEquivalences = true;

	private boolean bObtainFromOntology = true;

	private boolean bObtainFromMappings = true;


	//private boolean obtainFullMetadata = false;
	//private boolean sqlGenerateReplace = true;

	private boolean distinctResultSet = false;

	private boolean isVirtualMode = true;

	private Optional<String> aboxSchemaType = Optional.empty();

	private QuestCoreSettings preferences;

	private boolean inmemory;

	private String aboxJdbcURL;

	private String aboxJdbcUser;

	private String aboxJdbcPassword;

	private String aboxJdbcDriver;
				


	private DBMetadata metadata;

	//private DBConnector dbConnector;

	/**
	 * TODO: explain
	 */
	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	private final QuestComponentFactory questComponentFactory;
	private final OBDAFactoryWithException obdaFactory;
	private final MappingVocabularyFixer mappingVocabularyFixer;
	private final IMapping2DatalogConverter mapping2DatalogConverter;

	private DBConnector dbConnector;
	private final QueryCache queryCache;

	private final OntopModelFactory modelFactory;
	private final ExecutorRegistry executorRegistry;

	/***
	 * Will prepare an instance of quest in classic or virtual ABox mode. If the
	 * mappings are not null, then org.obda.owlreformulationplatform.aboxmode
	 * must be set to "virtual", if they are null it must be set to "classic".
	 * 
	 * <p>
	 * You must still call setupRepository() after creating the instance.
	 *  @param tbox
	 *            . The TBox must not be null, even if its empty. At least, the
	 *            TBox must define all the vocabulary of the system.
	 * @param obdaModel
	 *            . The mappings of the system. The vocabulary of the mappings
	 *            must be subset or equal to the vocabulary of the ontology.
	 *            Can be null.
	 * @param inputMetadata TODO: describe
	 * @param config
*            . The configuration parameters for quest. See
*            QuestDefaults.properties for a description (in
*            src/main/resources). Should not be null.
*@param nativeQLFactory
*@param queryCache
	 */
	@Inject
	private Quest(@Assisted Ontology tbox, @Assisted Optional<OBDAModel> obdaModel,
				  @Assisted Optional<DBMetadata> inputMetadata,
				  @Assisted ExecutorRegistry executorRegistry,
				  QuestCoreSettings config, NativeQueryLanguageComponentFactory nativeQLFactory,
				  OBDAFactoryWithException obdaFactory, QuestComponentFactory questComponentFactory,
				  MappingVocabularyFixer mappingVocabularyFixer, TMappingExclusionConfig excludeFromTMappings,
				  IMapping2DatalogConverter mapping2DatalogConverter, QueryCache queryCache,
				  OntopModelFactory modelFactory) throws DuplicateMappingException {
		if (tbox == null)
			throw new InvalidParameterException("TBox cannot be null");

		this.nativeQLFactory = nativeQLFactory;
		this.obdaFactory = obdaFactory;
		this.questComponentFactory = questComponentFactory;
		this.mappingVocabularyFixer = mappingVocabularyFixer;
		this.mapping2DatalogConverter = mapping2DatalogConverter;
		this.queryCache = queryCache;
		this.modelFactory = modelFactory;
		this.executorRegistry = executorRegistry;

		inputOntology = tbox;

		// Not null (default value defined by the Guice module)
		this.excludeFromTMappings = excludeFromTMappings;

		setPreferences(config);

		if ((!obdaModel.isPresent()) && isVirtualMode) {
			throw new IllegalArgumentException(
					"When working without mappings, you must set the ABox mode to \""
							+ QuestConstants.CLASSIC
							+ "\". If you want to work with no mappings in virtual ABox mode you must at least provide an empty but not null OBDAModel");
		}
		if (obdaModel.isPresent() && (!isVirtualMode)) {
			throw new IllegalArgumentException(
					"When working with mappings, you must set the ABox mode to \""
							+ QuestConstants.VIRTUAL
							+ "\". If you want to work in \"classic abox\" mode, that is, as a triple store, you may not provide mappings (quest will take care of setting up the mappings and the database), set them to null.");
		}

		loadOBDAModel(obdaModel.orElse(null), inputOntology.getVocabulary());
		// TODO: use the Optional instead
		this.metadata = inputMetadata.orElse(null);
	}

	// TEST ONLY
	@Override
	public List<CQIE> getUnfolderRules() {
		return engine.unfolder.ufp;
	}

	private void loadOBDAModel(OBDAModel model, ImmutableOntologyVocabulary vocabulary) {

		if (model == null) {
			//model = OBDADataFactoryImpl.getInstance().getOBDAModel();
			// TODO: refactor this pretty bad practice.
			//TODO: add the prefix.
			PrefixManager defaultPrefixManager = nativeQLFactory.create(new HashMap<String, String>());

			model = obdaFactory.createOBDAModel(ImmutableList.of(), defaultPrefixManager, vocabulary);
		}
		inputOBDAModel = model;
	}

	@Override
	public OBDAModel getOBDAModel() {
		return inputOBDAModel;
	}

	@Override
	public void dispose() {
		dbConnector.dispose();
	}

	@Override
	public QuestCoreSettings getPreferences() {
		return preferences;
	}


	private void setPreferences(QuestCoreSettings preferences) {
		this.preferences = preferences;

		reformulate = preferences.isRewritingEnabled();
		reformulationTechnique = preferences.getProperty(QuestCoreSettings.REFORMULATION_TECHNIQUE);
		bOptimizeEquivalences = preferences.isEquivalenceOptimizationEnabled();

		/**
		 * Classic A-box specific configuration
		 */
		bObtainFromOntology = preferences.getRequiredBoolean(QuestCoreSettings.OBTAIN_FROM_ONTOLOGY);
		bObtainFromMappings = preferences.getRequiredBoolean(QuestCoreSettings.OBTAIN_FROM_MAPPINGS);
		isVirtualMode = preferences.isInVirtualMode();
		aboxSchemaType = preferences.getProperty(QuestCoreSettings.DBTYPE);
		inmemory = preferences.getRequiredProperty(QuestCoreSettings.STORAGE_LOCATION)
				.equals(QuestConstants.INMEMORY);

		printKeys = preferences.isKeyPrintingEnabled();

		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(QuestCoreSettings.JDBC_URL)
					.orElseThrow(() -> new IllegalStateException("JDBC_URL must have a default value"));

			aboxJdbcUser = preferences.getProperty(OBDASettings.DB_USER)
					.orElseThrow(() -> new IllegalStateException("DB_USER must have a default value"));
			aboxJdbcPassword = preferences.getProperty(OBDASettings.DB_PASSWORD)
					.orElseThrow(() -> new IllegalStateException("DB_PASSWORD must have a default value"));
			aboxJdbcDriver = preferences.getProperty(OBDASettings.JDBC_DRIVER)
					.orElseThrow(() -> new IllegalStateException("JDBC_DRIVER must have a default value"));
		}

		log.debug("Quest configuration:");

		log.debug("Extensional query rewriting enabled: {}", reformulate);
		//log.debug("Reformulation technique: {}", reformulationTechnique);
		if(reformulate){
			log.debug("Extensional query rewriting technique: {}", reformulationTechnique);
		}
		log.debug("Optimize TBox using class/property equivalences: {}", bOptimizeEquivalences);
		log.debug("ABox mode: {}", isVirtualMode);
		if (!isVirtualMode) {
			log.debug("Use in-memory database: {}", inmemory);
			log.debug("Schema configuration: {}", aboxSchemaType);
			log.debug("Get ABox assertions from OBDA models: {}", bObtainFromMappings);
			log.debug("Get ABox assertions from ontology: {}", bObtainFromOntology);
		}

	}

	/***
	 * Method that starts all components of a Quest instance. Call this after
	 * creating the instance.
	 * 
	 * @throws Exception
	 */
	public void setupRepository() throws Exception {

		log.debug("Initializing Quest...");

		/*
		 * Input checking (we need to extend this)
		 */

		if (isVirtualMode && inputOBDAModel == null) {
			throw new Exception("ERROR: Working in virtual mode but no OBDA model has been defined.");
		}

		/*
		 * Fixing the typing of predicates, in case they are not properly given.
		 */
		if (inputOBDAModel == null) {
			throw new IllegalStateException("The input OBDA model should not be null");
		}
		if (!inputOntology.getVocabulary().isEmpty()) {
			inputOBDAModel = mappingVocabularyFixer.fixOBDAModel(inputOBDAModel,
					inputOntology.getVocabulary(), nativeQLFactory);
		}

		/*
		 * Simplifying the vocabulary of the TBox
		 */

		final TBoxReasoner reformulationReasoner = TBoxReasonerImpl.create(inputOntology, bOptimizeEquivalences);

		try {

			Collection<OBDAMappingAxiom> mappings = null;

			/*
			 * Preparing the data source
			 */

			if (!isVirtualMode) {
				if ((!aboxSchemaType.isPresent()) || (!aboxSchemaType.get().equals(QuestConstants.SEMANTIC_INDEX))) {
					throw new Exception(aboxSchemaType
							+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}

				throw new RuntimeException("TODO: fix quickly the configuration of the SI mode");

//				if (inmemory) {
//					String url = "jdbc:h2:mem:questrepository:" + System.currentTimeMillis()
//							+ ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
//
//					throw new RuntimeException("TODO: load the configuration somewhere else");
////					obdaSource = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "org.h2.Driver");
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "");
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "sa");
////					obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
////					obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//				}
//				else {
//					if (aboxJdbcURL.trim().equals(""))
//						throw new OBDAException("Found empty JDBC_URL parametery. Quest in CLASSIC/JDBC mode requires a JDBC_URL value.");
//
//					if (aboxJdbcDriver.trim().equals(""))
//						throw new OBDAException(
//								"Found empty JDBC_DRIVER parametery. Quest in CLASSIC/JDBC mode requires a JDBC_DRIVER value.");
//
//					throw new RuntimeException("TODO: load the configuration somewhere else");
////					obdaSource = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, aboxJdbcDriver.trim());
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, aboxJdbcPassword);
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, aboxJdbcURL.trim());
////					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, aboxJdbcUser.trim());
////					obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
////					obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//				}
//
//				// TODO one of these is redundant??? check
//				dbConnector = questComponentFactory.create(this);
//				dbConnector.connect();
//
//				// Classic mode only works with a JDBCConnector
//				if (!(dbConnector instanceof JDBCConnector)) {
//					throw new OBDAException("Classic mode requires using a JDBC connector");
//				}
//				JDBCConnector jdbcConnector = (JDBCConnector) dbConnector;
//				Connection localConnection = jdbcConnector.getSQLConnection();
//
//				dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner, inputOntology.getVocabulary(),
//						nativeQLFactory);
//
//				if (inmemory) {
//
//					log.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
//					// we work in memory (with H2), the database is clean and
//					// Quest will insert new Abox assertions into the database.
//					dataRepository.generateMetadata();
//
//					// Creating the ABox repository
//					dataRepository.createDBSchemaAndInsertMetadata(localConnection);
//				}
//				else {
//					// the repository has already been created in the database,
//					// restore the repository and do NOT insert any data in the repo,
//					// it should have been inserted already.
//					dataRepository.loadMetadata(localConnection);
//
//					// TODO add code to verify that the existing semantic index
//					// repository can be used with the current ontology, e.g.,
//					// checking the vocabulary of URIs, ranges wrt the ontology entailments
//				}
//
//				mappings = dataRepository.getMappings();
			}
			else if (isVirtualMode) {
				// log.debug("Working in virtual mode");

				dbConnector = questComponentFactory.create(this);
				dbConnector.connect();

				log.debug("Testing DB connection...");
				mappings = inputOBDAModel.getMappings();
			}


			//if the metadata was not already set
			if (metadata == null) {
				metadata = dbConnector.extractDBMetadata(inputOBDAModel);
			}
			else {
				metadata = dbConnector.extractDBMetadata(inputOBDAModel, metadata);
			}

			dbConnector.completePredefinedMetadata(metadata);


			// This is true if the QuestDefaults.properties contains PRINT_KEYS=true
			// Very useful for debugging of User Constraints (also for the end user)
			if (printKeys)
				System.out.println(metadata.printKeys());
			else
				log.debug("DB Metadata: \n{}", metadata);

    		VocabularyValidator vocabularyValidator = new VocabularyValidator(reformulationReasoner,
					inputOntology.getVocabulary(), nativeQLFactory);

            final QuestUnfolder unfolder = new QuestUnfolder(nativeQLFactory, mapping2DatalogConverter, preferences);

			/*
			 * T-Mappings and Fact mappings
			 */
			if (isVirtualMode)
				unfolder.setupInVirtualMode(mappings, metadata, dbConnector, vocabularyValidator, reformulationReasoner,
						inputOntology, excludeFromTMappings);
			else
				unfolder.setupInSemanticIndexMode(mappings, reformulationReasoner, metadata);


			/* The active ABox dependencies */
			LinearInclusionDependencies sigma = LinearInclusionDependencies.getABoxDependencies(reformulationReasoner, true);
			

			// Setting up the TBox we will use for the reformulation
			//TBoxReasoner reasoner = reformulationReasoner;
			//if (bOptimizeTBoxSigma) {
			//	SigmaTBoxOptimizer reducer = new SigmaTBoxOptimizer(reformulationReasoner);
			//	reasoner = TBoxReasonerImpl.create(reducer.getReducedOntology());
			//}

			QueryRewriter rewriter;
			// Setting up the reformulation engine
			if (reformulate == false || (!reformulationTechnique.isPresent()))
				rewriter = new DummyReformulator();
			else if (QuestConstants.TW.equals(reformulationTechnique.get()))
				rewriter = new TreeWitnessRewriter();
			else
				throw new IllegalArgumentException("Invalid value for argument: " + QuestCoreSettings.REFORMULATION_TECHNIQUE);

			rewriter.setTBox(reformulationReasoner, inputOntology.getVocabulary(), sigma);


			NativeQueryGenerator dataSourceQueryGenerator = isVirtualMode
					? questComponentFactory.create(metadata)
					: questComponentFactory.create(metadata, dataRepository.getUriMap());

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */
			engine = new QuestQueryProcessor(rewriter, sigma, unfolder, vocabularyValidator, getUriMap(),
					dataSourceQueryGenerator, queryCache, distinctResultSet, modelFactory, executorRegistry);

			if (dataRepository != null)
				dataRepository.addRepositoryChangedListener(new RepositoryChangedListener() {
					@Override
					public void repositoryChanged() {
						engine.clearNativeQueryCache();
						try {
							//
							engine = engine.changeMappings(dataRepository.getMappings(), reformulationReasoner);
							log.debug("Mappings and unfolder have been updated after inserts to the semantic index DB");
						}
						catch (Exception e) {
							log.error("Error updating Semantic Index mappings", e);
						}
					}
				});

			log.debug("... Quest has been initialized.");
		}
		catch (Exception e) {
			OBDAException ex = new OBDAException(e);
			if (e instanceof SQLException) {
				SQLException sqle = (SQLException) e;
				SQLException e1 = sqle.getNextException();
				while (e1 != null) {
					log.error("NEXT EXCEPTION");
					log.error(e1.getMessage());
					e1 = e1.getNextException();
				}
			}
			throw ex;
		}
		finally {
			if (!((!isVirtualMode) && (inmemory))) {
				/*
				 * If we are not in classic + inmemory mode we can disconnect
				 * the house-keeping connection, it has already been used.
				 */
				if (dbConnector != null) {
					dbConnector.disconnect();
				}
			}
		}
	}

	public ImmutableOntologyVocabulary getVocabulary() {
		return inputOntology.getVocabulary();
	}

	public void close() {
		dbConnector.close();
	}


	@Override
	public IQuestConnection getNonPoolConnection() throws OBDAException {
		return dbConnector.getNonPoolConnection();
	}

	@Override
	public IQuestConnection getConnection() throws OBDAException {
		return dbConnector.getConnection();
	}

	@Override
	public DBMetadata getMetaData() {
		return metadata;
	}

	public SemanticIndexURIMap getUriMap() {
		if (dataRepository != null)
			return dataRepository.getUriMap();
		else
			return null;
	}

	@Override
	public QuestQueryProcessor getEngine() {
		return engine;
	}

	public Optional<RDBMSSIRepositoryManager> getOptionalSemanticIndexRepository() {
		if (dataRepository == null) {
			return Optional.empty();
		}
		else {
			return Optional.of(dataRepository);
		}
	}
}
