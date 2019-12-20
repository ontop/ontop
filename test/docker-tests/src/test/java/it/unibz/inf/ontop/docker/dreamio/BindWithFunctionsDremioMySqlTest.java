package it.unibz.inf.ontop.docker.dreamio;

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


public class BindWithFunctionsDremioMySqlTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/dremio/bind/sparqlBind.owl";
    private static final String obdafile = "/dremio/bind/mapping/sparqlBindDremioMySql.obda";
    private static final String propertyfile = "/dremio/bind/sparqlBindDremio.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDremioMySqlTest() throws OWLOntologyCreationException {
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
    protected List<String> getDatatypeExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.15\"^^xsd:decimal");
        expectedValues.add("\"0.20\"^^xsd:decimal");
        expectedValues.add("\"0.20\"^^xsd:decimal");
        expectedValues.add("\"0.25\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1.5000\"^^xsd:decimal");
        expectedValues.add("\"8.5000\"^^xsd:decimal");
        expectedValues.add("\"6.7000\"^^xsd:decimal");
        expectedValues.add("\"5.7500\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        return expectedValues;
    }

    @Override
    protected List<String> getMonthExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");

        return expectedValues;
    }

    @Ignore("not yet supported")
    @Test
    public void testREGEX() throws Exception {
        super.testREGEX();
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0, 10\"^^xsd:string");
        expectedValues.add("\"0, 43\"^^xsd:string");
        expectedValues.add("\"0, 34\"^^xsd:string");
        expectedValues.add("\"0, 23\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.2500000000000\"^^xsd:decimal");
        expectedValues.add("\"11.5000000000000\"^^xsd:decimal");
        expectedValues.add("\"16.7500000000000\"^^xsd:decimal");
        expectedValues.add("\"5.0000000000000\"^^xsd:decimal");
        return expectedValues;
    }


}
