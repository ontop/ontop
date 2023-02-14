package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class LeftJoinProfDuckDBTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/duckdb/prof-duckdb.properties";
    private static final String OBDA_FILE_ATHENA = "/prof/duckdb/prof-duckdb.obda"; //Athena does not support default
                                                                                    //schemas, so we need to provide an
                                                                                    //obda file with fully qualified names.


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE_ATHENA, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.2\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.333333333333334\"^^xsd:decimal","\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333334\"^^xsd:decimal","\"12.0\"^^xsd:decimal", "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.000000000000000000\"^^xsd:decimal", "\"20.000000000000000000\"^^xsd:decimal",
                "\"84.500000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1() {
        return ImmutableList.of("\"31.000000000000000000\"^^xsd:decimal", "\"32.000000000000000000\"^^xsd:decimal", "\"115.500000000000000000\"^^xsd:decimal");
    }


    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.5\"^^xsd:decimal", "\"16.0\"^^xsd:decimal", "\"19.25\"^^xsd:decimal");
    }

}
