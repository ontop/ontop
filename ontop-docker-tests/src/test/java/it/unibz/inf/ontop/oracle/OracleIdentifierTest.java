package it.unibz.inf.ontop.oracle;

/*
 * #%L
 * ontop-test
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
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/***
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifiers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class OracleIdentifierTest {

	static final String owlfile = "src/test/resources/oracle/identifiers/identifiers.owl";
	static final String obdafile = "src/test/resources/oracle/identifiers/identifiers-oracle.obda";
	static final String propertiesfile = "src/test/resources/oracle/oracle.properties";

	RepositoryConnection con;
	Repository repository;


	@Before
	public void setUp() {

		try {
			OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
					.ontologyFile(owlfile)
					.nativeOntopMappingFile(obdafile)
					.enableExistentialReasoning(true)
					.enableTestMode()
					.propertyFile(propertiesfile)
					.build();

			repository = new OntopVirtualRepository(configuration);
			/*
			 * Repository must be always initialized first
			 */
			repository.initialize();

			/*
			 * Get the repository connection
			 */
			con = repository.getConnection();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * Test use of lowercase, unquoted table, schema and column identifiers (also in target)
	 * @throws Exception
	 */
	@After
	public void tearDown() {
		try {
			if (con != null && con.isOpen()) {
				con.close();
			}
			repository.shutDown();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina", val);
	}


	/**
	 * Test use of lowercase, unquoted table and column identifiers (also in target) with uppercase table identifiers
	 * @throws Exception
	 */

	@Test
	public void testUpperCaseTableUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country2} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-Argentina", val);
	}
	
	/**
	 * Test use of lowercase, quoted alias in a view definition 
	 * @throws Exception
	 */

	@Test
	public void testLowerCaseColumnViewDefQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country4} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-Argentina", val);
	}

	/**
	 * Test use of lowercase, unquoted alias in a view definition 
	 * @throws Exception
	 */

	@Test
	public void testLowerCaseColumnViewDefUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country5} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country5-Argentina", val);
	}

	private String runQueryAndReturnStringOfIndividualX(String query) {
		String  resultval = "";
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					query);
			TupleQueryResult result = tupleQuery.evaluate();

			assertTrue (result.hasNext());
			BindingSet setResult= result.next();
			resultval = setResult.getValue("x").stringValue();

			result.close();



		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultval;
	}


}

