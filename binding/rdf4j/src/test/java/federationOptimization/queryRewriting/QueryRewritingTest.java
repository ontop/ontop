package federationOptimization.queryRewriting;

import org.junit.Test;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
   // private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-hom-het.obda";
    // private static final String propertyFile = "src/test/resources/federation-test/sc2.properties";
    private static final String propertyFile = "src/test/resources/federation-test/teiid-local.properties";
    //private static final String hintFile = "src/test/resources/federation-test/hintsWithOutMatV.txt";
    private static final String hintFile = "src/test/resources/federation-test/hintsWithMatV.txt";
    private static final String labFile = "src/test/resources/federation-test/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/effLabel.txt";

    private static final String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "PREFIX bsbm-export: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/export/>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "\n" +
            "SELECT ?offer ?productURI ?productlabel ?vendorURI ?vendorname ?offerURL ?price ?deliveryDays ?validTo\n" +
            "WHERE {\n" +
            " ?offer bsbm:offerId ?id .\n" +
            " FILTER ( ?id < 1000)\n" +
            " ?offer bsbm:product ?productURI .\n" +
            " ?productURI rdfs:label ?productlabel .\n" +
            " ?offer bsbm:vendor ?vendorURI .\n" +
            " ?vendorURI rdfs:label ?vendorname .\n" +
            " ?vendorURI foaf:homepage ?vendorhomepage .\n" +
            " ?offer bsbm:offerWebpage ?offerURL .\n" +
            " ?offer bsbm:price ?price .\n" +
            " ?offer bsbm:deliveryDays ?deliveryDays .\n" +
            " ?offer bsbm:validTo ?validTo\n" +
            " }";

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, hintFile, labFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }

}