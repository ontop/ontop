package it.unibz.inf.ontop.owlapi;

/**
 * Test case for Rais ontology.
 * Problem with OPTIONAL when the left join is having on the right multiple mappings
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;

public class LeftJoinMultipleMatchingTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String owlFile = "src/test/resources/ljoptional/rais-ontology.owl";
    private static final String obdaFile = "src/test/resources/ljoptional/rais-ontology-small.obda";

    private Connection sqlConnection;

    static final String URL = "jdbc:h2:mem:raisjunit";
    static final String USER = "sa";
    static final String PASSWORD = "";

    @Before
    public void setUp() throws Exception {
        sqlConnection = DriverManager.getConnection(URL, USER, PASSWORD);
        executeFromFile(sqlConnection, "src/test/resources/ljoptional/rais-create-h2.sql");
    }

    @After
    public void tearDown() throws Exception{
        if (!sqlConnection.isClosed()) {
            executeFromFile(sqlConnection, "src/test/resources/ljoptional/rais-drop-h2.sql");
            sqlConnection.close();
        }
    }

    @Test
    public void testRaisNoOptional() throws Exception {
        executeSelectQuery("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rais: <http://www.ontorais.de/>\n" +
                "SELECT DISTINCT ?ao ?title ?date WHERE {\n" +
                "?ao a rais:ArchiveObject .\n" +
                "?ao rais:title ?title.\n" +
                "?ao rais:archivalDate ?date.\n" +
                "}");
    }

    @Test
    public void testRaisOptional() throws Exception {
        executeSelectQuery("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rais: <http://www.ontorais.de/>\n" +
                "SELECT DISTINCT ?ao ?title ?date WHERE {\n" +
                "?ao a rais:ArchiveObject .\n" +
                "?ao rais:title ?title.\n" +
                " OPTIONAL{ ?ao rais:archivalDate ?date.}\n" +
                "}");
    }

    @Test
    public void testOptionalQuery() throws Exception {
        executeSelectQuery("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rais: <http://www.ontorais.de/>\n" +
                "SELECT DISTINCT ?ao ?date WHERE {\n" +
                "?ao a rais:ArchiveObject .\n" +
                " OPTIONAL{ ?ao rais:archivalDate ?date.}\n" +
                "}\n");
    }

    private void executeSelectQuery(String query) throws Exception {

        // Creating a new instance of the reasoner
    	OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .jdbcUrl(URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        try (OWLConnection conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement()) {

            log.debug("Executing query: ");
            log.debug("Query: \n{}", query);

            long start = System.nanoTime();
            TupleOWLResultSet res = st.executeSelectQuery(query);
            long end = System.nanoTime();

            int count = 0;
            while (res.hasNext()) {
                OWLBindingSet binding = res.next();
                count++;
            }
            log.debug("Total result: {}", count);
            assertEquals(2, count);
            log.debug("Elapsed time: {} ms", (end - start) / 1_000_000);
        }
        finally {
            reasoner.dispose();
        }
    }
}

