package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class TBoxFactTest extends AbstractRDF4JTest {

    // TODO: use another SQL script
    private static final String CREATE_DB_FILE = "/label_comment.sql";
    // TODO: use another mapping file
    private static final String OBDA_FILE = "/label_comment.obda";
    // TODO: use an ontology file
    private static final String OWL_FILE = null;
    // TODO: use a properties file
    private static final String PROPERTIES_FILE = null;

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * TODO: replace MY_CLASS_TO_TEST by a concrete class
     */
    @Ignore
    @Test
    public void testSubclasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v rdfs:subClassOf MY_CLASS_TO_TEST .\n" +
                "}\n" +
                "ORDER BY ?v";

        // TODO: provide the expected results
        ImmutableList<String> results = ImmutableList.of("testdata");
        runQueryAndCompare(query, results);
    }

    @Ignore
    @Test
    public void testRDFSClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a rdfs:Class .\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, getExpectedClasses());
    }

    @Ignore
    @Test
    public void testOWLClasses() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "   ?v a owl:Class .\n" +
                "}\n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, getExpectedClasses());
    }

    /**
     * TODO: provide the list of expected classes
     */
    private ImmutableList<String> getExpectedClasses() {
        return ImmutableList.of("testdata");
    }


}