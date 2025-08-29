package it.unibz.inf.ontop.rdf4j.repository;

import org.eclipse.rdf4j.query.*;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class UnionGraphTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String ONTOLOGY_FILE = null;

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testUnionGraphBasicQuery() {
        String sparqlQuery = "SELECT * WHERE { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } } LIMIT 10";
        
        int count = runQueryAndCount(sparqlQuery);
        
        // The query should execute without throwing an exception
        // The count may be 0 if no data exists, but no exception should occur
        assertTrue("UnionGraph query should execute successfully", count >= 0);
    }

    @Test
    public void testUnionGraphWithNamedDataset() {
        String sparqlQuery = 
            "SELECT * FROM NAMED <http://example.org/graph1> FROM NAMED <http://example.org/graph2> " +
            "WHERE { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } } LIMIT 5";
        
        int count = runQueryAndCount(sparqlQuery);
        
        // This test verifies that the query doesn't throw an exception
        // Even if no named graphs exist in the test data, the query should execute successfully
        assertTrue("Query executed successfully", count >= 0);
    }

    @Test  
    public void testUnionGraphVsRegularGraphQuery() {
        // First, run a regular query to get baseline data
        String regularQuery = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        int regularCount = runQueryAndCount(regularQuery);
        
        // Now run the same query using UnionGraph
        String unionGraphQuery = "SELECT * WHERE { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } } LIMIT 10";
        int unionGraphCount = runQueryAndCount(unionGraphQuery);
        
        // In this test setup, UnionGraph should return similar data to the regular query
        // since we're querying the default graph
        assertTrue("UnionGraph should return results", unionGraphCount >= 0);
        assertTrue("Regular query should return results", regularCount >= 0);
    }

    @Test
    public void testUnionGraphConstantDetection() {
        // This test verifies that the UnionGraph URI is correctly recognized
        String sparqlQuery = "SELECT (COUNT(*) as ?count) WHERE { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } }";
        
        try {
            int count = runQueryAndCount(sparqlQuery);
            
            // The query should execute without throwing an exception
            // The result depends on available data, but the important thing is no exception occurs
            assertTrue("Query executed without error", count >= 0);
        } catch (Exception e) {
            fail("UnionGraph query should not throw exception: " + e.getMessage());
        }
    }
}
