package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class FunctionAliasTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/alias/h2_alias_function.sql",
                "/alias/alias_function.obda",
                "/alias/alias_function.owl");
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

        checkReturnedValues(query, "v",
                ImmutableList.of("\"the sun\"^^xsd:string",
                        "\"winter is coming\"^^xsd:string"));
    }
}
