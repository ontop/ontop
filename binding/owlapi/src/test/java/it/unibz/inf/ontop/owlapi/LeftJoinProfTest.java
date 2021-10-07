package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LeftJoinProfTest extends AbstractOWLAPITest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";

    @BeforeClass
    public static void setUp() throws Exception {
        initOBDA("/test/redundant_join/redundant_join_fk_create.sql",
                "/test/redundant_join/redundant_join_fk_test.obda",
                "/test/redundant_join/redundant_join_fk_test.owl",
                "/test/redundant_join/redundant_join_fk_test.properties");

    }

    @AfterClass
    public static void tearDown() throws Exception {
        release();
    }

    @Test
    public void testSimpleFirstName() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v\n" +
                "  }\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Roger\"^^xsd:string",
                "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string",
                "\"Michael\"^^xsd:string",
                "\"Diego\"^^xsd:string",
                "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string",
                "\"Mary\"^^xsd:string"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testFullName1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :lastName ?lastName .\n" +
                "  }\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Roger\"^^xsd:string",
                "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string",
                "\"Michael\"^^xsd:string",
                "\"Diego\"^^xsd:string",
                "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string",
                "\"Mary\"^^xsd:string"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testFullName2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v .\n" +
                "   }\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?lastName .\n" +
                "   }\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Roger\"^^xsd:string",
                "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string",
                "\"Michael\"^^xsd:string",
                "\"Diego\"^^xsd:string",
                "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string",
                "\"Mary\"^^xsd:string"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testFirstNameNickname() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :nickname ?nickname .\n" +
                "  }\n" +
                "} ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "UNBOUND",
                "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string",
                "\"Michael\"^^xsd:string",
                "\"Roger\"^^xsd:string"));

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testRequiredTeacherNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v; \n" +
                "        :teaches ?c ." +
                "  }\n" +
                "  FILTER (bound(?v))\n" +
                "}\n"
                + "ORDER BY ?v\n";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Johnny\"^^xsd:string",
                "\"Rog\"^^xsd:string"));

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testMinusNickname() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v .\n" +
                "   OPTIONAL {\n" +
                "      ?p :nickname ?nickname .\n" +
                "  }\n" +
                " FILTER (!bound(?nickname)) \n" +
                "} ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Barbara\"^^xsd:string",
                "\"Diego\"^^xsd:string",
                "\"Johann\"^^xsd:string",
                "\"Mary\"^^xsd:string"));

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testMinus2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v ; :lastName ?l . \n" +
                "   FILTER(contains(?v, \"a\"))\n" +
                "   OPTIONAL {\n" +
                "      ?p :nickname ?nickname .\n" +
                "      BIND(true AS ?w) \n" +
                "  }\n" +
                " FILTER (!bound(?w)) \n" +
                "} ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Barbara\"^^xsd:string",
                "\"Johann\"^^xsd:string",
                "\"Mary\"^^xsd:string"));

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testMinusLastname() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v .\n" +
                "   OPTIONAL {\n" +
                "      ?p :lastName ?n .\n" +
                "  }\n" +
                " FILTER (!bound(?n)) \n" +
                "} ORDER BY ?v";

        checkReturnedValues(query, "v", ImmutableList.of());
    }

    @Test
    public void testSimpleNickname() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "  }\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Rog\"^^xsd:string",
                "\"Frankie\"^^xsd:string",
                "\"Johnny\"^^xsd:string",
                "\"King of Pop\"^^xsd:string",
                "UNBOUND",
                "UNBOUND",
                "UNBOUND",
                "UNBOUND"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql.toLowerCase(), "\"professors\""));
    }

    @Test
    public void testNicknameAndCourse() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v ?f\n" +
                "WHERE {\n" +
                "   ?p a :Professor ;\n" +
                "      :firstName ?f ;\n" +
                "      :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY DESC(?v)\n";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Rog\"^^xsd:string",
                "\"Rog\"^^xsd:string",
                "\"Johnny\"^^xsd:string",
                "UNBOUND"));

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql.toLowerCase(), "\"professors\""));
    }

    @Test
    public void testCourseTeacherName() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY DESC(?v)";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Smith\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Depp\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testCourseJoinOnLeft1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?f ; \n" +
                "      :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "FILTER (bound(?f))\n" +
                "}\n" +
                "ORDER BY DESC(?v)";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Smith\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Depp\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testCourseJoinOnLeft2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v ; \n" +
                "      :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"John\"^^xsd:string",
                "\"Mary\"^^xsd:string",
                "\"Roger\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testNotEqOrUnboundCondition() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v . \n" +
                "   ?p :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?n\n" +
                "  }\n" +
                "  FILTER ((?n != \"Rog\") || !bound(?n))\n" +
                "}" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"John\"^^xsd:string",
                "\"Mary\"^^xsd:string"));
    }

    @Test
    public void testPreferences() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     ?p :nickname ?v .\n" +
                "   }\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Dodero\"^^xsd:string",
                "\"Frankie\"^^xsd:string",
                "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string",
                "\"Johnny\"^^xsd:string",
                "\"King of Pop\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Rog\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testUselessRightPart2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     ?p :lastName ?v .\n" +
                "   }\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Depp\"^^xsd:string",
                "\"Dodero\"^^xsd:string",
                "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string",
                "\"Jackson\"^^xsd:string",
                "\"Pitt\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Smith\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testOptionalTeachesAt() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor ; \n" +
                "        :lastName ?v .\n" +
                "   OPTIONAL { \n" +
                "     ?p :teachesAt ?u .\n" +
                "   }\n" +
                "   FILTER (bound(?u))\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Depp\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Smith\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testOptionalTeacherID() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor ; \n" +
                "        :lastName ?v .\n" +
                "   OPTIONAL { \n" +
                "     ?p :teacherID ?id .\n" +
                "   }\n" +
                "   FILTER (bound(?id))\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Depp\"^^xsd:string",
                "\"Poppins\"^^xsd:string",
                "\"Smith\"^^xsd:string"));

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testSumStudents1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"46\"^^xsd:integer"));
    }

    @Test
    public void testSumStudents2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"12\"^^xsd:integer",
                "\"13\"^^xsd:integer",
                "\"21\"^^xsd:integer"));
    }

    @Test
    public void testSumStudents3() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :teaches ?c .\n" +
                "      ?c :nbStudents ?nb .\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"13\"^^xsd:integer",
                "\"21\"^^xsd:integer"));
    }

    @Test
    public void testSumStudents4() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(?s)) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"John: 12\"^^xsd:string",
                "\"Mary: 13\"^^xsd:string",
                "\"Roger: 21\"^^xsd:string"));
    }

    @Test
    public void testSumStudents5() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(SUM(?nb))) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"John: 12\"^^xsd:string",
                "\"Mary: 13\"^^xsd:string",
                "\"Roger: 21\"^^xsd:string"));
    }

    @Test
    public void testDistinctAsGroupBy1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (CONCAT(?fName, \".\") AS ?v) ((1+1) AS ?y) \n" +
                "WHERE {\n" +
                "   ?p :firstName ?fName .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?fName";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Barbara.\"^^xsd:string",
                "\"Diego.\"^^xsd:string",
                "\"Frank.\"^^xsd:string",
                "\"Johann.\"^^xsd:string",
                "\"John.\"^^xsd:string",
                "\"Mary.\"^^xsd:string",
                "\"Michael.\"^^xsd:string",
                "\"Roger.\"^^xsd:string"));
    }

    @Test
    public void testAvgStudents1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"11.5\"^^xsd:decimal"));
    }

    @Test
    public void testAvgStudents2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"10.5\"^^xsd:decimal",
                "\"12\"^^xsd:decimal",
                "\"13\"^^xsd:decimal"));
    }

    @Test
    public void testAvgStudents3() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :teaches ?c .\n" +
                "      ?c :nbStudents ?nb .\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"10.5\"^^xsd:decimal",
                "\"12\"^^xsd:decimal",
                "\"13\"^^xsd:decimal"));
    }

    @Test
    public void testMinStudents1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (MIN(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"10\"^^xsd:integer"));
    }

    @Test
    public void testMinStudents2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (MIN(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"10\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"13\"^^xsd:integer"));
    }

    @Test
    public void testMaxStudents1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT (MAX(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"13\"^^xsd:integer"));
    }

    @Test
    public void testMaxStudents2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (MAX(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"11\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"13\"^^xsd:integer"));
    }

    @Test
    public void testDuration1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?d) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :teaches ?c .\n" +
                "      ?c :duration ?d .\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"18\"^^xsd:decimal",
                "\"20\"^^xsd:decimal",
                "\"54.5\"^^xsd:decimal"));
    }

    @Test
    public void testMultitypedSum1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   { ?c :duration ?n } \n" +
                "   UNION" +
                "   { ?c :nbStudents ?n }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"31\"^^xsd:decimal",
                "\"32\"^^xsd:decimal",
                "\"75.5\"^^xsd:decimal"));
    }

    @Test
    public void testMultitypedAvg1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (AVG(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   { ?c :duration ?n } \n" +
                "   UNION" +
                "   { ?c :nbStudents ?n }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"15.5\"^^xsd:decimal",
                "\"16\"^^xsd:decimal",
                "\"18.875\"^^xsd:decimal"));
    }

    /**
     * Checks that the type error is detected
     */
    @Test
    public void testMinusMultitypedSum() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor ;\n" +
                "        :lastName ?v .\n" +
                "   MINUS {\n " +
                "     SELECT ?p (SUM(?n) AS ?v){\n" +
                "       { \n" +
                "          ?p :teaches ?c .\n" +
                "          ?c :duration ?n " +
                "       } \n" +
                "       UNION" +
                "       { \n" +
                "          ?p :teaches ?c .\n" +
                "          ?p :lastName ?n " +
                "       }\n" +
                "     } GROUP BY ?p\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Dodero\"^^xsd:string",
                "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string",
                "\"Jackson\"^^xsd:string",
                "\"Pitt\"^^xsd:string"));
    }

    /**
     * Checks that the type error is detected
     */
    @Test
    public void testMinusMultitypedAvg() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor ;\n" +
                "        :lastName ?v .\n" +
                "   MINUS {\n " +
                "     SELECT ?p (AVG(?n) AS ?v){\n" +
                "       { \n" +
                "          ?p :teaches ?c .\n" +
                "          ?c :duration ?n " +
                "       } \n" +
                "       UNION" +
                "       { \n" +
                "          ?p :teaches ?c .\n" +
                "          ?p :lastName ?n " +
                "       }\n" +
                "     } GROUP BY ?p\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Dodero\"^^xsd:string",
                "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string",
                "\"Jackson\"^^xsd:string",
                "\"Pitt\"^^xsd:string"));
    }

    /**
     * Tests that the FILTER is not lifted above the query modifiers
     */
    @Test
    public void testLimitSubQuery1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v {\n" +
                "  ?p a :Professor; :lastName ?v .\n" +
                "  {\n" +
                "   SELECT ?p {\n" +
                "     ?p :teaches [ :duration ?d ]\n" +
                "     FILTER ((?d < 21) && (?d > 19))\n" +
                "    }\n" +
                "   ORDER BY ?d\n" +
                "   LIMIT 1\n" +
                "  }\n" +
                "}";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"Depp\"^^xsd:string"));
    }

    @Test
    public void testSumOverNull1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
   }

    @Test
    public void testAvgOverNull1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testCountOverNull1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (COUNT(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testMinOverNull1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (MIN(?nb) AS ?m) (0 AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testMaxOverNull1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (MAX(?nb) AS ?m) (0 AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testSumPreferences1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?s) ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "   OPTIONAL { \n" +
                "       ?p :nickname \"Rog\". \n" +
                "       BIND (\"A\" AS ?v)\n" +
                "   }\n" +
                "   OPTIONAL { \n" +
                "       ?p :firstName \"Mary\". \n" +
                "       BIND (\"B\" AS ?v)\n" +
                "   }\n" +
                "   OPTIONAL { \n" +
                "       ?p :firstName \"John\". \n" +
                "       BIND (\"C\" AS ?v)\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p ?v\n" +
                "ORDER BY ?s";

        String sql = checkReturnedValuesAndReturnSql(query, "v",ImmutableList.of(
                "\"C\"^^xsd:string",
                "\"B\"^^xsd:string",
                "\"A\"^^xsd:string"));
    }

    @Test
    public void testSumPreferences2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?s) ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "   OPTIONAL { \n" +
                "       ?p :nickname \"Rog\". \n" +
                "       BIND (\"A\"@en AS ?v)\n" +
                "   }\n" +
                "   OPTIONAL { \n" +
                "       ?p :firstName \"Mary\". \n" +
                "       BIND (\"B\" AS ?v)\n" +
                "   }\n" +
                "   OPTIONAL { \n" +
                "       ?p :firstName \"John\". \n" +
                "       BIND (\"C\" AS ?v)\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p ?v\n" +
                "ORDER BY ?s";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"C\"^^xsd:string",
                "\"B\"^^xsd:string",
                "\"A\"@en"));
    }

    @Test
    public void testSumPreferences3() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (SUM(?nb) AS ?s) ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?fn ; :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "   FILTER (?fn != \"John\")\n" +
                "   OPTIONAL { \n" +
                "       ?p :nickname \"Rog\". \n" +
                "       BIND (\"A\"@en AS ?v)\n" +
                "   }\n" +
                "   OPTIONAL { \n" +
                "       ?p :firstName \"Mary\". \n" +
                "       BIND (\"B\" AS ?v)\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p ?v\n" +
                "ORDER BY ?s";

        String sql = checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"B\"^^xsd:string",
                "\"A\"@en"));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (GROUP_CONCAT(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     ?p :nickname ?n .\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "Rog",
                "Frankie",
                "Johnny",
                "King of Pop",
                "",
                "",
                "",
                ""));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat2() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (GROUP_CONCAT(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     { ?p :nickname ?n }\n" +
                "     UNION \n" +
                "     { ?p :nickname ?n }\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "Rog Rog",
                "Frankie Frankie",
                "Johnny Johnny",
                "King of Pop King of Pop",
                "",
                "",
                "",
                ""));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat3() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (GROUP_CONCAT(DISTINCT ?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     { ?p :nickname ?n }\n" +
                "     UNION \n" +
                "     { ?p :nickname ?n }\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "Rog",
                "Frankie",
                "Johnny",
                "King of Pop",
                "",
                "",
                "",
                ""));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat4() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (GROUP_CONCAT(?n ; separator='|') AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     { ?p :nickname ?n }\n" +
                "     UNION \n" +
                "     { ?p :nickname ?n }\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "Rog|Rog",
                "Frankie|Frankie",
                "Johnny|Johnny",
                "King of Pop|King of Pop",
                "",
                "",
                "",
                ""));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat5() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (GROUP_CONCAT(DISTINCT ?n ; separator='|') AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     { ?p :nickname ?n }\n" +
                "     UNION \n" +
                "     { ?p :nickname ?n }\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "Rog",
                "Frankie",
                "Johnny",
                "King of Pop",
                "",
                "",
                "",
                ""));
    }

    @Ignore("ignored due to a bug in H2: org.h2.jdbc.JdbcSQLException: Function \"LISTAGG\" not found ")
    @Test
    public void testGroupConcat6() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?p (COALESCE(GROUP_CONCAT(?n),'nothing') AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     { ?p :nickname ?n }\n" +
                "     UNION \n" +
                "     { ?p :teaches ?c .\n" +
                "       ?c :nbStudents ?n }\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "nothing",
                "Frankie",
                "nothing",
                "King of Pop",
                "",
                "",
                "",
                "nothing"));
    }

    @Test
    public void testProperties() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   { [] ?p1 \"Frankie\"  }\n" +
                "    UNION \n" +
                "   { [] ?p2 10 }\n" +
                "   BIND(str(coalesce(?p1, ?p2)) AS ?v)" +
                "}\n" +
                "ORDER BY ?v\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nbStudents\"^^xsd:string",
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nickname\"^^xsd:string"));
    }

    @Test
    public void testCommonNonProjectedVariable() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   { SELECT ?c { ?c  :duration ?o  } }\n" +
                "   { SELECT ?c { ?o :teaches ?c } }\n" +
                "   BIND(str(?c) AS ?v)" +
                "}\n" +
                "ORDER BY ?v\n";

        checkReturnedValuesAndReturnSql(query, "v", ImmutableList.of(
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/AdvancedDatabases\"^^xsd:string",
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/DiscreteMathematics\"^^xsd:string",
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/LinearAlgebra\"^^xsd:string",
                "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/ScientificWriting\"^^xsd:string"));
    }

    @Test
    public void testNonOptimizableLJAndJoinMix() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p1 ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v .\n" +
                "     ?p1 :lastName ?v .\n" +
                "  }\n" +
                "}" +
                "ORDER BY ?v";

        String [] expectedValues = {
                "\"Depp\"^^xsd:string", "\"Poppins\"^^xsd:string", "\"Smith\"^^xsd:string", "\"Smith\"^^xsd:string"};
        String sql = checkReturnedValuesAndReturnSql(query, "v", Arrays.asList(expectedValues));

        assertTrue(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testValuesNodeOntologyProperty() throws Exception {
        String querySelect = "SELECT ?o WHERE {\n" +
                "?s <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#conductsLab> ?o\n" +
                "}";

        checkReturnedValues(querySelect, "o", ImmutableList.of(
                // From the facts
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/Algorithms>",
                "<http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#course/DataIntegration>"));

    }

    @Test
    public void testAggregationMappingProfStudentCountProperty() throws Exception {

        String querySelect =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?p a :Teacher ;\n" +
                "   :nbStudents ?v .\n" +
                "}" +
                "ORDER BY ?v\n" ;

        ImmutableList<String> expectedValues = ImmutableList.of("\"12\"^^xsd:integer", "\"13\"^^xsd:integer", "\"21\"^^xsd:integer");
        checkReturnedValues(querySelect, "v", expectedValues);
    }

    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }
}
