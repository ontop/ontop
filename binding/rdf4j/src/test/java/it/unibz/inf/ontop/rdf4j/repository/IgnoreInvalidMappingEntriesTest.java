package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class IgnoreInvalidMappingEntriesTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/ignore-invalid-mapping-entries/test.obda";
    private static final String SQL_SCRIPT = "/ignore-invalid-mapping-entries/test.sql";
    private static final String PROPERTY_FILE = "/ignore-invalid-mapping-entries/ignore-invalid.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTY_FILE, null);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCorrectNumberOfIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Animal . \n" +
                "}";
        assertEquals(8, runQueryAndCount(query));
    }

    @Test
    public void testCorrectNumberOfPartiallyIncorrectIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Rabbit . \n" +
                "}";
        assertEquals(2, runQueryAndCount(query));
    }

    @Test
    public void testCorrectNumberOfFullyIncorrectIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Turtle . \n" +
                "}";
        assertEquals(0, runQueryAndCount(query));
    }
}
