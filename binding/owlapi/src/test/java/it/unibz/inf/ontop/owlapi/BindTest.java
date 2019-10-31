package it.unibz.inf.ontop.owlapi;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 *
 * Class to test if bind in SPARQL is working properly.
 *
 * It uses the test from http://www.w3.org/TR/sparql11-query/#bind
 */

public class BindTest {

	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String OWL_FILE = "src/test/resources/test/bind/sparqlBind.owl";
    private static final String OBDA_FILE = "src/test/resources/test/bind/sparqlBind.obda";
    private static final String PROPERTY_FILE = "src/test/resources/test/bind/sparqlBind.properties";

    @Before
    public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
        // String driver = "org.h2.Driver";
        String url = "jdbc:h2:mem:questjunitdb";
        String username = "sa";
        String password = "";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBind-create-h2.sql");
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
	}

    @After
    public void tearDown() throws Exception {
        dropTables();
        conn.close();
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/bind/sparqlBind-drop-h2.sql");
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

    private OWLObject runTests(String query) throws Exception {

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(OBDA_FILE)
                .ontologyFile(OWL_FILE)
                .propertyFile(PROPERTY_FILE)
                .enableTestMode()
                .build();

		OntopOWLReasoner reasoner = factory.createReasoner(configuration);

        // Now we are ready for querying
        OWLConnection conn = reasoner.getConnection();
        OWLStatement st = conn.createStatement();


        try {
            TupleOWLResultSet rs = st.executeSelectQuery(query);
//            rs.hasNext();
            final OWLBindingSet bindingSet = rs.next();
            OWLObject ind1 = bindingSet.getOWLObject("title");
            OWLObject ind2 = bindingSet.getOWLObject("price");

            return ind2;

        }
        finally {
            st.close();
            reasoner.dispose();
        }
    }

    /**
     * querySelect1 return a literal instead of a numeric datatype
     * @throws Exception
     */
    @Test
    public void testSelect() throws Exception {

        //simple case
        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (17.25 AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "}";
        OWLObject price = runTests(querySelect);

        assertEquals("\"17.25\"^^xsd:decimal", price.toString());

        //complex case
        String querySelect1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p*(1-?discount) AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "}";
        OWLObject price1 = runTests(querySelect1);

        assertEquals("\"33.6\"^^xsd:decimal", price1.toString());





    }

    @Test
    public void testBind() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (?p*(1-?discount) AS ?w)\n"
                + "   FILTER(?w < 20)\n"
                + "   ?x dc:title ?title .\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"17.25\"^^xsd:decimal");


        checkReturnedValues(queryBind, expectedValues);
//        OWLObject price = runTests(p, queryBind);
//
//        assertEquals("\"17.25\"", price.toString());


    }

    @Test
    public void testBindWithStrlen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT ?x ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (?p*(1-?discount) AS ?w)\n"
                + "   FILTER(?w < 20 && strlen(?title) > 0) \n"
                + "   ?x dc:title ?title .\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"17.25\"^^xsd:decimal");


        checkReturnedValues(queryBind, expectedValues);
//        OWLObject price = runTests(p, queryBind);
//
//        assertEquals("\"17.25\"", price.toString());


    }
    @Test
    public void testDoubleBind() throws Exception{
        
		// double bind
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  ?w WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x dc:title ?title .\n" +
                "   ?x ns:price ?p .\n" +  // ROMAN (26 June 2016): moved and changed the line here
                                           // otherwise, ?p would not have any values as BIND is evaluated
                                           // in the context of the preceding part of the pattern
                "   BIND (?p AS ?fullPrice) \n" +
                "   BIND (?fullPrice  - ?discount AS ?w) \n" +
                "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"41.8\"^^xsd:decimal");
        expectedValues.add("\"22.75\"^^xsd:decimal");


        checkReturnedValues(queryBind, expectedValues);
    }


    @Test
    public void testBindWithSelect() throws Exception {

        //error in DataFactoryImpl to handle  nested functional terms getFreshCQIECopy
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  (?fullPrice * ?discount AS ?w) WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x dc:title ?title .\n" +
                "  ?x ns:price ?p .\n" +  // ROMAN (26 June 2016): moved and changed the line here
                                          // otherwise, ?p would not have any values as BIND is evaluated
                                          // in the context of the preceding part of the pattern
                "   BIND (?p AS ?fullPrice) \n" +
                "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.4\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");


        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testSelect2()  throws Exception {

        //complex case
        //variable should be assigned again in the same SELECT clause. SELECT Expressions, reuse the same variable
        String querySelect1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p AS ?price) (?price-?discount AS ?customerPrice)\n" + // ROMAN (1 June): changed to avoid NullPointerException as runTest requires variable ?price)
                "{ ?x ns:price ?p .\n" +
                "   ?x dc:title ?title . \n" +
                "   ?x ns:discount ?discount \n" +
                "}";
        runTests(querySelect1);
    }

    @Test(expected = OntopOWLException.class)
    public void testFailingSelect1()  throws Exception {

        //variable cannot be assigned again in the same SELECT clause. SELECT Expressions, reuse the same variable in FILTER
        String querySelect2 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p -?discount AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "  FILTER(?price < 20) \n" +
                "}";
        runTests(querySelect2);
    }

    /**
     * We don't support the union of BIND
     * SingletonSet operator is not supported
     * @throws Exception
     */
    @Test
    public void testFailingBind() throws Exception {

/*      ROMAN (26 June 2016): commented out - the query has little sense because the expressions
                              used in BIND can never have any values
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?price WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   {BIND (?p*(1-?discount) AS ?price)}\n"
                +         "UNION \n"
                + "   {BIND (?p*(2-?discount) AS ?price)}\n"
                + "   FILTER(?price < 20)\n"
                + "   ?x dc:title ?title .\n"
                + "}";
        try {
            OWLObject price = runTests(p, queryBind);

        } catch (OntopOWLException e) {
            assertEquals("it.unibz.inf.ontop.model.OBDAException", e.getCause().getClass().getName());
            // ROMAN: commented out -- now the message is different
            // assertEquals("Operator not supported: SingletonSet", e.getCause().getLocalizedMessage().trim());
        }
*/
        //error in DataFactoryImpl to handle  nested functional terms getFreshCQIECopy
//        (?fullPrice * ?discount AS ?customerPrice)
        String queryBind1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  (?fullPrice * (1- ?discount) AS ?price) WHERE \n" + // ROMAN: replaced customerPrice
                "{  ?x ns:discount ?discount .\n" +
                "   ?x ns:price ?p .\n" +  // ROMAN (26 June 2016): added the line
                "   ?x dc:title ?title .\n" +
                "   BIND (?p AS ?fullPrice) \n" +
                //"   ?x ns:price ?fullPrice .\n" + // ROMAN (26 June 2016): commented out - does not make sense
                "}";

        try {
            OWLObject price = runTests(queryBind1);

        } catch (OntopOWLException e) {

            assertEquals("it.unibz.inf.ontop.model.OBDAException", e.getCause().getClass().getName());

        }




    }

    @Test
    public void testBindWithConcat() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(?title, \" title\") AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial title\"^^xsd:string");
        expectedValues.add("\"The Semantic Web title\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testBindWithUCase() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL TUTORIAL\"@en");
        expectedValues.add("\"THE SEMANTIC WEB\"@en");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testBindWithConcatStrLen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRLEN(CONCAT(?title, \" \")) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", str(?v)) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial 16\"^^xsd:string");
        expectedValues.add("\"The Semantic Web 17\"^^xsd:string");
        checkReturnedValues(queryBind, expectedValues);


    }

    @Test
    public void testBindWithConcatLanguage() throws Exception {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(?title, ?title) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL TutorialSPARQL Tutorial\"@en");
        expectedValues.add("\"The Semantic WebThe Semantic Web\"@en");
        checkReturnedValues(queryBind, expectedValues);



    }

    @Test
    public void testBindConcatUnderUnion() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?w WHERE \n"
                + "{"
                + "   { BIND (\"plop\" AS ?w) }\n"
                + "   UNION\n"
                + "   { \n"
                + "     ?x ns:price ?p .\n" +
                "       ?x ns:discount ?discount .\n"
                + "    ?x dc:title ?title .\n"
                + "    BIND (CONCAT(?title, ?title) AS ?w) }\n"
                + "}";

        runTests(queryBind);
    }

    @Test
    public void testBindConcatAboveUnion() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?w WHERE \n"
                + "{"
                + "   ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   { BIND (\"plop\" AS ?s) }\n"
                + "   UNION\n"
                + "   { \n"
                + "    ?y dc:title ?s .\n"
                + "   }\n"
                + "   BIND (CONCAT(?title, ?s) AS ?w)"
                + "}";

        runTests(queryBind);
    }

    //language tag are not supported when they are stored in different columns or
    // when one of the two is stored in the database
    //the correct results would have been with "@en" language tag
    @Test
    public void testBindWithConcatLanguageinDatabase() throws Exception {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x dc:description ?desc .\n"
                + "   BIND (CONCAT(?desc, ?title) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"goodSPARQL Tutorial\"@en");
        expectedValues.add("\"badThe Semantic Web\"@en");
        checkReturnedValues(queryBind, expectedValues);



    }


    //CAST function not supported
    //The builtin function http://www.w3.org/2001/XMLSchema#string is not supported yet!
    @Test(expected = OntopReformulationException.class)
    public void testBindWithCast() throws Throwable {

		String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
                + "SELECT  ?title ?price WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount\n"
                + "   BIND (CONCAT(xsd:string(?p*(1-?discount)), xsd:string(?p)) AS ?price)\n"
                + "   ?x dc:title ?title .\n"
                + "}";

        try {
            runTests(queryBind);
        } catch (OntopOWLException e) {
            throw e.getCause();
        }

    }

    @Test
    public void testBindLangTag() throws Exception {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "SELECT ?w\n" +
                "WHERE {\n" +
                "  ?y dc:title ?title .\n" +
                "  BIND( lang(?title) AS ?w ) .\n" +
                "}\n";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"en\"^^xsd:string");
        expectedValues.add("\"en\"^^xsd:string");
        checkReturnedValues(query, expectedValues);
    }

    @Test
    public void testBindLangTag2() throws Exception {
        String query = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "SELECT ?w\n" +
                "WHERE {\n" +
                "  ?y dc:title ?title .\n" +
                "  BIND (str(?title) AS ?s)\n" +
                "  BIND( lang(?s) AS ?w ) .\n" +
                "}\n";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"^^xsd:string");
        expectedValues.add("\"\"^^xsd:string");
        checkReturnedValues(query, expectedValues);
    }


        private void checkReturnedValues(String query, List<String> expectedValues) throws Exception {

			// Creating a new instance of the reasoner
			OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
            OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .nativeOntopMappingFile(OBDA_FILE)
                    .ontologyFile(OWL_FILE)
                    .propertyFile(PROPERTY_FILE)
                    .enableTestMode()
                    .build();

			OntopOWLReasoner reasoner = factory.createReasoner(configuration);

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
