package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class UniversityWithIndividualsTBoxFactTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/tbox-facts/university.sql";
    private static final String OBDA_FILE = "/tbox-facts/university.obda";
    private static final String OWL_FILE = "/tbox-facts/university-with-individuals.ttl";
    private static final String PROPERTIES_FILE = "/tbox-facts/factextraction.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testABoxSaturationSubClass() {
        String query = "PREFIX : <http://example.org/voc#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v\n" +
                "WHERE {\n" +
                "?v a :FacultyMember .\n" +
                "}\n";

        ImmutableSet<String> results = ImmutableSet.of("http://example.org/voc#Humphrey");
        runQueryAndCompare(query, results);
    }
}
