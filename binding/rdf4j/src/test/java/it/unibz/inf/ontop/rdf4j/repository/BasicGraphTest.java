package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class BasicGraphTest extends AbstractRDF4JTest {
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
    public void testBasicTripleQuery() {
        String query = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  ?person a foaf:Person ; foaf:name ?name " +
            "} LIMIT 5";
        
        int count = runQueryAndCount(query);
        System.out.println("Basic triple query count: " + count);
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(query);
        System.out.println("Basic triple query results: " + results);
    }

    @Test
    public void testGraphVariableQuery() {
        String query = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH ?g { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} LIMIT 5";
        
        int count = runQueryAndCount(query);
        System.out.println("GRAPH ?g query count: " + count);
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(query);
        System.out.println("GRAPH ?g query results: " + results);
    }

    @Test
    public void testSpecificGraphQuery() {
        String query = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <http://example.org/graph/corp> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} LIMIT 5";
        
        int count = runQueryAndCount(query);
        System.out.println("Specific graph query count: " + count);
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(query);
        System.out.println("Specific graph query results: " + results);
    }

    @Test
    public void testUnionGraphQuery() {
        String query = 
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            "SELECT * WHERE { " +
            "  GRAPH <urn:x-arq:UnionGraph> { " +
            "    ?person a foaf:Person ; foaf:name ?name " +
            "  } " +
            "} LIMIT 5";
        
        int count = runQueryAndCount(query);
        System.out.println("UnionGraph query count: " + count);
        
        ImmutableList<ImmutableMap<String, String>> results = executeQuery(query);
        System.out.println("UnionGraph query results: " + results);
    }
}
