package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi2.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDirectDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingVocabularyTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingProcessor;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6074403119825754295L;

	/***
	 * Internal components
	 */

	/* The active ABox repository, might be null */
	protected RDBMSDataRepositoryManager dataRepository = null;

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
	protected SourceQueryGenerator datasourceQueryGenerator = null;

	/* The active query evaluation engine */
	protected EvaluationEngine evaluationEngine = null;

	/* The active ABox dependencies */
	protected Ontology sigma = null;

	/* The TBox used for query reformulation */
	protected Ontology reformulationOntology = null;

	/* The merge and tranlsation of all loaded ontologies */
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

	private boolean optimizeSigma = false;

	private String reformulationTechnique = QuestConstants.UCQBASED;

	private boolean bOptimizeEquivalences = true;

	private boolean bOptimizeTBoxSigma = true;

	private boolean bObtainFromOntology = true;

	private boolean bObtainFromMappings = true;

	private String unfoldingMode = QuestConstants.CLASSIC;

	private String dbType = QuestConstants.SEMANTIC;

	private OBDADataSource obdaSource;

	private QuestPreferences preferences;

	private boolean inmemory;

	private String aboxJdbcURL;

	private String aboxJdbcUser;

	private String aboxJdbcPassword;

	private String aboxJdbcDriver;

	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		inputOBDAModel = (OBDAModel) model.clone();
	}

	// TODO this method is buggy
	public void loadDependencies(Ontology sigma) {
		rewriter.setCBox(sigma);
	}

	public Ontology getOntology() {
		return this.inputTBox;
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
			this.evaluationEngine.dispose();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		try {
			disconnect();
		} catch (Exception e) {
			log.error(e.getMessage());
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

	public void setPreferences(QuestPreferences preferences) {
		this.preferences = preferences;

		reformulationTechnique = (String) preferences.getCurrentValue(QuestPreferences.REFORMULATION_TECHNIQUE);
		bOptimizeEquivalences = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_EQUIVALENCES);
		bOptimizeTBoxSigma = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_TBOX_SIGMA);
		// boolean bUseInMemoryDB = preferences.getCurrentValue(
		// ReformulationPlatformPreferences.DATA_LOCATION).equals(
		// QuestConstants.INMEMORY);
		bObtainFromOntology = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		bObtainFromMappings = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		unfoldingMode = (String) preferences.getCurrentValue(QuestPreferences.ABOX_MODE);
		dbType = (String) preferences.getCurrentValue(QuestPreferences.DBTYPE);
		inmemory = preferences.getProperty(QuestPreferences.STORAGE_LOCATION).equals(QuestConstants.INMEMORY);

		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(QuestPreferences.JDBC_URL);
			aboxJdbcUser = preferences.getProperty(QuestPreferences.DBUSER);
			aboxJdbcPassword = preferences.getProperty(QuestPreferences.DBPASSWORD);
			aboxJdbcDriver = preferences.getProperty(QuestPreferences.JDBC_DRIVER);
		}

		log.info("Active preferences:");

		for (Object key : preferences.keySet()) {
			log.info("{} = {}", key, preferences.get(key));
		}
	}

	/***
	 * Starts the local connection that Quest maintains to the DBMS. This
	 * connection belongs only to Quest and is used to get information from the
	 * DBMS. At the momemnt this connection is mainly used during
	 * initialization, to get metadata about the DBMS or to create repositories
	 * in classic mode.
	 * 
	 * @return
	 * @throws SQLException
	 */
	private boolean connect() throws SQLException {
		if (localConnection != null && !localConnection.isClosed())
			return true;

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

	// public boolean isConnected() throws SQLException {
	// if (conn == null || conn.isClosed())
	// return false;
	// return true;
	// }

	public void setupRepository() throws Exception {

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		log.debug("Initializing Quest's query answering engine...");

		/*
		 * Input checking (we need to extend this)
		 */

		if (unfoldingMode.equals(QuestConstants.VIRTUAL) && inputOBDAModel == null) {
			throw new Exception("ERROR: Working in virtual mode but no OBDA model has been defined.");
		}

		/*
		 * Fixing the typing of predicates, in case they are not properly given.
		 */

		log.debug("Fixing vocabulary typing");

		if (inputOBDAModel != null) {
			MappingVocabularyRepair repairmodel = new MappingVocabularyRepair();
			repairmodel.fixOBDAModel(inputOBDAModel, this.inputTBox.getVocabulary());
		}

		unfoldingOBDAModel = fac.getOBDAModel();
		sigma = OntologyFactoryImpl.getInstance().createOntology();

		/*
		 * Simplifying the vocabulary of the TBox
		 */

		if (bOptimizeEquivalences) {
			log.debug("Equivalence optimization. Input ontology: {}", inputTBox.toString());
			EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(inputTBox);
			equiOptimizer.optimize();

			/* This generates a new TBox with a simpler vocabulary */
			reformulationOntology = equiOptimizer.getOptimalTBox();

			/*
			 * This is used to simplify the vocabulary of ABox assertions and
			 * mappings
			 */
			equivalenceMaps = equiOptimizer.getEquivalenceMap();
			log.debug("Equivalence optimization. Output ontology: {}", reformulationOntology.toString());
		} else {
			reformulationOntology = inputTBox;
			equivalenceMaps = new HashMap<Predicate, Description>();
		}

		try {

			/*
			 * Preparing the data source
			 */

			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {

				log.debug("Working in classic mode");

				// if (bUseInMemoryDB || createMappings) {

				if (inmemory) {
					log.debug("Using in an memory database");
					String driver = "org.h2.Driver";
					String url = "jdbc:h2:mem:questrepository:" + System.currentTimeMillis();
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
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, aboxJdbcDriver);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, aboxJdbcPassword);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, aboxJdbcURL);
					obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, aboxJdbcUser);
					obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "false");
					obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
				}

				connect();

				if (dbType.equals(QuestConstants.SEMANTIC)) {
					dataRepository = new RDBMSSIRepositoryManager(reformulationOntology.getVocabulary());

				} else if (dbType.equals(QuestConstants.DIRECT)) {
					dataRepository = new RDBMSDirectDataRepositoryManager(reformulationOntology.getVocabulary());

				} else {
					throw new Exception(dbType
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
				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), dataRepository.getMappings());

				for (Axiom axiom : dataRepository.getABoxDependencies().getAssertions()) {
					sigma.addEntities(axiom.getReferencedEntities());
					sigma.addAssertion(axiom);
				}

			} else if (unfoldingMode.equals(QuestConstants.VIRTUAL)) {

				log.debug("Working in virtual mode");

				Collection<OBDADataSource> sources = this.inputOBDAModel.getSources();
				if (sources == null || sources.size() == 0) {
					throw new Exception("No datasource has been defined");
				} else if (sources.size() > 1) {
					throw new Exception("Currently the reasoner can only handle one datasource");
				} else {

					/* Setting up the OBDA model */

					obdaSource = sources.iterator().next();

					String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
					String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
					String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
					String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

					try {
						Class.forName(driver);
					} catch (ClassNotFoundException e1) {
						// Does nothing because the SQLException handles this
						// problem also.
					}
					log.debug("Establishing an internal connection for house-keeping");
					localConnection = DriverManager.getConnection(url, username, password);

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

			}

			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);
			URI sourceId = datasource.getSourceID();

			DBMetadata metadata = JDBCConnectionManager.getMetaData(localConnection);
			MappingAnalyzer analyzer = new MappingAnalyzer(unfoldingOBDAModel.getMappings(sourceId), metadata);
			unfoldingProgram = analyzer.constructDatalogProgram();

			/*
			 * T-mappings implementation
			 */
			if (unfoldingMode.equals(QuestConstants.VIRTUAL)
					|| (unfoldingMode.equals(QuestConstants.CLASSIC) && dbType.equals(QuestConstants.DIRECT))) {
				log.debug("Setting up T-Mappings");
				TMappingProcessor tmappingProc = new TMappingProcessor(reformulationOntology);
				unfoldingProgram = tmappingProc.getTMappings(unfoldingProgram);
				log.debug("Resulting mappings: {}", unfoldingProgram.getRules().size());
				sigma.addEntities(tmappingProc.getABoxDependencies().getVocabulary());
				sigma.addAssertions(tmappingProc.getABoxDependencies().getAssertions());
			}
			
			/*
			 * Eliminating redundancy from the unfolding program
			 */
			unfoldingProgram = DatalogNormalizer.normalizeDatalogProgram(unfoldingProgram);
					
			int unprsz = unfoldingProgram.getRules().size();
			CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true);			
			log.debug("Optimizing unfolding program. Initial size: {} Final size: {}", unprsz, unfoldingProgram.getRules().size());
			

			/*
			 * Setting up the unfolder and SQL generation
			 */
			unfolder = new DatalogUnfolder(unfoldingProgram, metadata);
			JDBCUtility jdbcutil = new JDBCUtility(datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			datasourceQueryGenerator = new SQLGenerator(metadata, jdbcutil);

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

			if (QuestConstants.PERFECTREFORMULATION.equals(reformulationTechnique)) {
				rewriter = new DLRPerfectReformulator();
			} else if (QuestConstants.UCQBASED.equals(reformulationTechnique)) {
				rewriter = new TreeRedReformulator();
			} else {
				throw new IllegalArgumentException("Invalid value for argument: " + QuestPreferences.REFORMULATION_TECHNIQUE);
			}

			rewriter.setTBox(reformulationOntology);
			rewriter.setCBox(sigma);

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */

			vocabularyValidator = new QueryVocabularyValidator(reformulationOntology, equivalenceMaps);

			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {

			log.error(e.getMessage(), e);
			OBDAException ex = new OBDAException(e.getMessage(), e) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
			};
			e.fillInStackTrace();

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

		}
	}

	/***
	 * Establishes a new connection to the data source.
	 * 
	 * @return
	 * @throws OBDAException
	 */
	public QuestConnection getConnection() throws OBDAException {

		Connection conn;

		String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			log.debug(e1.getMessage());
		}
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}

		return new QuestConnection(this, conn);

	}

}
