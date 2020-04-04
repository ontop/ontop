package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
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
import org.semanticweb.owlapi.model.OWLLiteral;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertTrue;

public class FunctionAliasTest {

    private static final String CREATE_SCRIPT = "src/test/resources/alias/h2_alias_function.sql";
    private static final String DROP_SCRIPT = "src/test/resources/alias/h2_alias_function_drop.sql";
    private static final String OWL_FILE = "src/test/resources/alias/alias_function.owl";
    private static final String ODBA_FILE = "src/test/resources/alias/alias_function.obda";
    private static final String PROPERTY_FILE = "src/test/resources/alias/alias_function.properties";

    private Connection conn;


    @Before
    public void setUp() throws Exception {
        String url = "jdbc:h2:mem:movie";
        String username = "sa";
        String password = "sa";

        conn = DriverManager.getConnection(url, username, password);
        executeFromFile(conn, CREATE_SCRIPT);
    }

    @After
    public void tearDown() throws Exception {
        executeFromFile(conn, DROP_SCRIPT);
        conn.close();
    }

    @Test
    public void testAlias() throws Exception {

        String query =  "PREFIX :	<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "\n" +
                "SELECT  ?x " +
                "WHERE {?y :title ?x . \n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, ImmutableList.of(
                "the sun", "winter is coming"));

        System.out.println("SQL Query: \n" + sql);

    }

    private String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(ODBA_FILE)
                .ontologyFile(OWL_FILE)
                .propertyFile(PROPERTY_FILE)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        String sql;
        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try (OntopOWLConnection conn = reasoner.getConnection();
             OntopOWLStatement st = conn.createStatement();) {
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
                OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.getLiteral());
                    System.out.println(ind1.getLiteral());
                    i++;
                }
            }
        }
        finally {
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

        return sql;
    }
}
