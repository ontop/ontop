package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
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
import static junit.framework.TestCase.*;

public class GeoSPARQLTest {

    private static final String owlFile = "src/test/resources/geosparql/geosparql.owl";
    private static final String obdaFile = "src/test/resources/geosparql/geosparql-h2.obda";
    private static final String propertyFile = "src/test/resources/geosparql/geosparql-h2.properties";

    private OntopOWLReasoner reasoner;
    private OWLConnection conn;
    private Connection sqlConnection;

    @Before
    public void setUp() throws Exception {

        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:geoms", "sa", "");
        H2GISExtension.load(sqlConnection);
        executeFromFile(sqlConnection, "src/test/resources/geosparql/create-h2.sql");

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
            } finally {
                sqlConnection.close();
            }
        }
    }

    @Test // Polygon within polygon
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

    @Test // Case when no SRID is defined - Default SRID is EPSG4326
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
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339241, val, 1.0);
    }

    @Test // Case when SRIDs defined are both CRS84
    public void testSelectDistance_Metre_CRS84() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/crs84/22> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/crs84/23> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339241, val, 1.0);
    }

    @Test // Case when SRIDs defined are both EPSG4326
    public void testSelectDistance_Metre_EPSG4326() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg4326/24> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg4326/25> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339241, val, 1.0);
    }

    @Test // Case when SRIDs defined are both EPSG3044 (Non WGS84) - Cartesian
    public void testSelectDistance_Metre_EPSG3044() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg3044/21> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg3044/26> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(1.41, val, 0.1);
    }

    @Test
    public void testSelectDistance_Kilometre() throws Exception {
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
    public void testSelectDistance_Kilometre_CRS84() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/crs84/22> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/crs84/23> a :Geom; geo:asWKT ?yWkt.\n" +
                //":3 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND((geof:distance(?xWkt, ?yWkt, uom:metre)/1000) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339.241, val, 1.0);
    }

    @Test
    public void testSelectDistance_Kilometre_EPSG4326() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg4326/24> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg4326/25> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND((geof:distance(?xWkt, ?yWkt, uom:metre)/1000) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339.241, val, 1.0);
    }

    @Test // Test with hardcoded NoSRID input
    public void testSelectDistance_Kilometre_fixedinput() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND((geof:distance(?xWkt, 'POINT(-0.0754 51.5055)'^^geo:wktLiteral, uom:metre)/1000) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(339.241, val, 1.0);
    }

    @Test // Test fails due to template hard coded input
    public void testSelectDistance_Kilometre_EPSG4326_fixedinput() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/crs84/22> a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND((geof:distance(?xWkt, '<http://www.opengis.net/def/crs/OGC/1.3/CRS84> POINT(-0.0754 51.5055)'^^geo:wktLiteral, uom:metre)/1000) as ?x) .\n" +
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
        //assertEquals(3.55, val, 0.01);
        assertEquals(3.05, val, 0.01);
    }

    @Test
    public void testSelectDistance_Degree_EPSG4326() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg4326/24> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg4326/25> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:degree) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(3.05, val, 0.01);
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
        assertEquals(0.053, val, 0.001);
    }

    @Test // Test distance function when an input is not a point
    public void testSelectDistance_Radian_NonPoints() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":5 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:radian) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(0.0245, val, 0.001);
    }

    @Test // Case when ST_DWITHIN might be faster
    public void testSelectDistance_Metre_DWithin() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "ASK WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "FILTER(?x < 350000) .\n" +
                "}\n";
        //double val = runQueryAndReturnDoubleX(query);
        //assertEquals(339241, val, 1.0);
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }


    @Test // Case when WKT is from a template  "POINT ({longitude} {latitude})"^^geo:wktLiteral
    public void testSelectDistance_long_lat() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/point/3> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/point/4> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "FILTER(?x < 350000) .\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Case when WKT is from template "<http://www.opengis.net/def/crs/EPSG/0/3044> POINT ({longitude} {latitude})"^^geo:wktLiteral
    public void testSelectDistance_Metre_EPSG3044_long_lat() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg3044/21> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg3044/26> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?x) .\n" +
                "}\n";
        double val = runQueryAndReturnDoubleX(query);
        assertEquals(1.41, val, 0.1);
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
    public void testSelectBuffer2() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:buffer(geof:buffer(?xWkt, 1, uom:degree), 1, uom:degree) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertTrue(val.startsWith("POLYGON ((-0.99"));
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

    @Test // Case when SRIDs defined are both EPSG3044 (Non WGS84) - Cartesian
    public void testSelectBuffer_Metre_EPSG3044() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg3044/21> a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:buffer(?xWkt, 10000, uom:metre) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertTrue(val.startsWith("POLYGON ((678682"));
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

    @Test // point within line
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

    @Test // polygon within polygon with overlapping edge
    public void testAskWithin6() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":7 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // polygon not fully within other polygon
    public void testAskWithin7() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":1 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:sfWithin(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertFalse(val);
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

    @Test // test intersect with user input geometry (not template)
    public void testAskIntersects3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                "FILTER (geof:sfIntersects(?xWkt, 'POLYGON((3 3, 8 3, 8 6, 3 6, 3 3))'^^geo:wktLiteral))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    // it.unibz.inf.ontop.owlapi.exception.OntopOWLException: i
    // t.unibz.inf.ontop.exception.OntopReformulationException: java.lang.IllegalArgumentException:
    // SRIDs do not match: <http://www.opengis.net/def/crs/OGC/1.3/CRS84> and <http://www.opengis.net/def/crs/EPSG/0/3044>
    @Test(expected = OntopOWLException.class) // test intersect with user input geometry (template)
    public void testAskIntersects4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                "FILTER (geof:sfIntersects(?xWkt, '<http://www.opengis.net/def/crs/EPSG/0/3044> POLYGON((3 3, 8 3, 8 6, 3 6, 3 3))'^^geo:wktLiteral))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // test intersect with user input geometry (template)
    public void testAskIntersects5() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                "FILTER (geof:sfIntersects(?xWkt, '<http://www.opengis.net/def/crs/OGC/1.3/CRS84> POLYGON((3 3, 8 3, 8 6, 3 6, 3 3))'^^geo:wktLiteral))\n" +
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

    @Test // polygon vs. polygon intersection is a polygon
    public void testSelectIntersection() throws Exception {
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
        //assertFalse(val.startsWith("POLYGON ((0.9100"));
        //assertEquals("POLYGON ((2 2, 7 2, 7 5, 2 5, 2 2))", val);
        assertEquals("POLYGON ((2 2, 2 5, 7 5, 7 2, 2 2))", val);
    }

    @Test // polygon vs. polygon intersection is a line
    public void testSelectIntersection2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":13 a :Geom; geo:asWKT ?xWkt.\n" +
                ":14 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        //assertEquals("LINESTRING (0 3, 3 3)", val);
        assertEquals("LINESTRING (3 3, 0 3)", val);
    }

    @Test // polygon vs. polygon intersection is a point
    public void testSelectIntersection3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":7 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POINT (2 2)", val);
    }

    @Test // polygon vs. line intersection is a line
    public void testSelectIntersection4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("LINESTRING (1 2, 8 2)", val);
    }

    @Test // polygon vs. point intersection is a point
    public void testSelectIntersection5() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":11 a :Geom; geo:asWKT ?xWkt.\n" +
                ":13 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POINT (2 2)", val);
    }

    @Test // line vs. line intersection is a point
    public void testSelectIntersection6() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":12 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POINT (2 2)", val);
    }

    @Test // line vs. point intersection is a point
    public void testSelectIntersection7() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POINT (2 2)", val);
    }

    @Test // case of disjoint polygons
    public void testSelectIntersection8() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":5 a :Geom; geo:asWKT ?xWkt.\n" +
                ":7 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:intersection(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POLYGON EMPTY", val);
    }

    @Test // polygon vs. polygon difference - remainder of first polygon
    // case when first polygon is within the other, so nothing remains
    public void testSelectDifference() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:difference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POLYGON EMPTY", val);
    }

    @Test // polygon vs. polygon difference - remainder of first polygon
    // case when second polygon is within the other, diff exists
    public void testSelectDifference2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":1 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:difference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // Output is polygon with hole
        assertEquals("POLYGON ((1 1, 1 7, 8 7, 8 1, 1 1), (2 2, 7 2, 7 5, 2 5, 2 2))", val);
    }

    @Test // line vs. line difference - remainder of first line
    public void testSelectDifference3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":9 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:difference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        //assertEquals("LINESTRING(10 2, 3 2)", val);
        assertEquals("LINESTRING (3 2, 10 2)", val);
    }

    @Test // polygon vs. line difference - no effect
    public void testSelectDifference4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":10 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:difference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // In theory no effect, in practice the intersecting line adds 2 vertices
        assertEquals("POLYGON ((8 2, 8 1, 1 1, 1 2, 1 7, 8 7, 8 2))", val);
    }

    @Test // polygon vs. point difference - no effect
    public void testSelectDifference5() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":11 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:difference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // No effect
        assertEquals("POLYGON ((2 2, 2 5, 7 5, 7 2, 2 2))", val);
    }

    @Test // polygon vs. polygon difference - remainder of first polygon
    // case when second polygon is within the other, diff exists - ALSO case with teamplate
    public void testSelectDifference6() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:difference(?xWkt, 'POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))'^^geo:wktLiteral) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // No effect
        assertEquals("POLYGON ((1 1, 1 7, 8 7, 8 1, 1 1), (2 2, 7 2, 7 5, 2 5, 2 2))", val);
    }

    @Test // polygon vs. polygon difference - remainder of first polygon
    // case when second polygon is within the other, diff exists - ALSO case with full teamplate
    public void testSelectDifference7() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:difference(?xWkt, '<http://www.opengis.net/def/crs/OGC/1.3/CRS84> POLYGON((2 2, 7 2, 7 5, 2 5, 2 2))'^^geo:wktLiteral) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // No effect
        assertEquals("POLYGON ((1 1, 1 7, 8 7, 8 1, 1 1), (2 2, 7 2, 7 5, 2 5, 2 2))", val);
    }

    @Test // Polygon vs Polygon
    public void testSelectSymDifference() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":5 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:symDifference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // Multipolygon output
        assertEquals("MULTIPOLYGON (((7 3, 7 2, 2 2, 2 5, 3 5, 3 3, 7 3)), ((7 3, 7 5, 3 5, 3 6, 8 6, 8 3, 7 3)))", val);
    }

    @Test // Line vs Line
    public void testSelectSymDifference2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":15 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:symDifference(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // Multiline output
        assertEquals("MULTILINESTRING ((1 2, 2 2), (10 2, 11 2))", val);
    }

    @Test // polygon + polygon
    public void testSelectUnion() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":13 a :Geom; geo:asWKT ?xWkt.\n" +
                ":14 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:union(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // All vertices are preserved in union
        assertEquals("POLYGON ((3 3, 3 0, 0 0, 0 3, 0 6, 3 6, 3 3))", val);
    }

    @Test // line + line
    public void testSelectUnion2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":10 a :Geom; geo:asWKT ?xWkt.\n" +
                ":15 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:union(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // All vertices are preserved in union
        assertEquals("MULTILINESTRING ((1 2, 2 2), (2 2, 10 2), (10 2, 11 2))", val);
    }

    @Test // envelope of triangle is rectangle
    public void testSelectEnvelope() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":16 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:envelope(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POLYGON ((2 2, 2 4, 6 4, 6 2, 2 2))", val);
    }

    @Test // envelope of vertical line is still the vertical line
    public void testSelectEnvelope2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":12 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:envelope(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // lines parallel to x or y axis have no envelopes
        assertEquals("LINESTRING (2 1, 2 10)", val);
    }

    @Test // envelope of point is the very same point
    public void testSelectEnvelope3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":11 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:envelope(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // lines parallel to x or y axis have no envelopes
        assertEquals("POINT (2 2)", val);
    }

    @Test // envelope of geometry collection
    public void testSelectEnvelope4() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":19 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:envelope(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // only provides the envelope of the corresponding polygon if polygon+point
        // works well for polygon + polygon
        assertEquals("POLYGON ((0 2, 0 6, 6 6, 6 2, 0 2))", val);
    }

    @Test // polygon boundary
    public void testSelectBoundary() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:boundary(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("LINEARRING (2 2, 7 2, 7 5, 2 5, 2 2)", val);
    }

    @Test
    public void testSelectConvexHull() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":17 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:convexHull(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("POLYGON ((2 2, 7 7, 6 2, 2 2))", val);
    }

    @Test // Boolean Relate - Check for equality
    public void testAskRelate() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":6 a :Geom; geo:asWKT ?yWkt.\n" +
                //"FILTER (geof:relate(?xWkt, ?yWkt, " + "\"TFFFTFFFT\"" + "))\n" +
                "FILTER (geof:relate(?xWkt, ?yWkt, 'TFFFTFFFT'))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Boolean relate - Check for within condition
    public void testAskRelate2() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                //"FILTER (geof:relate(?xWkt, ?yWkt, " + "\"T*F**F***\"" + "))\n" +
                "FILTER (geof:relate(?xWkt, ?yWkt, 'T*F**F***'))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // String relate - Retrieve matrix
    public void testSelectRelate3() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "\n" +
                "SELECT ?x WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:relate(?xWkt, ?yWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertFalse(val.startsWith("TTT"));
    }

    @Test // Sub-polygon contained properly non-tangential
    public void testAskRcc8Ntpp() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":1 a :Geom; geo:asWKT ?xWkt.\n" +
                ":2 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:rcc8ntpp(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Sub-polygon contained properly, tangential
    public void testAskRcc8Tpp() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":7 a :Geom; geo:asWKT ?xWkt.\n" +
                ":6 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:rcc8tpp(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Sub-polygon contains properly, non-tangential
    public void testAskRcc8Ntppi() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":2 a :Geom; geo:asWKT ?xWkt.\n" +
                ":1 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:rcc8ntppi(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Sub-polygon contains properly, tangential
    public void testAskRcc8Tppi() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "ASK WHERE {\n" +
                ":6 a :Geom; geo:asWKT ?xWkt.\n" +
                ":7 a :Geom; geo:asWKT ?yWkt.\n" +
                "FILTER (geof:rcc8tppi(?xWkt, ?yWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // ST_SRID retrieves an integer
    public void testSelectGetSRID() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?x WHERE {\n" +
                ":20 a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:getSRID(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        assertEquals("http://www.opengis.net/def/crs/OGC/1.3/CRS84", val);
    }

    @Test // ST_SRID retrieves an integer
    public void testSelectGetSRIDCRS84() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/crs84/3> a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:getSRID(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // val has type "xsd:anyURI"
        assertEquals("http://www.opengis.net/def/crs/OGC/1.3/CRS84", val);
    }


    @Test // ST_SRID retrieves an integer
    public void testSelectGetSRIDEPSG() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/epsg3044/21> a :Geom; geo:asWKT ?xWkt.\n" +
                "BIND(geof:getSRID(?xWkt) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // val has type "xsd:anyURI"
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3044", val);
    }

    @Test // ST_SRID retrieves an integer
    public void testSelectGetSRIDEPSG_Constant() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?x WHERE {\n" +
                "BIND(geof:getSRID('<http://www.opengis.net/def/crs/EPSG/0/3044> POINT(668683.853 5122640.964)'^^geo:wktLiteral) as ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnString(query);
        // val has type "xsd:anyURI"
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3044", val);
    }

    @Test // geo:hasGeometry format test
    public void testAskIntersectHasGeometry() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?yWkt, 10, uom:degree) AS ?bWkt) .\n" +
                "FILTER (geof:sfIntersects(?xWkt, ?bWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertTrue(val);
    }

    @Test // Select Distance with Limit 1, {Lon} {Lat} template example
    public void testSelectDistance_Lon_Lat_WithLimit() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?x WHERE {\n" +
                "?lake a :Lake ;\n" +
                "geo:asWKT ?w1 .\n" +
                "?river a :River ;\n" +
                "geo:asWKT ?w2 .\n" +
                "BIND(geof:distance(?w1,?w2,uom:metre) as ?x) .\n" +
                "} LIMIT 1\n";
        Double val = runQueryAndReturnDoubleX(query);
        assertEquals(111657.03929352836, val);
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

    private Integer runQueryAndReturnIntegerX(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLLiteral ind1 = bindingSet.getOWLLiteral("x");
            return ind1.parseInteger();
        }
    }
}
