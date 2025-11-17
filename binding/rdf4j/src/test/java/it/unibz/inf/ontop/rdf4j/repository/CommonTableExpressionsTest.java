package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Tests datatype inference for black-box SQL views
 */
public class CommonTableExpressionsTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/cte/cte.obda";
    private static final String SQL_SCRIPT = "/cte/cte.sql";
    private static final String PROPERTIES_FILE = "/cte/cte-on.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testPersonSums() {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :sum ?v . \n" +
                " FILTER (datatype(?v) = xsd:integer)\n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("0", "0"));
        String sql = reformulateIntoNativeQuery(query);
        assertTrue(sql.startsWith("WITH\nONTOP0 AS"));
    }
}

