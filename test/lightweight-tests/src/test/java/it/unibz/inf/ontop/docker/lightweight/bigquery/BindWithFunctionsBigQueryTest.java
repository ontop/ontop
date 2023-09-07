package it.unibz.inf.ontop.docker.lightweight.bigquery;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.BigQueryLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@BigQueryLightweightTest
public class BindWithFunctionsBigQueryTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/bigquery/books-bigquery.properties";
    private static final String OBDA_FILE = "/books/bigquery/books-bigquery.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("BigQuery does not support SHA384.")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Disabled("BigQuery does not support week-based timestamp diffs.")
    @Test
    @Override
    public void testWeeksBetweenDate() {
        super.testWeeksBetweenDate();
    }

    @Disabled("BigQuery does not support week-based timestamp diffs.")
    @Test
    @Override
    public void testWeeksBetweenDateTime() {
        super.testWeeksBetweenDateTime();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000\"^^xsd:decimal", "\"5.750000000\"^^xsd:decimal", "\"6.800000000\"^^xsd:decimal",
                "\"1.500000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.500000000\"^^xsd:decimal", "\"11.500000000\"^^xsd:decimal",
                "\"17.000000000\"^^xsd:decimal", "\"5.000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getFloorExpectedValues() {
        return ImmutableList.of("\"0.000000000\"^^xsd:decimal", "\"0.000000000\"^^xsd:decimal", "\"0.000000000\"^^xsd:decimal", "\"0.000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getDatatypeExpectedValues() {
        return ImmutableMultiset.of("\"0.200000000\"^^xsd:decimal", "\"0.250000000\"^^xsd:decimal", "\"0.200000000\"^^xsd:decimal",
                "\"0.150000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getCeilExpectedValues() {
        return ImmutableList.of("\"1.000000000\"^^xsd:decimal", "\"1.000000000\"^^xsd:decimal", "\"1.000000000\"^^xsd:decimal", "\"1.000000000\"^^xsd:decimal");
    }


    @Test
    public void testIn1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.15\"^^xsd:decimal) \n"
                + "         (\"0.25\"^^xsd:decimal) } \n"
                + "   FILTER(?v IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.150000000\"^^xsd:decimal",
                "\"0.250000000\"^^xsd:decimal"));
    }

    @Test
    public void testIn2() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   FILTER(?v IN (\"0.15\"^^xsd:decimal, \n"
                + "                     \"0.25\"^^xsd:decimal)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.150000000\"^^xsd:decimal",
                "\"0.250000000\"^^xsd:decimal"));
    }

    @Test
    public void testNotIn1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.20\"^^xsd:decimal) } \n"
                + "   FILTER(?v NOT IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of( "\"0.150000000\"^^xsd:decimal", "\"0.250000000\"^^xsd:decimal"));
    }

    @Test
    public void testNotIn2() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   FILTER(?v NOT IN (\"0.20\"^^xsd:decimal)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.150000000\"^^xsd:decimal", "\"0.250000000\"^^xsd:decimal"));
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00+00:00\"^^xsd:string",
                "\"2011-12-08T11:30:00+00:00\"^^xsd:string",
                "\"2014-06-05T16:47:52+00:00\"^^xsd:string",
                "\"2015-09-21T09:23:06+00:00\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getRoundExpectedValues() {
        //Round leaves entries in same data type, so discount of type DECIMAL remains decimal with 18 digits in the
        //fractional part
        return ImmutableSet.of("\"0.000000000, 43\"^^xsd:string", "\"0.000000000, 23\"^^xsd:string", "\"0.000000000, 34\"^^xsd:string",
                "\"0.000000000, 10\"^^xsd:string");
    }

    @Disabled("Argument have incorrect types, causing a problem.")
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }

    @Override
    protected ImmutableSet<String> getDivisionOutputTypeExpectedResults() {
        return ImmutableSet.of("\"3.3333333333333335\"^^xsd:decimal");
    }

    @Disabled("Currently Trino does not support DATE_TRUNC for the type `DECADE`")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00+00:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00+00:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00+00:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00+00:00\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.340000000\"^^xsd:decimal");
    }
}
