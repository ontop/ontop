package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class Issue798Test extends AbstractRDF4JTest {

    private static final String LENSES_FILE = "/issue798/lenses.json";
    private static final String OBDA_FILE = "/issue798/mapping.obda";
    private static final String SQL_SCRIPT = "/issue798/database.sql";
    private static final String PROPERTIES = "/issue798/issue798.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES, LENSES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }
    @Test
    public void testQuery() {
        String sql = reformulateIntoNativeQuery("PREFIX : <http://example.org/> \n" +
                "SELECT (SUM(?line_item_ct) AS ?totalLineItems) (SUM(?revenue) AS ?totalSales)\n" +
                "WHERE {\n" +
                "  ?transaction a :Transaction ;\n" +
                "               :revenue ?revenue ;\n" +
                "               :line_item_ct ?line_item_ct  ." +
                "}");
        assertFalse("There should be no distinct in " + sql, sql.toLowerCase().contains("distinct"));
    }

}
