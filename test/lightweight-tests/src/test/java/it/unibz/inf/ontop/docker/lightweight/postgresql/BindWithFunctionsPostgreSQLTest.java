package it.unibz.inf.ontop.docker.lightweight.postgresql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.PostgreSQLLightweightTest;
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
@PostgreSQLLightweightTest
public class BindWithFunctionsPostgreSQLTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/postgresql/books-postgresql.properties";

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
        return ImmutableSet.of("\"8.6\"^^xsd:decimal", "\"5.75\"^^xsd:decimal", "\"6.8\"^^xsd:decimal",
                "\"1.50\"^^xsd:decimal");
    }

    @Disabled("Please enable pgcrypto (CREATE EXTENSION pgcrypto")
    @Test
    @Override
    public void testHashSHA1() {
        super.testHashSHA1();
    }

    @Disabled("Please enable pgcrypto (CREATE EXTENSION pgcrypto")
    @Test
    @Override
    public void testHashSHA256() {
        super.testHashSHA256();
    }

    @Disabled("Please enable pgcrypto (CREATE EXTENSION pgcrypto")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Disabled("Please enable pgcrypto (CREATE EXTENSION pgcrypto")
    @Test
    @Override
    public void testHashSHA512() {
        super.testHashSHA512();
    }

    @Disabled("PostgreSQL v14 introduces trailing 0-s to result")
    @Test
    public void testSeconds() {
        super.testSeconds();
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.5000000000000000\"^^xsd:decimal", "\"11.5000000000000000\"^^xsd:decimal",
                "\"17.0000000000000000\"^^xsd:decimal", "\"5.0000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getDatatypeExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.2\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.50000000000000000000\"^^xsd:decimal");
    }
}
