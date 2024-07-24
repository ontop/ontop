package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OverlappingTemplatesTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/overlapping-templates/mapping.obda";
    private static final String SQL_SCRIPT = "/overlapping-templates/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testNonDuplicates() {
        var query = "SELECT * WHERE {\n" +
                "  <http://example.org/xyz/xyz> ?pred ?obj .\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

}
