package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class SubClassOfStarTest extends AbstractRDF4JTest {

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA("/subclassof-star/data.sql", "/subclassof-star/mapping.obda",
                "/subclassof-star/ontology.ttl", "/subclassof-star/properties.properties");
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testSubClassOfStar1() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { ?s a ?x . \n" +
                "?x rdfs:subClassOf* ?v \n" +
                " FILTER(!strstarts(str(?v), \"http://www.w3.org/\"))\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }

    @Test
    public void testSubClassOfStar2() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH ?g {\n" +
                "   ?s a ?x . " +
                "   ?x rdfs:subClassOf* ?v \n" +
                " }" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("http://example.org/ontology#Mammal", "http://example.org/ontology#Person"));
    }

    @Test
    public void testSubClassOfStar3() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH ?x {\n" +
                "   ?s a ?x . " +
                "   ?x rdfs:subClassOf* ?v \n" +
                " }" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("http://example.org/ontology#Mammal", "http://example.org/ontology#Person"));
    }

    @Test
    public void testSubClassOfStar4() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH <http://example.org/ontology#Person> {\n" +
                "   ?s a ?x . " +
                "   ?x rdfs:subClassOf* ?v \n" +
                " }" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("http://example.org/ontology#Mammal", "http://example.org/ontology#Person"));
    }

    /**
     * Disabled until unfolding of nodeInGraph is supported
     */
    @Test(expected = QueryEvaluationException.class)
    public void testSubClassOfStar5() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH <http://example.org/ontology#Person> {\n" +
                "   ?s a ?x . " +
                " }" +
                " ?x rdfs:subClassOf* ?v \n" +
                "}";
        runQuery(query);
    }

    /**
     * Disabled until unfolding of nodeInGraph is supported
     */
    @Test(expected = QueryEvaluationException.class)
    public void testSubClassOfStar6() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH ?g {\n" +
                "   ?x rdfs:subClassOf* ?v \n" +
                " }" +
                " ?s a ?x ." +
                "}";
        runQuery(query);
    }

    @Test
    public void testSubClassOfStar7() {
        String query = "SELECT DISTINCT ?x ?v " +
                "WHERE { \n" +
                " GRAPH ?g {\n" +
                "   ?s <http://example.org/ontology#p1> ?x . " +
                "   ?x rdfs:subClassOf* ?v \n" +
                " }" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("1", "2", "3"));
    }

    /**
     * NB: ?s rdfs:subClassOf ?s is currently not inserted in the graph
     */
    @Test
    public void testSubClassOfStar8() {
        String query = "SELECT DISTINCT ?v " +
                "WHERE { \n" +
                " GRAPH ?v {\n" +
                "   ?s <http://example.org/ontology#p1> ?v . " +
                "   ?v rdfs:subClassOf* ?v \n" +
                " }" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }
}
