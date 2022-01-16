package it.unibz.inf.ontop.docker.mysql;


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
public class BindWithFunctionsMySQLTest extends AbstractBindTestWithFunctions {

	private static final String owlfile = "/mysql/bindTest/sparqlBind.owl";
    private static final String obdafile = "/mysql/bindTest/sparqlBindMySQL.obda";
    private static final String propertyfile = "/mysql/bindTest/sparqlBindMySQL.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsMySQLTest() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertyfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }
    

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
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
    protected List<String> getYearExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"2015\"^^xsd:integer");
        expectedValues.add("\"1970\"^^xsd:integer");

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
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1970-11-05T06:50:00+00:00\"^^xsd:string");
        expectedValues.add("\"2011-12-08T11:30:00+00:00\"^^xsd:string");
        expectedValues.add("\"2014-06-05T16:47:52+00:00\"^^xsd:string");
        expectedValues.add("\"2015-09-21T07:23:06+00:00\"^^xsd:string");

        return expectedValues;
    }

    @Ignore("not supported")
    @Test
    public void testREPLACE() throws Exception {
        super.testREPLACE();
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.500000000000000000000000000000\"^^xsd:decimal");
        return expectedValues;
    }

    @Ignore("Not supported by MySQL < 8.0.2. TODO: enable it after updating the DB")
    @Test
    @Override
    public void testBNODE0() throws Exception {
        super.testBNODE0();
    }

    @Ignore("Not supported by MySQL < 8.0.2. TODO: enable it after updating the DB")
    @Test
    @Override
    public void testBNODE1() throws Exception {
        super.testBNODE1();
    }

    @Override
    protected List<String> getDaysExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16360\"^^xsd:long");
        expectedValues.add("\"17270\"^^xsd:long");
        expectedValues.add("\"17743\"^^xsd:long");
        expectedValues.add("\"1352\"^^xsd:long");

        return expectedValues;
    }

    protected List<String> getDaysDTExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16360\"^^xsd:long");
        expectedValues.add("\"17270\"^^xsd:long");
        expectedValues.add("\"17742\"^^xsd:long");
        expectedValues.add("\"1351\"^^xsd:long");

        return expectedValues;
    }

    @Override
    protected List<String> getSecondsExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1413511200\"^^xsd:long");
        expectedValues.add("\"1492154272\"^^xsd:long");
        expectedValues.add("\"1532987586\"^^xsd:long");
        expectedValues.add("\"116803200\"^^xsd:long");

        return expectedValues;
    }

}
