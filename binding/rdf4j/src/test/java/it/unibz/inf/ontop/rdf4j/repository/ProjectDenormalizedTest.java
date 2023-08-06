package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

public class ProjectDenormalizedTest extends AbstractRDF4JTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA("/project-denormalized/db.sql", "/project-denormalized/project.obda", null, null, "/project-denormalized/lenses.json");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testMunicipality1() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT * \n" +
                "WHERE {\n" +
                " ?m a :Municipality . \n" +
                " OPTIONAL { ?m :name ?v }  \n" +
                " OPTIONAL { " +
                "   ?m :province ?p . \n " +
                "   OPTIONAL { ?p :name ?provinceName } \n" +
                "   OPTIONAL { \n" +
                "      ?p :region ?r . \n " +
                "      OPTIONAL { ?r :name ?regionName } \n" +
                "   }\n" +
                " } \n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));

    }

    @Test
    public void testProvince1() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT * \n" +
                "WHERE {\n" +
                "   ?p a :Province . \n " +
                "   OPTIONAL { ?p :name ?provinceName } \n" +
                "   OPTIONAL { \n" +
                "      ?p :region ?r . \n " +
                "      OPTIONAL { ?r :name ?regionName } \n" +
                "   }\n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));

    }

    @Test
    public void testProject1() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT * \n" +
                "WHERE {\n" +
                " ?project a :Project \n" +
                " OPTIONAL { \n" +
                "   ?project :municipality ?m . \n" +
                "   OPTIONAL { ?m :name ?v }  \n" +
                "   OPTIONAL { " +
                "     ?m :province ?p . \n " +
                "     OPTIONAL { ?p :name ?provinceName } \n" +
                "     OPTIONAL { \n" +
                "        ?p :region ?r . \n " +
                "        OPTIONAL { ?r :name ?regionName } \n" +
                "     }\n" +
                "   } \n" +
                " } \n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));
        assertFalse("Distinct found in " + sql, sql.toLowerCase().contains("distinct"));
    }

    @Test
    public void testProject2() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT * \n" +
                "WHERE {\n" +
                " ?project a :Project . \n" +
                " ?project :municipality ?m . \n" +
                "   OPTIONAL { ?m :name ?v }  \n" +
                "   OPTIONAL { " +
                "     ?m :province ?p . \n " +
                "     OPTIONAL { ?p :name ?provinceName } \n" +
                "     OPTIONAL { \n" +
                "        ?p :region ?r . \n " +
                "        OPTIONAL { ?r :name ?regionName } \n" +
                "     }\n" +
                "   } \n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));
        assertFalse("Distinct found in " + sql, sql.toLowerCase().contains("distinct"));
    }

    @Test
    public void testProject3() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT ?project ?v ?provinceName ?regionName \n" +
                "WHERE {\n" +
                " ?project a :Project \n" +
                " OPTIONAL { \n" +
                "   ?project :municipality ?m . \n" +
                "   OPTIONAL { ?m :name ?v }  \n" +
                "   OPTIONAL { " +
                "     ?m :province ?p . \n " +
                "     OPTIONAL { ?p :name ?provinceName } \n" +
                "     OPTIONAL { \n" +
                "        ?p :region ?r . \n " +
                "        OPTIONAL { ?r :name ?regionName } \n" +
                "     }\n" +
                "   } \n" +
                " } \n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));
        assertFalse("Distinct found in " + sql, sql.toLowerCase().contains("distinct"));
    }

    @Test
    public void testProject4() {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "SELECT DISTINCT ?project ?v ?provinceName ?regionName \n" +
                "WHERE {\n" +
                " ?project a :Project \n" +
                " OPTIONAL { \n" +
                "   ?project :municipality ?m . \n" +
                "   OPTIONAL { ?m :name ?v }  \n" +
                "   OPTIONAL { " +
                "     ?m :province ?p . \n " +
                "     OPTIONAL { ?p :name ?provinceName } \n" +
                "     OPTIONAL { \n" +
                "        ?p :region ?r . \n " +
                "        OPTIONAL { ?r :name ?regionName } \n" +
                "     }\n" +
                "   } \n" +
                " } \n" +
                "}";

        String sql = reformulateIntoNativeQuery(query);
        assertFalse("Left joins found in " + sql, sql.toLowerCase().contains("left"));
        assertFalse("Distinct found in " + sql, sql.toLowerCase().contains("distinct"));
    }
}
