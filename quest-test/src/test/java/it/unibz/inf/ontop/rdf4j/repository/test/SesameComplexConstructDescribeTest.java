package it.unibz.inf.ontop.rdf4j.repository.test;

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

import it.unibz.inf.ontop.rdf4j.repository.OntopClassicInMemoryRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * This unit test is to ensure the correctness of construct and describe
 * queries in ontop through the Sesame API. All tests should be green.
 * @author timi
 *
 */
public class SesameComplexConstructDescribeTest {

	OntopRepositoryConnection con = null;
	Repository repo = null;
	ValueFactory fac = null;
	String fileName = "src/test/resources/complexConstruct.ttl";
	String owlFile = "src/test/resources/describeConstruct.owl";
	
	@Before
	public void setUp() throws Exception {
		
		try {
			System.out.println("In-memory quest repo.");
			
			repo = new OntopClassicInMemoryRepository("constructDescribe", owlFile, false, "TreeWitness");
			repo.initialize();
			con = (OntopRepositoryConnection) repo.getConnection();
			fac = con.getValueFactory();
			File data = new File(fileName);
			System.out.println(data.getAbsolutePath());
			if (data.canRead())
				con.add(data, "http://www.semanticweb.org/ontologies/test", RDFFormat.TURTLE, (Resource)null);
			else
				throw new Exception("The specified file cannot be found or has restricted access.");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@After
	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
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
		Assert.assertEquals(4, result);
	}


	@Test
	public void testConstruct2() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
				queryString);

		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			result++;
			Statement s = gresult.next();
			//System.out.println(s.toString());
		}
		Assert.assertEquals(2, result);
	}

	// https://github.com/ontop/ontop/issues/161
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
        Assert.assertEquals(2, result);
    }


}
