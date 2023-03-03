package it.unibz.inf.ontop.docker.lightweight.bigquery;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.BigQueryLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@BigQueryLightweightTest
public class LeftJoinProfBigQueryTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/bigquery/prof-bigquery.properties";
    private static final String OBDA_FILE = "/prof/bigquery/prof-bigquery.obda";


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("\"10.333333333333334\"^^xsd:decimal","\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333334\"^^xsd:decimal", "\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.000000000\"^^xsd:decimal", "\"20.000000000\"^^xsd:decimal", "\"84.500000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000000\"^^xsd:decimal", "\"16.000000000\"^^xsd:decimal", "\"19.250000000\"^^xsd:decimal");
    }

    @Override
    protected boolean supportsIntegrityConstraints() {
        return false;
    }

    @Disabled("Currently disabled because this test causes an issue in the translation of the query (Only DBFunctionSymbols must be provided to a SQLTermSerializer)")
    @Test
    @Override
    public void testMinusMultitypedSum() {
        super.testMinusMultitypedSum();
    }

    @Disabled("Currently disabled because this test causes an issue in the translation of the query (Only DBFunctionSymbols must be provided to a SQLTermSerializer)")
    @Test
    @Override
    public void testMinusMultitypedAvg() {
        super.testMinusMultitypedAvg();
    }
}
