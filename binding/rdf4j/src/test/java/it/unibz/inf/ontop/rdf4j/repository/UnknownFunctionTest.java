package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Makes sure Ontop does not complain when the mapping entry with the unknown function is not queried
 */
public class UnknownFunctionTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/unknown-function/unknown-fct-create.sql";
    private static final String OBDA_FILE = "/unknown-function/unknown-fct.obda";

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

    /**
     * Test unrecognized but valid SQL function (XMLATTR)
     */
    @Test
    public void testFakeProperty() {
        String query = "SELECT *\n" +
                "WHERE {\n" +
                "  ?source <http://example.org/xmlProperty> ?v .\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

}
