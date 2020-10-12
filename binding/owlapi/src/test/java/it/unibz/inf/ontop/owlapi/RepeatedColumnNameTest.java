package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class RepeatedColumnNameTest extends AbstractOWLAPITest {
    private static final String CREATE_SCRIPT = "/test/repeatedCN/repeatedCN_create.sql";
    private static final String OWL_FILE = "/test/repeatedCN/repeatedCN_test.owl";
    private static final String R2RML_FILE = "/test/repeatedCN/repeatedCN_test.ttl";

    @BeforeClass
    public static void setUp() throws Exception {
        initR2RML(CREATE_SCRIPT, R2RML_FILE, OWL_FILE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testQuery() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT * { ?v a :Professor. }";

        String sql = checkReturnedValuesAndReturnSql(query, ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1/1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/2/2>"));

//        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("professors"));
//        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("PROFESSORS"));
    }
}
