package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class MultipleSchemasH2Test extends AbstractOWLAPITest {

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/multischema/multiple-schema-test.sql",
                "/multischema/multiple-schema-test.obda",
                "/multischema/multiple-schema-test.owl");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testSingleColum() throws Exception {
        String query = "PREFIX : <http://www.ontop.org/> " +
                "SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore> a ?v}";
        checkReturnedValues(query, "v", ImmutableList.of(
                "<http://www.ontop.org/Test>"));
    }
}
