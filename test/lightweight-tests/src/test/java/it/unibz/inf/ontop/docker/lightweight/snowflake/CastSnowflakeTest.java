package it.unibz.inf.ontop.docker.lightweight.snowflake;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SnowflakeLightweightTest
public class CastSnowflakeTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/snowflake/books-snowflake.properties";

    @BeforeAll
    public static void before() {
        initOBDA("/books/snowflake/books-snowflake.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Snowflake limits datetime cast due to patterns")
    public void testCastDateTimeFromDate2() { super.testCastDateTimeFromDate2(); }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Snowflake limits datetime cast due to patterns")
    public void testCastDateTimeFromDate3() {
        super.testCastDateTimeFromDate3();
    }

    @Override
    protected ImmutableMultiset<String> getCastFloatFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:float", "\"0.25\"^^xsd:float", "\"0.2\"^^xsd:float",
                "\"0.15\"^^xsd:float");
    }

    @Override
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate1ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000Z\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T09:30:00.000Z\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromDoubleExpectedValues() {
        return ImmutableSet.of("\"2.0654000000\"^^xsd:decimal");
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
}
