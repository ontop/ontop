package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class MetaMappingExpansionMixTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/metamapping/metamapping.sql",
                "/metamapping/metamapping.obda",
                "/metamapping/metamapping.owl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void test_proper_mapping_split() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
                "SELECT ?x WHERE { ?x a :Broker }";

        checkReturnedValues(query, "x", ImmutableList.of(
                "<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-112>",
                "<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-113>",
                "<http://www.owl-ontologies.com/Ontology1207768242.owl#Broker-114>"));
    }
}
