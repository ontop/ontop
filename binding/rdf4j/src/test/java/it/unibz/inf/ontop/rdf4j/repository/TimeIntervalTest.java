package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;


public class TimeIntervalTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/time-interval/mapping.obda";
    private static final String SQL_FILE = "/time-interval/db.sql";

    @BeforeClass
    public static void before() throws SQLException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        initOBDA(SQL_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        TimeZone.setDefault(null);
        release();
    }

    @Test
    public void timestampConstantIntervalSumTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"2025-02-17T09:50:00\"^^xsd:dateTime + \"P1Y2M3DT4H5M6.5S\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of( "2026-04-20T13:55:06.500Z"));
    }

    @Test
    public void timestampVariableIntervalSumTest() {
        String sparql = "PREFIX : <http://vocabulary.example.org/>\n" +
                "SELECT * WHERE {\n" +
                "    ?p :birthInstant ?birth .\n" +
                "    BIND(?birth + \"PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("1993-01-01T13:30:00.000Z", "1998-02-02T14:35:00.000Z"));
    }

    @Test
    public void intervalDifferenceTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"2025-02-17T09:50:00\"^^xsd:dateTime - \"PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("2025-02-17T09:35:00.000Z"));
    }

    @Test
    public void negativeIntervalTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"2025-02-17T09:50:00\"^^xsd:dateTime + \"-PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("2025-02-17T09:35:00.000Z"));
    }

    @Test
    public void intervalSumArgumentsOrderTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"P1Y2M3DT4H5M6.5S\"^^xsd:duration + \"2025-02-17T09:50:00\"^^xsd:dateTime as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("2026-04-20T13:55:06.500Z"));
    }

    @Test
    public void constantDateIntervalSumTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"2025-02-17\"^^xsd:date + \"PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("2025-02-17"));
    }

    @Test
    public void variableDateIntervalSumTest() {
        String sparql = "PREFIX : <http://vocabulary.example.org/>\n" +
                "SELECT * WHERE {\n" +
                "    ?p :birthDate ?birthDate .\n" +
                "    BIND(?birthDate + \"PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("1993-01-01", "1998-02-02"));
    }

    @Test
    public void multipleIntervalsSumTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"2025-02-17T09:50:00\"^^xsd:dateTime + \"P1Y2M3DT4H5M6.5S\"^^xsd:duration + \"PT15M\"^^xsd:duration as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("2026-04-20T14:10:06.500Z"));
    }

    @Test
    @Ignore("Conversion back to xsd:duration not yet implemented")
    public void multiplyIntervalTest() {
        String sparql = "SELECT * WHERE {\n" +
                "    BIND(\"PT1H15M\"^^xsd:duration * 2 as ?v) \n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("INTERVAL '0 02:30:0' DAY TO SECOND"));
    }
}
