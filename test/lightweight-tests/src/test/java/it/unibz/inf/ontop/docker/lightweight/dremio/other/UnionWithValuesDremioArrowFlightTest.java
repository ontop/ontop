package it.unibz.inf.ontop.docker.lightweight.dremio.other;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DremioLightweightTest
public class UnionWithValuesDremioArrowFlightTest extends AbstractDockerRDF4JTest {

    private static final String PROPERTIES_FILE = "/books/dremio/books-dremio-arrowflight.properties";
    private static final String OBDA_FILE = "/books/dremio/books-dremio.obda";
    private static final String OWL_FILE = "/books/dremio/ontology-with-facts.ttl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void test() {
        String query = "SELECT * WHERE { ?s ?p ?o. }";
        assertNotEquals(0, runQueryAndCount(query));
    }
}
