package it.unibz.inf.ontop.docker.lightweight.mysql;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.MySQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@MySQLLightweightTest
public class BindWithFunctionsMySQLTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/mysql/books-mysql.properties";

    @BeforeAll
    public static void before()  {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.6000\"^^xsd:decimal", "\"5.7500\"^^xsd:decimal", "\"6.8000\"^^xsd:decimal",
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
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantDoubleDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDecimalDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:float");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getConstantFloatIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:float");
    }

    @Override
    protected ImmutableList<String> getConstantFloatDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000000000000000\"^^xsd:float");
    }

    @Override
    protected ImmutableSet<String> getDivisionOutputTypeExpectedResults() {
        return ImmutableSet.of("\"3.3333\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getExtraDateExtractionsExpectedValues() {
        return ImmutableSet.of("\"3 21 201 2 23 52000.0000 52000000\"^^xsd:string", "\"3 21 201 4 49 0.0000 0\"^^xsd:string",
                "\"3 21 201 3 39 6000.0000 6000000\"^^xsd:string", "\"2 20 197 4 45 0.0000 0\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00\"^^xsd:dateTime");
    }

    @Disabled("Currently MySQL does not support DATE_TRUNC for the type `DECADE`")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }


    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"215.3400\"^^xsd:decimal");
    }
}
