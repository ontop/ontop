package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class ConditionalAsLeftJoinTest extends AbstractOWLAPITest {

    private static final String CREATE_SCRIPT = "/test/conditional_leftjoin/conditional_leftjoin_create.sql";
    private static final String OWL_FILE = "/test/conditional_leftjoin/conditional_leftjoin_test.owl";
    private static final String ODBA_FILE = "/test/conditional_leftjoin/conditional_leftjoin_test.obda";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA(CREATE_SCRIPT, ODBA_FILE, OWL_FILE);
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

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "<http://www.semanticweb.org/ontologies/2016/10/untitled-ontology-2#aa>"));
    }

}
