package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class LeftJoinProfSparkSQLTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/spark/prof-spark.properties";
    private static final String OBDA_FILE = "/prof/spark/prof-spark.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMinStudents1() {
        return ImmutableList.of("\"10\"^^xsd:int");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMinStudents2() {
        return ImmutableList.of("\"10\"^^xsd:int","\"12\"^^xsd:int", "\"13\"^^xsd:int");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMaxStudents1() {
        return ImmutableList.of("\"13\"^^xsd:int");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMaxStudents2() {
        return ImmutableList.of("\"11\"^^xsd:int","\"12\"^^xsd:int", "\"13\"^^xsd:int");
    }

    @Override
    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("\"12\"^^xsd:long", "\"13\"^^xsd:long", "\"31\"^^xsd:long");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.333333333333334\"^^xsd:decimal", "\"12.0\"^^xsd:decimal",
                "\"13.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333334\"^^xsd:decimal", "\"12.0\"^^xsd:decimal",
                "\"13.0\"^^xsd:decimal");
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat1() {
        super.testGroupConcat1();
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat2() {
        super.testGroupConcat2();
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat3() {
        super.testGroupConcat3();
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat4() {
        super.testGroupConcat4();
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat5() {
        super.testGroupConcat5();
    }

    @Disabled("Spark SQL does not support LISTAGG() WITHIN GROUP or GROUP_CONCAT")
    @Test
    public void testGroupConcat6() {
        super.testGroupConcat6();
    }

    @Disabled("Cast to DECIMAL results in value with scale 0")
    @Test
    public void testDuration1() {
        super.testDuration1();
    }

    @Disabled("Cast to DECIMAL results in value with scale 0")
    @Test
    public void testMultitypedAvg1() {
        super.testMultitypedAvg1();
    }

    @Disabled("Cast to DECIMAL results in value with scale 0")
    @Test
    public void testMultitypedSum1() {
        super.testMultitypedSum1();
    }
}
