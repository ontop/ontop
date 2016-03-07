package it.unibz.krdb.odba;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import it.unibz.krdb.obda.utils.SQLScriptRunner;
import org.junit.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class MultipleSchemasTestH2 {

    private static OBDADataFactory fac;
    private static QuestOWLConnection conn;

    static Logger log = LoggerFactory.getLogger(MultipleSchemasTestH2.class);
    private static OBDAModel obdaModel;
    private static OWLOntology ontology;

    final static String owlfile = "src/test/resources/multiple-schema-test.owl";
    final static String obdafile = "src/test/resources/multiple-schema-test.obda";
    private static QuestOWL reasoner;

    private static Connection sqlConnection;

    @Before
    public void init() {

    }

    @After
    public void after() {

    }


    @BeforeClass
    public static void setUp() throws Exception {

        String url = "jdbc:h2:tcp://localhost/./helloworld";
        String username = "sa";
        String password = "";

        System.out.println("Test");
        fac = OBDADataFactoryImpl.getInstance();

        try {

            sqlConnection = DriverManager.getConnection(url, username, password);

            FileReader reader = new FileReader("src/test/resources/multiple-schema-test.sql");
            BufferedReader in = new BufferedReader(reader);
            SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
            runner.runScript(in);

            // Loading the OWL file
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

            // Loading the OBDA data
            fac = OBDADataFactoryImpl.getInstance();
            obdaModel = fac.getOBDAModel();

            ModelIOManager ioManager = new ModelIOManager(obdaModel);
            ioManager.load(obdafile);

            QuestPreferences p = new QuestPreferences();
            p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
            p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
            // Creating a new instance of the reasoner
            QuestOWLFactory factory = new QuestOWLFactory();
            QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
            reasoner = factory.createReasoner(ontology, config);

            // Now we are ready for querying
            conn = reasoner.getConnection();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            log.error(e.getMessage(), e);
            throw e;
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {

        FileReader reader = new FileReader("src/test/resources/multiple-schema-test.sql.drop");
        BufferedReader in = new BufferedReader(reader);
        SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
        runner.runScript(in);

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

    private void runTests(String query, int numberOfResults) throws Exception {
        QuestOWLStatement st = conn.createStatement();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);

            int count = 0;
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("x");
                System.out.println("Result " + ind1.toString());
                count += 1;
            }
            org.junit.Assert.assertTrue(count == numberOfResults);

        } catch (Exception e) {
            throw e;
        } finally {
            try {
            } catch (Exception e) {
                st.close();
                org.junit.Assert.assertTrue(false);
            }
            conn.close();
            reasoner.dispose();
        }
    }

    /**
     * Test use of two aliases to same table
     *
     * @throws Exception
     */
    @Test
    public void testSingleColum() throws Exception {
        String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%22ivore> a ?x}";
        runTests(query, 1);
    }
}
