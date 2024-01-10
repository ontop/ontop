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
//    private static final String LENS_FILE = "/nested/postgresql/nested-lenses-array.json";
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

//        String query = "PREFIX  rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                + "SELECT ?v WHERE \n"
//                + "{  ?v ?p ?o .\n"
//                + "}\n" +
//                "limit 1";
           String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
                    + "SELECT * {\n"
                    + "?x rdfs:label ?v .\n"
                    + "}\n";
//        executeAndCompareValues(query1, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
//                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));

            executeAndCompareValues(query1, ImmutableList.of("\"RasDaMan is connected\"^^xsd:string"));
    }

//    @Test
//    public void averageRaster() {
//
//        String query1 = "PREFIX :\t<http://www.semanticweb.org/arkaghosh/OntoRaster/>\n"
//                      + "PREFIX rdfs:\t<http://www.w3.org/2000/01/rdf-schema#>\n"
//                      + "PREFIX geo:\t<http://www.opengis.net/ont/geosparql#>\n"
//                      + "PREFIX rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
//                      + "SELECT * {\n"
//                      + "?r rdfs:label ?region_id .\n"
//                      + "?x rasdb:hasRasterName ?ras_table .\n"
//                      + "?x rasdb:hasScaleFactor ?ras_sf .\n"
//                      + "?r geo:asWKT ?region .\n"
//                      + "FILTER (?region_id = 'Deggendorf'.\n)"
//                      + "FILTER (contains(?ras_table)='bavaria') .\n"
//                      + "BIND (rasdb:average(?ras_table, ?region, 100, ?ras_sf) AS ?v)"
//                      + "}\n";
//
////        executeAndCompareValues(query1, ImmutableList.of("\"region\"^^xsd:string", "\"value\"^^xsd:double"));
//        executeAndCompareValues(query1, ImmutableList.of("\"66\"^^xsd:double"));
//
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