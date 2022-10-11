package it.unibz.inf.ontop.docker.lightweight.oracle;

import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */

public class BindWithFunctionsOracleTest extends AbstractBindTestWithFunctions {

    private static final String owlfile = "/books/books.owl";
    private static final String obdafile = "/books/books.obda";
    private static final String propertiesfile = "/books/oracle/books-oracle.properties";

    private static OntopOWLEngine REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsOracleTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
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
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1970-11-05T07:50:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T11:30:00.000000+01:00\"^^xsd:string");
        expectedValues.add("\"2014-06-05T16:47:52.000000+02:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06.000000+02:00\"^^xsd:string");

        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.5\"^^xsd:decimal");
        expectedValues.add("\"11.5\"^^xsd:decimal");
        expectedValues.add("\"17\"^^xsd:decimal");
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
        expectedValues.add("\"2:0\"^^xsd:string");
        expectedValues.add("\"1:0\"^^xsd:string");
        expectedValues.add("\"2:0\"^^xsd:string");
        expectedValues.add("\"1:0\"^^xsd:string");

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

    /*
    Due to the timezone, automatically UTC+00 is set, and the timezones are added respectively
     */
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"10\"^^xsd:integer");
        expectedValues.add("\"14\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        return expectedValues;
    }

    /*
     * Accounting for the timezone in the data
     */
    @Override
    protected List<String> getSecondsExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"116806800\"^^xsd:long");
        expectedValues.add("\"1413511200\"^^xsd:long");
        expectedValues.add("\"1492150672\"^^xsd:long");
        expectedValues.add("\"1532991186\"^^xsd:long");

        return expectedValues;
    }

    @Ignore("Currently Oracle handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() throws Exception {
        super.testDaysBetweenDateMappingInput();
    }

}
