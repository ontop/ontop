package it.unibz.inf.ontop.docker.lightweight.db2;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DB2LightweightTest
public class CastDB2Test extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/db2/books-db2.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset<String> getCastFloatFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:float", "\"0.25\"^^xsd:float", "\"0.2\"^^xsd:float",
                "\"0.15\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:float");
    }

    @Override
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromFloatExpectedValues() {
        return ImmutableSet.of("\"2.600000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromDoubleExpectedValues() {
        return ImmutableSet.of("\"2.065400\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromIntegerExpectedValues() {
        return ImmutableSet.of("\"19991214.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.065400\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDateTime1ExpectedValues() {
        return ImmutableSet.of("\"2014-06-05T16:47:52.000000\"^^xsd:dateTime",
                "\"2011-12-08T11:30:00.000000\"^^xsd:dateTime",
                "\"2015-09-21T09:23:06.000000\"^^xsd:dateTime",
                "\"1970-11-05T07:50:00.000000\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDate1ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00.000000\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T09:30:00.000000\"^^xsd:dateTime");
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in DB2 renders date-datetime casts impossible")
    public void testCastDateTimeFromDate2() {
        super.testCastDateTimeFromDate2();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in DB2 renders date-datetime casts impossible")
    public void testCastDateTimeFromDate3() {
        super.testCastDateTimeFromDate3();
    }
}
