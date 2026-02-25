package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue858Test extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/issue858/mapping.obda";
    private static final String SQL_FILE = "/issue858/db.sql";

    @BeforeClass
    public static void before() throws Exception {
        initOBDA(SQL_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws Exception {
        release();
    }

    @Test
    public void testQueryWorking() {
        String sparql =
                "PREFIX ex: <http://example.org/>\n" +
                        "SELECT * WHERE {\n" +
                        "  ?s ?p ?o .\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(1, count);
    }

    @Test
    public void testQueryError() {
        String sparql =
                "PREFIX ex: <http://example.org/>\n" +
                        "SELECT * WHERE {\n" +
                        "  <http://eg.com/something> ?p ?o .\n" +
                        "}";
        int count = runQueryAndCount(sparql);
        assertEquals(0, count);
    }
}
