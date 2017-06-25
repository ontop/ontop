package it.unibz.inf.ontop.docker.failing.local;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Ignore;

@Ignore("Local test")
public class SAPHANATest extends AbstractVirtualModeTest {

    private static final String owlfile = "/local/sap/SAPbooktutorial.owl";
    private static final String obdafile = "/local/sap/SAPbooktutorial.obda";
    private static final String propertyfile = "/local/sap/SAPbooktutorial.properties";

    public SAPHANATest() {
        super(owlfile, obdafile, propertyfile);
    }

//    final String owlfile = "src/test/resources/example/exampleBooks.owl";
//    final String obdafile = "src/test/resources/example/exampleBooks.obda";

    public void testSAP() throws Exception {
            /* 
            * Get the book information that is stored in the database 
            */
        String sparqlQuery =
                "PREFIX : <http://meraka/moss/exampleBooks.owl#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x ?y where {?x rdf:type :Author. ?x :name ?y.} limit 5 offset 2";

        runQuery(sparqlQuery);
    }

}
