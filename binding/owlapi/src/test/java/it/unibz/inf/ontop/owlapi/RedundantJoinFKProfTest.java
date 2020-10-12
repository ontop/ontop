package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;
import static org.junit.Assert.*;

public class RedundantJoinFKProfTest extends AbstractOWLAPITest {

    private static final String CREATE_SCRIPT = "/test/redundant_join/redundant_join_fk_create.sql";
    private static final String OWL_FILE = "/test/redundant_join/redundant_join_fk_test.owl";
    private static final String ODBA_FILE = "/test/redundant_join/redundant_join_fk_test.obda";
    private static final String NO_OPTIMIZATION_MSG = "The table professors should not be used";

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

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT * {\n" +
                "?v a :Professor ; :teaches ?c .\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, ImmutableList.of(
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>"));

        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("professors"));
        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("PROFESSORS"));
    }
}
