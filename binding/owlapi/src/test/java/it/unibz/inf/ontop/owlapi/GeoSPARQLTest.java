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
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

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
    public void testSelectDistance_Metre() throws Exception {
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
        assertEquals(339241, val, 1.0);
    }

    @Ignore("Triggers an Ontop Bug")
    @Test
    public void testSelectDistance_KiloMetre() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND((geof:distance(?xWkt, ?yWkt, uom:metre)/1000) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339.241, val, 1.0);
    }

    @Test
    public void testSelectDistance_Degree() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:degree) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(3.55, val, 0.01);
    }

    @Test
    public void testSelectDistance_Radian() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:radian) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(0.062, val, 0.001);
    }

    @Test
    public void testSelectBuffer() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:buffer(?xWkt, 1, uom:degree) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertTrue(val.startsWith("POLYGON ((0 1, 0 7,"));
    }

    @Test
    public void testSelectBuffer_Metre() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:buffer(?xWkt, 10000, uom:metre) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertTrue(val.startsWith("POLYGON ((0.9100"));
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

    @Test // line within line
    public void testAskWithin2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":9 a :Geom; geo:asWKT ?xWkt.\n" +
                ":10 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // line within polygon
    public void testAskWithin3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":9 a :Geom; geo:asWKT ?xWkt.\n" +
                ":6 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // point within polygon
    public void testAskWithin4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":11 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // point within linestring
    public void testAskWithin5() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":11 a :Geom; geo:asWKT ?xWkt.\n" +
                ":10 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testAskEquals() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":6 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfEquals(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // unequal if identical shape but different coordinates
    public void testAskEquals2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfEquals(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertFalse(val);
    }

    @Test
    public void testAskDisjoint() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":5 a :Geom; geo:asWKT ?xWkt.\n" +
                ":7 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfDisjoint(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // polygons
    public void testAskIntersects() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfIntersects(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // linestrings
    public void testAskIntersects2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":12 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfIntersects(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testAskTouches() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":7 a :Geom; geo:asWKT ?xWkt.\n" +
                ":8 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfTouches(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Test result is false for 2 overlapping polygons
    public void testAskCrosses() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfCrosses(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertFalse(val);
    }

    @Test // Test result is false for point in polygon / multipoint needed
    public void testAskCrosses2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":11 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfCrosses(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertFalse(val);
    }

    @Test // Test result is false for linestring within linestring
    public void testAskCrosses3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":9 a :Geom; geo:asWKT ?xWkt.\n" +
                ":10 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfCrosses(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertFalse(val);
    }

    @Test // Test result is true for linestring crossing polygon
    public void testAskCrosses4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":12 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfCrosses(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // polygons
    public void testAskContains() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":1 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfContains(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // linestrings
    public void testAskContains2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":9 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfContains(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // point within polygon
    public void testAskContains3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfContains(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // point within linestring
    public void testAskContains4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfContains(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // linestring within polygon
    public void testAskContains5() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":6 a :Geom; geo:asWKT ?xWkt.\n" +
                ":9 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfContains(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testAskOverlaps() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfOverlaps(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testAskOverlaps2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfOverlaps(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // same as contains for polygons
    public void testAskCovers() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":1 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:ehCovers(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // case when it differs vs. contains
    public void testAskCovers2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:ehCovers(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test
    public void testSelectIntersection() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertTrue(val.startsWith("POLYGON ((0.9100"));
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

    private String runQueryAndReturnString(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
            return ind1.getLiteral();
        }
    }
}
