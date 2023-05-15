package it.unibz.inf.ontop.docker.lightweight.db2;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@DB2LightweightTest
public class IdentifierDB2Test extends AbstractDockerRDF4JTest {
    private static final String OBDA_FILE = "/stockexchange/db2/identifiers-db2.obda";
    private static final String OWL_FILE = "/stockexchange/identifiers.owl";
    private static final String PROPERTIES_FILE = "/stockexchange/db2/stockexchange-db2.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    /**
     * Test use of quoted table and column identifiers
     */
    @Test
    public void testLowercaseQuoted() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Country\n" +
                "}\n" +
                "ORDER BY ?v";
        executeAndCompareValues(query,
                ImmutableList.of("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-991>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-992>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-993>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-995>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-996>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-997>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-998>"));
    }

    /**
     * Test use of lowercase, unquoted table, schema and column identifiers
     */
    @Test
    public void testLowercaseUnquotedSchema() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Country2\n" +
                "}\n" +
                "ORDER BY ?v";
        executeAndCompareValues(query,
                ImmutableList.of("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-991>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-992>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-993>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-995>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-996>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-997>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-998>"));
    }

    @Test
    public void testAliasUnquotedColumn() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Country3\n" +
                "}\n" +
                "ORDER BY ?v";
        executeAndCompareValues(query,
                ImmutableList.of("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-991>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-992>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-993>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-995>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-996>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-997>",
                        "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-998>"));
    }
}
