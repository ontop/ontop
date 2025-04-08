package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExistsTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_exists.obda";
    private static final String SQL_SCRIPT = "/person/person_exists.sql";

    @BeforeClass
    public static void before() throws Exception {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws Exception {
        release();
    }

    @Test
    public void existsTest() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE \n" +
                "{\n" +
                "   ?v a :Person .\n" +
                "   FILTER EXISTS { ?v :firstName ?fname }\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/2", "http://person.example.org/person/5"));
    }

    @Test
    public void testWithNoCommonVariables() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE \n" +
                "{\n" +
                "    ?v :lastName ?lname ;\n" +
                "    FILTER EXISTS { ?x :firstName ?fname }\n" +
                "}\n";

        int count = runQueryAndCount(sparql);
        Assert.assertEquals(1, count);
    }

    @Test
    public void testSharedVariable() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "    ?v :nickname ?sharedName .\n" +
                "    FILTER EXISTS { ?v :firstName ?sharedName . }\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/2"));
    }

    @Test
    public void testNullSharedVariable() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT * WHERE {\n" +
                "   ?v :lastName ?lname .\n" +
                "   FILTER EXISTS { <http://person.example.org/person/4> :lastName ?lname }\n" +
                "}";
        Assert.assertEquals(0, runQueryAndCount(sparql));
    }

    @Test
    public void testCardinalityConsistency() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "   ?s :firstName ?v .\n" +
                "   FILTER EXISTS { ?x :firstName ?v }\n" +
                "} ORDER BY ?v";
        runQueryAndCompare(sparql, ImmutableList.of("John", "Roger", "Roger"));
    }

    @Test
    public void quadsTest() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "   GRAPH <http://person.example.org/graph> { \n" +
                "       ?v a :Person .\n" +
                "       FILTER EXISTS {?v :firstName ?fname }\n" +
                "   }\n" +
                "} ORDER BY ?v";
        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/2", "http://person.example.org/person/5"));
    }

    @Test
    public void testOnlyConstantsInFilter() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?s WHERE {\n" +
                "   ?s a :Person .\n" +
                "   FILTER EXISTS { <http://person.example.org/person/1> :firstName \"Roger\" }\n" +
                "}";
        Assert.assertEquals(5, runQueryAndCount(sparql));
    }

    @Test
    public void testOnlyConstantsInFilter1() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?s WHERE {\n" +
                "   ?s a :Person .\n" +
                "   FILTER EXISTS { <http://person.example.org/person/1> :firstName \"WrongName\" }\n" +
                "}";
        Assert.assertEquals(0, runQueryAndCount(sparql));
    }
}
