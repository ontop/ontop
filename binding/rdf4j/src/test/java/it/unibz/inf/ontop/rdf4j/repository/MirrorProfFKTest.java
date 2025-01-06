package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
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
    public void testNoUnion()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                "}";
        String reformulatedQuery = reformulateIntoNativeQuery(query);
        assertFalse("Reformulated query with unwanted union: " + reformulatedQuery,
                reformulatedQuery.toLowerCase().contains("union"));
    }


    @Test
    public void testStartsWith1()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                " FILTER(STRSTARTS(str(?v), \"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1\")) \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/10"));
    }

    @Test
    public void testStartsWith2()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                " FILTER(STRSTARTS(str(?v), \"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/\")) \n" +
                "}\n" +
                "ORDER BY ?v\n" +
                "LIMIT 1";
        runQueryAndCompare(query, ImmutableSet.of("http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/10"));
    }

    @Test
    public void testStartsWith3()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                " FILTER(STRSTARTS(str(?v), \"http://nowhere.org/unrelatedURL\")) \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }

    @Test
    public void testStartsWith4()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Teacher . \n" +
                " FILTER(STRSTARTS(str(?v), \"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor-longer-prefix\")) \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }

}
