package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 *
 * It expands the tests from {@link it.unibz.krdb.obda.reformulation.test.BindTest}.
 */

public class BindTestWithFunctions {
	
	private OBDADataFactory fac;
	private Connection conn;

	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/bind/sparqlBind.owl";
	final String obdafile = "src/test/resources/test/bind/sparqlBindWithFunctions.obda";

    @Before
	public void setUp() throws Exception {
	
    	String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBindWithFns-create-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();
		
		st.executeUpdate(bf.toString()); 
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
	}

	@After
	public void tearDown() throws Exception {

		  dropTables();
			conn.close();
		
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBindWithFns-drop-h2.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();
		
		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private OWLObject runTests(Properties p, String query) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();


		try {
			QuestOWLResultSet rs = st.executeTuple(query);
            rs.nextRow();
			OWLObject ind1 = rs.getOWLObject("title");
            OWLObject ind2 = rs.getOWLObject("price");

         return ind2;

		}
		finally {
			st.close();
			reasoner.dispose();
		}
	}
	
	/*
	 * Tests for numeric functions
	 */
	
	
	@Test
    public void testCeil() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CEIL(?discount) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1.0\"");
        expectedValues.add("\"1.0\"");
        expectedValues.add("\"1.0\"");
        expectedValues.add("\"1.0\"");
        checkReturnedValues(p, queryBind, expectedValues);
    }
	
	
	@Test
    public void testFloor() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (FLOOR(?discount) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        checkReturnedValues(p, queryBind, expectedValues);
    }
	
	
	@Test
    public void testRound() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(ROUND(?discount),', ',ROUND(?p)) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0, 43.0\"");
        expectedValues.add("\"0.0, 23.0\"");
        expectedValues.add("\"0.0, 34.0\"");
        expectedValues.add("\"0.0, 10.0\"");
        checkReturnedValues(p, queryBind, expectedValues);
    }
	
	@Test
    public void testAbs() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (ABS((?p - ?discount*?p) - ?p)  AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.6\"");
        expectedValues.add("\"5.75\"");
        expectedValues.add("\"6.8\"");
        expectedValues.add("\"1.50\"");
        checkReturnedValues(p, queryBind, expectedValues);
	}	
	
	/*
	 * Tests for hash functions. H2 supports only SHA25 algorithm.
	 */
	
	@Test
    public void testHash() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount.\n"
                + "   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA256(?title) AS ?w)\n"
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

	          expectedValues.add(String.format("\"%s\"",hexString.toString()));
	  } catch(Exception ex){
	     throw new RuntimeException(ex);
	  }
        checkReturnedValues(p, queryBind, expectedValues);

    }
	
	public static String sha256(String base) {
	      try{
	          MessageDigest digest = MessageDigest.getInstance("SHA-256");
	          byte[] hash = digest.digest(base.getBytes("UTF-8"));
	          StringBuffer hexString = new StringBuffer();

	          for (int i = 0; i < hash.length; i++) {
	              String hex = Integer.toHexString(0xff & hash[i]);
	              if(hex.length() == 1) hexString.append('0');
	              hexString.append(hex);
	          }

	      return hexString.toString();
	  } catch(Exception ex){
	     throw new RuntimeException(ex);
	  }
	}
	
	/*
	 * Tests for functions on strings.
	 */
	
	
    @Test
    public void testURIEncoding() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


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
        expectedValues.add("\"The%20Semantic%20Web\"");
        expectedValues.add("\"The%20Logic%20Book%3A%20Introduction%2C%20Second%20Edition\"");
        checkReturnedValues(p, queryBind, expectedValues);
    } 
	

    
    @Test
    public void testStrEnds() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


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
        checkReturnedValues(p, queryBind, expectedValues);
    } 
    
    @Test
    public void testStrStarts() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


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

        checkReturnedValues(p, queryBind, expectedValues);
    } 
    
    
    @Test
    public void testContains() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

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
        checkReturnedValues(p, queryBind, expectedValues);

    }    
    
  
    @Test
    public void testBindWithUcase() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


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
        expectedValues.add("\"SPARQL Tutorial SPARQL TUTORIAL\"");
        expectedValues.add("\"The Semantic Web THE SEMANTIC WEB\"");
        expectedValues.add("\"Crime and Punishment CRIME AND PUNISHMENT\"");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " + 
        "The Logic Book: Introduction, Second Edition\"".toUpperCase());
        checkReturnedValues(p, queryBind, expectedValues);

    }
    
    @Test
    public void testBindWithLcase() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


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
        expectedValues.add("\"SPARQL Tutorial sparql tutorial\"");
        expectedValues.add("\"The Semantic Web the semantic web\"");
        expectedValues.add("\"Crime and Punishment crime and punishment\"");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " + 
        "The Logic Book: Introduction, Second Edition\"".toLowerCase());
        checkReturnedValues(p, queryBind, expectedValues);

    }
    
    
    @Test
    public void testBindWithBefore() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\" \") AS ?w)\n"
             + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL\"");
        expectedValues.add("\"The\"");
        expectedValues.add("\"Crime\"");
        expectedValues.add("\"The\"");
        checkReturnedValues(p, queryBind, expectedValues);

    }
    
    
//    @Test
    public void testBindWithAfter() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"The\") AS ?w)\n"
             + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"");
        expectedValues.add("\" Semantic Web\"");
        expectedValues.add("\"\"");
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"");
        checkReturnedValues(p, queryBind, expectedValues);

    } //Note: in specification of SPARQL function if the string doesn't contain the specified string empty string has to be returned,
    //here instead the whole string is returned
    
    
	/*
	 * Tests for functions on date and time
	 */
    
    
    @Test
    public void testMonth() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (MONTH(?year) AS ?w)\n"
             + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"");        
        expectedValues.add("\"12\""); 
        expectedValues.add("\"7\"");      
        expectedValues.add("\"11\"");        
        checkReturnedValues(p, queryBind, expectedValues);
    } 
    
    @Test
    public void testYear() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (YEAR(?year) AS ?w)\n"
             + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"");        
        expectedValues.add("\"2011\""); 
        expectedValues.add("\"1866\"");      
        expectedValues.add("\"1967\"");        
        checkReturnedValues(p, queryBind, expectedValues);
    } 

        private void checkReturnedValues(QuestPreferences p, String query, List<String> expectedValues) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);

        factory.setPreferenceHolder(p);

        QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();



            int i = 0;
            List<String> returnedValues = new ArrayList<>();
            try {
                QuestOWLResultSet rs = st.executeTuple(query);
                while (rs.nextRow()) {
                    OWLObject ind1 = rs.getOWLObject("w");
                    // log.debug(ind1.toString());
                    returnedValues.add(ind1.toString());
                    java.lang.System.out.println(ind1);
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
