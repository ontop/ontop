package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class UnionGraphDebugTest extends AbstractRDF4JTest {
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
    public void debugUnionGraphVsGraphVariable() {
        System.out.println("=== Testing GRAPH ?g ===");
        String graphVarQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH ?g { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "}";
        
        ImmutableList<ImmutableMap<String, String>> graphVarResults = executeQuery(graphVarQuery);
        System.out.println("GRAPH ?g results: " + graphVarResults.size());
        for (ImmutableMap<String, String> result : graphVarResults) {
            System.out.println("  " + result);
        }

        System.out.println("\n=== Testing UnionGraph ===");
        String unionGraphQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "}";
        
        System.out.println("UnionGraph query: " + unionGraphQuery);
        ImmutableList<ImmutableMap<String, String>> unionResults = executeQuery(unionGraphQuery);
        System.out.println("UnionGraph results: " + unionResults.size());
        for (ImmutableMap<String, String> result : unionResults) {
            System.out.println("  " + result);
        }

        System.out.println("\n=== Testing with FROM NAMED ===");
        String unionWithDatasetQuery = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * " +
            "FROM NAMED <http://example.org/graph/corp> " +
            "FROM NAMED <http://example.org/graph/academic> " +
            "WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "}";
        
        ImmutableList<ImmutableMap<String, String>> datasetResults = executeQuery(unionWithDatasetQuery);
        System.out.println("UnionGraph with FROM NAMED results: " + datasetResults.size());
        for (ImmutableMap<String, String> result : datasetResults) {
            System.out.println("  " + result);
        }

        // The UnionGraph should work the same as GRAPH ?g when no dataset is specified
        assertTrue("GRAPH ?g should return results", graphVarResults.size() > 0);
    }
}
