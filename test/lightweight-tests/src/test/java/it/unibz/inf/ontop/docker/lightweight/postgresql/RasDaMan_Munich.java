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
    public void avgElevation_Munich() {
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

        executeAndCompareValues(query2, ImmutableList.of("\"531.421\"^^xsd:double")); //Actual   :["506"^^xsd:string, "532.064"^^xsd:string, "531.421"^^xsd:string] If target not set
    }

    @Test
    public void avgTemperature_Munich2() {
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

        executeAndCompareValues(query3, ImmutableList.of("\"13801.222\"^^xsd:double"));
    }

    @Test
    public void avgElevation_Munich3() {
        String query4 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?dist rdfs:label ?distName .\n"
                + "?dist geo:asWKT ?distWkt .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
                + "FILTER (?v > 540.0\n)"
                + "}\n";

        executeAndCompareValues(query4, ImmutableList.of("\"541.145\"^^xsd:double", "\"541.369\"^^xsd:double", "\"542.039\"^^xsd:double", "\"540.93\"^^xsd:double", "\"559.552\"^^xsd:double", "\"547.363\"^^xsd:double"));
    }

    @Test
    public void OSM_Munich() {
        String query4 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
                + "SELECT (COUNT (?v) AS ?count) {\n"
                + "?bldg lgdo:bldgType ?bldgType .\n"
                + "FILTER (?bldgType = 'church'\n)"
                + "}\n";

        executeAndCompareValues(query4, ImmutableList.of("\"266\"^^xsd:integer"));
    }

    @Test
    public void OSM_Munich2() {
        String query5 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX geof:\t<http://www.opengis.net/def/function/geosparql/>\n"
                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?dist rdfs:label ?distName .\n"
                + "?dist geo:asWKT ?distWkt .\n"
                + "?bldg lgdo:bldgType ?bldgType .\n"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
                + "FILTER (?v < 520.0 && ?bldgType = 'synagogue'\n)"
                + "}\n";


        executeAndCompareValues(query5, ImmutableList.of("\"541.145\"^^xsd:double", "\"541.369\"^^xsd:double", "\"542.039\"^^xsd:double", "\"540.93\"^^xsd:double", "\"559.552\"^^xsd:double", "\"547.363\"^^xsd:double"));
    }

    @Test
    public void OSM_Munich3() {
        String query6 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX geof:\t<http://www.opengis.net/def/function/geosparql/>\n"
                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
//                + "?dist a :District .\n"
                + "?dist rdfs:label ?distName .\n"
                + "?dist geo:asWKT ?distWkt .\n"
                + "?bldg a lgdo:Synagogue .\n"
                + "?bldg geo:asWKT ?bldgWkt .\n"
                + "FILTER (geof:sfWithin(?bldgWkt, ?distWkt)\n)"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
//                + "FILTER (geof:sfWithin(?bldgWkt, ?distWkt) && ?bldgType = 'synagogue'\n)"
                + "FILTER (?v < 520.0\n)"
                + "}\n";

        executeAndCompareValues(query6, ImmutableList.of("\"517.755\"^^xsd:double"));
    }

    @Test
    public void OSM_Munich4() {
        String query6 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX geof:\t<http://www.opengis.net/def/function/geosparql/>\n"
                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?dist rdfs:label ?distName .\n"
                + "?dist geo:asWKT ?distWkt .\n"
                + "?bldg a lgdo:Temple .\n"
                + "?bldg geo:asWKT ?bldgWkt .\n"
                + "FILTER (geof:sfWithin(?bldgWkt, ?distWkt)\n)"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
                + "FILTER (?v < 530.0\n)"
                + "}\n";

        executeAndCompareValues(query6, ImmutableList.of("\"521.257\"^^xsd:double", "\"520.765\"^^xsd:double", "\"517.755\"^^xsd:double"));
    }

    @Test
    public void OSM_Munich5() {
        String query6 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX geof:\t<http://www.opengis.net/def/function/geosparql/>\n"
                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?dist rdfs:label ?distName .\n"
                + "?dist geo:asWKT ?distWkt .\n"
                + "?bldg a lgdo:Church .\n"
                + "?bldg geo:asWKT ?bldgWkt .\n"
                + "FILTER (geof:sfWithin(?bldgWkt, ?distWkt)\n)"
                + "?raster rasdb:rasterName ?rasterName .\n"
                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
                + "FILTER (?v > 545.0 && ?v < 550.0\n)"
                + "}\n";

        executeAndCompareValues(query6, ImmutableList.of("\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double", "\"547.363\"^^xsd:double"));
    }

//    @Test
//    public void OSM_Munich5() {
//        String query6 = "PREFIX :\t<https://github.com/aghoshpro/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX geof:\t<http://www.opengis.net/def/function/geosparql/>\n"
//                + "PREFIX lgdo:\t<http://linkedgeodata.org/ontology/>\n"
//                + "PREFIX rasdb:\t<https://github.com/aghoshpro/RasterDataCube/>\n"
//                + "SELECT ?v {\n"
//                + "?dist geo:asWKT ?distWkt .\n"
//                + "?bldg lgdo:bldgType ?bldgType .\n"
//                + "?bldg geo:asWKT ?bldgWkt .\n"
//                + "?raster rasdb:rasterName ?rasterName .\n"
//                + "FILTER (CONTAINS(?rasterName, 'Elevation')\n)"
//                + "BIND ('2000-02-11T00:00:00+00:00'^^xsd:dateTime AS ?timeStamp\n)"
//                + "BIND (rasdb:rasSpatialAverage(?timeStamp, ?distWkt, ?rasterName) AS ?v)"
//                + "FILTER (geof:sfWithin(?bldgWkt, ?distWkt) && ?bldgType = 'church'\n)"
//                + "FILTER (?v > 545.0 && ?v < 550.0\n)"
//                + "}\n";
//
//        executeAndCompareValues(query6, ImmutableList.of("\"517.755\"^^xsd:double", "\"521.257\"^^xsd:double", "\"520.765\"^^xsd:double"));
//    }

}
