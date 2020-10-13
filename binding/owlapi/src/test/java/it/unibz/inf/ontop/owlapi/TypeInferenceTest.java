package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

/**
 * test Type inference in the SQL Generator with the use of metadata when the value is unknown.
 */
public class TypeInferenceTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/typeinference/types-create-db.sql",
                "/test/typeinference/types.obda",
                "/test/typeinference/types.owl");
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

        checkReturnedValues(queryBind, "v", ImmutableList.of(
                "<http://example.org/types/voc#Philippines>",
                "<http://example.org/types/voc#China>"));
    }
}
