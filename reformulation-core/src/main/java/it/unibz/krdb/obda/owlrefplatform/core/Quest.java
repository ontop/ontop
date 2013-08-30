/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RepositoryChangedListener;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.AxiomToRuleTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DBMetadataUtil;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingDataTypeRepair;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingVocabularyTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLServerSQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DummyReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.EquivalenceTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.translator.MappingVocabularyRepair;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DatalogUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.utils.MappingAnalyzer;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.Serializable;
import java.net.URI;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openrdf.query.parser.ParsedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.hp.hpl.jena.query.Query;

public class Quest implements Serializable, RepositoryChangedListener {

	private static final long serialVersionUID = -6074403119825754295L;

	private PoolProperties poolProperties = null;
	private DataSource tomcatPool = null;
	private boolean isSemanticIdx = false;
	private Map<String, Integer> uriRefIds = null;
	private Map<Integer, String> uriMap = null;;
	// Tomcat pool default properties
	// These can be changed in the properties file
	protected int maxPoolSize = 10;
	protected int startPoolSize = 2;
	protected boolean removeAbandoned = false;
	protected int abandonedTimeout = 60; // 60 seconds
	protected boolean keepAlive = false;

	/***
	 * Internal components
	 */

	/* The active ABox repository, might be null */
	public RDBMSSIRepositoryManager dataRepository = null;

	// /* The query answering engine */
	// private TechniqueWrapper techwrapper = null;

	protected QueryVocabularyValidator vocabularyValidator;

	/* The active connection used to get metadata from the DBMS */
	private transient Connection localConnection = null;

	/* The active query rewriter */
	protected QueryRewriter rewriter = null;

	/* The active unfolding engine */
	protected UnfoldingMechanism unfolder = null;

	/* The active SQL generator */
	protected SQLQueryGenerator datasourceQueryGenerator = null;

	/* The active query evaluation engine */
	protected EvaluationEngine evaluationEngine = null;

	/* The active ABox dependencies */
	protected Ontology sigma = null;

	/* TBox axioms translated into rules */
	protected Map<Predicate, List<CQIE>> sigmaRulesIndex = null;

	/* The TBox used for query reformulation */
	protected Ontology reformulationOntology = null;

	/* The merge and translation of all loaded ontologies */
	protected Ontology inputTBox = null;

	/* The OBDA model used for query unfolding */
	protected OBDAModel unfoldingOBDAModel = null;

	/* As unfolding OBDAModel, but experimental */
	protected DatalogProgram unfoldingProgram = null;

	/* The input OBDA model */
	protected OBDAModel inputOBDAModel = null;

	/*
	 * The equivalence map for the classes/properties that have been simplified
	 */
	protected Map<Predicate, Description> equivalenceMaps = null;

	/*
	 * These are pattern matchers that will help transforming the URI's in
	 * queries into Functions, used by the SPARQL translator.
	 */
	private UriTemplateMatcher uriTemplateMatcher = new UriTemplateMatcher();

	final HashSet<String> templateStrings = new HashSet<String>();

	/***
	 * General flags and fields
	 */

	private Logger log = LoggerFactory.getLogger(Quest.class);

	/***
	 * Configuration
	 */

	public boolean reformulate = false;

	private String reformulationTechnique = QuestConstants.UCQBASED;

	private boolean bOptimizeEquivalences = true;

	private boolean bOptimizeTBoxSigma = true;

	private boolean bObtainFromOntology = true;

	private boolean bObtainFromMappings = true;

	private String aboxMode = QuestConstants.CLASSIC;

	private String aboxSchemaType = QuestConstants.SEMANTIC_INDEX;

	private OBDADataSource obdaSource;

	private Properties preferences;

	private boolean inmemory;

	private String aboxJdbcURL;

	private String aboxJdbcUser;

	private String aboxJdbcPassword;

	private String aboxJdbcDriver;

	/*
	 * The following are caches to queries that Quest has seen in the past. They
	 * are used by the statementts
	 */

	Map<String, String> querycache = new ConcurrentHashMap<String, String>();

	Map<String, List<String>> signaturecache = new ConcurrentHashMap<String, List<String>>();

	//Map<String, Query> jenaQueryCache = new ConcurrentHashMap<String, Query>();
	
	Map<String, ParsedQuery> sesameQueryCache = new ConcurrentHashMap<String, ParsedQuery>();

	Map<String, Boolean> isbooleancache = new ConcurrentHashMap<String, Boolean>();

	Map<String, Boolean> isconstructcache = new ConcurrentHashMap<String, Boolean>();

	Map<String, Boolean> isdescribecache = new ConcurrentHashMap<String, Boolean>();

	private DBMetadata metadata;

	private Map<Predicate, List<Integer>> pkeys;

	/***
	 * Will prepare an instance of Quest in "classic ABox mode", that is, to
	 * work as a triple store. The property
	 * "org.obda.owlreformulationplatform.aboxmode" must be set to "classic".
	 * 
	 * <p>
	 * You must still call setupRepository() after creating the instance.
	 * 
	 * 
	 * @param tbox
	 * @param config
	 */
	public Quest(Ontology tbox, Properties config) {

		this(tbox, null, config);
	}

	/***
	 * Will prepare an instance of quest in classic or virtual ABox mode. If the
	 * mappings are not null, then org.obda.owlreformulationplatform.aboxmode
	 * must be set to "virtual", if they are null it must be set to "classic".
	 * 
	 * <p>
	 * You must still call setupRepository() after creating the instance.
	 * 
	 * @param tbox
	 *            . The TBox must not be null, even if its empty. At least, the
	 *            TBox must define all the vocabulary of the system.
	 * @param mappings
	 *            . The mappings of the system. The vocabuarly of the mappings
	 *            must be subset or equal to the vocabulary of the ontology.
	 * @param config
	 *            . The configuration parameters for quest. See
	 *            QuestDefaults.properties for a description (in
	 *            src/main/resources)
	 */
	public Quest(Ontology tbox, OBDAModel mappings, Properties config) {
		if (tbox == null)
			throw new InvalidParameterException("TBox cannot be null");
		loadTBox(tbox);

		setPreferences(config);

		if (mappings == null && !aboxMode.equals(QuestConstants.CLASSIC)) {
			throw new IllegalArgumentException(
					"When working without mappings, you must set the ABox mode to \""
							+ QuestConstants.CLASSIC
							+ "\". If you want to work with no mappings in virtual ABox mode you must at least provide an empty but not null OBDAModel");
		}
		if (mappings != null && !aboxMode.equals(QuestConstants.VIRTUAL)) {
			throw new IllegalArgumentException(
					"When working with mappings, you must set the ABox mode to \""
							+ QuestConstants.VIRTUAL
							+ "\". If you want to work in \"classic abox\" mode, that is, as a triple store, you may not provide mappings (quest will take care of setting up the mappings and the database), set them to null.");
		}

		loadOBDAModel(mappings);
	}
	
	public Quest(Ontology tbox, OBDAModel mappings, DBMetadata metadata, Properties config) {
		this(tbox, mappings, config);
		this.metadata = metadata;
	}

	protected Map<String, String> getSQLCache() {
		return querycache;
	}

	protected Map<String, List<String>> getSignatureCache() {
		return signaturecache;
	}

//	protected Map<String, Query> getJenaQueryCache() {
//		return jenaQueryCache;
//	}

	protected Map<String, ParsedQuery> getSesameQueryCache() {
		return sesameQueryCache;
	}
	
	protected Map<String, Boolean> getIsBooleanCache() {
		return isbooleancache;
	}

	protected Map<String, Boolean> getIsConstructCache() {
		return isconstructcache;
	}

	public Map<String, Boolean> getIsDescribeCache() {
		return isdescribecache;
	}

	private void loadOBDAModel(OBDAModel model) {

		if (model == null) {
			model = OBDADataFactoryImpl.getInstance().getOBDAModel();
		}
		inputOBDAModel = (OBDAModel) model.clone();
	}

	public OBDAModel getOBDAModel() {
		return inputOBDAModel;
	}

	public Ontology getOntology() {
		return inputTBox;
	}

	/***
	 * Gets the internal TBox, the one used for reasoning and query answering.
	 * 
	 * @return
	 */
	public Ontology getTBox() {
		return reformulationOntology;
	}

	/***
	 * Gets the internal Sigma TBox, the one used for reasoning and query
	 * answering.
	 * 
	 * @return
	 */
	public Ontology getSigmaTBox() {
		return sigma;
	}

	// TODO This method has to be fixed... shouldnt be visible
	public Map<Predicate, Description> getEquivalenceMap() {
		return equivalenceMaps;
	}

	public void dispose() {
		try {
			if (evaluationEngine != null)
				this.evaluationEngine.dispose();
		} catch (Exception e) {
			log.debug("Error during disconnect: " + e.getMessage());
		}

		try {
			if (localConnection != null && !localConnection.isClosed())
				disconnect();
		} catch (Exception e) {
			log.debug("Error during disconnect: " + e.getMessage());
		}
	}

	/***
	 * Sets up the rewriting TBox
	 */
	private void loadTBox(Ontology tbox) {
		inputTBox = tbox;
	}

	public Properties getPreferences() {
		return preferences;
	}

	private void setPreferences(Properties preferences) {
		this.preferences = preferences;

		keepAlive = Boolean.valueOf((String) preferences.get(QuestPreferences.KEEP_ALIVE));
		removeAbandoned = Boolean.valueOf((String) preferences.get(QuestPreferences.REMOVE_ABANDONED));
		abandonedTimeout = Integer.valueOf((String) preferences.get(QuestPreferences.ABANDONED_TIMEOUT));
		startPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.INIT_POOL_SIZE));
		maxPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.MAX_POOL_SIZE));

		reformulate = Boolean.valueOf((String) preferences.get(QuestPreferences.REWRITE));
		reformulationTechnique = (String) preferences.get(QuestPreferences.REFORMULATION_TECHNIQUE);
		bOptimizeEquivalences = Boolean.valueOf((String) preferences.get(QuestPreferences.OPTIMIZE_EQUIVALENCES));
		bOptimizeTBoxSigma = Boolean.valueOf((String) preferences.get(QuestPreferences.OPTIMIZE_TBOX_SIGMA));
		bObtainFromOntology = Boolean.valueOf((String) preferences.get(QuestPreferences.OBTAIN_FROM_ONTOLOGY));
		bObtainFromMappings = Boolean.valueOf((String) preferences.get(QuestPreferences.OBTAIN_FROM_MAPPINGS));
		aboxMode = (String) preferences.get(QuestPreferences.ABOX_MODE);
		aboxSchemaType = (String) preferences.get(QuestPreferences.DBTYPE);
		inmemory = preferences.getProperty(QuestPreferences.STORAGE_LOCATION).equals(QuestConstants.INMEMORY);

		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(QuestPreferences.JDBC_URL);
			aboxJdbcUser = preferences.getProperty(QuestPreferences.DBUSER);
			aboxJdbcPassword = preferences.getProperty(QuestPreferences.DBPASSWORD);
			aboxJdbcDriver = preferences.getProperty(QuestPreferences.JDBC_DRIVER);
		}

		log.debug("Quest configuration:");
		log.debug("Reformulation technique: {}", reformulationTechnique);
		log.debug("Optimize equivalences: {}", bOptimizeEquivalences);
		log.debug("Optimize TBox: {}", bOptimizeTBoxSigma);
		log.debug("ABox mode: {}", aboxMode);
		if (!aboxMode.equals("virtual")) {
			log.debug("Use in-memory database: {}", inmemory);
			log.debug("Schema configuration: {}", aboxSchemaType);
			log.debug("Get ABox assertions from OBDA models: {}", bObtainFromMappings);
			log.debug("Get ABox assertions from ontology: {}", bObtainFromOntology);
		}

	}

	/***
	 * Starts the local connection that Quest maintains to the DBMS. This
	 * connection belongs only to Quest and is used to get information from the
	 * DBMS. At the moment this connection is mainly used during initialization,
	 * to get metadata about the DBMS or to create repositories in classic mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	private boolean connect() throws SQLException {
		if (localConnection != null && !localConnection.isClosed()) {
			return true;
		}
		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}
		localConnection = DriverManager.getConnection(url, username, password);

		if (localConnection != null) {
			return true;
		}
		return false;
	}

	public void disconnect() throws SQLException {
		try {
			localConnection.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	/***
	 * Method that starts all components of a Quest instance. Call this after
	 * creating the instance.
	 * 
	 * @throws Exception
	 */
	public void setupRepository() throws Exception {

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		log.debug("Initializing Quest...");

		/*
		 * Input checking (we need to extend this)
		 */

		if (aboxMode.equals(QuestConstants.VIRTUAL) && inputOBDAModel == null) {
			throw new Exception("ERROR: Working in virtual mode but no OBDA model has been defined.");
		}

		/*
		 * Fixing the typing of predicates, in case they are not properly given.
		 */

		if (inputOBDAModel != null && !inputTBox.getVocabulary().isEmpty()) {
			MappingVocabularyRepair repairmodel = new MappingVocabularyRepair();
			repairmodel.fixOBDAModel(inputOBDAModel, inputTBox.getVocabulary());
		}

		unfoldingOBDAModel = fac.getOBDAModel();

		sigma = OntologyFactoryImpl.getInstance().createOntology();

		/*
		 * Simplifying the vocabulary of the TBox
		 */

		if (bOptimizeEquivalences) {
			EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(inputTBox);
			equiOptimizer.optimize();

			/* This generates a new TBox with a simpler vocabulary */
			reformulationOntology = equiOptimizer.getOptimalTBox();

			/*
			 * This is used to simplify the vocabulary of ABox assertions and
			 * mappings
			 */
			equivalenceMaps = equiOptimizer.getEquivalenceMap();
		} else {
			reformulationOntology = inputTBox;
			equivalenceMaps = new HashMap<Predicate, Description>();
		}

		try {

			/*
			 * Preparing the data source
			 */

			if (aboxMode.equals(QuestConstants.CLASSIC)) {
				isSemanticIdx = true;
				if (inmemory) {
					String driver = "org.h2.Driver";
					String url = "jdbc:h2:mem:questrepository:" + System.currentTimeMillis()
							+ ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
					String username = "sa";
					String password = "";

					obdaSource = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
					obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
					obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
				} else {
					obdaSource = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));

					if (aboxJdbcURL.trim().equals(""))
						throw new OBDAException("Found empty JDBC_URL parametery. Quest in CLASSIC/JDBC mode requires a JDBC_URL value.");

					if (aboxJdbcDriver.trim().equals(""))
						throw new OBDAException(
								"Found empty JDBC_DRIVER parametery. Quest in CLASSIC/JDBC mode requires a JDBC_DRIVER value.");

					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, aboxJdbcDriver.trim());
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, aboxJdbcPassword);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, aboxJdbcURL.trim());
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, aboxJdbcUser.trim());
					obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
					obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
				}

				if (!aboxSchemaType.equals(QuestConstants.SEMANTIC_INDEX)) {
					throw new Exception(aboxSchemaType
							+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}

				// TODO one of these is redundant??? check
				connect();
				// setup connection pool
				setupConnectionPool();

				dataRepository = new RDBMSSIRepositoryManager(reformulationOntology.getVocabulary());
				dataRepository.addRepositoryChangedListener(this);

				dataRepository.setTBox(reformulationOntology);
				for (Axiom axiom : dataRepository.getABoxDependencies().getAssertions()) {
					sigma.addEntities(axiom.getReferencedEntities());
					sigma.addAssertion(axiom);
				}

				if (inmemory) {

					/*
					 * in this case we we work in memory (with H2), the database
					 * is clean and Quest will insert new Abox assertions into
					 * the database.
					 */

					/* Creating the ABox repository */

					if (!dataRepository.isDBSchemaDefined(localConnection)) {
						dataRepository.createDBSchema(localConnection, false);
						dataRepository.insertMetadata(localConnection);
					}

				} else {
					/*
					 * Here we expect the repository to be already created in
					 * the database, we will restore the repository and we will
					 * NOT insert any data in the repo, it should have been
					 * inserted already.
					 */
					dataRepository.loadMetadata(localConnection);

					// TODO add code to verify that the existing semantic index
					// repository can be used
					// with the current ontology, e.g., checking the vocabulary
					// of URIs, checking the
					// ranges w.r.t. to the ontology entailments, etc.

				}

				/* Setting up the OBDA model */

				unfoldingOBDAModel.addSource(obdaSource);
				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), dataRepository.getMappings());

				uriRefIds = dataRepository.getUriIds();
				uriMap = dataRepository.getUriMap();

			} else if (aboxMode.equals(QuestConstants.VIRTUAL)) {

				// log.debug("Working in virtual mode");

				Collection<OBDADataSource> sources = this.inputOBDAModel.getSources();
				if (sources == null || sources.size() == 0)
					throw new Exception(
							"No datasource has been defined. Virtual ABox mode requires exactly 1 data source in your OBDA model.");
				if (sources.size() > 1)
					throw new Exception(
							"Quest in virtual ABox mode only supports OBDA models with 1 single data source. Your OBDA model contains "
									+ sources.size() + " data sources. Please remove the aditional sources.");

				/* Setting up the OBDA model */

				obdaSource = sources.iterator().next();

				log.debug("Testing DB connection...");
				connect();

				// setup connection pool
				setupConnectionPool();

				unfoldingOBDAModel.addSource(obdaSource);

				/*
				 * Processing mappings with respect to the vocabulary
				 * simplification
				 */

				MappingVocabularyTranslator mtrans = new MappingVocabularyTranslator();
				Collection<OBDAMappingAxiom> newMappings = mtrans.translateMappings(
						this.inputOBDAModel.getMappings(obdaSource.getSourceID()), equivalenceMaps);

				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), newMappings);

			}

			// NOTE: Currently the system only supports one data source.
			//
			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);
			URI sourceId = datasource.getSourceID();

			//if the metadata was not already set
			if (metadata == null) {

				metadata = JDBCConnectionManager.getMetaData(localConnection);
			}
		
			SQLDialectAdapter sqladapter = SQLAdapterFactory
					.getSQLDialectAdapter(datasource
							.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));

			JDBCUtility jdbcutil = new JDBCUtility(
					datasource
							.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			datasourceQueryGenerator = new SQLGenerator(metadata, jdbcutil,
					sqladapter);
			if (isSemanticIdx) {
				datasourceQueryGenerator.setUriIds(uriRefIds);
			}

			preprocessProjection(localConnection, unfoldingOBDAModel.getMappings(sourceId), fac, sqladapter);

			/***
			 * Starting mapping processing
			 */

			MappingAnalyzer analyzer = new MappingAnalyzer(unfoldingOBDAModel.getMappings(sourceId), metadata);

			unfoldingProgram = analyzer.constructDatalogProgram();

			/***
			 * T-Mappings
			 */
			boolean optimizeMap = true;

			if ((aboxMode.equals(QuestConstants.VIRTUAL))) {
				log.debug("Original mapping size: {}", unfoldingProgram.getRules().size());

				/*
				 * Normalizing language tags. Making all LOWER CASE
				 */

				normalizeLanguageTagsinMappings(fac, unfoldingProgram);

				/*
				 * Normalizing equalities
				 */

				DatalogNormalizer.enforceEqualities(unfoldingProgram);

				unfoldingProgram = applyTMappings(metadata, optimizeMap, unfoldingProgram, sigma, true);

				/*
				 * Adding data typing on the mapping axioms.
				 */
				extendTypesWithMetadata(metadata, unfoldingProgram);

				/*
				 * Adding NOT NULL conditions to the variables used in the head
				 * of all mappings to preserve SQL-RDF semantics
				 */
				addNOTNULLToMappings(fac, unfoldingProgram);

			}

			/*
			 * 
			 * 
			 * /* Collecting URI templates
			 */
			generateURITemplateMatchers(fac, unfoldingProgram);

			/*
			 * Adding "triple(x,y,z)" mappings for support of unbounded
			 * predicates and variables as class names (implemented in the
			 * sparql translator)
			 */

			unfoldingProgram.appendRule(generateTripleMappings(fac, unfoldingProgram));

			log.debug("Final set of mappings: \n{}", unfoldingProgram);

			log.debug("DB Metadata: \n{}", metadata);

			/**
			 * Setting up the unfolder and SQL generation
			 */

			pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

			unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);

			/***
			 * Setting up the TBox we will use for the reformulation
			 */
			Ontology reducedOntology;
			if (bOptimizeTBoxSigma) {
				SigmaTBoxOptimizer reducer = new SigmaTBoxOptimizer(reformulationOntology, sigma);
				reducedOntology = reducer.getReducedOntology();
			} else {
				reducedOntology = reformulationOntology;
			}

			/*
			 * Setting up the reformulation engine
			 */

			setupRewriter(reducedOntology, sigma);

			Ontology saturatedSigma = sigma.clone();
			saturatedSigma.saturate();

			List<CQIE> sigmaRules = createSigmaRules(saturatedSigma);
			if (optimizeMap)
				sigmaRulesIndex = createSigmaRulesIndex(sigmaRules);
			else
				sigmaRulesIndex = new HashMap<Predicate, List<CQIE>>();

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */
			vocabularyValidator = new QueryVocabularyValidator(reformulationOntology, equivalenceMaps);

			log.debug("... Quest has been initialized.");
		} catch (Exception e) {
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
		} finally {
			if (!(aboxMode.equals(QuestConstants.CLASSIC) && (inmemory))) {
				/*
				 * If we are not in classic + inmemory mode we can discconect
				 * the house-keeping connection, it has already been used.
				 */
				disconnect();
			}
		}
	}

	public void updateSemanticIndexMappings() throws Exception {
		/* Setting up the OBDA model */

		unfoldingOBDAModel.removeAllMappings(obdaSource.getSourceID());

		unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), dataRepository.getMappings());

		MappingAnalyzer analyzer = new MappingAnalyzer(unfoldingOBDAModel.getMappings(obdaSource.getSourceID()), metadata);

		unfoldingProgram = analyzer.constructDatalogProgram();

		unfoldingProgram = applyTMappings(metadata, true, unfoldingProgram, sigma, false);
		;

		/*
		 * Adding "triple(x,y,z)" mappings for support of unbounded predicates
		 * and variables as class names (implemented in the sparql translator)
		 */

		unfoldingProgram.appendRule(generateTripleMappings(OBDADataFactoryImpl.getInstance(), unfoldingProgram));

		generateURITemplateMatchers(OBDADataFactoryImpl.getInstance(), unfoldingProgram);

		log.debug("Final set of mappings: \n{}", unfoldingProgram);

		/**
		 * Setting up the unfolder and SQL generation
		 */

		pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

		unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);

		log.debug("Mappings and unfolder have been updated after inserts to the semantic index DB");

	}

	private void setupRewriter(Ontology reformulationOntology, Ontology sigma) {
		if (reformulate == false) {
			rewriter = new DummyReformulator();
		} else if (QuestConstants.PERFECTREFORMULATION.equals(reformulationTechnique)) {
			rewriter = new DLRPerfectReformulator();
		} else if (QuestConstants.UCQBASED.equals(reformulationTechnique)) {
			rewriter = new TreeRedReformulator();
		} else if (QuestConstants.TW.equals(reformulationTechnique)) {
			rewriter = new TreeWitnessRewriter();
		} else {
			throw new IllegalArgumentException("Invalid value for argument: " + QuestPreferences.REFORMULATION_TECHNIQUE);
		}

		rewriter.setTBox(reformulationOntology);
		rewriter.setCBox(sigma);
	}

	private void extendTypesWithMetadata(DBMetadata metadata, DatalogProgram unfoldingProgram) throws OBDAException {
		MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
		typeRepair.insertDataTyping(unfoldingProgram);
	}

	private void addNOTNULLToMappings(OBDADataFactory fac, DatalogProgram unfoldingProgram) {

		for (CQIE mapping : unfoldingProgram.getRules()) {
			Set<Variable> headvars = mapping.getHead().getReferencedVariables();
			for (Variable var : headvars) {
				Function notnull = fac.getFunctionIsNotNull(var);
				mapping.getBody().add(notnull);
			}
		}

	}

	private void normalizeLanguageTagsinMappings(OBDADataFactory fac, DatalogProgram unfoldingProgram) {
		for (CQIE mapping : unfoldingProgram.getRules()) {
			Function head = mapping.getHead();
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function typedTerm = (Function) term;
				Predicate type = typedTerm.getFunctionSymbol();

				if (typedTerm.getTerms().size() != 2 || !type.getName().toString().equals(OBDAVocabulary.RDFS_LITERAL_URI))
					continue;
				/*
				 * changing the language, its always the second inner term
				 * (literal,lang)
				 */
				Term originalLangTag = typedTerm.getTerm(1);
				Term normalizedLangTag = null;

				if (originalLangTag instanceof Constant) {
					ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
					normalizedLangTag = fac.getConstantLiteral(originalLangConstant.getValue().toLowerCase(), originalLangConstant.getType());
				} else {
					normalizedLangTag = originalLangTag;
				}
				typedTerm.setTerm(1, normalizedLangTag);
			}
		}
	}

	private DatalogProgram applyTMappings(DBMetadata metadata, boolean optimizeMap, DatalogProgram unfoldingProgram, Ontology sigma,
			boolean full) throws OBDAException {
		final long startTime = System.currentTimeMillis();

		TMappingProcessor tmappingProc = new TMappingProcessor(reformulationOntology, optimizeMap);
		unfoldingProgram = tmappingProc.getTMappings(unfoldingProgram, full);

		sigma.addEntities(tmappingProc.getABoxDependencies().getVocabulary());
		sigma.addAssertions(tmappingProc.getABoxDependencies().getAssertions());

		/*
		 * Eliminating redundancy from the unfolding program
		 */
		unfoldingProgram = DatalogNormalizer.enforceEqualities(unfoldingProgram);
		List<CQIE> foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);

		if (optimizeMap) {
			CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true);
			unfoldingProgram = CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true, foreignKeyRules);
		}

		final long endTime = System.currentTimeMillis();

		log.debug("TMapping size: {}", unfoldingProgram.getRules().size());
		log.debug("TMapping processing time: {} ms", (endTime - startTime));

		return unfoldingProgram;
	}

	private void generateURITemplateMatchers(OBDADataFactory fac, DatalogProgram unfoldingProgram) {

		templateStrings.clear();
		getUriTemplateMatcher().clear();

		for (int i = 0; i < unfoldingProgram.getRules().size(); i++) {

			// Looking for mappings with exactly 2 data atoms
			CQIE mapping = unfoldingProgram.getRules().get(i);
			Function head = mapping.getHead();

			/*
			 * Collecting URI templates and making pattern matchers for them.
			 */
			for (Term term : head.getTerms()) {
				if (!(term instanceof Function)) {
					continue;
				}
				Function fun = (Function) term;
				if (!(fun.getFunctionSymbol().toString().equals(OBDAVocabulary.QUEST_URI))) {
					continue;
				}
				/*
				 * This is a URI function, so it can generate pattern matchers
				 * for the URIS. We have two cases, one where the arity is 1,
				 * and there is a constant/variable. <p> The second case is
				 * where the first element is a string template of the URI, and
				 * the rest of the terms are variables/constants
				 */
				if (fun.getTerms().size() == 1) {
					/*
					 * URI without tempalte, we get it direclty from the column
					 * of the table, and the function is only f(x)
					 */
					if (templateStrings.contains("(.+)")) {
						continue;
					}
					Function templateFunction = fac.getFunction(fac.getUriTemplatePredicate(1), fac.getVariable("x"));
					Pattern matcher = Pattern.compile("(.+)");
					getUriTemplateMatcher().put(matcher, templateFunction);
					templateStrings.add("(.+)");
				} else {
					ValueConstant template = (ValueConstant) fun.getTerms().get(0);
					String templateString = template.getValue();
					templateString = templateString.replace("{}", "(.+)");

					if (templateStrings.contains(templateString)) {
						continue;
					}
					Pattern mattcher = Pattern.compile(templateString);
					getUriTemplateMatcher().put(mattcher, fun);
					templateStrings.add(templateString);
				}
			}
		}
	}

	/***
	 * Expands a SELECT * into a SELECT with all columns implicit in the *
	 * 
	 * @param mappings
	 * @param factory
	 * @param adapter
	 * @throws SQLException
	 */
	private void preprocessProjection(Connection localConnection, ArrayList<OBDAMappingAxiom> mappings, OBDADataFactory factory,
			SQLDialectAdapter adapter) throws SQLException {

		// TODO this code seems buggy, it will probably break easily (check the
		// part with
		// parenthesis in the beggining of the for loop.

		Statement st = null;
		try {
			st = localConnection.createStatement();
			for (OBDAMappingAxiom axiom : mappings) {
				String sourceString = axiom.getSourceQuery().toString();

				/*
				 * Check if the projection contains select all keyword, i.e.,
				 * 'SELECT * [...]'.
				 */
				if (containSelectAll(sourceString)) {
					StringBuilder sb = new StringBuilder();

					 // If the SQL string has sub-queries in its statement
					if (containChildParentSubQueries(sourceString)) {
						int childquery1 = sourceString.indexOf("(");
						int childquery2 = sourceString.indexOf(") as CHILD");
						String childquery = sourceString.substring(childquery1 + 1, childquery2);

						String copySourceQuery = createDummyQueryToFetchColumns(childquery, adapter);
						if (st.execute(copySourceQuery)) {
							ResultSetMetaData rsm = st.getResultSet().getMetaData();
							boolean needComma = false;
							for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
								if (needComma) {
									sb.append(", ");
								}
								String col = rsm.getColumnName(pos);
								//sb.append("CHILD." + col );
								sb.append("CHILD.\"" + col + "\" as \"CHILD_" + (col)+"\"");
								needComma = true;
							}
						}
						sb.append(", ");

						int parentquery1 = sourceString.indexOf(", (", childquery2);
						int parentquery2 = sourceString.indexOf(") as PARENT");
						String parentquery = sourceString.substring(parentquery1 + 3, parentquery2);

						copySourceQuery = createDummyQueryToFetchColumns(parentquery, adapter);
						if (st.execute(copySourceQuery)) {
							ResultSetMetaData rsm = st.getResultSet().getMetaData();
							boolean needComma = false;
							for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
								if (needComma) {
									sb.append(", ");
								}
								String col = rsm.getColumnName(pos);
								//sb.append("PARENT." + col);
								sb.append("PARENT.\"" + col + "\" as \"PARENT_" + (col)+"\"");
								needComma = true;
							}
						}

						 //If the SQL string doesn't have sub-queries
					} else 
					
					{
						String copySourceQuery = createDummyQueryToFetchColumns(sourceString, adapter);
						if (st.execute(copySourceQuery)) {
							ResultSetMetaData rsm = st.getResultSet().getMetaData();
							boolean needComma = false;
							for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
								if (needComma) {
									sb.append(", ");
								}
								sb.append("\"" + rsm.getColumnName(pos) + "\"");
								needComma = true;
							}
						}
					}

					/*
					 * Replace the asterisk with the proper column names
					 */
					String columnProjection = sb.toString();
					String tmp = axiom.getSourceQuery().toString();
					int fromPosition = tmp.toLowerCase().indexOf("from");
					int asteriskPosition = tmp.indexOf('*');
					if (asteriskPosition != -1 && asteriskPosition < fromPosition) {
						String str = sourceString.replaceFirst("\\*", columnProjection);
						axiom.setSourceQuery(factory.getSQLQuery(str));
					}
				}
			}
		} finally {
			if (st != null) {
				st.close();
			}
		}
	}

	final String selectAllPattern = "(S|s)(E|e)(L|l)(E|e)(C|c)(T|t)\\s+\\*";
	final String subQueriesPattern = "\\(.*\\)\\s+(A|a)(S|s)\\s+(C|c)(H|h)(I|i)(L|l)(D|d),\\s+\\(.*\\)\\s+(A|a)(S|s)\\s+(P|p)(A|a)(R|r)(E|e)(N|n)(T|t)";

	private boolean containSelectAll(String sql) {
		final Pattern pattern = Pattern.compile(selectAllPattern);
		return pattern.matcher(sql).find();
	}

	private boolean containChildParentSubQueries(String sql) {
		final Pattern pattern = Pattern.compile(subQueriesPattern);
		return pattern.matcher(sql).find();
	}

	private String createDummyQueryToFetchColumns(String originalQuery, SQLDialectAdapter adapter) {
		String toReturn = String.format("select * from (%s) view20130219 ", originalQuery);
		if (adapter instanceof SQLServerSQLDialectAdapter) {
			SQLServerSQLDialectAdapter sqlServerAdapter = (SQLServerSQLDialectAdapter) adapter;
			toReturn = sqlServerAdapter.sqlLimit(toReturn, 1);
		} else {
			toReturn += adapter.sqlSlice(0, Long.MIN_VALUE);
		}
		return toReturn;
	}

	private List<CQIE> createSigmaRules(Ontology ontology) {
		List<CQIE> rules = new ArrayList<CQIE>();
		Set<Axiom> assertions = ontology.getAssertions();
		for (Axiom assertion : assertions) {
			try {
				CQIE rule = AxiomToRuleTranslator.translate(assertion);
				rules.add(rule);
			} catch (UnsupportedOperationException e) {
				log.warn(e.getMessage());
			}
		}
		return rules;
	}

	private Map<Predicate, List<CQIE>> createSigmaRulesIndex(List<CQIE> sigmaRules) {
		Map<Predicate, List<CQIE>> sigmaRulesMap = new HashMap<Predicate, List<CQIE>>();
		for (CQIE rule : sigmaRules) {
			Function atom = rule.getBody().get(0); // The rule always has one
													// body atom
			Predicate predicate = atom.getFunctionSymbol();
			List<CQIE> rules = sigmaRulesMap.get(predicate);
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				sigmaRulesMap.put(predicate, rules);
			}
			rules.add(rule);
		}
		return sigmaRulesMap;
	}

	/***
	 * Creates mappings with heads as "triple(x,y,z)" from mappings with binary
	 * and unary atoms"
	 * 
	 * @param fac
	 * @param unfoldingProgram
	 * @return
	 */
	private List<CQIE> generateTripleMappings(OBDADataFactory fac, DatalogProgram unfoldingProgram) {
		List<CQIE> newmappings = new LinkedList<CQIE>();

		for (CQIE mapping : unfoldingProgram.getRules()) {
			Function newhead = null;
			Function currenthead = mapping.getHead();
			Predicate pred = OBDAVocabulary.QUEST_TRIPLE_PRED;
			LinkedList<Term> terms = new LinkedList<Term>();
			if (currenthead.getArity() == 1) {
				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				terms.add(currenthead.getTerm(0));
				Function rdfTypeConstant = fac.getFunction(fac.getUriTemplatePredicate(1),
						fac.getConstantURI(OBDAVocabulary.RDF_TYPE));
				terms.add(rdfTypeConstant);

				String classname = currenthead.getFunctionSymbol().getName();
				terms.add(fac.getFunction(fac.getUriTemplatePredicate(1), fac.getConstantURI(classname)));
				newhead = fac.getFunction(pred, terms);

			} else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				terms.add(currenthead.getTerm(0));

				String propname = currenthead.getFunctionSymbol().getName();
				Function propconstant = fac.getFunction(fac.getUriTemplatePredicate(1), fac.getConstantURI(propname));
				terms.add(propconstant);
				terms.add(currenthead.getTerm(1));
				newhead = fac.getFunction(pred, terms);
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
		return newmappings;
	}

	private void setupConnectionPool() {
		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		poolProperties = new PoolProperties();
		poolProperties.setUrl(url);
		poolProperties.setDriverClassName(driver);
		poolProperties.setUsername(username);
		poolProperties.setPassword(password);
		poolProperties.setJmxEnabled(true);

		// TEST connection before using it
		poolProperties.setTestOnBorrow(keepAlive);
		if (keepAlive) {
			if (driver.contains("oracle"))
				poolProperties.setValidationQuery("select 1 from dual");
			else if (driver.contains("db2"))
				poolProperties.setValidationQuery("select 1 from sysibm.sysdummy1");
			else
				poolProperties.setValidationQuery("select 1");
		}

		poolProperties.setTestOnReturn(false);
		poolProperties.setMaxActive(maxPoolSize);
		poolProperties.setMaxIdle(maxPoolSize);
		poolProperties.setInitialSize(startPoolSize);
		poolProperties.setMaxWait(10000);
		poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
		poolProperties.setMinEvictableIdleTimeMillis(30000);
		poolProperties.setLogAbandoned(removeAbandoned);
		poolProperties.setRemoveAbandoned(removeAbandoned);
		poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
		tomcatPool = new DataSource();
		tomcatPool.setPoolProperties(poolProperties);

		log.debug("Connection Pool Properties:");
		log.debug("Start size: " + startPoolSize);
		log.debug("Max size: " + maxPoolSize);
		log.debug("Remove abandoned connections: " + removeAbandoned);

	}

	public void close() {
		tomcatPool.close();
	}

	public void releaseSQLPoolConnection(Connection co) {
		try {
			co.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized Connection getSQLPoolConnection() throws OBDAException {
		Connection conn = null;
		try {
			conn = tomcatPool.getConnection();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
		return conn;
	}

	/***
	 * Establishes a new connection to the data source. This is a normal JDBC
	 * connection. Used only internally to get metadata at the moment.
	 * 
	 * @return
	 * @throws OBDAException
	 */
	private Connection getSQLConnection() throws OBDAException {
		Connection conn;

		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		// if (driver.contains("mysql")) {
		// url = url + "?relaxAutoCommit=true";
		// }
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			log.debug(e1.getMessage());
		}
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		} catch (Exception e) {
			throw new OBDAException(e.getMessage());
		}
		return conn;
	}

	// get a real (non pool) connection - used for protege plugin
	public QuestConnection getNonPoolConnection() throws OBDAException {

		return new QuestConnection(this, getSQLConnection());
	}

	/***
	 * Returns a QuestConnection, the main object that a client should use to
	 * access the query answering services of Quest. With the QuestConnection
	 * you can get a QuestStatement to execute queries.
	 * 
	 * <p>
	 * Note, the QuestConnection is not a normal JDBC connection. It is a
	 * wrapper of one of the N JDBC connections that quest's connection pool
	 * starts on initialization. Calling .close() will not actually close the
	 * conneciton, with will just release it back to the pool.
	 * <p>
	 * to close all connections you must call Quest.close().
	 * 
	 * @return
	 * @throws OBDAException
	 */
	public QuestConnection getConnection() throws OBDAException {

		return new QuestConnection(this, getSQLPoolConnection());
	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}

	public void setUriRefIds(Map<String, Integer> uriIds) {
		this.uriRefIds = uriIds;
	}

	public Map<String, Integer> getUriRefIds() {
		return uriRefIds;
	}

	public void setUriMap(LinkedHashMap<Integer, String> uriMap) {
		this.uriMap = uriMap;
	}

	public Map<Integer, String> getUriMap() {
		return uriMap;
	}

	public void repositoryChanged() {
		// clear cache
		this.querycache.clear();
	}

	public RDBMSSIRepositoryManager getSIRepo() {
		return dataRepository;
	}

	public boolean isSemIdx() {
		return isSemanticIdx;
	}
}
