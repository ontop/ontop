package it.unibz.inf.ontop.docker.lightweight.dremio;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
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
@DremioLightweightTest
public class BindWithFunctionsDremioArrowFlightTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/dremio/books-dremio-arrowflight.properties";
    private static final String OBDA_FILE = "/books/dremio/books-dremio.obda";

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
        return ImmutableSet.of("\"8.600000\"^^xsd:decimal", "\"5.750000\"^^xsd:decimal", "\"6.800000\"^^xsd:decimal",
                "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Disabled("Dremio does not support milliseconds for timestampdiff.")
    @Test
    @Override
    public void testMilliSecondsBetween() {
        super.testMilliSecondsBetween();
    }

    @Disabled("Dremio does not support SHA384.")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Override
    protected ImmutableSet<String> getDateTruncGroupByExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00: 1\"^^xsd:string", "\"2010-01-01T00:00:00: 3\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.340000\"^^xsd:decimal");
    }
}
