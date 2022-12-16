package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@SnowflakeLightweightTest
public class LeftJoinProfSnowflakeTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/snowflake/prof-snowflake.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA("/prof/snowflake/prof-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.200000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.333333\"^^xsd:decimal","\"12.000000\"^^xsd:decimal", "\"13.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"10.333333\"^^xsd:decimal","\"12.000000\"^^xsd:decimal", "\"13.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"18.00000\"^^xsd:decimal", "\"20.00000\"^^xsd:decimal", "\"84.50000\"^^xsd:decimal");
    }


    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000\"^^xsd:decimal", "\"16.000000\"^^xsd:decimal", "\"19.250000\"^^xsd:decimal");
    }

}
