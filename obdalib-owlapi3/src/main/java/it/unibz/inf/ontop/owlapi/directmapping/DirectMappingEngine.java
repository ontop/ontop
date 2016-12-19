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
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.ontology.OClass;
import it.unibz.inf.ontop.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.ontology.impl.OntologyVocabularyImpl;
import it.unibz.inf.ontop.sql.RDBMetadata;
import it.unibz.inf.ontop.sql.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;
import org.semanticweb.owlapi.model.*;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


/***
 * 
 * A class that provides manipulation for Direct Mapping
 * 
 * @author Victor
 *
 */
public class DirectMappingEngine {

	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	private final OBDAFactoryWithException obdaFactory;
	private JDBCConnectionManager connManager = null;
    private String baseIRI;
	private int currentMappingIndex = 1;
	
	public DirectMappingEngine(String baseIRI, int numOfExisitingMappings,
							   NativeQueryLanguageComponentFactory nativeQLFactory,
							   OBDAFactoryWithException obdaFactory){
		connManager = JDBCConnectionManager.getJDBCConnectionManager();
		this.baseIRI = baseIRI;
		currentMappingIndex = numOfExisitingMappings + 1;
		this.nativeQLFactory = nativeQLFactory;
		this.obdaFactory = obdaFactory;
	}


    /*
     * set the base URI used in the ontology
     */
    public void setBaseURI(String prefix) {
        if (prefix.endsWith("#")) {
            this.baseIRI = prefix.replace("#", "/");
        } else if (prefix.endsWith("/")) {
            this.baseIRI = prefix;
        } else this.baseIRI = prefix + "/";
    }


    /***
	 * enrich the ontology according to mappings used in the model
	 * 
	 * @param manager
	 * @param model
	 * 
	 * @return a new ontology storing all classes and properties used in the mappings
	 *
	 */
	public OWLOntology getOntology(OWLOntology ontology, OWLOntologyManager manager, OBDAModel model) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException {

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
	 * @param source
	 *
	 * @return a new OBDA Model containing all the extracted mappings
	 */
	public OBDAModel extractMappings(OBDADataSource source) throws DuplicateMappingException, SQLException {
		//TODO: avoid this empty construction
		it.unibz.inf.ontop.io.PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
		OBDAModel emptyModel = obdaFactory.createOBDAModel(new HashSet<OBDADataSource>(),
				new HashMap<URI, ImmutableList<OBDAMappingAxiom>>(),prefixManager,
				new OntologyVocabularyImpl());
		return extractMappings(emptyModel, source);
	}

	public OBDAModel extractMappings(OBDAModel model, OBDADataSource source) throws DuplicateMappingException, SQLException {
		return bootstrapMappings(source, model);
	}


	/***
	 * extract mappings from given datasource, and insert them into the given model
	 *
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 */
	public OBDAModel bootstrapMappings(OBDADataSource source, OBDAModel model) throws SQLException, DuplicateMappingException {
		if (model == null) {
			throw new IllegalArgumentException("Model should not be null");
		}

		Set<OBDADataSource> dataSources = new HashSet<>(model.getSources());
		dataSources.add(source);

		OBDAModel updatedModel = model.newModel(dataSources, model.getMappings());

		Connection conn = connManager.getConnection(source);
		RDBMetadata metadata = RDBMetadataExtractionTools.createMetadata(conn);
		// this operation is EXPENSIVE
		RDBMetadataExtractionTools.loadMetadata(metadata, conn, null);
		return bootstrapMappings(metadata, updatedModel,source.getSourceID());
	}


	public OBDAModel bootstrapMappings(RDBMetadata metadata, OBDAModel model, URI sourceUri) throws DuplicateMappingException {
		if (baseIRI == null || baseIRI.isEmpty())
			this.baseIRI = model.getPrefixManager().getDefaultPrefix();
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<>();
		for (DatabaseRelationDefinition td : tables) {
			mappingAxioms.addAll(getMapping(td, baseIRI));
		}

		Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
		mappingIndex.putAll(model.getMappings());
		mappingIndex.put(sourceUri, ImmutableList.copyOf(mappingAxioms));

		OntologyVocabulary ontologyVocabulary = model.getOntologyVocabulary();

		for (URI uri : model.getMappings().keySet()) {
			for (OBDAMappingAxiom mapping : model.getMappings().get(uri)) {
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
		}
		return model.newModel(model.getSources(), mappingIndex, model.getPrefixManager(), ontologyVocabulary);
	}


	/***
	 * generate a mapping axiom from a table of a database
	 * 
	 * @param table : the data definition from which mappings are extraced
	 * @param baseUri : the base uri needed for direct mapping axiom
	 * 
	 *  @return a List of OBDAMappingAxiom-s
	 */
	public List<OBDAMappingAxiom> getMapping(DatabaseRelationDefinition table, String baseUri) {
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		DirectMappingAxiomProducer dmap = new DirectMappingAxiomProducer(baseUri, dfac);

		List<OBDAMappingAxiom> axioms = new ArrayList<>();
		axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, dfac.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table)));
		currentMappingIndex++;
		
		Map<String, List<Function>> refAxioms = dmap.getRefAxioms(table);
		for (Map.Entry<String, List<Function>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = dfac.getSQLQuery(e.getKey());
            List<Function> targetQuery = e.getValue();
            axioms.add(nativeQLFactory.create("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
			currentMappingIndex++;
		}
		
		return axioms;
	}


}
