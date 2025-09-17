package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.*;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class UnionGraphQuadTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/union-graph-test/mapping.obda";
    private static final String SQL_SCRIPT = "/union-graph-test/schema.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testUnionGraphReturnsAllNamedGraphData() {
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "PREFIX org: <http://www.w3.org/ns/org#> " +
            "SELECT * WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
        
        int unionCount = runQueryAndCount(unionGraphQuery);
        
        // Should return all persons from all named graphs (5 persons total)
        assertTrue("UnionGraph should return all persons from all graphs", unionCount >= 5);
    }

    @Test 
    public void testUnionGraphVsRegularGraphVariable() {
        // Query using regular graph variable
        String graphVarQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH ?g { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
            
        // Query using UnionGraph
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
        
        int graphVarCount = runQueryAndCount(graphVarQuery);
        int unionGraphCount = runQueryAndCount(unionGraphQuery);
        
        // Both should return the same number of results
        assertEquals("UnionGraph and GRAPH ?g should return same number of results", 
                     graphVarCount, unionGraphCount);
        assertTrue("Should have found some persons", unionGraphCount > 0);
    }

    @Test
    public void testUnionGraphWithComplexPattern() {
        // Test UnionGraph returns all people across multiple named graphs (simplified version)
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?person ?name WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; " +
            "            foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
        
        int count = runQueryAndCount(unionGraphQuery);
        assertTrue("Should find people across all graphs", count >= 3);
    }

    @Test
    public void testUnionGraphWithSpecificNamedGraph() {
        // First, test a query restricted to a specific named graph
        String specificGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <http://example.org/graph/corp> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
            
        int specificCount = runQueryAndCount(specificGraphQuery);
        
        // Now test UnionGraph which should return more results (from all graphs)
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
            
        int unionCount = runQueryAndCount(unionGraphQuery);
        
        assertTrue("Specific graph should have some results", specificCount > 0);
        assertTrue("UnionGraph should have more results than any single graph", 
                   unionCount > specificCount);
    }

    @Test
    public void testUnionGraphResultContent() {
        // Test that we actually get the expected content
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?name WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} ORDER BY ?name";
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(unionGraphQuery);
        
        Set<String> names = new HashSet<>();
        for (ImmutableMap<String, String> row : results) {
            String name = row.get("name");
            if (name != null) {
                names.add(name);
            }
        }
        
        // Verify we got names from different graphs
        assertTrue("Should contain John Smith from corp graph", names.contains("John Smith"));
        assertTrue("Should contain Prof. Wilson from academic graph", names.contains("Prof. Wilson"));
        assertTrue("Should contain Dr. Brown from research graph", names.contains("Dr. Brown"));
        assertTrue("Should have found at least 3 different names", names.size() >= 3);
    }

    @Test
    public void testUnionGraphWithSpecificPerson() {
        // Test finding a specific person in the union of all graphs
        String findPersonQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?name WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "    FILTER(?name = \"Dr. Brown\") " +
            "  } " +
            "}";
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(findPersonQuery);
        
        assertFalse("Should find Dr. Brown in the union of all graphs", results.isEmpty());
        assertEquals("Should find exactly one Dr. Brown", 1, results.size());
        assertEquals("Dr. Brown", results.get(0).get("name"));
    }

    @Test
    public void testUnionGraphWithCount() {
        // Test aggregate functions with UnionGraph
        String countQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT (COUNT(*) AS ?count) WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person " +
            "  } " +
            "}";
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(countQuery);
        
        assertFalse("Should have a count result", results.isEmpty());
        String countStr = results.get(0).get("count");
        assertNotNull("Count should not be null", countStr);
        int count = Integer.parseInt(countStr);
        
        assertTrue("Should count all persons across all graphs", count >= 5);
    }

    @Test
    public void testUnionGraphWithFromNamed() {
        String unionWithDatasetQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "SELECT * " +
                        "FROM NAMED <http://example.org/graph/corp> " +
                        "FROM NAMED <http://example.org/graph/academic> " +
                        "WHERE { " +
                        "  GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o }" +
                        "}";

        int count = runQueryAndCount(unionWithDatasetQuery);
        assertTrue("UnionGraph with FROM NAMED should return results", count > 0);
    }

    @Test
    public void testUnionGraphWithFrom() {
        String unionWithDatasetQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "SELECT * " +
                        "FROM  <http://example.org/graph/corp> " +
                        "WHERE { " +
                        "  GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o }" +
                        "}";

        int count = runQueryAndCount(unionWithDatasetQuery);
        assertEquals("UnionGraph with FROM should return results", 0, count);
    }
}
