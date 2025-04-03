package it.unibz.inf.ontop.docker.lightweight.duckdb;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

class DuckDBInitScriptTest extends AbstractDockerRDF4JTest {

    private static final String OBDA_FILE = "/initScript/person.obda";
    private static final String PROPERTIES_FILE = "/initScript/person.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, null, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    void testQuery() {
        String sparqlQuery = "PREFIX : <http://duckdb.example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        int count = runQueryAndCount(sparqlQuery);
        Assertions.assertEquals(3, count);
    }

    @Test
    void testQueryOnNullableColumn() {
        String sparqlQuery = "PREFIX : <http://duckdb.example.org/>" +
                "SELECT ?s WHERE { ?s :age ?o }";
        int count = runQueryAndCount(sparqlQuery);
        Assertions.assertEquals(2, count);
    }

    @Test
    void testPrimaryKeyConstraint() {
        String sparqlQuery = "PREFIX : <http://duckdb.example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        String query = reformulate(sparqlQuery);
        Assertions.assertFalse(query.contains("DISTINCT"));
    }
}
