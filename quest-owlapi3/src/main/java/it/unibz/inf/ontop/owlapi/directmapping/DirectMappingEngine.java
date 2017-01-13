package it.unibz.inf.ontop.owlapi.directmapping;

/*
 * #%L
 * ontop-obdalib-owlapi
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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.injection.OntopSQLSettings;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.ontology.OClass;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyVocabularyImpl;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


/***
 * 
 * A class that provides manipulation for Direct Mapping
 * 
 * @author Victor
 *
 */
public class DirectMappingEngine {

	public static class BootstrappingResults {
		private final OBDAModel mapping;
		private final OWLOntology ontology;

		public BootstrappingResults(OBDAModel mapping, OWLOntology ontology) {
			this.mapping = mapping;
			this.ontology = ontology;
		}

		public OBDAModel getMapping() {
			return mapping;
		}

		public OWLOntology getOntology() {
			return ontology;
		}
	}

	private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	private final OBDAFactoryWithException obdaFactory;
	private final OntopSQLSettings settings;
	private final JDBCConnectionManager connManager;

    private String baseIRI;
	private int currentMappingIndex = 1;

	/**
	 * Entry point.
	 *
	 */
	public static BootstrappingResults bootstrap(QuestConfiguration configuration, String baseIRI)
			throws InvalidDataSourceException, IOException, InvalidMappingException, OWLOntologyCreationException, SQLException, OWLOntologyStorageException {
		DirectMappingEngine engine = configuration.getInjector().getInstance(DirectMappingEngine.class);
		return engine.bootstrapMappingAndOntology(baseIRI, configuration.loadMapping(),
				configuration.loadInputOntology());
	}

	@Inject
	private DirectMappingEngine(OntopSQLSettings settings,
								NativeQueryLanguageComponentFactory nativeQLFactory,
								OBDAFactoryWithException obdaFactory) {
		connManager = JDBCConnectionManager.getJDBCConnectionManager();
		this.nativeQLFactory = nativeQLFactory;
		this.obdaFactory = obdaFactory;
		this.settings = settings;
	}

	/**
	 * NOT THREAD-SAFE (not reentrant)
	 */
	private BootstrappingResults bootstrapMappingAndOntology(String baseIRI, Optional<OBDAModel> inputObdaModel,
															 Optional<OWLOntology> inputOntology)
			throws SQLException, OWLOntologyCreationException, OWLOntologyStorageException {

		setBaseURI(baseIRI);

		OBDAModel newMapping = inputObdaModel.isPresent()
				? extractMappings(inputObdaModel.get())
				: extractMappings();

		OWLOntology newOntology = inputOntology.isPresent()
				? updateOntology(inputOntology.get(), newMapping)
				: updateOntology(createEmptyOntology(baseIRI), newMapping);

		return new BootstrappingResults(newMapping, newOntology);
	}

	private static OWLOntology createEmptyOntology(String baseIRI) throws OWLOntologyCreationException {
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		return mng.createOntology(IRI.create(baseIRI));
	}


	/*
     * set the base URI used in the ontology
     */
    private void setBaseURI(String prefix) {
        if (prefix.endsWith("#")) {
            this.baseIRI = prefix.replace("#", "/");
        } else if (prefix.endsWith("/")) {
            this.baseIRI = prefix;
        } else this.baseIRI = prefix + "/";
    }


    /***
	 * enrich the ontology according to mappings used in the model
	 * 
	 * @return a new ontology storing all classes and properties used in the mappings
	 *
	 */
	private OWLOntology updateOntology(OWLOntology ontology, OBDAModel model)
			throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();

		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		Set<OWLDeclarationAxiom> declarationAxioms = new HashSet<>();

		//Add all the classes
		for (OClass c :  model.getOntologyVocabulary().getClasses()) {
			OWLClass owlClass = dataFactory.getOWLClass(IRI.create(c.getName()));
			declarationAxioms.add(dataFactory.getOWLDeclarationAxiom(owlClass));
		}
		
		//Add all the object properties
		for (ObjectPropertyExpression p : model.getOntologyVocabulary().getObjectProperties()){
			OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(p.getName()));
			declarationAxioms.add(dataFactory.getOWLDeclarationAxiom(property));
		}
		
		//Add all the data properties
		for (DataPropertyExpression p : model.getOntologyVocabulary().getDataProperties()){
			OWLDataProperty property = dataFactory.getOWLDataProperty(IRI.create(p.getName()));
			declarationAxioms.add(dataFactory.getOWLDeclarationAxiom(property));
		}

		manager.addAxioms(ontology, declarationAxioms);

		return ontology;		
	}


	/***
	 * extract all the mappings from a datasource
	 *
	 * @return a new OBDA Model containing all the extracted mappings
	 */
	private OBDAModel extractMappings() throws DuplicateMappingException, SQLException {
		it.unibz.inf.ontop.io.PrefixManager prefixManager = nativeQLFactory.create(new HashMap<>());
		OBDAModel emptyModel = obdaFactory.createOBDAModel(ImmutableList.of(), prefixManager,
				new OntologyVocabularyImpl());
		return extractMappings(emptyModel);
	}

	private OBDAModel extractMappings(OBDAModel model)
			throws DuplicateMappingException, SQLException {
		currentMappingIndex = model.getMappings().size() + 1;
		return bootstrapMappings(model);
	}


	/***
	 * extract mappings from given datasource, and insert them into the given model
	 *
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 */
	private OBDAModel bootstrapMappings(OBDAModel model)
			throws SQLException, DuplicateMappingException {
		if (model == null) {
			throw new IllegalArgumentException("Model should not be null");
		}
		Connection conn = connManager.getConnection(settings);
		RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(conn);
		// this operation is EXPENSIVE
		RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
		return bootstrapMappings(metadata, model);
	}


	private OBDAModel bootstrapMappings(RDBMetadata metadata, OBDAModel model) throws DuplicateMappingException {
		if (baseIRI == null || baseIRI.isEmpty())
			this.baseIRI = model.getPrefixManager().getDefaultPrefix();
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<>();
		for (DatabaseRelationDefinition td : tables) {
			mappingAxioms.addAll(getMapping(td, baseIRI));
		}

		List<OBDAMappingAxiom> mappings = new ArrayList<>();
		mappings.addAll(model.getMappings());
		mappings.addAll(mappingAxioms);

		OntologyVocabulary ontologyVocabulary = model.getOntologyVocabulary();

		for (OBDAMappingAxiom mapping : model.getMappings()) {
			List<Function> rule = mapping.getTargetQuery();
			for (Function f : rule) {
				if (f.getArity() == 1)
					ontologyVocabulary.createClass(f.getFunctionSymbol().getName());
				else if (f.getFunctionSymbol().getType(1).equals(COL_TYPE.OBJECT))
					ontologyVocabulary.createObjectProperty(f.getFunctionSymbol().getName());
				else
					ontologyVocabulary.createDataProperty(f.getFunctionSymbol().getName());
			}
		}
		return model.newModel(ImmutableList.copyOf(mappings), model.getPrefixManager(), ontologyVocabulary);
	}


	/***
	 * generate a mapping axiom from a table of a database
	 * 
	 * @param table : the data definition from which mappings are extraced
	 * @param baseUri : the base uri needed for direct mapping axiom
	 * 
	 *  @return a List of OBDAMappingAxiom-s
	 */
	private List<OBDAMappingAxiom> getMapping(DatabaseRelationDefinition table, String baseUri) {

		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseUri, DATA_FACTORY);

		List<OBDAMappingAxiom> axioms = new ArrayList<>();
		axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, MAPPING_FACTORY.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table)));
		currentMappingIndex++;
		
		Map<String, List<Function>> refAxioms = dmap.getRefAxioms(table);
		for (Map.Entry<String, List<Function>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = MAPPING_FACTORY.getSQLQuery(e.getKey());
            List<Function> targetQuery = e.getValue();
            axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
			currentMappingIndex++;
		}
		
		return axioms;
	}
}
