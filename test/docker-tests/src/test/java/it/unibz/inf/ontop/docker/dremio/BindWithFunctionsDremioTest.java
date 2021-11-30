package it.unibz.inf.ontop.docker.dremio;

import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Executed with Dremio over Postgresql
 */
//@Ignore
public class BindWithFunctionsDremioTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/dremio/bind/sparqlBind.owl";
    private static final String obdafile = "/dremio/bind/sparqlBindDremio.obda";
    private static final String propertyfile = "/dremio/bind/sparqlBindDremio.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDremioTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertyfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 43\"^^xsd:string");
        expectedValues.add("\"0, 23\"^^xsd:string");
        expectedValues.add("\"0, 34\"^^xsd:string");
        expectedValues.add("\"0, 10\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getDatatypeExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.200000\"^^xsd:decimal");
        expectedValues.add("\"0.250000\"^^xsd:decimal");
        expectedValues.add("\"0.200000\"^^xsd:decimal");
        expectedValues.add("\"0.150000\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getMonthExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");

        return expectedValues;
    }

    @Override
    protected List<String> getDayExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"14\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");
        expectedValues.add("\"21\"^^xsd:integer");
        expectedValues.add("\"5\"^^xsd:integer");

        return expectedValues;
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.500000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1967-11-05T06:50:00+00:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T11:30:00+00:00\"^^xsd:string");
        expectedValues.add("\"2014-07-14T10:47:52+00:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06+00:00\"^^xsd:string");

        return expectedValues;
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.600000\"^^xsd:decimal");
        expectedValues.add("\"5.750000\"^^xsd:decimal");
        expectedValues.add("\"6.800000\"^^xsd:decimal");
        expectedValues.add("\"1.500000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"10\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.500000\"^^xsd:decimal");
        expectedValues.add("\"11.500000\"^^xsd:decimal");
        expectedValues.add("\"17.000000\"^^xsd:decimal");
        expectedValues.add("\"5.000000\"^^xsd:decimal");
        return expectedValues;
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
