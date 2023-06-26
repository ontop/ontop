package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMultiset;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@PostgreSQLLightweightTest
public class CastPostgreSQLTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/postgresql/books-postgresql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Disabled("PostgresSQL adds different TZ to result depending on local time")
    @Test
    public void testCastDateTimeFromDate1() {
        super.testCastDateTimeFromDate1();
    }

    @Disabled("PostgresSQL adds different TZ to result depending on local time")
    @Test
    public void testCastDateTimeFromDate2() {
        super.testCastDateTimeFromDate2();
    }

    @Disabled("PostgresSQL adds different TZ to result depending on local time")
    @Test
    public void testCastDateTimeFromDate3() {
        super.testCastDateTimeFromDate3();
    }

    @Disabled("PostgresSQL adds different TZ to result depending on local time")
    @Test
    public void testCastDateTimeFromString() {
        super.testCastDateTimeFromString();
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromDoubleExpectedValues() {
        return ImmutableSet.of("\"0\"^^xsd:float");
    }

    @Override
    protected ImmutableMultiset<String> getCastFloatFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:float", "\"0.25\"^^xsd:float", "\"0.2\"^^xsd:float",
                "\"0.15\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromDecimal2ExpectedValues() {
        return ImmutableSet.of("\"0\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromString3ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:float");
    }

    @Override
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromString2ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate3ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14 00:00:00\"^^xsd:dateTime");
    }
}
