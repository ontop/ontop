package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class Issue447Test extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/issue447/mapping.obda";
    private static final String SQL_SCRIPT = "/issue447/database.sql";

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
        int count = runQueryAndCount("prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?I ex:prop1 ?V.\n" +
                "  minus { ?I ex:prop2 ?E }\n" +
                "  minus { ?I ex:prop3 ?E }\n" +
                "}");
        assertEquals(1, count);
    }

}
