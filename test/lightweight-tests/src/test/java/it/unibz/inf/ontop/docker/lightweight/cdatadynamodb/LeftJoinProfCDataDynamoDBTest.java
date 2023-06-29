package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@CDataDynamoDBLightweightTest
public class LeftJoinProfCDataDynamoDBTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/cdatadynamodb/prof-cdatadynamodb.properties";
    private static final String OBDA_FILE = "/prof/cdatadynamodb/prof-cdatadynamodb.obda";


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat1() {
        super.testGroupConcat1();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat2() {
        super.testGroupConcat2();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat3() {
        super.testGroupConcat3();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat4() {
        super.testGroupConcat4();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat5() {
        super.testGroupConcat5();
    }

    @Disabled("GROUP CONCAT is not supported by CDataDynamoDB JDBC")
    @Test
    @Override
    public void testGroupConcat6() {
        super.testGroupConcat6();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("\"10.333333333333334\"^^xsd:decimal","\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"10.333333333333334\"^^xsd:decimal", "\"12\"^^xsd:decimal", "\"13\"^^xsd:decimal");
    }


    @Override
    protected boolean supportsIntegrityConstraints() {
        return false;
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("\"15.500000000000000000\"^^xsd:double", "\"16.000000000000000000\"^^xsd:double", "\"19.250000000000000000\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1() {
        return ImmutableList.of("\"31\"^^xsd:double", "\"32\"^^xsd:double", "\"115.5\"^^xsd:double");
    }

    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("\"0\"^^xsd:double", "\"0\"^^xsd:double", "\"0\"^^xsd:double", "\"0\"^^xsd:double",
                "\"0\"^^xsd:double", "\"18\"^^xsd:double", "\"20\"^^xsd:double", "\"84.5\"^^xsd:double");
    }
}
