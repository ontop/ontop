package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class IncompleteSerializedMetadataPersonTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_basicviews.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String VIEW_FILE = "/person/views/basic_views.json";
    private static final String DBMETADATA_FILE = "/person/incomplete_person.db-extract.json";
    private static final String PROPERTIES_FILE = "/person/with_black_box_extraction.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES_FILE, VIEW_FILE, DBMETADATA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Concatenation and upper case
     */
    @Test
    public void testPersonConcat() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :fullNameAndLocality ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("ROGER SMITH Botzen"));
    }
}
