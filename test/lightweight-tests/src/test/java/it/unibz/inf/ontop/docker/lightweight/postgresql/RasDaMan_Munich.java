package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaMan_Munich extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/OntoRaster.properties";
    private static final String OBDA_FILE = "/rasdaman/OntoRasterDemo.obda";
    private static final String OWL_FILE = "/rasdaman/OntoRasterX.owl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }
    /////////////////////////////////////////
    ////// Checking OntoRaster BEFORE ///////
    /////////////////////////////////////////

    @Test
    public void avgRasterSPATIAL() {
        String query0 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?regionName .\n"
                + "?vector geo:asWKT ?regionWkt .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (?regionName = 'München'\n)" //Vector region = Linköping (2022-08-24T00:00:00+00:00), Ultimo (2023-09-24T00:00:00+00:00),  München (2023-07-24T00:00:00+00:00)
                + "FILTER (CONTAINS(?rasterName, 'Bavaria_Temperature')\n)"
                + "BIND ('2023-09-04T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)" // Missing DateTime Issue
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?regionWkt, ?rasterName) AS ?v)"
                + "}\n";

        executeAndCompareValues(query0, ImmutableList.of("\"14331.952\"^^xsd:string"));
    }

    /////////////////////////////////////////
    ////// OntoRaster + OSM + CityGML ///////
    /////////////////////////////////////////

    @Test
    public void getRasterDimension() {

        String query1 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?raster rasdb:rasterName ?raster_name .\n"
                + "FILTER (CONTAINS(?raster_name, 'Elevation')\n)"
                + "BIND (rasdb:rasDimension(?raster_name) AS ?v)"
                + "}\n";
        executeAndCompareValues(query1, ImmutableList.of("\"[0:0,0:671,0:1304]\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Munich() {
        String query2 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?regionName .\n"
                + "?vector geo:asWKT ?regionWkt .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (?regionName = 'Laim'\n)"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?regionWkt, ?rasterName) AS ?v)"
                + "}\n";

        executeAndCompareValues(query2, ImmutableList.of("\"531.421\"^^xsd:string")); //Actual   :["506"^^xsd:string, "532.064"^^xsd:string, "531.421"^^xsd:string] If target not set
    }

    @Test
    public void avgRasterSPATIAL_Munich2() {
        String query3 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?regionName .\n"
                + "?vector geo:asWKT ?regionWkt .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (?regionName = 'Laim'\n)"
                + "FILTER (CONTAINS(?rasterName, 'Munich_Temperature')\n)"
                + "BIND ('2022-01-01T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?regionWkt, ?rasterName) AS ?v)"
                + "}\n";

        executeAndCompareValues(query3, ImmutableList.of("\"13801.222\"^^xsd:string"));
    }

    @Test
    public void avgRasterSPATIAL_Munich3() {
        String query4 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?vector rdfs:label ?regionName .\n"
                + "?vector geo:asWKT ?regionWkt .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (?regionName = 'Laim'\n)"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?regionWkt, ?rasterName) AS ?v)"
                + "}\n";

        executeAndCompareValues(query4, ImmutableList.of("\"531.421\"^^xsd:string"));
    }
}
