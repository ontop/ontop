package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class UnionLensTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/union-lens/test.obda";
    private static final String SQL_SCRIPT = "/union-lens/test.sql";
    private static final String VIEW_FILE = "/union-lens/lens.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCorrectNumberOfUnionEntries() {
        String query = "PREFIX : <http://www.ontop-vkg.com/union-test#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :DayRecord . \n" +
                "}";
        assertEquals(20, runQueryAndCount(query));
    }

    @Test
    public void testCorrectProvenanceColumn() {
        String query = "PREFIX : <http://www.ontop-vkg.com/union-test#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?y a :BudgetYear . \n" +
                " ?y :name ?v" +
                "}";
        runQueryAndCompare(query, ImmutableSet.of("BUDGETYEAR2020", "BUDGETYEAR2021", "lenses.Year2022"));
    }

    @Test
    public void testCorrectProvenanceColumnAssignment() {
        String query = "PREFIX : <http://www.ontop-vkg.com/union-test#>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  (SUM(?e) as ?v) \n" +
                "WHERE {\n" +
                " ?y a :BudgetYear . \n" +
                " ?d :year ?y . \n" +
                " ?d :earnings ?e .\n" +
                "} GROUP BY ?y";
        runQueryAndCompare(query, ImmutableSet.of("285", "360", "575"));
    }
}
