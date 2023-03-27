package it.unibz.inf.ontop.docker.lightweight.denodo;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.DenodoLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DenodoLightweightTest
public class CastDenodoTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/denodo/books-denodo.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Denodo renders this specific date-datetime casts impossible")
    public void testCastDateTimeFromDate2() {
        super.testCastDateTimeFromDate2();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in Denodo renders this specific date-datetime casts impossible")
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
        return ImmutableSet.of("\"1999-12-14 00:00:00.0\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14 09:30:00.0\"^^xsd:dateTime");
    }
}
