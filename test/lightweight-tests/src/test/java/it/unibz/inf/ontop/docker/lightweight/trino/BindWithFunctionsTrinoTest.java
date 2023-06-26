package it.unibz.inf.ontop.docker.lightweight.trino;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.TrinoLightweightTest;
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
@TrinoLightweightTest
public class BindWithFunctionsTrinoTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/trino/books-trino.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000000000000000000000000000000\"^^xsd:decimal", "\"5.750000000000000000000000000000000000\"^^xsd:decimal", "\"6.800000000000000000000000000000000000\"^^xsd:decimal",
                "\"1.500000000000000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.500000000000000000\"^^xsd:decimal", "\"11.500000000000000000\"^^xsd:decimal",
                "\"17.000000000000000000\"^^xsd:decimal", "\"5.000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05 07:50:00.000000\"^^xsd:string", "\"2011-12-08 11:30:00.000000\"^^xsd:string",
                "\"2014-06-05 16:47:52.000000\"^^xsd:string", "\"2015-09-21 09:23:06.000000\"^^xsd:string");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000000000000000\"^^xsd:decimal");
    }

    @Disabled("Trino counts one hour less on two results")
    @Test
    @Override
    public void testSecondsBetweenMappingInput() {
        super.testSecondsBetweenMappingInput();
    }

    @Disabled("Since Trino does not have unique constraint information, a 'DISTINCT' must be enforced. This DISTINCT" +
            "causes the remaining query to be packed into a sub-query, including the 'ORDER BY'. Selecting from sub" +
            "queries does not conserve order in Trino, so while the results are correct, they are in the wrong order")
    @Test
    @Override
    public void testREPLACE() {
        super.testREPLACE();
    }

    @Disabled("Trino does not support SHA384")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

}
