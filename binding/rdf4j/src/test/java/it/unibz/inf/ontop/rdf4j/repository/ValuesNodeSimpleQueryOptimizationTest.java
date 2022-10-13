package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;

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
        String SPARQL_Query_String = "SELECT ?v WHERE {\n" +
                "  ?v ?p ?o .\n" +
                "}" +
                "LIMIT 2";

        String expectedSQLQueryTranslation = "SELECT V1.C1 AS \"v1m6\"\n" +
                        "FROM (VALUES  ('http://te.st/ValuesNodeTest#student/Anna'), ('http://te.st/ValuesNodeTest#student/Francis') AS V1 )";

        String ontopSQLtranslation = reformulate(SPARQL_Query_String);

        assertEquals(expectedSQLQueryTranslation, adjustSQLTranslation(ontopSQLtranslation));
    }

    private String adjustSQLTranslation(String SQL_query) {
        // Remove ans, CONSTRUCT, NATIVE clauses i.e. first 3 lines
        String query2 = SQL_query.substring(SQL_query.indexOf('\n',
                SQL_query.indexOf('\n',
                        SQL_query.indexOf('\n')+1)+1)+1);
        // Remove any remaining newlines at the end
        return query2.replaceAll("([\\n\\r]+\\s*)*$", "");
    }

}
