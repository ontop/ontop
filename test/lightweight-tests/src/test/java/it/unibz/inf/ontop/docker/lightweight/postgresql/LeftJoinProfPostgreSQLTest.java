package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@PostgreSQLLightweightTest
public class LeftJoinProfPostgreSQLTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/postgresql/prof-postgresql.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.5000000000000000\"^^xsd:decimal", "\"16.0000000000000000\"^^xsd:decimal",
                "\"19.2500000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.2000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("\"10.3333333333333333\"^^xsd:decimal","\"12.0000000000000000\"^^xsd:decimal",
                "\"13.0000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.3333333333333333\"^^xsd:decimal", "\"12.0000000000000000\"^^xsd:decimal",
                "\"13.0000000000000000\"^^xsd:decimal");
    }

}
