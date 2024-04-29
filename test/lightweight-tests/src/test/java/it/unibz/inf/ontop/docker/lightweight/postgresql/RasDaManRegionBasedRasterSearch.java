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
    public void regionBasedRasterSearch() {

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
                + "FILTER (?region_name = 'Stockholm'\n)" //Vector region = München (13938), Deggendorf, Stockholm (14432), Linköping, Ultimo (13791), Bolzano
                + "BIND (100 AS ?time\n)"
                + "BIND (rasdb:rasSpatialMaximumX(?time, ?region, ?min_lon, ?max_lat, ?x_res, ?y_res, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query1, ImmutableList.of("\"14432\"^^xsd:integer"));
    }
}
