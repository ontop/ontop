package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
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

    @Test
    public void testUnboundGroupBy1() {
        String sparql = "SELECT DISTINCT ?subject ?dataset ?datasetLabel ?score WHERE {\n" +
                "  ?subject a ?type.\n" +
                "?subject ?keyProp ?key.\n" +
                "FILTER ISLITERAL(?key)\n" +
                "BIND(IF(STRLEN(?key) > STRLEN(\"stu\"),STRLEN(\"stu\") - STRLEN(?key),IF(STRLEN(?key) < STRLEN(\"stu\"),STRLEN(?key) - STRLEN(\"stu\"),\"0\"))  as ?score)\n" +
                "BIND(?key as ?snippet_private)\n" +
                "FILTER REGEX(LCASE(STR(?key)), LCASE(\"stu\"), \"i\").\n" +
                "\n" +
                "  FILTER(ISIRI(?subject))\n" +
                "}\n" +
                "GROUP BY ?subject ?dataset ?datasetLabel ?score\n" +
                "ORDER BY DESC (?score)\n" +
                "LIMIT 10\n";

        int countResults = runQueryAndCount(sparql);
        assertEquals(0, countResults);
    }

    @Test
    public void testUnboundGroupBy2() {
        String sparql = "SELECT DISTINCT ?dataset ?datasetLabel WHERE {\n" +
                "  ?subject a ?type.\n" +
                "?subject ?keyProp ?key.\n" +
                "FILTER ISLITERAL(?key)\n" +
                "FILTER REGEX(LCASE(STR(?key)), LCASE(\"stu\"), \"i\").\n" +
                "\n" +
                "  FILTER(ISIRI(?subject))\n" +
                "}\n" +
                "GROUP BY ?dataset ?datasetLabel\n" +
                "LIMIT 10\n";

        int countResults = runQueryAndCount(sparql);
        assertEquals(1, countResults);
    }

    @Test
    public void testUnboundGroupBy3() {
        String sparql = "SELECT ?dataset ?datasetLabel (COUNT(*) AS ?v) WHERE {\n" +
                "  ?subject a ?type.\n" +
                "?subject ?keyProp ?key.\n" +
                "FILTER ISLITERAL(?key)\n" +
                "FILTER REGEX(LCASE(STR(?key)), LCASE(\"stu\"), \"i\").\n" +
                "\n" +
                "  FILTER(ISIRI(?subject))\n" +
                "}\n" +
                "GROUP BY ?dataset ?datasetLabel\n" +
                "LIMIT 10\n";

        int countResults = runQueryAndCount(sparql);
        assertEquals(1, countResults);
        runQueryAndCompare(sparql, ImmutableSet.of("0"));
    }
}
