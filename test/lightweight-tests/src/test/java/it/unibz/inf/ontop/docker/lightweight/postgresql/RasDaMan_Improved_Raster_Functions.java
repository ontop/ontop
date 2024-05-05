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
                + "FILTER (?region_name = 'München'\n)" //Vector region = Würzburg Erding, Kelheim, Linköping, Ultimo
                + "FILTER (CONTAINS(?raster_name, 'Baveria')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialAverageX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"13701.162\"^^xsd:integer"));
    }

    @Test
    public void USER_avgRasterSPATIALX() {
        String queryX = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v {\n"
                + "?r rdfs:label ?region_name .\n"
                + "?r geo:asWKT ?region .\n"
                + "?x rasdb:hasRasterName ?raster_name .\n"
                + "FILTER (?region_name = 'München'\n)" //Vector region = Würzburg Erding, Kelheim, Linköping, Ultimo
                + "FILTER (CONTAINS(?raster_name, 'Baveria')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialAverageX(?time, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(queryX, ImmutableList.of("\"13701.162\"^^xsd:integer"));
    }

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
                + "FILTER (?region_name = 'München'\n)" //Vector region = Würzburg Erding, Kelheim, Linköping, Ultimo
                + "FILTER (CONTAINS(?raster_name, 'Baveria')\n)" //Raster Dataset = Bavaria_Temperature_MODIS_1km, Surface_Temperature_Sweden, South_Tyrol_Temperature_MODIS_1km
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

}
