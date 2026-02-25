package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DomainSimplificationTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/domain-simplification/mapping.obda";
    private static final String SQL_SCRIPT = "/domain-simplification/database.sql";
    private static final String ONTOLOGY = "/domain-simplification/ontology.ttl";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testT1() {
        var query = "prefix ex: <http://example.org/>\n" +
                "\n" +
                "select * where {\n" +
                "  ?t a ex:T1 \n" +
                "}";

        var sql = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse("The union should have been eliminated:\n " + sql,sql.contains("union"));
    }


}
