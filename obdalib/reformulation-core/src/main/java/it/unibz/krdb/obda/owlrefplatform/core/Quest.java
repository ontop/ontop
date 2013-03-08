package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
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
import it.unibz.krdb.obda.ontology.Assertion;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.iri.IRI;

public class Quest implements Serializable, RepositoryChangedListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6074403119825754295L;

	private static PoolProperties poolProperties = null;
	private static DataSource tomcatPool = null;

	// Tomcat pool default properties
	// These can be changed in the properties file
	protected int maxPoolSize = 10;
	protected int startPoolSize = 2;
	protected boolean removeAbandoned = true;
	protected int abandonedTimeout = 60; // 60 seconds
	
	
	/***
	 * Internal components
	 */

	/* The active ABox repository, might be null */
	protected RDBMSSIRepositoryManager dataRepository = null;

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

	// private ReformulationPlatformPreferences preferences = null;

	/***
	 * General flags and fields
	 */

	private boolean isClassified = false;

	private Logger log = LoggerFactory.getLogger(Quest.class);

	/***
	 * Configuration
	 */

	// private boolean optimizeEquivalences = true;

	private boolean reformulate = false;

	private boolean optimizeSigma = false;

	private String reformulationTechnique = QuestConstants.UCQBASED;

	private boolean bOptimizeEquivalences = true;

	private boolean bOptimizeTBoxSigma = true;

	private boolean bObtainFromOntology = true;

	private boolean bObtainFromMappings = true;

	private String unfoldingMode = QuestConstants.CLASSIC;

	private String dbType = QuestConstants.SEMANTIC;

	private OBDADataSource obdaSource;

	private Properties preferences;

	private boolean inmemory;

	private String aboxJdbcURL;

	private String aboxJdbcUser;

	private String aboxJdbcPassword;

	private String aboxJdbcDriver;

	private Iterator<Assertion> aboxIterator;

	Map<String, String> querycache = new ConcurrentHashMap<String, String>();

	Map<String, List<String>> signaturecache = new ConcurrentHashMap<String, List<String>>();

	Map<String, Boolean> isbooleancache = new ConcurrentHashMap<String, Boolean>();

	Map<String, Boolean> isconstructcache = new ConcurrentHashMap<String, Boolean>();

	Map<String, Boolean> isdescribecache = new ConcurrentHashMap<String, Boolean>();

	protected Map<String, String> getSQLCache() {
		return querycache;
	}

	protected Map<String, List<String>> getSignatureCache() {
		return signaturecache;
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

	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		inputOBDAModel = (OBDAModel) model.clone();
		aboxIterator = new Iterator<Assertion>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Assertion next() {
				return null;
			}

			@Override
			public void remove() {
			}
		};
	}

	public OBDAModel getOBDAModel() {
		return inputOBDAModel;
	}

	// TODO this method is buggy
	public void loadDependencies(Ontology sigma) {
		rewriter.setCBox(sigma);
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
	public void loadTBox(Ontology tbox) {
		inputTBox = tbox;
		isClassified = false;
	}

	public Properties getPreferences() {
		return preferences;
	}

	public void setPreferences(Properties preferences) {
		this.preferences = preferences;

		removeAbandoned = Boolean.valueOf((String) preferences
				.get(QuestPreferences.REMOVE_ABANDONED));
		abandonedTimeout = Integer.valueOf((String) preferences
				.get(QuestPreferences.ABANDONED_TIMEOUT));
		startPoolSize = Integer.valueOf((String) preferences
				.get(QuestPreferences.INIT_POOL_SIZE));
		maxPoolSize = Integer.valueOf((String) preferences
				.get(QuestPreferences.MAX_POOL_SIZE));
		
		reformulate = Boolean.valueOf((String) preferences
				.get(QuestPreferences.REWRITE));
		reformulationTechnique = (String) preferences
				.get(QuestPreferences.REFORMULATION_TECHNIQUE);
		bOptimizeEquivalences = Boolean.valueOf((String) preferences
				.get(QuestPreferences.OPTIMIZE_EQUIVALENCES));
		bOptimizeTBoxSigma = Boolean.valueOf((String) preferences
				.get(QuestPreferences.OPTIMIZE_TBOX_SIGMA));
		bObtainFromOntology = Boolean.valueOf((String) preferences
				.get(QuestPreferences.OBTAIN_FROM_ONTOLOGY));
		bObtainFromMappings = Boolean.valueOf((String) preferences
				.get(QuestPreferences.OBTAIN_FROM_MAPPINGS));
		unfoldingMode = (String) preferences.get(QuestPreferences.ABOX_MODE);
		dbType = (String) preferences.get(QuestPreferences.DBTYPE);
		inmemory = preferences.getProperty(QuestPreferences.STORAGE_LOCATION)
				.equals(QuestConstants.INMEMORY);

		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(QuestPreferences.JDBC_URL);
			aboxJdbcUser = preferences.getProperty(QuestPreferences.DBUSER);
			aboxJdbcPassword = preferences
					.getProperty(QuestPreferences.DBPASSWORD);
			aboxJdbcDriver = preferences
					.getProperty(QuestPreferences.JDBC_DRIVER);
		}

		log.info("Quest configuration:");
		log.info("Reformulation technique: {}", reformulationTechnique);
		log.info("Optimize equivalences: {}", bOptimizeEquivalences);
		log.info("Optimize TBox: {}", bOptimizeTBoxSigma);
		log.info("ABox mode: {}", unfoldingMode);
		if (!unfoldingMode.equals("virtual")) {
			log.info("Use in-memory database: {}", inmemory);
			log.info("Schema configuration: {}", dbType);
			log.info("Get ABox assertions from OBDA models: {}",
					bObtainFromMappings);
			log.info("Get ABox assertions from ontology: {}",
					bObtainFromOntology);
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
		if (localConnection != null && !localConnection.isClosed())
			return true;

		String url = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}
		localConnection = DriverManager.getConnection(url, username, password);

		if (localConnection != null)
			return true;
		return false;
	}

	public void disconnect() throws SQLException {
		try {
			localConnection.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	public void setupRepository() throws Exception {

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		log.debug("Initializing Quest...");

		/*
		 * Input checking (we need to extend this)
		 */

		if (unfoldingMode.equals(QuestConstants.VIRTUAL)
				&& inputOBDAModel == null) {
			throw new Exception(
					"ERROR: Working in virtual mode but no OBDA model has been defined.");
		}

		/*
		 * Fixing the typing of predicates, in case they are not properly given.
		 */

		// log.debug("Fixing vocabulary typing");

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
			// log.debug("Equivalence optimization. Input ontology: {}",
			// inputTBox.toString());
			EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(
					inputTBox);
			equiOptimizer.optimize();

			/* This generates a new TBox with a simpler vocabulary */
			reformulationOntology = equiOptimizer.getOptimalTBox();

			/*
			 * This is used to simplify the vocabulary of ABox assertions and
			 * mappings
			 */
			equivalenceMaps = equiOptimizer.getEquivalenceMap();
			// log.debug("Equivalence optimization. Output ontology: {}",
			// reformulationOntology.toString());
		} else {
			reformulationOntology = inputTBox;
			equivalenceMaps = new HashMap<Predicate, Description>();
		}

		try {

			/*
			 * Preparing the data source
			 */

			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {

				// log.debug("Working in classic mode");

				// if (bUseInMemoryDB || createMappings) {

				if (inmemory) {
					// log.debug("Using in an memory database");
					String driver = "org.h2.Driver";
					String url = "jdbc:h2:mem:questrepository:"
							+ System.currentTimeMillis();
					String username = "sa";
					String password = "";

					obdaSource = fac.getDataSource(URI
							.create("http://www.obda.org/ABOXDUMP"
									+ System.currentTimeMillis()));
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_DRIVER,
							driver);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_PASSWORD,
							password);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_URL, url);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_USERNAME,
							username);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
					obdaSource
							.setParameter(
									RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP,
									"true");

				} else {
					obdaSource = fac.getDataSource(URI
							.create("http://www.obda.org/ABOXDUMP"
									+ System.currentTimeMillis()));
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_DRIVER,
							aboxJdbcDriver);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_PASSWORD,
							aboxJdbcPassword);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_URL,
							aboxJdbcURL);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.DATABASE_USERNAME,
							aboxJdbcUser);
					obdaSource.setParameter(
							RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
					obdaSource
							.setParameter(
									RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP,
									"true");
				}

				connect();
				
				// setup connection pool
				setupConnectionPool();

				if (dbType.equals(QuestConstants.SEMANTIC)) {
					dataRepository = new RDBMSSIRepositoryManager(
							reformulationOntology.getVocabulary());
					dataRepository.addRepositoryChangedListener(this);
				} else {
					throw new Exception(
							dbType
									+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}
				dataRepository.setTBox(reformulationOntology);

				/* Creating the ABox repository */

				if (!dataRepository.isDBSchemaDefined(localConnection)) {
					dataRepository.createDBSchema(localConnection, false);
					dataRepository.insertMetadata(localConnection);
				}

				/* Setting up the OBDA model */

				unfoldingOBDAModel.addSource(obdaSource);
				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(),
						dataRepository.getMappings());

				for (Axiom axiom : dataRepository.getABoxDependencies()
						.getAssertions()) {
					sigma.addEntities(axiom.getReferencedEntities());
					sigma.addAssertion(axiom);
				}

			} else if (unfoldingMode.equals(QuestConstants.VIRTUAL)) {

				// log.debug("Working in virtual mode");

				Collection<OBDADataSource> sources = this.inputOBDAModel
						.getSources();
				if (sources == null || sources.size() == 0) {
					throw new Exception(
							"No datasource has been defined. Virtual ABox mode requires exactly 1 data source in your OBDA model.");
				} else if (sources.size() > 1) {
					throw new Exception(
							"Quest in virtual ABox mode only supports OBDA models with 1 single data source. Your OBDA model contains "
									+ sources.size()
									+ " data sources. Please remove the aditional sources.");
				} else {

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
					Collection<OBDAMappingAxiom> newMappings = mtrans
							.translateMappings(this.inputOBDAModel
									.getMappings(obdaSource.getSourceID()),
									equivalenceMaps);

					unfoldingOBDAModel.addMappings(obdaSource.getSourceID(),
							newMappings);
				}

			}

			// NOTE: Currently the system only supports one data source.
			//
			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);
			URI sourceId = datasource.getSourceID();

			DBMetadata metadata = JDBCConnectionManager
					.getMetaData(localConnection);
			SQLDialectAdapter sqladapter = SQLAdapterFactory
					.getSQLDialectAdapter(datasource
					.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			preprocessProjection(unfoldingOBDAModel.getMappings(sourceId), fac, sqladapter);
			MappingAnalyzer analyzer = new MappingAnalyzer(unfoldingOBDAModel
					.getMappings(sourceId), metadata);
			unfoldingProgram = analyzer.constructDatalogProgram();

			log.debug("Original mapping size: {}", unfoldingProgram.getRules().size());

			/*
			 * Normalizing language tags. Making all LOWER CASE
			 */

			for (CQIE mapping : unfoldingProgram.getRules()) {
				Function head = mapping.getHead();
				for (NewLiteral term : head.getTerms()) {
					if (!(term instanceof Function))
						continue;
					Function typedTerm = (Function) term;
					Predicate type = typedTerm.getFunctionSymbol();

					if (typedTerm.getTerms().size() != 2
							|| !type.getName().toString().equals(
									OBDAVocabulary.RDFS_LITERAL_URI))
						continue;
					/*
					 * changing the language, its always the second inner term
					 * (literal,lang)
					 */
					NewLiteral originalLangTag = typedTerm.getTerm(1);
					NewLiteral normalizedLangTag = null;

					if (originalLangTag instanceof Constant) {
						ValueConstant originalLangConstant = (ValueConstant) originalLangTag;
						normalizedLangTag = fac.getValueConstant(
								originalLangConstant.getValue().toLowerCase(),
								originalLangConstant.getType());
					} else {
						normalizedLangTag = originalLangTag;

					}

					typedTerm.setTerm(1, normalizedLangTag);

				}
			}

			/*
			 * Normalizing equalities
			 */

			unfoldingProgram = DatalogNormalizer
					.pushEqualities(unfoldingProgram);

			/***
			 * Adding ABox as facts in the unfolding program This feature is
			 * disabled for the current release
			 */
			
//			if (false && unfoldingMode.equals(QuestConstants.VIRTUAL)) {
//				ABoxToFactConverter.addFacts(this.aboxIterator,
//						unfoldingProgram, this.equivalenceMaps);
//			}
			
			/*
			 * T-mappings implementation
			 */

			boolean optimizeMap = true;

			final long startTime = System.currentTimeMillis();

			TMappingProcessor tmappingProc = new TMappingProcessor(
					reformulationOntology, optimizeMap);
			unfoldingProgram = tmappingProc.getTMappings(unfoldingProgram);

			sigma.addEntities(tmappingProc.getABoxDependencies().getVocabulary());
			sigma.addAssertions(tmappingProc.getABoxDependencies().getAssertions());

			/*
			 * Eliminating redundancy from the unfolding program
			 */
			unfoldingProgram = DatalogNormalizer.pushEqualities(unfoldingProgram);
			List<CQIE> foreignKeyRules = DBMetadataUtil.generateFKRules(metadata);

			if (optimizeMap) {
				CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true);
				unfoldingProgram = CQCUtilities.removeContainedQueriesSorted(
						unfoldingProgram, true, foreignKeyRules);
			}

			final long endTime = System.currentTimeMillis();

			log.debug("TMapping size: {}", unfoldingProgram.getRules().size());
			log.debug("TMapping processing time: {} ms", (endTime - startTime));

			/*
			 * Adding data typing on the mapping axioms.
			 */
			MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(metadata);
			typeRepair.insertDataTyping(unfoldingProgram);

			/*
			 * Adding NOT NULL conditions to the variables used in the head of
			 * all mappings to preserve SQL-RDF semantics
			 */
			List<CQIE> currentMappingRules = unfoldingProgram.getRules();
			for (CQIE mapping : currentMappingRules) {
				Set<Variable> headvars = mapping.getHead().getReferencedVariables();
				for (Variable var : headvars) {
					Function notnull = fac.getIsNotNullAtom(var);
					mapping.getBody().add(notnull);
				}
			}

			/*
			 * Collecting primary key data
			 */
			
			Map<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(metadata, unfoldingProgram);

			/*
			 * Collecting URI templates
			 */

			for (int i = 0; i < unfoldingProgram.getRules().size(); i++) {
				/* Looking for mappings with exactly 2 data atoms */
				CQIE mapping = currentMappingRules.get(i);
				Function head = mapping.getHead();
				/*
				 * Collecting URI templates and making pattern matchers for
				 * them.
				 */

				for (NewLiteral term : head.getTerms()) {
					if (!(term instanceof Function)) {
						continue;
					}
					Function fun = (Function) term;
					if (!(fun.getFunctionSymbol().toString().equals(OBDAVocabulary.QUEST_URI))) {
						continue;
					}
					/*
					 * This is a URI function, so it can generate pattern
					 * matchers for the URIS. We have two cases, one where the
					 * arity is 1, and there is a constant/variable. <p> The
					 * second case is where the first element is a string
					 * template of the URI, and the rest of the terms are
					 * variables/constants
					 */
					if (fun.getTerms().size() == 1) {
						/*
						 * URI without tempalte, we get it direclty from the
						 * column of the table, and the function is only f(x)
						 */
						if (templateStrings.contains("(.+)"))
							continue;
						Function templateFunction = fac.getFunctionalTerm(fac
								.getUriTemplatePredicate(1), fac
								.getVariable("x"));
						Pattern matcher = Pattern.compile("(.+)");
						getUriTemplateMatcher().put(matcher, templateFunction);
						templateStrings.add("(.+)");
					} else {
						ValueConstant template = (ValueConstant) fun.getTerms()
								.get(0);
						String templateString = template.getValue();
						templateString = templateString.replace("{}", "(.+)");

						if (templateStrings.contains(templateString))
							continue;

						Pattern mattcher = Pattern.compile(templateString);
						getUriTemplateMatcher().put(mattcher, fun);

						templateStrings.add(templateString);
					}
				}
			}

			/*
			 * Transforming body of mappings with 2 atoms into JOINs
			 */
			for (int i = 0; i < unfoldingProgram.getRules().size(); i++) {
				/* Looking for mappings with exactly 2 data atoms */
				CQIE mapping = currentMappingRules.get(i);
				int dataAtoms = 0;

				LinkedList dataAtomsList = new LinkedList();
				LinkedList otherAtomsList = new LinkedList();

				for (Function subAtom : mapping.getBody()) {
					if (subAtom.isDataFunction() || subAtom.isAlgebraFunction()) {
						dataAtoms += 1;
						dataAtomsList.add(subAtom);
					} else {
						otherAtomsList.add(subAtom);
					}
				}
				if (dataAtoms == 1) {
					continue;
				}

				/*
				 * This mapping can be transformed into a normal join with ON
				 * conditions. Doing so.
				 */
				Function foldedJoinAtom = null;

				while (dataAtomsList.size() > 1) {
					foldedJoinAtom = fac.getFunctionalTerm(
							OBDAVocabulary.SPARQL_JOIN,
							(NewLiteral) dataAtomsList.remove(0),
							(NewLiteral) dataAtomsList.remove(0));
					dataAtomsList.add(0, foldedJoinAtom);
				}

				List<Function> newBodyMapping = new LinkedList<Function>();
				newBodyMapping.add(foldedJoinAtom.asAtom());
				newBodyMapping.addAll(otherAtomsList);

				CQIE newmapping = fac
						.getCQIE(mapping.getHead(), newBodyMapping);

				unfoldingProgram.removeRule(mapping);
				unfoldingProgram.appendRule(newmapping);
				i -= 1;
			}

			/*
			 * Adding "triple(x,y,z)" mappings for support of unbounded
			 * predicates and variables as class names (implemented in the
			 * sparql translator)
			 */

			List<CQIE> newmappings = new LinkedList<CQIE>();
			generateTripleMappings(fac, newmappings);
			unfoldingProgram.appendRule(newmappings);

			log.debug("Final set of mappings: \n{}", unfoldingProgram);

			log.debug("DB Metadata: \n{}", metadata);

			/*
			 * Setting up the unfolder and SQL generation
			 */

			if (dbType.equals(QuestConstants.SEMANTIC)) {
				unfolder = new DatalogUnfolder(unfoldingProgram, pkeys, dataRepository);
			}
			else {
				unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);
			}
			JDBCUtility jdbcutil = new JDBCUtility(
					datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			datasourceQueryGenerator = new SQLGenerator(metadata, jdbcutil, sqladapter);

			/*
			 * Setting up the TBox we will use for the reformulation
			 */
			if (bOptimizeTBoxSigma) {
				SigmaTBoxOptimizer reducer = new SigmaTBoxOptimizer(reformulationOntology, sigma);
				reformulationOntology = reducer.getReducedOntology();
			}

			/*
			 * Setting up the reformulation engine
			 */

			if (reformulate == false) {
				rewriter = new DummyReformulator();
			} else if (QuestConstants.PERFECTREFORMULATION
					.equals(reformulationTechnique)) {
				rewriter = new DLRPerfectReformulator();
			} else if (QuestConstants.UCQBASED.equals(reformulationTechnique)) {
				rewriter = new TreeRedReformulator();
			} else if (QuestConstants.TW.equals(reformulationTechnique)) {
				rewriter = new TreeWitnessRewriter();
			} else {
				throw new IllegalArgumentException(
						"Invalid value for argument: "
								+ QuestPreferences.REFORMULATION_TECHNIQUE);
			}

			rewriter.setTBox(reformulationOntology);
			rewriter.setCBox(sigma);

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
			vocabularyValidator = new QueryVocabularyValidator(
					reformulationOntology, equivalenceMaps);

			log.debug("... Quest has been initialized.");
			isClassified = true;
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
		} finally {
			if (!(unfoldingMode.equals(QuestConstants.CLASSIC) && (inmemory))) {
				/*
				 * If we are not in classic + inmemory mode we can discconect
				 * the house-keeping connection, it has already been used.
				 */
				disconnect();
			}
		}
	}


	private void preprocessProjection(ArrayList<OBDAMappingAxiom> mappings, OBDADataFactory factory, 
			SQLDialectAdapter adapter) throws SQLException {
		Statement st = null;
		try {
			st = localConnection.createStatement();
			for (OBDAMappingAxiom axiom : mappings) {
				String sourceString = axiom.getSourceQuery().toString();
				if (sourceString.contains("as CHILD"))
				{
					int childquery1 = sourceString.indexOf("(");
					int childquery2 = sourceString.indexOf(") as CHILD");
					String childquery = sourceString.substring(childquery1+1, childquery2);
					
					StringBuffer sb = new StringBuffer();
					
					String copySourceQuery = createDummyQueryToFetchColumns(childquery, adapter);
					if (st.execute(copySourceQuery)) {
						ResultSetMetaData rsm = st.getResultSet().getMetaData();
						boolean needComma = false;
						for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
							if (needComma) {
								sb.append(", ");
							}
							String col = rsm.getColumnName(pos);
							sb.append("CHILD.\""+col+"\" as CHILD_"+(col));
							needComma = true;
						}
					}
					sb.append(", ");
					
					int parentquery1 = sourceString.indexOf(", (", childquery2);
					int parentquery2 = sourceString.indexOf(") as PARENT");
					String parentquery = sourceString.substring(parentquery1+3, parentquery2);
					
					 copySourceQuery = createDummyQueryToFetchColumns(parentquery, adapter);
					if (st.execute(copySourceQuery)) {
						
						ResultSetMetaData rsm = st.getResultSet().getMetaData();
						boolean needComma = false;
						for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
							if (needComma) {
								sb.append(", ");
							}
							String col = rsm.getColumnName(pos);
							sb.append("PARENT.\""+col+"\" as PARENT_"+(col));
							needComma = true;
						}
					}
					String columnProjection = sb.toString();
					
					String tmp = axiom.getSourceQuery().toString();
					int fromPosition = tmp.toLowerCase().indexOf("from");
					int asteriskPosition = tmp.indexOf('*');
					if (asteriskPosition != -1 && asteriskPosition < fromPosition) {
						String str = sourceString.replaceFirst("\\*", columnProjection);
						axiom.setSourceQuery(factory.getSQLQuery(str));
					}
				}
				
				
				
				else if (sourceString.contains("*"))
				{
				String copySourceQuery = createDummyQueryToFetchColumns(sourceString, adapter);
				if (st.execute(copySourceQuery)) {
					StringBuffer sb = new StringBuffer();
					ResultSetMetaData rsm = st.getResultSet().getMetaData();
					boolean needComma = false;
					for (int pos = 1; pos <= rsm.getColumnCount(); pos++) {
						if (needComma) {
							sb.append(", ");
						}
						
						sb.append("\""+rsm.getColumnName(pos)+"\"");
						needComma = true;
					}
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
			}
		} finally {
			if (st != null) {
				st.close();
			}
		}
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

	private Map<Predicate, List<CQIE>> createSigmaRulesIndex(
			List<CQIE> sigmaRules) {
		Map<Predicate, List<CQIE>> sigmaRulesMap = new HashMap<Predicate, List<CQIE>>();
		for (CQIE rule : sigmaRules) {
			Function atom = rule.getBody().get(0); // The rule always has one body atom
			Predicate predicate = atom.getPredicate();
			List<CQIE> rules = sigmaRulesMap.get(predicate);
			if (rules == null) {
				rules = new LinkedList<CQIE>();
				sigmaRulesMap.put(predicate, rules);
			}
			rules.add(rule);
		}
		return sigmaRulesMap;
	}

	private void generateTripleMappings(OBDADataFactory fac,
			List<CQIE> newmappings) {
		for (CQIE mapping : unfoldingProgram.getRules()) {
			Function newhead = null;
			Function currenthead = mapping.getHead();
			Predicate pred = OBDAVocabulary.QUEST_TRIPLE_PRED;
			LinkedList<NewLiteral> terms = new LinkedList<NewLiteral>();
			if (currenthead.getArity() == 1) {

				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				terms.add(currenthead.getTerm(0));
				Function rdfTypeConstant = fac.getFunctionalTerm(fac
						.getUriTemplatePredicate(1), fac
						.getURIConstant(OBDADataFactoryImpl
								.getIRI(OBDAVocabulary.RDF_TYPE)));
				// URI.create(OBDAVocabulary.RDF_TYPE)));
				terms.add(rdfTypeConstant);

				IRI classname = currenthead.getPredicate().getName();
				terms.add(fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
						fac.getURIConstant(classname)));
				newhead = fac.getAtom(pred, terms);

			} else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				terms.add(currenthead.getTerm(0));

				IRI propname = currenthead.getPredicate().getName();
				Function propconstant = fac.getFunctionalTerm(fac
						.getUriTemplatePredicate(1), fac
						.getURIConstant(propname));
				terms.add(propconstant);
				terms.add(currenthead.getTerm(1));

				newhead = fac.getAtom(pred, terms);
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
	}

//	private void setupConnectionPool() {
//	String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
//	String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
//	String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
//	String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
//	
//	if (driver.contains("mysql")) {
//		url = url + "?relaxAutoCommit=true";
//	}
//	
//	System.out.println();
//	System.out.println("Setting up connection pool:");
//	System.out.println(url);
//	System.out.println(username);
//	System.out.println(password);
//	System.out.println(driver);
//	try {
//		Class.forName(driver);
//	} catch (ClassNotFoundException e) {
//		e.printStackTrace();
//	} 
//	
//	BoneCPConfig config = new BoneCPConfig();
//	config.setJdbcUrl(url); 
//	config.setUsername(username); 
//	config.setPassword(password);
//	config.setMinConnectionsPerPartition(5);
//	config.setMaxConnectionsPerPartition(10);
//	config.setPartitionCount(2);
//	config.setCloseConnectionWatch(true);
//	config.setDetectUnclosedStatements(true);
//	config.setIdleMaxAgeInSeconds(10);
//	
//	//config.setPreparedStatementsCacheSize(0);
//	//config.setStatementCacheSize(0);
//	//config.setReleaseHelperThreads(4);
//	
//	//config.setDefaultAutoCommit(false);
//	try {
//		connectionPool = new BoneCP(config);
//	} catch (SQLException e) {
//		e.printStackTrace();
//	} 
//	System.out.println("Done");
//}
	
	
//	private void setupConnectionPool() {
//		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
//		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
//		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
//		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
//		
//		if (driver.contains("mysql")) {
//			url = url + "?relaxAutoCommit=true";
//		}
//		c3poPool = new ComboPooledDataSource();
//		try {
//			c3poPool.setDriverClass(driver );
//		} catch (PropertyVetoException e) {
//			e.printStackTrace();
//		}         
//		c3poPool.setJdbcUrl(url);
//		c3poPool.setUser(username);                                  
//		c3poPool.setPassword(password);
//		c3poPool.setMinPoolSize(3);
//		c3poPool.setMaxPoolSize(20);
//		c3poPool.setAcquireIncrement(1);
//		c3poPool.setDebugUnreturnedConnectionStackTraces(true);
//		c3poPool.setNumHelperThreads(5);
//		c3poPool.setCheckoutTimeout(0);
//	
//	}

	private void setupConnectionPool() {
		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		
		if (driver.contains("mysql")) {
			url = url + "?relaxAutoCommit=true";
		}		
        poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName(driver);
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setJmxEnabled(true);
        poolProperties.setTestOnBorrow(false);
        poolProperties.setTestOnReturn(false);
        poolProperties.setMaxActive(maxPoolSize);
        poolProperties.setMaxIdle(maxPoolSize);
        poolProperties.setInitialSize(startPoolSize);
        poolProperties.setMaxWait(10000);
        poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
        poolProperties.setMinEvictableIdleTimeMillis(30000);
        poolProperties.setLogAbandoned(removeAbandoned);
        poolProperties.setRemoveAbandoned(removeAbandoned);
        poolProperties.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        tomcatPool = new DataSource();
        tomcatPool.setPoolProperties(poolProperties);
    	
        System.out.println("Connection Pool Properties:");
        System.out.println("Start size: " + startPoolSize);
        System.out.println("Max size: " + maxPoolSize);
        System.out.println("Remove abandoned connections: " + removeAbandoned);
         
	}
	
	public void releaseSQLPoolConnection (Connection co) {
		try {
				co.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public Connection getSQLPoolConnection() throws OBDAException {
//	Connection conn = null;
//	System.out.println();
//	System.out.println("Getting SQL Pool Connection...");
//	int free = connectionPool.getTotalFree();
//	int used = connectionPool.getTotalLeased();
//	
//	System.out.println("Used connections: " + used);
//	System.out.println("Free connections: " + free);
//	System.out.println();
//	try {
//		conn = connectionPool.getConnection();		
//		//conn.setAutoCommit(false);
//		//System.out.println(conn.toString());
//	} catch (SQLException e) {
//		throw new OBDAException(e.getMessage());
//	}
//	return conn;
//}
	
//	public Connection getSQLPoolConnection() throws OBDAException {
//		Connection conn = null;
//		int busy = 0;
//		int free = 0;
//		
//		try {
//			free = c3poPool.getNumIdleConnections();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			busy = c3poPool.getNumBusyConnections();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		System.out.println("Busy: " + busy);
//		System.out.println("Free: " + free);
//		try {
//			conn = c3poPool.getConnection();
//		} catch (SQLException e) {
//			System.out.println("Error Getting a Connection!!!");
//			throw new OBDAException(e.getMessage());
//		}
//		return conn;
//	}
	
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
	 * Establishes a new connection to the data source.
	 * 
	 * @return
	 * @throws OBDAException
	 */
	protected Connection getSQLConnection() throws OBDAException {
		Connection conn;

		String url = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource
				.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		if (driver.contains("mysql")) {
			url = url + "?relaxAutoCommit=true";
		}
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
	
	public QuestConnection getConnection() throws OBDAException {

		return new QuestConnection(this, getSQLPoolConnection());
	}

	public void setABox(Iterator<Assertion> owlapi3aBoxIterator) {
		this.aboxIterator = owlapi3aBoxIterator;

	}

	public UriTemplateMatcher getUriTemplateMatcher() {
		return uriTemplateMatcher;
	}

	public void repositoryChanged() {
		// clear cache
		this.querycache.clear();
	}
}
