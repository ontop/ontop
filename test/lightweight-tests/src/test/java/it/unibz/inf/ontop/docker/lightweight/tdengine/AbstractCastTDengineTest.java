package it.unibz.inf.ontop.docker.lightweight.tdengine;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public abstract class AbstractCastTDengineTest extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/tdengine/functions.obda";

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

        executeAndCompareValues(query, ImmutableSet.of("\"0.0\"^^xsd:float"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastFloatFromDecimal1() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:float(?phase) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"0.23\"^^xsd:float", "\"0.23\"^^xsd:float",
                "\"0.25\"^^xsd:float", "\"0.33\"^^xsd:float"));
    }

    @Test
    public void testCastFloatFromDecimal2() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"0.0\"^^xsd:decimal) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"0.0\"^^xsd:float"));
    }

    @Test
    public void testCastFloatFromInteger() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:float(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"91214.0\"^^xsd:float"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"1.0\"^^xsd:float"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"2.0\"^^xsd:float"));
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
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:double(?phase) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"0.23\"^^xsd:double", "\"0.23\"^^xsd:double",
                "\"0.25\"^^xsd:double", "\"0.33\"^^xsd:double"));
    }

    @Test
    public void testCastDoubleFromInteger() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:double(\"91214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"91214.0\"^^xsd:double"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"1.0\"^^xsd:double"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"2.0\"^^xsd:double"));
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
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromFloat() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.6\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.6\"^^xsd:decimal"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromDouble() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:decimal"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromDecimal1() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:decimal(?phase) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"0.23\"^^xsd:decimal", "\"0.23\"^^xsd:decimal",
                "\"0.25\"^^xsd:decimal", "\"0.33\"^^xsd:decimal"));
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
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromInteger() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"19991214\"^^xsd:decimal"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"1.0\"^^xsd:decimal"));
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
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromString2() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2.0654\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0654\"^^xsd:decimal"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastDecimalFromString3() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:decimal(\"2\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2.0\"^^xsd:decimal"));
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
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:integer(?phase) AS ?v)\n"
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
    @Disabled("TDEngine does not support decimal type")
    public void testCastBooleanFromFloat() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"2.0654\"^^xsd:float) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:boolean"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastBooleanFromDouble() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"0.0\"^^xsd:double) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"false\"^^xsd:boolean"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastBooleanFromDecimal() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:boolean(?phase) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    @Disabled("TDEngine does not support decimal type")
    public void testCastBooleanFromInteger() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"19991214\"^^xsd:integer) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:boolean"));
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

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:boolean"));
    }

    @Test
    public void testCastBooleanFromString4() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:boolean(\"false\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"false\"^^xsd:boolean"));
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
                + "{  ?x ns:phase ?phase .\n"
                + "   BIND (xsd:string(?phase) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"0.23\"^^xsd:string", "\"0.23\"^^xsd:string",
                "\"0.25\"^^xsd:string", "\"0.33\"^^xsd:string"));
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
    @Disabled("Date type does not exists in TDEngine")
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
    @Disabled("Date type does not exists in TDEngine")
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
    @Disabled("Date type does not exists in TDEngine")
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

    @Test
    @Disabled("The timestamps don't include timezone information in TDEngine")
    public void testCastDateTimeFromDateTime1() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:instant ?year .\n"
                + "   BIND (xsd:dateTime(?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"2018-10-03T14:38:05.000\"^^xsd:dateTime",
                "\"2018-10-04T14:38:15.000\"^^xsd:dateTime", "\"2018-10-05T14:38:25.000\"^^xsd:dateTime",
                "\"2018-09-03T14:38:04.000\"^^xsd:dateTime", "\"2018-10-03T14:38:14.000\"^^xsd:dateTime"));
    }

    @Test
    public void testCastDateTimeFromDateTime2() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:dateTime) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14T09:30:00\"^^xsd:dateTime"));
    }

    //result: "1999-12-14 01:00:00.0"^^xsd:dateTime
    @Test
    @Disabled("The local timezone is used")
    public void testCastDateTimeFromDate1() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14 00:00:00\"^^xsd:dateTime"));
    }

    // result: "1999-12-14 01:00:00.0"^^xsd:dateTime
    @Test
    @Disabled("The local timezone is used")
    public void testCastDateTimeFromDate2() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14Z\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14 00:00:00\"^^xsd:dateTime"));
    }

    // result "1999-12-14 02:00:00.0"^^xsd:dateTime
    @Test
    @Disabled("The local timezone is used")
    public void testCastDateTimeFromDate3() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14+01:00\"^^xsd:date) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14 00:00:00\"^^xsd:dateTime"));
    }

    // result: "1999-12-14 10:30:00.0"^^xsd:dateTime
    @Test
    @Disabled("The local timezone is used")
    public void testCastDateTimeFromString() {
        String query = "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(xsd:dateTime(\"1999-12-14T09:30:00\"^^xsd:string) AS ?v )\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1999-12-14T09:30:00\"^^xsd:dateTime"));
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
