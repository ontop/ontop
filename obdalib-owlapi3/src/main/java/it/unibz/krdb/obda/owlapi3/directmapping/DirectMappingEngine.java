package it.unibz.krdb.obda.owlapi3.directmapping;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DBMetadataExtractor;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.DatabaseRelationDefinition;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


/***
 * 
 * A class that provides manipulation for Direct Mapping
 * 
 * @author Victor
 *
 */
public class DirectMappingEngine {
	
	private JDBCConnectionManager connManager = null;
    private String baseIRI;
	private int currentMappingIndex = 1;
	
	public DirectMappingEngine(String baseIRI, int numOfExisitingMappings){
		connManager = JDBCConnectionManager.getJDBCConnectionManager();
		this.baseIRI = baseIRI;
		currentMappingIndex = numOfExisitingMappings + 1;
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
	public OWLOntology getOntology(OWLOntology ontology, OWLOntologyManager manager, OBDAModel model) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException{

		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		//Add all the classes
		for (OClass c :  model.getOntologyVocabulary().getClasses()) {
			OWLClass owlClass = dataFactory.getOWLClass(IRI.create(c.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(owlClass);
			manager.addAxiom(ontology, declarationAxiom);
		}
		
		//Add all the object properties
		for (ObjectPropertyExpression p : model.getOntologyVocabulary().getObjectProperties()){
			OWLObjectProperty property = dataFactory.getOWLObjectProperty(IRI.create(p.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(property);
			manager.addAxiom(ontology, declarationAxiom);
		}
		
		//Add all the data properties
		for (DataPropertyExpression p : model.getOntologyVocabulary().getDataProperties()){
			OWLDataProperty property = dataFactory.getOWLDataProperty(IRI.create(p.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(property);
			manager.addAxiom(ontology, declarationAxiom);
		}
				
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
		OBDAModel model = new OBDAModelImpl();
		return extractMappings(model, source);
	}

	public OBDAModel extractMappings(OBDAModel model, OBDADataSource source) throws DuplicateMappingException, SQLException {
		bootstrapMappings(source, model);
		return model;
	}


	/***
	 * extract mappings from given datasource, and insert them into the given model
	 *
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 */
	public void bootstrapMappings(OBDADataSource source, OBDAModel model) throws SQLException, DuplicateMappingException {
		model.addSource(source);
		Connection conn = connManager.getConnection(source);
		DBMetadata metadata = DBMetadataExtractor.createMetadata(conn);
		// this operation is EXPENSIVE
		DBMetadataExtractor.loadMetadata(metadata, conn, null);
		bootstrapMappings(metadata, model,source.getSourceID());
	}


	public void bootstrapMappings(DBMetadata metadata, OBDAModel model, URI sourceUri) throws DuplicateMappingException {
		if (baseIRI == null || baseIRI.isEmpty())
			this.baseIRI = model.getPrefixManager().getDefaultPrefix();
		Collection<DatabaseRelationDefinition> tables = metadata.getDatabaseRelations();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<>();
		for (DatabaseRelationDefinition td : tables) {
			model.addMappings(sourceUri, getMapping(td, baseIRI));
		}
		model.addMappings(sourceUri, mappingAxioms);
		for (URI uri : model.getMappings().keySet()) {
			for (OBDAMappingAxiom mapping : model.getMappings().get(uri)) {
				List<Function> rule = mapping.getTargetQuery();
				for (Function f : rule) {
					if (f.getArity() == 1)
						model.getOntologyVocabulary().createClass(f.getFunctionSymbol().getName());
					else if (f.getFunctionSymbol().getType(1).equals(COL_TYPE.OBJECT))
						model.getOntologyVocabulary().createObjectProperty(f.getFunctionSymbol().getName());
					else
						model.getOntologyVocabulary().createDataProperty(f.getFunctionSymbol().getName());
				}
			}
		}
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
		axioms.add(dfac.getRDBMSMappingAxiom("MAPPING-ID"+ currentMappingIndex, dfac.getSQLQuery(dmap.getSQL(table)), dmap.getCQ(table)));
		currentMappingIndex++;
		
		Map<String, List<Function>> refAxioms = dmap.getRefAxioms(table);
		for (Map.Entry<String, List<Function>> e : refAxioms.entrySet()) {
            OBDASQLQuery sqlQuery = dfac.getSQLQuery(e.getKey());
            List<Function> targetQuery = e.getValue();
            axioms.add(dfac.getRDBMSMappingAxiom("MAPPING-ID"+ currentMappingIndex, sqlQuery, targetQuery));
			currentMappingIndex++;
		}
		
		return axioms;
	}


}
