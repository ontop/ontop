package it.unibz.inf.ontop.owlapi;

import org.junit.*;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.readFromFile;

/**
 * Checks that that SLICE does not distribute over UNION (reproduces a bug).
 */
public class SliceOverUnionDistribution extends AbstractOWLAPITest {

    private static final String CREATE_SCRIPT = "/slice/create.sql";
    private static final String OWL_FILE = "/slice/university.ttl";
    private static final String MAPPING_FILE = "/slice/university.obda";
    private static final String QUERY_FILE = "src/test/resources/slice/slice.rq";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA(CREATE_SCRIPT, MAPPING_FILE, OWL_FILE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testQuery() throws Exception {
        String query = readFromFile(QUERY_FILE);
        checkNumberOfReturnedValues(query, 1);
    }
}
