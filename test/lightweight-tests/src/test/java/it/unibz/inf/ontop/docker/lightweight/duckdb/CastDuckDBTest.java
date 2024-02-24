package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class CastDuckDBTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/duckdb/books-duckdb.properties";
    private static final String OBDA_FILE_DUCKDB = "/books/duckdb/books-duckdb.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE_DUCKDB, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in DuckDB limits datetime cast due to patterns")
    public void testCastDateTimeFromDate2() { super.testCastDateTimeFromDate2(); }

    @Override
    @Test
    @Disabled("Timestamp norm/denorm in DuckDB limits datetime cast due to patterns")
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
        return ImmutableSet.of("\"1999-12-14T00:00:00+00:00\"^^xsd:dateTime");
    }

    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T09:30:00+00:00\"^^xsd:dateTime");
    }
}
