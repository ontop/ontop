package it.unibz.inf.ontop.owlapi;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.*;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 * It expands the tests from {@link BindTest}.
 */
public abstract class AbstractBindWithFunctionsTest extends AbstractOWLAPITest {


	/*
	 * Tests for numeric functions
	 */
	
	
	@Test
    public void testCeil() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CEIL(?discount) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"1.0\"^^xsd:decimal",
                "\"1.0\"^^xsd:decimal",
                "\"1.0\"^^xsd:decimal",
                "\"1.0\"^^xsd:decimal"));
    }
	
	
	@Test
    public void testFloor() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (FLOOR(?discount) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.0\"^^xsd:decimal",
                "\"0.0\"^^xsd:decimal",
                "\"0.0\"^^xsd:decimal",
                "\"0.0\"^^xsd:decimal"));
    }
	
	
	@Test
    public void testRound() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(str(ROUND(?discount)),', ', str(ROUND(?p))) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.0, 43\"^^xsd:string",
                "\"0.0, 23\"^^xsd:string",
                "\"0.0, 34\"^^xsd:string",
                "\"0.0, 10\"^^xsd:string"));
    }
	
	@Test
    public void testAbs() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (ABS((?p - ?discount*?p) - ?p)  AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"8.6\"^^xsd:decimal",
                "\"5.75\"^^xsd:decimal",
                "\"6.8\"^^xsd:decimal",
                "\"1.50\"^^xsd:decimal"));
	}
	
	/*
	 * Tests for hash functions. H2 supports only SHA256 algorithm.
	 */
	
	@Test
    public void testHash() throws Exception {
        
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount.\n"
                + "   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA256(str(?title)) AS ?w)\n"
                + "}";

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest("The Semantic Web".getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                String.format("\"%s\"^^xsd:string",hexString.toString())));
    }

	
	/*
	 * Tests for functions on strings.
	 */

    @Test
    public void testStrLen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRLEN(?title) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"15\"^^xsd:integer",
                "\"16\"^^xsd:integer",
                "\"20\"^^xsd:integer",
                "\"44\"^^xsd:integer"));
    }

    //test substring with 2 parameters
    @Test
    public void testSubstr2() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"ARQL Tutorial\"@en",
                "\"e Semantic Web\"@en",
                "\"ime and Punishment\"@en",
                "\"e Logic Book: Introduction, Second Edition\"@en"));
    }

    //test substring with 3 parameters
    @Test
    public void testSubstr3() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3, 6) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"ARQL T\"@en",
                "\"e Sema\"@en",
                "\"ime an\"@en",
                "\"e Logi\"@en"));
    }
    @Test
    public void testURIEncoding() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   FILTER (STRSTARTS(?title,\"The\"))\n"
                + "   BIND (ENCODE_FOR_URI(?title) AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"The%20Semantic%20Web\"^^xsd:string",
                "\"The%20Logic%20Book%3A%20Introduction%2C%20Second%20Edition\"^^xsd:string"));
    }
	

    
    @Test
    public void testStrEnds() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(STRENDS(?title,\"b\"))\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"The Semantic Web\"@en"));
    }
    
    @Test
    public void testStrStarts() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"The Semantic Web\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testStrSubstring() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(SUBSTR(?title,1,STRLEN(?title)) AS ?w)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"The Semantic Web\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testContainsFilter() throws Exception {
        
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(CONTAINS(?title,\"Semantic\"))\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"The Semantic Web\"@en"));
    }

    @Test
    public void testContainsBind() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(CONTAINS(?title,\"Semantic\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean"));
    }

    @Test
    public void testNotBind() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(!(CONTAINS(?title,\"Semantic\")) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"true\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testNotFilter() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(!CONTAINS(?title,\"Semantic\"))\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial\"@en",
                "\"Crime and Punishment\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testAndBind() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") && CONTAINS(?title,\"Web\")) AS ?w)\n"
                + "}\n" +
                "ORDER BY ?w";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testAndBindDistinct() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") && CONTAINS(?title,\"Web\")) AS ?w)\n"
                + "}\n" +
                "ORDER BY ?w";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testOrBind() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND((CONTAINS(?title,\"Semantic\") || CONTAINS(?title,\"Book\")) AS ?w)\n"
                + "}\n" +
                "ORDER BY ?w";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"false\"^^xsd:boolean",
                "\"false\"^^xsd:boolean",
                "\"true\"^^xsd:boolean",
                "\"true\"^^xsd:boolean"));
    }

    @Test
    public void testBindWithUcase() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", ?v) AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial SPARQL TUTORIAL\"^^xsd:string",
                "\"The Semantic Web THE SEMANTIC WEB\"^^xsd:string",
                "\"Crime and Punishment CRIME AND PUNISHMENT\"^^xsd:string",
                "\"The Logic Book: Introduction, Second Edition " +
        "The Logic Book: Introduction, Second Edition\"".toUpperCase()+"^^xsd:string"));
    }
    
    @Test
    public void testBindWithLcase() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (LCASE(?title) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", ?v) AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial sparql tutorial\"^^xsd:string",
                "\"The Semantic Web the semantic web\"^^xsd:string",
                "\"Crime and Punishment crime and punishment\"^^xsd:string",
                "\"The Logic Book: Introduction, Second Edition " +
        "The Logic Book: Introduction, Second Edition\"".toLowerCase()+"^^xsd:string"));
    }
    
    
    @Test
    public void testBindWithBefore1() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\"ti\") AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"\"^^xsd:string",
                "\"The Seman\"@en",
                "\"\"^^xsd:string",
                "\"The Logic Book: Introduc\"@en"));
    }

    @Test
    public void testBindWithBefore2() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\"\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"\"@en",
                "\"\"@en",
                "\"\"@en",
                "\"\"@en"));
    }
    
    
    @Test
    public void testBindWithAfter1() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"The\") AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"\"^^xsd:string",
                "\" Semantic Web\"@en",
                "\"\"^^xsd:string",
                "\" Logic Book: Introduction, Second Edition\"@en"));
    }

    @Test
    public void testBindWithAfter2() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"SPARQL Tutorial\"@en",
                "\"The Semantic Web\"@en",
                "\"Crime and Punishment\"@en",
                "\"The Logic Book: Introduction, Second Edition\"@en"));
    }
    
    
	/*
	 * Tests for functions on date and time
	 */

    @Test
    public void testMonthDatetime() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?d .\n"
                + "   BIND (MONTH(?d) AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"6\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"7\"^^xsd:integer",
                "\"11\"^^xsd:integer"));
    }

    @Test
    public void testMonthDate() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubDate ?d .\n"
                + "   BIND (MONTH(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"6\"^^xsd:integer",
                "\"12\"^^xsd:integer",
                "\"7\"^^xsd:integer",
                "\"11\"^^xsd:integer"));
    }
    
    @Test
    public void testYearDatetime() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?d .\n"
                + "   BIND (YEAR(?d) AS ?w)\n"
             + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"2014\"^^xsd:integer",
                "\"2011\"^^xsd:integer",
                "\"1866\"^^xsd:integer",
                "\"1967\"^^xsd:integer"));
    }

    @Test
    public void testYearDate() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubDate ?d .\n"
                + "   BIND (YEAR(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"2014\"^^xsd:integer",
                "\"2011\"^^xsd:integer",
                "\"1866\"^^xsd:integer",
                "\"1967\"^^xsd:integer"));
    }

    @Test
    public void testDayDatetime() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?d .\n"
                + "   BIND (DAY(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"5\"^^xsd:integer",
                "\"8\"^^xsd:integer",
                "\"1\"^^xsd:integer",
                "\"5\"^^xsd:integer"));
    }

    @Test
    public void testDayDate() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubDate ?d .\n"
                + "   BIND (DAY(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"5\"^^xsd:integer",
                "\"8\"^^xsd:integer",
                "\"1\"^^xsd:integer",
                "\"5\"^^xsd:integer"));
    }

    @Test
    public void testMinutes() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (MINUTES(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"47\"^^xsd:integer",
                "\"46\"^^xsd:integer",
                "\"45\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testHoursDatetime() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?d .\n"
                + "   BIND (HOURS(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"18\"^^xsd:integer",
                "\"17\"^^xsd:integer",
                "\"16\"^^xsd:integer",
                "\"0\"^^xsd:integer"));
    }

    @Test
    public void testSeconds() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?d .\n"
                + "   BIND (SECONDS(?d) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"52\"^^xsd:decimal",
                "\"51\"^^xsd:decimal",
                "\"50\"^^xsd:decimal",
                "\"0\"^^xsd:decimal"));
    }

    @Test
    public void testNow() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (NOW() AS ?w)\n"
                + "}";

        checkNumberOfReturnedValues(queryBind, 4);
    }

    @Test
    public void testUuid() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (UUID() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        checkNumberOfReturnedValues(queryBind, 4);
    }

    @Test
    public void testStrUuid() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (STRUUID() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        checkNumberOfReturnedValues(queryBind, 4);
    }

    @Test
    public void testRand() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (RAND() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        checkNumberOfReturnedValues(queryBind, 4);
    }
    
    @Test
    public void testDivide() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND ((?p / 2) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"21.5\"^^xsd:decimal",
                "\"11.5\"^^xsd:decimal",
                "\"17\"^^xsd:decimal",
                "\"5\"^^xsd:decimal"));
    }

//    @Test timezone is not supported in h2
    public void testTZ() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (TZ(?year) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.0\"",
                "\"0.0\"",
                "\"0.0\"",
                "\"0.0\""));
    }

    //    @Test see results of datetime with locale
    public void testDatetime() throws Exception {
        TermFactory termFactory = OntopModelConfiguration.defaultBuilder().build().getTermFactory();

        String value = "Jan 31 2013 9:32AM";
        DateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.CHINA);

        try {
            java.util.Date date = df.parse(value);
            Timestamp ts = new Timestamp(date.getTime());
            System.out.println(termFactory.getRDFLiteralConstant(ts.toString().replace(' ', 'T'), XSD.DATETIME));
        }
        catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    @Test
    public void testConstantFloatDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:float AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1\"^^xsd:integer AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatDecimalDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:decimal AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:float"));
    }

    @Test
    public void testConstantFloatDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:float / \"2.0\"^^xsd:double AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:double"));
    }

    @Test
    public void testConstantDoubleDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:double / \"2.0\"^^xsd:double AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:double"));
    }

    @Test
    public void testConstantIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"2\"^^xsd:integer AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"0.5\"^^xsd:decimal"));
    }

    @Test
    public void testCoalesceDivideByZeroInt() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:integer / \"0\"^^xsd:integer, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceDivideByZeroDecimal() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:decimal / \"0\"^^xsd:decimal, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide1() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" / \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidDivide2() throws Exception {
        String queryBind = "SELECT (COALESCE(\"2\"^^xsd:integer / \"rrr\", \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSum() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" + \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidSub() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" - \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testCoalesceInvalidTimes() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" * \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Ignore("TODO: support it, by using a case")
    @Test
    public void testDivideByZeroFloat() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"0.0\"^^xsd:float AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"+INF\"^^xsd:float"));
    }

    @Test
    public void testBNODE0() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   BIND (BNODE() AS ?b)\n"
                + "   BIND (\"cst\" AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string"));
    }

    @Test
    public void testBNODE1() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT DISTINCT ?b ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   BIND (BNODE(\"b1\") AS ?b)\n"
                + "   BIND (\"cst\" AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string",
                "\"cst\"^^xsd:string"));
    }

    @Test
    public void testIRI1() throws Exception {
        String queryBind = "SELECT ?w  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI1_2() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI2() throws Exception {
        String queryBind = "SELECT ?w  {" +
                "BIND(IRI(<http://example.org/john>) AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI3() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI4() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(URI(\"john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI5() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"urn:john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<urn:john>"));
    }

    @Test
    public void testIRI6() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"mailto:john@somewhere.org\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<mailto:john@somewhere.org>"));
    }

    @Test
    public void testIRI7() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "{ VALUES ?v { \"john\" \"ernest\" \"http://example.org/alice\" } } UNION { BIND (str(rand()) AS ?v) } \n" +
                "BIND(IRI(?v) AS ?w)\n" +
                "VALUES ?y { <http://example.org/john> <http://otherdomain.org/ernest> } \n" +
                "FILTER (?w = ?y)\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/john>"));
    }

    @Test
    public void testIRI8() throws Exception {
        String queryBind = "BASE <http://example.org/project1#data/>\n" +
                "SELECT ?w {" +
                "BIND(IRI(\"john\") AS ?w)\n" +
                "} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "<http://example.org/project1#data/john>"));
    }

    @Test
    public void testIF1() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(\"rrr\" * \"2\"^^xsd:integer, \"1\", \"2\"), \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testIF2() throws Exception {
        String queryBind = "SELECT (IF(1 < 2, \"first\", \"second\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"first\"^^xsd:string"));
    }

    @Test
    public void testIF3() throws Exception {
        String queryBind = "SELECT (IF(1 > 2, \"first\", \"second\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"second\"^^xsd:string"));
    }

    @Test
    public void testIF4() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 < 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

    @Test
    public void testIF5() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 > 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"second\"^^xsd:string"));
    }

    @Test
    public void testIF6() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 > 2, \"first\", \"rrr\" * \"2\"^^xsd:integer), \"other\") AS ?w)  {} ";

        checkReturnedValues(queryBind, "w", ImmutableList.of(
                "\"other\"^^xsd:string"));
    }

}
