package it.unibz.inf.ontop.docker.lightweight.oracle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */

@OracleLightweightTest
public class BindWithFunctionsOracleTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/oracle/books-oracle.properties";

    @BeforeAll
    public static void before() {
        initOBDA("/books/oracle/books-oracle.obda", OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
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
    public void testHashSHA384() {
        super.testHashSHA384();
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
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.5\"^^xsd:decimal", "\"11.5\"^^xsd:decimal", "\"17\"^^xsd:decimal",
                "\"5\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getDatatypeExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.2\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Disabled("Currently Oracle does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }

    @Override
    protected ImmutableSet<String> getDivisionOutputTypeExpectedResults() {
        return ImmutableSet.of("\"3.33333333333333333333333333333333333333E00\"^^xsd:decimal");
    }

    @Disabled("Currently Oracle does not support DATE_TRUNC for the type `DECADE`")
    @Test
    @Override
    public void testDateTruncGroupBy() {
        super.testDateTruncGroupBy();
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000000+01:00\"^^xsd:dateTime", "\"2011-01-01T00:00:00.000000+01:00\"^^xsd:dateTime", "\"2014-01-01T00:00:00.000000+01:00\"^^xsd:dateTime", "\"2015-01-01T00:00:00.000000+01:00\"^^xsd:dateTime");
    }
}
