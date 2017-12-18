package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractLeftJoinProfTest extends AbstractVirtualModeTest {

    private static final String NO_SELF_LJ_OPTIMIZATION_MSG = "The table professors should be used only once";

    public AbstractLeftJoinProfTest(String owlFile, String obdaFile, String propertyFile) {
        super(owlFile, obdaFile, propertyFile);
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
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"professors\""));
        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql, "\"PROFESSORS\""));
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
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

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
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

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
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

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
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));


        System.out.println("SQL Query: \n" + sql);

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
                "  }\n" +
                "}";

        String [] expectedValues = {
                "Rog", "Rog", "Johnny"
        };
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

        assertFalse(NO_SELF_LJ_OPTIMIZATION_MSG, containsMoreThanOneOccurrence(sql.toLowerCase(), "\"professors\""));
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
                "}";

        String [] expectedValues = {
                "Smith", "Poppins", "Depp"
        };
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));


        System.out.println("SQL Query: \n" + sql);

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
                "FILTER (bound(?f))" +
                "}";

        String [] expectedValues = {
                "Smith", "Poppins", "Depp"
        };
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));


        System.out.println("SQL Query: \n" + sql);

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
                "}";

        String [] expectedValues = {
                "John", "Mary", "Roger"
        };
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

        assertFalse(sql.toUpperCase().contains("LEFT"));
    }

    @Ignore("Support preferences")
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

        String [] expectedValues = {
                "Dodero", "Frankie", "Gamper", "Helmer", "Johnny", "King of Pop", "Poppins", "Rog"
        };
        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));

        System.out.println("SQL Query: \n" + sql);

        assertTrue(sql.toUpperCase().contains("LEFT"));
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

        String sql = checkReturnedValuesAndReturnSql(query, Arrays.asList(expectedValues));
        System.out.println("SQL Query: \n" + sql);

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
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertTrue(sql.toUpperCase().contains("LEFT"));
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
        String sql = checkReturnedValuesAndReturnSql(query, expectedValues);

        System.out.println("SQL Query: \n" + sql);

        assertTrue(sql.toUpperCase().contains("LEFT"));
    }

    private static boolean containsMoreThanOneOccurrence(String query, String pattern) {
        int firstOccurrenceIndex = query.indexOf(pattern);
        if (firstOccurrenceIndex >= 0) {
            return query.substring(firstOccurrenceIndex + 1).contains(pattern);
        }
        return false;
    }

}
