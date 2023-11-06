package it.unibz.inf.ontop.docker.lightweight.postgresql.other;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.*;

public class GeoSPARQLPostGISTest extends AbstractDockerRDF4JTest {

    protected static final String OWL_FILE = "/geospatial/geospatial.owl";
    protected static final String OBDA_FILE = "/geospatial/geospatial.obda";
    private static final String PROPERTIES_FILE = "/geospatial/postgis/geospatial-postgis.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    /**
     * Check if geof:sfIntersects works correctly with mixed GEOMETRY and GEOGRAPHY data types
     */
    @Disabled
    @Test
    public void testAskIntersectsGeomGeog() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "SELECT ?v WHERE {\n" +
                "?x a :Geom; geo:asWKT ?xWkt .\n" +
                "?y a :Geog; geo:asWKT ?yWkt .\n" +
                "BIND (geof:sfIntersects(?xWkt, ?yWkt) AS ?v)\n" +
                "}\n";
        Assertions.assertFalse(reformulate(query).toLowerCase().contains("cast(st_astext"));
    }

    /**
     * Check if geof:sfIntersects works correctly with mixed GEOMETRY and GEOGRAPHY data types, and buffer
     */
    @Test
    public void testSelectIntersection1() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?v WHERE {\n" +
                "?x a :Geom; geo:asWKT ?xWkt.\n" +
                "?x rdfs:label ?xname .\n" +
                "?y a :NewGeom; geo:asWKT ?yWkt.\n" +
                "?y rdfs:label ?yname .\n" +
                "BIND(geof:buffer(?xWkt, 20, uom:metre) AS ?zWkt) .\n" +
                "FILTER(?xname = ?yname) .\n" +
                "BIND(geof:intersection(?zWkt, ?yWkt) as ?v) .\n" +
                "}\n" +
                "LIMIT 1";
        executeAndCompareValues(query, ImmutableList.of());
        Assertions.assertFalse(reformulate(query).toLowerCase().contains("cast(st_astext"));
    }

    /**
     * Check if geof:sfIntersects works correctly with mixed GEOMETRY and GEOGRAPHY data types, and buffer
     */
    @Test
    public void testSelectIntersection2() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?v WHERE {\n" +
                "?x a :Geom; geo:asWKT ?xWkt.\n" +
                "?x rdfs:label ?xname .\n" +
                "?y a :Geog; geo:asWKT ?yWkt.\n" +
                "?y rdfs:label ?yname .\n" +
                "BIND(geof:buffer(?xWkt, 20, uom:metre) AS ?zWkt) .\n" +
                "FILTER(?xname = ?yname) .\n" +
                "BIND(geof:intersection(?zWkt, ?yWkt) as ?v) .\n" +
                "}\n" +
                "LIMIT 1";
        executeAndCompareValues(query, ImmutableList.of(
                "\"POLYGON((2 5,6.999999999999999 5,6.999999999999999 2,2 2,2 5))\"" +
                        "^^geo:wktLiteral"));
        Assertions.assertFalse(reformulate(query).toLowerCase().contains("cast(st_astext"));
    }

    /**
     * Check if geof:sfIntersects works correctly with mixed GEOMETRY and GEOGRAPHY data types, and buffer
     */
    @Test
    public void testSelectIntersection3() {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "\n" +
                "SELECT ?v WHERE {\n" +
                "?x a :Geog; geo:asWKT ?xWkt.\n" +
                "?x rdfs:label ?xname .\n" +
                "?y a :NewGeom; geo:asWKT ?yWkt.\n" +
                "?y rdfs:label ?yname .\n" +
                "BIND(geof:buffer(?xWkt, 20, uom:metre) AS ?zWkt) .\n" +
                "FILTER(?xname = ?yname) .\n" +
                "BIND(geof:intersection(?zWkt, ?yWkt) as ?v) .\n" +
                "}\n" +
                "LIMIT 1";
        executeAndCompareValues(query, ImmutableList.of());
        Assertions.assertFalse(reformulate(query).toLowerCase().contains("cast(st_astext"));
    }
}
