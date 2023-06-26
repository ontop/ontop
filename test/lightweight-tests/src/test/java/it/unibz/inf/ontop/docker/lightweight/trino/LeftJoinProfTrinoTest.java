package it.unibz.inf.ontop.docker.lightweight.trino;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.TrinoLightweightTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@TrinoLightweightTest
public class LeftJoinProfTrinoTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/trino/prof-trino.properties";

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
        return ImmutableList.of("\"15.500000000000000000\"^^xsd:decimal", "\"16.000000000000000000\"^^xsd:decimal", "\"19.250000000000000000\"^^xsd:decimal");
    }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testCourseJoinOnLeft1() { super.testCourseJoinOnLeft1(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testCourseJoinOnLeft2() { super.testCourseJoinOnLeft2(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testCourseTeacherName() { super.testCourseTeacherName(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testFirstNameNickname() { super.testFirstNameNickname(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testFullName1() { super.testFullName1(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testFullName2() { super.testFullName2(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testMinus2() { super.testMinus2(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testMinusNickname() { super.testMinusNickname(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testNicknameAndCourse() { super.testNicknameAndCourse(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testPreferences() { super.testPreferences(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testSimpleFirstName() { super.testSimpleFirstName(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testSimpleNickname() { super.testSimpleNickname(); }

    @Disabled("This test requires integrity constraints that are not currently supported by trino.")
    @Test
    public void testUselessRightPart2() { super.testUselessRightPart2(); }

    @Disabled("This test requires avoiding applying ORDER BY to a sub-query")
    @Test
    @Override
    public void testDistinctAsGroupBy1() {
        super.testDistinctAsGroupBy1();
    }

    @Test
    public void testMinusMultitypedAvg() { super.testMinusMultitypedAvg(); }

    @Test
    public void testMinusMultitypedSum() { super.testMinusMultitypedSum(); }
}
