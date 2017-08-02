package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class ConditionalAsLeftJoinTest {

    private static final String CREATE_SCRIPT = "src/test/resources/test/conditional_leftjoin/conditional_leftjoin_create.sql";
    private static final String DROP_SCRIPT = "src/test/resources/test/conditional_leftjoin/conditional_leftjoin_drop.sql";
    private static final String OWL_FILE = "src/test/resources/test/conditional_leftjoin/conditional_leftjoin_test.owl";
    private static final String ODBA_FILE = "src/test/resources/test/conditional_leftjoin/conditional_leftjoin_test.obda";

    private Connection conn;

    private static final String URL = "jdbc:h2:mem:restaurant";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "sa";

    final static Logger log = LoggerFactory.getLogger(ConditionalAsLeftJoinTest.class);

    @Before
    public void setUp() throws Exception {

        conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Statement st = conn.createStatement();

        FileReader reader = new FileReader(CREATE_SCRIPT);

        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        conn.commit();
    }

    @After
    public void tearDown() throws Exception {
        dropTables();
        conn.close();
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();

        FileReader reader = new FileReader(DROP_SCRIPT);
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        st.close();
        conn.commit();
    }

    @Test
    public void testQuery() throws Exception {

        String query = "PREFIX : <http://www.semanticweb.org/ontologies/2016/10/untitled-ontology-2#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xml: <http://www.w3.org/XML/1998/namespace>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?a \n" +
                "WHERE {\n" +
                " :Tartaruga a :VegetarianRestaurant .\n" +
                "OPTIONAL { :Tartaruga :hasAddress ?a }\n" +
                "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://www.semanticweb.org/ontologies/2016/10/untitled-ontology-2#aa>");
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        log.debug("SQL Query: \n" + sql);
    }

    private String checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(ODBA_FILE)
                .ontologyFile(OWL_FILE)
                .jdbcUrl(URL)
                .jdbcUser(USERNAME)
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
            ExecutableQuery executableQuery = st.getExecutableQuery(query);
            if (! (executableQuery instanceof SQLExecutableQuery))
                throw new IllegalStateException("A SQLExecutableQuery was expected");
            sql = ((SQLExecutableQuery)executableQuery).getSQL();
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("a");
                returnedValues.add(ind1.toString());
                log.debug("Returned values:" + ind1);
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
