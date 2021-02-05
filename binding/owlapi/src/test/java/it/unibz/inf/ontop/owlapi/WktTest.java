package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;


public class WktTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/wkt/wkt_test_create.sql",
                "/test/wkt/wkt_test.obda",
                "/test/wkt/wkt_test.owl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testWkt1() throws Exception {
        String query =  "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" +
                "SELECT ?s ?v\n" +
                "WHERE { ?s geo:asWKT ?v }";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"POLYGON((-77.089005 38.913574, -77.029953 38.913574, -77.029953 38.886321, -77.089005 38.886321, -77.089005 38.913574))\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>"));
    }
}
