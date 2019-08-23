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
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class OntopRDF4JInMemoryTest {

	// REMARK: xiao(2019-01-04):
	//  It seems that the newer version of RDF4J
    // has disabled 'doctype-decl' and hence it cannot parse the old files anymore and throws the following exception.
	// org.eclipse.rdf4j.rio.RDFParseException: DOCTYPE is disallowed when the feature "http://apache.org/xml/features/disallow-doctype-decl" set to true. [line 4, column 10]
    //
    //  I did not find an easy way to change this behavior, but simply converted the file format
    //   from RDF/XML format into turtle .
    final static String owlFile = "/test/exampleBooks.ttl";
	final static String owlAboxFile = "/test/exampleBooksABox.ttl";

	private static Repository repository;
	private RepositoryConnection con;

	@BeforeClass
	public static void init() throws Exception {

		/*
		 * Create a Quest Sesame (in-memory) repository with additional setup
		 * that uses no existential reasoning and the rewriting technique is
		 * using TreeWitness algorithm.
		 */

		Properties p = new Properties();
		p.put(OntopReformulationSettings.EXISTENTIAL_REASONING, false);

		/*
		 * Add RDF data to the repository
		 */

		SimpleDataset dataset = new SimpleDataset();
		SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
		dataset.addDefaultGraph(valueFactory.createIRI(OntopRDF4JInMemoryTest.class.getResource(owlAboxFile).toString()));
		dataset.addDefaultGraph(valueFactory.createIRI(OntopRDF4JInMemoryTest.class.getResource(owlFile).toString()));

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, p)) {
			repository = OntopRepository.defaultRepository(loader.getConfiguration());
				/*
		 		* Repository must be always initialized first
				 */
			repository.initialize();
		}
	}

	@AfterClass
	public static void terminate() throws Exception {
		repository.shutDown();
	}

	@Before
	public void setUp() throws Exception {
		/*
		 * Get the repository connection
		 */
		con = repository.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		con.close();
	}

	@Test
	public void runQuery() throws Exception {

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
	        assertTrue(result.hasNext());
	        int nresults = 0;
			while (result.hasNext()) {
			    nresults++;
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
			assertEquals(24, nresults);

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
			OntopRDF4JInMemoryTest example = new OntopRDF4JInMemoryTest();
			example.init();
			example.setUp();
			example.runQuery();
			example.tearDown();
			example.terminate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
