package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableList;
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
    protected ImmutableList<String> getAbsExpectedValues() {
        return ImmutableList.of("\"8.600000\"^^xsd:decimal", "\"5.750000\"^^xsd:decimal", "\"6.800000\"^^xsd:decimal",
        "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getRoundExpectedValues() {
        return ImmutableList.of("\"0E-19, 43.0000000000000000000\"^^xsd:string",
                "\"0E-19, 23.0000000000000000000\"^^xsd:string",
                "\"0E-19, 34.0000000000000000000\"^^xsd:string",
                "\"0E-19, 10.0000000000000000000\"^^xsd:string");
    }

    @Disabled("not supported?")
    @Test
    public void testREGEX() {
        super.testREGEX();
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

    @Test
    @Disabled("TODO: support regex")
    @Override
    public void testIRI7() {
        super.testIRI7();
    }

    @Disabled("Current MS SQL Server handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }
}
