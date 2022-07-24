package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class Issue438Test extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/issue438/mapping.obda";
    private static final String SQL_SCRIPT = "/issue438/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testQuery() {
        int count = runQueryAndCount("PREFIX : <http://example.com/ontology#>\n" +
                "SELECT ?s WHERE { ?s a :Image } ");
        assertEquals(3, count);
    }

}
