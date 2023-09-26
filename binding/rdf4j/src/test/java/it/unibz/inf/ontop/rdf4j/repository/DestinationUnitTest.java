package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DestinationUnitTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination/dest-unit.obda";
    private static final String SQL_SCRIPT = "/destination/schema.sql";
    private static final String ONTOLOGY_FILE = "/destination/dest.owl";
    private static final String PROPERTIES_FILE = "/destination/dest.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
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
    }

}
