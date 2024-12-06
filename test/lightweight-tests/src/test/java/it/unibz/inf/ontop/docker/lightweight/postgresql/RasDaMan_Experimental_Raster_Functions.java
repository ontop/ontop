package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaMan_Experimental_Raster_Functions extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/OntoRaster.properties";
    private static final String OBDA_FILE = "/rasdaman/OntoRasterExperimental.obda";
    private static final String OWL_FILE = "/rasdaman/OntoRasterX.owl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    //////////////////////////////////////////////////////
    ////// Raster Field with Raster Name Functions ///////
    //////////////////////////////////////////////////////
    @Test
    public void avgRasterSPATIAL_Polygon_without_Field() {
        String queryField = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
//                + "?raster rasdb:rasterField ?field .\n"
                + "FILTER (?vector_region_name = 'Bolzano'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00), München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Tyrol')\n)"
//                + "FILTER (CONTAINS(?field, 'Snow_Cover')\n)"
                + "BIND ('2023-09-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryField, ImmutableList.of("\"14164.257\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Polygon_with_Field() {
        String queryField = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "?raster rasdb:fieldName ?field .\n"
                + "FILTER (?vector_region_name = 'Linköping'\n)" //Vector region = Linköping, Göteborg (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00), München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?raster_name, 'Sweden')\n)"
                + "FILTER (CONTAINS(?field, 'Snow')\n)"
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverageField(?timestamp, ?vector_region_wkt, ?field, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryField, ImmutableList.of("\"14308.944\"^^xsd:string"));
    }
    ///////////////////////////////////////////////////
    ////// Region Based Raster Search Functions ///////
    //////////////////////////////////////////////////

   // 1. Uncomment the special mapping for raster name and switch off the default mapping of raster name  ***
   // 2. Replace the vector region name of respective Region of interest in the special mapping for raster name   ***
    @Test
    public void I_PolygonRegionBasedRasterSearch() {
        String query11 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'München'\n)" //Vector region  = München, Würzburg, Cham (2023-07-24T00:00:00+00:00)
                + "BIND ('2023-07-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query11, ImmutableList.of("\"14228.07\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Geom_Based_Raster_Search() {
        String query11 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Linköping'\n)" //Vector region = Linköping, Kungsbacka, Kinda  (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München, Würzburg (2023-07-24T00:00:00+00:00)
                + "BIND ('2022-08-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query11, ImmutableList.of("\"14364.694\"^^xsd:string"));
    }


    @Test
    public void IV_V__PolygonRegionBasedRasterSearch() { // Swtich on the special mapping for raster name and switch off the default mapping ofr raster name
        String query11 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?vector_region_name .\n"
                + "?vector geo:asWKT ?vector_region_wkt .\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (?vector_region_name = 'Rosenheim'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  Cham, Rosenheim (2023-07-24T00:00:00+00:00)
                + "BIND ('2023-07-24T00:00:00+00:00'^^xsd:dateTime AS ?timestamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timestamp, ?vector_region_wkt, ?raster_name) AS ?v)"
                + "} LIMIT 1\n";

        executeAndCompareValues(query11, ImmutableList.of("\"14228.07\"^^xsd:string"));
    }
}
