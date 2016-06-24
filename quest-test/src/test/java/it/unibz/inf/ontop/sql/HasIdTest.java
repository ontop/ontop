package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

import org.semanticweb.owlapi.model.OWLException;


/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest extends AbstractVirtualModeTest {

    static final String owlFileName = "resources/pullOutEq/pullOutEq.ttl";
    static final String obdaFileName = "resources/pullOutEq/pullOutEq.obda";

    private QuestOWL reasoner;
    private QuestOWLConnection conn;

    protected HasIdTest() {
        super(owlFileName, obdaFileName);
    }


    private QuestOWLResultSet runQuery(String query) throws OBDAException, OWLException {

        // Now we are ready for querying
        conn = reasoner.getConnection();

        QuestOWLStatement st = conn.createStatement();
        return st.executeTuple(query);
    }

    public void test() throws OBDAException, OWLException {
        QuestOWLResultSet results = runQuery("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :hasId \"3\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}");
        // At least one result
        assertTrue(results.nextRow());
        assertEquals(results.getOWLIndividual(1).toString(), "<http://example.com/persons/3>");
        assertEquals(results.getOWLLiteral(2), null);
        assertEquals(results.getOWLLiteral(3), null);
        assertFalse(results.nextRow());
    }
}
