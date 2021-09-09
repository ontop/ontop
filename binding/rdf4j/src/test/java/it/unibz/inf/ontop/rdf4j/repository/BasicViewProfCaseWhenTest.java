package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class BasicViewProfCaseWhenTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/prof/prof_views.obda";
    private static final String SQL_SCRIPT = "/prof/prof_views.sql";
    private static final String VIEW_FILE = "/prof/views/casewhen_basicview.json";
    private static final String DBMETADATA_FILE = "/prof/prof.db-extract.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE, DBMETADATA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Test case 1 CASE WHEN with CAST
     */
    @Test
    public void testProfLastName() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Professor . \n" +
                " ?x :ufirstname ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Roger", "FRANK", "JOHN", "MICHAEL", "DIEGO", "JOHANN", "BARBARA", "MARY"));
    }

    /**
     * Test case 2 CASE WHEN with SUBSTRING
     */
    @Test
    public void testProfNickName() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Professor . \n" +
                " ?x :vnickname ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("frankie", "Johnny", "King of Pop"));
    }
}
