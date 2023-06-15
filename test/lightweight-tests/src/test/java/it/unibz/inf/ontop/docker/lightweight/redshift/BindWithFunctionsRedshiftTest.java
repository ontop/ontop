package it.unibz.inf.ontop.docker.lightweight.redshift;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.RedshiftLightweightTest;
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
@RedshiftLightweightTest
public class BindWithFunctionsRedshiftTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/redshift/books-redshift.properties";
    private static final String OBDA_FILE = "/books/redshift/books-redshift.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("Redshift is off by one on the hour count")
    @Override
    @Test
    public void testHoursBetween() {
        super.testHoursBetween();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000000000000000000000000000000\"^^xsd:decimal", "\"5.750000000000000000000000000000000000\"^^xsd:decimal", "\"6.800000000000000000000000000000000000\"^^xsd:decimal",
                "\"1.500000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.5000\"^^xsd:decimal", "\"11.5000\"^^xsd:decimal",
                "\"17.0000\"^^xsd:decimal", "\"5.0000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00+00\"^^xsd:string",
                "\"2011-12-08T11:30:00+00\"^^xsd:string",
                "\"2014-06-05T16:47:52+00\"^^xsd:string",
                "\"2015-09-21T09:23:06+00\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getRoundExpectedValues() {
        //Round leaves entries in same data type, so discount of type DECIMAL remains decimal with 18 digits in the
        //fractional part
        return ImmutableSet.of("\"0E-18, 43\"^^xsd:string", "\"0E-18, 23\"^^xsd:string", "\"0E-18, 34\"^^xsd:string",
                "\"0E-18, 10\"^^xsd:string");
    }

    @Disabled("Redshift is off by one in some results.")
    @Test
    @Override
    public void testDaysBetweenDateTimeMappingInput() {
        super.testDaysBetweenDateTimeMappingInput();
    }

    @Disabled("Redshift is off by one in some results.")
    @Test
    @Override
    public void testDaysBetweenDateTime() {
        super.testDaysBetweenDateTime();
    }

    @Disabled("Redshift does not support UUIDs.")
    @Test
    @Override
    public void testUuid() {
        super.testUuid();
    }

    @Disabled("Redshift does not support UUIDs.")
    @Test
    @Override
    public void testStrUuid() {
        super.testStrUuid();
    }

    @Override
    protected ImmutableSet<String> getDateTruncGroupByExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00+00: 1\"^^xsd:string", "\"2010-01-01T00:00:00+00: 3\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00+00\"^^xsd:dateTime", "\"2011-01-01T00:00:00+00\"^^xsd:dateTime", "\"2014-01-01T00:00:00+00\"^^xsd:dateTime", "\"2015-01-01T00:00:00+00\"^^xsd:dateTime");
    }
}
