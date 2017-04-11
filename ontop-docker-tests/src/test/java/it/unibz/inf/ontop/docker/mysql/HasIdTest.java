package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OntopOWLStatement;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import org.semanticweb.owlapi.model.OWLException;


/**
 * When the ID is both used for creating the URI and as the literal of a datatype property.
 */
public class HasIdTest extends AbstractVirtualModeTest {

    static final String owlFileName = "src/test/resources/mysql/pullOutEq/pullOutEq.ttl";
    static final String obdaFileName = "src/test/resources/mysql/pullOutEq/pullOutEq.obda";
    static final String propertyFileName = "src/test/resources/mysql/pullOutEq/pullOutEq.properties";

    public HasIdTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }


    private QuestOWLResultSet runLocalQuery(String query) throws OWLException {

        OntopOWLStatement st = conn.createStatement();
        return st.executeTuple(query);
    }

    public void test() throws OWLException {
        QuestOWLResultSet results = runLocalQuery("PREFIX : <http://example.com/vocab#>" +
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
