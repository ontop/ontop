package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An instance of Store that encapsulates all the functionality needed for a
 * "classic" store.
 * 
 * @author mariano
 * 
 */
public class QuestDBClassicStore extends QuestDBAbstractStore {

	// TODO all this needs to be refactored later to allow for transactions,
	// autocommit enable/disable, clients ids, etc

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory
			.getLogger(QuestDBClassicStore.class);

	protected transient OWLOntologyManager man = OWLManager
			.createOWLOntologyManager();

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public QuestDBClassicStore(String name, java.net.URI tboxFile)
			throws Exception {
		this(name, tboxFile, null);

	}

	public QuestDBClassicStore(String name, java.net.URI tboxFile,
			QuestPreferences config) throws Exception {

		super(name);
		if (config == null) {
			config = new QuestPreferences();
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(
				QuestConstants.CLASSIC))
			throw new Exception(
					"A classic repository must be created with the CLASSIC flag in the configuration.");

		OWLAPI3Translator translator = new OWLAPI3Translator();
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(tboxFile).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		OWLOntology owlontology = man
				.loadOntologyFromOntologyDocument(new File(tboxFile));
		Set<OWLOntology> clousure = man.getImportsClosure(owlontology);
		
		Ontology tbox = translator.mergeTranslateOntologies(clousure);

		createInstance(tbox, config);

	}

	public QuestDBClassicStore(String name, Dataset data,
			QuestPreferences config) throws Exception {

		super(name);
		if (config == null) {
			config = new QuestPreferences();
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(
				QuestConstants.CLASSIC))
			throw new Exception(
					"A classic repository must be created with the CLASSIC flag in the configuration.");
		Ontology tbox = getTBox(data);

		createInstance(tbox, config);

	}

	private void createInstance(Ontology tbox, Properties config)
			throws Exception {
		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.setupRepository();

		log.debug("Store {} has been created successfully", name);
	}
	
	

	public void saveState(String storePath) throws IOException {
		//StringBuffer filename = new StringBuffer();
	//	filename.append(storePath);
		//ObjectOutput out = new ObjectOutputStream(new FileOutputStream(filename.toString()));
	//	out.writeObject(store);
	//	out.close();

	}

	public QuestDBClassicStore restore(String storePath) throws IOException {
		//File file = new File(storePath);
		//ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		//QuestDBAbstractStore store = (QuestDBAbstractStore) in.readObject();
		//in.close();
		
		return this;
	}
	
	public QuestConnection getQuestConnection() {
		if (questConn == null) {
			try {
				questConn = questInstance.getConnection();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
		}
		return questConn;
	}

	private Ontology getTBox(Dataset dataset) throws Exception {
		// Merge default and named graphs to filter duplicates
		Set<URI> graphURIs = new HashSet<URI>();
		graphURIs.addAll(dataset.getDefaultGraphs());
		graphURIs.addAll(dataset.getNamedGraphs());

		Ontology result = ofac.createOntology();

		for (URI graphURI : graphURIs) {
			Ontology o = getOntology(((URI) graphURI), graphURI);
			result.addEntities(o.getVocabulary());
			result.addAssertions(result.getAssertions());
		}

		return result;

	}

	private Ontology getOntology(URI graphURI, Resource context)
			throws Exception {
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(
				graphURI.toString(), RDFFormat.TURTLE);
		RDFParser rdfParser = Rio.createParser(rdfFormat,
				ValueFactoryImpl.getInstance());
		rdfParser.setVerifyData(false);
		rdfParser.setDatatypeHandling(DatatypeHandling.IGNORE);
		rdfParser.setPreserveBNodeIDs(true);

		RDFTBoxReader reader = new RDFTBoxReader();
		rdfParser.setRDFHandler(reader);

		URL graphURL = new URL(graphURI.toString());
		InputStream in = graphURL.openStream();
		try {
			rdfParser.parse(in, graphURI.toString());
		} finally {
			in.close();
		}

		return reader.getOntology();

	}

	public class RDFTBoxReader extends RDFHandlerBase {

		private Ontology ontology = null;

		private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		public RDFTBoxReader() {

		}

		public Ontology getOntology() {
			return ontology;
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			ontology = ofac.createOntology();
		}

		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			ontology.addEntity(getVocabulary(st));
			Axiom axiom = getTBoxAxiom(st);
			if (axiom == null)
				return;
			ontology.addAssertion(axiom);
		}

		public Axiom getTBoxAxiom(Statement st) {
			return null;
		}

		public Predicate getVocabulary(Statement st) {
			URI pred = st.getPredicate();
			Value obj = st.getObject();
			if (obj instanceof Literal) {
				Predicate dataProperty = fac.getDataPropertyPredicate(pred
						.stringValue());
				return dataProperty;
			} else if (pred.stringValue().equals(OBDAVocabulary.RDF_TYPE)) {
				Predicate className = fac.getClassPredicate(obj.stringValue());
				return className;
			}
			Predicate objectProperty = fac.getObjectPropertyPredicate(pred
					.stringValue());
			return objectProperty;

		}

	}

}
