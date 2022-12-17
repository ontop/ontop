package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
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
@SnowflakeLightweightTest
public class BindWithFunctionsSnowflakeTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/snowflake/books-snowflake.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA("/books/snowflake/books-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getAbsExpectedValues() {
        return ImmutableList.of("\"8.600000000000\"^^xsd:decimal", "\"5.750000000000\"^^xsd:decimal", "\"6.800000000000\"^^xsd:decimal",
                "\"1.500000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getDivideExpectedValues() {
        return ImmutableList.of("\"21.500000000000\"^^xsd:decimal", "\"11.500000000000\"^^xsd:decimal",
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
}
