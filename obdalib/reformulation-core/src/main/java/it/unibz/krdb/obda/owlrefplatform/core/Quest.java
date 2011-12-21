package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDirectDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingVocabularyTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.EquivalenceTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.translator.MappingVocabularyRepair;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.obda.utils.MappingAnalyzer;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quest implements Serializable {

	/***
	 * Internal components
	 */

	/* The active ABox repository, might be null */
	private RDBMSDataRepositoryManager dataRepository = null;

	// /* The query answering engine */
	// private TechniqueWrapper techwrapper = null;

	private QueryVocabularyValidator validator;

	/* The active connection used to evaluate the queries */
	private transient Connection conn = null;

	/* The active query rewriter */
	private QueryRewriter rewriter = null;

	/* The active unfolding engine */
	private UnfoldingMechanism unfMech = null;

	/* The active SQL generator */
	private SourceQueryGenerator gen = null;

	/* The active query evaluation engine */
	private EvaluationEngine eval_engine = null;

	/* The active ABox dependencies */
	private Ontology sigma = null;

	/* The TBox used for query reformulation */
	private Ontology reformulationOntology = null;

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology inputTBox = null;

	/* The OBDA model used for query unfolding */
	private OBDAModel unfoldingOBDAModel = null;
	
	/* As unfolding OBDAModel, but experimental */
	private DatalogProgram unfoldingProgram = null;

	/* The input OBDA model */
	private OBDAModel inputOBDAModel = null;

	/*
	 * The equivalence map for the classes/properties that have been simplified
	 */
	private Map<Predicate, Description> equivalenceMaps = null;

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

	private ReformulationPlatformPreferences preferences;

	private boolean inmemory;

	private String aboxJdbcURL;

	private String aboxJdbcUser;

	private String aboxJdbcPassword;

	private String aboxJdbcDriver;

	public Ontology getABoxDependencies() {
		return null;
	}

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

	/***
	 * Gets the internal OBDA model, the one used for reasoning and query
	 * answering.
	 * 
	 * @return
	 */
	public Ontology getOBDAModel() {
		return sigma;
	}

	// TODO This method has to be fixed... shouldnt be visible
	public Map<Predicate, Description> getEquivalenceMap() {
		return equivalenceMaps;
	}

	public void dispose() throws Exception {
		try {
			this.eval_engine.dispose();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		try {
			disconnect();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	/***
	 * Sets up the rewriting TBox
	 */
	public void loadTBox(Ontology tbox) {
		inputTBox = tbox;
		isClassified = false;
	}

	public void createIndexes() throws Exception {
		dataRepository.createIndexes();
	}

	public void dropIndexes() throws Exception {
		dataRepository.dropIndexes();
	}

	/***
	 * Inserts a stream of ABox assertions into the repository.
	 * 
	 * @param data
	 * @param recreateIndexes
	 *            Indicates if indexes (if any) should be droped before
	 *            inserting the tuples and recreated afterwards. Note, if no
	 *            index existed before the insert no drop will be done and no
	 *            new index will be created.
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data, boolean useFile) throws SQLException {

		int result = -1;
		if (!useFile)
			result = dataRepository.insertData(data);
		else {
			try {
				// File temporalFile = new File("quest-copy.tmp");
				// FileOutputStream os = new FileOutputStream(temporalFile);
				result = (int) dataRepository.loadWithFile(data);
				// os.close();

			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		return result;
	}

	/***
	 * As before, but using recreateIndexes = false.
	 * 
	 * @param data
	 * @throws SQLException
	 */
	public int insertData(Iterator<Assertion> data) throws SQLException {
		return insertData(data, false);
	}

	public Properties getPreferences() {
		return preferences;
	}

	public void setPreferences(ReformulationPlatformPreferences preferences) {
		this.preferences = preferences;

		reformulationTechnique = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
		bOptimizeEquivalences = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES);
		bOptimizeTBoxSigma = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OPTIMIZE_TBOX_SIGMA);
		// boolean bUseInMemoryDB = preferences.getCurrentValue(
		// ReformulationPlatformPreferences.DATA_LOCATION).equals(
		// QuestConstants.INMEMORY);
		bObtainFromOntology = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY);
		bObtainFromMappings = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OBTAIN_FROM_MAPPINGS);
		unfoldingMode = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.ABOX_MODE);
		dbType = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.DBTYPE);
		inmemory = preferences.getProperty(ReformulationPlatformPreferences.STORAGE_LOCATION).equals(QuestConstants.INMEMORY);

		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(ReformulationPlatformPreferences.JDBC_URL);
			aboxJdbcUser = preferences.getProperty(ReformulationPlatformPreferences.DBUSER);
			aboxJdbcPassword = preferences.getProperty(ReformulationPlatformPreferences.DBPASSWORD);
			aboxJdbcDriver = preferences.getProperty(ReformulationPlatformPreferences.JDBC_DRIVER);
		}

		log.info("Active preferences:");

		for (Object key : preferences.keySet()) {
			log.info("{} = {}", key, preferences.get(key));
		}
	}

	public OBDAStatement getStatement() throws Exception {
		QuestOBDAStatement st = new QuestOBDAStatement(unfMech, rewriter, gen, validator, conn.createStatement(), unfoldingOBDAModel, dataRepository);
		st.setUnfoldingProgram(unfoldingProgram);
		return st;
	}

	public boolean connect() throws SQLException {
		if (conn != null && !conn.isClosed())
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
		conn = DriverManager.getConnection(url, username, password);
		if (dataRepository != null) {
			dataRepository.setDatabase(conn);
		}

		if (conn != null)
			return true;
		return false;
	}

	public void disconnect() throws SQLException {
		try {
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}

	public boolean isConnected() throws SQLException {
		if (conn == null || conn.isClosed())
			return false;
		return true;
	}

	public void setupRepository() throws Exception {

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		log.debug("Initializing Quest's query answering engine...");

		/***
		 * Fixing the typing of predicates, in case they are not properly given.
		 */

		if (unfoldingMode.equals(QuestConstants.VIRTUAL) && inputOBDAModel == null) {
			throw new Exception("ERROR: Working in virtual mode but no OBDA model has been defined.");
		}

		log.debug("Fixing vocabulary typing");

		if (inputOBDAModel != null) {
			MappingVocabularyRepair repairmodel = new MappingVocabularyRepair();
			repairmodel.fixOBDAModel(inputOBDAModel, this.inputTBox.getVocabulary());
		}

		unfoldingOBDAModel = fac.getOBDAModel();

		sigma = OntologyFactoryImpl.getInstance().createOntology();
		/*
		 * PART 0: Simplifying the vocabulary of the Ontology
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
					String url = "jdbc:h2:mem:aboxdump" + System.currentTimeMillis();
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
				// this.translatedOntologyMerge.saturate();

				// VocabularyExtractor extractor = new
				// VocabularyExtractor();
				// Set<Predicate> vocabulary =
				// extractor.getVocabulary(reformulationOntology);
				if (dbType.equals(QuestConstants.SEMANTIC)) {
					dataRepository = new RDBMSSIRepositoryManager(conn, reformulationOntology.getVocabulary());

				} else if (dbType.equals(QuestConstants.DIRECT)) {
					dataRepository = new RDBMSDirectDataRepositoryManager(conn, reformulationOntology.getVocabulary());

				} else {
					throw new Exception(dbType
							+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}
				dataRepository.setTBox(reformulationOntology);

				/* Creating the ABox repository */

				if (!dataRepository.isDBSchemaDefined()) {
					dataRepository.createDBSchema(false);
					dataRepository.insertMetadata();
				}

				/* Setting up the OBDA model */

				unfoldingOBDAModel.addSource(obdaSource);
				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), dataRepository.getMappings());

				for (Axiom axiom : dataRepository.getABoxDependencies().getAssertions()) {
					sigma.addEntities(axiom.getReferencedEntities());
					sigma.addAssertion(axiom);
				}

				conn = dataRepository.getConnection();

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
					conn = DriverManager.getConnection(url, username, password);

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

			/*
			 * Setting up the unfolder and SQL generation
			 */

			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);
			URI sourceId = datasource.getSourceID();

			ArrayList<OBDAMappingAxiom> mappingList = unfoldingOBDAModel.getMappings(sourceId);

			// DBMetadata dbMetaData =
			// JDBCConnectionManager.getJDBCConnectionManager().getMetaData(sourceId);

			// MappingAnalyzer mapAnalyzer = new MappingAnalyzer(mappingList,
			// dbMetaData);

			MappingViewManager viewMan = new MappingViewManager(mappingList);
			unfMech = new ComplexMappingUnfolder(unfoldingOBDAModel.getMappings(sourceId), viewMan);

			JDBCUtility util = new JDBCUtility(datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			gen = new ComplexMappingSQLGenerator(viewMan, util);

			log.debug("Setting up the connection;");
			// eval_engine = new JDBCEngine(datasource);

			/*
			 * Setting up the ontology we will use for the reformulation
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
				throw new IllegalArgumentException("Invalid value for argument: "
						+ ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
			}

			rewriter.setTBox(reformulationOntology);
			rewriter.setCBox(sigma);

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */

			validator = new QueryVocabularyValidator(reformulationOntology, equivalenceMaps);
			
			
//			DBMetadata metadata = JDBCConnectionManager.getMetaData(conn);
//			MappingAnalyzer analyzer = new MappingAnalyzer(unfoldingOBDAModel.getMappings(sourceId), metadata);
//			unfoldingProgram = analyzer.constructDatalogProgram();
//			
			
			

			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {

			log.error(e.getMessage(), e);
			Exception ex = new Exception(e.getMessage(), e) {
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

	public boolean isIndexed() {
		if (dataRepository == null)
			return false;
		return dataRepository.isIndexed();
	}

	public void dropRepository() throws SQLException {
		if (dataRepository == null)
			return;
		dataRepository.dropDBSchema();
	}

	/***
	 * In an ABox store (classic) this methods triggers the generation of the
	 * schema and the insertion of the metadata.
	 * 
	 * @throws SQLException
	 */
	public void createDB() throws SQLException {
		dataRepository.createDBSchema(false);
		dataRepository.insertMetadata();
	}

	public void analyze() throws Exception {
		dataRepository.collectStatistics();

	}

}
