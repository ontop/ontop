package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@SnowflakeLightweightTest
public class BindWithFunctionsSnowflakeTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/snowflake/books-snowflake.properties";

    @BeforeAll
    public static void before() {
        initOBDA("/books/snowflake/books-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000000\"^^xsd:decimal", "\"5.750000000000\"^^xsd:decimal", "\"6.800000000000\"^^xsd:decimal",
                "\"1.500000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.500000000000\"^^xsd:decimal", "\"11.500000000000\"^^xsd:decimal",
                "\"17.000000000000\"^^xsd:decimal", "\"5.000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00.000Z\"^^xsd:string", "\"2011-12-08T11:30:00.000Z\"^^xsd:string",
                "\"2014-06-05T16:47:52.000Z\"^^xsd:string", "\"2015-09-21T09:23:06.000Z\"^^xsd:string");
    }

    @Disabled("REGEXP_LIKE implicitly anchors a pattern at both ends")
    @Test
    @Override
    public void testREGEX() {
        super.testREGEX();
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000\"^^xsd:decimal");
    }

    @Disabled("Snowflake counts one day more")
    @Test
    @Override
    public void testDaysBetweenDateTime() {
        super.testDaysBetweenDateTime();
    }

    @Disabled("Snowflake counts one day more")
    @Test
    @Override
    public void testDaysBetweenDateTimeMappingInput() {
        super.testDaysBetweenDateTimeMappingInput();
    }

    @Disabled("Snowflake counts one hour more")
    @Test
    @Override
    public void testHoursBetween() {
        super.testHoursBetween();
    }

    @Disabled("Snowflake counts one hour less on one result")
    @Test
    @Override
    public void testSecondsBetweenMappingInput() {
        super.testSecondsBetweenMappingInput();
    }

    @Override
    protected ImmutableSet<String> getDivisionOutputTypeExpectedResults() {
        return ImmutableSet.of("\"3.333333\"^^xsd:decimal");
    }

    @Disabled("Currently Snowflake does not support DATE_TRUNC for the type `DECADE`")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000-0800\"^^xsd:dateTime", "\"2011-01-01T00:00:00.000-0800\"^^xsd:dateTime", "\"2014-01-01T00:00:00.000-0700\"^^xsd:dateTime", "\"2015-01-01T00:00:00.000-0700\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.340000\"^^xsd:decimal");
    }
}
