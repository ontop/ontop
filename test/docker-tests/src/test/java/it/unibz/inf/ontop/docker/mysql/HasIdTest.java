package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.*;


/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest extends AbstractVirtualModeTest {

    static final String owlFileName = "/mysql/pullOutEq/pullOutEq.ttl";
    static final String obdaFileName = "/mysql/pullOutEq/pullOutEq.obda";
    static final String propertyFileName = "/mysql/pullOutEq/pullOutEq.properties";

    public HasIdTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }


    private TupleOWLResultSet runLocalQuery(String query) throws OWLException {

        OWLStatement st = conn.createStatement();
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
