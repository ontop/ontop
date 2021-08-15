package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

@Ignore
public class RDFStarTest extends AbstractRDF4JTest {
    private static final String MAPPING = "/prof/rdfstartest/prof-rdfstar.ttl";
    private static final String SQL_SCRIPT = "/prof/prof.sql";
    private static final String ONTOLOGY = "/prof/prof.owl";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initR2RML(SQL_SCRIPT, MAPPING, ONTOLOGY);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test // illustrating my thesis, let's not commit this
    public void thesisIllustration() throws Exception {
        String query = "PREFIX : <http://lukas.thesis.org/films#>\n" +
                "SELECT DISTINCT ?actor \n" +
                "WHERE {\n" +
                " ?film :stars ?actor . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }

    @Test // Retrieve all triples
    public void testSPO() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?s ?p ?o \n" +
                "WHERE {\n" +
                " ?s ?p ?o . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }

    @Test // normal query with sparql-star results
    public void testEmbeddedTripleInQueryResult() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v ex:certainty ?c.  \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }


    @Test // Sparql-star test, with embedded triple in the query
    public void testEmbeddedTripleInSPARQLWhere() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT  ?c\n" +
                "WHERE {\n" +
                "<<?x rdf:type :Professor>> ex:certainty ?c.  \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }


    @Test // Double nesting
    public void testDeeperEmbeddedTripleInSPARQLWhere() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "PREFIX ex: <http://example.org/>\n" +
                "SELECT  ?x\n" +
                "WHERE {\n" +
                "<<<<?x rdf:type :Professor>> ex:certainty ?c>> ex:source ?s. \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }


    @Test // Sparql-star test, with very deep embedded triple in the query
    public void testReallyDeepEmbeddingInSPARQLWhere() throws Exception {
        String query = "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT  ?a ?e ?h\n" +
                "WHERE {\n" +
                "<< << <<?a ?b ?c>> ?d <<?e ?f ?g>> >> ?h << <<?i ?j ?k>> ?l <<?m ?n ?o>> >> >> ?p " +
                "<< << <<?a2 ?b2 ?c2>> ?d2 <<?e2 ?f2 ?g2>> >> ?h2 << <<?i2 ?j2 ?k2>> ?l2 <<?m ?n ?o>> >> >>" +
                "}";
        runQueryAndCompare(query, ImmutableList.of());
    }
}
