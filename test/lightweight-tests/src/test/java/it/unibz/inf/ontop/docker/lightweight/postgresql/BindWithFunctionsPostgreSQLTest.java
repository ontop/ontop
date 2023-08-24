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

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@PostgreSQLLightweightTest
public class BindWithFunctionsPostgreSQLTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/postgresql/books-postgresql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.60\"^^xsd:decimal", "\"5.75\"^^xsd:decimal", "\"6.80\"^^xsd:decimal",
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
        return ImmutableMultiset.of("\"0.20\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.20\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.50000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getExtraDateExtractionsExpectedValues() {
        return ImmutableSet.of("\"3 21 201 2 23 52000.000 52000000\"^^xsd:string", "\"3 21 201 4 49 0.000 0\"^^xsd:string",
                "\"3 21 201 3 39 6000.000 6000000\"^^xsd:string", "\"2 20 197 4 45 0.000 0\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.3400000000000000\"^^xsd:decimal");
    }
}
