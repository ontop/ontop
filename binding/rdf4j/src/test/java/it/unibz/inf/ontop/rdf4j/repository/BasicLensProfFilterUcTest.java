package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class BasicLensProfFilterUcTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/prof/prof_with_mirror_views.obda";
    private static final String SQL_SCRIPT = "/prof/prof.sql";
    private static final String VIEW_FILE = "/prof/views/lens_prof_filter_uc.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testProfLastName()  {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Professor . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/10"));
    }
}
