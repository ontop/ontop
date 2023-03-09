package it.unibz.inf.ontop.docker.lightweight.denodo;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.DenodoLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DenodoLightweightTest
public class LeftJoinProfDenodoTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/denodo/prof-denodo.properties";


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }


    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.00000000000000000000\"^^xsd:decimal", "\"20.00000000000000000000\"^^xsd:decimal", "\"84.50000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.50000000000000000000\"^^xsd:decimal", "\"16.00000000000000000000\"^^xsd:decimal", "\"19.25000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31.00000000000000000000\"^^xsd:decimal", "\"32.00000000000000000000\"^^xsd:decimal", "\"115.50000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.333333333333334\"^^xsd:decimal","\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333334\"^^xsd:decimal", "\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Disabled("Denodo does not support DISTINCT in GROUP_CONCAT")
    @Test
    @Override
    public void testGroupConcat3() {
        super.testGroupConcat3();
    }

    @Disabled("Denodo does not support DISTINCT in GROUP_CONCAT")
    @Test
    @Override
    public void testGroupConcat5() {
        super.testGroupConcat5();
    }

    @Disabled("Denodo does not allow LIMIT inside sub-queries")
    @Test
    @Override
    public void testLimitSubQuery1() {
        super.testLimitSubQuery1();
    }
}
