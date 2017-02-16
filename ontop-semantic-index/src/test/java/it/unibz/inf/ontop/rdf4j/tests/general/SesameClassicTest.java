package it.unibz.inf.ontop.rdf4j.tests.general;

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

import it.unibz.inf.ontop.injection.OntopQueryAnsweringSettings;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class SesameClassicTest {

	RepositoryConnection con;
	Repository repo;
	String baseURI = "http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#";
	SimpleDataset dataset;
	ValueFactory valueFactory;

	@Before
	public void setupInMemory() throws Exception 
	{
		// create a sesame in-memory repository
		String owlfile = "src/test/resources/onto2.owl";

		Properties p = new Properties();
		p.put(OntopQueryAnsweringSettings.EXISTENTIAL_REASONING, false);

		dataset = new SimpleDataset();
		File dataFile = new File(owlfile);
		valueFactory = SimpleValueFactory.getInstance();
		dataset.addDefaultGraph(valueFactory.createIRI(dataFile.toURI().toString()));

		addFromURI();

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, p)) {
			repo = new OntopVirtualRepository(loader.getConfiguration());
				/*
		 		* Repository must be always initialized first
				 */
			repo.initialize();
		}

		con = repo.getConnection();
	}

	@After
	public void close() throws RepositoryException
	{
		System.out.println("Closing...");
		con.close();
		repo.shutDown();
		System.out.println("Done.");
	}
	
	public void addFromFile() throws RDFParseException, RepositoryException, IOException
	{
		// /add data to repo
		File file = new File("src/test/resources/example/onto2plus.owl");

		System.out.println("Add from file.");
		dataset.addDefaultGraph(valueFactory.createIRI(file.toURI().toString()));
//		con.add(file, baseURI, RDFFormat.RDFXML);
	}
	
	
	public void addFromURI() throws RepositoryException
	{


		// create some resources and literals to make statements out of
		IRI alice = valueFactory.createIRI(baseURI+"Alice");
		IRI bob = valueFactory.createIRI(baseURI+"Bob");
		IRI age = valueFactory.createIRI(baseURI + "age");
		IRI person = valueFactory.createIRI(baseURI+ "Person");
		Literal bobsAge = valueFactory.createLiteral(5);
		Literal alicesAge = valueFactory.createLiteral(14);

		Statement aliceStatement = valueFactory.createStatement(alice, RDF.TYPE, person);

		dataset.addNamedGraph((IRI)aliceStatement.getContext());


//		dataset.addDefaultGraph(valueFactory.createIRI(dataFile.toURI().toString()));
		// alice is a person
//		Statement aliceStatement = valueFactory.createStatement(alice, RDF.TYPE, person);

//		Statement aliceAgeStatement = valueFactory.createStatement(alice, age, alicesAge);

		// bob is a person
		Statement bobStatement = valueFactory.createStatement(alice, RDF.TYPE, person);


//		Statement bobAgeStatement = valueFactory.createStatement(bob, age, bobsAge);
	}
	

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
		TupleQuery tupleQuery = (con).prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		System.out.println(result.getBindingNames());

		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			Value valueOfX = bindingSet.getValue("x");
			Literal valueOfY = (Literal) bindingSet.getValue("y");
			System.out.println(valueOfX.stringValue() + ", "+ valueOfY.floatValue());
		}
		result.close();
	}
	
	public void graphQuery() throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		String queryString = "CONSTRUCT {?x a ?y} WHERE { ?x a ?y} ";
		// String queryString =
		// "SELECT ?x ?y WHERE { ?x a onto:Person. ?x onto:age ?y } ";
		GraphQuery graphQuery = (con).prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		GraphQueryResult result = graphQuery.evaluate();

		while (result.hasNext()) {
			Statement st = result.next();
			System.out.println(st.toString());
		}
		result.close();
	}
	
	public void booleanQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	
		
		// /query repo
		String queryString = "PREFIX : \n<http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#>\n "
				+ "ASK { :Lisa a :Person} ";
		BooleanQuery boolQuery = (con).prepareBooleanQuery(
				QueryLanguage.SPARQL, queryString);
		boolean result = boolQuery.evaluate();

		System.out.println(result);
	}
	


	@Test
	public void test1() throws Exception
	{
		try{


		//addFromFile();
	//	tupleQuery();
		//booleanQuery();
		graphQuery();
		}
		catch(Exception e)
		{e.printStackTrace();
		throw e;}
		
	}
	

}
