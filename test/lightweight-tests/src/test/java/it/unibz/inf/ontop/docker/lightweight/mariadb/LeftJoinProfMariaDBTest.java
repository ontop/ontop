package it.unibz.inf.ontop.docker.lightweight.mariadb;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@MariaDBLightweightTest
public class LeftJoinProfMariaDBTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/mysql/prof-mariadb.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.2000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.3333\"^^xsd:decimal", "\"12.0000\"^^xsd:decimal", "\"13.0000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.3333\"^^xsd:decimal","\"12.0000\"^^xsd:decimal", "\"13.0000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000000000000000000000000000\"^^xsd:decimal",
                "\"16.000000000000000000000000000000\"^^xsd:decimal",
                "\"19.250000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.000\"^^xsd:decimal", "\"20.000\"^^xsd:decimal", "\"84.500\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31.000\"^^xsd:decimal", "\"32.000\"^^xsd:decimal", "\"115.500\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal", "\"31\"^^xsd:decimal");
    }
}
