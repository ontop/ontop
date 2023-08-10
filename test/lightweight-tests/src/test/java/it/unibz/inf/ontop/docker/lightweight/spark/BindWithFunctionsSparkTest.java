package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@SparkSQLLightweightTest
public class BindWithFunctionsSparkTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/spark/books-spark.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.500000\"^^xsd:decimal", "\"11.500000\"^^xsd:decimal",
                "\"17.000000\"^^xsd:decimal", "\"5.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000\"^^xsd:decimal", "\"5.750000\"^^xsd:decimal", "\"6.800000\"^^xsd:decimal",
                "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00.000+00:00\"^^xsd:string",
                "\"2011-12-08T11:30:00.000+00:00\"^^xsd:string",
                "\"2014-06-05T16:47:52.000+00:00\"^^xsd:string",
                "\"2015-09-21T09:23:06.000+00:00\"^^xsd:string");
    }

    @Override
    protected ImmutableMultiset<String> getSecondsExpectedValues() {
        return ImmutableMultiset.of("\"52.000000\"^^xsd:decimal", "\"0.000000\"^^xsd:decimal", "\"6.000000\"^^xsd:decimal",
                "\"0.000000\"^^xsd:decimal");
    }

    @Disabled("Spark SQL does not support OFFSET, Databricks does")
    @Test
    public void testOffset1() {
        super.testOffset1();
    }

    @Disabled("Spark SQL does not support OFFSET, Databricks does")
    @Test
    public void testOffset2() {
        super.testOffset2();
    }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testMilliSecondsBetween() { super.testMilliSecondsBetween(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testWeeksBetweenDateTime() { super.testWeeksBetweenDateTime(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testDaysBetweenDateTime() { super.testDaysBetweenDateTime(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testDaysBetweenDateTimeMappingInput() { super.testDaysBetweenDateTimeMappingInput(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testDaysBetweenDateMappingInput() { super.testDaysBetweenDateMappingInput(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testHoursBetween() { super.testHoursBetween(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testMinutesBetween() { super.testMinutesBetween(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testSecondsBetween() { super.testSecondsBetween(); }

    @Disabled("Duration functions currently not supported for Spark SQL")
    @Test
    public void testSecondsBetweenMappingInput() { super.testSecondsBetweenMappingInput(); }

    @Override
    protected ImmutableSet<String> getDivisionOutputTypeExpectedResults() {
        return ImmutableSet.of("\"3.3333333333333335\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getExtraDateExtractionsExpectedValues() {
        return ImmutableSet.of("\"3 21 201 2 23 52000.000000 52000000\"^^xsd:string", "\"3 21 201 4 49 0.000000 0\"^^xsd:string",
                "\"3 21 201 3 39 6000.000000 6000000\"^^xsd:string", "\"2 20 197 4 45 0.000000 0\"^^xsd:string");
    }

    @Disabled("Currently SparkSQL does not support DATE_TRUNC for the type `DECADE`")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000+00:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00.000+00:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00.000+00:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00.000+00:00\"^^xsd:dateTime");
    }

    @Test
    public void testDateTruncFailsDialectDoesNotSupport() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX dc:  <http://purl.org/dc/elements/1.1/>"
                + "PREFIX  fn: <https://w3id.org/obda/functions#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT (fn:dateTrunc(?date, 'century') AS ?v) WHERE \n"
                + "{  "
                + "   ?x ns:pubYear ?date .\n"
                + "} ORDER BY ?date";

        var error = assertThrows(QueryEvaluationException.class, () -> this.runQuery(query));
        assertEquals("it.unibz.inf.ontop.exception.OntopReformulationException: java.lang.IllegalArgumentException: SparkSQL does not support DATE_TRUNC on century.", error.getMessage());
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.340000\"^^xsd:decimal");
    }
}
