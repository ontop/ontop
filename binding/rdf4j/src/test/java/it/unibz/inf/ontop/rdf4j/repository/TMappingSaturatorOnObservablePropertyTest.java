package it.unibz.inf.ontop.rdf4j.repository;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TMappingSaturatorOnObservablePropertyTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/destination-obervable-property/dest.obda";
    private static final String SQL_SCRIPT = "/destination-obervable-property/schema.sql";
    private static final String ONTOLOGY_FILE = "/destination-obervable-property/dest.owl";
    private static final String PROPERTIES_FILE = "/destination-obervable-property/dest.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }


    @Test
    public void testObservableProperties() {
        String sparql = "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT * WHERE {\n" +
                " ?sub a sosa:ObservableProperty .\n" +
                "}\n";

        String sql = reformulateIntoNativeQuery(sparql);
        System.out.println(sql);
        assertEquals(0, StringUtils.countMatches(sql, "DISTINCT"));
        assertEquals(0, StringUtils.countMatches(sql, "UNION"));
    }

}
