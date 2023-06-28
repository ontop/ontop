package federationOptimization.queryRewriting;

import federationOptimization.ObdfTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Category(ObdfTest.class)
public class QueryRewritingTest {

    private static final String owlFile = "src/test/resources/federation-test/denodo/bsbm-ontology.owl";
    private static final String obdaFile = "src/test/resources/federation-test/denodo/bsbm-mappings-hom-het.obda";
    private static final String propertyFile = "src/test/resources/federation-test/denodo/system-het.properties";
    private static final String hintFile = "src/test/resources/federation-test/denodo/hint-full-het.txt";
    private static final String labFile = "src/test/resources/federation-test/denodo/SourceLab.txt";
    private static final String sourceFile = "src/test/resources/federation-test/denodo/SourceFile.txt";
    private static final String effLabel = "src/test/resources/federation-test/denodo/hom-effLabel.txt";
    private static final String constraintFile = "src/test/resources/federation-test/denodo/constraints.fed.txt";
    private static final String lenseFile = "src/test/resources/federation-test/denodo/lenses.fed.json";

    private static final String queryFile = "src/test/resources/federation-test/SPARQL/01.SPARQL";

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