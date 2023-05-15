package federationOptimization.queryRewriting;

import org.junit.Test;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
//    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-hom-het.obda";
    private static final String propertyFile = "src/test/resources/federation-test/sc2.properties";
    private static final String hintFile = "src/test/resources/federation-test/hintFile.txt";
    private static final String labFile = "src/test/resources/federation-test/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/effLabel.txt";

    private static final String query = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "\n" +
            "SELECT DISTINCT ?product ?label\n" +
            "WHERE {\n" +
            " ?product rdfs:label ?label .\n" +
            " ?product a bsbm:Product .\n" +
            " ?product bsbm:productFeature bsbm-inst:ProductFeature89 .\n" +
            " ?product bsbm:productFeature bsbm-inst:ProductFeature91 .\n" +
            " ?product bsbm:productPropertyNumeric1 ?value1 .\n" +
            " FILTER (?value1 < 1000)\n" +
            "}";

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, hintFile, labFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }

}