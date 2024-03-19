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
//                + "BIND (rasdb:rasCheckBBOX(?regionBBOX, ?rasterBBOX) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query2, ImmutableList.of("\"array\"^^xsd:string"));
//    }

    //    @Test
//    public void clipRaster() {
//
//        String query2 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT * {\n"
////                + "?r rdfs:label ?region_name .\n"
//                + "?x rasdb:hasRasterName ?raster_name .\n"
//                + "?r geo:asWKT ?region .\n"
////                + "FILTER (?region_name = 'Deggendorf'\n)"
////                + "FILTER (contains(?raster_name,'Bavaria')) .\n"
//                + "BIND (rasdb:rasClipRaster(?raster_name, ?region, 100) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query2, ImmutableList.of("\"array\"^^xsd:string"));
//    }

    @Test
    public void averageRaster() {

        String query3 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT * {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "?x rasdb:hasScaleFactor ?ras_sf .\n"
                + "?r geo:asWKT ?region .\n"
                + "FILTER (?region_name = 'Deggendorf'\n)"
//                + "FILTER (contains(?ras_table,'Bavaria')) .\n"
                + "BIND (rasdb:rasSpatialAverage(?raster_name, ?region, 100, ?ras_sf) AS ?v)"
                + "}\n";

        executeAndCompareValues(query3, ImmutableList.of("\"7.989\"^^xsd:double"));
    }

//    @Test
//    public void averageRaster() {
//
//        String query4 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT * {\n"
//                + "?r rdfs:label ?region_id .\n"
//                + "?x rasdb:hasRasterName ?ras_table .\n"
//                + "?x rasdb:hasScaleFactor ?ras_sf .\n"
//                + "?r geo:asWKT ?region .\n"
//                + "FILTER (?region_id = 'Deggendorf'\n)"
//                + "FILTER (contains(?ras_table,'Bavaria')) .\n"
//                + "BIND (rasdb:rasSpatialAverage(?ras_table, ?region, 100, ?ras_sf) AS ?v)"
//                + "}\n";
//
//        executeAndCompareValues(query4, ImmutableList.of("\"66\"^^xsd:double"));
//    }
//
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
//        executeAndCompareValues(query1, ImmutableList.of("\"array\"^^xsd:string")); //Array issue in RDF @Diego
//    }

}
