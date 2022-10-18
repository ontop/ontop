package it.unibz.inf.ontop.rdf4j.repository.rules;

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
    public void testEmployeeFullNames() {
        String query = "PREFIX : <http://employee.example.org/voc#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Employee . \n" +
                " ?x :fullName ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Roger Smith", "Anna Gross"));
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
                "    ?x rdfs:label ?v .\n" +
                " }\n" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("Roger Smith (Developer)"));
    }
}
