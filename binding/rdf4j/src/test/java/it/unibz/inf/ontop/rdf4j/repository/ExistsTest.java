package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void testFilterExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   FILTER EXISTS { ?v :firstName ?fname }\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1",
                "http://person.example.org/person/2", "http://person.example.org/person/5"));
    }

    @Test
    public void testFilterDisjunction() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   FILTER (?lname = \"Smith\" || EXISTS { ?v :firstName ?fname })\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1",
                "http://person.example.org/person/2", "http://person.example.org/person/5"));
    }

    // Non overlapping variables are not supported
    @Test(expected = QueryEvaluationException.class)
    public void testFilterNoCommonVariables() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "    ?v :lastName ?lname ;\n" +
                "    FILTER EXISTS { ?x :firstName ?fname }\n" +
                "}\n";

        int count = runQueryAndCount(sparql);
        Assert.assertEquals(1, count);
    }

    @Test
    public void testFilterSharedVariable() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
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
    public void testFilterWithQuads() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "   GRAPH <http://person.example.org/graph> { \n" +
                "       ?v a :Person .\n" +
                "       FILTER EXISTS {?v :firstName ?fname }\n" +
                "   }\n" +
                "} ORDER BY ?v";
        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1",
                "http://person.example.org/person/2", "http://person.example.org/person/5"));
    }

    // Not supported (no variables in the exists graph pattern)
    @Test(expected = QueryEvaluationException.class)
    public void testOnlyConstantsInFilter() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?s WHERE {\n" +
                "   ?s a :Person .\n" +
                "   FILTER EXISTS { <http://person.example.org/person/1> :firstName \"Roger\" }\n" +
                "}";
        Assert.assertEquals(5, runQueryAndCount(sparql));
    }

    // Not supported (no variables in the exists graph pattern)
    @Test(expected = QueryEvaluationException.class)
    public void testOnlyConstantsInFilter1() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?s WHERE {\n" +
                "   ?s a :Person .\n" +
                "   FILTER EXISTS { <http://person.example.org/person/1> :firstName \"WrongName\" }\n" +
                "}";
        Assert.assertEquals(0, runQueryAndCount(sparql));
    }

    @Test
    public void testNestedExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   FILTER EXISTS { ?v :firstName ?fname . FILTER EXISTS { ?v :lastName ?lname } }\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1"));
    }

    @Test
    public void testMultipleFilterExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   FILTER EXISTS { ?v :firstName ?fname }\n" +
                "   FILTER EXISTS { ?v :lastName ?lname }\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1"));
    }

    @Test
    public void testNotExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?v :firstName ?fname }\n" +
                "}\n";

        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/3",
                "http://person.example.org/person/4", "http://person.example.org/person/6"));
    }

    // Non overlapping variables are not supported
    @Test(expected = QueryEvaluationException.class)
    public void testNonOverlappingVariables() {
        String sparql = "SELECT * \n" +
                "WHERE { ?s ?p ?o \n" +
                "   FILTER NOT EXISTS { ?x ?y ?z } \n" +
                "}\n";
        int countResults = runQueryAndCount(sparql);
        assertEquals(0, countResults);
    }

    @Test
    public void testFilterNotExistsConstants1() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE { " +
                "   ?v :firstName ?fname ; \n" +
                "   FILTER NOT EXISTS { <http://person.example.org/person/1> :firstName ?fname } \n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/2"));
    }

    // Not supported (no variables present in the not exists graph pattern)
    @Test(expected = QueryEvaluationException.class)
    public void testFilterNotExistsAllConstants() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE { " +
                "       ?v :firstName ?fname ; \n" +
                "       FILTER NOT EXISTS { <http://person.example.org/person/1> :firstName \"Roger\" } \n" +
                "}\n";
        int countResults = runQueryAndCount(sparql);
        assertEquals(0, countResults);
    }

    // The inner filter variables not bound in the not exists graph pattern is not supported
    @Test(expected = QueryEvaluationException.class)
    public void testFilterOnUnboundVariable() {
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
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/1", "http://person.example.org/person/5"));
    }

    @Test
    public void testGraphPatternFilterOrder() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v ?lname WHERE {\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?v :firstName ?fname }\n" +
                "    ?v :lastName ?lname\n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of("http://person.example.org/person/3"));
    }

    @Test
    public void testNotExistsSharedVariables() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?person :nickname ?sharedName }\n" +
                "    ?v :firstName ?sharedName\n" +
                "} ORDER BY ?v\n";
        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/5"));
    }

    @Test
    public void testExistsSharedVariables() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "    ?v a :Person .\n" +
                "    FILTER EXISTS { ?person :nickname ?sharedName }\n" +
                "    ?v :firstName ?sharedName\n" +
                "} ORDER BY ?v\n";
        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/2"));
    }

    @Test
    public void testBindExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?s a :Person .\n" +
                "   BIND(EXISTS { ?s :firstName ?fname } AS ?v)\n" +
                "} ORDER BY ?s \n";

        runQueryAndCompare(sparql, ImmutableList.of("true", "true", "false", "false", "true", "false"));
    }

    @Test
    public void testBindIfExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?s a :Person .\n" +
                "   BIND(IF(EXISTS { ?s :firstName ?fname }, \"true\"^^xsd:boolean, \"false\"^^xsd:boolean) AS ?v)\n" +
                "} ORDER BY ?s \n";

        runQueryAndCompare(sparql, ImmutableList.of("true", "true", "false", "false", "true", "false"));
    }

    @Test
    public void testBindExistsCardinality() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?s a :Person .\n" +
                "   BIND(EXISTS { ?s :nickname ?nick } AS ?v)\n" +
                "} ORDER BY ?s \n";

        Assert.assertEquals(6, runQueryAndCount(sparql));
    }

    @Test
    public void testMultipleBindExists() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   BIND(EXISTS { ?v :firstName ?fname } AS ?hasFirstName)\n" +
                "   BIND(EXISTS { ?v :lastName ?lname } AS ?hasLastName)\n" +
                "   FILTER(?hasFirstName = false && ?hasLastName = false)\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/4", "http://person.example.org/person/6"));
    }

    @Test
    public void testExistsWithCoalesce() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "   FILTER(COALESCE(EXISTS { ?v :firstName ?fname }, false))\n" +
                "} ORDER BY ?v \n";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/2",
                "http://person.example.org/person/5"));
    }

    // exists inside aggregation not supported
    @Test(expected = QueryEvaluationException.class)
    public void testExistsWithAggregation() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?person (SUM(IF(EXISTS { ?person :firstName ?fname . } , 1, 0)) AS ?v)\n" +
                "WHERE {\n" +
                "  ?person a :Person .\n" +
                "}\n" +
                "GROUP BY ?person \n";

        runQueryAndCompare(sparql, ImmutableList.of("1", "1", "0", "0", "1", "0"));
    }

    // exists inside aggregation not yet supported
    @Test(expected = QueryEvaluationException.class)
    public void testExistsWithAggregationNoSharedVars() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?person (SUM(IF(EXISTS { ?s :firstName ?fname . } , 1, 0)) AS ?v)\n" +
                "WHERE {\n" +
                "  ?person a :Person .\n" +
                "}\n" +
                "GROUP BY ?person \n";

        runQueryAndCompare(sparql, ImmutableList.of("1", "1", "1", "1", "1", "1"));
    }

    // exists inside order by not yet supported
    @Test(expected = QueryEvaluationException.class)
    public void testExistsWithOrderBy() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "} ORDER BY DESC(EXISTS { ?v :firstName ?fname })";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/2", "http://person.example.org/person/5",
                "http://person.example.org/person/3", "http://person.example.org/person/4", "http://person.example.org/person/6"));
    }

    // exists inside order by not yet supported
    @Test(expected = QueryEvaluationException.class)
    public void testExistsWithOrderByNoSharedVars() {
        String sparql = "PREFIX  : <http://person.example.org/> \n" +
                "SELECT ?v WHERE {\n" +
                "   ?v a :Person .\n" +
                "} ORDER BY DESC(EXISTS { ?s :firstName ?fname })";

        runQueryAndCompare(sparql, ImmutableList.of("http://person.example.org/person/1", "http://person.example.org/person/2", "http://person.example.org/person/3",
                "http://person.example.org/person/4", "http://person.example.org/person/5", "http://person.example.org/person/6"));
    }

    // Not yet supported
    @Test(expected = QueryEvaluationException.class)
    public void testNullableOrTheRight() {
        String sparql = "PREFIX : <http://person.example.org/>\n" +
                "SELECT ?v WHERE {\n" +
                "    ?v a :Person .\n" +
                "    FILTER NOT EXISTS { ?person a :Person . OPTIONAL { ?person :locality ?sharedName } }\n" +
                "    ?v :nickname ?sharedName\n" +
                "}\n";
        runQueryAndCompare(sparql, ImmutableSet.of());
    }
}
