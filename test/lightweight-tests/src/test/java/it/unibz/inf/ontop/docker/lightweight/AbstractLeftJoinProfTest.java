package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public abstract class AbstractLeftJoinProfTest extends AbstractDockerRDF4JTest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";
    private static final String LEFT_JOIN_NOT_OPTIMIZED_MSG = "The left join is still present in the output query";

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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        executeAndCompareValues(query, ImmutableList.of("\"Barbara\"^^xsd:string", "\"Diego\"^^xsd:string",
                "\"Johann\"^^xsd:string", "\"Mary\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        executeAndCompareValues(query, ImmutableList.of("\"Barbara\"^^xsd:string", "\"Johann\"^^xsd:string",
                "\"Mary\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of());
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

        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string", "\"Michael\"^^xsd:string", "\"Diego\"^^xsd:string", "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string", "\"Mary\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"), LEFT_JOIN_NOT_OPTIMIZED_MSG);
        executeAndCompareValues(query, ImmutableList.of("\"Johnny\"^^xsd:string", "\"Rog\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string", "\"Michael\"^^xsd:string", "\"Diego\"^^xsd:string", "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string", "\"Mary\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation, "\"PROFESSORS\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string", "\"Michael\"^^xsd:string", "\"Diego\"^^xsd:string", "\"Johann\"^^xsd:string",
                "\"Barbara\"^^xsd:string", "\"Mary\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableSet.of("\"Roger\"^^xsd:string", "\"Frank\"^^xsd:string",
                "\"John\"^^xsd:string", "\"Michael\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation.toLowerCase(), "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        executeAndCompareValues(query, ImmutableSet.of("\"Rog\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"Johnny\"^^xsd:string", "\"King of Pop\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && containsMoreThanOneOccurrence(ontopSQLtranslation.toLowerCase(), "\"professors\""),
                NO_SELF_LJ_OPTIMIZATION_MSG);
        executeAndCompareValues(query, getExpectedValuesNicknameAndCourse());
    }

    protected ImmutableList<String> getExpectedValuesNicknameAndCourse() {
        return ImmutableList.of("\"Rog\"^^xsd:string", "\"Rog\"^^xsd:string", "\"Rog\"^^xsd:string",
                "\"Johnny\"^^xsd:string");
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Smith\"^^xsd:string", "\"Poppins\"^^xsd:string",
                "\"Depp\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Smith\"^^xsd:string", "\"Poppins\"^^xsd:string",
                "\"Depp\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"John\"^^xsd:string", "\"Mary\"^^xsd:string",
                "\"Roger\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"John\"^^xsd:string", "\"Mary\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Dodero\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"Gamper\"^^xsd:string", "\"Helmer\"^^xsd:string", "\"Johnny\"^^xsd:string",
                "\"King of Pop\"^^xsd:string", "\"Poppins\"^^xsd:string", "\"Rog\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Depp\"^^xsd:string", "\"Dodero\"^^xsd:string",
                "\"Gamper\"^^xsd:string", "\"Helmer\"^^xsd:string", "\"Jackson\"^^xsd:string", "\"Pitt\"^^xsd:string",
                "\"Poppins\"^^xsd:string", "\"Smith\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Depp\"^^xsd:string", "\"Poppins\"^^xsd:string",
                "\"Smith\"^^xsd:string"));
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

        Assertions.assertFalse(supportsIntegrityConstraints() && ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, ImmutableList.of("\"Depp\"^^xsd:string", "\"Poppins\"^^xsd:string",
                "\"Smith\"^^xsd:string"));
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

        executeAndCompareValues(query, getExpectedValueSumStudents1());
    }

    protected ImmutableList<String> getExpectedValueSumStudents1() {
        return ImmutableList.of("\"56\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValueSumStudents2());
    }

    protected ImmutableList<String> getExpectedValueSumStudents2() {
        return ImmutableList.of("\"12\"^^xsd:integer", "\"13\"^^xsd:integer", "\"31\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValueSumStudents3());
    }

    protected ImmutableList<String> getExpectedValueSumStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"12\"^^xsd:integer", "\"13\"^^xsd:integer", "\"31\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValueSumStudents4());
    }

    protected ImmutableList<String> getExpectedValueSumStudents4() {
        return ImmutableList.of("\"John: 12\"^^xsd:string", "\"Mary: 13\"^^xsd:string", "\"Roger: 31\"^^xsd:string");
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

        executeAndCompareValues(query, getExpectedValueSumStudents5());
    }

    protected ImmutableList<String> getExpectedValueSumStudents5() {
        return ImmutableList.of("\"John: 12\"^^xsd:string", "\"Mary: 13\"^^xsd:string", "\"Roger: 31\"^^xsd:string");
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

        executeAndCompareValues(query, ImmutableList.of("\"Barbara.\"^^xsd:string", "\"Diego.\"^^xsd:string",
                "\"Frank.\"^^xsd:string", "\"Johann.\"^^xsd:string", "\"John.\"^^xsd:string", "\"Mary.\"^^xsd:string",
                "\"Michael.\"^^xsd:string", "\"Roger.\"^^xsd:string"));
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

        executeAndCompareValues(query, getExpectedValuesAvgStudents1());
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.2\"^^xsd:decimal");
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

        executeAndCompareValues(query, getExpectedValuesAvgStudents2());
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.3\"^^xsd:decimal","\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
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

        executeAndCompareValues(query, getExpectedValuesAvgStudents3());
    }

    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.3\"^^xsd:decimal", "\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
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

        executeAndCompareValues(query, getExpectedValuesMinStudents1());
    }

    protected ImmutableList<String> getExpectedValuesMinStudents1() {
        return ImmutableList.of("\"10\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValuesMinStudents2());
    }

    protected ImmutableList<String> getExpectedValuesMinStudents2() {
        return ImmutableList.of("\"10\"^^xsd:integer","\"12\"^^xsd:integer", "\"13\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValuesMaxStudents1());
    }

    protected ImmutableList<String> getExpectedValuesMaxStudents1() {
        return ImmutableList.of("\"13\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValuesMaxStudents2());
    }

    protected ImmutableList<String> getExpectedValuesMaxStudents2() {
        return ImmutableList.of("\"11\"^^xsd:integer","\"12\"^^xsd:integer", "\"13\"^^xsd:integer");
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

        executeAndCompareValues(query, getExpectedValuesDuration1());
    }

    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18\"^^xsd:decimal", "\"20\"^^xsd:decimal", "\"84.5\"^^xsd:decimal");
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

        executeAndCompareValues(query, getExpectedValuesMultitypedSum1());
    }

    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31\"^^xsd:decimal", "\"32\"^^xsd:decimal", "\"115.5\"^^xsd:decimal");
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

        executeAndCompareValues(query, getExpectedValuesMultitypedAvg1());
    }

    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.5\"^^xsd:decimal", "\"16\"^^xsd:decimal", "\"19.25\"^^xsd:decimal");
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

        executeAndCompareValues(query, ImmutableList.of("\"Dodero\"^^xsd:string", "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string", "\"Jackson\"^^xsd:string", "\"Pitt\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"Dodero\"^^xsd:string", "\"Gamper\"^^xsd:string",
                "\"Helmer\"^^xsd:string", "\"Jackson\"^^xsd:string", "\"Pitt\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"Depp\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
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

        executeAndCompareValues(query, ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
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

        executeAndCompareValues(query, ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
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

        executeAndCompareValues(query, ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
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

        executeAndCompareValues(query, ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
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

        executeAndCompareValues(query, ImmutableList.of("\"Rog\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"Johnny\"^^xsd:string", "\"King of Pop\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"Rog Rog\"^^xsd:string", "\"Frankie Frankie\"^^xsd:string",
                "\"Johnny Johnny\"^^xsd:string", "\"King of Pop King of Pop\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string"));
    }

    @Test
    public void testGroupConcat3() {

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

        executeAndCompareValues(query, ImmutableList.of("\"Rog\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"Johnny\"^^xsd:string", "\"King of Pop\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"Rog|Rog\"^^xsd:string", "\"Frankie|Frankie\"^^xsd:string",
                "\"Johnny|Johnny\"^^xsd:string", "\"King of Pop|King of Pop\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string"));
    }

    @Test
    public void testGroupConcat5() {

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

        executeAndCompareValues(query, ImmutableList.of("\"Rog\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"Johnny\"^^xsd:string", "\"King of Pop\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string"));
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

        executeAndCompareValues(query, ImmutableList.of("\"nothing\"^^xsd:string", "\"Frankie\"^^xsd:string",
                "\"nothing\"^^xsd:string", "\"King of Pop\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string",
                "\"\"^^xsd:string", "\"nothing\"^^xsd:string"));
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

        executeAndCompareValues(query,
                ImmutableList.of("\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nbStudents\"^^xsd:string",
                        "\"http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#nickname\"^^xsd:string"));
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

        Assertions.assertTrue(!supportsIntegrityConstraints() || ontopSQLtranslation.toUpperCase().contains("LEFT"));
        executeAndCompareValues(query, getExpectedValuesNonOptimizableLJAndJoinMix());
    }

    protected ImmutableList<String> getExpectedValuesNonOptimizableLJAndJoinMix() {
        return ImmutableList.of("\"Depp\"^^xsd:string", "\"Poppins\"^^xsd:string", "\"Smith\"^^xsd:string",
                "\"Smith\"^^xsd:string", "\"Smith\"^^xsd:string");
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

        executeAndCompareValues(query, ImmutableSet.of("\"Jane Smith\"^^xsd:string", "\"Joe Logan\"^^xsd:string"));
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

        executeAndCompareValues(query, getExpectedAggregationMappingProfStudentCountPropertyResults());
    }

    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("\"12\"^^xsd:integer", "\"13\"^^xsd:integer", "\"31\"^^xsd:integer");
    }

    @Test
    public void testSample() {

        String query =  "PREFIX : <http://www.semanticweb.org/user/ontologies/2016/8/untitled-ontology-84#>\n" +
                "\n" +
                "SELECT ?p (SAMPLE(?nb) AS ?v)\n" +
                "WHERE {\n" +
                "   ?p :teaches ?c .\n" +
                "   ?c :nbStudents ?nb .\n" +
                "}\n" +
                "GROUP BY ?p \n" +
                "ORDER BY ?p";

        var result = runQuery(query);
        Assertions.assertTrue(getExpectedValuesSample().stream()
                .anyMatch(exp -> exp.equals(result)));
    }

    protected ImmutableList<ImmutableList<String>> getExpectedValuesSample() {
        return ImmutableList.of(
                ImmutableList.of("\"11\"^^xsd:integer", "\"12\"^^xsd:integer", "\"13\"^^xsd:integer"),
                ImmutableList.of("\"10\"^^xsd:integer", "\"12\"^^xsd:integer", "\"13\"^^xsd:integer"),
                ImmutableList.of("\"11\"^^xsd:int", "\"12\"^^xsd:int", "\"13\"^^xsd:int"),
                ImmutableList.of("\"10\"^^xsd:int", "\"12\"^^xsd:int", "\"13\"^^xsd:int")
        );
    }

    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }

    protected boolean supportsIntegrityConstraints() {
        return true;
    }

}
