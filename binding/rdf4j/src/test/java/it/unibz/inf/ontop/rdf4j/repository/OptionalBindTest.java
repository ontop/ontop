package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * See https://github.com/ontop/ontop/issues/283
 */
public class OptionalBindTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/opt-bind/opt-bind-create.sql";
    private static final String OBDA_FILE = "/opt-bind/opt-bind.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testBindLeftOptional() {
        String query = "SELECT *\n" +
                "WHERE {\n" +
                "  BIND( <http://example.org/Individual1> AS ?source )\n" +
                "  OPTIONAL { ?source rdfs:label ?label . }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testLeftValuesOptional() {
        String query = "SELECT *\n" +
                "WHERE {\n" +
                "  VALUES ?source { <http://example.org/Individual1> }\n" +
                "  OPTIONAL { ?source rdfs:label ?label . }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

    @Test
    public void testOptionalWithoutLeft() {
        String query = "SELECT *\n" +
                "WHERE {\n" +
                "  OPTIONAL { <http://example.org/Individual1> rdfs:label ?l . }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }


}
