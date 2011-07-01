package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.DataQueryReasoner;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.MappingController;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxSerializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import it.unibz.krdb.obda.owlrefplatform.core.abox.AboxDumpException;
import it.unibz.krdb.obda.owlrefplatform.core.abox.AboxFromDBLoader;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.abox.DirectMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.MappingValidator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexMappingGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticReduction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.EvaluationEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCEngine;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.DLRPerfectReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryRewriter;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.QueryVocabularyValidator;
import it.unibz.krdb.obda.owlrefplatform.core.reformulation.TreeRedReformulator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.ComplexMappingSQLGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SimpleDirectQueryGenrator;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.ComplexMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.DirectMappingUnfolder;
import it.unibz.krdb.obda.owlrefplatform.core.unfolding.UnfoldingMechanism;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.inference.OWLReasoner;
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
 * 
 * 
 */

public class OBDAOWLReformulationPlatform implements OWLReasoner, DataQueryReasoner, MonitorableOWLReasoner {

	private static final String					NOT_IMPLEMENTED_STR		= "Service not available.";

	private OWLOntologyManager					ontoManager				= null;

	/* The merge and tranlsation of all loaded ontologies */
	private DLLiterOntology						translatedOntologyMerge	= null;

	private TechniqueWrapper					techwrapper				= null;
	private HashSet<OWLOntology>				loadedOntologies		= null;
	private ProgressMonitor						progressMonitor			= new NullProgressMonitor();

	private OBDAModel							obdaModel				= null;

	private Logger								log						= LoggerFactory.getLogger(OBDAOWLReformulationPlatform.class);

	private boolean								isClassified			= false;

	private ReformulationPlatformPreferences	preferences				= null;

	private QueryVocabularyValidator			validator				= null;

	protected OBDAOWLReformulationPlatform(OWLOntologyManager manager) {
		ontoManager = manager;
	}

	@Override
	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		obdaModel = model;
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

	public void setPreferences(ReformulationPlatformPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public Statement getStatement() throws Exception {
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

		// String useMem = (String)
		String reformulationTechnique = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
		boolean useInMemoryDB = preferences.getCurrentValue(ReformulationPlatformPreferences.DATA_LOCATION).equals("inmemory");
		String unfoldingMode = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.ABOX_MODE);
		String dbType = (String) preferences.getCurrentValue(ReformulationPlatformPreferences.DBTYPE);
		boolean createMappings = preferences.getCurrentValue(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS).equals("true");
		log.debug("Initializing Quest query answering engine...");
		log.debug("Active preferences:");
		log.debug("{} = {}", ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, reformulationTechnique);
		log.debug("{} = {}", ReformulationPlatformPreferences.DATA_LOCATION, useInMemoryDB);
		log.debug("{} = {}", ReformulationPlatformPreferences.ABOX_MODE, unfoldingMode);
		log.debug("{} = {}", ReformulationPlatformPreferences.DBTYPE, dbType);
		log.debug("{} = {}", ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, createMappings);

		QueryRewriter rewriter;
		UnfoldingMechanism unfMech = null;
		SourceQueryGenerator gen = null;
		DataSource ds;
		EvaluationEngine eval_engine;
		DAG dag = null;
		DAG pureIsa = null;
		QueryVocabularyValidator validator = null;

		try {
			/** Setup the validator */
			validator = new QueryVocabularyValidator(loadedOntologies);

			if (useInMemoryDB && (OBDAConstants.CLASSIC.equals(unfoldingMode) || createMappings)) {
				log.debug("Using in an memory database");
				String driver = "org.h2.Driver";
				String url = "jdbc:h2:mem:aboxdump";
				String username = "sa";
				String password = "";
				Connection connection;

				OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
				DataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP"));
				source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
				source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
				source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
				source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
				source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
				source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

				// apic.getDatasourcesController().addDataSource(source);
				// apic.getDatasourcesController().setCurrentDataSource(source.getSourceID());
				ds = source;
				connection = JDBCConnectionManager.getJDBCConnectionManager().getConnection(ds);
				if (dbType.equals(OBDAConstants.SEMANTIC)) {
					// perform semantic import
					dag = DAGConstructor.getISADAG(this.translatedOntologyMerge);
					pureIsa = DAGConstructor.filterPureISA(dag);
					pureIsa.index();
					// dag.index();
					ABoxSerializer.recreate_tables(connection);
					ABoxSerializer.ABOX2DB(loadedOntologies, dag, pureIsa, connection);
				} else if (dbType.equals(OBDAConstants.DIRECT)) {
					// perform direct import
					String[] types = { "TABLE" };

					ResultSet set = connection.getMetaData().getTables(null, null, "%", types);
					Vector<String> drops = new Vector<String>();
					while (set.next()) {
						String table = set.getString(3);
						drops.add("DROP TABLE " + table);
					}
					set.close();

					java.sql.Statement st = connection.createStatement();
					for (String drop_table : drops) {
						st.executeUpdate(drop_table);
					}
					ABoxToDBDumper dumper;
					try {
						dumper = new ABoxToDBDumper(source);
						dumper.materialize(loadedOntologies, false);
					} catch (AboxDumpException e) {
						throw new Exception(e);
					}
					if (createMappings) {
						DirectMappingGenerator mapGen = new DirectMappingGenerator();
						Set<OBDAMappingAxiom> mappings = mapGen.getMappings(loadedOntologies, dumper.getMapper());
						Iterator<OBDAMappingAxiom> it = mappings.iterator();
						MappingController mapCon = obdaModel.getMappingController();
						while (it.hasNext()) {
							mapCon.insertMapping(ds.getSourceID(), it.next());
						}
					}
				} else {
					throw new Exception(dbType
							+ " is unknown or not yet supported Data Base type. Currently only the direct db type is supported");
				}
				eval_engine = new JDBCEngine(connection);

			} else {
				log.debug("Using a persistent database");

				Collection<DataSource> sources = obdaModel.getDatasourcesController().getAllSources();
				if (sources == null || sources.size() == 0) {
					throw new Exception("No datasource has been defined");
				} else if (sources.size() > 1) {
					throw new Exception("Currently the reasoner can only handle one datasource");
				} else {
					ds = sources.iterator().next();
				}
				eval_engine = new JDBCEngine(ds);
			}
			List<Assertion> onto;
			if (dbType.equals(OBDAConstants.SEMANTIC)) {

				// Reachability DAGs
				SemanticReduction reducer = new SemanticReduction(dag, DAGConstructor.getSigma(this.translatedOntologyMerge));
				onto = reducer.reduce();
			} else {
				onto = this.translatedOntologyMerge.getAssertions();
			}

			if (OBDAConstants.PERFECTREFORMULATION.equals(reformulationTechnique)) {
				rewriter = new DLRPerfectReformulator(onto);
			} else if (OBDAConstants.UCQBASED.equals(reformulationTechnique)) {
				rewriter = new TreeRedReformulator(onto);
			} else {
				throw new IllegalArgumentException("Invalid value for argument: "
						+ ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE);
			}

			if (OBDAConstants.VIRTUAL.equals(unfoldingMode) || dbType.equals(OBDAConstants.SEMANTIC)) {
				if (dbType.equals(OBDAConstants.SEMANTIC)) {
					List<SemanticIndexMappingGenerator.MappingKey> simple_maps = SemanticIndexMappingGenerator.build(dag, pureIsa);
					for (OBDAMappingAxiom map : SemanticIndexMappingGenerator.compile(simple_maps)) {
						log.debug(map.toString());
						obdaModel.getMappingController().insertMapping(ds.getSourceID(), map);
					}
				}
				List<OBDAMappingAxiom> mappings = obdaModel.getMappingController().getMappings(ds.getSourceID());

				// Validate the mappings against the ontology
				MappingValidator mappingValidator = new MappingValidator(loadedOntologies);
				mappingValidator.validate(mappings);

				MappingViewManager viewMan = new MappingViewManager(mappings);
				unfMech = new ComplexMappingUnfolder(mappings, viewMan);
				JDBCUtility util = new JDBCUtility(ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
				gen = new ComplexMappingSQLGenerator(viewMan, util);
			} else if (OBDAConstants.CLASSIC.equals(unfoldingMode)) {
				unfMech = new DirectMappingUnfolder();
				AboxFromDBLoader loader = new AboxFromDBLoader();
				gen = new SimpleDirectQueryGenrator(loader.getMapper(ds));
			} else {
				log.error("Invalid parameter for {}", ReformulationPlatformPreferences.ABOX_MODE);
			}

			/***
			 * Done, sending a new reasoner with the modules we just configured
			 */

			this.techwrapper = new BolzanoTechniqueWrapper(unfMech, rewriter, gen, validator, eval_engine, obdaModel);
			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new OWLReasonerException(e.getMessage(), e) {
			};
		} finally {
			getProgressMonitor().setFinished();
		}
	}

	public void clearOntologies() throws OWLReasonerException {
		loadedOntologies.clear();
		translatedOntologyMerge = null;
		isClassified = false;
	}

	public void dispose() throws OWLReasonerException {
		techwrapper.dispose();
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
			translatedOntologyMerge = new DLLiterOntologyImpl(uri);
		}
		if (loadedOntologies == null) {
			loadedOntologies = new HashSet<OWLOntology>();
		}

		log.debug("Load ontologies called. Translating ontologies.");
		OWLAPITranslator translator = new OWLAPITranslator();
		Set<URI> uris = new HashSet<URI>();

		DLLiterOntology translation = new DLLiterOntologyImpl(uri);
		for (OWLOntology onto : ontologies) {
			uris.add(onto.getURI());
			DLLiterOntology aux;
			try {
				aux = translator.translate(onto);
			} catch (Exception e) {
				throw new OWLReasonerException("Error translating ontology: " + onto.toString(), e) {
				};
			}
			translation.addAssertions(aux.getAssertions());
			translation.addConcepts(new ArrayList<ConceptDescription>(aux.getConcepts()));
			translation.addRoles(new ArrayList<RoleDescription>(aux.getRoles()));
		}
		/* we translated successfully, now we append the new assertions */

		this.loadedOntologies.addAll(ontologies);
		translatedOntologyMerge.addAssertions(translation.getAssertions());
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

	public void setProgressMonitor(ProgressMonitor progressMonitor) {

		// this.progressMonitor = progressMonitor;
	}

	private ProgressMonitor getProgressMonitor() {
		if (progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return progressMonitor;
	}

	public void finishProgressMonitor() {

		getProgressMonitor().setFinished();
	}

	public void startProgressMonitor(String msg) {
		// getProgressMonitor().setMessage(msg);
		// getProgressMonitor().setIndeterminate(true);
		// getProgressMonitor().setStarted();

	}
}
