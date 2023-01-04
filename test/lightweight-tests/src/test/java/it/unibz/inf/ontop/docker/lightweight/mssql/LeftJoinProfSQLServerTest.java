package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MSSQLLightweightTest
public class LeftJoinProfSQLServerTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/mssql/prof-mssql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after()  {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("\"11.200000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("\"10.333333\"^^xsd:decimal", "\"12.000000\"^^xsd:decimal", "\"13.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333\"^^xsd:decimal", "\"12.000000\"^^xsd:decimal",
                "\"13.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.0000000000000000000\"^^xsd:decimal", "\"20.0000000000000000000\"^^xsd:decimal",
                "\"84.5000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.5000000000000000000\"^^xsd:decimal", "\"16.0000000000000000000\"^^xsd:decimal",
                "\"19.2500000000000000000\"^^xsd:decimal");
    }

    /**
     * DISTINCT is not (yet ?) supported in STRING_AGG
     */
    @Disabled
    @Test
    @Override
    public void testGroupConcat3() throws Exception {
        super.testGroupConcat3();
    }

    /**
     * DISTINCT is not (yet ?) supported in STRING_AGG
     */
    @Disabled
    @Test
    @Override
    public void testGroupConcat5() throws Exception {
        super.testGroupConcat5();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31.0000000000000000000\"^^xsd:decimal", "\"32.0000000000000000000\"^^xsd:decimal",
                "\"115.5000000000000000000\"^^xsd:decimal");
    }
}


