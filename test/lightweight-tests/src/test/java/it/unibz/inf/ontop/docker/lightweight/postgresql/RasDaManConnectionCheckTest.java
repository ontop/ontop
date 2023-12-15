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

        String query = "PREFIX  rasdb:\t<http://www.semanticweb.org/RasterDataCube/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?v ?p ?o .\n"
                + "}\n" +
                "limit 1";

        executeAndCompareValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }
}
