package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaManRasterFunctionsTest extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/rasdaman.properties";
    private static final String OBDA_FILE = "/rasdaman/rasdaman.obda";
    private static final String OWL_FILE = "/rasdaman/rasdaman.owl";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

//    @Test
//    public void getRasterMeta() {
//
//        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www/.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT * {\n"
////                + "?r ?raster_id .\n"
//                + "?r rasdb:hasRasterName ?v .\n"
////                + "FILTER (contains(?raster_name,'Bavaria')) .\n"
//                + "}\n";
//
//        executeAndCompareValues(query1, ImmutableList.of("\"Bavaria_Temperature_MODIS_1km\"^^xsd:string"));
//    }
//

//    @Test
//    public void checkBBOX(){
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT * {\n"
////              + "?r rdfs:label ?region_name .\n"
//                + "?r geo:asWKT ?regionBBOX .\n"
    //            + "?x rasdb:hasRasterName ?raster_name .\n"
    //            + ?x rasdb:hasRasterBBOX /\n"
//                + "BIND (rasdb:rasCheckBBOX(Raster?regionBBOX, ?rasterBBOX) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query2, ImmutableList.of("\"array\"^^xsd:string"));
//    }


    @Test
    public void averageRasterSPATIAL() {

        String query3 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasScaleFactor ?ras_sf .\n"
                + "?r geo:asWKT ?region .\n"
                + "FILTER (?region_name = 'Kelheim'\n)"
//                + "FILTER (contains(?ras_table,'Bavaria')) .\n" Regen Erding Kelheim
                + "BIND (rasdb:rasSpatialAverage(100, ?ras_sf, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query3, ImmutableList.of("\"275.462\"^^xsd:double"));
    }

    @Test
    public void maxRasterSPATIAL() {

        String query4 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasScaleFactor ?ras_sf .\n"
                + "?r geo:asWKT ?region .\n"
                + "FILTER (?region_name = 'Deggendorf'\n)"
                + "BIND (rasdb:rasSpatialMaximum(100, ?ras_sf, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query4, ImmutableList.of("\"277.82\"^^xsd:double"));
    }

    @Test
    public void minRasterSPATIAL() {

        String query5 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasScaleFactor ?ras_sf .\n"
                + "?r geo:asWKT ?region .\n"
                + "FILTER (?region_name = 'Deggendorf'\n)"
                + "BIND (rasdb:rasSpatialMinimum(100, ?ras_sf, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query5, ImmutableList.of("\"263.9\"^^xsd:double"));
    }

    @Test
    public void clipRaster() {

        String query2 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?r geo:asWKT ?region .\n"
                + "FILTER (?region_name = 'Deggendorf'\n)"
                + "BIND (rasdb:rasClipRaster(100, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query2, ImmutableList.of("\"array\"^^xsd:string")); //xsd:array is not availabel
    }
//
//
//        @Test
//    public void getRasterArraySmall() {
//
//        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT ?v {\n"
//                + "?x rasdb:hasRasterName ?raster_name .\n"
//                + "?x rasdb:hasStartTime ?start_time; rasdb:hasEndTime ?end_time .\n"
//                + "BIND (rasdb:rasClipRaster(?start_time,?end_time, ?raster_name) AS ?v)" //%s %s %s
//                + "}\n";
//
//        executeAndCompareValues(query1, ImmutableList.of("\"{{{0,0,0},{0,0,0},{0,0,0}},{{0,0,0},{0,0,0},{0,0,0}}}\"^^xsd:string")); //Array issue in RDF @Diego
//    }
//    @Test
//    public void getRasterArray() {
//
//        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT * {\n"
//                + "?x rasdb:hasRasterName ?ras_table . \n"
//                + "?x rasdb:hasStartTime ?start_time; rasdb:hasEndTime ?end_time .\n"
//                + "FILTER (contains(?ras_table)='bavaria') .\n"
//                + "BIND (rasdb:getArray(?ras_table, ?start_time, ?end_time) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query1, ImmutableList.of("\"{{{0,0,0},{0,0,0},{0,0,0}},{{0,0,0},{0,0,0},{0,0,0}}}\"^^xsd:string")); //Array issue in RDF @Diego
//    }

}
