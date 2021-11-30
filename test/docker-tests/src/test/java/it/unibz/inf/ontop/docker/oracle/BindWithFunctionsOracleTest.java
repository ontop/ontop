package it.unibz.inf.ontop.docker.oracle;


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
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */

public class BindWithFunctionsOracleTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/oracle/bindTest/sparqlBind.owl";
    private static final String obdafile = "/oracle/bindTest/sparqlBindOracle.obda";
    private static final String propertiesfile = "/oracle/oracle.properties";
    
    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsOracleTest() throws OWLOntologyCreationException {
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
        expectedValues.add("\"8.5\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.7\"^^xsd:decimal");
        expectedValues.add("\"1.5\"^^xsd:decimal");
        return expectedValues;
    }

    /*
	 * Tests for hash functions. Oracle does not support any hash functions if DBMS CRYPTO is not enabled
	 */
    @Ignore("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashSHA256() throws Exception {
        super.testHashSHA256();
    }

    @Ignore("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithBefore1() throws Exception {
        super.testBindWithBefore1();
    }

    @Ignore("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithAfter1() throws Exception {
        super.testBindWithAfter1();
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"10\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.25\"^^xsd:decimal");
        expectedValues.add("\"11.5\"^^xsd:decimal");
        expectedValues.add("\"16.75\"^^xsd:decimal");
        expectedValues.add("\"5\"^^xsd:decimal");
        return expectedValues;    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 43\"^^xsd:string");
        expectedValues.add("\"0, 23\"^^xsd:string");
        expectedValues.add("\"0, 34\"^^xsd:string");
        expectedValues.add("\"0, 10\"^^xsd:string");
        return expectedValues;
    }

    //Note: in specification of SPARQL function if the string doesn't contain the specified string empty string has to be returned,
    //here instead return null value
    @Override
    protected List<String> getBindWithAfter1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add(null);
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");

        return expectedValues;
    }

    //Note: in specification of SPARQL function if the string doesn't contain the specified string empty string has to be returned,
    //here instead return null value

    @Override
    protected List<String> getBindWithBefore1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add(null);  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add(null);
        expectedValues.add("\"The Logic Book: Introduc\"@en");

        return expectedValues;
    }

    @Override
    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8:0\"^^xsd:string");
        expectedValues.add("\"1:0\"^^xsd:string");
        expectedValues.add("\"0:0\"^^xsd:string");
        expectedValues.add("\"1:0\"^^xsd:string");

        return expectedValues;
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1967-11-05T07:50:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T12:30:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2014-06-05T18:47:52.000000+08:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06.000000+00:00\"^^xsd:string");

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
    protected List<String> getDaysDTExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16360\"^^xsd:long");
        expectedValues.add("\"17270\"^^xsd:long");
        expectedValues.add("\"17743\"^^xsd:long");
        expectedValues.add("\"255\"^^xsd:long");

        return expectedValues;
    }

    @Override
    protected List<String> getSecondsExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1413514800\"^^xsd:long");
        expectedValues.add("\"1492136272\"^^xsd:long");
        expectedValues.add("\"1532998386\"^^xsd:long");
        expectedValues.add("\"22112400\"^^xsd:long");

        return expectedValues;
    }

    @Ignore("Current Oracle handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() throws Exception {
        super.testDaysBetweenDateMappingInput();
    }

}
