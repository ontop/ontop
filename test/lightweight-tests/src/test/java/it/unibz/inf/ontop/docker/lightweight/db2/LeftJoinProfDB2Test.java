package it.unibz.inf.ontop.docker.lightweight.db2;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@DB2LightweightTest
public class LeftJoinProfDB2Test extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/db2/prof-db2.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return ImmutableList.of("\"11.2000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("\"10.3333333333333333333\"^^xsd:decimal","\"12.0000000000000000000\"^^xsd:decimal",
                "\"13.0000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.3333333333333333333\"^^xsd:decimal",
                "\"12.0000000000000000000\"^^xsd:decimal", "\"13.0000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"18.000\"^^xsd:decimal", "\"20.000\"^^xsd:decimal",
                "\"84.500\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000000000000000000000\"^^xsd:decimal",
                "\"16.000000000000000000000000\"^^xsd:decimal", "\"19.250000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("\"31.000\"^^xsd:decimal", "\"32.000\"^^xsd:decimal", "\"115.500\"^^xsd:decimal");
    }

    /**
     * R2RMLIRISafeEncoder triggers tens of nested REPLACE due to "#" symbol in cases where a class is both mapped
     * from the db and provided as facts.
     * DB2 triggers error https://www.ibm.com/docs/en/db2-for-zos/11?topic=codes-134 in these cases
     * "A string value with a length attribute greater than 255 bytes is not allowed in a SELECT list that also
     * specifies DISTINCT."
     */
    @Disabled
    @Test
    @Override
    public void testMinStudents1() throws Exception {
        super.testMinStudents1();
    }

    @Disabled
    @Test
    @Override
    public void testMaxStudents1() throws Exception {
        super.testMaxStudents1();
    }

    @Disabled
    @Test
    @Override
    public void testSumStudents1() throws Exception {
        super.testSumStudents1();
    }

    @Disabled
    @Test
    @Override
    public void testAvgStudents1() throws Exception {
        super.testAvgStudents1();
    }

}