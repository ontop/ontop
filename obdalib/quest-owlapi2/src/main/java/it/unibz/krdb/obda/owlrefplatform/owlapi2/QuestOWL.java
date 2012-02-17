package it.unibz.krdb.obda.owlrefplatform.owlapi2;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQueryReasoner;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi2.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi2.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlapi2.OWLAPI2Translator;
import it.unibz.krdb.obda.owlapi2.OWLAPI2VocabularyExtractor;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer.VirtualTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.MappingVocabularyRepair;

import java.util.HashMap;
import java.util.HashSet;
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

	private HashSet<OWLOntology> loadedOntologies = null;

	private ProgressMonitor progressMonitor = new NullProgressMonitor();

	private OBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private boolean isClassified = false;

	private QuestPreferences preferences = null;

	OWLAPI2VocabularyExtractor vext = new OWLAPI2VocabularyExtractor();

	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private Quest questInstance = null;

	private QuestConnection conn = null;

	/***
	 * Optimization flags
	 */

	// private boolean optimizeEquivalences = true;

	public QuestOWL(OWLOntologyManager manager) {
		ontoManager = manager;
		questInstance = new Quest();
	}

	public void setPreferences(QuestPreferences preferences) {
		this.preferences = new QuestPreferences();
		this.preferences.putAll(preferences);
	}

	@Override
	public QuestStatement getStatement() throws OBDAException {
		return conn.createStatement();
	}

	public boolean isConsistent(OWLOntology ontology) throws OWLReasonerException {
		return true;
	}

	public void classify() throws OWLReasonerException {

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
					OWLAPI2ABoxIterator aBoxIter = new OWLAPI2ABoxIterator(loadedOntologies, questInstance.getEquivalenceMap());

					st.insertData(aBoxIter, 5000, 500);

				}
				if (bObtainFromMappings) {
					log.debug("Loading data from Mappings into the database");

					VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(obdaModel);
					VirtualTriplePredicateIterator assertionIter = (VirtualTriplePredicateIterator) materializer.getAssertionIterator();
					st.insertData(assertionIter, 5000, 500);
					materializer.disconnect();
				}

				st.createIndexes();
				st.close();
				conn.commit();

			}

			log.debug("... Quest has been setup and is ready for querying");
			isClassified = true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			OWLReasonerException ex = new OWLReasonerException(e.getMessage(), e) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 8546901292849186314L;
			};
			e.fillInStackTrace();
			throw ex;
		} finally {
			getProgressMonitor().setFinished();
		}
	}

	public void dispose() throws OWLReasonerException {
		// TODO fix this!
		try {
			conn.close();
			questInstance.dispose();
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
		log.debug("Load ontologies called. Translating ontologies.");

		OWLAPI2Translator translator = new OWLAPI2Translator();

		try {
			translatedOntologyMerge = translator.mergeTranslateOntologies(ontologies);
		} catch (Exception e) {
			throw new OWLReasonerException(e) {

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

	// @Override
	// public void finishProgressMonitor() {
	// getProgressMonitor().setFinished();
	// }
	//
	// @Override
	// public void startProgressMonitor(String msg) {
	// getProgressMonitor().setMessage(msg);
	// getProgressMonitor().setIndeterminate(true);
	// getProgressMonitor().setStarted();
	// }

	@Override
	public void clearOntologies() throws OWLReasonerException {
		if (loadedOntologies != null) {
			loadedOntologies.clear();
		}
		translatedOntologyMerge = null;
		isClassified = false;
	}

	@Override
	public void loadOBDAModel(OBDAModel model) {
		isClassified = false;
		obdaModel = (OBDAModel) model.clone();
	}

	@Override
	public QuestConnection getConnection() throws OBDAException {
		return conn;
	}

}
