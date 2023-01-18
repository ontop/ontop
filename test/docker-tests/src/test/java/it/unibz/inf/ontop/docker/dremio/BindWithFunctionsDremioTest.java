package it.unibz.inf.ontop.docker.dremio;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Executed with Dremio over Postgresql
 */

public class BindWithFunctionsDremioTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/dremio/bind/sparqlBind.owl";
    private static final String obdafile = "/dremio/bind/sparqlBindDremio.obda";
    private static final String propertyfile = "/dremio/bind/sparqlBindDremio.properties";

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
                "\"0.200000\"^^xsd:decimal",
                "\"0.250000\"^^xsd:decimal",
                "\"0.200000\"^^xsd:decimal",
                "\"0.150000\"^^xsd:decimal");
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
    protected List<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of(
                "\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getStrExpectedValues() {
        return ImmutableList.of(
                "\"1967-11-05T06:50:00+00:00\"^^xsd:string",
                "\"2011-12-08T11:30:00+00:00\"^^xsd:string",
                "\"2014-07-14T10:47:52+00:00\"^^xsd:string",
                "\"2015-09-21T09:23:06+00:00\"^^xsd:string");
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        return ImmutableList.of(
                "\"8.600000\"^^xsd:decimal",
                "\"5.750000\"^^xsd:decimal",
                "\"6.800000\"^^xsd:decimal",
                "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        return ImmutableList.of(
                "\"6\"^^xsd:integer",
                "\"10\"^^xsd:integer",
                "\"9\"^^xsd:integer",
                "\"11\"^^xsd:integer");
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        return ImmutableList.of(
                "\"21.500000\"^^xsd:decimal",
                "\"11.500000\"^^xsd:decimal",
                "\"17.000000\"^^xsd:decimal",
                "\"5.000000\"^^xsd:decimal");
    }

    @Ignore("Not supported yet ")
    @Test
    @Override
    public void testUuid() {
    }

    @Ignore("Not supported yet ")
    @Test
    @Override
    public void testTZ() {
    }

    @Ignore("Not supported yet ")
    @Test
    @Override
    public void testHashSHA256() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
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
