package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/*
Test whether multiple OPTIONAL clauses with language tags can work
 */

public class MultipleOptionalTest extends AbstractRDF4JTest {

    private static final String CREATE_DB_FILE = "/multiple-optional/multiple-optional-create.sql";
    private static final String OBDA_FILE = "/multiple-optional/multiple-optional.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

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
        runQueryAndCompare(query, ImmutableList.of("english"));
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
        runQueryAndCompare(query, ImmutableList.of("english", "french"));
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
        runQueryAndCompare(query, ImmutableList.of("english", "french"));
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
        runQueryAndCompare(query, ImmutableList.of("english", "french"));
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
        runQueryAndCompare(query, ImmutableList.of("en", "fr", "de"));
    }
}
