package it.unibz.krdb.odba;


import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Class to test if CONCAT in mapping is working properly.
 * rdfs:label is a concat of literal and variables
 *

 */
public class ConcatNullDatabasesTest {
    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;
    private String owlFile;
    private String obdaFile;
    private Connection sqlConnection;

    final String owlOracle = "src/test/resources/publicaciones/vivo-isf-public-1.6.owl";
    final String obdaOracle = "src/test/resources/publicaciones/vivo-isf-public-1.6.obda";

    final String owlDb2 = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaDb2 = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-db2.obda";

    final String owlH2 = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaH2 = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-h2.obda";

    final String owlMssql = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaMssql = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mssql.obda";

    final String owlMysql = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaMysql = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";

    final String owlPostgres = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    final String obdaPostgres = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-pgsql.obda";



    @Before
    public void setUp()  {

        fac = OBDADataFactoryImpl.getInstance();


    }

    private void databaseSet(int database) throws Exception{
        switch (database){
            case 1 : owlFile = owlOracle;
                     obdaFile = obdaOracle ;
                break;
            case 2 : owlFile = owlDb2;
                     obdaFile = obdaDb2;
                break;
            case 3 : owlFile = owlH2;
                     obdaFile = obdaH2;
                break;
            case 4 : owlFile = owlMssql;
                obdaFile = obdaMssql;
                break;
            case 5 : owlFile = owlMysql;
                obdaFile = obdaMysql;
                break;
            case 6 : owlFile = owlPostgres;
                obdaFile = obdaPostgres;
                break;

        }

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();

        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);

    }


    private void createH2Database() throws Exception {
        try {
            sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questrepository", "fish", "fish");
            java.sql.Statement s = sqlConnection.createStatement();

            try {
                String text = new Scanner( new File("src/test/resources/stockexchange-h2.sql") ).useDelimiter("\\A").next();
                s.execute(text);
                //Server.startWebServer(sqlConnection);

            } catch(SQLException sqle) {
                System.out.println("Exception in creating db from script");
                sqle.printStackTrace();
                throw sqle;
            }

            s.close();
        } catch (Exception exc) {
            try {
                deleteH2Database();
            } catch (Exception e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
    }

    private void deleteH2Database() throws Exception {
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

    @Test //NO SUPPORT
    public void testOracle() throws Exception {

        databaseSet(1);
        QuestPreferences p = new QuestPreferences();

//
        String queryBound = "PREFIX new: <http://addedMapping.org/>\n" +
                "PREFIX : <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX vivo: <http://vivoweb.org/ontology/core#>\n" +
                "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
                "\n" +
                "SELECT DISTINCT  ?cve ?novalue \n" +
                "WHERE { \n" +
                "?cve a new:CompletedDocument .\n" +
                "OPTIONAL {\n" +
                "?cve bibo:status ?novalue .\n" +
                "}\n" +
                "\n" +
                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";



        int results = runTestQuery(p, queryBound);
        assertEquals(18552, results);
    }

    @Test
    public void testDb2() throws Exception {

        databaseSet(2);
        QuestPreferences p = new QuestPreferences();


        String queryBound = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"  +
                "SELECT DISTINCT  ?transaction ?novalue \n" +
                "WHERE { \n" +
                "?transaction a :Transaction .\n" +

                "\n" +
                "OPTIONAL {\n" +
                "?transaction :involvesInstrument ?novalue .\n" +
                "}\n" +

                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";



        int results = runTestQuery(p, queryBound);
        assertEquals(1, results);
    }

    @Test
    public void testH2() throws Exception {
        createH2Database();

        databaseSet(3);

        QuestPreferences p = new QuestPreferences();


        String queryBound = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"  +
                "SELECT DISTINCT  ?transaction ?novalue \n" +
                "WHERE { \n" +
                "?transaction a :Transaction .\n" +

                "\n" +
                "OPTIONAL {\n" +
                "?transaction :involvesInstrument ?novalue .\n" +
                "}\n" +

                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";



        int results = runTestQuery(p, queryBound);
        assertEquals(1, results);

        deleteH2Database();
    }


    @Test //NO SUPPORT
    public void testMssql() throws Exception {

        databaseSet(4);
        QuestPreferences p = new QuestPreferences();

        String queryBound = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"  +
                "SELECT DISTINCT  ?transaction ?novalue \n" +
                "WHERE { \n" +
                "?transaction a :Transaction .\n" +

                "\n" +
                "OPTIONAL {\n" +
                "?transaction :involvesInstrument ?novalue .\n" +
                "}\n" +

                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";

        int results = runTestQuery(p, queryBound);
        assertEquals(1, results);
    }

    @Test
    public void testMysql() throws Exception {

        databaseSet(5);
        QuestPreferences p = new QuestPreferences();

        String queryBound = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"  +
                "SELECT DISTINCT  ?transaction ?novalue \n" +
                "WHERE { \n" +
                "?transaction a :Transaction .\n" +

                "\n" +
                "OPTIONAL {\n" +
                "?transaction :involvesInstrument ?novalue .\n" +
                "}\n" +

                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";

        int results = runTestQuery(p, queryBound);
        assertEquals(1, results);
    }

    @Test
    public void testPostgresql() throws Exception {

        databaseSet(5);
        QuestPreferences p = new QuestPreferences();

        String queryBound = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n"  +
                "SELECT DISTINCT  ?transaction ?novalue \n" +
                "WHERE { \n" +
                "?transaction a :Transaction .\n" +

                "\n" +
                "OPTIONAL {\n" +
                "?transaction :involvesInstrument ?novalue .\n" +
                "}\n" +

                "FILTER(!BOUND(?novalue) )\n" +
                "\n" +
                "}";

        int results = runTestQuery(p, queryBound);
        assertEquals(1, results);
    }


    private int runTestQuery(Properties p, String query) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


                log.debug("Executing query: ");
                log.debug("Query: \n{}", query);

                long start = System.nanoTime();
                QuestOWLResultSet res = st.executeTuple(query);
                long end = System.nanoTime();

                double time = (end - start) / 1000;

                int count = 0;
                if(res.nextRow()) {
                    count += 1;
                    OWLObject nullValue = res.getOWLObject("novalue");

                    log.debug("nullValue " + nullValue);


                    while (res.nextRow()) {
                        count += 1;


                    }

                    log.debug("Total result: {}", count);



                    assertNull(nullValue);

                }

                assertFalse(count == 0);
                log.debug("Elapsed time: {} ms", time);

        return count;



    }



}

