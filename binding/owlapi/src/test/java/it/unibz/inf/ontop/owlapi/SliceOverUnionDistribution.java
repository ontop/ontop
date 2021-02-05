package it.unibz.inf.ontop.owlapi;

import org.junit.*;

/**
 * Checks that that SLICE does not distribute over UNION (reproduces a bug).
 */
public class SliceOverUnionDistribution extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/slice/create.sql",
                "/slice/university.obda",
                "/slice/university.ttl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testQuery() throws Exception {
        String query = "PREFIX : <http://example.org/voc#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "\n" +
                "SELECT ?person ?name WHERE {\n" +
                "    ?person foaf:lastName ?name .\n" +
                "    {\n" +
                "        SELECT ?person WHERE { ?person rdf:type :Teacher . } LIMIT 1\n" +
                "    }\n" +
                "}";
        checkNumberOfReturnedValues(query, 1);
    }
}
