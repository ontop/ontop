package it.unibz.inf.ontop.docker.lightweight.db2;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@DB2LightweightTest
public class BindWithFunctionsDB2Test extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/db2/books-db2.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testHashMd5() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testHashSHA1() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testHashSHA256() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testHashSHA512() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Disabled("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Override
    protected ImmutableList<String> getDivideExpectedValues() {
        return ImmutableList.of("\"21.2500000000000000000000000\"^^xsd:decimal",
                "\"11.5000000000000000000000000\"^^xsd:decimal",
                "\"16.7500000000000000000000000\"^^xsd:decimal",
                "\"5.0000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.50000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getAbsExpectedValues() {
        return ImmutableList.of("\"8.5000\"^^xsd:decimal", "\"5.7500\"^^xsd:decimal", "\"6.7000\"^^xsd:decimal",
                "\"1.5000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00.000000\"^^xsd:string",
                "\"2011-12-08T11:30:00.000000\"^^xsd:string",
                "\"2014-06-05T16:47:52.000000\"^^xsd:string",
                "\"2015-09-21T09:23:06.000000\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getRoundExpectedValues() {
        return ImmutableList.of("\"0.00, 43.00\"^^xsd:string", "\"0.00, 23.00\"^^xsd:string",
                "\"0.00, 34.00\"^^xsd:string", "\"0.00, 10.00\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getSecondsExpectedValues() {
        return ImmutableList.of("\"52.000000\"^^xsd:decimal", "\"0.000000\"^^xsd:decimal", "\"6.000000\"^^xsd:decimal",
                "\"0.000000\"^^xsd:decimal");
    }
}
