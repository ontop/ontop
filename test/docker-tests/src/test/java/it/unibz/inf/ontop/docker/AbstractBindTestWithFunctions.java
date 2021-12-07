package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 */

public abstract class AbstractBindTestWithFunctions {

    protected static Logger log = LoggerFactory.getLogger(AbstractBindTestWithFunctions.class);
    private final OntopOWLReasoner reasoner;
    private final OWLConnection conn;


    protected AbstractBindTestWithFunctions(OntopOWLReasoner reasoner) {
        this.reasoner = reasoner;
        this.conn = reasoner.getConnection();
    }

    protected static OntopOWLReasoner createReasoner(String owlFile, String obdaFile, String propertiesFile) throws OWLOntologyCreationException {
        owlFile = AbstractBindTestWithFunctions.class.getResource(owlFile).toString();
        obdaFile =  AbstractBindTestWithFunctions.class.getResource(obdaFile).toString();
        propertiesFile =  AbstractBindTestWithFunctions.class.getResource(propertiesFile).toString();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();
        return factory.createReasoner(config);
    }

    public OntopOWLReasoner getReasoner() {
        return reasoner;
    }

    public OWLConnection getConnection() {
        return conn;
    }

    private void runTests(String query) throws Exception {
        try (OWLStatement st = conn.createStatement()) {
            int i = 0;
            try (TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    OWLObject ind1 = bindingSet.getOWLObject("w");

                    log.debug(ind1.toString());
                    i++;
                }
                assertTrue(i > 0);
            }
        }
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
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
                + "}\n"
                + "ORDER BY ?w";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }


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

        checkReturnedValuesUnordered(queryBind, getCeilExpectedValues());
    }

    protected List<String> getCeilExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");

        return expectedValues;
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

        checkReturnedValuesUnordered(queryBind, getFloorExpectedValues());
    }

    protected List<String> getFloorExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        return expectedValues;
    }


    @Test
    public void testRound() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(STR(ROUND(?discount)),', ',STR(ROUND(?p))) AS ?w)\n"
                + "}";


        checkReturnedValuesUnordered(queryBind, getRoundExpectedValues());
    }

    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0, 43.0\"^^xsd:string");
        expectedValues.add("\"0.0, 23.0\"^^xsd:string");
        expectedValues.add("\"0.0, 34.0\"^^xsd:string");
        expectedValues.add("\"0.0, 10.0\"^^xsd:string");
        return expectedValues;
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

        checkReturnedValuesUnordered(queryBind, getAbsExpectedValues());
    }

    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.7\"^^xsd:decimal");
        expectedValues.add("\"1.5\"^^xsd:decimal");
        return expectedValues;
    }

	/*
	 * Tests for hash functions. H2 supports only SHA256 algorithm.
	 */

    @Test
    public void testHashSHA256() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount.\n"
                + "   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA256(str(?title)) AS ?w)\n"
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

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString.toString()));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        checkReturnedValuesUnordered(queryBind, expectedValues);

    }

    @Ignore
    @Test
    public void testHashMd5() {

    }

    @Ignore
    @Test
    public void testHashSHA1() {

    }

    @Ignore
    @Test
    public void testHashSHA384() {

    }

    @Ignore
    @Test
    public void testHashSHA512() {

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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"15\"^^xsd:integer");
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"20\"^^xsd:integer");
        expectedValues.add("\"44\"^^xsd:integer");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"ARQL Tutorial\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"e Semantic Web\"@en");
        expectedValues.add("\"ime and Punishment\"@en");
        expectedValues.add("\"e Logic Book: Introduction, Second Edition\"@en");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"ARQL T\"@en");   // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"e Sema\"@en");
        expectedValues.add("\"ime an\"@en");
        expectedValues.add("\"e Logi\"@en");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The%20Semantic%20Web\"^^xsd:string");
        expectedValues.add("\"The%20Logic%20Book%3A%20Introduction%2C%20Second%20Edition\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");

        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en"); // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly

        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial SPARQL TUTORIAL\"^^xsd:string");
        expectedValues.add("\"The Semantic Web THE SEMANTIC WEB\"^^xsd:string");
        expectedValues.add("\"Crime and Punishment CRIME AND PUNISHMENT\"^^xsd:string");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " +
                "The Logic Book: Introduction, Second Edition\"".toUpperCase()+"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);

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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial sparql tutorial\"^^xsd:string");
        expectedValues.add("\"The Semantic Web the semantic web\"^^xsd:string");
        expectedValues.add("\"Crime and Punishment crime and punishment\"^^xsd:string");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " +
                "The Logic Book: Introduction, Second Edition\"".toLowerCase()+"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);

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

        checkReturnedValuesUnordered(queryBind, getBindWithBefore1ExpectedValues());

    }

    protected List<String> getBindWithBefore1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\"The Logic Book: Introduc\"@en");

        return expectedValues;
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
        checkReturnedValuesUnordered(queryBind, getBindWithBefore2ExpectedValues());
    }

    protected List<String> getBindWithBefore2ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        return expectedValues;
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

        checkReturnedValuesUnordered(queryBind, getBindWithAfter1ExpectedValues());
    }

    protected List<String> getBindWithAfter1ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");
        return expectedValues;
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

        checkReturnedValuesUnordered(queryBind, getBindWithAfter2ExpectedValues());

    }

    protected List<String> getBindWithAfter2ExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial\"@en");
        expectedValues.add("\"The Semantic Web\"@en");
        expectedValues.add("\"Crime and Punishment\"@en");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");

        return expectedValues;
    }


	/*
	 * Tests for functions on date and time
	 */


    @Test
    public void testMonth() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (MONTH(?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getMonthExpectedValues());
    }

    protected List<String> getMonthExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");

        return expectedValues;
    }

    @Test
    public void testYear() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (YEAR(?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getYearExpectedValues());
    }

    protected List<String> getYearExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"2015\"^^xsd:integer");
        expectedValues.add("\"1967\"^^xsd:integer");

        return expectedValues;
    }

    @Test
    public void testDay() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (DAY(?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getDayExpectedValues());
    }

    protected List<String> getDayExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"5\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");
        expectedValues.add("\"21\"^^xsd:integer");
        expectedValues.add("\"5\"^^xsd:integer");

        return expectedValues;
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"47\"^^xsd:integer");
        expectedValues.add("\"30\"^^xsd:integer");
        expectedValues.add("\"23\"^^xsd:integer");
        expectedValues.add("\"50\"^^xsd:integer");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testHours() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (HOURS(?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getHoursExpectedValues());
    }

    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"18\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        return expectedValues;
    }


    @Test
    public void testSeconds() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (SECONDS(?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getSecondsExpectedValues());
    }

    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"6\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");

        return expectedValues;
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

        runTests(queryBind);
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


        runTests(queryBind);
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


        runTests(queryBind);
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


        runTests(queryBind);
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


        checkReturnedValuesUnordered(queryBind, getDivideExpectedValues());
    }

    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.250000\"^^xsd:decimal");
        expectedValues.add("\"11.500000\"^^xsd:decimal");
        expectedValues.add("\"16.750000\"^^xsd:decimal");
        expectedValues.add("\"5.000000\"^^xsd:decimal");
        return expectedValues;
    }

    @Test
    public void testTZ() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (TZ(?year) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getTZExpectedValues());
    }

    protected List<String> getTZExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"08:00\"^^xsd:string");
        expectedValues.add("\"01:00\"^^xsd:string");
        expectedValues.add("\"00:00\"^^xsd:string");
        expectedValues.add("\"01:00\"^^xsd:string");

        return expectedValues;
    }


    @Test
    public void testBound() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (BOUND(?title) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   OPTIONAL{ \n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER (STRSTARTS(?title, \"T\"))\n"
                + "   } \n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesUnordered(queryBind, expectedValues);

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
    public void testRDFTermEqual1() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?title,\" | \",?title2) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER (?discount = ?discount2 && ?title != ?title2)\n"
                + "   } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }

    @Test
    public void testRDFTermEqual2() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?title,\" | \",?title2) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER (?discount = ?discount2 && str(?title) != str(?title2))\n"
                + "   } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"Crime and Punishment | SPARQL Tutorial\"^^xsd:string");
        expectedValues.add("\"SPARQL Tutorial | Crime and Punishment\"^^xsd:string");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }


    @Test
    public void testSameTerm() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?title,\" | \",?title2) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?y ns:discount ?discount2 .\n"
                + "   ?y dc:title ?title2 .\n"
                + "   FILTER(sameTerm(?discount, ?discount2) && !sameTerm(?title, ?title2))\n"
                + "   } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"Crime and Punishment | SPARQL Tutorial\"^^xsd:string");
        expectedValues.add("\"SPARQL Tutorial | Crime and Punishment\"^^xsd:string");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }


    @Test
    public void testIsIRI() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isIRI(?title) AS ?w) WHERE \n"
                + "{  ?x ns:price ?price .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }


    @Test
    public void testIsBlank() {
            //no example data
    }

    @Test
    public void testIsLiteral() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isLiteral(?discount) AS ?w) WHERE \n"
                + "{  ?x ns:price ?price .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }


    @Test
    public void testIsNumeric() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (isNumeric(?discount) AS ?w) WHERE \n"
                + "{  ?x ns:price ?price .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }


    @Test
    public void testStr() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (str(?year) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   } ORDER BY ?year ";

        checkReturnedValuesAndOrder(queryBind, getStrExpectedValues());

    }

    protected List<String> getStrExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1970-11-05T07:50:00.000000\"^^xsd:string");
        expectedValues.add("\"2011-12-08T12:30:00.000000\"^^xsd:string");
        expectedValues.add("\"2014-06-05T18:47:52.000000\"^^xsd:string");
        expectedValues.add("\"2015-09-21T09:23:06.000000\"^^xsd:string");

        return expectedValues;
    }


    @Test
    public void testLang() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (lang(?title) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   }  ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"en\"^^xsd:string");
        expectedValues.add("\"en\"^^xsd:string");
        expectedValues.add("\"en\"^^xsd:string");
        expectedValues.add("\"en\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    //In SPARQL 1.0, the DATATYPE function was not defined for literals with a language tag
    @Test
    public void testDatatype() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT (?discount AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   FILTER ( datatype(?discount) = xsd:decimal)\n"
                + "   }  ";

        checkReturnedValuesUnordered(queryBind, getDatatypeExpectedValues());
    }

    protected List<String> getDatatypeExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.20\"^^xsd:decimal");
        expectedValues.add("\"0.25\"^^xsd:decimal");
        expectedValues.add("\"0.20\"^^xsd:decimal");
        expectedValues.add("\"0.15\"^^xsd:decimal");

        return expectedValues;
    }



    @Test
    public void testConcat() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (CONCAT(?title,\" | \", ?description) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x dc:description ?description .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"Crime and Punishment | good\"^^xsd:string");
        expectedValues.add("\"SPARQL Tutorial | good\"^^xsd:string");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition | good\"^^xsd:string");
        expectedValues.add("\"The Semantic Web | bad\"^^xsd:string");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }


    @Test
    public void testLangMatches() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?title) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   OPTIONAL{\n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER(langMatches( lang(?title), \"EN\" )) \n"
                + "   } } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }


    @Test
    public void testREGEX() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  (BOUND(?title) AS ?w) WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   OPTIONAL{\n"
                + "     ?x dc:title ?title .\n"
                + "     FILTER(REGEX( ?title, \"Semantic\" )) \n"
                + "   } } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }

    @Test
    public void testREPLACE() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?w WHERE \n"
                + "{  \n"
                + "   ?x ns:price ?p .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x dc:description ?description .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND(REPLACE(?title, \"Second\", \"First\") AS ?w) .\n"
                + "   } ORDER BY ?title";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"Crime and Punishment\"@en");
        expectedValues.add("\"SPARQL Tutorial\"@en");
        expectedValues.add("\"The Logic Book: Introduction, First Edition\"@en");
        expectedValues.add("\"The Semantic Web\"@en");
        checkReturnedValuesAndOrder(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:float AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1\"^^xsd:integer AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatDecimalDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:decimal AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:float / \"2.0\"^^xsd:double AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:double");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testConstantDoubleDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:double / \"2.0\"^^xsd:double AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:double");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testConstantIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"2\"^^xsd:integer AS ?w)  {} ";


        checkReturnedValuesUnordered(queryBind, getConstantIntegerDivideExpectedResults());
    }

    protected List<String> getConstantIntegerDivideExpectedResults() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:decimal");
        return expectedValues;
    }

    @Test
    public void testCoalesceDivideByZeroInt() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:integer / \"0\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceDivideByZeroDecimal() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:decimal / \"0\"^^xsd:decimal, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidDivide1() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" / \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidDivide2() throws Exception {
        String queryBind = "SELECT (COALESCE(\"2\"^^xsd:integer / \"rrr\", \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidSum() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" + \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidSub() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" - \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidTimes() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" * \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Ignore("TODO: support it, by using a case")
    @Test
    public void testDivideByZeroFloat() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"0.0\"^^xsd:float AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"INF\"^^xsd:float");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    private void checkReturnedValuesUnordered(String query, List<String> expectedValues) throws Exception {
        checkReturnedValues(query, expectedValues, false);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        expectedValues.add("\"cst\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI1() throws Exception {
        String queryBind = "SELECT ?w  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI1_2() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"http://example.org/john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI2() throws Exception {
        String queryBind = "SELECT ?w  {" +
                "BIND(IRI(<http://example.org/john>) AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI3() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI4() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(URI(\"john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI5() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"urn:john\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<urn:john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI6() throws Exception {
        String queryBind = "BASE <http://example.org/>\n" +
                "SELECT ?w  {" +
                "BIND(IRI(\"mailto:john@somewhere.org\") AS ?w)\n" +
                "FILTER (isIRI(?w))\n" +
                "} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<mailto:john@somewhere.org>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIRI8() throws Exception {
        String queryBind = "BASE <http://example.org/project1#data/>\n" +
                "SELECT ?w {" +
                "BIND(IRI(\"john\") AS ?w)\n" +
                "} ";
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("<http://example.org/project1#data/john>");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF1() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(\"rrr\" * \"2\"^^xsd:integer, \"1\", \"2\"), \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF2() throws Exception {
        String queryBind = "SELECT (IF(1 < 2, \"first\", \"second\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"first\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF3() throws Exception {
        String queryBind = "SELECT (IF(1 > 2, \"first\", \"second\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"second\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF4() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 < 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF5() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 > 2, \"rrr\" * \"2\"^^xsd:integer, \"second\"), \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"second\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testIF6() throws Exception {
        String queryBind = "SELECT (COALESCE(IF(1 > 2, \"first\", \"rrr\" * \"2\"^^xsd:integer), \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValuesUnordered(queryBind, expectedValues);
    }

    @Test
    public void testWeeksBetweenDate() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"3538\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }

    @Test
    public void testDaysBetweenDate() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14\"^^xsd:date AS ?end )\n"
                + "   BIND(\"1932-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?w)\n"
                + "}";
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"24767\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }

    @Test
    public void testWeeksBetweenDateTime() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:weeksBetween(?start, ?end) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"3538\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }

    @Test
    public void testDaysBetweenDateTime() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?end) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(query, getDaysBetweenDTExpectedValues());
    }

    protected List<String> getDaysBetweenDTExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"24766\"^^xsd:long");
        return expectedValues;
    }

    @Test
    public void testDaysBetweenDateTimeMappingInput() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getDaysDTExpectedValuesMappingInput());
    }

    protected List<String> getDaysDTExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16360\"^^xsd:long");
        expectedValues.add("\"17309\"^^xsd:long");
        expectedValues.add("\"17742\"^^xsd:long");
        expectedValues.add("\"255\"^^xsd:long");

        return expectedValues;
    }

    @Test
    public void testDaysBetweenDateMappingInput() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22\"^^xsd:date AS ?start )\n"
                + "   BIND (ofn:daysBetween(?start, ?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getDaysExpectedValuesMappingInput());
    }

    protected List<String> getDaysExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"16360\"^^xsd:long");
        expectedValues.add("\"17309\"^^xsd:long");
        expectedValues.add("\"17743\"^^xsd:long");
        expectedValues.add("\"256\"^^xsd:long");

        return expectedValues;
    }

    @Test
    public void testHoursBetween() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:hoursBetween(?start, ?end) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(query, getHoursBetweenExpectedValues());
    }

    protected List<String> getHoursBetweenExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"594407\"^^xsd:long");
        return expectedValues;
    }

    @Test
    public void testMinutesBetween() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:minutesBetween(?start, ?end) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"35664450\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }

    @Test
    public void testSecondsBetween() throws Exception {

        String query = "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1932-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?end) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2139867000\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }

    @Test
    public void testSecondsBetweenMappingInput() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND(\"1967-02-22T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:secondsBetween(?start, ?year) AS ?w)\n"
                + "}";

        checkReturnedValuesUnordered(queryBind, getSecondsExpectedValuesMappingInput());
    }
    
    protected List<String> getSecondsExpectedValuesMappingInput() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1413514800\"^^xsd:long");
        expectedValues.add("\"1495505872\"^^xsd:long");
        expectedValues.add("\"1532998386\"^^xsd:long");
        expectedValues.add("\"22112400\"^^xsd:long");

        return expectedValues;
    }

    @Test
    public void testMilliSeconds() throws Exception {

        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX  ofn:  <http://www.ontotext.com/sparql/functions/>\n"
                + "SELECT ?w WHERE \n"
                + "{  BIND(\"1999-12-14T09:00:00\"^^xsd:dateTime AS ?end )\n"
                + "   BIND(\"1999-12-13T09:30:00\"^^xsd:dateTime AS ?start )\n"
                + "   BIND (ofn:millisBetween(?start, ?end) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"84600000\"^^xsd:long");
        checkReturnedValuesUnordered(query, expectedValues);
    }
    
    private void checkReturnedValuesAndOrder(String query, List<String> expectedValues) throws Exception {
        checkReturnedValues(query, expectedValues, true);
    }

    private void checkReturnedValues(String query, List<String> expectedValues, boolean sameOrder) throws Exception {

        try (OWLConnection conn = reasoner.getConnection(); OWLStatement st = conn.createStatement()) {
            int i = 0;
            List<String> returnedValues = new ArrayList<>();
            try (TupleOWLResultSet rs = st.executeSelectQuery(query)) {
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    OWLObject ind1 = bindingSet.getOWLObject("w");

                    if (ind1 != null) {
                        String value = ToStringRenderer.getInstance().getRendering(ind1);
                        returnedValues.add(value);
                        log.debug(value);
                    }
                    else {
                        returnedValues.add(null);
                    }
                    i++;
                }
            }
            if(!sameOrder){
                Collections.sort(expectedValues);
                Collections.sort(returnedValues);
            }
            assertEquals(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()), expectedValues, returnedValues);
            assertEquals(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size(), i);
        }
    }



}
