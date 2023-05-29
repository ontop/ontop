package federationOptimization.queryRewriting;

import org.junit.Test;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
   // private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
    private static final String obdaFile = "src/test/resources/federation-test/bsbm-mappings-hom-het.obda";
    // private static final String propertyFile = "src/test/resources/federation-test/sc2.properties";
    private static final String propertyFile = "src/test/resources/federation-test/teiid-local.properties";
    //private static final String propertyFile = "src/test/resources/federation-test/dremio.properties";
    //private static final String hintFile = "src/test/resources/federation-test/hintsWithOutMatV.txt";
    private static final String hintFile = "src/test/resources/federation-test/hintsWithMatV.txt";
    private static final String labFile = "src/test/resources/federation-test/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/effLabel.txt";

    private static final String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
            "\n" +
            "SELECT ?productLabel ?offer ?price ?vendor ?vendorTitle ?review ?revTitle\n" +
            " ?reviewer ?revName ?rating1 ?rating2\n" +
            "WHERE {\n" +
            "\t<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> rdfs:label ?productLabel .\n" +
            " OPTIONAL {\n" +
            " ?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .\n" +
            " ?offer bsbm:price ?price .\n" +
            " ?offer bsbm:vendor ?vendor .\n" +
            " ?vendor rdfs:label ?vendorTitle .\n" +
            " ?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#DE> .\n" +
            " ?offer dc:publisher ?vendor .\n" +
            " ?offer bsbm:validTo ?date .\n" +
            " FILTER (?date > '1988-01-01'^^xsd:date)\n" +
            " }\n" +
            " OPTIONAL {\n" +
            "\t?review bsbm:reviewFor <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer/Product88> .\n" +
            "\t?review rev:reviewer ?reviewer .\n" +
            "\t?reviewer foaf:name ?revName .\n" +
            "\t?review dc:title ?revTitle .\n" +
            " OPTIONAL { ?review bsbm:rating1 ?rating1 . }\n" +
            " OPTIONAL { ?review bsbm:rating2 ?rating2 . }\n" +
            " }\n" +
            "}";

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, hintFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }
}