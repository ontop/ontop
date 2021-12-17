package it.unibz.inf.ontop.docker.denodo;

import com.google.common.collect.ImmutableList;
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
 * Executed with Denodo over Postgresql.
 * The Docker image and data for the Postgresql DB can be found at:
 * https://github.com/ontop/ontop-dockertests/tree/master/pgsql
 * The parameters to connect to Postgresql from Denodo are in "src/test/resources/pgsql/bind/sparqlBindPostgreSQL.properties"
 */
@Ignore
public class BindWithFunctionsDenodoTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/denodo/bind/sparqlBind.owl";
    private static final String obdafile = "/denodo/bind/sparqlBindDenodo.obda";
    private static final String propertyfile = "/denodo/bind/sparqlBindDenodo.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDenodoTest() throws OWLOntologyCreationException {
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
        expectedValues.add("\"0.2\"^^xsd:decimal");
        expectedValues.add("\"0.25\"^^xsd:decimal");
        expectedValues.add("\"0.2\"^^xsd:decimal");
        expectedValues.add("\"0.15\"^^xsd:decimal");

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
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1967-11-05T07:50:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T12:30:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2014-07-14T12:47:52.000000+02:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T11:23:06.000000+02:00\"^^xsd:string");

        return expectedValues;
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.6000000000000000000000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"5.7500000000000000000000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"6.8000000000000000000000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"1.5000000000000000000000000000000000000000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.50000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"11.50000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"17.00000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"5.00000000000000000000\"^^xsd:decimal");
        return expectedValues;
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
