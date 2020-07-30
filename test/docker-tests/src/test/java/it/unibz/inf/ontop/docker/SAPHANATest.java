package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Ignore("Stopped database test")
public class SAPHANATest extends AbstractVirtualModeTest {

    private static final String owlfile = "/sap/SAPbooktutorial.owl";
    private static final String obdafile = "/sap/SAPbooktutorial.obda";
    private static final String propertyfile = "/sap/SAPbooktutorial.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlfile, obdafile, propertyfile);
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

    @Test
    public void testSAP() throws Exception {
            /* 
            * Get the book information that is stored in the database 
            */
        String sparqlQuery =
                "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x ?y where {?x rdf:type :Author. ?x :name ?y.} limit 5 offset 2";

        checkThereIsAtLeastOneResult(sparqlQuery);
    }

}
