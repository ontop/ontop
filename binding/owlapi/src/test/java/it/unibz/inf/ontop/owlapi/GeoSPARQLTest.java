package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.h2gis.ext.H2GISExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class GeoSPARQLTest {

    private static final String owlFile = "src/test/resources/geosparql/geosparql.owl";
    private static final String obdaFile = "src/test/resources/geosparql/geosparql-h2.obda";
    private static final String propertyFile = "src/test/resources/geosparql/geosparql-h2.properties";

    private OntopOWLReasoner reasoner;
    private OWLConnection conn;
    private Connection sqlConnection;

    @Before
    public void setUp() throws Exception {

        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:geoms","sa", "");
        H2GISExtension.load(sqlConnection);
        executeFromFile(sqlConnection,"src/test/resources/geosparql/create-h2.sql");

//        org.h2.tools.Server.startWebServer(sqlConnection);

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        reasoner = factory.createReasoner(config);
        conn = reasoner.getConnection();
    }

    @After
    public void tearDown() throws Exception {
        conn.close();
        reasoner.dispose();
        if (!sqlConnection.isClosed()) {
            try (java.sql.Statement s = sqlConnection.createStatement()) {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            }
            finally {
                sqlConnection.close();
            }
        }
    }

    @Test
    public void testSelectWithin() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?x ?y WHERE {\n" +
                "?x a :Geom; geo:asWKT ?xWkt.\n" +
                "?y a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt) && ?x != ?y)\n" +
                "}\n";
        String val = runQueryReturnIndividual(query);
        assertEquals("<http://ex.org/1>", val);
    }

    @Test
    public void testAskWithin() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testSelectDistance() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
//                "BIND((geof:distance(?xWkt, ?yWkt, uom:metre)/1000) as ?x) .\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
        //        "BIND(geof:distance(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(530571, val, 1.0);
    }

    private String runQueryReturnIndividual(String query) throws OWLException {
        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("x");
            String retval = ind1.toString();
            return retval;
        }
    }

    private boolean runQueryAndReturnBooleanX(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            BooleanOWLResultSet rs = st.executeAskQuery(query);
            boolean retval = rs.getValue();
            return retval;
        }
    }

    private double runQueryAndReturnDoubleX(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
            return ind1.parseDouble();
        }
    }
}
