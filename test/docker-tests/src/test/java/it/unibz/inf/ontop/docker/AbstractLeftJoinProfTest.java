package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractLeftJoinProfTest extends AbstractVirtualModeTest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLeftJoinProfTest.class);

    @Test
    public void testMinusNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v .\n" +
                "   OPTIONAL {\n" +
                "      ?p :nickname ?nickname .\n" +
                "  }\n" +
                " FILTER (!bound(?nickname)) \n" +
                "} ORDER BY ?v";

        List<String> expectedValues = Lists.newArrayList(
                "Barbara", "Diego", "Johann", "Mary"
        );
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testMinus2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of(
                "Barbara", "Johann", "Mary"
        );
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testMinusLastname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v .\n" +
                "   OPTIONAL {\n" +
                "      ?p :lastName ?n .\n" +
                "  }\n" +
                " FILTER (!bound(?n)) \n" +
                "} ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of();
        checkReturnedValuesAndOrder(expectedValues, query);
    }


    @Test
    public void testSimpleFirstName() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v\n" +
                "  }\n" +
                "}";


        String [] expectedValues = {"Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"};
        String sql = checkReturnedValuesUnorderedReturnSql(query, Arrays.asList(expectedValues));

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
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

        List<String> expectedValues = Lists.newArrayList(
                "Johnny", "Rog"
        );
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
        assertFalse(LEFT_JOIN_NOT_OPTIMIZED_MSG, sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testFullName1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :lastName ?lastName .\n" +
                "  }\n" +
                "}";

        String [] expectedValues = {"Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"};
        String sql = checkReturnedValuesUnorderedReturnSql(query, Arrays.asList(expectedValues));

        LOGGER.debug("SQL Query: \n" + sql);

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

        String [] expectedValues = {"Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara", "Mary"};
        String sql = checkReturnedValuesUnorderedReturnSql(query, Arrays.asList(expectedValues));

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
    }

    @Test
    public void testFirstNameNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v ;\n" +
                "          :nickname ?nickname .\n" +
                "  }\n" +
                "}";

        String [] expectedValues = {
                "Roger", "Frank", "John", "Michael"};
        String sql = checkReturnedValuesUnorderedReturnSql(query, Arrays.asList(expectedValues));

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testSimpleNickname() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "  }\n" +
                "}";

        String [] expectedValues = {
                "Rog", "Frankie", "Johnny", "King of Pop"
        };
        String sql = checkReturnedValuesUnorderedReturnSql(query, Arrays.asList(expectedValues));


        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql.toLowerCase(), "\"professors\""));
    }

    @Test
    public void testNicknameAndCourse() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v ?f\n" +
                "WHERE {\n" +
                "   ?p a :Professor ;\n" +
                "      :firstName ?f ;\n" +
                "      :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "    }\n" +
                " }\n" +
                "ORDER BY DESC(?v)";

        List<String> expectedValues = getExpectedValuesNicknameAndCourse();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql.toLowerCase(), "\"professors\""));
    }

    protected List<String> getExpectedValuesNicknameAndCourse() {
        return ImmutableList.of("Rog", "Rog", "Rog", "Johnny");
    }

    @Test
    public void testCourseTeacherName() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY DESC(?v)";

        String [] expectedValues = {
                "Smith", "Poppins", "Depp"
        };
        String sql = checkReturnedValuesAndOrderReturnSql(query, Arrays.asList(expectedValues));


        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testCourseJoinOnLeft1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of(
                "Smith", "Poppins", "Depp"
        );
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testCourseJoinOnLeft2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   ?p :firstName ?v ; \n" +
                "      :teaches ?c .\n" +
                "   OPTIONAL {\n" +
                "     ?p :lastName ?v\n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";

        String [] expectedValues = {
                "John", "Mary", "Roger"
        };
        String sql = checkReturnedValuesAndOrderReturnSql(query, Arrays.asList(expectedValues));

        LOGGER.debug("SQL Query: \n" + sql);

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

        List<String> expectedValues = Lists.newArrayList(
                "John", "Mary"
        );
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testPreferences() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of(
                "Dodero", "Frankie", "Gamper", "Helmer", "Johnny", "King of Pop", "Poppins", "Rog");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testUselessRightPart2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        String [] expectedValues = {
                "Depp", "Dodero", "Gamper", "Helmer", "Jackson", "Pitt", "Poppins", "Smith"};

        String sql = checkReturnedValuesAndOrderReturnSql(query, Arrays.asList(expectedValues));
        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testOptionalTeachesAt() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = Arrays.asList("Depp", "Poppins", "Smith");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testOptionalTeacherID() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v ?id\n" +
                "WHERE {\n" +
                "   ?p a :Professor ; \n" +
                "        :lastName ?v .\n" +
                "   OPTIONAL { \n" +
                "     ?p :teacherID ?id .\n" +
                "   }\n" +
                "   FILTER (bound(?id))\n" +
                "}\n" +
                "ORDER BY ?v";

        List<String> expectedValues = Arrays.asList("Depp", "Poppins", "Smith");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Test
    public void testSumStudents1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        List<String> expectedValues = getExpectedValueSumStudents1();
        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected List<String> getExpectedValueSumStudents1() {
        return ImmutableList.of("56");
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

        List<String> expectedValues = getExpectedValueSumStudents2();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected List<String> getExpectedValueSumStudents2() {
        return ImmutableList.of("12", "13", "31");
    }

    @Test
    public void testSumStudents3() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = getExpectedValueSumStudents3();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected List<String> getExpectedValueSumStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "12", "13", "31");
    }

    @Test
    public void testSumStudents4() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(?s)) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        List<String> expectedValues = getExpectedValueSumStudents4();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected List<String> getExpectedValueSumStudents4() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 31");
    }

    @Test
    public void testSumStudents5() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(SUM(?nb))) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        List<String> expectedValues = getExpectedValueSumStudents5();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected List<String> getExpectedValueSumStudents5() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 31");
    }

    @Test
    public void testDistinctAsGroupBy1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "\n" +
                "SELECT (CONCAT(?fName, \".\") AS ?v) ((1+1) AS ?y) \n" +
                "WHERE {\n" +
                "   ?p :firstName ?fName .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?fName";

        List<String> expectedValues = ImmutableList.of("Barbara.", "Diego.", "Frank.", "Johann.", "John.", "Mary.",
                "Michael.", "Roger.");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testAvgStudents1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        List<String> expectedValues = getExpectedValuesAvgStudents1();
        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.2");
    }

    @Test
    public void testAvgStudents2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        List<String> expectedValues = getExpectedValuesAvgStudents2();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("10.3","12", "13");
    }


    @Test
    public void testAvgStudents3() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = getExpectedValuesAvgStudents3();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.3", "12", "13");
    }

    @Test
    public void testMinStudents1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT (MIN(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        List<String> expectedValues = ImmutableList.of("10");
        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testMinStudents2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MIN(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("10","12", "13");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testMaxStudents1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT (MAX(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?c a :Course ; \n" +
                "        :nbStudents ?nb .\n" +
                "}\n";

        List<String> expectedValues = ImmutableList.of("13");
        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testMaxStudents2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MAX(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("11","12", "13");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testDuration1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = getExpectedValuesDuration1();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18", "20", "84.5");
    }

    @Test
    public void testMultitypedSum1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   { ?c :duration ?n } \n" +
                "   UNION" +
                "   { ?c :nbStudents ?n }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = getExpectedValuesMultitypedSum1();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31", "32", "115.5");
    }

    @Test
    public void testMultitypedAvg1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (AVG(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   { ?c :duration ?n } \n" +
                "   UNION" +
                "   { ?c :nbStudents ?n }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = getExpectedValuesMultitypedAvg1();
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5", "16", "19.25");
    }


    @Test
    public void testMinusMultitypedSum() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Dodero", "Gamper", "Helmer", "Jackson", "Pitt");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    /**
     * Checks that the type error is detected
     */
    @Test
    public void testMinusMultitypedAvg() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Dodero", "Gamper", "Helmer", "Jackson", "Pitt");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    /**
     * Tests that the FILTER is not lifted above the query modifiers
     */
    @Test
    public void testLimitSubQuery1() throws Exception {
        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Depp");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testSumOverNull1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testAvgOverNull1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testCountOverNull1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (COUNT(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testMinOverNull1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MIN(?nb) AS ?m) (0 AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testMaxOverNull1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MAX(?nb) AS ?m) (0 AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {" +
                "      ?p :nonExistingProperty ?nb\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?v";

        List<String> expectedValues = ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0");
        String sql = checkReturnedValuesAndOrderReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testGroupConcat1() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (GROUP_CONCAT(?n) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p a :Professor . \n" +
                "   OPTIONAL { \n" +
                "     ?p :nickname ?n .\n" +
                "   }\n" +
                "}\n" +
                "GROUP BY ?p\n" +
                "ORDER BY ?p\n";

        List<String> expectedValues = ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", "");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testGroupConcat2() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Rog Rog", "Frankie Frankie", "Johnny Johnny", "King of Pop King of Pop", "", "", "", "");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testGroupConcat3() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", "");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testGroupConcat4() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Rog|Rog", "Frankie|Frankie", "Johnny|Johnny", "King of Pop|King of Pop", "", "", "", "");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testGroupConcat5() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", "");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testGroupConcat6() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
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

        List<String> expectedValues = ImmutableList.of("nothing", "Frankie", "nothing", "King of Pop", "", "", "", "nothing");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
    }

    @Test
    public void testProperties() throws Exception {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT DISTINCT ?v\n" +
                "WHERE {\n" +
                "   { [] ?p1 \"Frankie\"  }\n" +
                "    UNION \n" +
                "   { \n" +
                "     [] ?p2 ?n \n" +
                "     FILTER (?n = 10)" +
                " }\n" +
                "   BIND(str(coalesce(?p1, ?p2)) AS ?v)" +
                "}\n" +
                "ORDER BY ?v\n";

        List<String> expectedValues = ImmutableList.of("http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nbStudents", "http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nickname");
        checkReturnedValuesAndOrderReturnSql(query, expectedValues);
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

        String sql = checkReturnedValuesAndOrderReturnSql(query, getExpectedValuesNonOptimizableLJAndJoinMix());

        assertTrue(sql.toUpperCase().contains("LEFT"));
    }

    protected List<String> getExpectedValuesNonOptimizableLJAndJoinMix() {
        return ImmutableList.of("Depp", "Poppins", "Smith", "Smith", "Smith");
    }

    @Test
    public void testValuesNodeOntologyProperty() throws Exception {

        String querySelect =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?p a :Teacher ;\n" +
                "   :fullname ?v ;\n" +
                "   :conductsLab ?c .\n" +
                "}\n" ;

        List<String> expectedValues = Lists.newArrayList("Jane Smith", "Joe Logan");
        checkReturnedValuesAndOrderReturnSql(querySelect, expectedValues);

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

        ImmutableList<String> expectedValues = getExpectedAggregationMappingProfStudentCountPropertyResults();
        checkReturnedValuesAndOrderReturnSql(querySelect, expectedValues);
    }

    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("12", "13", "31");
    }

    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }

}
