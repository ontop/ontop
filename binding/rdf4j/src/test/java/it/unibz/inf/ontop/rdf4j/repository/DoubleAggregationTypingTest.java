package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class DoubleAggregationTypingTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/double-aggregation/mapping.obda";
    private static final String SQL_SCRIPT = "/double-aggregation/database.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testQuery1() {
        int count = runQueryAndCount("PREFIX : <http://foobar.abc/ontology/demo#>\n" +
                "\n" +
                "SELECT (SUM(?length)/?totalLength as ?calc) WHERE {\n" +
                "  ?foo :length ?length .\n" +
                "\n" +
                "  {\n" +
                "    SELECT (SUM(?length) as ?totalLength) WHERE {\n" +
                "      ?foo :length ?length .\n" +
                "    }\n" +
                "  }  \n" +
                "}\n" +
                "GROUP BY ?totalLength");
        assertEquals(1, count);
    }

    @Test
    public void testQuery2() {
        int count = runQueryAndCount("PREFIX : <http://foobar.abc/ontology/demo#>\n" +
                "\n" +
                "SELECT (SUM(?length)/?totalLength as ?calc) WHERE {\n" +
                "  ?foo :length ?length .\n" +
                "\n" +
                "  {\n" +
                "    SELECT (SUM(?length)/1 as ?totalLength) WHERE {\n" +
                "      ?foo :length ?length .\n" +
                "    }\n" +
                "  }  \n" +
                "}\n" +
                "GROUP BY ?totalLength");
        assertEquals(1, count);
    }

    @Test
    public void testQuery3() {
        int count = runQueryAndCount("PREFIX : <http://foobar.abc/ontology/demo#>\n" +
                "    SELECT (SUM(?length) as ?totalLength) WHERE {\n" +
                "      ?foo :length ?length .\n" +
                "    }\n");
        assertEquals(1, count);
    }

}
