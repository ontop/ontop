package it.unibz.inf.ontop.rdf4j.repository;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class EmployeeTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/employee/employee.obda";
    private static final String SQL_SCRIPT = "/employee/employee.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testMergeLJsWithValues() {
        String sparql = "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX : <http://employee.example.org/voc#>\n" +
                "\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  VALUES ?p { \n" +
                "   <http://employee.example.org/data/person/1> \n " +
                "   <http://employee.example.org/data/person/2> \n" +
                "  } \n" +
                "  OPTIONAL { \n" +
                "      ?p :firstName ?fName . \n" +
                "  }\n" +
                "  OPTIONAL { \n" +
                "     ?p  :lastName ?lastName . \n" +
                "  }\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql);

        assertEquals(1, StringUtils.countMatches(sql, "LEFT OUTER JOIN"));

        int countResults = runQueryAndCount(sparql);
        assertEquals(2, countResults);
    }
}
