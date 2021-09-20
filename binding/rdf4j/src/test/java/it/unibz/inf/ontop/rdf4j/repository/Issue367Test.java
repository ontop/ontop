package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class Issue367Test extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/issue367/issue367.obda";
    private static final String SQL_SCRIPT = "/issue367/example.sql";

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
        int count = runQueryAndCount("PREFIX : <http://example.org/>\n" +
                "PREFIX myBase: <http://mybase.example.com/>" +
                "select  ?ppv {\n" +
                "  ?rce a :RA; :prop1 ?ppv .\n" +
                "  ?ppv a myBase:Prop .\n" +
                "}");
        assertEquals(count, 1);
    }
}
