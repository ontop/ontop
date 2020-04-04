package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;

public class CanonicalEntitiesTest {
    private Connection conn;

    final String owlfile = "src/test/resources/test/simplemapping.owl";
    final String obdafile = "src/test/resources/test/simplemapping.obda";

    private static final String url = "jdbc:h2:mem:questjunitdb";
    private static final String username = "sa";
    private static final String password = "";

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection(url, username, password);
        executeFromFile(conn, "src/test/resources/test/simplemapping-create-h2.sql");
    }

    @After
    public void tearDown() throws Exception {
        executeFromFile(conn, "src/test/resources/test/simplemapping-drop-h2.sql");
        conn.close();
    }

    @Test
    public void test_canonical_entities() throws Exception {
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .properties(new Properties())
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableExistentialReasoning(true)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        String query = "PREFIX : <http://it.unibz.inf/obda/test/simple#> SELECT ?S ?P ?O WHERE { ?S ?S ?P }";
        try (OWLConnection conn = reasoner.getConnection();
             OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
        }
    }
}