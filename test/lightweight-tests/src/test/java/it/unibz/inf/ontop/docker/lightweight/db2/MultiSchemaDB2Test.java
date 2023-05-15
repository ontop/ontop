package it.unibz.inf.ontop.docker.lightweight.db2;

import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URIs directly.
 */
@DB2LightweightTest
public class MultiSchemaDB2Test extends AbstractDockerRDF4JTest {
    private static final String OBDA_FILE = "/stockexchange/db2/multischema-db2.obda";
    private static final String OWL_FILE = "/stockexchange/multischema.owl";
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
     * Test use of two aliases to same table
     */
    @Test
    public void testOneSchema() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Address\n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testTableOneSchema() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Broker\n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testAliasOneSchema() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v a :Worker\n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testSchemaWhere() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v ?r \n" +
                "WHERE {\n" +
                " ?v :isBroker ?r" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testMultischema() {
        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                " ?v :hasFile ?r \n" +
                "}";
        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }
}
