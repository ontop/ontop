package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.impl.SQLExecutableQuery;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
    public void testAlias() throws Exception {

        String query =  "PREFIX :	<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "\n" +
                "SELECT  ?x " +
                "WHERE {?y :title ?x . \n" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "the sun", "winter is coming");

        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

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
                OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.getLiteral());
                    System.out.println(ind1.getLiteral());
                    i++;
                }
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
