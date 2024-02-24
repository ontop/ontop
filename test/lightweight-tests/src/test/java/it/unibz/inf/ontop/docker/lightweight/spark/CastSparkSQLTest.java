package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class CastSparkSQLTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/spark/books-spark.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate1ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000+00:00\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate2ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14 00:00:00.0\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset<String> getCastFloatFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:float", "\"0.25\"^^xsd:float", "\"0.2\"^^xsd:float",
                "\"0.15\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromFloatExpectedValues() {
        return ImmutableSet.of("\"2.6000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromIntegerExpectedValues() {
        return ImmutableSet.of("\"19991214.0000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.0654000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.0000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromDoubleExpectedValues() {
        return ImmutableSet.of("\"2.0654000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T09:30:00.000+00:00\"^^xsd:dateTime");
    }

    //TODO: Add DateDenormFunctionSymbol for SparkSQL
    @Disabled("DateDenormFunctionSymbol needed which removes 'Z' from patterns 1999-12-14Z")
    @Override
    @Test
    public void testCastDateTimeFromDate2() {
        super.testCastDateTimeFromDate2();
    }

    //TODO: Modify SparkSQLTimestampDenormFunctionSymbol for SparkSQL
    @Disabled("SparkSQLTimestampDenormFunctionSymbol needs to add space before + sign in pattern 1999-12-14+01:00")
    @Override
    @Test
    public void testCastDateTimeFromDate3() {
        super.testCastDateTimeFromDate3();
    }
}
