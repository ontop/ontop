package it.unibz.inf.ontop.docker.lightweight.mariadb;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@MariaDBLightweightTest
public class CastMariaDBTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/mariadb/books-mariadb.properties";

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
    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromIntegerExpectedValues() {
        return ImmutableSet.of("\"19991214.000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.065400000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromString2ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromDecimal2ExpectedValues() {
        return ImmutableSet.of("\"0\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromDoubleExpectedValues() {
        return ImmutableSet.of("\"0\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1\"^^xsd:double");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastFloatFromString3ExpectedValues() {
        return ImmutableSet.of("\"2\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getCastDoubleFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214\"^^xsd:double");
    }
}
