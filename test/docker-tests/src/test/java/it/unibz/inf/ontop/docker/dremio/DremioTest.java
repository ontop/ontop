package it.unibz.inf.ontop.docker.dremio;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Ignore("Local database test")
public class DremioTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/dremio/incidents/incidents.owl";
    private static final String obdafile = "/dremio/incidents/incidents.obda";
    private static final String propertyfile = "/dremio/dremio.properties";

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
    public void testDremio() throws Exception {
            /* 
            * Get the  information that is stored in the database 
            */
        String sparqlQuery =
                "PREFIX : <http://www.semanticweb.org/incidents#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x  {?x rdf:type :Incident} limit 5 ";

        System.out.print(runQueryAndReturnStringOfIndividualX(sparqlQuery));
    }

}
