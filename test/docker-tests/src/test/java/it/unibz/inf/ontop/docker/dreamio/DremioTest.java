package it.unibz.inf.ontop.docker.dreamio;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Local database test")
public class DremioTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/dremio/incidents/incidents.owl";
    private static final String obdafile = "/dremio/incidents/incidents.obda";
    private static final String propertyfile = "/dremio/dremio.properties";

    public DremioTest() {
        super(owlfile, obdafile, propertyfile);
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
