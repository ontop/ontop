package it.unibz.krdb.obda.owlrefplatform.core;

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


import com.google.common.collect.Lists;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RepositoryChangedListener;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.VocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLAdapterFactory;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DummyReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeWitnessRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.sql.SQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.MappingVocabularyRepair;
import it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.krdb.obda.utils.MappingParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.ImplicitDBConstraints;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.RelationJSQL;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.openrdf.query.parser.ParsedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class Quest implements Serializable, RepositoryChangedListener {

	private static final long serialVersionUID = -6074403119825754295L;

	private PoolProperties poolProperties = null;
	private DataSource tomcatPool = null;
	// Tomcat pool default properties
	// These can be changed in the properties file
	protected int maxPoolSize = 20;
	protected int startPoolSize = 2;
	protected boolean removeAbandoned = true;
	protected boolean logAbandoned = false;
	protected int abandonedTimeout = 60; // 60 seconds
	protected boolean keepAlive = true;
	
	// Whether to print primary and foreign keys to stdout.
	private boolean printKeys;

	/***
	 * Internal components
	 */

	/* The active ABox repository (is null if there is no Semantic Index, i.e., in Virtual Mode) */
	private RDBMSSIRepositoryManager dataRepository = null;

	private VocabularyValidator vocabularyValidator;

	/* The active connection used to get metadata from the DBMS */
	private transient Connection localConnection = null;

	/* The active query rewriter */
	private QueryRewriter rewriter;

	/* The active SQL generator */
	private SQLQueryGenerator datasourceQueryGenerator = null;

	/* The active query evaluation engine */
	protected EvaluationEngine evaluationEngine = null;

	/* The TBox used for query reformulation (ROMAN: not really, it can be reduced by Sigma) */
	private TBoxReasoner reformulationReasoner;

	private LinearInclusionDependencies sigma;
	
	/* The merge and translation of all loaded ontologies */
	private final Ontology inputOntology;

	/* The input OBDA model */
	private OBDAModel inputOBDAModel = null;

	/* The input OBDA model */
	private OBDAModel unfoldingOBDAModel;
	
	private QuestUnfolder unfolder;
		
	/**
	 * This represents user-supplied constraints, i.e. primary
	 * and foreign keys not present in the database metadata
	 */
	private ImplicitDBConstraints userConstraints = null;
	
	/*
	 * Whether to apply the user-supplied database constraints given above
	 * userConstraints must be initialized and non-null whenever this is true
	 */
	private boolean applyUserConstraints;

	/** Davide> Exclude specific predicates from T-Mapping approach **/
	private TMappingExclusionConfig excludeFromTMappings = TMappingExclusionConfig.empty();
	
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

	private String reformulationTechnique = QuestConstants.UCQBASED;

	private boolean bOptimizeEquivalences = true;

	private boolean bObtainFromOntology = true;

	private boolean bObtainFromMappings = true;
	
	private boolean obtainFullMetadata = false;

    private boolean sqlGenerateReplace = true;

	private boolean distinctResultSet = false;

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
	 * are used by the statements
	 */

	private final Map<String, String> querycache = new ConcurrentHashMap<String, String>();

	private final Map<String, List<String>> signaturecache = new ConcurrentHashMap<String, List<String>>();

	private final Map<String, ParsedQuery> sesameQueryCache = new ConcurrentHashMap<String, ParsedQuery>();

//	private final Map<String, Boolean> isbooleancache = new ConcurrentHashMap<String, Boolean>();
//	private final Map<String, Boolean> isconstructcache = new ConcurrentHashMap<String, Boolean>();
//	private final Map<String, Boolean> isdescribecache = new ConcurrentHashMap<String, Boolean>();

	private DBMetadata metadata;


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
		this(tbox, null, null, config);
	}

	public Quest(Ontology tbox, OBDAModel mappings, Properties config) {
		this(tbox, mappings, null, config);
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
	 *            . The mappings of the system. The vocabulary of the mappings
	 *            must be subset or equal to the vocabulary of the ontology.
	 * @param config
	 *            . The configuration parameters for quest. See
	 *            QuestDefaults.properties for a description (in
	 *            src/main/resources)
	 */
	public Quest(Ontology tbox, OBDAModel mappings, DBMetadata metadata, Properties config) {
		if (tbox == null)
			throw new InvalidParameterException("TBox cannot be null");
		
		inputOntology = tbox;
		this.metadata = metadata;

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
	
	

	/** Davide> Exclude specific predicates from T-Mapping approach **/
	public void setExcludeFromTMappings(TMappingExclusionConfig excludeFromTMappings){
		assert(excludeFromTMappings != null);
		this.excludeFromTMappings = excludeFromTMappings;
	}

	/**
	 * Supply user constraints: that is primary and foreign keys not in the database
	 * Can be useful for eliminating self-joins
	 *
	 * @param userConstraints User supplied primary and foreign keys (only useful if these are not in the metadata)
	 * 						May be used by ontop to eliminate self-joins
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints userConstraints){
		assert(userConstraints != null);
		this.userConstraints = userConstraints;
		this.applyUserConstraints = true;
	}

	protected String getCachedSQL(String query) {
		return querycache.get(query);
	}
	
	protected boolean hasCachedSQL(String query) {
		return querycache.containsKey(query);
	}

	protected void cacheSQL(String strquery, String sql) {
		querycache.put(strquery, sql);
	}
	
	// TODO: replace by a couple of methods to get/set values
	protected Map<String, List<String>> getSignatureCache() {
		return signaturecache;
	}
	// TODO: replace by a couple of methods to get/set value 
	// Note, however, that this one is never read (only put in QuestStatement)
	protected Map<String, ParsedQuery> getSesameQueryCache() {
		return sesameQueryCache;
	}
	
	
	public TBoxReasoner getReasoner() {
		return reformulationReasoner;
	}
	
	public DatalogProgram getRewriting(DatalogProgram cqie) throws OBDAException {
		return rewriter.rewrite(cqie);
	}

	public DatalogProgram getOptimizedRewriting(DatalogProgram cqie) throws OBDAException {
		// Query optimization w.r.t Sigma rules
		for (CQIE cq : cqie.getRules())
			CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
		cqie = rewriter.rewrite(cqie);
		for (CQIE cq : cqie.getRules())
			CQCUtilities.optimizeQueryWithSigmaRules(cq.getBody(), sigma);
		return cqie;
	}
	
	public QuestUnfolder getUnfolder() {
		return unfolder;
	}

	public ExpressionEvaluator getExpressionEvaluator() {
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		evaluator.setUriTemplateMatcher(unfolder.getUriTemplateMatcher());		
		return evaluator;
	}
	
	public SparqlAlgebraToDatalogTranslator getSparqlAlgebraToDatalogTranslator() {
		SparqlAlgebraToDatalogTranslator translator = new SparqlAlgebraToDatalogTranslator(unfolder.getUriTemplateMatcher(), getUriMap());	
		return translator;
	}
	
	// used only once
	public VocabularyValidator getVocabularyValidator() {
		return vocabularyValidator;
	}

//	protected Map<String, Query> getJenaQueryCache() {
//		return jenaQueryCache;
//	}
//	protected Map<String, Boolean> getIsBooleanCache() {
//		return isbooleancache;
//	}
//	protected Map<String, Boolean> getIsConstructCache() {
//		return isconstructcache;
//	}
//	public Map<String, Boolean> getIsDescribeCache() {
//		return isdescribecache;
//	}

	private void loadOBDAModel(OBDAModel model) {

		if (model == null) {
			model = OBDADataFactoryImpl.getInstance().getOBDAModel();
		}
		inputOBDAModel = (OBDAModel) model.clone();
	}

	public OBDAModel getOBDAModel() {
		return inputOBDAModel;
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
		bObtainFromOntology = Boolean.valueOf((String) preferences.get(QuestPreferences.OBTAIN_FROM_ONTOLOGY));
		bObtainFromMappings = Boolean.valueOf((String) preferences.get(QuestPreferences.OBTAIN_FROM_MAPPINGS));
		aboxMode = (String) preferences.get(QuestPreferences.ABOX_MODE);
		aboxSchemaType = (String) preferences.get(QuestPreferences.DBTYPE);
		inmemory = preferences.getProperty(QuestPreferences.STORAGE_LOCATION).equals(QuestConstants.INMEMORY);
		
		obtainFullMetadata = Boolean.valueOf((String) preferences.get(QuestPreferences.OBTAIN_FULL_METADATA));	
		printKeys = Boolean.valueOf((String) preferences.get(QuestPreferences.PRINT_KEYS));
		distinctResultSet = Boolean.valueOf((String) preferences.get(QuestPreferences.DISTINCT_RESULTSET));
        sqlGenerateReplace = Boolean.valueOf((String) preferences.get(QuestPreferences.SQL_GENERATE_REPLACE));
                
		if (!inmemory) {
			aboxJdbcURL = preferences.getProperty(QuestPreferences.JDBC_URL);
			aboxJdbcUser = preferences.getProperty(QuestPreferences.DBUSER);
			aboxJdbcPassword = preferences.getProperty(QuestPreferences.DBPASSWORD);
			aboxJdbcDriver = preferences.getProperty(QuestPreferences.JDBC_DRIVER);
		}

		log.debug("Quest configuration:");

		log.debug("Extensional query rewriting enabled: {}", reformulate);
		//log.debug("Reformulation technique: {}", reformulationTechnique);
		if(reformulate){
			log.debug("Extensional query rewriting technique: {}", reformulationTechnique);
		}
		log.debug("Optimize TBox using class/property equivalences: {}", bOptimizeEquivalences);
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

		//TODO: check and remove this block
		/*
		 * Fixing the typing of predicates, in case they are not properly given.
		 */
		if (inputOBDAModel != null && !inputOntology.getVocabulary().isEmpty()) {
			MappingVocabularyRepair.fixOBDAModel(inputOBDAModel, inputOntology.getVocabulary());
		}

		unfoldingOBDAModel = fac.getOBDAModel();


		/*
		 * Simplifying the vocabulary of the TBox
		 */

		reformulationReasoner = TBoxReasonerImpl.create(inputOntology);
		
		if (bOptimizeEquivalences) {
			// generate a new TBox with a simpler vocabulary
			reformulationReasoner = TBoxReasonerImpl.getEquivalenceSimplifiedReasoner(reformulationReasoner);
		} 
		vocabularyValidator = new VocabularyValidator(reformulationReasoner);

		try {

			/*
			 * Preparing the data source
			 */

			if (aboxMode.equals(QuestConstants.CLASSIC)) {
				//isSemanticIdx = true;
				
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

				dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner, inputOntology.getVocabulary());
				dataRepository.addRepositoryChangedListener(this);

				if (inmemory) {

					/*
					 * in this case we we work in memory (with H2), the database
					 * is clean and Quest will insert new Abox assertions into
					 * the database.
					 */
					dataRepository.generateMetadata();
					
					/* Creating the ABox repository */
					dataRepository.createDBSchemaAndInsertMetadata(localConnection);
				} 
				else {
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
			} 
			else if (aboxMode.equals(QuestConstants.VIRTUAL)) {
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


				/*
				 * Processing mappings with respect to the vocabulary
				 * simplification
				 */

				Collection<OBDAMappingAxiom> newMappings = 
						vocabularyValidator.replaceEquivalences(inputOBDAModel.getMappings(obdaSource.getSourceID()));

				unfoldingOBDAModel.addSource(obdaSource);
				unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), newMappings);
			}

			// NOTE: Currently the system only supports one data source.
			//
			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);
			URI sourceId = datasource.getSourceID();

            SQLDialectAdapter sqladapter = SQLAdapterFactory
                    .getSQLDialectAdapter(datasource
                            .getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			
			//if the metadata was not already set
			if (metadata == null) {
				// if we have to parse the full metadata or just the table list in the mappings
				if (obtainFullMetadata) {
					metadata = JDBCConnectionManager.getMetaData(localConnection);
				} else {
					// This is the NEW way of obtaining part of the metadata
					// (the schema.table names) by parsing the mappings
					
					// Parse mappings. Just to get the table names in use
					MappingParser mParser = new MappingParser(localConnection, unfoldingOBDAModel.getMappings(sourceId));
							
					try{
						List<RelationJSQL> realTables = mParser.getRealTables();
						
						if (applyUserConstraints) {
							// Add the tables referred to by user-supplied foreign keys
							userConstraints.addReferredTables(realTables);
						}

						metadata = JDBCConnectionManager.getMetaData(localConnection, realTables);
					}catch (JSQLParserException e){
						System.out.println("Error obtaining the tables"+ e);
					}catch( SQLException e ){
						System.out.println("Error obtaining the Metadata"+ e);
					
					}
					
				}
			}
			
			//Adds keys from the text file
			if (applyUserConstraints) {
				userConstraints.addConstraints(metadata);
			}
			
			// This is true if the QuestDefaults.properties contains PRINT_KEYS=true
			// Very useful for debugging of User Constraints (also for the end user)
			if (printKeys) { 
				// Prints all primary keys
				System.out.println("\n====== Primary keys ==========");
				Collection<TableDefinition> table_list = metadata.getTables();
				for(TableDefinition dd : table_list){
					System.out.print("\n" + dd.getName() + ":");
					for(Attribute attr : dd.getPrimaryKeys() ){
						System.out.print(attr.getName() + ",");
					}
				}
				// Prints all foreign keys
				System.out.println("\n====== Foreign keys ==========");
				for(TableDefinition dd : table_list){
					System.out.print("\n" + dd.getName() + ":");
					Map<String, List<Attribute>> fkeys = dd.getForeignKeys();
					for(String fkName : fkeys.keySet() ){
							System.out.print("(" + fkName + ":");
							for(Attribute attr : fkeys.get(fkName)){
								System.out.print(attr.getName() + ",");
							}
							System.out.print("),");
					}
				}		
			}



			datasourceQueryGenerator = new SQLGenerator(metadata, sqladapter, sqlGenerateReplace, distinctResultSet, getUriMap());






			unfolder = new QuestUnfolder(unfoldingOBDAModel, metadata, localConnection, sourceId);

			/*
			 * T-Mappings and Fact mappings
			 */


			if (aboxMode.equals(QuestConstants.VIRTUAL)) {
				log.debug("Original mapping size: {}", unfolder.getRulesSize());

				 // Normalizing language tags: make all LOWER CASE
				unfolder.normalizeLanguageTagsinMappings();

				 // Normalizing equalities
				unfolder.normalizeEqualities();
				
				// Apply TMappings
				//unfolder.applyTMappings(reformulationReasoner, true, metadata);
				// Davide> Option to disable T-Mappings (TODO: Test)
				//if( tMappings ){
				unfolder.applyTMappings(reformulationReasoner, true, metadata, excludeFromTMappings);
				//}

				
                // Adding ontology assertions (ABox) as rules (facts, head with no body).
                unfolder.addClassAssertionsAsFacts(inputOntology.getClassAssertions());
                unfolder.addObjectPropertyAssertionsAsFacts(inputOntology.getObjectPropertyAssertions());
                unfolder.addDataPropertyAssertionsAsFacts(inputOntology.getDataPropertyAssertions());

				// Adding data typing on the mapping axioms.
				unfolder.extendTypesWithMetadata(reformulationReasoner, metadata);

				
				 // Adding NOT NULL conditions to the variables used in the head
				 // of all mappings to preserve SQL-RDF semantics
				unfolder.addNOTNULLToMappings();
			}

			
			unfolder.setupUnfolder(metadata);

			log.debug("DB Metadata: \n{}", metadata);

			/* The active ABox dependencies */
			sigma = LinearInclusionDependencies.getABoxDependencies(reformulationReasoner, true);
			
			
			// Setting up the TBox we will use for the reformulation
			//TBoxReasoner reasoner = reformulationReasoner;
			//if (bOptimizeTBoxSigma) {
			//	SigmaTBoxOptimizer reducer = new SigmaTBoxOptimizer(reformulationReasoner);
			//	reasoner = TBoxReasonerImpl.create(reducer.getReducedOntology());
			//} 

			// Setting up the reformulation engine
			if (reformulate == false) {
				rewriter = new DummyReformulator();
			} 
			else if (QuestConstants.TW.equals(reformulationTechnique)) {
				rewriter = new TreeWitnessRewriter();
			} 
			else {
				throw new IllegalArgumentException("Invalid value for argument: " + QuestPreferences.REFORMULATION_TECHNIQUE);
			}

			rewriter.setTBox(reformulationReasoner, sigma);

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */

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
				 * If we are not in classic + inmemory mode we can disconnect
				 * the house-keeping connection, it has already been used.
				 */
				disconnect();
			}
		}
	}

	


	public void updateSemanticIndexMappings() throws DuplicateMappingException, OBDAException {
		/* Setting up the OBDA model */

		unfoldingOBDAModel.removeAllMappings(obdaSource.getSourceID());
		unfoldingOBDAModel.addMappings(obdaSource.getSourceID(), dataRepository.getMappings());

		unfolder.updateSemanticIndexMappings(unfoldingOBDAModel.getMappings(obdaSource.getSourceID()), 
										reformulationReasoner, metadata);
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
		poolProperties.setMaxWait(30000);
		poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
		poolProperties.setMinEvictableIdleTimeMillis(30000);
		poolProperties.setLogAbandoned(logAbandoned);
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
	 * connection, with will just release it back to the pool.
	 * <p>
	 * to close all connections you must call Quest.close().
	 * 
	 * @return
	 * @throws OBDAException
	 */
	public QuestConnection getConnection() throws OBDAException {

		return new QuestConnection(this, getSQLPoolConnection());
	}
	
	public DBMetadata getMetaData() {
		return metadata;
	}

	public void repositoryChanged() {
		// clear cache
		this.querycache.clear();
	}

	public SemanticIndexURIMap getUriMap() {
		if (dataRepository != null)
			return dataRepository.getUriMap();
		else
			return null;
	}

	public RDBMSSIRepositoryManager getSemanticIndexRepository() {
		return dataRepository;
	}
	
	public SQLQueryGenerator getDatasourceQueryGenerator() {
		return datasourceQueryGenerator;		
	}

}
