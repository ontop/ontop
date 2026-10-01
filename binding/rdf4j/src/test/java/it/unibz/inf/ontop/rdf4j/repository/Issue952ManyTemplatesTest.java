package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Same as {@link Issue952Test} with twice as many IRI templates, as the results used to
 * depend on their number.
 */
public class Issue952ManyTemplatesTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/issue952/issue952-many-templates.obda";
    private static final String SQL_SCRIPT = "/issue952/schema.sql";
    private static final String PROPERTIES_FILE = "/issue952/issue952.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void constantSubjectMatchingOneOfTwoCompatibleTemplates() {
        String sparql = "SELECT ?p ?o WHERE {\n" +
                "  <http://example.org/data/item/1> ?p ?o .\n" +
                "}";
        assertEquals(2, runQueryAndCount(sparql));
    }

    @Test
    public void constantSubjectMatchingNonInjectiveTemplate() {
        String sparql = "SELECT ?p ?o WHERE {\n" +
                "  <http://example.org/data/itemline/1-2> ?p ?o .\n" +
                "}";
        assertEquals(1, runQueryAndCount(sparql));
    }
}
