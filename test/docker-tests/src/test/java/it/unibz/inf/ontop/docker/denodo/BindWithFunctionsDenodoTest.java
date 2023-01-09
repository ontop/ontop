package it.unibz.inf.ontop.docker.denodo;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Executed with Denodo over Postgresql.
 * The Docker image and data for the Postgresql DB can be found at:
 * <a href="https://github.com/ontop/ontop-dockertests/tree/master/pgsql">github.com/ontop/ontop-dockertests/tree/master/pgsql</a>
 * The parameters to connect to Postgresql from Denodo are in "src/test/resources/pgsql/bind/sparqlBindPostgreSQL.properties"
 */
@Ignore
public class BindWithFunctionsDenodoTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/denodo/bind/sparqlBind.owl";
    private static final String obdafile = "/denodo/bind/sparqlBindDenodo.obda";
    private static final String propertyfile = "/denodo/bind/sparqlBindDenodo.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertyfile);
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        return ImmutableList.of(
                "\"0, 43\"^^xsd:string",
                "\"0, 23\"^^xsd:string",
                "\"0, 34\"^^xsd:string",
                "\"0, 10\"^^xsd:string");
    }

    @Override
    protected List<String> getDatatypeExpectedValues() {
        return ImmutableList.of(
                "\"0.2\"^^xsd:decimal",
                "\"0.25\"^^xsd:decimal",
                "\"0.2\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Override
    protected List<String> getMonthExpectedValues() {
        return ImmutableList.of(
                "\"7\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"9\"^^xsd:integer",
                "\"11\"^^xsd:integer");
    }

    @Override
    protected List<String> getDayExpectedValues() {
        return ImmutableList.of(
                "\"14\"^^xsd:integer",
                "\"8\"^^xsd:integer",
                "\"21\"^^xsd:integer",
                "\"5\"^^xsd:integer");
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        return ImmutableList.of(
                "\"12\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"11\"^^xsd:integer",
                "\"7\"^^xsd:integer");
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getStrExpectedValues() {
        return ImmutableList.of(
                "\"1967-11-05T07:50:00.000000+01:00\"^^xsd:string",
                "\"2011-12-08T12:30:00.000000+01:00\"^^xsd:string",
                "\"2014-07-14T12:47:52.000000+02:00\"^^xsd:string",
                "\"2015-09-21T11:23:06.000000+02:00\"^^xsd:string");
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        return ImmutableList.of(
                "\"8.6000000000000000000000000000000000000000\"^^xsd:decimal",
                "\"5.7500000000000000000000000000000000000000\"^^xsd:decimal",
                "\"6.8000000000000000000000000000000000000000\"^^xsd:decimal",
                "\"1.5000000000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        return ImmutableList.of(
                "\"21.50000000000000000000\"^^xsd:decimal",
                "\"11.50000000000000000000\"^^xsd:decimal",
                "\"17.00000000000000000000\"^^xsd:decimal",
                "\"5.00000000000000000000\"^^xsd:decimal");
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * Regex in SELECT clause not supported
     */
    public void testREGEX() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * Denodo cannot parse a logical AND in the ORDER BY clause
     */
    public void testAndBind() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testAndBind() for an explanation
     */
    public void testAndBindDistinct() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testAndBind() for an explanation
     */
    public void testOrBind() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHashSHA256() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testWeeksBetweenDate() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testDaysBetweenDate() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testWeeksBetweenDateTime() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testDaysBetweenDateTime() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testDaysBetweenDateTimeMappingInput() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testDaysBetweenDateMappingInput() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testHoursBetween() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testMinutesBetween() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testSecondsBetween() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testSecondsBetweenMappingInput() {
    }

    @Test
    @Ignore("Test not applicable")
    @Override
    public void testMilliSeconds() {
    }
}
