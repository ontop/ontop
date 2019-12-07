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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

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

        // Creating a new instance of the reasoner

        // Now we are ready for querying
        OWLStatement st = conn.createStatement();


        int i = 0;

        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("w");


                System.out.println(ind1);
                i++;
            }
            assertTrue(i > 0);

        } catch (Exception e) {
            throw e;
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, getCeilExpectedValues());
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

        checkReturnedValues(queryBind, getFloorExpectedValues());
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


        checkReturnedValues(queryBind, getRoundExpectedValues());
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

        checkReturnedValues(queryBind, getAbsExpectedValues());
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
            byte[] hash = digest.digest("The Semantic Web".getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"^^xsd:string",hexString.toString()));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        checkReturnedValues(queryBind, expectedValues);

    }

    @Ignore
    @Test
    public void testHashMd5() throws Exception {

    }

    @Ignore
    @Test
    public void testHashSHA1() throws Exception {

    }

    @Ignore
    @Test
    public void testHashSHA384() throws Exception {

    }

    @Ignore
    @Test
    public void testHashSHA512() throws Exception {

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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);

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
        checkReturnedValues(queryBind, expectedValues);

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

        checkReturnedValues(queryBind, getBindWithBefore1ExpectedValues());

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
        checkReturnedValues(queryBind, getBindWithBefore2ExpectedValues());
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

        checkReturnedValues(queryBind, getBindWithAfter1ExpectedValues());
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

        checkReturnedValues(queryBind, getBindWithAfter2ExpectedValues());

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

        checkReturnedValues(queryBind, getMonthExpectedValues());
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

        checkReturnedValues(queryBind, getYearExpectedValues());
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

        checkReturnedValues(queryBind, getDayExpectedValues());
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
        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, getHoursExpectedValues());
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

        checkReturnedValues(queryBind, getSecondsExpectedValues());
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


        checkReturnedValues(queryBind, getDivideExpectedValues());
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

        checkReturnedValues(queryBind, getTZExpectedValues());
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
        checkReturnedValues(queryBind, expectedValues);

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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
    }


    @Test
    public void testIsBlank() throws Exception {
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, getStrExpectedValues());

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
        checkReturnedValues(queryBind, expectedValues);
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

        checkReturnedValues(queryBind, getDatatypeExpectedValues());
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
    }

    private void checkReturnedValues(String query, List<String> expectedValues) throws Exception {

        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();



        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.hasNext()) {
                final OWLBindingSet bindingSet = rs.next();
                OWLObject ind1 = bindingSet.getOWLObject("w");

                // log.debug(ind1.toString());

                if (ind1 != null) {
                    String value = ToStringRenderer.getInstance().getRendering(ind1);
                    returnedValues.add(value);
                    System.out.println(value);
                } else {
                    returnedValues.add(null);
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

    }



}
