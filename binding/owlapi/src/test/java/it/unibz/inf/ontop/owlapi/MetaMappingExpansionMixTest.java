package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;

public class MetaMappingExpansionMixTest {
    private Connection sqlConnection;
    private OWLConnection conn;

    private static final String OWL_FILE = "src/test/resources/metamapping/metamapping.owl";
    private static final String OBDA_FILE = "src/test/resources/metamapping/metamapping.obda";

    private static final String url = "jdbc:h2:mem:questjunitdb0";
    private static final String username = "sa";
    private static final String password = "";

    @Before
    public void setUp() throws Exception {
        sqlConnection = DriverManager.getConnection(url, username, password);
        executeFromFile(sqlConnection,"src/test/resources/metamapping/metamapping.sql");

        // Creating a new instance of the reasoner
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(OWL_FILE)
                .nativeOntopMappingFile(OBDA_FILE)
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .enableTestMode()
                .build();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopOWLReasoner reasoner = factory.createReasoner(configuration);

        // Now we are ready for querying
        conn = reasoner.getConnection();
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
        sqlConnection.close();
    }

    @Test
    public void test_proper_mapping_split() throws Exception {
        String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x a :Broker }";

        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            OWLBindingSet bindingSet = rs.next();
            OWLObject ind1 = bindingSet.getOWLObject("x");
            bindingSet = rs.next();
            OWLObject ind2 = bindingSet.getOWLObject("x");
            bindingSet = rs.next();
            OWLObject ind3 = bindingSet.getOWLObject("x");
        }
    }
}
