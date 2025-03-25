package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class NotExistsTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_not_exists.obda";
    private static final String SQL_SCRIPT = "/person/person_not_exists.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void notExistsTest() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE \n" +
                "{\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?v :firstName ?fname }\n" +
                "}\n";

        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/3", "http://person.example.org/person/4"));
    }

    @Ignore("Non overlapping variables are not supported")
    @Test
    public void testNonOverlappingVariables() {
        String sparql = "SELECT * \n" +
                "WHERE { ?s ?p ?o \n" +
                "         FILTER NOT EXISTS { ?x ?y ?z } \n" +
                "}\n";
        int countResults = runQueryAndCount(sparql);
        assertEquals(0, countResults);
    }

    @Test
    public void testFilterConstants1() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "\n" +
                "SELECT ?v \n" +
                "WHERE { ?v :firstName ?fname ; \n" +
                "       FILTER NOT EXISTS { <http://person.example.org/person/1> :firstName ?fname } \n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/2"));
    }

    @Ignore("At least one variable must be present in the not exists graph pattern")
    @Test
    public void testFilterConstants2() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "\n" +
                "SELECT ?v \n" +
                "WHERE { ?v :firstName ?fname ; \n" +
                "       FILTER NOT EXISTS { <http://person.example.org/person/1> :firstName \"Roger\" } \n" +
                "}\n";
        int countResults = runQueryAndCount(sparql);
        assertEquals(0, countResults);
    }

    @Ignore("The inner filter variables must be bound in the not exists graph pattern")
    @Test
    public void testFilterUnboundVariable() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "        ?v :firstName ?fname .\n" +
                "        FILTER NOT EXISTS {\n" +
                "                ?v :nickname ?nick .\n" +
                "                FILTER(?fname = ?nick)\n" +
                "        }\n" +
                "}";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/1"));
    }

    @Test
    public void testFilterSharedConstants() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "    ?v a :Person ;\n" +
                "        :firstName \"Roger\" .\n" +
                "   FILTER NOT EXISTS {\n" +
                "       ?v :firstName ?fname .\n" +
                "       FILTER (?fname NOT IN (\"Roger\", \"Paul\"))\n" +
                "    }\n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/1"));
    }

    @Test
    public void testGraphPatternFilterOrder() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "\n" +
                "SELECT ?v ?lname\n" +
                "WHERE \n" +
                "{\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?v :firstName ?fname }\n" +
                "    ?v :lastName ?lname\n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/3"));
    }

    @Test
    public void testSharedVariables() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "\n" +
                "SELECT ?v \n" +
                "WHERE \n" +
                "{\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?person :firstName ?sharedName }\n" +
                "    ?v :nickname ?sharedName\n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/1", "http://person.example.org/person/3", "http://person.example.org/person/4"));
    }
}
