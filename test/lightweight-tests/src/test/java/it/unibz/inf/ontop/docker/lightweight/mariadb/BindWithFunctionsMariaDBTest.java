package it.unibz.inf.ontop.docker.lightweight.mariadb;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@MariaDBLightweightTest
public class BindWithFunctionsMariaDBTest extends AbstractBindTestWithFunctions {

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
    protected ImmutableList<String> getAbsExpectedValues() {
        return ImmutableList.of("\"8.6000\"^^xsd:decimal", "\"5.7500\"^^xsd:decimal", "\"6.8000\"^^xsd:decimal",
                "\"1.5000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00+00:00\"^^xsd:string",
                "\"2011-12-08T11:30:00+00:00\"^^xsd:string",
                "\"2014-06-05T16:47:52+00:00\"^^xsd:string",
                "\"2015-09-21T09:23:06+00:00\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantDoubleDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDecimalDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:float");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getConstantFloatIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:float");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDivideExpectedResults() {
        return ImmutableList.of("\"0.5000000000000000000000000000000000\"^^xsd:float");
    }

}
