package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class EmptyNamedGraphsTest extends AbstractRDF4JTest {

    private static final String DB_FILE = "/empty-named-graphs/database.sql";
    private static final String OBDA_FILE = "/empty-named-graphs/mapping.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void queryGraphsWithNoFromNamedTest() {
        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT * " +
                "FROM <http://example.org/graphA>" +
                "FROM <http://example.org/graphB>" +
                "WHERE { GRAPH ?v {" +
                " ?v a foaf:Person" +
                "} }";

        runQueryAndCompare(query, ImmutableSet.of());
    }

    @Test
    public void newDefaultGraphTest() {
        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT ?v " +
                "FROM <http://example.org/graphA>" +
                "FROM <http://example.org/graphB>" +
                "WHERE { ?v a foaf:Person . }";

        runQueryAndCompare(query, ImmutableSet.of(
                "http://example.org/person/1",
                "http://example.org/person/2",
                "http://example.org/person/3"));
    }

    @Test
    public void queryEmptyDefaultGraphTest() {
        String query = "SELECT * " +
                "FROM NAMED <http://example.org/graphC>" +
                "WHERE { ?v ?p ?o }";

        runQueryAndCompare(query, ImmutableSet.of());
    }

    @Test
    public void queryAllNamedGraphsTest() {
        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT * " +
                "WHERE { GRAPH ?v {" +
                " ?s a foaf:Person" +
                "} }";

        runQueryAndCompare(query, ImmutableSet.of("http://example.org/graphA", "http://example.org/graphB",
                "http://example.org/graphC"));
    }
}
