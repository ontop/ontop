package it.unibz.inf.ontop.docker.lightweight.redshift;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.RedshiftLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@RedshiftLightweightTest
public class LeftJoinProfRedshiftTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/redshift/prof-redshift.properties";
    private static final String OBDA_FILE = "/prof/redshift/prof-redshift.obda";


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
        return   ImmutableList.of("\"10.333333333333333333\"^^xsd:decimal","\"12.000000000000000000\"^^xsd:decimal", "\"13.000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333333333\"^^xsd:decimal", "\"12.000000000000000000\"^^xsd:decimal", "\"13.000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.000000000000000000\"^^xsd:decimal", "\"20.000000000000000000\"^^xsd:decimal", "\"84.500000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000000000000000\"^^xsd:decimal", "\"16.000000000000000000\"^^xsd:decimal", "\"19.250000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31.000000000000000000\"^^xsd:decimal", "\"32.000000000000000000\"^^xsd:decimal", "\"115.500000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.200000000000000000\"^^xsd:decimal");
    }

}
