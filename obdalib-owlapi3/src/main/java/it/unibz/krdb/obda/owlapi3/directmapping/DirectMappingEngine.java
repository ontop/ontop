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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DatabaseRelationDefinition;
import it.unibz.krdb.sql.JDBCConnectionManager;
import it.unibz.krdb.sql.TableDefinition;

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
	
	private JDBCConnectionManager conMan = null;
	private DBMetadata metadata = null;
	private String baseuri;
	private int mapidx = 1;
	
	private static OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	public DirectMappingEngine(String baseUri, int mapnr){
		conMan = JDBCConnectionManager.getJDBCConnectionManager();
		baseuri = baseUri;
		mapidx = mapnr + 1;
	}
	
	public DirectMappingEngine(DBMetadata metadata, String baseUri, int mapnr){
		this.metadata = metadata;
		baseuri = baseUri;
		mapidx = mapnr + 1;
	}
	
	
	/*
	 * set the base URI used in the ontology
	 */
	public void setBaseURI(String prefix){
		if(prefix.endsWith("#")){
			this.baseuri = prefix.replace("#", "/");
		}else if(prefix.endsWith("/")){
			this.baseuri = prefix;
		}else this.baseuri = prefix+"/";
	}
	
	
	
	/***
	 * enrich the ontology according to the datasources specified in the OBDAModel
	 * basically from the database structure
	 * 
	 * @param ontology
	 * @param model
	 * 
	 * @return null
	 * 		   the ontology is updated
	 * 
	 * @throws Exceptions
	 */
			
	public void enrichOntology(OWLOntology ontology, OBDAModel model) throws OWLOntologyStorageException, SQLException{
		List<OBDADataSource> sourcelist = new ArrayList<OBDADataSource>();
		sourcelist = model.getSources();
		OntoExpansion oe = new OntoExpansion();
		if(model.getPrefixManager().getDefaultPrefix().endsWith("/")){
			oe.setURI(model.getPrefixManager().getDefaultPrefix());
		}else{
			oe.setURI(model.getPrefixManager().getDefaultPrefix()+"/");
		}
		
		//For each data source, enrich into the ontology
		if (metadata == null) {
			for (int i = 0; i < sourcelist.size(); i++) {
				Connection conn = conMan.getConnection(sourcelist.get(i));		
				oe.enrichOntology(JDBCConnectionManager.getMetaData(conn, null), ontology);
			}
		} else
			oe.enrichOntology(this.metadata, ontology);
	}
	
	
	
	/***
	 * enrich the ontology according to mappings used in the model
	 * 
	 * @param manager
	 * @param model
	 * 
	 * @return a new ontology storing all classes and properties used in the mappings
	 * 
	 * @throws Exceptions
	 */
	public OWLOntology getOntology(OWLOntology ontology, OWLOntologyManager manager, OBDAModel model) throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException{

		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		//Add all the classes
		for (OClass c :  model.getOntologyVocabulary().getClasses()) {
			OWLClass newclass = dataFactory.getOWLClass(IRI.create(c.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(ontology, declarationAxiom);
		}
		
		//Add all the object properties
		for (ObjectPropertyExpression p : model.getOntologyVocabulary().getObjectProperties()){
			OWLObjectProperty newclass = dataFactory.getOWLObjectProperty(IRI.create(p.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(ontology, declarationAxiom);
		}
		
		//Add all the data properties
		for (DataPropertyExpression p : model.getOntologyVocabulary().getDataProperties()){
			OWLDataProperty newclass = dataFactory.getOWLDataProperty(IRI.create(p.getName()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
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
	 * @throws Exception 
	 */
	public OBDAModel extractMappings(OBDADataSource source) throws Exception{
		OBDAModelImpl model = new OBDAModelImpl();
		return extractMappings(model, source);
	}
	
	public OBDAModel extractMappings(OBDAModel model, OBDADataSource source) throws Exception{
		insertMapping(source, model);
		return model;
	}
	
	
	/***
	 * extract mappings from given datasource, and insert them into the given model
	 * 
	 * @param source
	 * @param model
	 * 
	 * @return null
	 * 
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 * @throws Exception 
	 */
	public void insertMapping(OBDADataSource source, OBDAModel model) throws Exception {		
		model.addSource(source);
		Connection conn = conMan.getConnection(source);		
		// this operation is EXPENSIVE
		DBMetadata metadata = JDBCConnectionManager.getMetaData(conn, null);
		insertMapping(metadata, model,source.getSourceID());
	}
	
	
	public void insertMapping(DBMetadata metadata, OBDAModel model, URI sourceUri) throws Exception{			
		if (baseuri == null || baseuri.isEmpty())
			this.baseuri = model.getPrefixManager().getDefaultPrefix();
		Collection<TableDefinition> tables = metadata.getTables();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<OBDAMappingAxiom>();
		for (TableDefinition td : tables) {
			model.addMappings(sourceUri, getMapping(td, metadata, baseuri));
		}
		model.addMappings(sourceUri, mappingAxioms);
		for (URI uri : model.getMappings().keySet()) {
			for (OBDAMappingAxiom mapping : model.getMappings().get(uri)) {
				CQIE rule = mapping.getTargetQuery();
				for (Function f : rule.getBody()) {
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
	 * @param table : the datadefinition from which mappings are extraced
	 * @param metadata : the metadata of the database required
	 * @param baseUri : the base uri needed for direct mapping axiom
	 * 
	 *  @return a List of OBDAMappingAxiom-s
	 * @throws Exception 
	 */
	public List<OBDAMappingAxiom> getMapping(DatabaseRelationDefinition table, DBMetadata metadata, String baseUri) throws Exception {
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

		DirectMappingAxiom dma = new DirectMappingAxiom(baseUri, table, metadata, dfac);

		List<OBDAMappingAxiom> axioms = new ArrayList<OBDAMappingAxiom>();
		axioms.add(dfac.getRDBMSMappingAxiom("MAPPING-ID"+mapidx,dma.getSQL(), dma.getCQ()));
		mapidx++;
		
		Map<String, CQIE> refAxioms = dma.getRefAxioms();
		for (String refSQL : refAxioms.keySet()) {
			axioms.add(dfac.getRDBMSMappingAxiom("MAPPING-ID"+mapidx, refSQL, refAxioms.get(refSQL)));
			mapidx++;
		}
		
		return axioms;
	}


}
