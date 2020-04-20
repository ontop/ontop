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
}
