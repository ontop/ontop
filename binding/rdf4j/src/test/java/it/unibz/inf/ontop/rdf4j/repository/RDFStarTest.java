package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;


public class RDFStarTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_basicviews.obda";
    private static final String SQL_SCRIPT = "/person/person.sql";
    private static final String VIEW_FILE = "/person/views/basic_views.json";
    private static final String ONTOLOGY = "/person/rdfstar/person_ontology.owl";
    //private static final String ONTOLOGY = "/person/rdfstar/person_ontology_star.owl"; This is an ontology written in rdf-star

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, ONTOLOGY, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test // normal query, test only that the turtle star ontology loads
    @Ignore // As we don't actually load the turtle star ontology
    public void testEmbeddedTripleInTurtleFile() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :localityAbbrev ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Bz"));
    }

    @Test // normal query with sparql-star results
    public void testEmbeddedTripleInQueryResult() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v :certainty ?c.  \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of(
                "<<http://person.example.org/#Jane http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://person.example.org/Person>>",
                "<<http://person.example.org/#Per http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://person.example.org/Person>>"));
    }


    @Test // Sparql-star test, with embedded triple in the query
    public void testEmbeddedTripleInSPARQLWhere() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT  ?x ?c ?s\n" +
                "WHERE {\n" +
                "<<<<?x rdf:type :Person>> ex:certainty ?c>> ex:source ?s.  \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Per", "Jane"));
    }


    @Test // Sparql-star test, with very deep embedded triple in the query
    public void testDeepEmbeddingInSPARQLWhere() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT  ?a ?e ?h\n" +
                "WHERE {\n" +
                "<< <<?a ?b ?c>> ?d <<?e ?f ?g>> >> ?h << <<?i ?j ?k>> ?l <<?m ?n ?o>> >> " +
                "}";
        runQueryAndCompare(query, ImmutableList.of("Per", "Jane"));
    }
}
