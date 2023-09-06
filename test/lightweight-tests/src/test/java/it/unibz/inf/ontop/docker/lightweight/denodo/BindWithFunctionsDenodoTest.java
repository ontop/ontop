package it.unibz.inf.ontop.docker.lightweight.denodo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DenodoLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@DenodoLightweightTest
public class BindWithFunctionsDenodoTest extends AbstractBindTestWithFunctions {

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
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.6000000000000000000000000000000000000000\"^^xsd:decimal", "\"5.7500000000000000000000000000000000000000\"^^xsd:decimal", "\"6.8000000000000000000000000000000000000000\"^^xsd:decimal",
                "\"1.5000000000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Disabled("Denodo does not yet support UUIDs")
    @Test
    @Override
    public void testUuid() {
        super.testUuid();
    }

    @Disabled("Denodo does not yet support UUIDs")
    @Test
    @Override
    public void testStrUuid() {
        super.testStrUuid();
    }

    @Disabled("Denodo does not yet support SHA Hash")
    @Test
    @Override
    public void testHashSHA256() {
        super.testHashSHA256();
    }

    @Disabled("Denodo does not yet support SHA Hash")
    @Test
    @Override
    public void testHashSHA1() {
        super.testHashSHA1();
    }

    @Disabled("Denodo does not yet support SHA Hash")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Disabled("Denodo does not yet support SHA Hash")
    @Test
    @Override
    public void testHashSHA512() {
        super.testHashSHA512();
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05 07:50:00.0\"^^xsd:string", "\"2011-12-08 11:30:00.0\"^^xsd:string",
                "\"2014-06-05 16:47:52.0\"^^xsd:string", "\"2015-09-21 09:23:06.0\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.50000000000000000000\"^^xsd:decimal", "\"11.50000000000000000000\"^^xsd:decimal",
                "\"17.00000000000000000000\"^^xsd:decimal", "\"5.00000000000000000000\"^^xsd:decimal");
    }

    @Disabled("Denodo uses a different timezone depending on the system, so this result is not accurate.")
    @Test
    @Override
    public void testDay() {
        super.testDay();
    }

    @Disabled("Denodo uses a different timezone depending on the system, so this result is not accurate.")
    @Test
    @Override
    public void testHours() {
        super.testHours();
    }

    @Disabled("Denodo does not allow the use of REGEXP_LIKE in the projection part.")
    @Test
    @Override
    public void testREGEX() {
        super.testREGEX();
    }

    @Disabled("Denodo uses a different format for MD5, and does not support conversion to HEX.")
    @Test
    @Override
    public void testHashMd5() {
        super.testHashMd5();
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000000-08:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00.000000-08:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00.000000-08:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00.000000-08:00\"^^xsd:dateTime");
    }

    @Disabled("Denodo does not support date_trunc by decade.")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.340000\"^^xsd:decimal");
    }
}
