package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQueryReasoner;
import it.unibz.krdb.obda.ontology.Ontology;
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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 */
public class QuestOWL extends StructuralReasoner implements OBDAOWLReasoner, OBDAQueryReasoner {

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology translatedOntologyMerge = null;

	private HashSet<OWLOntology> loadedOntologies = null;

	private OBDAModel obdaModel = null;

	private Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private QuestPreferences preferences = null;

	OWLAPI3VocabularyExtractor vext = new OWLAPI3VocabularyExtractor();

	private Quest questInstance = null;

	private QuestConnection conn = null;

	/***
	 * Optimization flags
	 */

	// private boolean optimizeEquivalences = true;

	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences) {

		super(rootOntology, configuration, bufferingMode);
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

//		String reformulationTechnique = (String) preferences.getCurrentValue(QuestPreferences.REFORMULATION_TECHNIQUE);
//		boolean bOptimizeEquivalences = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_EQUIVALENCES);
//		boolean bOptimizeTBoxSigma = preferences.getCurrentBooleanValueFor(QuestPreferences.OPTIMIZE_TBOX_SIGMA);
		boolean bObtainFromOntology = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		boolean bObtainFromMappings = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		String unfoldingMode = (String) preferences.getCurrentValue(QuestPreferences.ABOX_MODE);
		//		String dbType = (String) preferences.getCurrentValue(QuestPreferences.DBTYPE);

		getProgressMonitor().reasonerTaskStarted("Initializing");
		// getProgressMonitor().setMessage("Classifying...");
		// getProgressMonitor().setStarted();

		questInstance.loadTBox(translatedOntologyMerge);
		questInstance.loadOBDAModel(obdaModel);

		if (preferences == null) {
			throw new NullPointerException("ReformulationPlatformPreferences not set");
		}

		log.debug("Initializing Quest query answering engine...");

		questInstance.setPreferences(preferences);

		try {

			getProgressMonitor().reasonerTaskProgressChanged(1, 4);

			questInstance.setupRepository();
			getProgressMonitor().reasonerTaskProgressChanged(2, 4);
			conn = questInstance.getConnection();
			getProgressMonitor().reasonerTaskProgressChanged(3, 4);
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
			// isClassified = true;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			getProgressMonitor().reasonerTaskProgressChanged(4, 4);
			getProgressMonitor().reasonerTaskStopped();
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

	}

	private ReasonerProgressMonitor getProgressMonitor() {
		ReasonerProgressMonitor progressMonitor = getReasonerConfiguration().getProgressMonitor();
		return progressMonitor;
	}

	@Override
	public void loadOBDAModel(OBDAModel model) {

		obdaModel = (OBDAModel) model.clone();
	}

	@Override
	public QuestConnection getConnection() throws OBDAException {
		return conn;
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
	public void interrupt() {
		super.interrupt();

	}

	@Override
	protected void handleChanges(Set<OWLAxiom> addAxioms, Set<OWLAxiom> removeAxioms) {
		super.handleChanges(addAxioms, removeAxioms);
		questInstance.dispose();
		questInstance = new Quest();
		Set<OWLOntology> closure = getRootOntology().getOWLOntologyManager().getImportsClosure(getRootOntology());
		loadedOntologies = new HashSet<OWLOntology>();
		loadedOntologies.addAll(closure);
		try {
			loadOntologies(closure);
			classify();
		} catch (Exception e) {
			throw new OWLRuntimeException(e);
		}

	}

}
