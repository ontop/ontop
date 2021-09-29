package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class BasicViewPersonNumericFilterTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_basic_views_filter.obda";
    private static final String SQL_SCRIPT = "/person/person_filter.sql";
    private static final String VIEW_FILE = "/person/views/basic_views_filterexp_numeric.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Filter works on numeric values
     */
    @Test
    public void testPersonConcat() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :fullName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("John Doe", "Jane Doe", "Corto Maltese"));
    }
}
