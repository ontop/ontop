package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class RDF4JBnodeProfTest extends AbstractRDF4JTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA("/prof/prof.sql", "/prof/prof-bnode.obda");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testProfessor1() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  *\n" +
                "WHERE {\n" +
                "  ?v a :Professor ; :firstName ?n \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(8, count);
    }

    @Test
    public void testProfessor2() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (COALESCE(str(?p), \"not-available\") AS ?v)\n" +
                "WHERE {\n" +
                "  ?p a :Professor \n" +
                "}" +
                "LIMIT 2";
        runQueryAndCompare(query, ImmutableList.of("not-available", "not-available"));
    }

    @Test
    public void testProfessor3() {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  *\n" +
                "WHERE {\n" +
                "  ?v a :Professor ; :firstName \"Michael\" . \n" +
                "}";
        int count = runQueryAndCount(query);
        assertEquals(1, count);
    }

}
