package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;

/**
Test whether multiple OPTIONAL clauses with language tags can work
 */
public abstract class AbstractMultipleOptionalsTest extends AbstractRDF4JTest {

    @Test
    public void testOneOptional() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'en')}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("english"));
    }

    @Test
    public void testTwoOptionals() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . \n" +
                "  FILTER (LANG(?v) = 'en') .}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . \n" +
                "  FILTER (LANG(?v) = 'fr') .}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("french", "english"));
    }

    @Test
    public void testThreeOptionals() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'en')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'fr')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'de')}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("french", "german", "english"));
    }

    @Test
    public void testThreeOptionalsWithOrderBy() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v ?l\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'en')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'fr')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'de')}\n" +
                "  BIND(str(?v) AS ?l)\n" +
                "}\n" +
                "ORDER BY ?l";
        runQueryAndCompare(query, ImmutableList.of("english","french", "german"));
    }

    @Test
    public void testFourOptionals() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'en')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'fr')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'de')}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANG(?v) = 'zh')}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("chinese", "german", "french", "english"));
    }

    @Test
    public void testThreeOptionalsConstantLang() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?label . " +
                "  FILTER (LANG(?label) = 'en')" +
                "   BIND (\"en\" AS ?v)}\n" +
                "  OPTIONAL { ?source rdfs:label ?label . " +
                "  FILTER (LANG(?label) = 'fr')" +
                "   BIND (\"fr\" AS ?v)}\n" +
                "  OPTIONAL { ?source rdfs:label ?label . " +
                "  FILTER (LANG(?label) = 'de')" +
                "   BIND (\"de\" AS ?v)}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("en", "fr", "de"));
    }

    @Test
    public void testOneOptionalLangMatches() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'en'))}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("english"));
    }

    @Test
    public void testTwoOptionalsLangMatches() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . \n" +
                "  FILTER (LANGMATCHES(LANG(?v),'en'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . \n" +
                "  FILTER (LANGMATCHES(LANG(?v),'fr'))}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("french", "english"));
    }
    
    @Test
    public void testThreeOptionalsLangMatches() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'en'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'fr'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'de'))}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("french", "german", "english"));
    }

    @Test
    public void testFourOptionalsLangMatches() {
        String query = "PREFIX ex: <http://example.org/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?source a ex:Individual . \n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'en'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'fr'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'de'))}\n" +
                "  OPTIONAL { ?source rdfs:label ?v . " +
                "  FILTER (LANGMATCHES(LANG(?v),'zh'))}\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("chinese", "german", "french", "english"));
    }
}
