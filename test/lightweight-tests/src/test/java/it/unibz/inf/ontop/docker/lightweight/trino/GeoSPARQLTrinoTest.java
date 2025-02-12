package it.unibz.inf.ontop.docker.lightweight.trino;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.docker.lightweight.TrinoLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@TrinoLightweightTest
public class GeoSPARQLTrinoTest extends AbstractDockerRDF4JTest {

    protected static final String OWL_FILE = "/geospatial/geospatial.owl";
    protected static final String OBDA_FILE = "/geospatial/trino/geospatial-trino.obda";
    private static final String PROPERTIES_FILE = "/geospatial/trino/geospatial-trino.properties";
    private static final String LENSES_FILE = "/geospatial/trino/geospatial-trino.json";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENSES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Test
    public void testPointDistanceMeters() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?v WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?v) .\n" +
                "}\n";
        executeAndCompareValues(query, ImmutableList.of("\"339241.362811672\"^^xsd:double"));
    }

    @Test
    public void testPointDistanceMeters_epsg3044() { // cartesian distance
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?v WHERE {\n" +
                "<http://ex.org/epsg3044/3> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg3044/4> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?v) .\n" +
                "}\n";
        executeAndCompareValues(query, ImmutableList.of("\"3.5529655810322693\"^^xsd:double"));
    }

    @Test
    public void testPointDistanceMeters_epsg4326() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?v WHERE {\n" +
                "<http://ex.org/epsg4326/3> a :Geom; geo:asWKT ?xWkt.\n" +
                "<http://ex.org/epsg4326/4> a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:metre) as ?v) .\n" +
                "}\n";
        executeAndCompareValues(query, ImmutableList.of("\"339241.362811672\"^^xsd:double"));
    }

    @Test
    public void testPointDistanceDegrees() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?v WHERE {\n" +
                ":3 a :Geom; geo:asWKT ?xWkt.\n" +
                ":4 a :Geom; geo:asWKT ?yWkt.\n" +
                "BIND(geof:distance(?xWkt, ?yWkt, uom:degree) as ?v) .\n" +
                "}\n";
        executeAndCompareValues(query, ImmutableList.of("\"3.050877576151497\"^^xsd:double"));
    }

}
