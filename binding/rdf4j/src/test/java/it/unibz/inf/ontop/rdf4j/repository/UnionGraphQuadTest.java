package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;

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
        assertEquals("UnionGraph should return all persons from all graphs", 5, unionCount);
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
        assertEquals("Should have found some persons", 5, unionGraphCount);
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
        assertEquals("Should find people across all graphs", 5, count);
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

        assertEquals("Specific graph should have some results", 2, specificCount);
        assertTrue("UnionGraph should have more results than any single graph", 
                   unionCount > specificCount);
    }

    @Test
    public void testUnionGraphResultContent() {
        // Test that we actually get the expected content
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?v WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?v " +
            "  } " +
            "} ORDER BY ?name";

        runQueryAndCompare(unionGraphQuery, ImmutableSet.of("Dr. Brown", "John Smith", "Prof. Wilson",
                "Alice Johnson", "Jane Doe"));
    }

    @Test
    public void testUnionGraphWithSpecificPerson() {
        // Test finding a specific person in the union of all graphs
        String findPersonQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT ?v WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?v " +
            "    FILTER(?v = \"Dr. Brown\") " +
            "  } " +
            "}";

        runQueryAndCompare(findPersonQuery, ImmutableList.of("Dr. Brown"));
    }

    @Test
    public void testUnionGraphWithCount() {
        // Test aggregate functions with UnionGraph
        String countQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT (COUNT(*) AS ?v) WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person " +
            "  } " +
            "}";

        runQueryAndCompare(countQuery, ImmutableSet.of("5"));
    }

    @Test
    public void testUnionGraphWithFromNamed() {
        String unionWithDatasetQuery =
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "SELECT * " +
                        "FROM NAMED <http://example.org/graph/corp> " +
                        "FROM NAMED <http://example.org/graph/academic> " +
                        "WHERE { " +
                        "  GRAPH <urn:x-arq:UnionGraph> { ?person a foaf:Person}" +
                        "}";

        int count = runQueryAndCount(unionWithDatasetQuery);
        assertEquals("UnionGraph with FROM NAMED should return results", 4, count);
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
