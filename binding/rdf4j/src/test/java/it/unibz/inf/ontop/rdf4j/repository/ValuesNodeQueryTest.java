package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class ValuesNodeQueryTest extends AbstractRDF4JTest {

    private static final String MAPPING_FILE = "/values-node/mapping.obda";
    private static final String ONTOLOGY_FILE = "/values-node/ontology.owl";
    private static final String SQL_SCRIPT = "/values-node/example.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, MAPPING_FILE, ONTOLOGY_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testOntologyObjectProperty() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?s <http://te.st/ValuesNodeTest#teaches> ?o\n" +
                "}");
        assertEquals(count, 3);
    }

    @Test
    public void testOntologySpo() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "\t?s ?p ?o\n" +
                "}");
        assertEquals(count, 16);
    }

    @Test
    public void testBGP1() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "  ?c a <http://te.st/ValuesNodeTest#Course> ." +
                "  ?t a <http://te.st/ValuesNodeTest#Teacher>.\n" +
                "  ?t <http://te.st/ValuesNodeTest#teaches> ?c " +
                "}");
        assertEquals(count, 3);
    }

    @Test
    public void testBGP2() {
        int count = runQueryAndCount("SELECT * WHERE {\n" +
                "  ?s a <http://te.st/ValuesNodeTest#Student>.\n" +
                "  ?s <http://te.st/ValuesNodeTest#attends> ?c " +
                "}");
        assertEquals(count, 2);
    }

    @Test
    public void testBGP3() {
        int count = runQueryAndCount("SELECT ?c WHERE {\n" +
                "  ?s a ?c \n" +
                "  FILTER (?c = <http://te.st/ValuesNodeTest#Student>)" +
                "}");
        assertEquals(count, 2);
    }
}
