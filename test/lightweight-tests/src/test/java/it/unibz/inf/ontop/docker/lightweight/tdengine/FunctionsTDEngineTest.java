package it.unibz.inf.ontop.docker.lightweight.tdengine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.*;

public class FunctionsTDEngineTest extends AbstractDockerRDF4JTest {
    private static final String PROPERTIES_FILE = "/tdengine/functions.properties";
    private static final String MAPPING_FILE = "/tdengine/functions.obda";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(MAPPING_FILE, null, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testAndBind() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   ?x dc:location ?loc .\n"
                + "   BIND((CONTAINS(?desc,\"sensor\") && CONTAINS(?loc,\"Monica\")) AS ?v)\n"
                + "} ORDER BY ?desc \n";

        executeAndCompareValues(query, ImmutableList.of("\"true\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    @Disabled
    public void testAndBindDistinct() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT DISTINCT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   ?x dc:location ?loc .\n"
                + "   BIND((CONTAINS(?desc,\"sensor\") && CONTAINS(?loc,\"Monica\")) AS ?v)\n"
                + "}\n";

        executeAndCompareValues(query, ImmutableSet.of("\"true\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testOrBind() {
        String query = "PREFIX dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   ?x dc:location ?loc .\n"
                + "   BIND((CONTAINS(?desc,\"sensor\") || CONTAINS(?loc,\"Monica\")) AS ?v)\n"
                + "} ORDER BY ?desc \n";

        executeAndCompareValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }


    /*
     * Tests for numeric functions
     */
    @Test
    public void testCeil() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:current ?curr .\n"
                + "   BIND (CEIL(?curr) AS ?v)\n"
                + "} ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"11.0\"^^xsd:double", "\"11.0\"^^xsd:double",
                "\"11.0\"^^xsd:double","\"13.0\"^^xsd:double","\"13.0\"^^xsd:double"));
    }

    @Test
    public void testFloor() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:current ?curr .\n"
                + "   BIND (FLOOR(?curr) AS ?v)\n"
                + "} ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"10.0\"^^xsd:double", "\"10.0\"^^xsd:double",
                "\"10.0\"^^xsd:double", "\"12.0\"^^xsd:double","\"12.0\"^^xsd:double"));
    }

    @Test
    public void testRound() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:voltage ?volt .\n"
                + "   ?x ns:current ?curr .\n"
                + "   BIND (CONCAT(STR(ROUND(?volt)),', ',STR(ROUND(?curr))) AS ?v)\n"
                + "} ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"218, 10.0\"^^xsd:string", "\"218, 13.0\"^^xsd:string",
                "\"220, 10.0\"^^xsd:string", "\"220, 10.0\"^^xsd:string", "\"221, 12.0\"^^xsd:string"));
    }


    @Test
    public void testAbs() {
        String query = "PREFIX  ns: <http://example.org/ns#>\n"
        + "SELECT ?v WHERE \n"
                + "{  ?x ns:voltage ?volt .\n"
                + "   ?x ns:current ?curr .\n"
                + "   ?x ns:phase ?phase .\n"
                + "   BIND (ROUND(ABS((?curr - ?volt*?phase) - ?curr))  AS ?v)\n"
                + "} ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"51.0\"^^xsd:double", "\"51.0\"^^xsd:double", "\"55.0\"^^xsd:double",
                "\"72.0\"^^xsd:double"));
    }


    /*
     * Tests for hash functions.
     */

    @Test
    @Disabled("SHA256() is not supported in TDEngine")
    public void testHashSHA256() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc, \"second\"))\n"
                + "   BIND (SHA256(str(?desc)) AS ?v)\n"
                + "}";

        String hexString = new DigestUtils(SHA_256).digestAsHex("second sensor first measure");
        executeAndCompareValues(query, ImmutableList.of(String.format("\"%s\"^^xsd:string", hexString)));
    }

    @Test
    public void testHashMd5() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc, \"second\"))\n"
                + "   BIND (MD5(str(?desc)) AS ?v)\n"
                + "}";

        String hexString = new DigestUtils(MD5).digestAsHex("second sensor first measure");
        executeAndCompareValues(query, ImmutableList.of(String.format("\"%s\"^^xsd:string", hexString)));
    }

    @Test
    @Disabled("SHA1() is not supported in TDEngine")
    public void testHashSHA1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc, \"second\"))\n"
                + "   BIND (SHA1(str(?desc)) AS ?v)\n"
                + "}";

        String hexString = new DigestUtils(SHA_1).digestAsHex("second sensor first measure");
        executeAndCompareValues(query, ImmutableList.of(String.format("\"%s\"^^xsd:string", hexString)));
    }

    @Test
    @Disabled("SHA384() is not supported in TDEngine")
    public void testHashSHA384() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc, \"second\"))\n"
                + "   BIND (SHA384(str(?desc)) AS ?v)\n"
                + "}";

        String hexString = new DigestUtils(SHA_384).digestAsHex("second sensor first measure");
        executeAndCompareValues(query, ImmutableList.of(String.format("\"%s\"^^xsd:string", hexString)));
    }

    @Test
    @Disabled("SHA512() is not supported in TDEngine")
    public void testHashSHA512() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc, \"second\"))\n"
                + "   BIND (SHA512(str(?desc)) AS ?v)\n"
                + "}";

        String hexString = new DigestUtils(SHA_512).digestAsHex("second sensor first measure");
        executeAndCompareValues(query, ImmutableList.of(String.format("\"%s\"^^xsd:string", hexString)));
    }

    /*
     * Tests for functions on strings.
     */

    @Test
    public void testStrLen() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (STRLEN(?desc) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"7\"^^xsd:integer", "\"13\"^^xsd:integer", "\"27\"^^xsd:integer"));
    }

    //test substring with 2 parameters
    @Test
    public void testSubstr2() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (SUBSTR(?desc, 3) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"cond sensor first measure\"^^xsd:string", "\"nsor1\"^^xsd:string", "\"st sensor 1\"^^xsd:string"));
    }

    //test substring with 3 parameters
    @Test
    public void testSubstr3() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (SUBSTR(?desc, 3, 6) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"cond s\"^^xsd:string", "\"nsor1\"^^xsd:string", "\"st sen\"^^xsd:string"));
    }

    @Test
    public void testURIEncoding() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   FILTER (STRSTARTS(?desc,\"test\"))\n"
                + "   BIND (ENCODE_FOR_URI(?desc) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"test%20sensor%201\"^^xsd:string"));
    }

    @Test
    public void testStrEnds() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND(?desc AS ?v)\n"
                + "   FILTER(STRENDS(?desc,\"1\"))\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"sensor1\"^^xsd:string", "\"test sensor 1\"^^xsd:string"));
    }

    @Test
    public void testStrSubstring() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND(SUBSTR(?desc,1,STRLEN(?desc)) AS ?v)\n"
                + "   FILTER(STRSTARTS(?desc,\"test\"))\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"test sensor 1\"^^xsd:string"));
    }

    @Test
    public void testContainsBind() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND(CONTAINS(?desc,\"first\") AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testContainsFilter() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND(?desc AS ?v)\n"
                + "   FILTER(CONTAINS(?desc,\"first\"))\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"second sensor first measure\"^^xsd:string"));
    }

    @Test
    public void testBindWithUcase() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (UCASE(?desc) AS ?w)\n"
                + "   BIND (CONCAT(?desc, \" \", ?w) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"sensor1 SENSOR1\"^^xsd:string",
                "\"test sensor 1 TEST SENSOR 1\"^^xsd:string", "\"second sensor first measure SECOND SENSOR FIRST MEASURE\"^^xsd:string"));
    }

    @Test
    public void testBindWithLcase() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (LCASE(?desc) AS ?w)\n"
                + "   BIND (CONCAT(?desc, \" \", ?w) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"sensor1 sensor1\"^^xsd:string",
                "\"test sensor 1 test sensor 1\"^^xsd:string", "\"second sensor first measure second sensor first measure\"^^xsd:string"));
    }

    @Test
    public void testBindWithBefore1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (STRBEFORE(?desc,\"measure\") AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableMultiset.of("\"second sensor first \"^^xsd:string",
                "\"\"^^xsd:string", "\"\"^^xsd:string"));
    }

    @Test
    public void testBindWithBefore2() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (STRBEFORE(?desc,\"\") AS ?v)\n"
                + "}";
        executeAndCompareValues(query, ImmutableList.of("\"\"^^xsd:string", "\"\"^^xsd:string", "\"\"^^xsd:string"));
    }

    @Test
    public void testBindWithAfter1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (STRAFTER(?desc,\"sensor \") AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"first measure\"^^xsd:string","\"1\"^^xsd:string", "\"\"^^xsd:string"));
    }

    @Test
    public void testBindWithAfter2() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "   BIND (STRAFTER(?desc,\"\") AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"second sensor first measure\"^^xsd:string",
                "\"sensor1\"^^xsd:string", "\"test sensor 1\"^^xsd:string"));
    }

    /*
     * Tests for functions on date and time
     */

    @Test
    public void testNow() {
        String query = "SELECT ?v WHERE \n"
                + "{  BIND (NOW() AS ?v)\n"
                + "}";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    @Disabled("UUID() is not supported in TDEngine")
    public void testUuid() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (UUID() AS ?v) WHERE \n"
                + "{  ?x ns:current ?c .\n"
                + "}";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    @Disabled("UUID() is not supported in TDEngine")
    public void testStrUuid() {
        String query = "SELECT (STRUUID() AS ?v) WHERE { }";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testRand() {
        String query = "SELECT (RAND() AS ?v) WHERE { }";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    @Disabled("TZ() is not supported in TDEngine")
    public void testTZ() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (TZ(?year) AS ?v) WHERE \n"
                + "{  ?x ns:instant ?year .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string",
                "\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string"));
    }

    @Test
    @Disabled("Decimal type not supported")
    public void testDivide() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:voltage ?p .\n"
                + "   BIND ((?p / 2) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"110.00000\"^^xsd:decimal", "\"109.00000\"^^xsd:decimal",
                "\"110.50000\"^^xsd:decimal", "\"110.000000\"^^xsd:decimal", "\"109.000000\"^^xsd:decimal"));
    }

    @Test
    public void testBound() {
        String query = "PREFIX  dc: <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns: <http://example.org/ns#>\n"
                + "SELECT (BOUND(?desc) AS ?v) WHERE \n"
                + "{  ?x ns:voltage ?volt .\n"
                + "   OPTIONAL{ \n"
                + "     ?x dc:description ?desc .\n"
                + "     #FILTER (STRSTARTS(?desc, \"s\"))\n"
                + "   } \n"
                + "} ORDER BY ?desc";

        executeAndCompareValues(query, ImmutableMultiset.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    @Disabled("VALUES clause is not supported in TDEngine")
    public void testIn1() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:phase ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.23\"^^xsd:decimal) \n"
                + "         (\"0.25\"^^xsd:decimal) } \n"
                + "   FILTER(?v IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.23\"^^xsd:decimal",
                "\"0.23\"^^xsd:decimal"));
    }

    @Test
    public void testIn2() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:phase ?v .\n"
                + "   FILTER(?v IN (\"0.23\"^^xsd:double, \"0.25\"^^xsd:double)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.23\"^^xsd:double",
                "\"0.23\"^^xsd:double", "\"0.25\"^^xsd:double"));
    }

    @Test
    @Disabled("VALUES clause is not supported in TDEngine")
    public void testNotIn1() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:phase ?v .\n"
                + "   VALUES (?w) { \n"
                + "         (\"0.23\"^^xsd:decimal) } \n"
                + "   FILTER(?v NOT IN (?w)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of( "\"0.25\"^^xsd:decimal", "\"0.33\"^^xsd:decimal"));
    }

    @Test
    public void testNotIn2() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ns:phase ?v .\n"
                + "   FILTER(?v NOT IN (\"0.23\"^^xsd:double)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"0.25\"^^xsd:double", "\"0.33\"^^xsd:double"));
    }

    @Test
    @Disabled("OFFSET must be used with LIMIT")
    public void testOffset1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?v . }\n"
                + "ORDER BY ASC(?v) \n"
                + "OFFSET 2";

        executeAndCompareValues(query, ImmutableList.of("\"test sensor 1\"^^xsd:string"));
    }

    @Test
    public void testOffset2() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:description ?v . }\n"
                + "ORDER BY ASC(?v) \n"
                + "OFFSET 1  \n"
                + "LIMIT 1";

        executeAndCompareValues(query, ImmutableList.of("\"sensor1\"^^xsd:string"));
    }

    @Test
    public void testIsIRI() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT (isIRI(?desc) AS ?v) WHERE \n"
                + "{  ?x dc:description ?desc .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testIsBlank() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isBlank(?flow) AS ?v) WHERE \n"
                + "{  ?x ns:flow ?flow .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testIsLiteral() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT (isLiteral(?desc) AS ?v) WHERE {\n"
                + "  ?x dc:description ?desc .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testIsNumeric() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isNumeric(?flow) AS ?v) WHERE \n"
                + "{  ?x ns:flow ?flow .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testStr() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (str(?year) AS ?v) WHERE \n"
                + "{ ?x ns:instant ?year . }\n"
                + "ORDER BY ?year ";

        executeAndCompareValues(query, ImmutableList.of("\"2018-09-03T14:38:04.000\"^^xsd:string", "\"2018-10-03T14:38:05.000\"^^xsd:string",
                "\"2018-10-03T14:38:14.000\"^^xsd:string", "\"2018-10-04T14:38:15.000\"^^xsd:string", "\"2018-10-05T14:38:25.000\"^^xsd:string"));
    }

    @Test
    public void testLang() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (lang(?loc) AS ?v) WHERE \n"
                + "{  ?x a ns:WaterMeasurement .\n"
                + "   ?x dc:location ?loc .\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"en\"^^xsd:string", "\"en\"^^xsd:string",
                "\"en\"^^xsd:string"));
    }

    @Test
    public void testConcat() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?location,\" | \", ?description) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x a ns:ElectricityMeasurement .\n"
                + "   ?x dc:location ?location .\n"
                + "   ?x dc:description ?description .\n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"San Francisco | sensor1\"^^xsd:string",
                "\"San Francisco | test sensor 1\"^^xsd:string",
                "\"Santa Monica | second sensor first measure\"^^xsd:string"));
    }

    @Test
    public void testConcatNullable() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?location,\" | \", ?description) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x a ns:ElectricityMeasurement .\n"
                + "   ?x dc:location ?location .\n"
                + "   OPTIONAL {?x dc:description ?description .}\n"
                + "   } ORDER BY ?v";

        executeAndCompareValues(query, ImmutableList.of("\"San Francisco | sensor1\"^^xsd:string",
                "\"San Francisco | test sensor 1\"^^xsd:string",
                "\"Santa Monica | second sensor first measure\"^^xsd:string"));
    }

    @Test
    public void testLangMatches() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?loc) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x a ns:WaterMeasurement .\n"
                + "   ?x dc:location ?loc .\n"
                + "   FILTER(langMatches( lang(?loc), \"EN\" )) \n"
                + "   } ORDER BY ?loc";

        executeAndCompareValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    @Disabled("REGEX functions not yet supported in TDEngine")
    public void testREGEX() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?desc) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x a ns:WaterMeasurement .\n"
                + "   OPTIONAL{\n"
                + "     ?x dc:description ?desc .\n"
                + "     FILTER(REGEX( ?desc, \"sensor\" )) \n"
                + "   } } ORDER BY ?desc";

        executeAndCompareValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    @Disabled("REGEX functions not yet supported in TDEngine")
    public void testREPLACE() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT  ?v WHERE \n"
                + "{  \n"
                + "   ?x dc:description ?desc .\n"
                + "   BIND(REPLACE(?desc, \" \", \"\") AS ?v) .\n"
                + "   } ORDER BY ?desc";

        executeAndCompareValues(query, ImmutableList.of("\"secondsensorfirstmeasure\"^^xsd:string",
                "\"sensor1\"^^xsd:string", "\"testsensor1\"^^xsd:string"));
    }

    @Test
    public void testConstantFloatDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:float AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatIntegerDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1\"^^xsd:integer AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatDecimalDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:decimal AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatDoubleDivide() {
        String query = "SELECT (\"1.0\"^^xsd:float / \"2.0\"^^xsd:double AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:double"));
    }

    @Test
    public void testConstantDoubleDoubleDivide() {
        String query = "SELECT (\"1.0\"^^xsd:double / \"2.0\"^^xsd:double AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:double"));
    }

    @Test
    @Disabled("Decimal type not supported")
    public void testConstantIntegerDivide() {
        String query = "SELECT (\"1\"^^xsd:integer / \"2\"^^xsd:integer AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"0.5\"^^xsd:decimal"));
    }

    @Test
    public void testCoalesceDivideByZeroInt() {
        String query = "SELECT (COALESCE(\"1\"^^xsd:integer / \"0\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceDivideByZeroDecimal() {
        String query = "SELECT (COALESCE(\"1\"^^xsd:decimal / \"0\"^^xsd:decimal, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide1() {
        String query = "SELECT (COALESCE(\"rrr\" / \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide2() {
        String query = "SELECT (COALESCE(\"2\"^^xsd:integer / \"rrr\", \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSum() {
        String query = "SELECT (COALESCE(\"rrr\" + \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSub() {
        String query = "SELECT (COALESCE(\"rrr\" - \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidTimes() {
        String query = "SELECT (COALESCE(\"rrr\" * \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Disabled("TODO: support it, by using a case")
    @Test
    public void testDivideByZeroFloat() {
        String query = "SELECT (\"1\"^^xsd:integer / \"0.0\"^^xsd:float AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"INF\"^^xsd:float"));
    }

    @Test
    @Disabled("ROW_NUMBER not supported in TDEngine")
    public void testBNODE0() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?v WHERE \n"
                + "{  ?x ns:voltage ?p .\n"
                + "   BIND (BNODE() AS ?b)\n"
                + "   BIND (\"cst\" AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"cst\"^^xsd:string", "\"cst\"^^xsd:string", "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string"));
    }

    @Test
    @Disabled("ROW_NUMBER not supported in TDEngine")
    public void testBNODE1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?v WHERE \n"
                + "{  ?x ns:voltage ?p .\n"
                + "   BIND (BNODE(\"b1\") AS ?b)\n"
                + "   BIND (\"cst\" AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"cst\"^^xsd:string", "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string", "\"cst\"^^xsd:string"));
    }

    @Test
    public void testIRI1() {
        String query = "SELECT ?v  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI1_2() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI2() {
        String query = "SELECT ?v  {" +
                "BIND(IRI(<http://example.org/john>) AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI3() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI4() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(URI(\"john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI5() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"urn:john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<urn:john>"));
    }

    @Test
    public void testIRI6() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"mailto:john@somewhere.org\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<mailto:john@somewhere.org>"));
    }

    @Test
    @Disabled("VALUES clause is not supported in TDEngine")
    public void testIRI7() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "{ VALUES ?w { \"john\" \"ernest\" \"http://example.org/alice\" } } UNION { BIND (str(rand()) AS ?w) } \n" +
                "BIND(IRI(?w) AS ?v)\n" +
                "VALUES ?y { <http://example.org/john> <http://otherdomain.org/ernest> } \n" +
                "FILTER (?v = ?y)\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI8() {
        String query = "BASE <http://example.org/project1#data/>\n" +
                "SELECT ?v {" +
                "BIND(IRI(\"john\") AS ?v)\n" +
                "} ";

        executeAndCompareValues(query, ImmutableList.of("<http://example.org/project1#data/john>"));
    }

    @Test
    public void testIF1() {
        String query = "SELECT (COALESCE(IF(\"rrr\" * \"2\"^^xsd:integer, \"1\", \"2\"), \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testIF2() {
        String query = "SELECT (IF(1 < 2, \"first\", \"second\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"first\"^^xsd:string"));
    }

    @Test
    public void testIF3() {
        String query = "SELECT (IF(1 > 2, \"first\", \"second\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"second\"^^xsd:string"));
    }

    @Test
    public void testIF4() {
        String query = "SELECT (COALESCE(IF(1 < 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testIF5() {
        String query = "SELECT (COALESCE(IF(1 > 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"second\"^^xsd:string"));
    }

    @Test
    public void testIF6() {
        String query = "SELECT (COALESCE(IF(1 > 2, \"first\", \"rrr\" * \"2\"^^xsd:integer), \"other\") AS ?v)  {} ";

        executeAndCompareValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testWeeksBetweenDate() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"3538\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testDaysBetweenDate() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"24767\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testWeeksBetweenDateTime() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"3538\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testDaysBetweenDateTime() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"24766\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testDaysBetweenDateTimeMappingInput() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:instant ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"17270\"^^xsd:long", "\"16360\"^^xsd:long", "\"17742\"^^xsd:long",
                "\"1351\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testDaysBetweenDateMappingInput() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:instant ?year .\n"
                + "   BIND(\"1967-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"17270\"^^xsd:long", "\"16360\"^^xsd:long", "\"17743\"^^xsd:long",
                "\"1352\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testHoursBetween() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:hoursBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"594407\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testMinutesBetween() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:minutesBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"35664450\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testSecondsBetween() {
        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"2139867000\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testSecondsBetweenMappingInput() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x ns:instant ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\"1492154272\"^^xsd:long", "\"1413511200\"^^xsd:long",
                "\"1532994786\"^^xsd:long", "\"116806800\"^^xsd:long"));
    }

    @Test
    @Disabled("ofn functions are not yet supported")
    public void testMilliSecondsBetween() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1999-12-13T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:millisBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareValues(query, ImmutableList.of("\"84600000\"^^xsd:long"));
    }

    @Test
    @Disabled("Decimal type not yet supported")
    public void testStatisticalAggregates() {
        String query = "PREFIX  ns:  <http://example.org/ns#>\n "
                + "PREFIX agg: <http://jena.apache.org/ARQ/function/aggregate#>\n"
                + "SELECT (ROUND((agg:stdev_pop(?p) + agg:var_samp(?p)) / 0.01) / 100.0  AS ?v) WHERE \n"
                + "{  ?x ns:voltage ?p .\n"
                + "}";

        executeAndCompareValues(query, ImmutableSet.of("\215.34\"^^xsd:decimal"));
    }
}
