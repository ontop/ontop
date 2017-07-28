package it.unibz.inf.ontop.rdf4j.repository;

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

import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.*;

import java.io.File;
import java.util.Properties;

/**
 * This unit test is to ensure the correctness of construct and describe
 * queries in ontop through the Sesame API. All tests should be green.
 * @author timi
 *
 */
public class RDF4JComplexConstructDescribeTest {

	private static Repository REPOSITORY;
	private static final String DATA_FILE_PATH = "src/test/resources/complexConstruct.ttl";

	private RepositoryConnection con;

	@BeforeClass
	public static void init() throws Exception {
		SimpleDataset dataset = new SimpleDataset();
		File dataFile = new File(DATA_FILE_PATH);
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		dataset.addDefaultGraph(valueFactory.createIRI(dataFile.toURI().toString()));

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, new Properties())) {
			REPOSITORY = OntopRepository.defaultRepository(loader.getConfiguration());
			REPOSITORY.initialize();
		}
	}

	@AfterClass
	public static void terminate() throws Exception {
		REPOSITORY.shutDown();
	}
	
	@Before
	public void setUp() throws Exception {
		con = REPOSITORY.getConnection();
	}
	
	@After
	public void tearDown() throws Exception {
		con.close();
	}
	
	@Test
	public void testInsertData() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
				queryString);

		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			Statement s = gresult.next();
			result++;
			System.out.println(s.toString());
		}
		Assert.assertEquals(5, result);
	}

	// Test case for: https://github.com/ontop/ontop/issues/161
    @Test
    public void testConstructOptional() throws Exception {
        int result = 0;
        String queryString = "PREFIX : <http://www.semanticweb.org/ontologies/test#> \n" +
                "CONSTRUCT { ?s :p ?o1. ?s :p ?o2. }\n" +
                "WHERE {\n" +
                "{?s :p1 ?o1}\n" +
                "OPTIONAL {?s :p2 ?o2}\n" +
                "}";
        GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
                queryString);

        GraphQueryResult gresult = graphQuery.evaluate();
        while (gresult.hasNext()) {
            result++;
            Statement s = gresult.next();
            System.out.println(s.toString());
        }
        Assert.assertEquals(4, result);
    }

}
