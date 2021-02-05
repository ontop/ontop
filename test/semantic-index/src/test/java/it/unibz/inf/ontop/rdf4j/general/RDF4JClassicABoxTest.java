package it.unibz.inf.ontop.rdf4j.general;

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

import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class RDF4JClassicABoxTest {


	static Repository repo;
	String baseURI = "http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#";
	static SimpleDataset dataset;
	static ValueFactory valueFactory;

	@BeforeClass
	public static void setupInMemory() throws Exception
	{
		// create a sesame in-memory repository
		String owlfile = "src/test/resources/onto2.ttl";

		Properties p = new Properties();
		p.put(OntopReformulationSettings.EXISTENTIAL_REASONING, false);

		dataset = new SimpleDataset();
		File dataFile = new File(owlfile);
		valueFactory = SimpleValueFactory.getInstance();

		System.out.println("First file.");
		dataset.addDefaultGraph(valueFactory.createIRI(dataFile.toURI().toString()));

		System.out.println("Add from files.");

		// /add data to dataset for the repository
		File file = new File("src/test/resources/onto2plus.ttl");
		File file2 = new File("src/test/resources/onto2.ttl");

		dataset.addDefaultGraph(valueFactory.createIRI(file.toURI().toString()));

		dataset.addDefaultGraph(valueFactory.createIRI(file2.toURI().toString()));

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, p)) {
			repo = OntopRepository.defaultRepository(loader.getConfiguration());
				/*
		 		* Repository must be always initialized first
				 */
			repo.initialize();
		}

	}

	@AfterClass
	public static void close() throws RepositoryException
	{
		if (repo != null) {
			System.out.println("Closing...");
			repo.shutDown();
			System.out.println("Done.");
		}
	}


	// I need to pass a dataset to the repository, it is not possible anymore to add triples to the repository later
	private void addFromURI() throws RepositoryException
	{


		// create some resources and literals to make statements out of
		IRI alice = valueFactory.createIRI(baseURI+"Alice");
		IRI bob = valueFactory.createIRI(baseURI+"Bob");
		IRI age = valueFactory.createIRI(baseURI + "age");
		IRI person = valueFactory.createIRI(baseURI+ "Person");
		Literal bobsAge = valueFactory.createLiteral(5);
		Literal alicesAge = valueFactory.createLiteral(14);

		Statement aliceStatement = valueFactory.createStatement(alice, RDF.TYPE, person);



		// alice is a person
//		Statement aliceStatement = valueFactory.createStatement(alice, RDF.TYPE, person);

//		Statement aliceAgeStatement = valueFactory.createStatement(alice, age, alicesAge);

		// bob is a person
		Statement bobStatement = valueFactory.createStatement(alice, RDF.TYPE, person);


//		Statement bobAgeStatement = valueFactory.createStatement(bob, age, bobsAge);
	}
	
	@Test
	public void tupleQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	


		// /query repo
		// con.setNamespace("onto",
		// "<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>");
		// System.out.println(con.getNamespaces().next().toString());
		String queryString = "PREFIX : \n<http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#>\n "
				+ "SELECT ?x ?y WHERE { ?x a :Person. ?x :age ?y } ";
		// String queryString =
		// "SELECT ?x ?y WHERE { ?x a onto:Person. ?x onto:age ?y } ";

		try (RepositoryConnection con = repo.getConnection()) {
			TupleQuery tupleQuery = (con).prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			TupleQueryResult result = tupleQuery.evaluate();

			System.out.println(result.getBindingNames());
			int nresult = 0;
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				Value valueOfX = bindingSet.getValue("x");
				Literal valueOfY = (Literal) bindingSet.getValue("y");
				System.out.println(valueOfX.stringValue() + ", " + valueOfY.floatValue());
				nresult++;
			}
			assertEquals(3, nresult);
			result.close();
		}
	}

	@Test
	public void graphQuery() throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		String queryString = "CONSTRUCT {?x a ?y } WHERE { ?x a ?y } ";
		// String queryString =
		// "SELECT ?x ?y WHERE { ?x a onto:Person. ?x onto:age ?y } ";
		try (RepositoryConnection con = repo.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
			GraphQueryResult result = graphQuery.evaluate();

			int nresult = 0;
			while (result.hasNext()) {
				Statement st = result.next();
				System.out.println(st.toString());
				nresult++;
			}
			//BC: apparently, no reasoning is going on...
			assertEquals(6, nresult);
			result.close();
		}
	}

	@Test
	public void booleanQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	
		
		// /query repo
		String queryString = "PREFIX : \n<http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#>\n "
				+ "ASK { :Lisa a :Person} ";
		try (RepositoryConnection con = repo.getConnection()) {
			BooleanQuery boolQuery = (con).prepareBooleanQuery(
					QueryLanguage.SPARQL, queryString);
			boolean result = boolQuery.evaluate();
			assertTrue(result);
		}
	}

	

}
