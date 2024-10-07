package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaMan_Improved_Raster_Functions extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/OntoRaster.properties";
    private static final String OBDA_FILE = "/rasdaman/OntoRasterDemo.obda";
    private static final String OWL_FILE = "/rasdaman/OntoRaster.owl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
    /////////////////////////////////////
    ////// Simple Raster Functions ///////
    /////////////////////////////////////

    @Test
    public void Q1_getRasterDimension() {

        String query1 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND (rasdb:rasDimension(?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query1, ImmutableList.of("\"[0:303,0:395,0:583]\"^^xsd:string"));
    }

//@Disabled
//    @Test
//    public void Q2_processRasterCell() {
//
//        String query2 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
//                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
//                + "SELECT ?v {\n"
//                + "?raster rasdb:rasterName ?raster_name .\n"
//                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
//                + "BIND ('*' AS ?operator\n)"
//                + "BIND (0.02 AS ?operand\n)"
//                + "BIND ('2022-04-18T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
//                + "BIND (rasdb:rasCellOp(?timestamp, ?operator, ?operand, ?raster_name) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query2, ImmutableList.of("\"{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,283.3,283.64,283.64,283.88,283.34000000000003,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,<...>0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,281.38,281.38,281.90000000000003,282.1,0,0,0,281,280.02,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}}\"^^xsd:string"));
//    }

    @Test
    public void DateTime2Grid() {
        String query3 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?x rasdb:rasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('2023-06-15T00:00:00+00:00'^^xsd:dateTime AS ?time\n)"
                + "BIND (rasdb:rasDate2Grid(?time, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query3, ImmutableList.of("\"165\"^^xsd:string"));
    }


    ///////////////////////////////////////
    ////// Aggregate Raster Functions //////
    ///////////////////////////////////////

//    @Test
//    public void avgRasterSPATIALX() {
//
//        String query4 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
//                + "SELECT ?v {\n"
//                + "?r rdfs:label ?region_name .\n"
//                + "?r geo:asWKT ?region .\n"
//                + "?x rasdb:rasterName ?raster_name .\n"
//                + "?x rasdb:hasMinLongitude ?min_lon .\n"
//                + "?x rasdb:hasMaxLatitude ?max_lat .\n"
//                + "?x rasdb:hasSpatialResolution_lon ?x_res .\n"
//                + "?x rasdb:hasSpatialResolution_lat ?y_res .\n"
//                + "FILTER (CONTAINS(?region_name, 'Castelrotto')\n)" //Vector region = Würzburg Erding, Kelheim, Linköping (13.029), Göteborg Ultimo
//                + "FILTER (CONTAINS(?raster_name, 'Tyrol')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
//                + "BIND (145 AS ?time\n)"
//                + "BIND (rasdb:rasSpatialAverageX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query4, ImmutableList.of("\"5.282\"^^xsd:double"));
//    }


    @Test
    public void avgRasterSPATIAL_Polygon_P1_FINAL() {
        String query5 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'München'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('2023-10-01T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query5, ImmutableList.of("\"14207.288\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Polygon_P1_FINAL_02() {
        String query6 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Linköping'\n)" //Vector region = Söderköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query6, ImmutableList.of("\"14308.944\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Polygon_P2_FINAL() {
        String query5 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Würzburg'\n)" //Vector region = Würzburg, Bayreuth (2023-07-24T00:00:00+00:00, 2023-10-01T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('2023-10-01T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query5, ImmutableList.of("\"14177.483\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_MultiPolygon_MP1_FINAL() {
        String query7 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Göteborg'\n)" //Vector region = Umeå (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  Bayreuth (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query7, ImmutableList.of("\"14452.05\"^^xsd:string"));
    }

    @Test
    public void minRasterSPATIAL_MultiPolygon_MP1_FINAL() {
        String query7 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?region a :Region .\n"
                + "?region rdfs:label ?region_name .\n"
                + "?region geo:asWKT ?region_wkt .\n"
                + "?coverage a :Raster .\n"
                + "?coverage rasdb:rasterName ?coverage_id .\n"
                + "FILTER (?region_name = 'Göteborg'\n)" //Vector region = Umeå (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  Bayreuth (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?coverage_id, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialMinimum(?timestamp, ?region_wkt, ?coverage_id) AS ?v)"
                + "}\n";

        executeAndCompareValues(query7, ImmutableList.of("\"14304\"^^xsd:string"));
    }

    @Test
    public void maxRasterSPATIAL_MultiPolygon_MP1_FINAL() {
        String query7 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Söderköping'\n)" //Vector region = Umeå, Söderköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  Bayreuth (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialMaximum(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query7, ImmutableList.of("\"14503\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_MultiPolygon_MP1_FINAL_2() {
        String query10 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Cham'\n)" //Vector region = Cham (2023-07-24T00:00:00+00:00, 2023-10-01T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('2023-10-01T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query10, ImmutableList.of("\"14206.48\"^^xsd:string"));
    }

    @Test
    public void minRasterTEMPORAL() {
        String query8 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?region a :Region .\n"
                + "?region rdfs:label ?region_name .\n"
                + "?region geo:asWKT ?region_wkt .\n"
                + "?coverage a :Raster .\n"
                + "?coverage rasdb:rasterName ?coverage_id .\n"
                + "FILTER (?region_name = 'Sarentino'\n)" //Vector region = Deggendorf, Würzburg, Bayreuth (2023-07-24T00:00:00+00:00, 2023-10-01T00:00:00+00:00) mytimestamp'^^xsd:dateTime
                + "FILTER (CONTAINS(?coverage_id, 'Tyrol')\n)"
                + "BIND ('2023-07-15T00:00:00+00:00'^^xsd:dateTime AS ?start_time\n)"
                + "BIND ('2023-07-21T00:00:00+00:00'^^xsd:dateTime AS ?end_time\n)"
                + "BIND (rasdb:rasTemporalMinimum(?start_time, ?end_time, ?region_wkt, ?coverage_id) AS ?v)"
                + "}\n";

        executeAndCompareValues(query8, ImmutableList.of("\"13924.253\"^^xsd:string"));
    }

    @Test
    public void maxRasterTEMPORAL() {
        String query9 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?region a :Region .\n"
                + "?region rdfs:label ?region_name .\n"
                + "?region geo:asWKT ?region_wkt .\n"
                + "?coverage a :Raster .\n"
                + "?coverage rasdb:rasterName ?coverage_id .\n"
                + "FILTER (?region_name = 'Göteborg'\n)" //Vector region = Sarentino, Silandro, Castelrotto, Ultimo, San Leonardo In Passiria
                + "FILTER (CONTAINS(?coverage_id, 'Sweden')\n)"
                + "BIND ('2022-04-05T00:00:00+00:00'^^xsd:dateTime AS ?start_time\n)"
                + "BIND ('2022-06-19T00:00:00+00:00'^^xsd:dateTime AS ?end_time\n)"
                + "BIND (rasdb:rasTemporalMaximum(?start_time, ?end_time , ?region_wkt, ?coverage_id) AS ?v)"
                + "}\n";

        executeAndCompareValues(query9, ImmutableList.of("\"14338.136\"^^xsd:string"));
    }

    @Test
    public void avgRasterTEMPORAL() {
        String query9 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?region a :Region .\n"
                + "?region rdfs:label ?region_name .\n"
                + "?region geo:asWKT ?region_wkt .\n"
                + "?coverage a :Raster .\n"
                + "?coverage rasdb:rasterName ?coverage_id .\n"
                + "FILTER (?region_name = 'Cham'\n)" //Vector region = Würzburg, Bayreuth (2023-07-24T00:00:00+00:00, 2023-10-01T00:00:00+00:00)
                + "FILTER (CONTAINS(?coverage_id, 'Bavaria')\n)"
                + "BIND ('2023-06-15T00:00:00+00:00'^^xsd:dateTime AS ?start_time\n)"
                + "BIND ('2023-07-21T00:00:00+00:00'^^xsd:dateTime AS ?end_time\n)"
                + "BIND (rasdb:rasTemporalAverage(?start_time, ?end_time , ?region_wkt, ?coverage_id) AS ?v)"
                + "}\n";

        executeAndCompareValues(query9, ImmutableList.of("\"10771.558\"^^xsd:string"));
    }

    /////////////////////////////////////
    ////// Clip Raster Functions  ///////
    /////////////////////////////////////

    @Test
    public void clipRaster() {
        String query12 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Castelrotto'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Tyrol')\n)"
                + "BIND ('2023-09-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasClipRaster(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query12, ImmutableList.of("\"{{0,0,0,0,14092,14092,14045,14047,14047,14042,14041,14041,14020,13993,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,14098,14091,14091,14091,14044,14035,14035,14023,14008,14008,14001,13979,13976,0,0,0,0,0,0,0,0,0,0,0},{0,14110,14110,14092,14090,14046,14046,14021,14007,14007,13981,13976,13976,13956,13950,13950,13939,13930,0,0,0,0,0,0,0,0},{0,14116,14087,14087,14083,14042,14042,13982,13973,13973,13943,13937,13937,13903,13899,13915,13915,13928,13927,13927,13909,13902,0,0,0,0},{14064,14135,14075,14075,14062,14039,14039,13969,13963,13920,13920,13907,13854,13854,13840,13849,13849,13882,13878,13878,13886,13886,13886,13885,0,0},{14135,14135,14102,14029,14029,14019,13963,13963,13955,13902,13902,13884,13813,13813,13797,13803,13814,13814,13806,13829,13829,13849,13844,13844,0,0},{0,14121,14104,14031,14031,14018,13969,13969,13957,13904,13869,13869,13805,13745,13745,13744,13705,13705,13694,13717,13717,13809,13807,0,0,0},{0,0,0,14032,14001,14001,13979,13955,13955,13918,13846,13846,13823,13711,13711,13708,13717,13717,13725,13729,13793,13793,13793,13806,0,0},{0,0,0,14024,13997,13997,13975,13952,13952,13912,13835,13835,13835,13697,13698,13698,13717,13735,13735,13737,13782,13782,13787,13776,0,0},{0,0,0,0,13970,13901,13901,13842,13818,13818,13790,13786,13786,13708,13703,13703,13723,13754,13754,13754,13775,13782,13782,13760,13720,0},{0,0,0,0,0,0,13861,13787,13779,13779,13764,13757,13710,13710,13704,13713,13713,13738,13736,13736,13722,13709,13709,13685,13631,13631},{0,0,0,0,0,0,0,0,0,0,0,13708,13709,13709,13697,13698,13698,13715,13713,13713,13706,13712,13714,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,13682,13682,13688,13698,0,0,0,0,0,0}}\"^^xsd:string")); //xsd:array is not available

    }

    @Test
    public void clipRasterAnyGeom() {
        String query13 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Bavaria')\n)"
                + "BIND ('POLYGON((11.324157714843748 48.29050321714061,11.911926269531248 48.279537342260085,11.88995361328125 48.01932418480118,11.340637207031248 48.01564978668938,11.324157714843748 48.29050321714061))' AS ?custom_region_wkt\n)"
                + "BIND ('2023-07-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "BIND (rasdb:rasClipRasterAnyGeom(?timestamp, ?custom_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query13, ImmutableList.of("\"{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14306,14297,14297,14297,14306,14306,14283,14167,14167,14156,14128,14128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14312,14286,14286,14277,14276,14276,0,14170,14170,14096,14063,14063,14057,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14374,14374,14374,0,0,0,14276,14276,14276,14271,14207,14207,14207,14098,14098,14057,14057,14057,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14384,14381,14381,14376,14374,14374,14374,0,0,14276,0,0,0,14206,14206,14207,14207,14207,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14384,14384,14384,14354,14374,14374,0,0,0,0,14200,14200,14200,14200,14200,0,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,0,0,0,0,0,0,14311,14311,14255,14238,14238,14245,14294,14294,14294,0,0,14200,14196,14196,14184,14041,14041,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,14448,14448,14448,0,0,0,0,0,0,14238,14238,14238,14282,14294,14294,14294,14294,14294,0,14164,14164,14164,14136,14136,13932,13932,13932,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14448,14448,14448,14448,0,0,0,0,0,0,0,0,0,0,0,14294,14294,14294,0,0,0,14164,14164,14164,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14315,14315,14313,14308,14308,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14363,14321,14321,14312,14309,14309,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14386,14347,14311,14311,14309,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14386,14386,14386,14386,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14511,14511,14511,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,14443,14443,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,14443,14443,14443,14443,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}}\"^^xsd:string")); //xsd:array is not available

    }
}
