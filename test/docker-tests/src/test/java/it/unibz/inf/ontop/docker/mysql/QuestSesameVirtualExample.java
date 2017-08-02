package it.unibz.inf.ontop.docker.mysql;

/*
 * #%L
 * ontop-quest-sesame
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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


public class QuestSesameVirtualExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	final String owlFile = "/mysql/example/exampleBooks.owl";
	final String obdaFile = "/mysql/example/exampleBooks.obda";
	final String ttlFile = "/mysql/example/Books-mappings.ttl";
	final String propertyFile = "/mysql/example/exampleBooks.properties";

	final String owlFileName =  this.getClass().getResource(owlFile).toString();
	final String obdaFileName =  this.getClass().getResource(obdaFile).toString();
	final String propertyFileName =  this.getClass().getResource(propertyFile).toString();

	public void runQuery() throws Exception {




		/*
		 * Create a Quest Sesame repository with additional setup that uses no
		 * existential reasoning and the rewriting technique is using
		 * TreeWitness algorithm.
		 */
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.nativeOntopMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();

		Repository repo = OntopRepository.defaultRepository(configuration);
		

		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		RepositoryConnection con = repo.getConnection();

		/*
		 * Sample query: show all books with their title.
		 */
		String sparqlQuery = 
				"PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" + 
				"SELECT ?x ?y \n" +
				"WHERE {?x a :Book; :title ?y}";
			
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
			TupleQueryResult result = tupleQuery.evaluate();
	
			/*
			 * Print out the results to the standard output
			 */
			List<String> bindingNames = result.getBindingNames();
			System.out.println(bindingNames);
	
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				boolean needSeparator = false;
				for (String binding : bindingNames) {
					if (needSeparator) {
						System.out.print(", ");
					}
					Value value = bindingSet.getValue(binding);
					System.out.print(value.toString());
					needSeparator = true;
				}
				System.out.println();
			}
	
			/*
			 * Close result set to release resources
			 */
			result.close();
		} finally {
			
			/*
			 * Finally close the connection to release resources
			 */
			if (con != null && con.isOpen()) {
				con.close();
			}
		}
	}
	
	public void runR2RML() throws Exception {
		/*
		 * Create a Quest Sesame repository with additional setup that uses no
		 * existential reasoning and the rewriting technique is using
		 * TreeWitness algorithm.
		 */
		
		//create owlontology from file
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		URL owlFileName =  this.getClass().getResource(owlFile);
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(owlFileName.getPath()).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		OWLOntology owlontology = man.loadOntologyFromOntologyDocument(new File(owlFileName.getPath()));
		
		//create RDF Graph from ttl file
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		InputStream in =  this.getClass().getResourceAsStream(ttlFile);
		URL documentUrl = new URL("file://" + ttlFile);
		//Graph myGraph = new org.eclipse.rdf4j.model.impl.GraphImpl();
		Model myModel = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(myModel);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());

		String ttlFileName =  this.getClass().getResource(ttlFile).toString();
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.r2rmlMappingFile(ttlFileName)
				.enableExistentialReasoning(true)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();
		
		Repository repo = OntopRepository.defaultRepository(configuration);

		System.out.println(myModel);
		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		RepositoryConnection con = repo.getConnection();

		/*
		 * Sample query: show all books with their title.
		 */
		String sparqlQuery = 
				"PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" + 
				"SELECT ?x ?y \n" +
				"WHERE {?x a :Book; :hasEdition ?y}";
			
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
			TupleQueryResult result = tupleQuery.evaluate();
	
			/*
			 * Print out the results to the standard output
			 */
			List<String> bindingNames = result.getBindingNames();
			System.out.println(bindingNames);
	
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				boolean needSeparator = false;
				for (String binding : bindingNames) {
					if (needSeparator) {
						System.out.print(", ");
					}
					Value value = bindingSet.getValue(binding);
					System.out.print(value.toString());
					needSeparator = true;
				}
				System.out.println();
			}
	
			/*
			 * Close result set to release resources
			 */
			result.close();
		} finally {
			
			/*
			 * Finally close the connection to release resources
			 */
			if (con != null && con.isOpen()) {
				con.close();
			}
		}
	}
	public static void main(String[] args) {
		try {
			QuestSesameVirtualExample example = new QuestSesameVirtualExample();
		//	example.runQuery();
			example.runR2RML();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
