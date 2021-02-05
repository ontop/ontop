package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.*;

import static org.junit.Assert.assertFalse;


public class NullIfTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void before() throws Exception {
        initOBDA("/nullif/nullif-create.sql",
                "/nullif/nullif.obda",
                "/nullif/nullif.ttl");
    }

    @AfterClass
    public static void after() throws Exception {
        release();
    }

    @Test
    public void testSelectNumbers() throws Exception {
        String query = "PREFIX ex: <http://example.org/>" +
                "SELECT ?v\n" +
                "WHERE { ?s ex:hasNumber ?v }";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of("\"2\"^^xsd:integer"));
        assertFalse(sql.toUpperCase().contains("NULLIF"));
    }
}
