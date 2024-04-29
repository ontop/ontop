package it.unibz.inf.ontop.docker.lightweight.postgresql;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.sql.SQLException;

public class RasDaManConnectionCheckTest extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/rasdaman/rasdaman.properties";
    private static final String OBDA_FILE = "/rasdaman/rasdaman.obda";

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
    public void testConnection() {

        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT * {\n"
                + "?x rasdb:hasConnection ?v .\n"
                + "}\n";

            executeAndCompareValues(query1, ImmutableList.of("\"RasDaMan is connected\"^^xsd:string"));
    }

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
                + "FILTER (?region_name = 'Deggendorf'\n)"
                + "BIND (rasdb:rasSpatialAverage(100, ?ras_sf, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query3, ImmutableList.of("\"274.998\"^^xsd:double"));
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

        executeAndCompareValues(query4, ImmutableList.of("\"277.08\"^^xsd:double"));
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
                + "FILTER (?region_name = 'Deggendorf'\n)" //Regen Erding Kelheim Linköping Deggendorf Ultimo and ALSO change 3 params in geo2grid_coords function
                + "BIND (rasdb:rasSpatialMinimum(100, ?ras_sf, ?region, ?raster_name) AS ?v)"
                + "}\n";

        executeAndCompareValues(query5, ImmutableList.of("\"272.88\"^^xsd:double"));
    }
}