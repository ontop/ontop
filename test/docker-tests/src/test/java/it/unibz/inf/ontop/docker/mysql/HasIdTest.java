package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.*;


/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest extends AbstractVirtualModeTest {

    private static final String owlFileName = "/mysql/pullOutEq/pullOutEq.ttl";
    private static final String obdaFileName = "/mysql/pullOutEq/pullOutEq.obda";
    private static final String propertyFileName = "/mysql/pullOutEq/pullOutEq.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFileName, obdaFileName, propertyFileName);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }


    @Test
    public void test() throws OWLException {
        try (OWLStatement st = createStatement()) {
            TupleOWLResultSet results = st.executeSelectQuery("PREFIX : <http://example.com/vocab#>" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                    "SELECT ?p ?firstName ?lastName " +
                    "WHERE { " +
                    "    ?p :hasId \"3\"^^xsd:int . " +
                    "    OPTIONAL { ?p :firstName ?firstName }" +
                    "    OPTIONAL { ?p :lastName ?lastName }" +
                    "}");
            // At least one result
            assertTrue(results.hasNext());
            OWLBindingSet bindingSet = results.next();
            assertEquals(bindingSet.getOWLIndividual("p").toString(), "<http://example.com/persons/3>");
            assertNull(bindingSet.getOWLLiteral("firstName"));
            assertNull(bindingSet.getOWLLiteral("lastName"));
            assertFalse(results.hasNext());
        }
    }
}
