package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DestinationUnitTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest-unit.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testUnit1() {
        var sql = reformulateIntoNativeQuery("PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "PREFIX qudt: <http://qudt.org/schema/qudt#>\n" +
                "\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  ?r a sosa:Result ; qudt:unit ?v" +
                "\n" +
                "}\n");

        assertFalse("Should have no unions: "  + sql, sql.toLowerCase().contains("union"));
        assertFalse("Should have no distincts: "  + sql, sql.toLowerCase().contains("distinct"));
    }

    @Test
    public void testUnit2() {
        var sql = reformulateIntoNativeQuery("PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "PREFIX qudt: <http://qudt.org/schema/qudt#>\n" +
                "\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "  ?r a sosa:Result ." +
                "  OPTIONAL {" +
                "  ?r qudt:unit ?v " +
                "}" +
                "\n" +
                "}\n");

        assertFalse("Should have no unions: "  + sql, sql.toLowerCase().contains("union"));
        assertFalse("Should have no distincts: "  + sql, sql.toLowerCase().contains("distinct"));
    }

    @Test
    public void testUnit3() {
        var sql = reformulateIntoNativeQuery("PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "PREFIX qudt: <http://qudt.org/schema/qudt#>\n" +
                "\n" +
                "SELECT ?r\n" +
                "WHERE {\n" +
                "  ?r a sosa:Result ." +
                "  OPTIONAL {" +
                "  ?r qudt:unit ?v " +
                "}" +
                "\n" +
                "}\n");

        assertFalse("Should have no unions: "  + sql, sql.toLowerCase().contains("union"));
        assertFalse("Should have no distincts: "  + sql, sql.toLowerCase().contains("distinct"));
    }
    
    @Test
    public void testOnlyOneUnitPerResult() {
        try {
            String s = reformulateIntoNativeQuery("PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                    "PREFIX qudt: <http://qudt.org/schema/qudt#>\n" +
                    "\n" +
                    "SELECT *\n" +
                    "WHERE {\n" +
                    "  ?r a sosa:Result . \n" +
                    "  ?r qudt:unit ?v, ?v2 .\n" +
                    " FILTER (?v2 != ?v)\n" +
                    "\n" +
                    "}\n");
            System.out.println(s);
        } catch (Exception e) {
            // Make sure the query is reformulated as empty
            assertTrue(e.getMessage().contains("EMPTY"));
            return;
        }
        fail("The query has not been detected as empty (without execution)");
    }

}
