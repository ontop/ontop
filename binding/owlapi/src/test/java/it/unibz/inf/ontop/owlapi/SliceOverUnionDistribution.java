package it.unibz.inf.ontop.owlapi;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static it.unibz.inf.ontop.utils.OWLAPITestingTools.readFromFile;
import static junit.framework.TestCase.assertEquals;

/**
 * Checks that that SLICE does not distribute over UNION (reproduces a bug).
 */
public class SliceOverUnionDistribution {

    private static final String CREATE_SCRIPT = "src/test/resources/slice/create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/slice/drop.sql";
    private static final String OWL_FILE = "src/test/resources/slice/university.ttl";
    private static final String MAPPING_FILE = "src/test/resources/slice/university.obda";
    private static final String QUERY_FILE = "src/test/resources/slice/slice.rq";

    private static final String URL = "jdbc:h2:mem:slice";
    private static final String USER = "sa";
    private static final String PASSWORD = "sa";

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
        executeFromFile(conn, CREATE_SCRIPT);
    }

    @After
    public void tearDown() throws Exception {
        executeFromFile(conn, DROP_SCRIPT);
        conn.close();
    }

    @Test
    public void testQuery() throws Exception {
        String query = readFromFile(QUERY_FILE);
        execute(query, 1);
    }


    private void execute(String query, int expectedCardinality) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(MAPPING_FILE)
                .ontologyFile(OWL_FILE)
                .jdbcUrl(URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        int i = 0;
        while (rs.hasNext()) {
            OWLBindingSet o = rs.next();
            i++;
        }
        assertEquals(expectedCardinality, i);
        conn.close();
        reasoner.dispose();
    }
}
