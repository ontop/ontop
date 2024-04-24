package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.Assert.*;

public class StrStartsFunctionTest extends AbstractRDF4JTest{
    private static final String CREATE_DB_FILE = "/strstarts-function/sparql-strstarts.sql";
    private static final String OBDA_FILE = "/strstarts-function/sparql-strstarts.obda";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(CREATE_DB_FILE, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testStrStartsConstants() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT ?s\n" +
                "WHERE {\n" +
                "?s a schema:Result.\n" +
                "FILTER (STRSTARTS(\"name\", \"x\"))\n"+
                "}";
        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of());
    }

    @Test
    public void testStrStartsPredicate() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT DISTINCT ?s\n" +
                "WHERE {\n" +
                " ?s ?p ?o. \n" +
                " ?s a schema:Person. \n" +
                " FILTER (STRSTARTS(str(?p), \"http://www.schema.org/givenName\"))\n"+
                "}";
        // predicates are constants so they can be optimized
        String reformulatedQuery = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse(reformulatedQuery.contains("substring"));

        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of(
                ImmutableMap.of("s", "http://www.schema.org/Person/1"),
                ImmutableMap.of("s", "http://www.schema.org/Person/2"),
                ImmutableMap.of("s", "http://www.schema.org/Person/4")
        ));
    }

    @Test
    public void testStrStartsIRI() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT DISTINCT ?s\n" +
                "WHERE {\n" +
                "?s ?p ?o.\n" +
                " FILTER (STRSTARTS(STR(?s), \"http://www.w3\"))\n"+
                "}";

        String reformulatedQuery = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse(reformulatedQuery.contains("substring"));

        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of(
                ImmutableMap.of("s", "http://www.w3.org/ns/sosa/Result/1"),
                ImmutableMap.of("s", "http://www.w3.org/ns/sosa/Result/2")));
    }

    @Test
    public void testStrStartsObject() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT DISTINCT ?s\n" +
                "WHERE {\n" +
                "?s ?p ?o.\n" +
                "?s a sosa:Result. \n" +
                " FILTER (STRSTARTS(STR(?o), \"2024\"))\n"+
                "}";
        String reformulatedQuery = reformulateIntoNativeQuery(query).toLowerCase();
        // objects aren't iri templates or a constants so the query can't be optimized
        assertTrue(reformulatedQuery.contains("substring"));
    }

    @Test
    public void testStrStartsComplex() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT DISTINCT ?o\n" +
                "WHERE {\n" +
                "?s ?p ?o.\n" +
                " FILTER (STRSTARTS(STR(?o), \"http://www.schema.org\"))\n"+
                "}";
        String reformulatedQuery = reformulateIntoNativeQuery(query).toLowerCase();
        // some of the objects are iri templates while others aren't
        assertTrue(reformulatedQuery.contains("substring"));

        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of(
                ImmutableMap.of("o", "http://www.schema.org/House"),
                ImmutableMap.of("o", "http://www.schema.org/Person"),
                ImmutableMap.of("o", "http://www.schema.org/Person/1"),
                ImmutableMap.of("o", "http://www.schema.org/Person/2")
        ));
    }

    @Test
    public void testStrStartsNull() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT ?res\n" +
                "WHERE {\n" +
                " ?s a schema:House.\n" +
                " OPTIONAL { \n"+
                "  ?s schema:belongs ?person. \n" +
                " } \n" +
                " BIND( COALESCE(STRSTARTS(STR(?person), \"http://www.schema.org\"), \"default\") AS ?res) \n"+
                "}";

        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertTrue(result.stream().anyMatch(r -> Objects.equals(r.get("res"), "default")));
    }

    @Test
    @Ignore("Optimization missing for concat function")
    public void testStrStartsConcat() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa/>\n" +
                "SELECT DISTINCT ?s \n" +
                "WHERE {\n" +
                "?s ?p ?o. \n" +
                "?s a schema:Person. \n" +
                " FILTER (STRSTARTS(CONCAT(STR(?s), \"a\"), \"http://www.schema.org\"))\n"+
                "}";
        String reformulatedQuery = reformulateIntoNativeQuery(query).toLowerCase();
        assertFalse(reformulatedQuery.contains("substring"));

        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of(
                ImmutableMap.of("s", "http://www.schema.org/Person/1"),
                ImmutableMap.of("s", "http://www.schema.org/Person/2"),
                ImmutableMap.of("s", "http://www.schema.org/Person/3"),
                ImmutableMap.of("s", "http://www.schema.org/Person/4"))
        );
    }

    @Test
    @Ignore("Optimization missing for concat function")
    public void testStrStartsConcatNull() {
        String query = "PREFIX schema: <http://www.schema.org/>\n" +
                "PREFIX sosa: <http://www.w3.org/ns/sosa#>\n" +
                "SELECT ?person ?res \n" +
                "WHERE {\n" +
                " ?person a schema:Person.\n" +
                " OPTIONAL {"+
                "  ?person schema:givenName ?name.\n" +
                "}" +
                " BIND( COALESCE(STRSTARTS(" +
                "                STR(CONCAT(?name, \"a\")), " +
                "                \"http://www.schema.org\"), " +
                "       \"default\") " +
                " AS ?res) \n"+
                "}";
        ImmutableList<ImmutableMap<String, String>> result = executeQuery(query);
        assertEquals(result, ImmutableList.of(
                ImmutableMap.of("person", "http://www.schema.org/Person/3", "res", "default"),
                ImmutableMap.of("person", "http://www.schema.org/Person/4", "res", "false"),
                ImmutableMap.of("person", "http://www.schema.org/Person/2", "res", "false"),
                ImmutableMap.of("person", "http://www.schema.org/Person/1", "res", "false"))
        );
    }



}
