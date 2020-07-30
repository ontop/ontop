package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertTrue;

public class RepeatedColumnNameTest {
    private static final String CREATE_SCRIPT = "src/test/resources/test/repeatedCN/repeatedCN_create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/test/repeatedCN/repeatedCN_drop.sql";
    private static final String OWL_FILE = "src/test/resources/test/repeatedCN/repeatedCN_test.owl";
    private static final String R2RML_FILE = "src/test/resources/test/repeatedCN/repeatedCN_test.ttl";

    private static final String URL = "jdbc:h2:mem:professor";
    private static final String USER = "sa";
    private static final String PASSWORD = "sa";
    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatedColumnNameTest.class);

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

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT * {\n" +
                "?p a :Professor.\n" +
                "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1/1>");
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/2/2>");
        String sql = execute(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

//        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("professors"));
//        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("PROFESSORS"));
    }



    private String execute(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(R2RML_FILE)
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
        String sql;

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            IQ executableQuery = st.getExecutableQuery(query);
            sql = Optional.of(executableQuery.getTree())
                    .filter(t -> t instanceof UnaryIQTree)
                    .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                    .filter(n -> n instanceof NativeNode)
                    .map(n -> ((NativeNode) n).getNativeQueryString())
                    .orElseThrow(() -> new RuntimeException("The executable query was expected to have a construction and a native node"));
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("p");
                returnedValues.add(ind1.toString());
                java.lang.System.out.println(ind1);
                i++;
            }
        }
        finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

        return sql;
    }
}
