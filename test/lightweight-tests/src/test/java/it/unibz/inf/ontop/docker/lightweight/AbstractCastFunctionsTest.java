package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMultiset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public abstract class AbstractCastFunctionsTest extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/books/books.obda";
    protected static final String OWL_FILE = "/books/books.owl";

    @Test
    public void testCastFloatFromFloat() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:float"));
    }

    @Test
    public void testCastFloatFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromDoubleExpectedValues());
    }

    protected ImmutableSet<String> getCastFloatFromDoubleExpectedValues() {
        return ImmutableSet.of("\"0.0\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromDecimal1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:float(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromDecimal1ExpectedValues());
    }

    protected ImmutableMultiset<String> getCastFloatFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.20\"^^xsd:float", "\"0.25\"^^xsd:float", "\"0.20\"^^xsd:float",
                "\"0.15\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromDecimal2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromDecimal2ExpectedValues());
    }

    protected ImmutableSet<String> getCastFloatFromDecimal2ExpectedValues() {
        return ImmutableSet.of("\"0.0\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromIntegerExpectedValues());
    }

    protected ImmutableSet<String> getCastFloatFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214.0\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastFloatFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastFloatFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromBooleanExpectedValues());
    }

    protected ImmutableSet<String> getCastFloatFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1.0\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x dc:title ?title . "
                + " BIND(xsd:float(?title) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastFloatFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:float"));
    }

    @Test
    public void testCastFloatFromString3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastFloatFromString3ExpectedValues());
    }

    protected ImmutableSet<String> getCastFloatFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.0\"^^xsd:float");
    }

    @Test
    public void testCastFloatFromString4() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDoubleFromFloat() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.6\"^^xsd:double"));
    }

    @Test
    public void testCastDoubleFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0.0\"^^xsd:double"));
    }

    @Test
    public void testCastDoubleFromDecimal() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:double(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastDoubleFromDecimalExpectedValues());
    }

    protected ImmutableMultiset<String> getCastDoubleFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"0.20\"^^xsd:double", "\"0.25\"^^xsd:double", "\"0.20\"^^xsd:double",
                "\"0.15\"^^xsd:double");
    }

    @Test
    public void testCastDoubleFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDoubleFromIntegerExpectedValues());
    }

    protected ImmutableSet<String> getCastDoubleFromIntegerExpectedValues() {
        return ImmutableSet.of("\"91214.0\"^^xsd:double");
    }

    @Test
    public void testCastDoubleFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDoubleFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDoubleFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDoubleFromBooleanExpectedValues());
    }

    protected ImmutableSet<String> getCastDoubleFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1.0\"^^xsd:double");
    }

    @Test
    public void testCastDoubleFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:double"));
    }

    @Test
    public void testCastDoubleFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDoubleFromString2ExpectedValues());
    }

    protected ImmutableSet<String> getCastDoubleFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.0\"^^xsd:double");
    }

    @Test
    public void testCastDoubleFromString3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDecimalFromFloat() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromFloatExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromFloatExpectedValues() {
        return ImmutableSet.of("\"2.6\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromDoubleExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromDoubleExpectedValues() {
        return ImmutableSet.of("\"2.0654\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromDecimal1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:decimal(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromDecimal1ExpectedValues());
    }

    protected ImmutableMultiset<String> getCastDecimalFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.20\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.20\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromDecimal2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0.0\"^^xsd:decimal"));
    }

    @Test
    public void testCastDecimalFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromIntegerExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromIntegerExpectedValues() {
        return ImmutableSet.of("\"19991214\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDecimalFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDecimalFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromBooleanExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromBooleanExpectedValues() {
        return ImmutableSet.of("\"1.0\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x dc:title ?title . "
                + " BIND(xsd:decimal(?title) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDecimalFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromString2ExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromString2ExpectedValues() {
        return ImmutableSet.of("\"2.0654\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromString3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDecimalFromString3ExpectedValues());
    }

    protected ImmutableSet<String> getCastDecimalFromString3ExpectedValues() {
        return ImmutableSet.of("\"2.0\"^^xsd:decimal");
    }

    @Test
    public void testCastDecimalFromString4() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastIntegerFromFloat1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromFloat2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromFloat3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"-2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"-2\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromDecimal1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:integer(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"0\"^^xsd:integer", "\"0\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromDecimal2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0\"^^xsd:integer"));
    }


    @Test
    public void testCastIntegerFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"19991214\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastIntegerFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastIntegerFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"

                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x dc:title ?title . "
                + " BIND(xsd:integer(?title) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastIntegerFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromString3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"2.5\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2\"^^xsd:integer"));
    }

    @Test
    public void testCastIntegerFromString4() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:integer(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastBooleanFromFloat() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromFloatExpectedValues());
    }

    protected ImmutableSet<String> getCastBooleanFromFloatExpectedValues() {
        return ImmutableSet.of("\"true\"^^xsd:boolean");
    }

    @Test
    public void testCastBooleanFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromDoubleExpectedValues());
    }

    protected ImmutableSet<String> getCastBooleanFromDoubleExpectedValues() {
        return ImmutableSet.of("\"false\"^^xsd:boolean");
    }

    @Test
    public void testCastBooleanFromDecimal() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:boolean(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromDecimalExpectedValues());
    }

    protected ImmutableMultiset<String> getCastBooleanFromDecimalExpectedValues() {
        return ImmutableMultiset.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean");
    }

    @Test
    public void testCastBooleanFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromIntegerExpectedValues());
    }

    protected ImmutableSet<String> getCastBooleanFromIntegerExpectedValues() {
        return ImmutableSet.of("\"true\"^^xsd:boolean");
    }

    @Test
    public void testCastBooleanFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastBooleanFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastBooleanFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:boolean"));
    }

    @Test
    public void testCastBooleanFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE {\n"
                + " ?x dc:title ?title . "
                + " BIND(xsd:boolean(?title) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }


    @Test
    public void testCastBooleanFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastBooleanFromString3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"1\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromString3ExpectedValues());
    }

    protected ImmutableSet<String> getCastBooleanFromString3ExpectedValues() {
        return ImmutableSet.of("\"true\"^^xsd:boolean");
    }

    @Test
    public void testCastBooleanFromString4() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"false\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastBooleanFromString4ExpectedValues());
    }

    protected ImmutableSet<String> getCastBooleanFromString4ExpectedValues() {
        return ImmutableSet.of("\"false\"^^xsd:boolean");
    }

    @Test
    public void testCastStringFromFloat() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0.0\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromDecimal1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (xsd:string(?discount) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastStringFromDecimal1ExpectedValues());
    }

    protected ImmutableMultiset<String> getCastStringFromDecimal1ExpectedValues() {
        return ImmutableMultiset.of("\"0.20\"^^xsd:string", "\"0.25\"^^xsd:string", "\"0.20\"^^xsd:string",
                "\"0.15\"^^xsd:string");
    }

    @Test
    public void testCastStringFromDecimal2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"19991214\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromDateTime() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14T09:30:00\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromBoolean() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"true\"^^xsd:boolean) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"abc\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"abc\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromIRI() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"http://www.w3.org/2001/XMLSchema#string\") AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"http://www.w3.org/2001/XMLSchema#string\"^^xsd:string"));
    }

    @Test
    public void testCastStringFromLiteral() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:string(\"abc\"^^rdfs:literal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"abc\"^^xsd:string"));
    }

    @Test
    public void testCastDateFromDateTime1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (xsd:date(?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2014-06-05\"^^xsd:date", "\"2011-12-08\"^^xsd:date",
                "\"2015-09-21\"^^xsd:date", "\"1970-11-05\"^^xsd:date"));
    }

    @Test
    public void testCastDateFromDateTime2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14\"^^xsd:date"));
    }

    @Test
    public void testCastDateFromDate() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14\"^^xsd:date"));
    }

    @Test
    public void testCastDateFromString1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"1999-12-14\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14\"^^xsd:date"));
    }

    @Test
    public void testCastDateFromString2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"199912-14\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDateFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDateFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:date(\"19991214.12\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Disabled("Timezones are system specific")
    @Test
    public void testCastDateTimeFromDateTime1() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (xsd:dateTime(?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, getCastDateTimeFromDateTime1ExpectedValues());
    }

    protected ImmutableSet<String> getCastDateTimeFromDateTime1ExpectedValues() {
        return ImmutableSet.of("\"2014-06-05 16:47:52+02\"^^xsd:dateTime",
                "\"2011-12-08 11:30:00+01\"^^xsd:dateTime",
                "\"2015-09-21 09:23:06+02\"^^xsd:dateTime",
                "\"1970-11-05 07:50:00+01\"^^xsd:dateTime");
    }

    @Test
    public void testCastDateTimeFromDateTime2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14T09:30:00\"^^xsd:dateTime"));
    }

    @Test
    public void testCastDateTimeFromDate1() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDateTimeFromDate1ExpectedValues());
    }

    protected ImmutableSet<String> getCastDateTimeFromDate1ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00\"^^xsd:dateTime");
    }

    @Test
    public void testCastDateTimeFromDate2() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14Z\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDateTimeFromDate2ExpectedValues());
    }

    protected ImmutableSet<String> getCastDateTimeFromDate2ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T00:00:00\"^^xsd:dateTime");
    }

    @Test
    public void testCastDateTimeFromDate3() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14+01:00\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDateTimeFromDate3ExpectedValues());
    }

    protected ImmutableSet<String> getCastDateTimeFromDate3ExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T01:00:00\"^^xsd:dateTime");
    }

    @Test
    public void testCastDateTimeFromString() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, getCastDateTimeFromStringExpectedValues());
    }

    protected ImmutableSet<String> getCastDateTimeFromStringExpectedValues() {
        return ImmutableSet.of("\"1999-12-14T09:30:00\"^^xsd:dateTime");
    }

    @Test
    public void testCastDateTimeFromInteger() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }

    @Test
    public void testCastDateTimeFromDouble() {

        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"19991214.12\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of());
    }
}
