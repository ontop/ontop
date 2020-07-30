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


public class NullIfTest extends AbstractOWLAPITest {

    private static final String CREATE_DB_FILE = "/nullif/nullif-create.sql";
    private static final String OBDA_FILE = "/nullif/nullif.obda";
    private static final String ONTOLOGY_FILE = "/nullif/nullif.ttl";

    @BeforeClass
    public static void before() throws IOException, SQLException, OWLOntologyCreationException {
        AbstractOWLAPITest.initOBDA(CREATE_DB_FILE, OBDA_FILE, ONTOLOGY_FILE);
    }

    @AfterClass
    public static void after() throws SQLException, OWLException {
        AbstractOWLAPITest.release();
    }

    @Test
    public void testSelectNumbers() throws Exception {
        String query = "PREFIX ex: <http://example.org/>" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "  ?s ex:hasNumber ?v\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, ImmutableList.of("2"));
        assertFalse(sql.toUpperCase().contains("NULLIF"));
    }
}
