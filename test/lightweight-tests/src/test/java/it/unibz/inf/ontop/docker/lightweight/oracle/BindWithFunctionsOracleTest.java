package it.unibz.inf.ontop.docker.lightweight.oracle;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
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

@OracleLightweightTest
public class BindWithFunctionsOracleTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/oracle/books-oracle.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }


    /*
     * Tests for hash functions. Oracle does not support any hash functions if DBMS CRYPTO is not enabled
     */
    @Disabled("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashMd5() {
        super.testHashMd5();
    }

    @Disabled("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashSHA1() {
        super.testHashSHA1();
    }

    @Disabled("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashSHA256() {
        super.testHashSHA256();
    }

    @Disabled("Require DBMS CRYPTO to be enabled")
    @Test
    @Override
    public void testHashSHA512() {
        super.testHashSHA512();
    }

    @Disabled("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithBefore1() {
        super.testBindWithBefore1();
    }

    @Disabled("Find a way to distinguish empty strings and NULLs")
    @Test
    @Override
    public void testBindWithAfter1() {
        super.testBindWithAfter1();
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00.000000\"^^xsd:string",
                "\"2011-12-08T11:30:00.000000\"^^xsd:string",
                "\"2014-06-05T16:47:52.000000\"^^xsd:string",
                "\"2015-09-21T09:23:06.000000\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getDivideExpectedValues() {
        return ImmutableList.of("\"21.5\"^^xsd:decimal", "\"11.5\"^^xsd:decimal", "\"17\"^^xsd:decimal",
                "\"5\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getRoundExpectedValues() {
        return ImmutableList.of("\"0, 43\"^^xsd:string", "\"0, 23\"^^xsd:string", "\"0, 34\"^^xsd:string",
                "\"0, 10\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getDatatypeExpectedValues() {
        return ImmutableList.of("\"0.2\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.2\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Disabled("Currently Oracle does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }

}
