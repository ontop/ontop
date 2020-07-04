package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class Issue325Test extends AbstractRDF4JTest {

    private static final String R2RML_FILE = "/issue325/mapping.r2rml.ttl";
    private static final String SQL_SCRIPT = "/issue325/example.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, R2RML_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testQuery() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?s <http://ex.com#ex2> ?s2\n" +
                "}");
        assertEquals(count, 3);
    }
}
