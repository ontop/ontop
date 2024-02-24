package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/***
 * Class to test if cast functions in SPARQL are working properly.
 */
public class CastPrimitiveDatatypesTest extends AbstractRDF4JTest {

    private static final String OBDA_FILE = "/cast/cast.obda";
    private static final String SQL_SCRIPT = "/cast/cast-schema.sql";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCastFloatFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastFloatFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0654"));
    }

    @Test
    public void testCastFloatFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastFloatFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }

    @Test
    public void testCastFloatFromDouble4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"1.0E500\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("Infinity"));
    }

    @Test
    public void testCastFloatFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastFloatFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }

    @Test
    public void testCastFloatFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0", "0.0", "0.0", "0.0", "5.0", "7.0"));
    }

    @Test
    public void testCastFloatFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("91214.0"));
    }

    @Test
    public void testCastFloatFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastFloatFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastFloatFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastFloatFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastFloatFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0", "0.0", "0.0", "0.0", "1.0", "1.0"));
    }

    @Test
    public void testCastFloatFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.0"));
    }

    @Test
    public void testCastFloatFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:float(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.0", "2.0", "3.0"));
    }

    @Test
    public void testCastFloatFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0654"));
    }

    @Test
    public void testCastFloatFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0"));
    }

    @Test
    public void testCastFloatFromString4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDoubleFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastDoubleFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.6"));
    }

    @Test
    public void testCastDoubleFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastDoubleFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }

    @Test
    public void testCastDoubleFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastDoubleFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }

    @Test
    public void testCastDoubleFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0", "0.0", "0.0", "0.0", "5.0", "7.0"));
    }

    @Test
    public void testCastDoubleFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("91214.0"));
    }

    @Test
    public void testCastDoubleFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDoubleFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDoubleFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDoubleFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDoubleFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0", "0.0", "0.0", "0.0", "1.0", "1.0"));
    }

    @Test
    public void testCastDoubleFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.0"));
    }

    @Test
    public void testCastDoubleFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:double(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.0", "2.0", "3.0"));
    }

    @Test
    public void testCastDoubleFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0654"));
    }

    @Test
    public void testCastDoubleFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0"));
    }

    @Test
    public void testCastDoubleFromString4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.100000", "0.200000", "0.300000", "0.040000", "5.050000", "6.660000"));
    }

    @Test
    public void testCastDecimalFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.600000"));
    }

    @Test
    public void testCastDecimalFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.100000", "0.200000", "0.300000", "0.040000", "5.050000", "6.660000"));
    }

    @Test
    public void testCastDecimalFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.065400"));
    }

    @Test
    public void testCastDecimalFromDouble3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2E10\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.100000", "0.200000", "0.300000", "0.040000", "5.050000", "6.660000"));
    }

    @Test
    public void testCastDecimalFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }


    @Test
    public void testCastDecimalFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.000000", "0.000000", "0.000000", "0.000000", "5.000000", "7.000000"));
    }

    @Test
    public void testCastDecimalFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("19991214.000000"));
    }

    @Test
    public void testCastDecimalFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDecimalFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0", "0.0", "0.0", "0.0", "1.0", "1.0"));
    }

    @Test
    public void testCastDecimalFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.0"));
    }

    @Test
    public void testCastDecimalFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:decimal(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.000000", "2.000000", "3.000000"));
    }

    @Test
    public void testCastDecimalFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.065400"));
    }

    @Test
    public void testCastDecimalFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.000000"));
    }

    @Test
    public void testCastDecimalFromString4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastIntegerFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "5", "6"));
    }

    @Test
    public void testCastIntegerFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2"));
    }

    @Test
    public void testCastIntegerFromFloat3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2"));
    }

    @Test
    public void testCastIntegerFromFloat4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"-2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("-2"));
    }

    @Test
    public void testCastIntegerFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "5", "6"));
    }

    @Test
    public void testCastIntegerFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0"));
    }

    @Test
    public void testCastIntegerFromDouble3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0"));
    }

    @Test
    public void testCastIntegerFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "5", "6"));
    }

    @Test
    public void testCastIntegerFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0"));
    }

    @Test
    public void testCastIntegerFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "5", "7"));
    }

    @Test
    public void testCastIntegerFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("19991214"));
    }

    @Test
    public void testCastIntegerFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastIntegerFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastIntegerFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastIntegerFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastIntegerFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "1", "1"));
    }

    @Test
    public void testCastIntegerFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1"));
    }

    @Test
    public void testCastIntegerFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1", "2", "3"));
    }

    @Test
    public void testCastIntegerFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2"));
    }

    @Test
    public void testCastIntegerFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.5\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2"));
    }

    @Test
    public void testCastIntegerFromString4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true", "true", "true", "true", "true", "true"));
    }

    @Test
    public void testCastBooleanFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }

    @Test
    public void testCastBooleanFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true", "true", "true", "true", "true", "true"));
    }

    @Test
    public void testCastBooleanFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false"));
    }

    @Test
    public void testCastBooleanFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true", "true", "true", "true", "true", "true"));
    }

    @Test
    public void testCastBooleanFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false"));
    }

    @Test
    public void testCastBooleanFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false", "false", "false", "false", "true", "true"));
    }

    @Test
    public void testCastBooleanFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }

    @Test
    public void testCastBooleanFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:integer(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false", "false", "false", "false", "true", "true"));
    }

    @Test
    public void testCastBooleanFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }

    @Test
    public void testCastBooleanFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:boolean(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }


    @Test
    public void testCastBooleanFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastBooleanFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }


    @Test
    public void testCastBooleanFromString4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"false\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false"));
    }

    @Test
    public void testCastStringFromFloat1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:float_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastStringFromFloat2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0654"));
    }

    @Test
    public void testCastStringFromDouble1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:double_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastStringFromDouble2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0"));
    }

    //NOTE: H2 does not support the Mantissa
    @Test
    public void testCastStringFromDouble3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1000001.0\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1000001.0"));
    }

    @Test
    public void testCastStringFromDecimal1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:decimal_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0.1", "0.2", "0.3", "0.04", "5.05", "6.66"));
    }

    @Test
    public void testCastStringFromDecimal2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0"));
    }

    @Test
    public void testCastStringFromDecimal3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1.5\"^^xsd:decimal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1.5"));
    }

    @Test
    public void testCastStringFromInteger1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:integer_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("0", "0", "0", "0", "5", "7"));
    }

    @Test
    public void testCastStringFromInteger2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("19991214"));
    }

    @Ignore("Handling of timezones varies based on local system")
    @Test
    public void testCastStringFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10T09:00:00.000+01:00", "1998-12-10T09:15:00.000+01:00",
                "1997-12-13T11:00:06.000+01:00"));
    }

    @Test
    public void testCastStringFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14T09:30:00"));
    }

    @Test
    public void testCastStringFromDate() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10", "1998-12-10", "1997-12-13"));
    }

    @Test
    public void testCastStringFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14"));
    }

    @Test
    public void testCastStringFromBoolean1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:boolean_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("false", "false", "false", "false", "true", "true"));
    }

    @Test
    public void testCastStringFromBoolean2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("true"));
    }

    @Test
    public void testCastStringFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?rating . "
                + " BIND(xsd:string(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("A", "B", "C", "1", "2", "3"));
    }

    @Test
    public void testCastStringFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("2.0654"));
    }

    @Test
    public void testCastStringFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("abc"));
    }

    @Test
    public void testCastStringFromIRI() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"http://www.w3.org/2001/XMLSchema#string\") AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("http://www.w3.org/2001/XMLSchema#string"));
    }

    @Test
    public void testCastStringFromLiteral() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"abc\"^^rdfs:literal) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("abc"));
    }

    @Ignore("H2 limitations in possible datetime pattern casts and datetime pattern checks")
    @Test
    public void testCastDateFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:date(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10", "1998-12-10", "1997-12-13"));
    }


    @Test
    public void testCastDateFromDateTime2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(query, ImmutableList.of("1999-12-14"));
    }

    @Test
    public void testCastDateFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:date(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10", "1998-12-10", "1997-12-13"));
    }

    @Test
    public void testCastDateFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14"));
    }


    @Test
    public void testCastDateFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datestring_label ?rating . "
                + " BIND(xsd:date(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10", "1998-12-10", "1997-12-13"));
    }


    @Test
    public void testCastDateFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14"));
    }

    @Test
    public void testCastDateFromString3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"199912-14\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDateFromInteger() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDateFromDouble() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"19991214.12\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }


    @Ignore("Handling of timezones varies based on local system")
    @Test
    public void testCastDateTimeFromDateTime1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetime_label ?rating . "
                + " BIND(xsd:dateTime(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10T09:00:00.000+01:00", "1998-12-10T09:15:00.000+01:00",
                "1997-12-13T11:00:06.000+01:00"));
    }


    @Test
    public void testCastDateTimeFromDateTime2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14T09:30:00"));
    }


    @Ignore("Query result retrieves local system timezone")
    @Test
    public void testCastDateTimeFromDate1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:date_label ?rating . "
                + " BIND(xsd:dateTime(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10 00:00:00", "1998-12-10 00:00:00",
                "1997-12-13 00:00:00"));
    }


    @Ignore("Query result retrieves local system timezone")
    @Test
    public void testCastDateTimeFromDate2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14 00:00:00"));
    }

    @Ignore("H2 limitations in possible datetime pattern casts and datetime pattern checks")
    @Test
    public void testCastDateTimeFromDate3() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14Z\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14T00:00:00+00:00"));
    }

    @Ignore("H2 limitations in possible datetime pattern casts and datetime pattern checks")
    @Test
    public void testCastDateTimeFromDate4() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14+01:00\"^^xsd:date) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14T00:00:00+01:00"));
    }


    @Ignore("H2 limitations in possible datetime pattern casts and datetime pattern checks")
    @Test
    public void testCastDateTimeFromString1() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:datetimestring_label ?rating . "
                + " BIND(xsd:dateTime(?rating) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-10 11:00:00+03", "1998-12-10 11:15:00+03",
                "1997-12-13 11:00:06"));
    }


    @Ignore("Query result retrieves local system timezone")
    @Test
    public void testCastDateTimeFromString2() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:string) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1999-12-14 09:30:00"));
    }

    @Test
    public void testCastDateTimeFromInteger() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testCastDateTimeFromDouble() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"19991214.12\"^^xsd:double) AS ?v )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of());
    }

    @Test
    public void testDatatypeCast() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x a ex:Individual . "
                + " ?x ex:string_label ?v . "
                + " FILTER(DATATYPE(xsd:integer(?v)) = xsd:integer )\n"
                + "}";

        runQueryAndCompare(queryBind, ImmutableList.of("1", "2", "3"));
    }

    @Test
    public void testCoalesceCast() {

        String queryBind = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX ex: <http://example.org/>\n"
                + "SELECT ?x (AVG(IF(ISNUMERIC(?string_rating), ?string_rating, COALESCE(xsd:integer(?string_rating), 0))) AS ?v) WHERE {\n"
                + " ?x ex:string_label ?string_rating . "
                + "}\n"
                + "GROUP BY ?x";

        runQueryAndCompare(queryBind, ImmutableList.of("0.0000000000", "0.0000000000", "0.0000000000", "1.0000000000",
                "2.0000000000", "3.0000000000"));
    }
}