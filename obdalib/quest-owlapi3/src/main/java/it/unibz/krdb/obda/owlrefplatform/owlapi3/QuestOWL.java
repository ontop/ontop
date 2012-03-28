package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OBDAOWLReasoner;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3VocabularyExtractor;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLQueryReasoner;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer;
import it.unibz.krdb.obda.owlrefplatform.core.abox.VirtualABoxMaterializer.VirtualTriplePredicateIterator;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
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
public class QuestOWL extends StructuralReasoner implements OBDAOWLReasoner, OWLQueryReasoner {

	/* The merge and tranlsation of all loaded ontologies */
	private Ontology translatedOntologyMerge;

	private OBDAModel obdaModel;

	private HashSet<OWLOntology> loadedOntologies = new HashSet<OWLOntology>();

	private QuestPreferences preferences = new QuestPreferences();

	private Quest questInstance = new Quest();

	private static Logger log = LoggerFactory.getLogger(QuestOWL.class);

	private QuestConnection conn = null;

	private QuestOWLConnection owlconn = null;

	/***
	 * Default constructor.
	 */
	public QuestOWL(OWLOntology rootOntology, OBDAModel obdaModel, OWLReasonerConfiguration configuration, BufferingMode bufferingMode,
			Properties preferences) {
		super(rootOntology, configuration, bufferingMode);
		this.obdaModel = obdaModel;
		this.preferences.putAll(preferences);

		Set<OWLOntology> closure = rootOntology.getOWLOntologyManager().getImportsClosure(rootOntology);
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
	public OWLStatement getStatement() throws OWLException {
		return owlconn.createStatement();
	}

	private void classify() throws Exception {

		log.debug("Initializing Quest query answering engine...");

		final boolean bObtainFromOntology = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		final boolean bObtainFromMappings = preferences.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		final String unfoldingMode = (String) preferences.getCurrentValue(QuestPreferences.ABOX_MODE);

		getProgressMonitor().reasonerTaskStarted("Initializing");

		// Loading the OBDA Model and TBox ontology to Quest
		questInstance.loadTBox(translatedOntologyMerge);
		questInstance.loadOBDAModel(obdaModel);

		// Load the preferences
		log.debug("Initializing Quest query answering engine...");
		questInstance.setPreferences(preferences);

		try {
			getProgressMonitor().reasonerTaskProgressChanged(1, 4);

			// Setup repository
			questInstance.setupRepository();
			getProgressMonitor().reasonerTaskProgressChanged(2, 4);

			// Retrives the connection from Quest
			conn = questInstance.getConnection();
			owlconn = new QuestOWLConnection(conn);
			getProgressMonitor().reasonerTaskProgressChanged(3, 4);

			// Preparing the data source
			if (unfoldingMode.equals(QuestConstants.CLASSIC)) {
				QuestStatement st = conn.createStatement();
				if (bObtainFromOntology) {
					// Retrieves the ABox from the ontology file.
					log.debug("Loading data from Ontology into the database");
					OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(loadedOntologies, questInstance.getEquivalenceMap());
					st.insertData(aBoxIter, 5000, 500);
				}
				if (bObtainFromMappings) {
					// Retrieves the ABox from the target database via mapping.
					log.debug("Loading data from Mappings into the database");

					VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(questInstance.getOBDAModel());
					VirtualTriplePredicateIterator assertionIter = (VirtualTriplePredicateIterator) materializer.getAssertionIterator();
					st.insertData(assertionIter, 5000, 500);
					assertionIter.disconnect();
				}
				st.createIndexes();
				st.close();
				conn.commit();
			} else {
				// VIRTUAL MODE - NO-OP
			}
			log.debug("Quest has completed the setup and it is ready for query answering!");
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
		 * We will keep track of the loaded ontologies and translate the TBox
		 * part of them into our internal representation.
		 */
		log.debug("Load ontologies called. Translating ontologies.");

		OWLAPI3Translator translator = new OWLAPI3Translator();

		try {
			translatedOntologyMerge = translator.mergeTranslateOntologies(ontologies);
		} catch (Exception e) {
			throw e;
		}

		if (loadedOntologies == null) {
			loadedOntologies = new HashSet<OWLOntology>();
		}
		loadedOntologies.addAll(ontologies);

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
	public OWLConnection getConnection() throws OBDAException {
		return owlconn;
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
