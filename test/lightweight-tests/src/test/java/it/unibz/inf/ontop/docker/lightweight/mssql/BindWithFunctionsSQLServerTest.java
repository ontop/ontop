package it.unibz.inf.ontop.docker.lightweight.mssql;

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
public class BindWithFunctionsSQLServerTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/books/books.owl";
    private static final String obdafile = "/books/books.obda";
    private static final String propertiesfile = "/books/mssql/books-mssql.properties";

    private static OntopOWLEngine REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsSQLServerTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
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
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0E-19, 43.0000000000000000000\"^^xsd:string");
        expectedValues.add("\"0E-19, 23.0000000000000000000\"^^xsd:string");
        expectedValues.add("\"0E-19, 34.0000000000000000000\"^^xsd:string");
        expectedValues.add("\"0E-19, 10.0000000000000000000\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1970-11-05T07:50:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T11:30:00\"^^xsd:string");
        expectedValues.add("\"2014-06-05T16:47:52\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06\"^^xsd:string");

        return expectedValues;
    }

    @Ignore("DATETIME does not have an offset. TODO: update the data source (use DATETIME2 instead)")
    @Test
    public void testTZ() throws Exception {
        super.testTZ();
    }

    @Ignore("not supported?")
    @Test
    public void testREGEX() throws Exception {
        super.testREGEX();
    }

    @Ignore("not supported")
    @Test
    public void testREPLACE() throws Exception {
        super.testREPLACE();
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.500000\"^^xsd:decimal");
        return expectedValues;
    }

    @Test
    @Ignore("TODO: support regex")
    @Override
    public void testIRI7() throws Exception {
        super.testIRI7();
    }

    @Ignore("Current MS SQL Server handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() throws Exception {
        super.testDaysBetweenDateMappingInput();
    }
}
