package org.semanticweb.ontop.owlapi3.directmapping;

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

import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDAFactoryWithException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.DataDefinition;
import org.semanticweb.ontop.sql.JDBCConnectionManager;
import org.semanticweb.ontop.sql.TableDefinition;
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

import javax.activation.DataSource;

//import com.hp.hpl.jena.iri.impl.IRIFactoryImpl;
//import it.unibz.krdb.obda.model.net.IRIFactory;



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
    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
	
	public DirectMappingEngine(String baseUri, int mapnr,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory){
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
		conMan = JDBCConnectionManager.getJDBCConnectionManager();
		baseuri = baseUri;
		mapidx = mapnr + 1;
	}
	
	public DirectMappingEngine(DBMetadata metadata, String baseUri, int mapnr,
                               NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory){
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
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
	 */
			
	public void enrichOntology(OWLOntology ontology, OBDAModel model) throws OWLOntologyStorageException, SQLException{
		Set<OBDADataSource> sources = model.getSources();
		OntoExpansion oe = new OntoExpansion();
		if(model.getPrefixManager().getDefaultPrefix().endsWith("/")){
			oe.setURI(model.getPrefixManager().getDefaultPrefix());
		}else{
			oe.setURI(model.getPrefixManager().getDefaultPrefix()+"/");
		}
		
		//For each data source, enrich into the ontology
		if (metadata == null) {
			for (OBDADataSource source: sources) {
				oe.enrichOntology(conMan.getMetaData(source),
						ontology);
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
	 */
	public OWLOntology getOntology(OWLOntology ontology, OWLOntologyManager manager, OBDAModel model)
            throws OWLOntologyCreationException, OWLOntologyStorageException, SQLException{
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		
		Set<Predicate> classset = model.getDeclaredClasses();
		Set<Predicate> objectset = model.getDeclaredObjectProperties();
		Set<Predicate> dataset = model.getDeclaredDataProperties();
		
		//Add all the classes
		for(Iterator<Predicate> it = classset.iterator(); it.hasNext();){
			OWLClass newclass = dataFactory.getOWLClass(IRI.create(it.next().getName().toString()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(ontology,declarationAxiom );
		}
		
		//Add all the object properties
		for(Iterator<Predicate> it = objectset.iterator(); it.hasNext();){
			OWLObjectProperty newclass = dataFactory.getOWLObjectProperty(IRI.create(it.next().getName().toString()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(ontology,declarationAxiom );
		}
		
		//Add all the data properties
		for(Iterator<Predicate> it = dataset.iterator(); it.hasNext();){
			OWLDataProperty newclass = dataFactory.getOWLDataProperty(IRI.create(it.next().getName().toString()));
			OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newclass);
			manager.addAxiom(ontology,declarationAxiom );
		}
				
		return ontology;		
	}
	
	
	/***
	 * extract all the mappings from a datasource.
     *
     * TODO: refactor.
	 * 
	 * @param source
	 * 
	 * @return a new OBDA Model containing all the extracted mappings
	 * @throws Exception 
	 */
	public OBDAModel extractMappings(OBDADataSource source) throws Exception{
        //TODO: avoid this empty construction
        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel emptyModel = obdaFactory.createOBDAModel(new HashSet<OBDADataSource>(),
                new HashMap<URI, ImmutableList<OBDAMappingAxiom>>(),prefixManager);

		return extractMappings(emptyModel, source);
	}
	
	public OBDAModel extractMappings(OBDAModel model, OBDADataSource source) throws Exception{
		return insertMapping(source, model);
	}
	
	
	/***
	 * extract mappings from given datasource, and insert them into the given model
	 * 
	 * @param source
	 * @param model
	 * 
	 * @return the new model
	 * 
	 * Duplicate Exception may happen,
	 * since mapping id is generated randomly and same id may occur
	 * @throws Exception 
	 */
	public OBDAModel insertMapping(OBDADataSource source, OBDAModel model) throws Exception{
        if (model == null) {
            throw new IllegalArgumentException("Model should not be null");
        }

        Set<OBDADataSource> dataSources = new HashSet<>(model.getSources());
        dataSources.add(source);

        DBMetadata metadata = conMan.getMetaData(source);
        URI sourceUri = source.getSourceID();

		if (baseuri == null || baseuri.isEmpty())
			this.baseuri = model.getPrefixManager().getDefaultPrefix();


		List<TableDefinition> tables = metadata.getTableList();
		List<OBDAMappingAxiom> mappingAxioms = new ArrayList<>();
		for (int i = 0; i < tables.size(); i++) {
			TableDefinition td = tables.get(i);
            mappingAxioms.addAll(getMapping(td, metadata, baseuri));
		}

        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>(model.getMappings());
        if (mappingIndex.containsKey(sourceUri)) {
            // Should throw an exception when constructing the model if there is duplicates.
            mappingAxioms.addAll(mappingIndex.get(sourceUri));
        }
        mappingIndex.put(sourceUri, ImmutableList.copyOf(mappingAxioms));

        // Inconsistencies should throw an exception
        OBDAModel newModel = model.newModel(dataSources, mappingIndex);

		for (URI uri : newModel.getMappings().keySet()) {
			for (OBDAMappingAxiom mapping : newModel.getMappings().get(uri)) {
				OBDAQuery q = mapping.getTargetQuery();
				CQIE rule = (CQIE) q;
				for (Function f : rule.getBody()) {
					if (f.getArity() == 1)
                        newModel.declarePredicate(f.getFunctionSymbol());
					else if (f.getFunctionSymbol().getType(1)
							.equals(COL_TYPE.OBJECT))
                        newModel.declareObjectProperty(f.getFunctionSymbol());
					else
                        newModel.declareDataProperty(f.getFunctionSymbol());
				}
			}
		}
        return newModel;
	}
	
	/***
	 * generate a mapping axiom from a table of some database
	 * 
	 * @param table : the datadefinition from which mappings are extraced
	 * @param source : datasource that the table may refer to, such as foreign keys
	 * 
	 *  @return a List of OBDAMappingAxiom-s
	 * @throws Exception 
	 */
	public List<OBDAMappingAxiom> getMapping(DataDefinition table, OBDADataSource source) throws Exception{
		return getMapping(table,conMan.getMetaData(source),baseuri);
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
	public List<OBDAMappingAxiom> getMapping(DataDefinition table, DBMetadata metadata, String baseUri) throws Exception {
		OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
		DirectMappingAxiom dma=null;

			dma = new DirectMappingAxiom(baseUri, table, metadata, dfac);

		dma.setbaseuri(baseUri);
		
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
