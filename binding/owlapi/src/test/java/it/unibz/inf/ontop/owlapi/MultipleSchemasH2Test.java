package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

public class MultipleSchemasH2Test extends AbstractOWLAPITest {

    private final static String dbfile = "/multischema/multiple-schema-test.sql";
    private final static String owlfile = "/multischema/multiple-schema-test.owl";
    private final static String obdafile = "/multischema/multiple-schema-test.obda";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA(dbfile, obdafile, owlfile);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testSingleColum() throws Exception {
        String query = "PREFIX : <http://www.ontop.org/> SELECT ?v WHERE {<http://www.ontop.org/test-Cote%20D%27ivore> a ?v}";
        checkReturnedValues(query, "v", ImmutableList.of("<http://www.ontop.org/Test>"));
    }
}
