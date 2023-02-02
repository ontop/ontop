package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

public class NoIRISafeEncodingPersonTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/person/person-mirror.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String LENS_FILE = "/person/views/no-iri-encoding-lens.json";

    private static final String PROPERTIES_FILE = "/person/full-native-sql-query.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCountry() {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT ?v \n" +
                "WHERE {\n" +
                " ?p a :Person ; :country ?v \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("http://person.example.org/country/it"));
        String sql = reformulateIntoNativeQuery(query);
        assertFalse("R2RML IRI safe encoding still present in " + sql, sql.toLowerCase().contains("replace"));
    }

    @Test
    public void testCountry2() {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT ?v ?code \n" +
                "WHERE {\n" +
                " ?p a :Person ; :country ?v . " +
                " ?v :code ?code \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("http://person.example.org/country/it"));
        String sql = reformulateIntoNativeQuery(query);
        assertFalse("R2RML IRI safe encoding still present in " + sql, sql.toLowerCase().contains("replace"));
    }
}
