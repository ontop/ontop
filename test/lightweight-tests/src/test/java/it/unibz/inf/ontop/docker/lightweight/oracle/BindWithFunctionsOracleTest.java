package it.unibz.inf.ontop.docker.lightweight.oracle;

import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */

@OracleLightweightTest
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

    @AfterAll
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    /*
     * Tests for hash functions. Oracle does not support any hash functions if DBMS CRYPTO is not enabled
     */
    @Disabled("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashSHA256() throws Exception {
        super.testHashSHA256();
    }

    @Disabled("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithBefore1() throws Exception {
        super.testBindWithBefore1();
    }

    @Disabled("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithAfter1() throws Exception {
        super.testBindWithAfter1();
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1970-11-05T07:50:00.000000\"^^xsd:string");
        expectedValues.add("\"2011-12-08T11:30:00.000000\"^^xsd:string");
        expectedValues.add("\"2014-06-05T16:47:52.000000\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06.000000\"^^xsd:string");

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

    @Override
    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0:0\"^^xsd:string");
        expectedValues.add("\"0:0\"^^xsd:string");
        expectedValues.add("\"0:0\"^^xsd:string");
        expectedValues.add("\"0:0\"^^xsd:string");

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

    @Disabled("Currently Oracle does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() throws Exception {
        super.testDaysBetweenDateMappingInput();
    }

}
