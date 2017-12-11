package it.unibz.inf.ontop.docker;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Stopped database test")
public class SAPHANATest extends AbstractVirtualModeTest {

    private static final String owlfile = "/sap/SAPbooktutorial.owl";
    private static final String obdafile = "/sap/SAPbooktutorial.obda";
    private static final String propertyfile = "/sap/SAPbooktutorial.properties";

    public SAPHANATest() {
        super(owlfile, obdafile, propertyfile);
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
