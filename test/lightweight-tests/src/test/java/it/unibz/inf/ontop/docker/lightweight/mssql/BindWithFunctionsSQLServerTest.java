package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.MSSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@MSSQLLightweightTest
public class BindWithFunctionsSQLServerTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/mssql/books-mssql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after()  {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000\"^^xsd:decimal", "\"5.750000\"^^xsd:decimal", "\"6.800000\"^^xsd:decimal",
        "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getRoundExpectedValues() {
        return ImmutableSet.of("\"0E-19, 43.0000000000000000000\"^^xsd:string",
                "\"0E-19, 23.0000000000000000000\"^^xsd:string",
                "\"0E-19, 34.0000000000000000000\"^^xsd:string",
                "\"0E-19, 10.0000000000000000000\"^^xsd:string");
    }

    @Disabled("not supported")
    @Test
    public void testREPLACE() {
        super.testREPLACE();
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Disabled("not supported")
    @Test
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    @Disabled("Current MS SQL Server handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }

    @Disabled("Decade is not supportes ad part for SQLServer DATETRUNC")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
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
