package it.unibz.inf.ontop.rdf4j.repository;


import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RDF4JNullMetaMappingTest extends AbstractRDF4JTest {
    private static final String CREATE_DB_FILE = "/meta/null/meta-null-create.sql";
    private static final String MAPPING_FILE = "/meta/null/meta-null.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, MAPPING_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void test() {
        String query = "" +
                "PREFIX : <http://example.org/>\n"+
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
                "SELECT  ?s ?c\n" +
                "WHERE {\n" +
                "  ?s a ?c \n" +
                "}";

        TupleQueryResult result = evaluate(query);
        assertTrue(result.hasNext());
        result.next();
        assertFalse(result.hasNext());
        result.close();
    }
}

