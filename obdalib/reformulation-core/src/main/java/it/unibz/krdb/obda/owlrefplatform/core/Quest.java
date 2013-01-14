package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.Atom;
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
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxToFactConverter;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RepositoryChangedListener;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.CQCUtilities;
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quest implements Serializable, RepositoryChangedListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6074403119825754295L;

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

	Map<String, String> querycache = new HashMap<String, String>();

	Map<String, List<String>> signaturecache = new HashMap<String, List<String>>();

	Map<String, Boolean> isbooleancache = new HashMap<String, Boolean>();

	Map<String, Boolean> isconstructcache = new HashMap<String, Boolean>();

	Map<String, Boolean> isdescribecache = new HashMap<String, Boolean>();

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

		// for (Object key : preferences.keySet()) {
		// log.info("{} = {}", key, preferences.get(key));
		// }
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

	// public boolean isConnected() throws SQLException {
	// if (conn == null || conn.isClosed())
	// return false;
	// return true;
	// }

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

		if (inputOBDAModel != null) {
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

					String url = obdaSource
							.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
					String username = obdaSource
							.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
					String password = obdaSource
							.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
					String driver = obdaSource
							.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

					log.debug("Testing DB connection...");
					connect();

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
			MappingAnalyzer analyzer = new MappingAnalyzer(
					unfoldingOBDAModel.getMappings(sourceId), metadata);
			unfoldingProgram = analyzer.constructDatalogProgram();

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
							|| !type.getName().toString()
									.equals(OBDAVocabulary.RDFS_LITERAL_URI))
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
			 * Adding ABox as facts in the unfolding program
			 * This feature is disabled for the current release
			 */
			if (false && unfoldingMode.equals(QuestConstants.VIRTUAL)) {
				ABoxToFactConverter.addFacts(this.aboxIterator,
						unfoldingProgram, this.equivalenceMaps);
			}
			/*
			 * T-mappings implementation
			 */
			// log.debug("Setting up T-Mappings");
			TMappingProcessor tmappingProc = new TMappingProcessor(
					reformulationOntology);
			unfoldingProgram = tmappingProc.getTMappings(unfoldingProgram);
			sigma.addEntities(tmappingProc.getABoxDependencies()
					.getVocabulary());
			sigma.addAssertions(tmappingProc.getABoxDependencies()
					.getAssertions());

			/*
			 * Eliminating redundancy from the unfolding program
			 */
			unfoldingProgram = DatalogNormalizer
					.pushEqualities(unfoldingProgram);

			CQCUtilities.removeContainedQueriesSorted(unfoldingProgram, true);

			/*
			 * Adding data typing on the mapping axioms.
			 */
			MappingDataTypeRepair typeRepair = new MappingDataTypeRepair(
					metadata);
			typeRepair.insertDataTyping(unfoldingProgram);

			/*
			 * Adding NOT NULL conditions to the variables used in the head of
			 * all mappings to preserve SQL-RDF semantics
			 */
			List<CQIE> currentMappingRules = unfoldingProgram.getRules();
			for (CQIE mapping : currentMappingRules) {
				Set<Variable> headvars = mapping.getHead()
						.getReferencedVariables();

				for (Variable var : headvars) {
					Atom notnull = fac.getIsNotNullAtom(var);
					mapping.getBody().add(notnull);
				}
			}

			/*
			 * Collecting primary key data
			 */
			Map<Predicate, List<Integer>> pkeys = DBMetadata.extractPKs(
					metadata, unfoldingProgram);

			/*
			 * Collecting URI templates
			 */

			for (int i = 0; i < unfoldingProgram.getRules().size(); i++) {
				/* Looking for mappings with exactly 2 data atoms */
				CQIE mapping = currentMappingRules.get(i);
				Atom head = mapping.getHead();
				/*
				 * Collecting URI templates and making pattern matchers for
				 * them.
				 */

				for (NewLiteral term : head.getTerms()) {
					if (!(term instanceof Function))
						continue;
					Function fun = (Function) term;
					if (!(fun.getFunctionSymbol().toString()
							.equals(OBDAVocabulary.QUEST_URI)))
						continue;
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
						Function templateFunction = fac.getFunctionalTerm(
								fac.getUriTemplatePredicate(1),
								fac.getVariable("x"));
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
				
				for (Atom subAtom : mapping.getBody()) {
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
							OBDAVocabulary.SPARQL_JOIN, (NewLiteral)dataAtomsList.remove(0), (NewLiteral) dataAtomsList.remove(0));
					dataAtomsList.add(0,foldedJoinAtom);
				}
						
				List<Atom> newBodyMapping = new LinkedList<Atom>();
				newBodyMapping.add(foldedJoinAtom.asAtom());
				newBodyMapping.addAll(otherAtomsList);
				
				CQIE newmapping = fac.getCQIE(mapping.getHead(), newBodyMapping);

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

			if (dbType.equals(QuestConstants.SEMANTIC))
				unfolder = new DatalogUnfolder(unfoldingProgram, pkeys,
						dataRepository);
			else
				unfolder = new DatalogUnfolder(unfoldingProgram, pkeys);

			JDBCUtility jdbcutil = new JDBCUtility(
					datasource
							.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			SQLDialectAdapter sqladapter = SQLAdapterFactory
					.getSQLDialectAdapter(datasource
							.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			datasourceQueryGenerator = new SQLGenerator(metadata, jdbcutil,
					sqladapter);

			/*
			 * Setting up the TBox we will use for the reformulation
			 */
			if (bOptimizeTBoxSigma) {
				SigmaTBoxOptimizer reducer = new SigmaTBoxOptimizer(
						reformulationOntology, sigma);
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

			/*
			 * Done, sending a new reasoner with the modules we just configured
			 */
			vocabularyValidator = new QueryVocabularyValidator(
					reformulationOntology, equivalenceMaps);

			log.debug("... Quest has been initialized.");
			isClassified = true;

		} catch (Exception e) {

			// log.error(e.getMessage(), e);
			OBDAException ex = new OBDAException(e.getMessage());
			ex.setStackTrace(e.getStackTrace());

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

	private void generateTripleMappings(OBDADataFactory fac,
			List<CQIE> newmappings) {
		for (CQIE mapping : unfoldingProgram.getRules()) {
			Atom newhead = null;
			Atom currenthead = mapping.getHead();
			Predicate pred = OBDAVocabulary.QUEST_TRIPLE_PRED;
			LinkedList<NewLiteral> terms = new LinkedList<NewLiteral>();
			if (currenthead.getArity() == 1) {

				/*
				 * head is Class(x) Forming head as triple(x,uri(rdf:type),
				 * uri(Class))
				 */
				terms.add(currenthead.getTerm(0));
				Function rdfTypeConstant = fac
						.getFunctionalTerm(fac.getUriTemplatePredicate(1), fac
								.getURIConstant(URI
										.create(OBDAVocabulary.RDF_TYPE)));
				terms.add(rdfTypeConstant);

				URI classname = currenthead.getPredicate().getName();
				terms.add(fac.getFunctionalTerm(fac.getUriTemplatePredicate(1),
						fac.getURIConstant(classname)));
				newhead = fac.getAtom(pred, terms);

			} else if (currenthead.getArity() == 2) {
				/*
				 * head is Property(x,y) Forming head as triple(x,uri(Property),
				 * y)
				 */
				terms.add(currenthead.getTerm(0));

				URI propname = currenthead.getPredicate().getName();
				Function propconstant = fac.getFunctionalTerm(
						fac.getUriTemplatePredicate(1),
						fac.getURIConstant(propname));
				terms.add(propconstant);
				terms.add(currenthead.getTerm(1));

				newhead = fac.getAtom(pred, terms);
			}
			CQIE newmapping = fac.getCQIE(newhead, mapping.getBody());
			newmappings.add(newmapping);
		}
	}

	/***
	 * Establishes a new connection to the data source.
	 * 
	 * @return
	 * @throws OBDAException
	 */
	protected Connection getSQLConnection() throws OBDAException{
		Connection conn;

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
	
	public QuestConnection getConnection() throws OBDAException {
	
		return new QuestConnection(this, getSQLConnection());
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
