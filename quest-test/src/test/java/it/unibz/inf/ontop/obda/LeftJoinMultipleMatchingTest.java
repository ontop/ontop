package it.unibz.inf.ontop.obda;

/**
 * Test case for Rais ontology.
 * Problem with OPTIONAL when the left join is having on the right multiple mappings
 */

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.QueryIOManager;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import it.unibz.inf.ontop.querymanager.QueryController;
import it.unibz.inf.ontop.querymanager.QueryControllerGroup;
import it.unibz.inf.ontop.querymanager.QueryControllerQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LeftJoinMultipleMatchingTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String owlFile = "src/test/resources/optional/rais-ontology.owl";


    private Connection sqlConnection;
    private QuestOWLConnection conn;
    private QuestOWL reasoner;

    @Before
    public void setUp() throws Exception {

        sqlConnection= DriverManager.getConnection("jdbc:h2:mem:raisjunit","sa", "");
        java.sql.Statement s = sqlConnection.createStatement();

        try {
            String text = new Scanner( new File("src/test/resources/optional/rais-create-h2.sql") ).useDelimiter("\\A").next();
            s.execute(text);

        } catch(SQLException sqle) {
            System.out.println("Exception in creating db from script");
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
                s.execute("DROP ALL OBJECTS DELETE FILES");
            } catch (SQLException sqle) {
                System.out.println("Table not found, not dropping");
            } finally {
                s.close();
                sqlConnection.close();
            }
        }
    }

    private void runTests(String obdaFile) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .build();
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


        QueryController qc = new QueryController();
        QueryIOManager qman = new QueryIOManager(qc);
        qman.load("src/test/resources/optional/rais-ontology.q");

        for (QueryControllerGroup group : qc.getGroups()) {
            for (QueryControllerQuery query : group.getQueries()) {

                log.debug("Executing query: {}", query.getID());
                log.debug("Query: \n{}", query.getQuery());

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query.getQuery());
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                while (res.nextRow()) {
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
        runTests("src/test/resources/optional/rais-ontology-unmodified.obda");
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

        assertEquals(0, runTestQuery("src/test/resources/optional/rais-ontology-small.obda", optional));
    }

    private int runTestQuery(String obdaFile, String query) throws Exception {

        // Creating a new instance of the reasoner
    	QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .build();
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        long start = System.nanoTime();
        QuestOWLResultSet res = st.executeTuple(query);
        long end = System.nanoTime();

        double time = (end - start) / 1000;

        int count = 0;
        while (res.nextRow()) {
            count += 1;
        }
        log.debug("Total result: {}", count);

        assertFalse(count == 0);

        log.debug("Elapsed time: {} ms", time);

        return count;
    }
}

