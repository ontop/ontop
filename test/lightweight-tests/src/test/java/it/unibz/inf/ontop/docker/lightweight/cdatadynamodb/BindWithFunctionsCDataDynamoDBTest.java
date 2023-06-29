package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
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
@CDataDynamoDBLightweightTest
public class BindWithFunctionsCDataDynamoDBTest extends AbstractBindTestWithFunctions {

    private static final String PROPERTIES_FILE = "/books/cdatadynamodb/books-cdatadynamodb.properties";
    private static final String OBDA_FILE = "/books/cdatadynamodb/books-cdatadynamodb.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    @Override
    //In CDataDynamoDB all numbers have type double.
    public void testDatatype() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (?discount AS ?v) WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   FILTER ( datatype(?discount) = xsd:double)\n"
                + "   }  ";

        executeAndCompareValues(query, getDatatypeExpectedValues());
    }

    @Test
    @Disabled("UUID is not yet supported")
    @Override
    public void testUuid() {
        super.testUuid();
    }

    @Test
    @Disabled("UUID is not yet supported")
    @Override
    public void testStrUuid() {
        super.testStrUuid();
    }

    @Test
    @Disabled("REGEX is not yet supported")
    @Override
    public void testREGEX() {
        super.testREGEX();
    }

    @Test
    @Disabled("REGEX is not yet supported")
    @Override
    public void testREPLACE() {
        super.testREPLACE();
    }

    @Test
    @Disabled("'week' datediff is not yet supported")
    @Override
    public void testWeeksBetweenDate() {
        super.testWeeksBetweenDate();
    }

    @Test
    @Disabled("'week' datediff is not yet supported")
    @Override
    public void testWeeksBetweenDateTime() {
        super.testWeeksBetweenDateTime();
    }

    @Test
    @Disabled("REGEX is not yet supported")
    @Override
    public void testIRI7() {
        super.testIRI7();
    }

    @Test
    @Disabled("ROW_NUMBER is not yet supported")
    @Override
    public void testBNODE0() {
        super.testBNODE0();
    }

    @Test
    @Disabled("ROW_NUMBER is not yet supported")
    @Override
    public void testBNODE1() {
        super.testBNODE1();
    }

    @Disabled("Dynamodb counts one day too few")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() {
        super.testDaysBetweenDateMappingInput();
    }

    @Disabled("Dynamodb counts one day too few")
    @Test
    @Override
    public void testSecondsBetweenMappingInput() {
        super.testSecondsBetweenMappingInput();
    }

    @Disabled("Dynamodb returns different hash value")
    @Test
    @Override
    public void testHashSHA384() {
        super.testHashSHA384();
    }

    /*-------Numeric type problems-------*/

    @Override
    protected ImmutableSet<String> getAbsExpectedValues() {
        return ImmutableSet.of("\"8.600000000000001\"^^xsd:double", "\"5.75\"^^xsd:double", "\"6.800000000000001\"^^xsd:double",
                "\"1.5\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getCeilExpectedValues() {
        return ImmutableList.of("\"1\"^^xsd:double", "\"1\"^^xsd:double", "\"1\"^^xsd:double", "\"1\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.500000\"^^xsd:decimal");
    }

    @Override
    protected ImmutableMultiset<String> getDatatypeExpectedValues() {
        return ImmutableMultiset.of("\"0.2\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.2\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Override
    protected ImmutableList<String> getFloorExpectedValues() {
        return ImmutableList.of("\"0\"^^xsd:double", "\"0\"^^xsd:double", "\"0\"^^xsd:double", "\"0\"^^xsd:double");
    }

    @Override
    @Test
    public void testIn1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.15\"^^xsd:decimal) \n"
                + "         (\"0.25\"^^xsd:decimal) } \n"
                + "   FILTER(?v IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.15\"^^xsd:double",
                "\"0.25\"^^xsd:double"));
    }

    @Override
    @Test
    public void testIn2() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   FILTER(?v IN (\"0.15\"^^xsd:decimal, \n"
                + "                     \"0.25\"^^xsd:decimal)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.15\"^^xsd:double",
                "\"0.25\"^^xsd:double"));
    }

    @Override
    @Test
    public void testNotIn1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.20\"^^xsd:decimal) } \n"
                + "   FILTER(?v NOT IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of( "\"0.15\"^^xsd:double", "\"0.25\"^^xsd:double"));
    }

    @Override
    @Test
    public void testNotIn2() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?v .\n"
                + "   FILTER(?v NOT IN (\"0.20\"^^xsd:decimal)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.15\"^^xsd:double", "\"0.25\"^^xsd:double"));
    }


    /*-------Date/Time type problems-------*/

    @Override
    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00+01:00\"^^xsd:string", "\"2011-12-08T11:30:00+01:00\"^^xsd:string",
                "\"2014-06-05T16:47:52+02:00\"^^xsd:string", "\"2015-09-21T09:23:06+02:00\"^^xsd:string");
    }
}
