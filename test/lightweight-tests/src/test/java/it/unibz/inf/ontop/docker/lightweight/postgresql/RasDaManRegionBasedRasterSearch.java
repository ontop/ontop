package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class RasDaManRegionBasedRasterSearch extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/rasdaman.properties";

    private static final String OBDA_FILE = "/rasdaman/ExperimentalMappings.obda";
    //  private static final String LENS_FILE = "/nested/postgresql/nested-lenses-array.json";
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
    public void I_PolygonRegionBasedRasterSearch() {

        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
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
                + "FILTER (?region_name = 'München'\n)" //Vector region = München (14623), Deggendorf, Stockholm (14432), Linköping, Ultimo (13791), Bolzano
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query1, ImmutableList.of("\"13938\"^^xsd:integer"));
    }  //München (14623)

    @Test
    public void II_PolygonRegionBasedRasterSearch() {

        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
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
                + "FILTER (?region_name = 'Würzburg'\n)" //Vector region = Würzburg (14356),
                + "BIND (273 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "} LIMIT 1\n";

        executeAndCompareValues(query1, ImmutableList.of("\"14356\"^^xsd:integer"));
    } // Würzburg (14356)

    @Test
    public void III_PolygonRegionBasedRasterSearch() {

        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
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
                + "FILTER (?region_name = 'Bayreuth'\n)" //Vector region = Bayreuth III (14286),
                + "BIND (273 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "} LIMIT 1\n";

        executeAndCompareValues(query1, ImmutableList.of("\"14286\"^^xsd:integer"));
    } // Bayreuth (14286)

    @Test
    public void IV_MultiPolygonRegionBasedRasterSearch() {

        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
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
                + "FILTER (?region_name = 'Göteborg'\n)" //Vector region = Göteborg IV (14422), Umeå IV (14392)
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "} LIMIT 1\n";

        executeAndCompareValues(query1, ImmutableList.of("\"14422\"^^xsd:integer"));
    } // Göteborg (14422)

    @Test
    public void V_MultiPolygonRegionBasedRasterSearch() {

            String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
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
                    + "FILTER (?region_name = 'Rosenheim'\n)" //Vector region = Rosenheim V (13853),
                    + "BIND (100 AS ?time\n)"
                    + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                    + "} LIMIT 1\n";

            executeAndCompareValues(query1, ImmutableList.of("\"13853\"^^xsd:integer"));
    } // Rosenheim (13853)
}



