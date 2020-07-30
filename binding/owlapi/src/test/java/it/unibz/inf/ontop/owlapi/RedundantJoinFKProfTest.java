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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RedundantJoinFKProfTest {

    private static final String CREATE_SCRIPT = "src/test/resources/test/redundant_join/redundant_join_fk_create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/test/redundant_join/redundant_join_fk_drop.sql";
    private static final String OWL_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.owl";
    private static final String ODBA_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.obda";
    private static final String NO_OPTIMIZATION_MSG = "The table professors should not be used";

    private static final String URL = "jdbc:h2:mem:professor";
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

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT * {\n" +
                "?p a :Professor ; :teaches ?c .\n" +
                "}\n" +
                "ORDER BY ?p";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>");
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/1>");
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/3>");
        expectedValues.add("<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#professor/8>");
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("professors"));
        assertFalse(NO_OPTIMIZATION_MSG, sql.contains("PROFESSORS"));
    }

    private String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(ODBA_FILE)
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
                    .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + executableQuery));
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("p");
                // log.debug(ind1.toString());
                returnedValues.add(ind1.toString());
                java.lang.System.out.println(ind1);
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

        return sql;
    }
}
