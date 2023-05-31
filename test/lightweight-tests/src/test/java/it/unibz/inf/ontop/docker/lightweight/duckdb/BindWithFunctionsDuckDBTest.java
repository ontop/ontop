package it.unibz.inf.ontop.docker.lightweight.duckdb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@DuckDBLightweightTest
public class BindWithFunctionsDuckDBTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/duckdb/books-duckdb.properties";
    private static final String OBDA_FILE_DUCKDB = "/books/duckdb/books-duckdb.obda"; //DuckDB's JDBC does not support default
                                                                                //schemas, so we need to provide an
                                                                                //obda file with fully qualified names.

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE_DUCKDB, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000000000000000000000000000000\"^^xsd:decimal", "\"5.750000000000000000000000000000000000\"^^xsd:decimal", "\"6.800000000000000000000000000000000000\"^^xsd:decimal",
                "\"1.500000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.5\"^^xsd:decimal", "\"11.5\"^^xsd:decimal",
                "\"17.0\"^^xsd:decimal", "\"5.0\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05 07:50:00.0\"^^xsd:string", "\"2011-12-08 11:30:00.0\"^^xsd:string",
                "\"2014-06-05 16:47:52.0\"^^xsd:string", "\"2015-09-21 09:23:06.0\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:decimal");
    }

    @Disabled("DuckDB counts one hour less on two results")
    @Test
    @Override
    public void testSecondsBetweenMappingInput() {
        super.testSecondsBetweenMappingInput();
    }

    @Disabled("DuckDB's week count is off by one")
    @Test
    @Override
    public void testWeeksBetweenDate() {
        super.testWeeksBetweenDate();
    }

    @Disabled("DuckDB's week count is off by one'")
    @Test
    @Override
    public void testWeeksBetweenDateTime() {
        super.testWeeksBetweenDateTime();
    }

    @Disabled("DuckDB's day count is off by one'")
    @Test
    @Override
    public void testDaysBetweenDateTime() {
        super.testDaysBetweenDateTime();
    }

    @Disabled("DuckDB doesn't support the DATE_DIFF function on mixed date times. However, the time literal is parsed" +
            "as `TIMESTAMP` while the table date attribute is parsed as `TIMESTAMP WITH TIME ZONE`")
    @Test
    @Override
    public void testDaysBetweenDateTimeMappingInput() {
        super.testDaysBetweenDateTimeMappingInput();
    }

    @Disabled("DuckDB does not support SHA hashing")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Disabled("DuckDB does not support SHA hashing")
    @Test
    @Override
    public void testHashSHA1() {
        super.testHashSHA1();
    }

    @Disabled("DuckDB does not support SHA hashing")
    @Test
    @Override
    public void testHashSHA256() {
        super.testHashSHA256();
    }

    @Disabled("DuckDB does not support SHA hashing")
    @Test
    @Override
    public void testHashSHA512() {
        super.testHashSHA512();
    }

    @Override
    protected ImmutableSet<String> getDateTruncGroupByExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00+01: 1\"^^xsd:string", "\"2010-01-01T00:00:00+01: 3\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00+01\"^^xsd:dateTime", "\"2011-01-01T00:00:00+01\"^^xsd:dateTime", "\"2014-01-01T00:00:00+01\"^^xsd:dateTime", "\"2015-01-01T00:00:00+01\"^^xsd:dateTime");
    }
}
