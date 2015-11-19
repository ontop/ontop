package it.unibz.krdb.obda.owlapi3.bootstrapping;

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
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.directmapping.DirectMappingEngine;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.sql.SQLException;

public class DirectMappingBootstrapper {


    private OWLOntology onto;
    private OBDAModel model;
    private OBDADataSource source;


    public DirectMappingBootstrapper(String baseuri, String url, String user, String password, String driver) throws Exception{
		OBDADataFactory fact = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fact.getJDBCDataSource(url, user, password, driver);
		//create empty ontology and model, add source to model
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		OWLOntology onto = mng.createOntology(IRI.create(baseuri));
		OBDAModel model = fact.getOBDAModel();
		model.addSource(source);
		bootstrapOntologyAndDirectMappings(baseuri, onto, model, source);
	}

	public DirectMappingBootstrapper(String baseUri, OWLOntology ontology, OBDAModel model, OBDADataSource source) throws Exception {
		bootstrapOntologyAndDirectMappings(baseUri, ontology, model, source);
	}

    private void bootstrapOntologyAndDirectMappings(String baseuri, OWLOntology onto, OBDAModel model, OBDADataSource source) throws DuplicateMappingException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
        this.source = source;
        DirectMappingEngine engine = new DirectMappingEngine(baseuri, model.getMappings(source.getSourceID()).size());
        this.model =  engine.extractMappings(model, source);
        this.onto =  engine.getOntology(onto, onto.getOWLOntologyManager(), model);
    }


    /***
	 * Creates an OBDA model using direct mappings
	 */
	public OBDAModel getModel() {
		return this.model;
	}

	/***
	 * Creates an OBDA file using direct mappings. Internally this one calls the
	 * previous one and just renders the file.
	 */
	public OWLOntology getOntology() {
		return this.onto;
	}

}
