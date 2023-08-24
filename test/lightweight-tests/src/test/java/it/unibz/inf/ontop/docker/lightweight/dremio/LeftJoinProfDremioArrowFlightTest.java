package it.unibz.inf.ontop.docker.lightweight.dremio;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@DremioLightweightTest
public class LeftJoinProfDremioArrowFlightTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/dremio/prof-dremio-arrowflight.properties";
    private static final String OBDA_FILE = "/prof/dremio/prof-dremio.obda";


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected boolean supportsIntegrityConstraints() {
        return false;
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.0000\"^^xsd:decimal", "\"20.0000\"^^xsd:decimal", "\"84.5000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.5\"^^xsd:decimal", "\"16.0\"^^xsd:decimal", "\"19.25\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1() {
        return ImmutableList.of("\"31.000000000000000000000000000000\"^^xsd:decimal", "\"32.000000000000000000000000000000\"^^xsd:decimal", "\"115.500000000000000000000000000000\"^^xsd:decimal");
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
}
