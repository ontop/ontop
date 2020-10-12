package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class FunctionAliasTest extends AbstractOWLAPITest {

    private static final String CREATE_SCRIPT = "/alias/h2_alias_function.sql";
    private static final String OWL_FILE = "/alias/alias_function.owl";
    private static final String ODBA_FILE = "/alias/alias_function.obda";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA(CREATE_SCRIPT, ODBA_FILE, OWL_FILE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testAlias() throws Exception {

        String query = "PREFIX :	<http://www.movieontology.org/2009/11/09/movieontology.owl#>" +
                "SELECT  ?v " +
                "WHERE {?y :title ?v . }";

        String sql = checkReturnedValuesAndReturnSql(query, "v",
                ImmutableList.of("the sun", "winter is coming"));
    }
}
