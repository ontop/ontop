package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.SQLScriptRunner;
import org.junit.*;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;


public class MultipleSchemasH2Test {

    private static OWLConnection conn;

    static Logger log = LoggerFactory.getLogger(MultipleSchemasH2Test.class);

    final static String owlfile = "src/test/resources/multischema/multiple-schema-test.owl";
    final static String obdafile = "src/test/resources/multischema/multiple-schema-test.obda";
    private static OntopOWLReasoner reasoner;

    private static Connection sqlConnection;

    @BeforeClass
    public static void setUp() throws Exception {

        String url = "jdbc:h2:mem:helloworld";
        String username = "sa";
        String password = "";

        sqlConnection = DriverManager.getConnection(url, username, password);
        executeFromFile(sqlConnection, "src/test/resources/multischema/multiple-schema-test.sql");

        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableTestMode()
                .build();
        reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        conn = reasoner.getConnection();
    }

    @AfterClass
    public static void tearDown() throws Exception {

        FileReader reader = new FileReader("src/test/resources/multischema/multiple-schema-test.sql.drop");
        BufferedReader in = new BufferedReader(reader);
        SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
        runner.runScript(in);

        conn.close();
        reasoner.dispose();
        if (!sqlConnection.isClosed()) {
            java.sql.Statement s = sqlConnection.createStatement();
            try {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            }
            finally {
                s.close();
                sqlConnection.close();
            }
        }
    }

    private void runTests(String query, int numberOfResults) throws Exception {
        OWLStatement st = conn.createStatement();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);

            int count = 0;
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("x");
                System.out.println("Result " + ind1.toString());
                count += 1;
            }
            Assert.assertEquals(count, numberOfResults);

        }
        finally {
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
        String query = "PREFIX : <http://www.ontop.org/> SELECT ?x WHERE {<http://www.ontop.org/test-Cote%20D%27ivore> a ?x}";
        runTests(query, 1);
    }
}
