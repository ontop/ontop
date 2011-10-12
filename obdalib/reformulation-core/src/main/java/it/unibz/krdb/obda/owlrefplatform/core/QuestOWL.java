package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQueryReasoner;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDirectDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.MappingVocabularyTranslator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Axiom;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.EquivalenceTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.tboxprocessing.SigmaTBoxOptimizer;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2VocabularyExtractor;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.NullProgressMonitor;
import org.semanticweb.owl.util.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL implements OBDAOWLReasoner, OBDAQueryReasoner, MonitorableOWLReasoner {

	private static final String NOT_IMPLEMENTED_STR = "Service not available.";

	private OWLOntologyManager ontoManager = null;

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology translatedOntologyMerge = null;

	private TechniqueWrapper techwrapper = null;
	private HashSet<OWLOntology> loadedOntologies = null;
	private ProgressMonitor progressMonitor = new NullProgressMonitor();

	private OBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private boolean isClassified = false;

	private ReformulationPlatformPreferences preferences = null;

	private QueryVocabularyValidator validator = null;

	// private Ontology aboxDependencies = null;

	private Ontology reducedOntology = null;

	OWLAPI2VocabularyExtractor vext = new OWLAPI2VocabularyExtractor();

	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	/***
	 * Optimization flags
	 */

	// private boolean optimizeEquivalences = true;

	private boolean optimizeSigma = false;

	public QuestOWL(OWLOntologyManager manager) {
		ontoManager = manager;
	}

	public Ontology getReducedOntology() {
		return reducedOntology;
	}

	public Ontology getABoxDependencies() {
		return null;
	}

	@Override
	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		obdaModel = model;
	}

	public void loadDependencies(Ontology sigma) {
		techwrapper.loadDependencies(sigma);
	}

	/**
	 * Set the technique wrapper which specifies which rewriting, unfolding and
	 * evaluation techniques are used.
	 * 
	 * @param newTechnique
	 *            the technique wrapper
	 */
	public void setTechniqueWrapper(TechniqueWrapper newTechnique) {
		techwrapper = newTechnique;
	}

	public TechniqueWrapper getTechniqueWrapper() {
		return techwrapper;
	}

	public void setPreferences(ReformulationPlatformPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public OBDAStatement getStatement() throws Exception {
		if (techwrapper != null && isClassified == true) {
			return techwrapper.getStatement();
		} else {
			throw new Exception(
					"Error, the technique wrapper has not been setup up yet. Make sure you have loaded the OWL Ontologies and the OBDA model, and classified before calling this method.");
		}
	}

	public boolean isConsistent(OWLOntology ontology) throws OWLReasonerException {
		return true;
	}

	public Ontology getOntology() {
		return this.translatedOntologyMerge;
	}

	public void classify() throws OWLReasonerException {

		getProgressMonitor().setIndeterminate(true);
		getProgressMonitor().setMessage("Classifying...");
		getProgressMonitor().setStarted();

		if (obdaModel == null) {
			throw new NullPointerException("APIController not set");
		}
		if (preferences == null) {
			throw new NullPointerException("ReformulationPlatformPreferences not set");
		}
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		/***
		 * Duplicating the OBDA model to avoid strange behavior.
		 */
		String reformulationTechnique = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
		boolean bOptimizeEquivalences = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES);
		boolean bOptimizeTBoxSigma = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES);
		// boolean bUseInMemoryDB = preferences.getCurrentValue(
		// ReformulationPlatformPreferences.DATA_LOCATION).equals(
		// QuestConstants.INMEMORY);
		boolean bObtainFromOntology = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY);
		boolean bObtainFromMappings = preferences.getCurrentBooleanValueFor(ReformulationPlatformPreferences.OBTAIN_FROM_MAPPINGS);
		String unfoldingMode = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.ABOX_MODE);
		String dbType = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.DBTYPE);

		// For testing purposes.
		// boolean createMappings = preferences
		// .getCurrentBooleanValueFor(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS);

		log.debug("Initializing Quest query answering engine...");
		log.debug("Active preferences:");

		for (Object key : preferences.keySet()) {
			log.debug("{} = {}", key, preferences.get(key));
		}

		//
		// log.debug("{} = {}",
		// ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE,
		// reformulationTechnique);
		// log.debug("{} = {}",
		// ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES,
		// bOptimizeEquivalences);
		// log.debug("{} = {}", ReformulationPlatformPreferences.DATA_LOCATION,
		// bUseInMemoryDB);
		// log.debug("{} = {}",
		// ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY,
		// bObtainFromOntology);
		// log.debug("{} = {}",
		// ReformulationPlatformPreferences.OBTAIN_FROM_MAPPINGS,
		// bObtainFromMappings);
		// log.debug("{} = {}", ReformulationPlatformPreferences.ABOX_MODE,
		// unfoldingMode);
		// log.debug("{} = {}", ReformulationPlatformPreferences.DBTYPE,
		// dbType);
		// log.debug("{} = {}",
		// ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS,
		// createMappings);

		QueryRewriter rewriter = null;
		UnfoldingMechanism unfMech = null;
		SourceQueryGenerator gen = null;
		EvaluationEngine eval_engine;

		Ontology sigma = ofac.createOntology(URI.create("sigmaontology"));
		Ontology reformulationOntology = null;
		OBDAModel unfoldingOBDAModel = fac.getOBDAModel();
		Map<Predicate, Description> equivalenceMaps = null;

		/*
		 * PART 0: Simplifying the vocabulary of the ontology
		 */

		if (bOptimizeEquivalences) {
			log.debug("Equivalence optimization. Input ontology: {}", translatedOntologyMerge.toString());
			EquivalenceTBoxOptimizer equiOptimizer = new EquivalenceTBoxOptimizer(translatedOntologyMerge);
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
			reformulationOntology = translatedOntologyMerge;
			equivalenceMaps = new HashMap<Predicate, Description>();
		}

		try {

			/*
			 * Preparing the data source
			 */

			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {

				log.debug("Working in classic mode");

				// if (bUseInMemoryDB || createMappings) {

				log.debug("Using in an memory database");
				String driver = "org.h2.Driver";
				String url = "jdbc:h2:mem:aboxdump" + System.currentTimeMillis();
				String username = "sa";
				String password = "";

				OBDADataSource newsource = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
				newsource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
				newsource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
				newsource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
				newsource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
				newsource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
				newsource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

				// this.translatedOntologyMerge.saturate();

				RDBMSDataRepositoryManager dataRepository;

				// VocabularyExtractor extractor = new
				// VocabularyExtractor();
				// Set<Predicate> vocabulary =
				// extractor.getVocabulary(reformulationOntology);
				if (dbType.equals(QuestConstants.SEMANTIC)) {
					dataRepository = new RDBMSSIRepositoryManager(newsource, reformulationOntology.getVocabulary());

				} else if (dbType.equals(QuestConstants.DIRECT)) {
					dataRepository = new RDBMSDirectDataRepositoryManager(newsource, reformulationOntology.getVocabulary());

				} else {
					throw new Exception(dbType
							+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}
				dataRepository.setTBox(reformulationOntology);

				/* Creating the ABox repository */

				getProgressMonitor().setMessage("Creating database schema...");

				dataRepository.createDBSchema(true);
				dataRepository.insertMetadata();

				if (bObtainFromOntology) {
					log.debug("Loading data from Ontology into the database");
					OWLAPI2ABoxIterator aBoxIter = new OWLAPI2ABoxIterator(loadedOntologies, equivalenceMaps);
					dataRepository.insertData(aBoxIter);
				}
				if (bObtainFromMappings) {
					log.debug("Loading data from Mappings into the database");
					VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(obdaModel);
					Iterator<Assertion> assertionIter = materializer.getAssertionIterator();
					dataRepository.insertData(assertionIter);
				}

				dataRepository.createIndexes();

				/* Setting up the OBDA model */

				unfoldingOBDAModel.addSource(newsource);
				unfoldingOBDAModel.addMappings(newsource.getSourceID(), dataRepository.getMappings());

				for (Axiom axiom : dataRepository.getABoxDependencies().getAssertions()) {
					sigma.addEntities(axiom.getReferencedEntities());
					sigma.addAssertion(axiom);
				}
				// }
			} else if (unfoldingMode.equals(QuestConstants.VIRTUAL)) {

				log.debug("Working in virtual mode");

				Collection<OBDADataSource> sources = this.obdaModel.getSources();
				if (sources == null || sources.size() == 0) {
					throw new Exception("No datasource has been defined");
				} else if (sources.size() > 1) {
					throw new Exception("Currently the reasoner can only handle one datasource");
				} else {

					/* Setting up the OBDA model */

					OBDADataSource ds = sources.iterator().next();
					unfoldingOBDAModel.addSource(ds);

					/*
					 * Processing mappings with respect to the vocabulary
					 * simplification
					 */

					MappingVocabularyTranslator mtrans = new MappingVocabularyTranslator();
					Collection<OBDAMappingAxiom> newMappings = mtrans.translateMappings(this.obdaModel.getMappings(ds.getSourceID()),
							equivalenceMaps);

					unfoldingOBDAModel.addMappings(ds.getSourceID(), newMappings);
				}
			}

			/*
			 * Setting up the unfolder and SQL generation
			 */

			OBDADataSource datasource = unfoldingOBDAModel.getSources().get(0);

			// MappingValidator mappingValidator = new
			// MappingValidator(loadedOntologies);
			// boolean validmappings =
			// mappingValidator.validate(unfoldingOBDAModel.getMappings(datasource.getSourceID()));

			MappingViewManager viewMan = new MappingViewManager(unfoldingOBDAModel.getMappings(datasource.getSourceID()));
			unfMech = new ComplexMappingUnfolder(unfoldingOBDAModel.getMappings(datasource.getSourceID()), viewMan);

			JDBCUtility util = new JDBCUtility(datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			gen = new ComplexMappingSQLGenerator(viewMan, util);

			log.debug("Setting up the connection;");
			eval_engine = new JDBCEngine(unfoldingOBDAModel.getSources().get(0));

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

			QueryVocabularyValidator validator = new QueryVocabularyValidator(reformulationOntology, equivalenceMaps);

			this.techwrapper = new QuestTechniqueWrapper(unfMech, rewriter, gen, validator, eval_engine, unfoldingOBDAModel);
			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			OWLReasonerException ex = new OWLReasonerException(e.getMessage(), e) {
			};
			e.fillInStackTrace();
			throw ex;
		} finally {
			getProgressMonitor().setFinished();
		}
	}

	public void clearOntologies() throws OWLReasonerException {
		if (loadedOntologies != null) {
			loadedOntologies.clear();
		}
		translatedOntologyMerge = null;
		isClassified = false;
	}

	public void dispose() throws OWLReasonerException {
		// TODO fix this!
		try {
			techwrapper.dispose();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		try {
			disconnect();
		} catch (Exception e) {
			log.debug(e.getMessage());

		}
	}

	public Set<OWLOntology> getLoadedOntologies() {
		return loadedOntologies;
	}

	public boolean isClassified() throws OWLReasonerException {
		return isClassified;
	}

	public boolean isDefined(OWLClass cls) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLObjectProperty prop) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLDataProperty prop) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLIndividual ind) throws OWLReasonerException {
		return true;
	}

	public boolean isRealised() throws OWLReasonerException {
		return isClassified;
	}

	/***
	 * This method loads the given ontologies in the system. This will merge
	 * these new ontologies with the existing ones in a set. Then it will
	 * translate the assertions in all the ontologies into a single one, in our
	 * internal representation.
	 * 
	 * The translation is done using our OWLAPITranslator that gets the TBox
	 * part of the ontologies and filters all the DL-Lite axioms (RDFS/OWL2QL
	 * and DL-Lite).
	 * 
	 * The original ontologies and the merged/translated ontology are kept and
	 * are used later when classify() is called.
	 * 
	 */
	public void loadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		/*
		 * We will keep track of the loaded ontologies and tranlsate the TBox
		 * part of them into our internal represntation
		 */
		URI uri = URI.create("http://it.unibz.krdb.obda/Quest/auxiliaryontology");
		if (translatedOntologyMerge == null) {
			translatedOntologyMerge = ofac.createOntology(uri);
		}
		if (loadedOntologies == null) {
			loadedOntologies = new HashSet<OWLOntology>();
		}

		log.debug("Load ontologies called. Translating ontologies.");
		OWLAPI2Translator translator = new OWLAPI2Translator();
		Set<URI> uris = new HashSet<URI>();

		Ontology translation = ofac.createOntology(uri);
		for (OWLOntology onto : ontologies) {
			uris.add(onto.getURI());
			Ontology aux;
			try {
				aux = translator.translate(onto);
			} catch (Exception e) {
				throw new OWLReasonerException("Error translating ontology: " + onto.toString(), e) {
				};
			}
			translation.addConcepts(aux.getConcepts());
			translation.addRoles(aux.getRoles());
			translation.addAssertions(aux.getAssertions());
		}
		/* we translated successfully, now we append the new assertions */

		this.loadedOntologies.addAll(ontologies);
		translatedOntologyMerge = translation;

		// translatedOntologyMerge.addAssertions(translation.getAssertions());
		// translatedOntologyMerge.addConcepts(new
		// ArrayList<ClassDescription>(translation.getConcepts()));
		// translatedOntologyMerge.addRoles(new
		// ArrayList<Property>(translation.getRoles()));
		// translatedOntologyMerge.saturate();

		log.debug("Ontology loaded: {}", translatedOntologyMerge);

		isClassified = false;
	}

	public void realise() throws OWLReasonerException {
		classify();
	}

	public void unloadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		boolean result = loadedOntologies.removeAll(ontologies);
		// if no ontologies where removed
		if (!result)
			return;

		// otherwise clear everything and update
		Set<OWLOntology> resultSet = new HashSet<OWLOntology>();
		resultSet.addAll(loadedOntologies);
		clearOntologies();
		loadOntologies(resultSet);
	}

	public Set<Set<OWLClass>> getAncestorClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<Set<OWLClass>> getDescendantClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<OWLClass> getEquivalentClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLClass>();
	}

	public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLClass>();
	}

	public Set<Set<OWLClass>> getSubClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<Set<OWLClass>> getSuperClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public boolean isEquivalentClass(OWLDescription clsC, OWLDescription clsD) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public boolean isSubClassOf(OWLDescription clsC, OWLDescription clsD) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public boolean isSatisfiable(OWLDescription description) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public Map<OWLDataProperty, Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) throws OWLReasonerException {
		// TODO implement owl
		return new HashMap<OWLDataProperty, Set<OWLConstant>>();
	}

	public Set<OWLIndividual> getIndividuals(OWLDescription clsC, boolean direct) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLIndividual>();
	}

	public Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) throws OWLReasonerException {
		// TODO implement owl
		return new HashMap<OWLObjectProperty, Set<OWLIndividual>>();
	}

	public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject, OWLObjectPropertyExpression property)
			throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLIndividual>();
	}

	public Set<OWLConstant> getRelatedValues(OWLIndividual subject, OWLDataPropertyExpression property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLConstant>();
	}

	public Set<Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();

	}

	public boolean hasDataPropertyRelationship(OWLIndividual subject, OWLDataPropertyExpression property, OWLConstant object)
			throws OWLReasonerException {
		// TODO implement
		return false;
	}

	public boolean hasObjectPropertyRelationship(OWLIndividual subject, OWLObjectPropertyExpression property, OWLIndividual object)
			throws OWLReasonerException {
		// TODO implement
		return false;
	}

	public boolean hasType(OWLIndividual individual, OWLDescription type, boolean direct) throws OWLReasonerException {
		// TODO implement
		return false;
	}

	public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();

	}

	public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDescription>>();
	}

	public Set<Set<OWLDescription>> getDomains(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDescription>>();

	}

	public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLObjectProperty>();

	}

	public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDataProperty>();
	}

	public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();
	}

	public Set<OWLDescription> getRanges(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDescription>();
	}

	public Set<OWLDataRange> getRanges(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDataRange>();
	}

	public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();
	}

	public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public boolean isAntiSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return false;
	}

	public boolean isFunctional(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isFunctional(OWLDataProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isInverseFunctional(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isIrreflexive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isReflexive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isTransitive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public OWLEntity getCurrentEntity() {
		return null;
		// return ontoManager.getOWLDataFactory().getOWLThing();
	}

	/* The following methods need revision */

	@Override
	public void setProgressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	private ProgressMonitor getProgressMonitor() {
		if (progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return progressMonitor;
	}

	@Override
	public void finishProgressMonitor() {
		getProgressMonitor().setFinished();
	}

	@Override
	public void startProgressMonitor(String msg) {
		getProgressMonitor().setMessage(msg);
		getProgressMonitor().setIndeterminate(true);
		getProgressMonitor().setStarted();
	}

	public void disconnect() throws SQLException {
		JDBCConnectionManager.getJDBCConnectionManager().closeConnections();
	}
}
