package it.unibz.inf.ontop.docker.lightweight.oracle;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@OracleLightweightTest
public class LeftJoinProfOracleTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/oracle/prof-oracle.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    /**
     * DISTINCT is not (yet ?) supported in LISTAGG in Oracle
     */
    @Disabled
    @Test
    @Override
    public void testGroupConcat3() throws Exception {
        super.testGroupConcat3();
    }

    /**
     * DISTINCT is not (yet ?) supported in LISTAGG in Oracle
     */
    @Disabled
    @Test
    @Override
    public void testGroupConcat5() throws Exception {
        super.testGroupConcat5();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"1.03333333333333333333333333333333333333E01\"^^xsd:decimal", "\"12\"^^xsd:decimal",
                "\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"1.03333333333333333333333333333333333333E01\"^^xsd:decimal",
                "\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMaxStudents1() {
        return ImmutableList.of("\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMaxStudents2() {
        return ImmutableList.of("\"11\"^^xsd:decimal", "\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValueSumStudents1() {
        return ImmutableList.of("\"56\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValueSumStudents2() {
        return ImmutableList.of("\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal", "\"31\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValueSumStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal", "\"31\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMinStudents2() {
        return ImmutableList.of("\"10\"^^xsd:decimal","\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMinStudents1() {
        return ImmutableList.of("\"10\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal", "\"31\"^^xsd:decimal");
    }
}
