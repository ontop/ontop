package org.semanticweb.ontop.sesame.tests.general;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class QuestSesameVirtualExample {

	/*
	 * Use the sample database using H2 from
	 * https://babbage.inf.unibz.it/trac/obdapublic/wiki/InstallingTutorialDatabases
	 */
	final String owlFile = "src/test/resources/example/exampleBooks.owl";
	final String obdaFile = "src/test/resources/example/exampleBooks.obda";
	final String ttlFile = "src/test/resources/example/Books-mappings.ttl";
	
	public void runQuery() throws Exception {

		/*
		 * Create a Quest Sesame repository with additional setup that uses no
		 * existential reasoning and the rewriting technique is using
		 * TreeWitness algorithm.
		 */
		Repository repo = new SesameVirtualRepo("virtualExample", owlFile, obdaFile, false, "TreeWitness");
		

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
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(owlFile).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		OWLOntology owlontology = man.loadOntologyFromOntologyDocument(new File(owlFile));
		
		//create RDF Graph from ttl file
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		InputStream in = new FileInputStream(ttlFile);
		URL documentUrl = new URL("file://" + ttlFile);
		//Graph myGraph = new org.openrdf.model.impl.GraphImpl();
		Model myModel = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(myModel);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());
		
		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.REWRITE, "true");
		p.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		//set jdbc params in config
		p.setProperty(QuestPreferences.DB_NAME, "books");
		p.setProperty(QuestPreferences.JDBC_URL, "jdbc:mysql://10.7.20.39/books?sessionVariables=sql_mode='ANSI'");
		p.setProperty(QuestPreferences.DB_USER, "fish");
		p.setProperty(QuestPreferences.DB_PASSWORD, "fish");
		p.setProperty(QuestPreferences.JDBC_DRIVER, "com.mysql.jdbc.Driver");
		
		Repository repo = new SesameVirtualRepo("virtualExample2", owlontology, myModel, new QuestPreferences(p));

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
