package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;


public class JoinViewPersonTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_joinviews.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String VIEW_FILE = "/person/views/join_views.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test // Concatenation function
    public void testPersonConcat() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :fullNameAndLocality ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("ROGER SMITH Botzen"));
    }

    @Test // Replace function
    public void testPersonReplace() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :localityAbbrev ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Bz"));
    }

    @Test // Nullif function
    public void testPersonNullif() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :nullifItaly ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }

    /**
     * Rename attribute with hidden column
     */
    @Test
    public void testPersonRenameAttribute() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :region ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Botzen"));
    }
}
