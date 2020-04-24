package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.*;


/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest extends AbstractVirtualModeTest {

    private static final String owlFileName = "/mysql/pullOutEq/pullOutEq.ttl";
    private static final String obdaFileName = "/mysql/pullOutEq/pullOutEq.obda";
    private static final String propertyFileName = "/mysql/pullOutEq/pullOutEq.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFileName, obdaFileName, propertyFileName);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }


    private TupleOWLResultSet runLocalQuery(String query) throws OWLException {

        OWLStatement st = createStatement();
        return st.executeSelectQuery(query);
    }

    @Test
    public void test() throws OWLException {
        TupleOWLResultSet  results = runLocalQuery("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :hasId \"3\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}");
        // At least one result
        assertTrue(results.hasNext());
        final OWLBindingSet bindingSet = results.next();
        assertEquals(bindingSet.getOWLIndividual("p").toString(), "<http://example.com/persons/3>");
        assertNull(bindingSet.getOWLLiteral("firstName"));
        assertNull(bindingSet.getOWLLiteral("lastName"));
        assertFalse(results.hasNext());
    }
}
