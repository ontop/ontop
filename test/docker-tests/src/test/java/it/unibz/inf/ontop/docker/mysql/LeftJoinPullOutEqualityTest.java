package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

/**
 * TODO: describe
 */
public class LeftJoinPullOutEqualityTest extends AbstractVirtualModeTest {

    private static final String owlFileName = "/mysql/pullOutEq/pullOutEq.ttl";
    private static final String obdaFileName = "/mysql/pullOutEq/pullOutEq.obda";
    private static final String propertyFileName = "/mysql/pullOutEq/pullOutEq.properties";

    private static OntopOWLEngine REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() {
        REASONER = createReasoner(owlFileName, obdaFileName, propertyFileName);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    @Test
    public void testFlatLeftJoins() throws  OWLException {
        countResults(1, "PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}");
    }

    @Test
    public void testNestedLeftJoins() throws  OWLException {
        countResults(1, "PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName " +
                "               OPTIONAL { ?p :lastName ?lastName }" +
                "    }" +
                "}");
    }

    @Test
    public void testJoinAndFlatLeftJoins() throws  OWLException {
        countResults(1, "PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :gender ?g . " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    FILTER (str(?g) = \"F\") " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}");
    }

    @Test
    public void testBasic() throws OWLException {
        countResults(1, "PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "}");
    }
}
