package it.unibz.inf.ontop.docker.lightweight.db2;

import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@DB2LightweightTest
public class BindWithFunctionsDB2Test extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/books/books.owl";
    private static final String obdafile = "/books/db2/books-db2.obda";
    private static final String propertiesfile = "/books/db2/books-db2.properties";

    private static OntopOWLEngine REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDB2Test() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterAll
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testHashSHA256() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.2500000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"11.5000000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"16.7500000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"5.0000000000000000000000000\"^^xsd:decimal");
        return expectedValues;
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.50000000000000000000000000\"^^xsd:decimal");
        return expectedValues;
    }


    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5000\"^^xsd:decimal");
        expectedValues.add("\"5.7500\"^^xsd:decimal");
        expectedValues.add("\"6.7000\"^^xsd:decimal");
        expectedValues.add("\"1.5000\"^^xsd:decimal");
        return expectedValues;
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
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.00, 43.00\"^^xsd:string");
        expectedValues.add("\"0.00, 23.00\"^^xsd:string");
        expectedValues.add("\"0.00, 34.00\"^^xsd:string");
        expectedValues.add("\"0.00, 10.00\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");
        expectedValues.add("\"6.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");

        return expectedValues;
    }
}
