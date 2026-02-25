package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexCaseOptimizationTest extends AbstractRDF4JTest {
    private static final String DB_FILE = "/employee/employee.sql";
    private static final String OBDA_FILE = "/employee/employee.obda";

    @BeforeClass
    public static void before() throws SQLException, IOException {
        initOBDA(DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testLCase() {
        String sparql = "PREFIX ex: <http://employee.example.org/voc#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?v ex:firstName ?name.\n" +
                "FILTER (REGEX(LCASE(?name), \"ger\", \"i\"))\n" +
                "}";
        String sql = reformulateIntoNativeQuery(sparql);
        assertFalse(sql.contains("LOWER"));

        runQueryAndCompare(sparql, ImmutableSet.of("http://employee.example.org/data/person/1"));
    }

    @Test
    public void testUCase() {
        String sparql = "PREFIX ex: <http://employee.example.org/voc#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?v ex:firstName ?name.\n" +
                "FILTER (REGEX(UCASE(?name), \"GER\", \"i\"))\n" +
                "}";
        String sql = reformulateIntoNativeQuery(sparql);
        assertFalse(sql.contains("UPPER"));

        runQueryAndCompare(sparql, ImmutableSet.of("http://employee.example.org/data/person/1"));
    }

    @Test
    public void testMixedCases() {
        String sparql = "PREFIX ex: <http://employee.example.org/voc#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?v ex:firstName ?name.\n" +
                "FILTER (REGEX(UCASE(?name), LCASE(\"Ger\"), \"i\"))\n" +
                "}";
        String sql = reformulateIntoNativeQuery(sparql);
        assertFalse(sql.contains("LOWER") || sql.contains("UPPER"));

        runQueryAndCompare(sparql, ImmutableSet.of("http://employee.example.org/data/person/1"));
    }

    @Test
    public void testNonCaseSensitiveRegex() {
        String sparql = "PREFIX ex: <http://employee.example.org/voc#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?v ex:firstName ?name.\n" +
                "FILTER (REGEX(LCASE(?name), \"Roger\"))\n" +
                "}";
        String sql = reformulateIntoNativeQuery(sparql);
        assertTrue(sql.contains("LOWER"));

        runQueryAndCompare(sparql, ImmutableSet.of());
    }

    @Test
    public void testMultipleFlags() {
        String sparql = "PREFIX ex: <http://employee.example.org/voc#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?v ex:firstName ?name.\n" +
                "FILTER (REGEX(LCASE(?name), \"ger\", \"is\"))\n" +
                "}";
        String sql = reformulateIntoNativeQuery(sparql);
        assertFalse(sql.contains("LOWER"));

        runQueryAndCompare(sparql, ImmutableSet.of("http://employee.example.org/data/person/1"));
    }
}
