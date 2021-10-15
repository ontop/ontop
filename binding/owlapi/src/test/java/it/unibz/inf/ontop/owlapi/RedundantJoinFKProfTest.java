package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;
import static org.junit.Assert.*;

public class RedundantJoinFKProfTest extends AbstractOWLAPITest {

    private static final String NO_OPTIMIZATION_MSG = "The table professors should not be used";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/redundant_join/redundant_join_fk_create.sql",
                "/test/redundant_join/redundant_join_fk_test.obda",
                "/test/redundant_join/redundant_join_fk_test.owl",
                "/test/redundant_join/redundant_join_fk_test.properties");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testQuery() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT * {\n" +
                "?v a :Professor ; :teaches ?c .\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>"));

        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("professors"));
        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("PROFESSORS"));
    }
}
