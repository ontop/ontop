import it.unibz.inf.ontop.rdf4j.repository.AbstractRDF4JTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class QueryContainmentIRISafeTest extends AbstractRDF4JTest {

    private static final String MAPPING_FILE = "/iri-safe-query-containment/mapping.obda";
    private static final String SQL_SCRIPT = "/iri-safe-query-containment/example.sql";
    private static final String ONTOLOGY_FILE = "/iri-safe-query-containment/ontology.ttl";
    private static final String LENSES_FILE = "/iri-safe-query-containment/lenses.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, MAPPING_FILE, ONTOLOGY_FILE, null, LENSES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testT() {
        var sql = reformulateIntoNativeQuery("SELECT * WHERE {\n" +
                "\t?s a <http://example.org/ontology#T>\n" +
                "}");
        assertFalse("Should not contain an UNION", sql.toLowerCase().contains("union"));
    }
}
