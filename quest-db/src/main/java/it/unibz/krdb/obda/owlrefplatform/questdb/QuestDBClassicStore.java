package it.unibz.krdb.obda.owlrefplatform.questdb;

/*
 * #%L
 * ontop-quest-db
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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Axiom;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.SubClassOfAxiom;
import it.unibz.krdb.obda.ontology.SubPropertyOfAxiom;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.EquivalentTriplePredicateIterator;
import it.unibz.krdb.obda.owlrefplatform.core.abox.QuestMaterializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.Dataset;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
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
 */
public class QuestDBClassicStore extends QuestDBAbstractStore {

	// TODO all this needs to be refactored later to allow for transactions,
	// autocommit enable/disable, clients ids, etc

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBClassicStore.class);

	protected transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private Set<OWLOntology> closure;

	public QuestDBClassicStore(String name, java.net.URI tboxFile) 	throws Exception {
		this(name, tboxFile, null);
	}

	public QuestDBClassicStore(String name, java.net.URI tboxFile, QuestPreferences config) throws Exception {
		super(name);
		Ontology tbox = readOntology(tboxFile.toASCIIString());
		setup(tbox, config);
	}
	
	public QuestDBClassicStore(String name, String tboxFile, QuestPreferences config) throws Exception {
		super(name);
		Ontology tbox = null;
		if (tboxFile == null) {
			tbox = ofac.createOntology(name);
		} else {
			tbox = readOntology(tboxFile);
		}
		setup(tbox, config);
	}
	
	private Ontology readOntology(String tboxFile) throws Exception {
		OWLAPI3Translator translator = new OWLAPI3Translator();
		File f = new File(tboxFile);
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(f.getParentFile(), false);
		man.addIRIMapper(iriMapper);
		
		OWLOntology owlontology = null;
		if (tboxFile.contains("file:")) {
			owlontology = man.loadOntologyFromOntologyDocument(new URL(tboxFile).openStream());
		} else {
			owlontology = man.loadOntologyFromOntologyDocument(new File(tboxFile));
		}
		closure = man.getImportsClosure(owlontology);
		return translator.mergeTranslateOntologies(closure);
	}

	public QuestDBClassicStore(String name, Dataset data, QuestPreferences config) throws Exception {
		super(name);
		Ontology tbox = getTBox(data);
		setup(tbox, config);
	}
	
	private void setup(Ontology onto, QuestPreferences config) throws Exception {
		if (config == null) {
			config = new QuestPreferences();
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			throw new Exception("A classic repository must be created with the CLASSIC flag in the configuration.");
		}
		createInstance(onto, config);
	}

	private void createInstance(Ontology tbox, QuestPreferences config) throws Exception {
		questInstance = new Quest(tbox,config);

		questInstance.setupRepository();
		
		final boolean bObtainFromOntology = config.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_ONTOLOGY);
		final boolean bObtainFromMappings = config.getCurrentBooleanValueFor(QuestPreferences.OBTAIN_FROM_MAPPINGS);
		QuestConnection conn = questInstance.getNonPoolConnection();
		QuestStatement st = conn.createStatement();
		if (bObtainFromOntology) {
			// Retrieves the ABox from the ontology file.
			log.debug("Loading data from Ontology into the database");
			OWLAPI3ABoxIterator aBoxIter = new OWLAPI3ABoxIterator(closure);
			EquivalentTriplePredicateIterator aBoxNormalIter = 
							new EquivalentTriplePredicateIterator(aBoxIter, questInstance.getReasoner());
			
			int count = st.insertData(aBoxNormalIter, 5000, 500);
			log.debug("Inserted {} triples from the ontology.", count);
		}
		if (bObtainFromMappings) {
			// Retrieves the ABox from the target database via mapping.
			log.debug("Loading data from Mappings into the database");
			OBDAModel obdaModelForMaterialization = questInstance.getOBDAModel();
			for (Predicate p : tbox.getConcepts()) {
				obdaModelForMaterialization.declarePredicate(p);
			}
			for (Predicate p : tbox.getRoles()) {
				obdaModelForMaterialization.declarePredicate(p);
			}
			QuestMaterializer materializer = new QuestMaterializer(obdaModelForMaterialization);
			Iterator<Assertion> assertionIter = materializer.getAssertionIterator();
			int count = st.insertData(assertionIter, 5000, 500);
			materializer.disconnect();
			log.debug("Inserted {} triples from the mappings.", count);
		}
//		st.createIndexes();
		st.close();
		if (!conn.getAutoCommit())
		conn.commit();
		
		questInstance.updateSemanticIndexMappings();

		log.debug("Store {} has been created successfully", name);
	}

	public void saveState(String storePath) throws IOException {
		// NO-OP
	}

	public QuestDBClassicStore restore(String storePath) throws IOException {	
		return this;
	}
	
	public QuestConnection getQuestConnection() {
		QuestConnection conn = null;
		try {
			conn = questInstance.getConnection();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return conn;
	}

	private Ontology getTBox(Dataset dataset) throws Exception {
		// Merge default and named graphs to filter duplicates
		Set<URI> graphURIs = new HashSet<URI>();
		graphURIs.addAll(dataset.getDefaultGraphs());
		graphURIs.addAll(dataset.getNamedGraphs());

		Ontology result = ofac.createOntology();

		for (URI graphURI : graphURIs) {
			Ontology o = getOntology(((URI) graphURI), graphURI);
			for (Predicate p : o.getConcepts())
				result.addConcept(p);
			for (Predicate p : o.getRoles())
				result.addRole(p);
			for (SubPropertyOfAxiom ax : result.getSubPropertyAxioms())  // TODO (ROMAN): check whether it's result and not o
				result.addAssertionWithCheck(ax);
			for (SubClassOfAxiom ax : result.getSubClassAxioms())  // TODO (ROMAN): check whether it's result and not o
				result.addAssertionWithCheck(ax);
		}
		return result;
	}

	private Ontology getOntology(URI graphURI, Resource context) throws Exception {
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURI.toString(), RDFFormat.TURTLE);
		RDFParser rdfParser = Rio.createParser(rdfFormat, ValueFactoryImpl.getInstance());
		ParserConfig config = rdfParser.getParserConfig();

		// To emulate DatatypeHandling.IGNORE 
		config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//		rdfParser.setVerifyData(false);
//		rdfParser.setDatatypeHandling(DatatypeHandling.IGNORE);
//		rdfParser.setPreserveBNodeIDs(true);

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

		public Ontology getOntology() {
			return ontology;
		}

		@Override
		public void startRDF() throws RDFHandlerException {
			ontology = ofac.createOntology();
		}

		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			Predicate pred = getVocabulary(st);
				if (pred.getArity() == 1) {
					ontology.addConcept(pred);
				} else {
					ontology.addRole(pred);
				}
		/*
		 * ROMAN: this code does nothing because	getTBoxAxiom is null
			Axiom axiom = getTBoxAxiom(st);
			if (axiom == null) {
				return;
			}
			ontology.addAssertionWithCheck(axiom);
		*/
		}

		public Axiom getTBoxAxiom(Statement st) {
			return null;
		}

		public Predicate getVocabulary(Statement st) {
			URI pred = st.getPredicate();
			Value obj = st.getObject();
			if (obj instanceof Literal) {
				Predicate dataProperty = fac.getDataPropertyPredicate(pred.stringValue());
				return dataProperty;
			} else if (pred.stringValue().equals(OBDAVocabulary.RDF_TYPE)) {
				Predicate className = fac.getClassPredicate(obj.stringValue());
				return className;
			}
			Predicate objectProperty = fac.getObjectPropertyPredicate(pred.stringValue());
			return objectProperty;
		}
	}
}
