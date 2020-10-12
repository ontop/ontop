package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WktTest {

    private static final String CREATE_SCRIPT = "src/test/resources/test/wkt/wkt_test_create.sql";
    private static final String ODBA_FILE = "src/test/resources/test/wkt/wkt_test.obda";
    private static final String OWL_FILE = "src/test/resources/test/wkt/wkt_test.owl";
    private static final String PROPERTY_FILE = "src/test/resources/test/wkt/wkt_test.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(WktTest.class);

    private Connection conn;


    @Before
    public void setUp() throws Exception {

        String url = "jdbc:h2:mem:wkt";
        String username = "sa";
        String password = "sa";

        conn = DriverManager.getConnection(url, username, password);
        executeFromFile(conn, CREATE_SCRIPT);
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
    }

    @Test
    public void testWkt1() throws Exception {

        String query =  "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?s geo:asWKT ?v" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "POLYGON((-77.089005 38.913574, -77.029953 38.913574, -77.029953 38.886321, -77.089005 38.886321, -77.089005 38.913574))");
        checkReturnedValuesAndReturnSql(query, expectedValues);
    }

    private void checkReturnedValuesAndReturnSql(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(ODBA_FILE)
                .propertyFile(PROPERTY_FILE)
                .ontologyFile(OWL_FILE)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLLiteral ind1 = bindingSet.getOWLLiteral("v");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.getLiteral());
                    LOGGER.debug("v = " + ind1);
                    i++;
                }
            }
        }
        finally {
            conn.close();
            reasoner.dispose();
        }
        assertEquals(expectedValues, returnedValues);
        assertEquals(expectedValues.size(), i);
    }
}
