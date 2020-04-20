package it.unibz.inf.ontop.docker.postgres;

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

public class BindWithFunctionsDenodoTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/denodo/bind/sparqlBind.owl";
    private static final String obdafile = "/denodo/bind/sparqlBindDenodo.obda";
    private static final String propertiesfile = "/denodo/bind/sparqlBindDenodo.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDenodoTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.6\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.8\"^^xsd:decimal");
        expectedValues.add("\"1.50\"^^xsd:decimal");
        return expectedValues;
    }

    @Ignore("Please enable pgcrypto (CREATE EXTENSION pgcrypto")
    @Test
    @Override
    public void testHashSHA256() throws Exception {
        super.testHashSHA256();
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

    /**
     * TODO: re-ajust the DB entries (34 instead of 33.5, 23 instead of 22.5)
     */
    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.5000000000000000\"^^xsd:decimal");
        expectedValues.add("\"11.5000000000000000\"^^xsd:decimal");
        expectedValues.add("\"17.0000000000000000\"^^xsd:decimal");
        expectedValues.add("\"5.0000000000000000\"^^xsd:decimal");
        return expectedValues;
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
    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"02:00\"^^xsd:string");
        expectedValues.add("\"01:00\"^^xsd:string");
        expectedValues.add("\"02:00\"^^xsd:string");
        expectedValues.add("\"01:00\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1967-11-05T07:50:00+01:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T12:30:00+01:00\"^^xsd:string");
        expectedValues.add("\"2014-07-14T12:47:52+02:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T11:23:06+02:00\"^^xsd:string");

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
    /**
     * Denodo exposes dates relative to the local timezone (which can be set in the query).
     * So the original format of the date is lost.
     */
    public void testTZ() {
    }


    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testTZ() for an explanation
     */
    public void testHours() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testTZ() for an explanation
     */
    public void testDay() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testTZ() for an explanation
     */
    public void testMonth() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    /**
     * See testTZ() for an explanation
     */
    public void testYear() {
    }
}
