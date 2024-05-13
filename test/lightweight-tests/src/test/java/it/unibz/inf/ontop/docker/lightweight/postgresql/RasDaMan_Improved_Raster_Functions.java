package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaMan_Improved_Raster_Functions extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/rasdaman.properties";
    private static final String OBDA_FILE = "/rasdaman/OntoRasterDemo.obda";
    private static final String OWL_FILE = "/rasdaman/rasdaman.owl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
    /////////////////////////////////////
    ////// Simple Raster Function ///////
    /////////////////////////////////////

    @Test
    public void Q1_getRasterDimension() {

        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND (rasdb:rasDimension(?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"[0:303,0:395,0:583]\"^^xsd:string"));
    }

    @Test
    public void Q4_processRasterCell() {

        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND (- AS ?operator\n)"
                + "BIND (10000 AS ?value\n)"
                + "BIND ('2023-07-24T00:00:00+00:00' AS ?timestamp\n)"
                + "BIND (rasdb:rasProcessRasterOp(?timestamp, ?operator, ?value ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"[0:303,0:395,0:583]\"^^xsd:string"));
    }



    ///////////////////////////////////////
    ////// Aggregate Raster Function //////
    ///////////////////////////////////////

    @Test
    public void avgRasterSPATIALX() {

        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?r geo:asWKT ?region .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasMinLongitude ?min_lon .\n"
                + "?x rasdb:hasMaxLatitude ?max_lat .\n"
                + "?x rasdb:hasSpatialResolution_lon ?x_res .\n"
                + "?x rasdb:hasSpatialResolution_lat ?y_res .\n"
                + "FILTER (CONTAINS(?region_name, 'Linköping')\n)" //Vector region = Würzburg Erding, Kelheim, Linköping (13.029), Göteborg Ultimo
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
                + "BIND (145 AS ?time\n)"
                + "BIND (rasdb:rasSpatialAverageX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"13.029\"^^xsd:double"));
    }

    @Test
    public void avgRasterSPATIALX_Polygon_FINAL() {
        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'München'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('2023-09-24T00:00:00+00:00' AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverageFINAL(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"6.762\"^^xsd:string"));
    }


    @Test
    public void avgRasterSPATIALX_MultiPolygon_FINAL() {
        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Göteborg'\n)" //Vector region = Umeå (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  Bayreuth (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00' AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverageFINAL(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "} LIMIT 1\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"15.891\"^^xsd:string"));
    }


//    @Test
//    public void DateTime2Grid() {
//        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT ?v {\n"
////                + "?x rasdb:hasRasterId ?raster_id .\n"
//                + "?x rasdb:hasRasterName ?raster_name .\n"
//                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
//                + "BIND ('2022-08-23T00:00:00+00:00' AS ?time\n)"
//                + "BIND (rasdb:rasDate2Grid(?time, ?raster_id) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(queryX, ImmutableList.of("\"144\"^^xsd:integer"));
//    }


    @Test
    public void minRasterSPATIALX() {

        String query10 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?r geo:asWKT ?region .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasMinLongitude ?min_lon .\n"
                + "?x rasdb:hasMaxLatitude ?max_lat .\n"
                + "?x rasdb:hasSpatialResolution_lon ?x_res .\n"
                + "?x rasdb:hasSpatialResolution_lat ?y_res .\n"
                + "FILTER (?region_name = 'München'\n)" //Vector region = Würzburg Erding, Kelheim, Linköping, Ultimo, Cham
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMinimumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query10, ImmutableList.of("\"13554\"^^xsd:integer"));
    }

    @Test
    public void maxRasterSPATIALX() {

        String query11 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?r geo:asWKT ?region .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasMinLongitude ?min_lon .\n"
                + "?x rasdb:hasMaxLatitude ?max_lat .\n"
                + "?x rasdb:hasSpatialResolution_lon ?x_res .\n"
                + "?x rasdb:hasSpatialResolution_lat ?y_res .\n"
                + "FILTER (CONTAINS(?region_name, 'Linköping')\n)" //Vector region = Regen, Erding, Kelheim, Linköping (14534), Ultimo (13791), Hofors
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query11, ImmutableList.of("\"14534\"^^xsd:integer"));
    }

    ///////////////////////////////////////
    ////// Clip Raster Function //////
    ///////////////////////////////////////

    @Test
    public void clipRaster() {
        String query12 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Stockholm'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00' AS ?timestamp\n)"
                + "BIND (rasdb:rasClipRaster(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query12, ImmutableList.of("\"{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14284,14284,14333,14333,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14257,14302,14302,14332,14332,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14250,14266,14266,14345,14345,14345,14345,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,14278,14262,14262,14318,14318,14327,14327,14331,14331,14331,14331,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,14342,14334,14334,14290,14290,14301,14301,14308,14308,14311,14311,14311,14311,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{14468,14420,14420,14388,14388,14324,14324,14330,0,0,14343,14343,14346,14346,14341,14341,14342,14342,14327,14327,14323,14323,14305,14305,14311,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{14490,14490,14506,14506,14362,14362,14358,14358,14351,14351,14349,14349,14349,14349,14351,14351,14358,14358,14362,14362,14368,14368,14379,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,14429,14515,14515,14448,14403,14403,14364,14364,14345,14345,14354,14354,14355,14355,14360,14360,14353,14353,14380,14380,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14435,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,14524,14511,14511,14458,14458,14433,14433,14327,14327,14320,14320,14307,14307,14309,14309,14312,14312,14356,14356,14390,14390,0,0,0,0,0,0,0,0,0,0,14361,14326,14326,14317,14317,14396,14396,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,14537,14520,14520,14503,14503,14361,14361,14357,14357,14332,14332,14336,14336,14331,14331,14340,14340,14374,14374,14385,14385,0,0,0,0,0,0,0,0,0,14379,14374,14374,14366,14366,14381,14381,14381,14381,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,14530,14485,14485,14413,14413,14412,14412,14412,14412,14381,14381,14368,14368,14348,14348,14333,14333,14370,14370,14384,14384,14434,14443,0,0,0,0,0,0,14421,14416,14416,14390,14390,14381,14381,14352,14352,14347,14347,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,14316,14328,14328,14432,14432,14420,14420,14345,14345,14358,14358,14395,14395,14406,14406,14424,14424,14425,14425,14434,14434,14431,14431,14412,14412,14399,14399,14374,14374,14318,14318,14309,14309,14351,14351,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,14372,14372,14421,14421,14384,14384,14367,14389,14389,14389,14389,14415,14415,14427,14427,14445,14445,14453,14453,14458,14458,0,0,0,0,0,0,14315,14315,14332,14332,14373,14373,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14410,14416,14416,14433,14433,14429,14429,14409,14409,14436,14436,14458,14458,14467,14467,14468,14468,14443,14443,0,0,0,0,0,0,0,0,14418,14387,14387,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14485,14489,14489,14496,14496,14503,14503,14475,14475,14473,14473,14444,14444,14438,14438,14435,14435,14443,14443,14407,14407,14398,14398,14361,14361,14354,14354,14336,14336,14323,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14464,14441,14441,14406,14406,14404,14404,14412,14412,14425,14425,14436,14436,14469,14469,14481,14481,14421,14421,14401,14360,14360,14348,14348,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14401,14401,14393,14393,14349,14349,14359,14359,14389,14389,14385,14385,14379,14379,14377,14377,14386,14386,14383,14383,14383,14383,14364,14364,14332,14332,14339,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,14445,14445,14387,14387,14370,14370,14363,14363,14364,14364,14365,14365,14367,14367,14361,14361,14351,14351,14347,14347,14353,14346,14346,14342,14342,14334,14334,14271,14271,14274,14274,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,14470,14470,14454,14454,14357,14357,14352,14352,14340,14340,14331,14331,14315,14315,14312,14312,14318,14318,14323,14323,14330,14330,14328,14328,14328,14328,14328,14328,14326,14326,14300,14300,14287,14287,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,14451,14451,14403,14403,14358,14358,14338,14338,14322,14322,14315,14315,14306,14306,14306,14306,14317,14317,14318,14318,14326,14327,14327,14337,14337,14335,14335,14320,14320,14314,14314,14289,14289,14285,14285,14291,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,14388,14388,14367,14367,14321,0,0,0,0,0,0,14298,14305,14305,14313,14313,14321,14321,14322,14322,14320,14320,14328,14328,14330,14330,14319,14319,14318,14318,14288,14288,14277,14277,14313,14313,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14295,14303,14320,14320,14313,14313,14303,14303,14303,14303,14325,14325,14331,14331,14349,14349,14348,14348,14327,14327,14330,14330,14329,14329,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14297,14297,14295,14295,14295,14295,14326,14326,14342,14342,14379,14379,14390,14390,14389,14389,14389,14389,14355,14336,14336,14265,14265,14258,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14308,14308,14320,14320,14351,14351,14355,14355,14372,14372,14381,14381,14384,14384,14383,14383,14398,14398,14346,14346,14273,14273},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14325,14325,14340,14340,14356,14356,14359,14359,14350,14343,14343,14373,14373,14370,14370,14267,14267,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14242,14252,14252,14292,14292,0,0,0,0,0,14318,14349,14349,14308,14308,0}}\"^^xsd:string")); //xsd:array is not available

    }

    @Test
    public void clipRasterAnyGeom() {
        String query13 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('POLYGON((11.324157714843748 48.29050321714061,11.911926269531248 48.279537342260085,11.88995361328125 48.01932418480118,11.340637207031248 48.01564978668938,11.324157714843748 48.29050321714061))' AS ?custom_region_wkt\n)"
                + "BIND ('2023-07-24T00:00:00+00:00' AS ?timestamp\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "BIND (rasdb:rasClipRasterAnyGeom(?timestamp, ?custom_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query13, ImmutableList.of("\"{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14306,14297,14297,14297,14306,14306,14283,14167,14167,14156,14128,14128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14312,14286,14286,14277,14276,14276,0,14170,14170,14096,14063,14063,14057,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14374,14374,14374,0,0,0,14276,14276,14276,14271,14207,14207,14207,14098,14098,14057,14057,14057,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14384,14381,14381,14376,14374,14374,14374,0,0,14276,0,0,0,14206,14206,14207,14207,14207,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14384,14384,14384,14354,14374,14374,0,0,0,0,14200,14200,14200,14200,14200,0,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,0,0,0,0,0,0,14311,14311,14255,14238,14238,14245,14294,14294,14294,0,0,14200,14196,14196,14184,14041,14041,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,14448,14448,14448,0,0,0,0,0,0,14238,14238,14238,14282,14294,14294,14294,14294,14294,0,14164,14164,14164,14136,14136,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,14448,0,0,0,0,0,0,0,0,0,0,0,14294,14294,14294,0,0,0,14164,14164,14164,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14315,14315,14313,14308,14308,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14363,14321,14321,14312,14309,14309,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14386,14347,14311,14311,14309,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14386,14386,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,14443,14443,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,14443,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}}\"^^xsd:string")); //xsd:array is not available

    }
}
