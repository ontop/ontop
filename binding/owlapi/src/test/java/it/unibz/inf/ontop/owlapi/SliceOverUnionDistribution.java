package it.unibz.inf.ontop.owlapi;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.stream.Collectors.joining;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Makes sure that SLICE does not distribute over UNION (reproduces a bug).
 * Warning: assumes that the implementation of LIMIT is deterministic
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
        Statement st = conn.createStatement();
        st.executeUpdate(readFile(CREATE_SCRIPT));
        conn.commit();
    }

    @After
    public void tearDown() throws Exception {
        dropTables();
        conn.close();
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();
        st.executeUpdate(readFile(DROP_SCRIPT));
        st.close();
        conn.commit();
    }

    @Test
    public void testQuery() throws Exception {
        String query = readFile(QUERY_FILE);
        execute(query, 1);
    }

    private String readFile(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath)).collect(joining());
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
            rs.next();
            i++;
        }
        assertTrue(i == 1);
        conn.close();
        reasoner.dispose();
    }
}
