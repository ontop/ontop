package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Run tests on whether the SQL translation of the Slice Optimization for Values Node
 * works properly. Simple ontology with one table and one mapping used.
 */
public class ValuesNodeSimpleQueryOptimizationTest extends AbstractRDF4JTest {

    private static final String MAPPING_FILE = "/values-node/mapping.obda";
    private static final String ONTOLOGY_FILE = "/values-node/ontology.owl";
    private static final String SQL_SCRIPT = "/values-node/example.sql";

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
                "LIMIT 2";

        String ontopSQLtranslation = reformulateIntoNativeQuery(sparqlQueryString);
        int count = runQueryAndCount(sparqlQueryString);

        assertEquals(2, count);
        assertFalse("SQL has an unexpected union: " + ontopSQLtranslation, ontopSQLtranslation.toLowerCase().contains("union"));
        assertFalse("SQL has an unexpected limit: " + ontopSQLtranslation, ontopSQLtranslation.toLowerCase().contains("limit"));
    }

}
