package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;


public class IRIDecompositionTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void before() throws Exception {
        initR2RML("/iri-decomposition/iri-decomposition.sql",
                "/iri-decomposition/iri-decomposition.ttl",
                "/iri-decomposition/iri-decomposition.owl");
    }

    @AfterClass
    public static void after() throws Exception {
        release();
    }

    @Test
    public void testIriDecomposition() throws Exception {
        String query = "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
                        "SELECT ?p " +
                        "WHERE { <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/Reviewer10> ?p ?o}";

        String sql = checkReturnedValuesAndReturnSql(query, "p", ImmutableList.of());
        assertFalse(sql.toUpperCase().contains("ER10"));
    }
}
