package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Reproduces bug #263
 **/

public class SubLiftTest {
    private static final String CREATE_SCRIPT = "src/test/resources/subLift/create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/subLift/drop.sql";
    private static final String OWL_FILE = "src/test/resources/subLift/test.owl";
    private static final String MAPPING_FILE = "src/test/resources/subLift/test.obda";
    private static final ToStringRenderer renderer = ToStringRenderer.getInstance();

    private static final String URL = "jdbc:h2:mem:job";
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

        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?instance ?predicate_object ?instanceLabel ?object ?objectLabel " +
                "WHERE " +
                "{ ?instance  ?predicate_object  ?object " +
                "OPTIONAL " +
                "{ ?instance  <http://www.w3.org/2000/01/rdf-schema#label>  ?instanceLabel}" +
                " OPTIONAL " +
                "{ ?object  <http://www.w3.org/2000/01/rdf-schema#label>  ?objectLabel} } " +
                "VALUES ?instance { <http://www.semanticweb.org/test#person1> }";


        int expectedCardinality = 3;
        String sql = execute(query, expectedCardinality);

        System.out.println("SQL Query: \n" + sql);
    }


    private String execute(String query, int expectedCardinality) throws Exception {

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
        String sql;

        int i = 0;
        try {
            IQ executableQuery = st.getExecutableQuery(query);
            sql = ((NativeNode) executableQuery.getTree().getChildren().get(0)).getNativeQueryString();
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                Iterator<OWLBinding> it = bindingSet.iterator();
                System.out.println(i);
                while (it.hasNext()) {
                    OWLBinding b = it.next();
                    System.out.println(("\t" + b.getName() + "\t" + stringify(b.getValue())));
                }
                assertTrue(
                        !stringify(bindingSet.getBinding("object").getValue()).equals("<http://www.semanticweb.org/test#job1>") ||
                                stringify(bindingSet.getBinding("objectLabel").getValue()).equals("\"Job 1\"^^xsd:string")
                );
                assertTrue(
                        stringify(bindingSet.getBinding("predicate_object").getValue()).equals("<http://www.semanticweb.org/test#hasJob>") ||
                                bindingSet.getBindingNames().size() == 4
                );
                i++;
            }
            assertEquals(expectedCardinality, i);
        }
        finally {
            conn.close();
            reasoner.dispose();
        }
        return sql;
    }

    private String stringify(OWLObject owlObject) {
        return renderer.getRendering(owlObject);
    }
}
