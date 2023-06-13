package federationOptimization.queryRewriting;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/teiid/bsbm-ontology.owl";
    private static final String obdaFile = "src/test/resources/federation-test/teiid/bsbm-mappings-hom-het.obda";
    private static final String propertyFile = "src/test/resources/federation-test/teiid/system-hom.properties";
    private static final String hintFile = "src/test/resources/federation-test/teiid/hint-full.txt";
    private static final String labFile = "src/test/resources/federation-test/teiid/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/teiid/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/teiid/hom-effLabel.txt";
    private static final String constraintFile = "src/test/resources/federation-test/teiid/constraints.fed.txt";

    private static final String queryFile = "src/test/resources/federation-test/SPARQL/02.SPARQL";
    //private static final String lenseFile = "src/test/resources/federation-test/teiid/lenses.fed.json";
    private static String query = "";
    {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(queryFile)));
            String line = null;
            while((line=br.readLine()) != null ){
                query = query + line +" ";
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryRewriting() throws Exception {
        //need to change the obda file and property file to test on the federation setting
        QueryRewriting QR = new QueryRewriting(owlFile, obdaFile, propertyFile, constraintFile, hintFile, sourceFile, effLabel);
        System.out.println(QR.getOptimizedSQL(query));
    }
}