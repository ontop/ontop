package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/***
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 */

public abstract class AbstractBindTestWithFunctions extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/books/books.obda";
    protected static final String OWL_FILE = "/books/books.owl";

    @Test
    public void testAndBind() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") && CONTAINS(?title,\"Web\")) AS ?v)\n"
                + "}\n" +
                "ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testAndBindDistinct() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT DISTINCT ?title ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") && CONTAINS(?title,\"Web\")) AS ?v)\n"
                + "}\n" +
                "ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testOrBind() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT DISTINCT ?title ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") || CONTAINS(?title,\"Book\")) AS ?v)\n"
                + "}\n"
                + "ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }


    /*
	 * Tests for numeric functions
    */


    @Test
    public void testCeil() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (CEIL(?discount) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getCeilExpectedValues());
    }

    protected ImmutableList<String> getCeilExpectedValues() {
        return ImmutableList.of("\"1\"^^xsd:decimal", "\"1\"^^xsd:decimal", "\"1\"^^xsd:decimal", "\"1\"^^xsd:decimal");
    }


    @Test
    public void testFloor() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   BIND (FLOOR(?discount) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getFloorExpectedValues());
    }

    protected ImmutableList<String> getFloorExpectedValues() {
        return ImmutableList.of("\"0\"^^xsd:decimal", "\"0\"^^xsd:decimal", "\"0\"^^xsd:decimal", "\"0\"^^xsd:decimal");
    }


    @Test
    public void testRound() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   BIND (CONCAT(STR(ROUND(?discount)),', ',STR(ROUND(?p))) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getRoundExpectedValues());
    }

    protected ImmutableList<String> getRoundExpectedValues() {
        return ImmutableList.of("\"0.0, 43.0\"^^xsd:string", "\"0.0, 23.0\"^^xsd:string", "\"0.0, 34.0\"^^xsd:string",
        "\"0.0, 10.0\"^^xsd:string");
    }

    @Test
    public void testAbs() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   BIND (ABS((?p - ?discount*?p) - ?p)  AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getAbsExpectedValues());
    }

    protected ImmutableList<String> getAbsExpectedValues() {
        return ImmutableList.of("\"8.6\"^^xsd:decimal", "\"5.75\"^^xsd:decimal", "\"6.8\"^^xsd:decimal",
        "\"1.5\"^^xsd:decimal");
    }

    /*
	 * Tests for hash functions.
    */

    @Test
    public void testHashSHA256() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA256(str(?title)) AS ?v)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest("The Semantic Web".getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        executeAndCompareLexicalValues(query, ImmutableList.copyOf(expectedValues));

    }

    @Test
    public void testHashMd5() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (MD5(str(?title)) AS ?v)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest("The Semantic Web".getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        executeAndCompareLexicalValues(query, ImmutableList.copyOf(expectedValues));
    }

    @Test
    public void testHashSHA1() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA1(str(?title)) AS ?v)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest("The Semantic Web".getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        executeAndCompareLexicalValues(query, ImmutableList.copyOf(expectedValues));
    }

    @Disabled
    @Test
    public void testHashSHA384() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA384(str(?title)) AS ?v)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest("The Semantic Web".getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        executeAndCompareLexicalValues(query, ImmutableList.copyOf(expectedValues));
    }

    @Test
    public void testHashSHA512() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA512(str(?title)) AS ?v)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest("The Semantic Web".getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        executeAndCompareLexicalValues(query, ImmutableList.copyOf(expectedValues));
    }

    /*
	 * Tests for functions on strings.
    */

    @Test
    public void testStrLen() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (STRLEN(?title) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"15\"^^xsd:integer", "\"16\"^^xsd:integer", "\"20\"^^xsd:integer",
                "\"44\"^^xsd:integer"));
    }

    //test substring with 2 parameters
    @Test
    public void testSubstr2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"ARQL Tutorial\"@en", "\"e Semantic Web\"@en",
                "\"ime and Punishment\"@en", "\"e Logic Book: Introduction, Second Edition\"@en"));
    }

    //test substring with 3 parameters
    @Test
    public void testSubstr3() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3, 6) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"ARQL T\"@en", "\"e Sema\"@en", "\"ime an\"@en", "\"e Logi\"@en"));
    }
    @Test
    public void testURIEncoding() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title,\"The\"))\n"
                + "   BIND (ENCODE_FOR_URI(?title) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The%20Semantic%20Web\"^^xsd:string",
                "\"The%20Logic%20Book%3A%20Introduction%2C%20Second%20Edition\"^^xsd:string"));
    }



    @Test
    public void testStrEnds() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND(?title AS ?v)\n"
                + "   FILTER(STRENDS(?title,\"b\"))\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Semantic Web\"@en"));
    }

    @Test
    public void testStrStarts() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND(?title AS ?v)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Semantic Web\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testStrSubstring() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND(SUBSTR(?title,1,STRLEN(?title)) AS ?v)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Semantic Web\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testContainsBind() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND(CONTAINS(?title,\"Semantic\") AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testContainsFilter() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND(?title AS ?v)\n"
                + "   FILTER(CONTAINS(?title,\"Semantic\"))\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Semantic Web\"@en"));
    }


    @Test
    public void testBindWithUcase() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?w)\n"
                + "   BIND (CONCAT(?title, \" \", ?w) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"SPARQL Tutorial SPARQL TUTORIAL\"^^xsd:string",
                "\"The Semantic Web THE SEMANTIC WEB\"^^xsd:string",
                "\"Crime and Punishment CRIME AND PUNISHMENT\"^^xsd:string",
                "\"The Logic Book: Introduction, Second Edition " +
                        "The Logic Book: Introduction, Second Edition\"".toUpperCase()+"^^xsd:string"));
    }

    @Test
    public void testBindWithLcase() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (LCASE(?title) AS ?w)\n"
                + "   BIND (CONCAT(?title, \" \", ?w) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"SPARQL Tutorial sparql tutorial\"^^xsd:string",
                "\"The Semantic Web the semantic web\"^^xsd:string",
                "\"Crime and Punishment crime and punishment\"^^xsd:string",
                "\"The Logic Book: Introduction, Second Edition " +
                        "The Logic Book: Introduction, Second Edition\"".toLowerCase()+"^^xsd:string"));
    }



    @Test
    public void testBindWithBefore1() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\"ti\") AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getBindWithBefore1ExpectedValues());
    }

    protected ImmutableList<String> getBindWithBefore1ExpectedValues() {
        return ImmutableList.of("\"\"^^xsd:string", "\"The Seman\"@en", "\"\"^^xsd:string",
                "\"The Logic Book: Introduc\"@en");
    }

    @Test
    public void testBindWithBefore2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\"\") AS ?v)\n"
                + "}";
        executeAndCompareLexicalValues(query, getBindWithBefore2ExpectedValues());
    }

    protected ImmutableList<String> getBindWithBefore2ExpectedValues() {
        return ImmutableList.of("\"\"@en", "\"\"@en", "\"\"@en", "\"\"@en");
    }

    @Test
    public void testBindWithAfter1() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"The\") AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getBindWithAfter1ExpectedValues());
    }

    protected ImmutableList<String> getBindWithAfter1ExpectedValues() {
        return ImmutableList.of("\"\"^^xsd:string", "\" Semantic Web\"@en", "\"\"^^xsd:string",
        "\" Logic Book: Introduction, Second Edition\"@en");
    }

    @Test
    public void testBindWithAfter2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"\") AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getBindWithAfter2ExpectedValues());
    }

    protected ImmutableList<String> getBindWithAfter2ExpectedValues() {
        return ImmutableList.of("\"SPARQL Tutorial\"@en", "\"The Semantic Web\"@en", "\"Crime and Punishment\"@en",
        "\"The Logic Book: Introduction, Second Edition\"@en");
    }


    /*
	 * Tests for functions on date and time
    */


    @Test
    public void testMonth() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (MONTH(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"6\"^^xsd:integer","\"12\"^^xsd:integer", "\"9\"^^xsd:integer",
                "\"11\"^^xsd:integer"));
    }

    @Test
    public void testYear() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (YEAR(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"2014\"^^xsd:integer", "\"2011\"^^xsd:integer",
                "\"2015\"^^xsd:integer", "\"1970\"^^xsd:integer"));
    }

    @Test
    public void testDay() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (DAY(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"5\"^^xsd:integer", "\"8\"^^xsd:integer", "\"21\"^^xsd:integer",
                "\"5\"^^xsd:integer"));
    }

    @Test
    public void testMinutes() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (MINUTES(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"47\"^^xsd:integer", "\"30\"^^xsd:integer", "\"23\"^^xsd:integer",
                "\"50\"^^xsd:integer"));
    }

    @Test
    public void testHours() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND (HOURS(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"16\"^^xsd:integer", "\"11\"^^xsd:integer",
                "\"9\"^^xsd:integer", "\"7\"^^xsd:integer"));
    }

    @Test
    public void testSeconds() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x ns:pubYear ?year .\n"
                + "   BIND (SECONDS(?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getSecondsExpectedValues());
    }

    protected ImmutableList<String> getSecondsExpectedValues() {
        return ImmutableList.of("\"52\"^^xsd:decimal", "\"0\"^^xsd:decimal", "\"6\"^^xsd:decimal",
                "\"0\"^^xsd:decimal");
    }


    @Test
    public void testNow() {

        String query = "SELECT ?v WHERE \n"
                + "{  BIND (NOW() AS ?v)\n"
                + "}";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
    public void testUuid() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (UUID() AS ?v) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "}";

        int count = runQueryAndCount(query);
        Assertions.assertTrue(count > 0);
    }

    @Test
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
    public void testDivide() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   BIND ((?p / 2) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, getDivideExpectedValues());
    }

    protected ImmutableList<String> getDivideExpectedValues() {
        return ImmutableList.of("\"21.500000\"^^xsd:decimal", "\"11.500000\"^^xsd:decimal",
                "\"17.000000\"^^xsd:decimal", "\"5.000000\"^^xsd:decimal");
    }

    @Disabled
    @Test
    public void testTZ() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (TZ(?year) AS ?v) WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string",
                "\"00:00\"^^xsd:string", "\"00:00\"^^xsd:string"));
    }

    @Test
    public void testBound() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (BOUND(?title) AS ?v) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   OPTIONAL{ \n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER (STRSTARTS(?title, \"T\"))\n"
                + "   } \n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }


    /**
     * Currently equalities between lang strings are treated as RDFTermEqual.
     *
     * Therefore != is always false or null (which corresponds to false under 2VL)
     *
     * THIS COULD CHANGE IN THE FUTURE as we could extend the SPARQL spec
     * (TODO: see how other systems behave)
     */
    @Test
    public void testRDFTermEqual1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?title,\" | \",?title2) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER (?discount = ?discount2 && ?title != ?title2)\n"
                + "   } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of());
    }

    @Test
    public void testRDFTermEqual2() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (CONCAT(?title,\" | \",?title2) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER (?discount = ?discount2 && str(?title) != str(?title2))\n"
                + "   } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment | SPARQL Tutorial\"^^xsd:string",
                "\"SPARQL Tutorial | Crime and Punishment\"^^xsd:string"));
    }

    @Test
    public void testSameTerm() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (CONCAT(?title,\" | \",?title2) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER(sameTerm(?discount, ?discount2) && !sameTerm(?title, ?title2))\n"
                + "   } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment | SPARQL Tutorial\"^^xsd:string",
                "\"SPARQL Tutorial | Crime and Punishment\"^^xsd:string"));
    }

    @Test
    public void testIn() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x ?p ?v .\n"
                + "   VALUES (?chosentitles) { \n"
                + "         (\"Crime and Punishment\"@en) \n"
                + "         (\"SPARQL Tutorial\"@en) } \n"
                + "   FILTER(?v IN (?chosentitles)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment\"@en",
                "\"SPARQL Tutorial\"@en"));
    }

    @Test
    public void testIn2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x dc:title ?v .\n"
                + "   FILTER(?v IN (\"Crime and Punishment\"@en, \n"
                + "                     \"SPARQL Tutorial\"@en)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment\"@en",
                "\"SPARQL Tutorial\"@en"));
    }

    @Disabled
    @Test
    public void testNotIn1() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x dc:title ?v .\n"
                + "   VALUES (?chosentitles) { \n"
                + "         (\"Crime and Punishment\"@en) \n"
                + "         (\"SPARQL Tutorial\"@en) } \n"
                + "   FILTER(?v NOT IN (?chosentitles)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Logic Book: Introduction, Second Edition\"@en",
        "\"The Semantic Web\"@en"));
    }

    @Disabled
    @Test
    public void testNotIn2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  \n"
                + "   ?x dc:title ?v .\n"
                + "   FILTER(?v NOT IN (\"Crime and Punishment\"@en, \n"
                + "                     \"SPARQL Tutorial\"@en)) \n"
                + "   } ORDER BY ?v";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Logic Book: Introduction, Second Edition\"@en",
                "\"The Semantic Web\"@en"));
    }

    @Test
    public void testOffset1() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?v . }\n"
                + "ORDER BY ASC(?v) \n"
                + "OFFSET 2";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Logic Book: Introduction, Second Edition\"@en",
                "\"The Semantic Web\"@en"));
    }

    @Test
    public void testOffset2() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x dc:title ?v . }\n"
                + "ORDER BY ASC(?v) \n"
                + "OFFSET 2  \n"
                + "LIMIT 1";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testIsIRI() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT (isIRI(?title) AS ?v) WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testIsBlank() {

        /*String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isBlank(?discount) AS ?v) WHERE \n"
                + "{ ?x ns:discount [a ?discount .\n"
                + "  ?x ns:discount [a ?discount .\n"
                + "}";
        ?s :child [a :Person] .*/
        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isBlank(?discount) AS ?v) WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testIsLiteral() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isLiteral(?discount) AS ?v) WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testIsNumeric() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isNumeric(?discount) AS ?v) WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testStr() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (str(?year) AS ?v) WHERE \n"
                + "{ ?x ns:pubYear ?year . }\n"
                + "ORDER BY ?year ";

        executeAndCompareLexicalValues(query, getStrExpectedValues());
    }

    protected ImmutableList<String> getStrExpectedValues() {
        return ImmutableList.of("\"1970-11-05T07:50:00\"^^xsd:string", "\"2011-12-08T11:30:00\"^^xsd:string",
        "\"2014-06-05T16:47:52\"^^xsd:string", "\"2015-09-21T09:23:06\"^^xsd:string");
    }

    @Test
    public void testLang() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT (lang(?title) AS ?v) WHERE \n"
                + "{  ?x dc:title ?title .\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"en\"^^xsd:string", "\"en\"^^xsd:string",
                "\"en\"^^xsd:string", "\"en\"^^xsd:string"));
    }

    //In SPARQL 1.0, the DATATYPE function was not defined for literals with a language tag
    @Test
    public void testDatatype() {

        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (?discount AS ?v) WHERE \n"
                + "{  ?x ns:discount ?discount .\n"
                + "   FILTER ( datatype(?discount) = xsd:decimal)\n"
                + "   }  ";

        executeAndCompareLexicalValues(query, getDatatypeExpectedValues());
    }

    protected ImmutableList<String> getDatatypeExpectedValues() {
        return ImmutableList.of("\"0.20\"^^xsd:decimal", "\"0.25\"^^xsd:decimal", "\"0.20\"^^xsd:decimal",
                "\"0.15\"^^xsd:decimal");
    }

    @Test
    public void testConcat() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT  (CONCAT(?title,\" | \", ?description) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x dc:title ?title .\n"
                + "   ?x dc:description ?description .\n"
                + "   } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment | good\"^^xsd:string",
                "\"SPARQL Tutorial | good\"^^xsd:string",
                "\"The Logic Book: Introduction, Second Edition | good\"^^xsd:string",
                "\"The Semantic Web | bad\"^^xsd:string"));
    }


    @Test
    public void testLangMatches() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?title) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   OPTIONAL{\n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER(langMatches( lang(?title), \"EN\" )) \n"
                + "   } } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }


    @Test
    public void testREGEX() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?title) AS ?v) WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   OPTIONAL{\n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER(REGEX( ?title, \"Semantic\" )) \n"
                + "   } } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"false\"^^xsd:boolean", "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean", "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testREPLACE() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "SELECT  ?v WHERE \n"
                + "{  \n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(REPLACE(?title, \"Second\", \"First\") AS ?v) .\n"
                + "   } ORDER BY ?title";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"Crime and Punishment\"@en", "\"SPARQL Tutorial\"@en",
                "\"The Logic Book: Introduction, First Edition\"@en", "\"The Semantic Web\"@en"));
    }

    @Test
    public void testConstantFloatDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:float AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantFloatDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantFloatDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:float");
    }

    @Test
    public void testConstantFloatIntegerDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1\"^^xsd:integer AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantFloatIntegerDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantFloatIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:float");
    }

    @Test
    public void testConstantFloatDecimalDivide() {
        String query = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:decimal AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantFloatDecimalDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantFloatDecimalDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:float");
    }

    @Test
    public void testConstantFloatDoubleDivide() {
        String query = "SELECT (\"1.0\"^^xsd:float / \"2.0\"^^xsd:double AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantFloatDoubleDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantFloatDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:double");
    }

    @Test
    public void testConstantDoubleDoubleDivide() {
        String query = "SELECT (\"1.0\"^^xsd:double / \"2.0\"^^xsd:double AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantDoubleDoubleDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantDoubleDoubleDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:double");
    }

    @Test
    public void testConstantIntegerDivide() {
        String query = "SELECT (\"1\"^^xsd:integer / \"2\"^^xsd:integer AS ?v)  {} ";

        executeAndCompareLexicalValues(query, getConstantIntegerDivideExpectedResults());
    }

    protected ImmutableList<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of("\"0.5\"^^xsd:decimal");
    }

    @Test
    public void testCoalesceDivideByZeroInt() {
        String query = "SELECT (COALESCE(\"1\"^^xsd:integer / \"0\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceDivideByZeroDecimal() {
        String query = "SELECT (COALESCE(\"1\"^^xsd:decimal / \"0\"^^xsd:decimal, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide1() {
        String query = "SELECT (COALESCE(\"rrr\" / \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide2() {
        String query = "SELECT (COALESCE(\"2\"^^xsd:integer / \"rrr\", \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSum() {
        String query = "SELECT (COALESCE(\"rrr\" + \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSub() {
        String query = "SELECT (COALESCE(\"rrr\" - \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidTimes() {
        String query = "SELECT (COALESCE(\"rrr\" * \"2\"^^xsd:integer, \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Disabled("TODO: support it, by using a case")
    @Test
    public void testDivideByZeroFloat() {
        String query = "SELECT (\"1\"^^xsd:integer / \"0.0\"^^xsd:float AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"INF\"^^xsd:float"));
    }

    @Test
    public void testBNODE0() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?v WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   BIND (BNODE() AS ?b)\n"
                + "   BIND (\"cst\" AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"cst\"^^xsd:string", "\"cst\"^^xsd:string", "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string"));
    }

    @Test
    public void testBNODE1() {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?v WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   BIND (BNODE(\"b1\") AS ?b)\n"
                + "   BIND (\"cst\" AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"cst\"^^xsd:string", "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string", "\"cst\"^^xsd:string"));
    }

    @Test
    public void testIRI1() {
        String query = "SELECT ?v  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI1_2() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI2() {
        String query = "SELECT ?v  {" +
                "BIND(IRI(<http://example.org/john>) AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI3() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI4() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(URI(\"john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI5() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"urn:john\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<urn:john>"));
    }

    @Test
    public void testIRI6() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "BIND(IRI(\"mailto:john@somewhere.org\") AS ?v)\n" +
                "FILTER (isIRI(?v))\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<mailto:john@somewhere.org>"));
    }

    @Test
    public void testIRI7() {
        String query = "BASE <http://example.org/>\n" +
                "SELECT ?v  {" +
                "{ VALUES ?w { \"john\" \"ernest\" \"http://example.org/alice\" } } UNION { BIND (str(rand()) AS ?w) } \n" +
                "BIND(IRI(?w) AS ?v)\n" +
                "VALUES ?y { <http://example.org/john> <http://otherdomain.org/ernest> } \n" +
                "FILTER (?v = ?y)\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/john>"));
    }

    @Test
    public void testIRI8() {
        String query = "BASE <http://example.org/project1#data/>\n" +
                "SELECT ?v {" +
                "BIND(IRI(\"john\") AS ?v)\n" +
                "} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("<http://example.org/project1#data/john>"));
    }

    @Test
    public void testIF1() {
        String query = "SELECT (COALESCE(IF(\"rrr\" * \"2\"^^xsd:integer, \"1\", \"2\"), \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testIF2() {
        String query = "SELECT (IF(1 < 2, \"first\", \"second\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"first\"^^xsd:string"));
    }

    @Test
    public void testIF3() {
        String query = "SELECT (IF(1 > 2, \"first\", \"second\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"second\"^^xsd:string"));
    }

    @Test
    public void testIF4() {
        String query = "SELECT (COALESCE(IF(1 < 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testIF5() {
        String query = "SELECT (COALESCE(IF(1 > 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"second\"^^xsd:string"));
    }

    @Test
    public void testIF6() {
        String query = "SELECT (COALESCE(IF(1 > 2, \"first\", \"rrr\" * \"2\"^^xsd:integer), \"other\") AS ?v)  {} ";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"other\"^^xsd:string"));
    }

    @Test
    public void testWeeksBetweenDate() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"3538\"^^xsd:long"));
    }

    @Test
    public void testDaysBetweenDate() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"24767\"^^xsd:long"));
    }

    @Test
    public void testWeeksBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"3538\"^^xsd:long"));
    }

    @Test
    public void testDaysBetweenDateTime() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"24766\"^^xsd:long"));
    }

    @Test
    public void testDaysBetweenDateTimeMappingInput() {


        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"17270\"^^xsd:long", "\"16360\"^^xsd:long", "\"17742\"^^xsd:long",
                "\"1351\"^^xsd:long"));
    }

    @Test
    public void testDaysBetweenDateMappingInput() {


        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"17270\"^^xsd:long", "\"16360\"^^xsd:long", "\"17743\"^^xsd:long",
                "\"1352\"^^xsd:long"));
    }

    @Test
    public void testHoursBetween() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:hoursBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"594407\"^^xsd:long"));
    }

    @Test
    public void testMinutesBetween() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:minutesBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"35664450\"^^xsd:long"));
    }

    @Test
    public void testSecondsBetween() {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"2139867000\"^^xsd:long"));
    }

    @Test
    public void testSecondsBetweenMappingInput() {


        String query = "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{   ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?year) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"1492154272\"^^xsd:long", "\"1413511200\"^^xsd:long",
                "\"1532994786\"^^xsd:long", "\"116806800\"^^xsd:long"));
    }

    @Test
    public void testMilliSeconds() {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?v WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1999-12-13T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:millisBetween(?start, ?end) AS ?v)\n"
                + "}";

        executeAndCompareLexicalValues(query, ImmutableList.of("\"84600000\"^^xsd:long"));
    }

}
