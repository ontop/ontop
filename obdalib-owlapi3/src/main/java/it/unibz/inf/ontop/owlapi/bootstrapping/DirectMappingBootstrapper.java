package it.unibz.inf.ontop.owlapi.bootstrapping;

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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.ontology.impl.OntologyVocabularyImpl;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;

public class DirectMappingBootstrapper {


    private OWLOntology onto;
    private OBDAModel model;
    private OBDADataSource source;


    public DirectMappingBootstrapper(String baseuri, String url, String user, String password, String driver,
									 NativeQueryLanguageComponentFactory nativeQLFactory,
									 OBDAFactoryWithException obdaFactory) throws Exception{
		OBDADataFactory fact = OBDADataFactoryImpl.getInstance();
		OBDADataSource source = fact.getJDBCDataSource(url, user, password, driver);
		//create empty ontology and model, add source to model
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		OWLOntology onto = mng.createOntology(IRI.create(baseuri));
		//TODO: avoid creating a model without mappings
		PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
		OBDAModel model = obdaFactory.createOBDAModel(ImmutableSet.of(source),
				new HashMap<URI, ImmutableList<OBDAMappingAxiom>>(), prefixManager,
				new OntologyVocabularyImpl());

		bootstrapOntologyAndDirectMappings(baseuri, onto, model, source, nativeQLFactory, obdaFactory);
	}

	public DirectMappingBootstrapper(String baseUri, OWLOntology ontology, OBDAModel model, OBDADataSource source,
									 NativeQueryLanguageComponentFactory nativeQLFactory,
									 OBDAFactoryWithException obdaFactory) throws Exception {
		bootstrapOntologyAndDirectMappings(baseUri, ontology, model, source, nativeQLFactory, obdaFactory);
	}

    private void bootstrapOntologyAndDirectMappings(String baseuri, OWLOntology onto, OBDAModel model,
													OBDADataSource source, NativeQueryLanguageComponentFactory nativeQLFactory,
													OBDAFactoryWithException obdaFactory) throws DuplicateMappingException, SQLException, OWLOntologyCreationException, OWLOntologyStorageException {
        this.source = source;
        DirectMappingEngine engine = new DirectMappingEngine(baseuri, model.getMappings(source.getSourceID()).size(), nativeQLFactory, obdaFactory);
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
