package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;


public class WktTest extends AbstractOWLAPITest {

    private static final String CREATE_SCRIPT = "/test/wkt/wkt_test_create.sql";
    private static final String ODBA_FILE = "/test/wkt/wkt_test.obda";
    private static final String OWL_FILE = "/test/wkt/wkt_test.owl";

    @BeforeClass
    public static void setUp() throws Exception {
        AbstractOWLAPITest.initOBDA(CREATE_SCRIPT, ODBA_FILE, OWL_FILE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        AbstractOWLAPITest.release();
    }

    @Test
    public void testWkt1() throws Exception {

        String query =  "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?s geo:asWKT ?v" +
                "}";

        List<String> expectedValues = ImmutableList.of(
                "POLYGON((-77.089005 38.913574, -77.029953 38.913574, -77.029953 38.886321, -77.089005 38.886321, -77.089005 38.913574))");
        checkReturnedValuesAndReturnSql(query, expectedValues);
    }
}
