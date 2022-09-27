package it.unibz.inf.ontop.rdf4j.repository.rules;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.rdf4j.repository.AbstractRDF4JTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

    @Ignore("TODO: support SPARQL rules")
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
}
