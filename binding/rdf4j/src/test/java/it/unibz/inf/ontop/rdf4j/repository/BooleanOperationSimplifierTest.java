package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class BooleanOperationSimplifierTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/boolean-operation-simplifier/boolean-operation-simplifier.sql";
    private static final String OBDA_FILE = "/boolean-operation-simplifier/boolean-operation-simplifier.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSimpleQuery() {
        String query = "PREFIX : <http://ontop.example.org/ontology#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?s a :LatestObservation .\n" +
                "?s :hasPeriodInSeconds ?v .\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("3600", "7200"));
    }

    @Test
    public void testQueryMultipleIndividuals() {
        String query = "PREFIX : <http://ontop.example.org/ontology#>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?s1 sosa:madeBySensor ?r .\n" +
                "?s2 sosa:madeBySensor ?r .\n" +
                "?s2 :hasPeriodInSeconds ?v .\n" +
                "FILTER(?s1 != ?s2)\n" +
                "}";

        runQueryAndCompare(query, ImmutableSet.of("3600", "7200"));
    }
}
