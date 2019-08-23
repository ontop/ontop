package it.unibz.inf.ontop.owlapi;

/**
 * Test case for Rais ontology.
 * Problem with OPTIONAL when the left join is having on the right multiple mappings
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LeftJoinMultipleMatchingTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String owlFile = "src/test/resources/ljoptional/rais-ontology.owl";


    private Connection sqlConnection;
    private OWLConnection conn;
    private OntopOWLReasoner reasoner;

    String URL = "jdbc:h2:mem:raisjunit";
    String USER = "sa";
    String PASSWORD = "";

    @Before
    public void setUp() throws Exception {

        sqlConnection= DriverManager.getConnection(URL, USER, PASSWORD);
        java.sql.Statement s = sqlConnection.createStatement();

        try {
            String text = new Scanner( new File("src/test/resources/ljoptional/rais-create-h2.sql") ).useDelimiter("\\A").next();
            s.execute(text);

        } catch(SQLException sqle) {
            System.out.println("Exception in creating db from script "+sqle);
        }

        s.close();
    }


    @After
    public void tearDown() throws Exception{
        conn.close();
        reasoner.dispose();
        if (!sqlConnection.isClosed()) {
            java.sql.Statement s = sqlConnection.createStatement();
            try {
                dropTables();
            } catch (SQLException sqle) {
                System.out.println("Table not found, not dropping");
            } finally {
                s.close();
                sqlConnection.close();
            }
        }
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = sqlConnection.createStatement();

        FileReader reader = new FileReader("src/test/resources/ljoptional/rais-drop-h2.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        st.close();
        sqlConnection.commit();
    }


    private void runTests(String obdaFile) throws Exception {

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
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();


        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load("src/test/resources/ljoptional/rais-ontology.q");

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                TupleOWLResultSet res = st.executeSelectQuery(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.hasNext()) {
                    res.next();
                    count += 1;
                }
                log.debug("Total result: {}", count);
                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);
            }
        }
    }


    @Test
    public void testRaisQueries() throws Exception {
        runTests("src/test/resources/ljoptional/rais-ontology-small.obda");
    }


    @Test
    public void testOptionalQuery() throws Exception {
        String optional = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rais: <http://www.ontorais.de/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?ao ?date WHERE {\n" +
                "?ao a rais:ArchiveObject .\n" +
                " OPTIONAL{ ?ao rais:archivalDate ?date.}\n" +
                "}\n";

        assertEquals(2, runTestQuery("src/test/resources/ljoptional/rais-ontology-small.obda", optional));
    }


    private int runTestQuery(String obdaFile, String query) throws Exception {

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
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();

        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        long start = System.nanoTime();
        TupleOWLResultSet  res = st.executeSelectQuery(query);
        long end = System.nanoTime();

        double time = (end - start) / 1000;

        int count = 0;
        while (res.hasNext()) {
            res.next();
            count += 1;
        }
        log.debug("Total result: {}", count);

        assertFalse(count != 2);

        log.debug("Elapsed time: {} ms", time);

        return count;
    }
}

