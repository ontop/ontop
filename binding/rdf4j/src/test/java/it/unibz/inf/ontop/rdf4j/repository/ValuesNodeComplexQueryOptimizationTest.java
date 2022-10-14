package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Run tests on whether the SQL translation of the Slice Optimization for Values Node
 * works properly. Complex ontology with multiple tables and multiple mappings used.
 */
public class ValuesNodeComplexQueryOptimizationTest extends AbstractRDF4JTest {

    private static final String MAPPING_FILE = "/values-node/university.obda";
    private static final String ONTOLOGY_FILE = "/values-node/university.ttl";
    private static final String SQL_SCRIPT = "/values-node/university.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, MAPPING_FILE, ONTOLOGY_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testTranslatedSQLQuery1() {
        String sparqlQueryString = "SELECT ?v WHERE {\n" +
                "  ?v ?p ?o .\n" +
                "}" +
                "LIMIT 4";

        String expectedSQLQueryTranslation = "SELECT V1.C1 AS \"v5m25\"\n" +
                "FROM (VALUES  ('http://te.st/ValuesNodeTest#student/Francis'), ('http://te.st/ValuesNodeTest#student/Anna'), " +
                "('http://te.st/ValuesNodeTest#teacher/Jane'), ('http://te.st/ValuesNodeTest#teacher/Joe') AS V1 )";

        String ontopSQLtranslation = reformulate(sparqlQueryString);

        assertTrue(ontopSQLtranslation.contains(expectedSQLQueryTranslation));
    }
}
