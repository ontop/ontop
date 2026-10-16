package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Reproduces <a href="https://github.com/ontop/ontop/issues/952">issue #952</a>:
 * a triple pattern with a constant subject and a variable predicate returned no result.
 *
 * <p>exd:item/{id} and the non-injective exd:itemline/{id}-{sub} have colliding static
 * prefixes, so both are compatible with the constant exd:item/1.</p>
 */
public class Issue952Test extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/issue952/issue952.obda";
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

    // Same results expected as with the constant subject
    @Test
    public void filterOnSubjectInsteadOfConstant() {
        String sparql = "SELECT ?p ?o WHERE {\n" +
                "  ?s ?p ?o .\n" +
                "  FILTER(?s = <http://example.org/data/item/1>)\n" +
                "}";
        assertEquals(2, runQueryAndCount(sparql));
    }

    @Test
    public void describeConstantSubject() {
        String sparql = "DESCRIBE <http://example.org/data/item/1>";
        assertEquals(2, runGraphQueryAndCount(sparql));
    }

    // Matches the non-injective template, not exd:item/{id}
    @Test
    public void constantSubjectMatchingNonInjectiveTemplate() {
        String sparql = "SELECT ?p ?o WHERE {\n" +
                "  <http://example.org/data/itemline/1-2> ?p ?o .\n" +
                "}";
        assertEquals(1, runQueryAndCount(sparql));
    }

    @Test
    public void constantSubjectMatchingNoTemplate() {
        String sparql = "SELECT ?p ?o WHERE {\n" +
                "  <http://example.org/data/item/404> ?p ?o .\n" +
                "}";
        assertEquals(0, runQueryAndCount(sparql));
    }
}
