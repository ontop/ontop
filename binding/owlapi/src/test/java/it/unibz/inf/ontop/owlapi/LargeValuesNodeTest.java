package it.unibz.inf.ontop.owlapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LargeValuesNodeTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/large-values-node/create-tables.sql",
                "/large-values-node/mapping.obda",
                "/large-values-node/ontology.ttl",
                "/large-values-node/config.properties");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void test() throws Exception {
        String query =
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX hip: <https://undrr-hip.org/>\n" +
                "\n" +
                "SELECT DISTINCT ?class ?metaclass {\n" +
                "  [] rdfs:subClassOf hip:SpecificHazard ; rdfs:subClassOf ?class .\n" +
                "  ?class a ?metaclass\n" +
                "}";

        checkNumberOfReturnedValues(query, 618);
    }
}
