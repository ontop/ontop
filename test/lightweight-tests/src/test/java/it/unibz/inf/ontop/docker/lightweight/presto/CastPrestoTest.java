package it.unibz.inf.ontop.docker.lightweight.presto;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.PrestoLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@PrestoLightweightTest
public class CastPrestoTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/presto/books-presto.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    //TODO: Trino timestamp denormalizer missing
    @Override
    @Test
    @Disabled("Lack of Trino timestamp denormalizer")
    public void testCastDateTimeFromString() {
        super.testCastDateTimeFromString();
    }

    //TODO: Trino timestamp denormalizer missing
    @Override
    @Test
    @Disabled("Lack of Trino timestamp denormalizer")
    public void testCastDateFromDateTime1() {
        super.testCastDateFromDateTime1();
    }

    //TODO: Trino timestamp denormalizer missing
    @Override
    @Test
    @Disabled("Lack of Trino timestamp denormalizer")
    public void testCastDateFromDateTime2() {
        super.testCastDateFromDateTime2();
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
    protected ImmutableSet<String> getCastDecimalFromDoubleExpectedValues() {
        return ImmutableSet.of("\"2.065400000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromFloatExpectedValues() {
        return ImmutableSet.of("\"2.600000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromIntegerExpectedValues() {
        return ImmutableSet.of("\"19991214.000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.065400000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate1ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000Z\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate2ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000Z\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate3ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000+01:00\"^^xsd:dateTime");
    }
}
