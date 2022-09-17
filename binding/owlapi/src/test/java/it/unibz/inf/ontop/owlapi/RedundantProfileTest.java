package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

public class RedundantProfileTest extends AbstractOWLAPITest {

    @BeforeClass
    public static void before() throws OWLOntologyCreationException, SQLException, IOException {
        initOBDA("/profile/create-db.sql", "/profile/profile.obda");
    }

    @AfterClass
    public static void after() throws OWLException, SQLException {
        release();
    }

    @Test
    public void testProfileApp1() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/profiling/voc#>\n" +
                "SELECT * {\n" +
                "  ?p a :Profile ; :app <http://example.com/app1> . \n" +
                "}";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "p", ImmutableList.of(
                "<http://example.com/profile/1>"))
                .toLowerCase();
        assertFalse(loweredSQL.contains("union"));
        assertFalse(loweredSQL.contains("distinct"));
    }

    @Test
    public void testProfileApp2() throws Exception {
        String sparqlQuery = "PREFIX : <http://example.org/profiling/voc#>\n" +
                "SELECT * {\n" +
                "  ?p a :Profile ; :app <http://example.com/app2> . \n" +
                "} ORDER BY ?p";

        String loweredSQL = checkReturnedValuesAndReturnSql(sparqlQuery, "p", ImmutableList.of(
                "<http://example.com/profile/1>",
                "<http://example.com/profile/2>"))
                .toLowerCase();
        assertFalse(loweredSQL.contains("union"));
        assertFalse(loweredSQL.contains("distinct"));
    }
}
