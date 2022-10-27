package it.unibz.inf.ontop.rdf4j.repository.rules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.rdf4j.repository.AbstractRDF4JTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class SparqlRuleEmployeeTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/employee/employee.obda";
    private static final String SQL_SCRIPT = "/employee/employee.sql";
    private static final String SPARQL_RULES = "/employee/employee-rules.toml";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, null, null, SPARQL_RULES);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testDefaultGraphEmployeeFullNames() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Employee . \n" +
                " ?x :fullName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Roger Smith", "Anna Gross"));
    }

    @Test
    public void testCompany2EmployeeFullNames() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "FROM <http://employee.example.org/data/graph/company2>\n" +
                "WHERE {\n" +
                " ?x a :Employee . \n" +
                " ?x :fullName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Papov Vlad", "Doe John"));
    }

    @Test
    public void testCompany2EmployeeFullNamesNewGraph() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "FROM <http://employee.example.org/data/newGraph>\n" +
                "WHERE {\n" +
                " ?x :fullName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Papov Vlad", "Doe John"));
    }

    @Test
    public void testDeveloperPosition() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Developer . \n" +
                " ?x :hasPosition ?p . \n" +
                " ?p a :Position . \n" +
                " ?p rdfs:label ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Developer"));
    }

    @Test
    public void testEmployeeLabels() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Employee . \n" +
                " GRAPH <http://employee.example.org/graph1> {\n" +
                "    ?x :label ?v .\n" +
                " }\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Roger Smith (Developer)"));
    }

    @Test
    public void testPositionCategoryCounts() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " BIND(<http://example.org/positionCategory/Developer> AS ?positionCategory)\n" +
                " ?positionCategory a :PositionCategory ; :count ?v ." +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("1"));
    }

    @Test
    public void testEvent() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?event a :Event ; :label ?v ." +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Deployment on Kubernetes", "CRM update"));
    }

    @Test
    public void testNotAClass() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :NotAClass ." +
                "}";
        runQueryAndCompare(query, ImmutableSet.of());
    }

    @Test
    public void testCapitalizedNames() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x :capitalizedName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("PAPOV", "DOE"));
    }

    @Test
    public void testLowerCaseNames() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x :lowerCaseName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("taylor", "doe"));
    }

    /**
     * NB: does not involve a rule, just testing FROM, making it removes duplicates
     */
    @Test
    public void testCompany2Employees() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "FROM <http://employee.example.org/data/graph/company2>\n" +
                "FROM <http://employee.example.org/data/graph/company2-source2>\n" +
                "WHERE {\n" +
                " ?x a :Employee .\n" +
                " ?x :lastName ?v . \n" +
                "}\n" +
                "ORDER BY ?v";
        runQueryAndCompare(query, ImmutableList.of("Doe", "Papov", "Taylor"));
    }

    /**
     * NB: does not involve a rule, just testing FROM NAMED
     */
    @Test
    public void testCompany2Employees2() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "FROM NAMED <http://employee.example.org/data/graph/company2>\n" +
                "FROM NAMED <http://employee.example.org/data/graph/company2-source2>\n" +
                "WHERE {\n" +
                " GRAPH ?g { \n" +
                "   ?x a :Employee .\n" +
                "   ?x :lastName ?v . \n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";
        runQueryAndCompare(query, ImmutableList.of("Doe", "Doe", "Papov", "Taylor"));
    }

    /**
     * NB: does not involve a rule, just testing FROM NAMED
     */
    @Test
    public void testCompany2Employees3() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "FROM NAMED <http://employee.example.org/data/graph/company2-source2>\n" +
                "WHERE {\n" +
                " GRAPH ?g { \n" +
                "   ?x a :Employee .\n" +
                "   ?x :lastName ?v . \n" +
                "  }\n" +
                "}\n" +
                "ORDER BY ?v";
        runQueryAndCompare(query, ImmutableList.of("Doe", "Taylor"));
    }

}
