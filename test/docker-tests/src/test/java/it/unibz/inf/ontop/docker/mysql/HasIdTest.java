package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBindingSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.TupleOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;


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
        assertEquals(bindingSet.getOWLIndividual(1).toString(), "<http://example.com/persons/3>");
        assertEquals(bindingSet.getOWLLiteral(2), null);
        assertEquals(bindingSet.getOWLLiteral(3), null);
        assertFalse(results.hasNext());
    }
}
