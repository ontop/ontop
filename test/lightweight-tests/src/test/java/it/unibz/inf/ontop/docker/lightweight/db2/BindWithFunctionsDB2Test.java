package it.unibz.inf.ontop.docker.lightweight.db2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;


/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
@DB2LightweightTest
public class BindWithFunctionsDB2Test extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/db2/books-db2.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
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
    public void testHashSHA384() {
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

    @Disabled("CAST AS DECIMAL in DB2 gives default precision 0")
    @Test
    @Override
    public void testIn1() {
    }

    @Disabled("CAST AS DECIMAL in DB2 gives default precision 0")
    @Test
    @Override
    public void testIn2() {
    }

    @Disabled("CAST AS DECIMAL in DB2 gives default precision 0")
    @Test
    @Override
    public void testNotIn1() {
    }

    @Disabled("CAST AS DECIMAL in DB2 gives default precision 0")
    @Test
    @Override
    public void testNotIn2() {
    }

    @Override
    protected ImmutableSet<String> getDivideExpectedValues() {
        return ImmutableSet.of("\"21.2500000000000000000000000\"^^xsd:decimal",
                "\"11.5000000000000000000000000\"^^xsd:decimal",
                "\"16.7500000000000000000000000\"^^xsd:decimal",
                "\"5.0000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.50000000000000000000000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.5000\"^^xsd:decimal", "\"5.7500\"^^xsd:decimal", "\"6.7000\"^^xsd:decimal",
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
    protected ImmutableSet<String> getRoundExpectedValues() {
        return ImmutableSet.of("\"0.00, 43.00\"^^xsd:string", "\"0.00, 23.00\"^^xsd:string",
                "\"0.00, 34.00\"^^xsd:string", "\"0.00, 10.00\"^^xsd:string");
    }

    @Override
    protected ImmutableMultiset<String> getSecondsExpectedValues() {
        return ImmutableMultiset.of("\"52.000000\"^^xsd:decimal", "\"0.000000\"^^xsd:decimal", "\"6.000000\"^^xsd:decimal",
                "\"0.000000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableSet<String> getExtraDateExtractionsExpectedValues() {
        return ImmutableSet.of("\"3 21 201 2 23 52000.000000 52000000\"^^xsd:string", "\"3 21 201 4 49 0.000000 0\"^^xsd:string",
                "\"3 21 201 3 39 6000.000000 6000000\"^^xsd:string", "\"2 20 197 4 45 0.000000 0\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getDateTruncGroupByExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000000: 1\"^^xsd:string", "\"2010-01-01T00:00:00.000000: 3\"^^xsd:string");
    }

    @Override
    protected ImmutableSet<String> getSimpleDateTrunkExpectedValues() {
        return ImmutableSet.of("\"1970-01-01T00:00:00.000000\"^^xsd:dateTime", "\"2011-01-01T00:00:00.000000\"^^xsd:dateTime", "\"2014-01-01T00:00:00.000000\"^^xsd:dateTime", "\"2015-01-01T00:00:00.000000\"^^xsd:dateTime");
    }

    @Disabled("DB2 struggles with the division included in the query as it seems to interpret it as a division by 0.")
    @Test
    @Override
    public void testStatisticalAggregates() {
        super.testStatisticalAggregates();
    }

    @Override
    protected ImmutableSet<String> getStatisticalAttributesExpectedResults() {
        return ImmutableSet.of("\"207.00000000000000000000000000\"^^xsd:decimal");
    }
}
