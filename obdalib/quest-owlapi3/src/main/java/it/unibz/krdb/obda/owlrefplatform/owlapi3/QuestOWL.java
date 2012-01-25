package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQueryReasoner;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3VocabularyExtractor;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer.VirtualTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.MappingVocabularyRepair;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.util.NullProgressMonitor;
import org.semanticweb.owlapi.util.ProgressMonitor;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL extends org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase implements OBDAOWLReasoner, OBDAQueryReasoner {

	private static final String NOT_IMPLEMENTED_STR = "Service not available.";

	private OWLOntologyManager ontoManager = null;

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology translatedOntologyMerge = null;

	private HashSet<OWLOntology> loadedOntologies = null;

	private ProgressMonitor progressMonitor = new NullProgressMonitor();

	private OBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private boolean isClassified = false;

	private QuestPreferences preferences = null;

	OWLAPI3VocabularyExtractor vext = new OWLAPI3VocabularyExtractor();

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private Quest questInstance = null;

	private QuestConnection conn = null;

	/***
	 * Optimization flags
	 */

	// private boolean optimizeEquivalences = true;

	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingModem,
			Properties preferences) {

		super(rootOntology, new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
		this.preferences = new QuestPreferences();
		this.preferences.putAll(preferences);

		this.obdaModel = obdaModel;
		questInstance = new Quest();
		Set<OWLOntology> closure = rootOntology.getOWLOntologyManager().getImportsClosure(rootOntology);
		loadedOntologies = new HashSet<OWLOntology>();
		loadedOntologies.addAll(closure);
		try {
			loadOntologies(closure);
			classify();
		} catch (Exception e) {
			throw new OWLRuntimeException(e);
		}

	}

	public void setPreferences(QuestPreferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public QuestStatement getStatement() throws OBDAException {
		return conn.createStatement();
	}

	private void classify() throws Exception {

		log.debug("Initializing Quest query answering engine...");

		String reformulationTechnique = (String) preferences.getCurrentValue(QuestPreferences.REFORMULATION_TECHNIQUE);
		boolean bOptimizeEquivalences = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_EQUIVALENCES);
		boolean bOptimizeTBoxSigma = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_TBOX_SIGMA);
		boolean bObtainFromOntology = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		boolean bObtainFromMappings = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		String unfoldingMode = (String) preferences.getCurrentValue(QuestPreferences.ABOX_MODE);
		String dbType = (String) preferences.getCurrentValue(QuestPreferences.DBTYPE);

		getProgressMonitor().setIndeterminate(true);
		getProgressMonitor().setMessage("Classifying...");
		getProgressMonitor().setStarted();

		/***
		 * Fixing the typing of predicates, in case they are not properly given.
		 */

		log.debug("Fixing vocabulary typing");

		MappingVocabularyRepair repairmodel = new MappingVocabularyRepair();
		repairmodel.fixOBDAModel(obdaModel, this.translatedOntologyMerge.getVocabulary());

		questInstance.loadTBox(translatedOntologyMerge);
		questInstance.loadOBDAModel(obdaModel);

		if (preferences == null) {
			throw new NullPointerException("ReformulationPlatformPreferences not set");
		}

		log.debug("Initializing Quest query answering engine...");

		questInstance.setPreferences(preferences);

		try {

			questInstance.setupRepository();
			conn = questInstance.getConnection();

			/*
			 * Preparing the data source
			 */

			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {

				QuestStatement st = conn.createStatement();
				if (bObtainFromOntology) {
					log.debug("Loading data from Ontology into the database");
					OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(loadedOntologies, questInstance.getEquivalenceMap());

					st.insertData(aBoxIter, 5000, 500);

				}
				if (bObtainFromMappings) {
					log.debug("Loading data from Mappings into the database");

					VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(obdaModel, questInstance.getEquivalenceMap());
					VirtualTriplePredicateIterator assertionIter = (VirtualTriplePredicateIterator) materializer.getAssertionIterator();
					st.insertData(assertionIter, 5000, 500);
					assertionIter.disconnect();
				}

				st.createIndexes();
				st.close();
				conn.commit();

			}

			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			getProgressMonitor().setFinished();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			conn.close();
			questInstance.dispose();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
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
	private void loadOntologies(Set<OWLOntology> ontologies) throws Exception {
		/*
		 * We will keep track of the loaded ontologies and tranlsate the TBox
		 * part of them into our internal represntation
		 */
		log.debug("Load ontologies called. Translating ontologies.");

		OWLAPI3Translator translator = new OWLAPI3Translator();

		try {
			translatedOntologyMerge = translator.mergeTranslateOntologies(ontologies);
		} catch (Exception e) {
			throw new Exception(e) {

				/**
				 * 
				 */
				private static final long serialVersionUID = -745370766306607220L;
			};
		}

		if (loadedOntologies == null) {
			loadedOntologies = new HashSet<OWLOntology>();
		}
		this.loadedOntologies.addAll(ontologies);

		log.debug("Ontology loaded: {}", translatedOntologyMerge);

		isClassified = false;
	}

	// @Override
	// public void realise() throws OWLReasonerException {
	// classify();
	// }

	// public void unloadOntologies(Set<OWLOntology> ontologies) throws
	// OWLReasonerException {
	// boolean result = loadedOntologies.removeAll(ontologies);
	// // if no ontologies where removed
	// if (!result)
	// return;
	//
	// // otherwise clear everything and update
	// Set<OWLOntology> resultSet = new HashSet<OWLOntology>();
	// resultSet.addAll(loadedOntologies);
	// clearOntologies();
	// loadOntologies(resultSet);
	// }

	/* The following methods need revision */

	// @Override
	// public void setProgressMonitor(ProgressMonitor progressMonitor) {
	// this.progressMonitor = progressMonitor;
	// }
	//
	private ProgressMonitor getProgressMonitor() {
		if (progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return progressMonitor;
	}

	// @Override
	// public void clearOntologies() throws OWLReasonerException {
	// if (loadedOntologies != null) {
	// loadedOntologies.clear();
	// }
	// translatedOntologyMerge = null;
	// isClassified = false;
	// }

	@Override
	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		obdaModel = (OBDAModel) model.clone();
	}

	@Override
	public QuestConnection getConnection() throws OBDAException {
		return conn;
	}

	@Override
	public Node<OWLClass> getBottomClassNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0, boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual arg0, OWLDataProperty arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return new OWLNamedIndividualNodeSet();
	}

	@Override
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression arg0) throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression arg0) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0, boolean arg1) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return new OWLNamedIndividualNodeSet();
	}

	@Override
	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression arg0)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression arg0, boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression arg0, boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		return new OWLNamedIndividualNodeSet();
	}

	@Override
	public Set<InferenceType> getPrecomputableInferenceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReasonerName() {
		return "Quest";
	}

	@Override
	public Version getReasonerVersion() {
		return new Version(1, 7, 0, 0);
	}

	@Override
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression arg0, boolean arg1) throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException, ClassExpressionNotInProfileException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0, boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0, boolean arg1) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty arg0, boolean arg1) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression arg0, boolean arg1)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getTopClassNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual arg0, boolean arg1) throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailed(OWLAxiom arg0) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> arg0) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrecomputed(InferenceType arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression arg0) throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void precomputeInferences(InferenceType... arg0) throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleChanges(Set<OWLAxiom> arg0, Set<OWLAxiom> arg1) {
		// TODO Auto-generated method stub

	}

}
