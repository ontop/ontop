package federationOptimization.queryRewriting;

import org.junit.Test;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
   // private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-hom-het-dremio-denodo.obda";
    // private static final String propertyFile = "src/test/resources/federation-test/sc2.properties";
    //private static final String propertyFile = "src/test/resources/federation-test/denodo.properties";
    private static final String propertyFile = "src/test/resources/federation-test/dremio.properties";
    //private static final String propertyFile = "src/test/resources/federation-test/teiid-local.properties";
    //private static final String hintFile = "src/test/resources/federation-test/hintsWithOutMatV.txt";
    //private static final String hintFile = "src/test/resources/federation-test/hint-denodo.txt";
    private static final String hintFile = "src/test/resources/federation-test/hint-dremio.txt";
    private static final String labFile = "src/test/resources/federation-test/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/het-effLabel.txt";
    //private static final String lenseFile = "src/test/resources/federation-test/lenses.fed.dremio.json";
    private static final String constraintFile = "src/test/resources/federation-test/constraints.fed.dremio.denodo.txt";

    private static final String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "\n" +
            "SELECT ?product ?productLabel\n" +
            "WHERE {\n" +
            "\t?product rdfs:label ?productLabel .\n" +
            "    FILTER (<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> != ?product)\n" +
            "\t<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productFeature ?prodFeature .\n" +
            "\t?product bsbm:productFeature ?prodFeature .\n" +
            "\t<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric1 ?origProperty1 .\n" +
            "\t?product bsbm:productPropertyNumeric1 ?simProperty1 .\n" +
            "\tFILTER (?simProperty1 < (?origProperty1 + 120))\n" +
            "\t<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> bsbm:productPropertyNumeric2 ?origProperty2 .\n" +
            "\t?product bsbm:productPropertyNumeric2 ?simProperty2 .\n" +
            "\tFILTER (?simProperty2 < (?origProperty2 + 170) )\n" +
            "}";

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, constraintFile, propertyFile, hintFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }
}