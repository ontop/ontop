package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertEquals;

/**
 * Tests to check if PostGIS supports GeoSPARQL properly.
 */
@Ignore
public class GeoSPARQLPostGISTest extends AbstractVirtualModeTest {

    static final String owlfile = "/pgsql/geosparql/geosparql.owl";
    static final String obdafile = "/pgsql/geosparql/geosparql-postgres.obda";
    static final String propertiesfile = "/pgsql/geosparql/geosparql-postgres.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlfile, obdafile, propertiesfile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    /**
     * Test to check if PostGIS supports ST_INTERSECTS properly
     * There are 3 options for ST_INTERSECTS in PostGIS and it needs to distinguish between them
     * (TEXT, TEXT),
     * (GEOMETRY, GEOMETRY),
     * (GEOGRAPHY, GEOGRAPHY)
     */
    @Test
    public void testAskIntersect() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?yWkt, 350000, uom:metre) AS ?bWkt) .\n" +
                "FILTER (geof:sfIntersects(?xWkt, ?bWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertEquals(val, true);
    }

    /**
     * ST_BUFFER when not a subexpression should return geometries in wkt format, NOT binary
     */
    @Test
    public void testAskIntersectBuffer() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "BIND(geof:buffer(?xWkt, 350000, uom:metre) AS ?x) .\n" +
                "}\n";
        String val = runQueryAndReturnStringOfLiteralX(query);
        assertEquals(val, "\"POLYGON((7.061002551206959 48.78897147461564,6.922049938911179 48.179326183965884," +
                "6.613378124919122 47.59859472026753,6.151277122927114 47.067682652819585," +
                "5.555932897661403 46.6052102216297,4.850517732837641 46.22706288552067," +
                "4.060429948034984 45.94602396511869,3.212649741026308 45.771481921775255," +
                "2.335173894157313 45.70921402310402,1.456497354773591 45.76125299684746," +
                "0.60511744812205 45.92584302395559,-0.190958340113786 46.197487218014665," +
                "-0.904720148354073 46.56708307652216,-1.510703614008204 47.02213825242614," +
                "-1.985610513116994 47.54705887684699,-2.309040873313564 48.123507728617085," +
                "-2.464372523241459 48.73083909628524,-2.439805072975696 49.34662851696791," +
                "-2.2295437839114 49.94732425203034,-1.835032756335628 50.50904784309939," +
                "-1.26606362995152 51.0085580749328,-0.541506735709377 51.42436318808722," +
                "0.310628212145415 51.737922363745724,1.254055064448258 51.934828816401186," +
                "2.246108213423229 52.00582967202254,3.240556359103219 51.947530460136164," +
                "4.19097249809059 51.762665649982935,5.054136742595582 51.459886918433476," +
                "5.792938739156385 51.05310695382713,6.378394102707328 50.560510099736874," +
                "6.790628071657954 50.00337972063693,7.018913233125993 49.404889482675," +
                "7.061002551206959 48.78897147461564))\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>");
    }

    /**
     * ST_WITHIN in PostGIS only supports geometries, any buffer is cast back to a geometry
     */
    @Test
    public void testAskWithinBuffer() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?yWkt, 350000, uom:metre) AS ?bWkt) .\n" +
                "FILTER (geof:sfWithin(?xWkt, ?bWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertEquals(val, true);
    }

    /**
     * ST_CONTAINS in PostGIS only supports geometries, any buffer is cast back to a geometry
     */
    @Test
    public void testAskContainsBuffer() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?yWkt, 350000, uom:metre) AS ?bWkt) .\n" +
                "FILTER (geof:sfContains(?bWkt, ?xWkt))\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertEquals(val, true);
    }

    /**
     * ST_UNION in PostGIS supports geometries
     */
    @Test
    public void testAskUnion() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?xWkt, 10, uom:metre) AS ?aWkt) .\n" +
                "BIND(geof:buffer(?yWkt, 10, uom:metre) AS ?bWkt) .\n" +
                "BIND(geof:union(?xWkt, ?yWkt) AS ?x)\n" +
                "}\n";
        String val = runQueryAndReturnStringOfLiteralX(query);
        assertEquals(val, "\"MULTIPOINT(2.2945 48.8584,-0.0754 51.5055)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>");
    }

    /**
     * ST_UNION in PostGIS works when inputs are geometry and ST_BUFFER(geometry)
     */
    @Test
    public void testAskUnionBuffer() throws Exception {
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "SELECT ?x WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "<http://ex.org/feature/2> a :Feature; geo:hasGeometry/geo:asWKT ?yWkt.\n" +
                "BIND(geof:buffer(?yWkt, 10, uom:metre) AS ?bWkt) .\n" +
                "BIND(geof:union(?xWkt, ?bWkt) AS ?x)\n" +
                "}\n";
        String val = runQueryAndReturnStringOfLiteralX(query);
        assertEquals(val, "\"GEOMETRYCOLLECTION(POINT(2.2945 48.8584),POLYGON((-0.075256104751844 51.50549640923696," +
                "-0.075259992200971 51.5054789591328,-0.075269260063586 51.50546231761998,-0.075283552176091 51.505447124221234," +
                "-0.075302319297681 51.50543396280859,-0.07532484021783 51.50542333916582,-0.075350249472073 51.50541566155171," +
                "-0.075377570601051 51.50541122501118,-0.075405753674676 51.505410200037126,-0.075433715639506 51.505412626018604," +
                "-0.075460381938927 51.505418409727156,-0.075484727806845 51.50542732889937,-0.075505817648146 51.50543904077822," +
                "-0.075522840992622 51.50545309528471,-0.075535143640826 51.5054689523139,-0.075542252804855 51.505486002490514," +
                "-0.075543895277947 51.5055035905867,-0.075540007934509 51.50552104070189,-0.075530740156956 51.50553768223735," +
                "-0.075516448095942 51.505552875666886,-0.075497680984426 51.50556603711378,-0.075475160031401 51.505576660789075," +
                "-0.075449750706341 51.505584338429,-0.075422429479382 51.50558877498473,-0.075394246295532 51.50558979996102," +
                "-0.075366284225013 51.50558737396851,-0.075339617840528 51.50558159023733,-0.075315271921121 51.50557267103432," +
                "-0.075294182069747 51.505560959121205,-0.075277158758145 51.5055469045822,-0.075264856180759 51.5055310475272," +
                "-0.075257747114709 51.505513997335385,-0.075256104751844 51.50549640923696)))\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>");
    }

    /**
     * ST_DISTANCESPHERE in PostGIS supported
     */
    @Test
    public void testAskDistance() throws Exception {
        //language=TEXT
        String query = "PREFIX : <http://ex.org/> \n" +
                "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" +
                "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/>\n" +
                "ASK WHERE {\n" +
                "<http://ex.org/feature/1> a :Feature; geo:hasGeometry/geo:asWKT ?xWkt.\n" +
                "BIND((geof:distance(?xWkt, 'POINT(-0.0754 51.5055)'^^geo:wktLiteral, uom:metre)/1000) as ?x) .\n" +
                "FILTER(?x < 350000) .\n" +
                "}\n";
        boolean val = runQueryAndReturnBooleanX(query);
        assertEquals(val, true);
    }



}
