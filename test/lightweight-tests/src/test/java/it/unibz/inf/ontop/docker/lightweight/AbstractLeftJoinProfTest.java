package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractLeftJoinProfTest extends AbstractDockerRDF4JTest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLeftJoinProfTest.class);

    protected static final String OWL_FILE = "/prof/prof.owl";
    protected static final String OBDA_FILE = "/prof/prof.obda";


    @Test
    public void testMinusNickname() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        runQueryAndCompare(query, ImmutableList.of("Barbara", "Diego", "Johann", "Mary"));
    }

    @Test
    public void testMinus2() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        runQueryAndCompare(query, ImmutableList.of("Barbara", "Johann", "Mary"));
    }

    @Test
    public void testMinusLastname() {

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

        runQueryAndCompare(query, ImmutableList.of());
    }


    @Test
    public void testSimpleFirstName() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :firstName ?v\n" +
                "  }\n" +
                "}";

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        runQueryAndCompare(query, ImmutableList.of("Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara",
                "Mary"));
    }

    @Test
    public void testRequiredTeacherNickname() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        runQueryAndCompare(query, ImmutableList.of("Johnny", "Rog"));
    }

    @Test
    public void testFullName1() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        runQueryAndCompare(query, ImmutableList.of("Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara",
                "Mary"));
    }

    @Test
    public void testFullName2() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        runQueryAndCompare(query, ImmutableList.of("Roger", "Frank", "John", "Michael", "Diego", "Johann", "Barbara",
                "Mary"));
    }

    @Test
    public void testFirstNameNickname() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Roger", "Frank", "John", "Michael"));
    }

    @Test
    public void testSimpleNickname() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "   ?p a :Professor .\n" +
                "   OPTIONAL {\n" +
                "     ?p :nickname ?v\n" +
                "  }\n" +
                "}";

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation.toLowerCase(), "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        runQueryAndCompare(query, ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop"));
    }

    @Test
    public void testNicknameAndCourse() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(containsMoreThanOneOccurrence(ontopSQLtranslation.toLowerCase(), "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        runQueryAndCompare(query, getExpectedValuesNicknameAndCourse());
    }

    protected ImmutableList<String> getExpectedValuesNicknameAndCourse() {
        return ImmutableList.of("Rog", "Rog", "Rog", "Johnny");
    }

    @Test
    public void testCourseTeacherName() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Smith", "Poppins", "Depp"));
    }

    @Test
    public void testCourseJoinOnLeft1() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Smith", "Poppins", "Depp"));
    }

    @Test
    public void testCourseJoinOnLeft2() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("John", "Mary", "Roger"));
    }

    @Test
    public void testNotEqOrUnboundCondition() {

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

        runQueryAndCompare(query, ImmutableList.of("John", "Mary"));
    }

    @Test
    public void testPreferences() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Dodero", "Frankie", "Gamper", "Helmer", "Johnny", "King of Pop",
                "Poppins", "Rog"));
    }

    @Test
    public void testUselessRightPart2() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Depp", "Dodero", "Gamper", "Helmer", "Jackson", "Pitt", "Poppins",
                "Smith"));
    }

    @Test
    public void testOptionalTeachesAt() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Depp", "Poppins", "Smith"));
    }

    @Test
    public void testOptionalTeacherID() {

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

        String ontopSQLtranslation = reformulate(query);

        LOGGER.debug("SQL Query: \n" + ontopSQLtranslation);

        Assertions.assertFalse(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, ImmutableList.of("Depp", "Poppins", "Smith"));
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

        runQueryAndCompare(query, getExpectedValueSumStudents1());
    }

    protected ImmutableList<String> getExpectedValueSumStudents1() {
        return ImmutableList.of("56");
    }

    @Test
    public void testSumStudents2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, getExpectedValueSumStudents2());
    }

    protected ImmutableList<String> getExpectedValueSumStudents2() {
        return ImmutableList.of("12", "13", "31");
    }

    @Test
    public void testSumStudents3() {

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

        runQueryAndCompare(query, getExpectedValueSumStudents3());
    }

    protected ImmutableList<String> getExpectedValueSumStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "12", "13", "31");
    }

    @Test
    public void testSumStudents4() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(?s)) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        runQueryAndCompare(query, getExpectedValueSumStudents4());
    }

    protected ImmutableList<String> getExpectedValueSumStudents4() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 31");
    }

    @Test
    public void testSumStudents5() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SUM(?nb) AS ?s) (CONCAT(?fName, \": \", str(SUM(?nb))) AS ?v) \n" +
                "WHERE {\n" +
                "   ?p :teaches ?c ; :firstName ?fName .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?s";

        runQueryAndCompare(query, getExpectedValueSumStudents5());
    }

    protected ImmutableList<String> getExpectedValueSumStudents5() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 31");
    }

    @Test
    public void testDistinctAsGroupBy1() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "\n" +
                "SELECT (CONCAT(?fName, \".\") AS ?v) ((1+1) AS ?y) \n" +
                "WHERE {\n" +
                "   ?p :firstName ?fName .\n" +
                "}\n" +
                "GROUP BY ?p ?fName \n" +
                "ORDER BY ?fName";

        runQueryAndCompare(query, ImmutableList.of("Barbara.", "Diego.", "Frank.", "Johann.", "John.", "Mary.",
                "Michael.", "Roger."));
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

        runQueryAndCompare(query, getExpectedValuesAvgStudents1());
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.2");
    }

    @Test
    public void testAvgStudents2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (AVG(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, getExpectedValuesAvgStudents2());
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("10.3","12", "13");
    }


    @Test
    public void testAvgStudents3() {

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

        runQueryAndCompare(query, getExpectedValuesAvgStudents3());
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

        runQueryAndCompare(query, ImmutableList.of("10"));
    }

    @Test
    public void testMinStudents2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MIN(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("10","12", "13"));
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

        runQueryAndCompare(query, ImmutableList.of("13"));
    }

    @Test
    public void testMaxStudents2() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (MAX(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?v";

        runQueryAndCompare(query, ImmutableList.of("11","12", "13"));
    }

    @Test
    public void testDuration1() {

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

        runQueryAndCompare(query, getExpectedValuesDuration1());
    }

    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18", "20", "84.5");
    }

    @Test
    public void testMultitypedSum1() {

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

        runQueryAndCompare(query, getExpectedValuesMultitypedSum1());
    }

    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31", "32", "115.5");
    }

    @Test
    public void testMultitypedAvg1() {

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

        runQueryAndCompare(query, getExpectedValuesMultitypedAvg1());
    }

    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5", "16", "19.25");
    }

    @Test
    public void testMinusMultitypedSum() {

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

        runQueryAndCompare(query, ImmutableList.of("Dodero", "Gamper", "Helmer", "Jackson", "Pitt"));
    }

    /**
     * Checks that the type error is detected
     */
    @Test
    public void testMinusMultitypedAvg() {

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

        runQueryAndCompare(query, ImmutableList.of("Dodero", "Gamper", "Helmer", "Jackson", "Pitt"));
    }

    /**
     * Tests that the FILTER is not lifted above the query modifiers
     */
    @Test
    public void testLimitSubQuery1() {
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

        runQueryAndCompare(query, ImmutableList.of("Depp"));
    }

    @Test
    public void testSumOverNull1() {

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

        runQueryAndCompare(query, ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void testAvgOverNull1() {

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

        runQueryAndCompare(query, ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void testCountOverNull1() {

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

        runQueryAndCompare(query, ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void testMinOverNull1() {

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

        runQueryAndCompare(query, ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void testMaxOverNull1() {

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

        runQueryAndCompare(query, ImmutableList.of("0", "0", "0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void testGroupConcat1() {

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

        runQueryAndCompare(query, ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", ""));
    }

    @Test
    public void testGroupConcat2() {

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

        runQueryAndCompare(query, ImmutableList.of("Rog Rog", "Frankie Frankie", "Johnny Johnny",
                "King of Pop King of Pop", "", "", "", ""));
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

        runQueryAndCompare(query, ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", ""));
    }

    @Test
    public void testGroupConcat4() {

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

        runQueryAndCompare(query, ImmutableList.of("Rog|Rog", "Frankie|Frankie", "Johnny|Johnny",
                "King of Pop|King of Pop", "", "", "", ""));
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

        runQueryAndCompare(query, ImmutableList.of("Rog", "Frankie", "Johnny", "King of Pop", "", "", "", ""));
    }

    @Test
    public void testGroupConcat6() {

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

        runQueryAndCompare(query, ImmutableList.of("nothing", "Frankie", "nothing", "King of Pop", "", "", "",
                "nothing"));
    }

    @Test
    public void testProperties() {

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

        runQueryAndCompare(query,
                ImmutableList.of("http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nbStudents",
                        "http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nickname"));
    }

    @Test
    public void testNonOptimizableLJAndJoinMix() {

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

        String ontopSQLtranslation = reformulate(query);

        Assertions.assertTrue(ontopSQLtranslation.toUpperCase().contains("LEFT"));
        runQueryAndCompare(query, getExpectedValuesNonOptimizableLJAndJoinMix());
    }

    protected ImmutableList<String> getExpectedValuesNonOptimizableLJAndJoinMix() {
        return ImmutableList.of("Depp", "Poppins", "Smith", "Smith", "Smith");
    }

    @Test
    public void testValuesNodeOntologyProperty() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?p a :Teacher ;\n" +
                "   :fullname ?v ;\n" +
                "   :conductsLab ?c .\n" +
                "}\n" ;

        runQueryAndCompare (query, ImmutableList.of("Jane Smith", "Joe Logan"));
    }

    @Test
    public void testAggregationMappingProfStudentCountProperty() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?v\n" +
                "WHERE {\n" +
                "?p a :Teacher ;\n" +
                "   :nbStudents ?v .\n" +
                "}" +
                "ORDER BY ?v\n" ;

        runQueryAndCompare(query, getExpectedAggregationMappingProfStudentCountPropertyResults());
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
