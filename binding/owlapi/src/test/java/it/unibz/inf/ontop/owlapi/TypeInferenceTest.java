package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

/**
 * test Type inference in the SQL Generator with the use of metadata when the value is unknown.
 */
public class TypeInferenceTest extends AbstractOWLAPITest {

    private static final String ONTOLOGY_FILE = "/test/typeinference/types.owl";
    private static final String OBDA_FILE = "/test/typeinference/types.obda";
    private static final String CREATE_DB_FILE = "/test/typeinference/types-create-db.sql";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA(CREATE_DB_FILE, OBDA_FILE, ONTOLOGY_FILE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testType() throws Exception {
        String queryBind = "PREFIX : <http://example.org/types/voc#>\n" +
                "SELECT ?v \n" +
                "WHERE { ?x a :Asian_Company ; :hasCompanyLocation ?v . }";

        ImmutableList<String> expectedValues = ImmutableList.of(
                "<http://example.org/types/voc#Philippines>",
                "<http://example.org/types/voc#China>"
        );
        checkReturnedValues(queryBind, "v", expectedValues);
    }
}
