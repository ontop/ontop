package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class RDF4JLangTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/label_comment.sql";
    private static final String OBDA_FILE = "/label_comment.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSameLanguageLabelComment() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "  ?o rdfs:label ?label ;\n" +
                "     rdfs:comment ?comment .\n" +
                "  FILTER( LANG(?label) = LANG(?comment) )\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(2, count);
    }

    @Test
    public void testLangMatches1() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "  ?o rdfs:label ?label .\n" +
                "  FILTER(LANGMATCHES(LANG(?label), \"de\"))\n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }
}
