package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;


public class MirrorProfFKTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/prof/prof_with_mirror_views.obda";
    private static final String OWL_FILE = "/prof/prof.owl";
    private static final String SQL_SCRIPT = "/prof/prof.sql";
    private static final String VIEW_FILE = "/prof/views/mirrors.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, OWL_FILE, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Test if the FK between views is inferred, so that some unions can be eliminated after mapping saturation.
     */
    @Test
    public void testNoUnion() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                "}";
        String reformulatedQuery = reformulate(query);
        assertFalse("Reformulated query with unwanted union: " + reformulatedQuery,
                reformulatedQuery.toLowerCase().contains("union"));
    }

}
