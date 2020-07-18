package it.unibz.inf.ontop.owlapi;


import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;

import java.security.MessageDigest;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static org.junit.Assert.assertTrue;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 * It expands the tests from {@link BindTest}.
 */
public class BindWithFunctionsTest {

	private Connection conn;

	private static final String owlfile = "src/test/resources/test/bind/sparqlBind.owl";
	private static final String obdafile = "src/test/resources/test/bind/sparqlBindWithFunctions.obda";
	private static final String propertyFile = "src/test/resources/test/bind/sparqlBindWithFunctions.properties";

    @Before
	public void setUp() throws Exception {
    	String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		conn = DriverManager.getConnection(url, username, password);
		executeFromFile(conn, "src/test/resources/test/bind/sparqlBindWithFns-create-h2.sql");
	}

	@After
	public void tearDown() throws Exception {
		executeFromFile(conn, "src/test/resources/test/bind/sparqlBindWithFns-drop-h2.sql");
        conn.close();
	}



	private void runTests(String query) throws Exception {

        // Creating a new instance of the reasoner

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();
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
        } finally {
            conn.close();
            reasoner.dispose();
        }
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1.0\"^^xsd:decimal");
        expectedValues.add("\"1.0\"^^xsd:decimal");
        expectedValues.add("\"1.0\"^^xsd:decimal");
        expectedValues.add("\"1.0\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0\"^^xsd:decimal");
        expectedValues.add("\"0.0\"^^xsd:decimal");
        expectedValues.add("\"0.0\"^^xsd:decimal");
        expectedValues.add("\"0.0\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0, 43\"^^xsd:string");
        expectedValues.add("\"0.0, 23\"^^xsd:string");
        expectedValues.add("\"0.0, 34\"^^xsd:string");
        expectedValues.add("\"0.0, 10\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.6\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.8\"^^xsd:decimal");
        expectedValues.add("\"1.50\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
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
    public void testNotBind() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(!(CONTAINS(?title,\"Semantic\")) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial\"@en");
        expectedValues.add("\"Crime and Punishment\"@en");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");
        checkReturnedValues(queryBind, expectedValues);
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
                + "}\n" +
                "ORDER BY ?w";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"false\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
        expectedValues.add("\"true\"^^xsd:boolean");
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\"The Logic Book: Introduc\"@en");
        checkReturnedValues(queryBind, expectedValues);

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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\"\"@en");
        checkReturnedValues(queryBind, expectedValues);

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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial\"@en");
        expectedValues.add("\"The Semantic Web\"@en");
        expectedValues.add("\"Crime and Punishment\"@en");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");
        checkReturnedValues(queryBind, expectedValues);

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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"1866\"^^xsd:integer");
        expectedValues.add("\"1967\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"1866\"^^xsd:integer");
        expectedValues.add("\"1967\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"5\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");
        expectedValues.add("\"1\"^^xsd:integer");
        expectedValues.add("\"5\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"5\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");
        expectedValues.add("\"1\"^^xsd:integer");
        expectedValues.add("\"5\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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
        expectedValues.add("\"46\"^^xsd:integer");
        expectedValues.add("\"45\"^^xsd:integer");
        expectedValues.add("\"0\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"18\"^^xsd:integer");
        expectedValues.add("\"17\"^^xsd:integer");
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"0\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
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


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52\"^^xsd:decimal");
        expectedValues.add("\"51\"^^xsd:decimal");
        expectedValues.add("\"50\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.5\"^^xsd:decimal");
        expectedValues.add("\"11.5\"^^xsd:decimal");
        expectedValues.add("\"17\"^^xsd:decimal");
        expectedValues.add("\"5\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
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

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        checkReturnedValues(queryBind, expectedValues);
    }

    //    @Test see results of datetime with locale
    public void testDatetime() throws Exception {
        TermFactory termFactory = OntopModelConfiguration.defaultBuilder().build().getTermFactory();

        String value = "Jan 31 2013 9:32AM";

        DateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.CHINA);

        java.util.Date date;
        try {
            date = df.parse(value);
            Timestamp ts = new Timestamp(date.getTime());
            System.out.println(termFactory.getRDFLiteralConstant(ts.toString().replace(' ', 'T'), XSD.DATETIME));

        } catch (ParseException pe) {

            throw new RuntimeException(pe);
        }
    }

    @Test
    public void testConstantFloatDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:float AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1\"^^xsd:integer AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatDecimalDivide() throws Exception {
        String queryBind = "SELECT (\"0.5\"^^xsd:float / \"1.0\"^^xsd:decimal AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:float");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testConstantFloatDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:float / \"2.0\"^^xsd:double AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:double");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testConstantDoubleDoubleDivide() throws Exception {
        String queryBind = "SELECT (\"1.0\"^^xsd:double / \"2.0\"^^xsd:double AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:double");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testConstantIntegerDivide() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"2\"^^xsd:integer AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.5\"^^xsd:decimal");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceDivideByZeroInt() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:integer / \"0\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceDivideByZeroDecimal() throws Exception {
        String queryBind = "SELECT (COALESCE(\"1\"^^xsd:decimal / \"0\"^^xsd:decimal, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidDivide1() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" / \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidDivide2() throws Exception {
        String queryBind = "SELECT (COALESCE(\"2\"^^xsd:integer / \"rrr\", \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidSum() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" + \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidSub() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" - \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testCoalesceInvalidTimes() throws Exception {
        String queryBind = "SELECT (COALESCE(\"rrr\" * \"2\"^^xsd:integer, \"other\") AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"other\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Ignore("TODO: support it, by using a case")
    @Test
    public void testDivideByZeroFloat() throws Exception {
        String queryBind = "SELECT (\"1\"^^xsd:integer / \"0.0\"^^xsd:float AS ?w)  {} ";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"+INF\"^^xsd:float");
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
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
        checkReturnedValues(queryBind, expectedValues);
    }

    private void checkReturnedValues(String query, List<String> expectedValues) throws Exception {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();
        OntopOWLReasoner reasoner = factory.createReasoner(config);


        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();



            int i = 0;
            List<String> returnedValues = new ArrayList<>();
            try {
                TupleOWLResultSet  rs = st.executeSelectQuery(query);
                while (rs.hasNext()) {
                    final OWLBindingSet bindingSet = rs.next();
                    OWLObject ind1 = bindingSet.getOWLObject("w");
                    String value = ToStringRenderer.getInstance().getRendering(ind1);
                    // log.debug(ind1.toString());
                    returnedValues.add(value);
                    java.lang.System.out.println(value);
                    i++;
                }
            } catch (Exception e) {
                throw e;
            } finally {
                conn.close();
                reasoner.dispose();
            }
            assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                    returnedValues.equals(expectedValues));
            assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

    }


}
