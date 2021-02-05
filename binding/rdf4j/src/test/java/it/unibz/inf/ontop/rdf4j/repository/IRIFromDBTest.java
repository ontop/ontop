package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class IRIFromDBTest extends AbstractRDF4JTest {

    private static final String MAPPING_FILE = "/iri-from-db/mapping.obda";
    private static final String SQL_SCRIPT = "/iri-from-db/example.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, MAPPING_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testSPO() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?s ?p ?o\n" +
                "}");
        assertEquals(count, 1);
    }

    @Test
    public void testSC() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?c\n" +
                "}");
        assertEquals(count, 1);
    }
}
