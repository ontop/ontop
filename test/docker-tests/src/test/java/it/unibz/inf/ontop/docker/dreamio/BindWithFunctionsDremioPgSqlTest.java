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

public class BindWithFunctionsDremioPgSqlTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/dremio/bind/sparqlBind.owl";
    private static final String obdafile = "/dremio/bind/mapping/sparqlBindDremioPgSql.obda";
    private static final String propertyfile = "/dremio/bind/sparqlBindDremio.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDremioPgSqlTest() throws OWLOntologyCreationException {
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
        expectedValues.add("\"0.150\"^^xsd:decimal");
        expectedValues.add("\"0.200\"^^xsd:decimal");
        expectedValues.add("\"0.200\"^^xsd:decimal");
        expectedValues.add("\"0.250\"^^xsd:decimal");

        return expectedValues;
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1.500\"^^xsd:decimal");
        expectedValues.add("\"8.600\"^^xsd:decimal");
        expectedValues.add("\"6.800\"^^xsd:decimal");
        expectedValues.add("\"5.750\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getDayExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"5\"^^xsd:integer");
        expectedValues.add("\"14\"^^xsd:integer");
        expectedValues.add("\"21\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");

        return expectedValues;
    }

    @Override
    protected List<String> getMonthExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"11\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");

        return expectedValues;
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
        expectedValues.add("\"21.500000\"^^xsd:decimal");
        expectedValues.add("\"11.500000\"^^xsd:decimal");
        expectedValues.add("\"17.000000\"^^xsd:decimal");
        expectedValues.add("\"5.000000\"^^xsd:decimal");
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

    @Ignore("Not yet supported")
    @Test
    public void testContainsFilter() throws Exception {
        super.testContainsFilter();
    }

    @Ignore("Not yet supported")
    @Test
    public void testNow() throws Exception {
        super.testNow();
    }

    @Ignore("Not yet supported")
    @Test
    public void testStr() throws Exception {
        super.testStr();
    }

    @Ignore("Not yet supported")
    @Test
    public void testAndBind() throws Exception {
        super.testAndBind();
    }

    @Ignore("Returns four times null, maybe not supported")
    @Test
    public void testREGEX() throws Exception {
        super.testREGEX();
    }

    @Ignore("Not yet supported")
    @Test
    public void testStrUuid() throws Exception {
        super.testStrUuid();
    }

    @Ignore("Not yet supported")
    @Test
    public void testTZ() throws Exception {
        super.testTZ();
    }

    @Ignore("Not yet supported")
    @Test
    public void testHashSHA256() throws Exception {
        super.testHashSHA256();
    }

    @Ignore("Not yet supported")
    @Test
    public void testBindWithBefore1() throws Exception {
        super.testBindWithBefore1();
    }

    @Ignore("Not yet supported")
    @Test
    public void testAndBindDistinct() throws Exception {
        super.testAndBindDistinct();
    }

    @Ignore("Not yet supported")
    @Test
    public void testBindWithAfter1() throws Exception {
        super.testBindWithAfter1();
    }

    @Ignore("Not yet supported")
    @Test
    public void testBindWithAfter2() throws Exception {
        super.testBindWithAfter2();
    }

    @Ignore("Not yet supported")
    @Test
    public void testOrBind() throws Exception {
        super.testOrBind();
    }

    @Ignore("Not yet supported")
    @Test
    public void testUuid() throws Exception {
        super.testUuid();
    }
}
