package it.unibz.inf.ontop.docker.lightweight.oracle;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@OracleLightweightTest
public class CastOracleTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/oracle/books-oracle.properties";
    private static final String OBDA_FILE = "/books/oracle/books-oracle.obda";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
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
    protected ImmutableMultiset<String> getCastDecimalFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.2\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getCastStringFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:string", "\"0.25\"^^xsd:string", "\"0.2\"^^xsd:string",
                "\"0.15\"^^xsd:string");
    }

    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Oracle renders date-datetime casts impossible")
    public void testCastDateFromDateTime1() {
        super.testCastDateFromDateTime1();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Oracle renders date-datetime casts impossible")
    public void testCastDateFromDateTime2() {
        super.testCastDateFromDateTime2();
    }

    @Override
    @Test
    @Disabled("TO_DATE in Oracle incorrectly adds hh:mm:ss")
    public void testCastDateFromString1() {
        super.testCastDateFromString1();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Oracle renders date-datetime casts impossible")
    public void testCastDateTimeFromDate1() {
        super.testCastDateTimeFromDate1();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Oracle renders date-datetime casts impossible")
    public void testCastDateTimeFromDate2() {
        super.testCastDateTimeFromDate2();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Oracle renders date-datetime casts impossible")
    public void testCastDateTimeFromDate3() {
        super.testCastDateTimeFromDate3();
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromDateTime1ExpectedValues() {
        return ImmutableSet.of("\"2014-06-05T16:47:52.000000\"^^xsd:dateTime",
                "\"2011-12-08T11:30:00.000000\"^^xsd:dateTime",
                "\"2015-09-21T09:23:06.000000\"^^xsd:dateTime",
                "\"1970-11-05T07:50:00.000000\"^^xsd:dateTime");
    }

    @Override
    @Disabled("Oracle handling of TZ system dependent. Output is 1999-12-14 09:30:00.0 Europe/Rome\"^^xsd:dateTime")
    @Test
    public void testCastDateTimeFromString() {
        super.testCastDateTimeFromString();
    }
}
