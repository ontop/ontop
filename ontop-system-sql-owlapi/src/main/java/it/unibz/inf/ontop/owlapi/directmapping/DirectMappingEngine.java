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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.ontology.*;
import it.unibz.inf.ontop.ontology.utils.MappingVocabularyExtractor;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

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

	private static final SQLMappingFactory SQL_MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
	private final SpecificationFactory specificationFactory;
	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	private final OBDAFactoryWithException obdaFactory;
	private final OntopSQLCoreSettings settings;
	private final JDBCConnectionManager connManager;

    private String baseIRI;
	private int currentMappingIndex = 1;

	/**
	 * Entry point.
	 *
	 */
	public static BootstrappingResults bootstrap(OntopSQLOWLAPIConfiguration configuration, String baseIRI)
			throws MappingIOException, InvalidMappingException, OWLOntologyCreationException, SQLException, OWLOntologyStorageException, DuplicateMappingException {
		DirectMappingEngine engine = configuration.getInjector().getInstance(DirectMappingEngine.class);
		return engine.bootstrapMappingAndOntology(baseIRI, configuration.loadPPMapping(),
				configuration.loadInputOntology());
	}

	@Inject
	private DirectMappingEngine(OntopSQLCoreSettings settings, SpecificationFactory specificationFactory,
                                NativeQueryLanguageComponentFactory nativeQLFactory,
                                OBDAFactoryWithException obdaFactory) {
		connManager = JDBCConnectionManager.getJDBCConnectionManager();
		this.specificationFactory = specificationFactory;
		this.nativeQLFactory = nativeQLFactory;
		this.obdaFactory = obdaFactory;
		this.settings = settings;
	}

	/**
	 * NOT THREAD-SAFE (not reentrant)
	 */
	private BootstrappingResults bootstrapMappingAndOntology(String baseIRI, Optional<OBDAModel> inputObdaModel,
															 Optional<OWLOntology> inputOntology)
			throws SQLException, OWLOntologyCreationException, OWLOntologyStorageException, DuplicateMappingException {

		setBaseURI(baseIRI);

		OBDAModel newMapping = inputObdaModel.isPresent()
				? extractMappings(inputObdaModel.get())
				: extractMappings();

		ImmutableOntologyVocabulary newVocabulary = MappingVocabularyExtractor.extractVocabulary(
				newMapping.getMappings().stream()
						.flatMap(ax -> ax.getTargetQuery().stream()));

		OWLOntology newOntology = inputOntology.isPresent()
				? updateOntology(inputOntology.get(), newVocabulary)
				: updateOntology(createEmptyOntology(baseIRI), newVocabulary);

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
	private OWLOntology updateOntology(OWLOntology ontology, ImmutableOntologyVocabulary vocabulary)
			throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();

		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		Set<OWLDeclarationAxiom> declarationAxioms = new HashSet<>();

		//Add all the classes
		for (OClass c :  vocabulary.getClasses()) {
			OWLClass owlClass = dataFactory.getOWLClass(IRI.create(c.getName()));
			declarationAxioms.add(dataFactory.getOWLDeclarationAxiom(owlClass));
		}
		
		//Add all the object properties
		for (ObjectPropertyExpression p : vocabulary.getObjectProperties()){
			OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(p.getName()));
			declarationAxioms.add(dataFactory.getOWLDeclarationAxiom(property));
		}
		
		//Add all the data properties
		for (DataPropertyExpression p : vocabulary.getDataProperties()){
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
		it.unibz.inf.ontop.io.PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
		MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager, UriTemplateMatcher.create(Stream.empty()));
		OBDAModel emptyModel = obdaFactory.createOBDAModel(ImmutableList.of(), mappingMetadata);
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
			this.baseIRI = model.getMetadata().getPrefixManager().getDefaultPrefix();
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<>();
		for (DatabaseRelationDefinition td : tables) {
			mappingAxioms.addAll(getMapping(td, baseIRI));
		}

		List<OBDAMappingAxiom> mappings = new ArrayList<>();
		mappings.addAll(model.getMappings());
		mappings.addAll(mappingAxioms);

		return model.newModel(ImmutableList.copyOf(mappings), model.getMetadata());
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
		axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, SQL_MAPPING_FACTORY.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table)));
		currentMappingIndex++;
		
		Map<String, List<Function>> refAxioms = dmap.getRefAxioms(table);
		for (Map.Entry<String, List<Function>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = SQL_MAPPING_FACTORY.getSQLQuery(e.getKey());
            List<Function> targetQuery = e.getValue();
            axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
			currentMappingIndex++;
		}
		
		return axioms;
	}
}
