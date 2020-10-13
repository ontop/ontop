package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class ConditionalAsLeftJoinTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/conditional_leftjoin/conditional_leftjoin_create.sql",
                "/test/conditional_leftjoin/conditional_leftjoin_test.obda",
                "/test/conditional_leftjoin/conditional_leftjoin_test.owl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testQuery() throws Exception {

        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2016/10/untitled-ontology-2#>\n" +
                "SELECT ?v \n" +
                "WHERE {\n" +
                " :Tartaruga a :VegetarianRestaurant .\n" +
                "OPTIONAL { :Tartaruga :hasAddress ?v }\n" +
                "}";

        checkReturnedValues(query, "v", ImmutableList.of(
                "<http://www.semanticweb.org/ontologies/2016/10/untitled-ontology-2#aa>"));
    }

}
