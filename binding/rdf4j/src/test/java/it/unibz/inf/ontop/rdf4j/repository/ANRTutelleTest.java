package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ANRTutelleTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/anr-tutelle/mapping.obda";
    private static final String SQL_SCRIPT = "/anr-tutelle/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testLJ1() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?partner ex:tutelle ?t.\n" +
                "  OPTIONAL { \n" +
                "     ?t ex:name ?name \n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }

    @Test
    public void testLJ2() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t a ex:Tutelle .\n" +
                "  OPTIONAL { \n" +
                "     ?t ex:category ?cat . \n" +
                "    # OPTIONAL { \n" +
                "     ?cat ex:name ?name \n" +
                "    #}\n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }
    
    @Test
    public void testLJ3() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t a ex:Tutelle .\n" +
                "  OPTIONAL { \n" +
                "     ?t ex:category ?cat . \n" +
                "     OPTIONAL { \n" +
                "     ?cat ex:name ?name \n" +
                "    }\n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }

    @Test
    public void testLJ4() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t a ex:Tutelle .\n" +
                " # OPTIONAL { \n" +
                "     ?t ex:category ?cat . \n" +
                "     OPTIONAL { \n" +
                "     ?cat ex:name ?name \n" +
                "    }\n" +
                " # }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }

    @Test
    public void testLJ5() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t ex:category ?cat . \n" +
                "  OPTIONAL { \n" +
                "     ?cat ex:name ?name \n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }

    @Test
    public void testLJ6() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t a ex:Tutelle .\n" +
                "  OPTIONAL { \n" +
                "     ?t ex:category ?cat . \n" +
                "     OPTIONAL { \n" +
                "     ?cat ex:name ?name \n" +
                "    }\n" +
                "     OPTIONAL { \n" +
                "     ?cat ex:label ?label \n" +
                "    }\n" +
                "  }\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The left-join should have been optimized out:\n " + sql,sql.contains("left"));
    }

}
