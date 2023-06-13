package federationOptimization.queryRewriting;

import org.junit.Test;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/teiid/bsbm-ontology.owl";
    private static final String obdaFile = "src/test/resources/federation-test/teiid/bsbm-mappings-hom-het.obda";
    private static final String propertyFile = "src/test/resources/federation-test/teiid/system-hom.properties";
    private static final String hintFile = "src/test/resources/federation-test/teiid/hint-full.txt";
    private static final String labFile = "src/test/resources/federation-test/teiid/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/teiid/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/teiid/hom-effLabel.txt";
    private static final String constraintFile = "src/test/resources/federation-test/teiid/constraints.fed.txt";
    //private static final String lenseFile = "src/test/resources/federation-test/teiid/lenses.fed.json";

    private static final String query = "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "\n" +
            "SELECT ?p ?mbox_sha1sum ?country ?r ?product ?title\n" +
            "WHERE {\n" +
            "<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review88> rev:reviewer ?p .\n" +
            "?p foaf:name ?name .\n" +
            "?p foaf:mbox_sha1sum ?mbox_sha1sum .\n" +
            "?p bsbm:country ?country .\n" +
            "?r rev:reviewer ?p .\n" +
            "?r bsbm:reviewFor ?product .\n" +
            "?r dc:title ?title .\n" +
            "}";

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, constraintFile, hintFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }
}