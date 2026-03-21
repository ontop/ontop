package it.unibz.inf.ontop.owlapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LargeValuesNodeSecondTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/large-values-node-2/create-tables.sql",
                "/large-values-node-2/mapping.obda",
                "/large-values-node-2/ontology.ttl",
                "/large-values-node-2/config.properties");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void test() throws Exception {
        String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX hip: <https://undrr-hip.org/>\n" +
                "\n" +
                "SELECT ?iri (GROUP_CONCAT(?d; separator=\"; \") AS ?definition) # query works if removing the aggregation or removing the \"a skos:Definition\" pattern\n" +
                "{    \n" +
                "    ?iri hip:definedAs [ a hip:Definition ; skos:definition ?d ] \n" +
                "}\n" +
                "GROUP BY ?iri";

        checkNumberOfReturnedValues(query, 77);
    }
}
