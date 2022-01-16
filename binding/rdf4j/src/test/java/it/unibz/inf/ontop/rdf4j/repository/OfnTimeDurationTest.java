package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Time extension - duration arithmetic
 * Tests for both date and datetime
 */
public class OfnTimeDurationTest extends AbstractRDF4JTest {

    // Any input will do since we test constants
    private static final String OBDA_FILE = "/empty.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/multityped-facts.ttl";
    private static final String PROPERTIES_FILE = "/destination/dest-no-tbox.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testWeeksBetweenDate() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("3538"));
    }

    @Test
    public void testDaysBetweenDate() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("24767"));
    }

    @Test
    public void testWeeksBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("3538"));
    }

    @Test
    public void testDaysBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("24766"));
    }

    @Test
    public void testDaysBetweenMixedInput() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("24767"));
    }

    @Test
    public void testDaysBetweenMixedInput2() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("24766"));
    }

    @Test
    public void testHoursBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:hoursBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("594407"));
    }

    @Test
    public void testMinutesBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:minutesBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("35664450"));
    }

    @Test
    public void testSecondsBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("2139867000"));
    }

    @Test
    public void testMilliSecondsBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:millisBetween(?start, ?end) AS ?v)\n"
                + "}";

        runQueryAndCompare(query, ImmutableSet.of("2139867000000"));
    }
}

