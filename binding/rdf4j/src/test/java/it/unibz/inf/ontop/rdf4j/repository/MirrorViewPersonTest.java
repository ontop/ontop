package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class MirrorViewPersonTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/person/person.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String VIEW_FILE = "/person/views/mirror_views.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testPerson() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?p a <http://person.example.org/Person> \n" +
                "}");
        assertEquals(count, 1);
    }

    @Test
    public void testPerson2() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?p a <http://person.example.org/Person> \n" +
                " VALUES ?p { <http://person.example.org/person/1> <http://person.example.org/person/2> <http://person.example.org/person/%2B1> }\n" +
                "}");
        assertEquals(count, 1);
    }

    @Test
    public void testFullNameIn() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?p <http://person.example.org/fullName> ?n \n" +
                "FILTER(?n IN (\"Roger Smith\",\"Francis Menard\"))\n" +
                "}");
        assertEquals(count, 1);
    }

    @Test
    public void testFullNameNotIn() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?p <http://person.example.org/fullName> ?n \n" +
                "FILTER(?n NOT IN (\"Roger Smith\",\"Francis Menard\"))\n" +
                "}");
        assertEquals(count, 0);
    }
}
