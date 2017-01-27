package it.unibz.inf.ontop.owlrefplatform.questdb;

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

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.RDFHandlerBase;

import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An instance of Store that encapsulates all the functionality needed for a
 * "classic" store.
 */
public class QuestDBClassicStore {

	// TODO all this needs to be refactored later to allow for transactions,
	// autocommit enable/disable, clients ids, etc

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBClassicStore.class);

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	private Set<OWLOntology> closure;

	/**
	 * TODO: integrate Ontology loading from a Dataset into QuestConfiguration
     */
	public QuestDBClassicStore(String name, Dataset data, QuestConfiguration config) throws Exception {
		Ontology tbox = loadTBoxFromDataset(data);
		throw new RuntimeException("TODO: remove the QuestDBClassicStore");
		//createInstance(tbox, config);
	}

//	private void createInstance(Ontology tbox, QuestConfiguration configuration) throws Exception {
//		QuestSettings preferences = configuration.getSettings();
//        questInstance = getComponentFactory().create(tbox, Optional.empty(), Optional.empty(), getExecutorRegistry());
//		questInstance.setupRepository();
//
//		final boolean bObtainFromOntology = preferences.getRequiredBoolean(QuestCoreSettings.OBTAIN_FROM_ONTOLOGY);
//		final boolean bObtainFromMappings = preferences.getRequiredBoolean(QuestCoreSettings.OBTAIN_FROM_MAPPINGS);
//		IQuestConnection conn = questInstance.getNonPoolConnection();
//        //TODO: avoid this cast
//		SIQuestStatement st = conn.createSIStatement();
//		if (bObtainFromOntology) {
//			// Retrieves the ABox from the ontology file.
//			log.debug("Loading data from Ontology into the database");
//			OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(closure, questInstance.getVocabulary());
//			int count = st.insertData(aBoxIter, 5000, 500);
//			log.debug("Inserted {} triples from the ontology.", count);
//		}
//		if (bObtainFromMappings) {
//			// Retrieves the ABox from the target database via mapping.
//			log.debug("Loading data from Mappings into the database");
//			OBDAModel obdaModelForMaterialization = questInstance.getOBDAModel();
//			//obdaModelForMaterialization.getOntologyVocabulary().merge(tbox.getVocabulary());
//
//			/**
//			 * TODO: check if tbox is the expected vocabulary
//			 * TODO: why is it using the QuestMaterializer, not the OWLAPIMaterializer
//			 */
//			QuestMaterializer materializer = new QuestMaterializer(configuration, tbox, false);
//			Iterator<Assertion> assertionIter = materializer.getAssertionIterator();
//			int count = st.insertData(assertionIter, 5000, 500);
//			materializer.disconnect();
//			log.debug("Inserted {} triples from the mappings.", count);
//		}
////		st.createIndexes();
//		st.close();
//		if (!conn.getAutoCommit())
//			conn.commit();
//
//		//questInstance.updateSemanticIndexMappings();
//
//		log.debug("Store {} has been created successfully", name);
//	}

	public IQuestConnection getQuestConnection() {
		throw new RuntimeException("TODO: remove the QuestDBClassicStore");
	}

	private Ontology loadTBoxFromDataset(Dataset dataset) throws Exception {
		// Merge default and named graphs to filter duplicates
		Set<URI> graphURIs = new HashSet<>();
		graphURIs.addAll(dataset.getDefaultGraphs());
		graphURIs.addAll(dataset.getNamedGraphs());

		OntologyVocabulary vb = ofac.createVocabulary();

		for (URI graphURI : graphURIs) {
			Ontology o = getOntology(graphURI, graphURI);
			vb.merge(o.getVocabulary());
			
			// TODO: restore copying ontology axioms (it was copying from result into result, at least since July 2013)
			
			//for (SubPropertyOfAxiom ax : result.getSubPropertyAxioms()) 
			//	result.add(ax);
			//for (SubClassOfAxiom ax : result.getSubClassAxioms()) 
			//	result.add(ax);	
		}
		Ontology result = ofac.createOntology(vb);

		// TODO: what about closure?
		closure = null;

		return result;
	}

	private Ontology getOntology(URI graphURI, Resource context) throws Exception {
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURI.toString()).get();
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
		private OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		private OntologyVocabulary vb = ofac.createVocabulary();

		public Ontology getOntology() {
			return ofac.createOntology(vb);
		}

		@Override
		public void handleStatement(Statement st) throws RDFHandlerException {
			URI pred = st.getPredicate();
			Value obj = st.getObject();
			if (obj instanceof Literal) {
				String dataProperty = pred.stringValue();
				vb.createDataProperty(dataProperty);
			} 
			else if (pred.stringValue().equals(OBDAVocabulary.RDF_TYPE)) {
				String className = obj.stringValue();
				vb.createClass(className);
			} 
			else {
				String objectProperty = pred.stringValue();
				vb.createObjectProperty(objectProperty);
			}

		/* Roman 10/08/15: recover?
			Axiom axiom = getTBoxAxiom(st);
			ontology.addAssertionWithCheck(axiom);
		*/
		}

	}

}
