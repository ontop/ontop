package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Tests datatype inference for black-box SQL views
 */
public class SQLViewPersonAggregationTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_sql_views_aggregation_query.obda";
    private static final String SQL_SCRIPT = "/person/person_sql_views_db.sql";
    private static final String VIEW_FILE = "/person/views/sql_views_aggregation_query.json";
    private static final String PROPERTIES_FILE = "/person/person_sql_views_aggregation_query.properties";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTIES_FILE, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Test correct columns are projected for query with aggregation
     */
    @Test
    public void testPersonSelect() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :country ?v . \n" +
                " FILTER (datatype(?v) = xsd:string)\n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("ie", "mt"));
    }

    @Test
    public void testPersonSums() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :sum ?v . \n" +
                " FILTER (datatype(?v) = xsd:integer)\n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("0", "0"));
    }
}

