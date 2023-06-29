package it.unibz.inf.ontop.docker.lightweight.mysql;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractCastFunctionsTest;
import it.unibz.inf.ontop.docker.lightweight.MySQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

@MySQLLightweightTest
public class CastMySQLTest extends AbstractCastFunctionsTest {

    private static final String PROPERTIES_FILE = "/books/mysql/books-mysql.properties";

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
    protected ImmutableSet<String> getCastDateTimeFromDateTime1ExpectedValues() {
        return ImmutableSet.of("\"2014-06-05T16:47:52+00:00\"^^xsd:dateTime",
                "\"2011-12-08T11:30:00+00:00\"^^xsd:dateTime",
                "\"2015-09-21T09:23:06+00:00\"^^xsd:dateTime",
                "\"1970-11-05T07:50:00+00:00\"^^xsd:dateTime");
    }
}