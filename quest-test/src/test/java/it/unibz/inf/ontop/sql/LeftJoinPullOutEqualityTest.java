package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;
import org.semanticweb.owlapi.model.OWLException;

/**
 * TODO: describe
 */
public class LeftJoinPullOutEqualityTest extends AbstractVirtualModeTest {

    static final String owlFileName = "resources/pullOutEq/pullOutEq.ttl";
    static final String obdaFileName = "resources/pullOutEq/pullOutEq.obda";

    public LeftJoinPullOutEqualityTest() {
        super(owlFileName, obdaFileName);
    }

    public void testFlatLeftJoins() throws OBDAException, OWLException {
        countResults("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}", 1);
    }

    public void testNestedLeftJoins() throws OBDAException, OWLException {
        countResults("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    OPTIONAL { ?p :firstName ?firstName " +
                "               OPTIONAL { ?p :lastName ?lastName }" +
                "    }" +
                "}", 1);
    }

    public void testJoinAndFlatLeftJoins() throws OBDAException, OWLException {
        countResults("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p ?firstName ?lastName " +
                "WHERE { " +
                "    ?p :gender ?g . " +
                "    ?p :age \"33\"^^xsd:int . " +
                "    FILTER (str(?g) = \"F\") " +
                "    OPTIONAL { ?p :firstName ?firstName }" +
                "    OPTIONAL { ?p :lastName ?lastName }" +
                "}", 1);
    }

    public void testBasic() throws OBDAException, OWLException {
        countResults("PREFIX : <http://example.com/vocab#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "SELECT ?p " +
                "WHERE { " +
                "    ?p :age \"33\"^^xsd:int . " +
                "}", 1);
    }
}
