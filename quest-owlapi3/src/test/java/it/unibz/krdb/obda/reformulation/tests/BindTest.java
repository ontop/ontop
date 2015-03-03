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
import it.unibz.krdb.obda.owlapi3.OntopOWLException;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class to test if bind in SPARQL is working properly.
 * Refer in particular to the class {@link it.unibz.krdb.obda.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 *
 * It uses the test from http://www.w3.org/TR/sparql11-query/#bind
 */

public class BindTest {



	private OBDADataFactory fac;
	private Connection conn;

	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/sparqlBind.owl";
	final String obdafile = "src/test/resources/test/sparqlBind.obda";

    @Before
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/sparqlBind-create-h2.sql");
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

		FileReader reader = new FileReader("src/test/resources/test/sparqlBind-drop-h2.sql");
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

    /**
     * querySelect1 return a literal instead of a numeric datatype
     * @throws Exception
     */
    @Test
	public void testSelect() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        //simple case
        String querySelect = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (17.25 AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "}";
        OWLObject price = runTests(p, querySelect);

        assertEquals("\"17.25\"^^xsd:decimal", price.toString());

//        //complex case
//        String querySelect1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
//                "PREFIX  ns:  <http://example.org/ns#>\n" +
//                "SELECT  ?title (?p*(1-?discount) AS ?price)\n" +
//                "{ ?x ns:price ?p .\n" +
//                "  ?x dc:title ?title . \n" +
//                "  ?x ns:discount ?discount . \n" +
//                "}";
//        OWLObject price1 = runTests(p, querySelect1);
//
//        assertEquals("\"33.6\"", price1.toString());





    }

    @Test
    public void testBind() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

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
        expectedValues.add("\"17.25\"");


        checkReturnedValues(p, queryBind, expectedValues);
//        OWLObject price = runTests(p, queryBind);
//
//        assertEquals("\"17.25\"", price.toString());


    }

    @Test
    public void testFailingSelect()  throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        //complex case
        //variable should be assigned again in the same SELECT clause. SELECT Expressions, reuse the same variable in FILTER
        String querySelect1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p AS ?fullPrice) (?fullPrice*(1-?discount) AS ?customerPrice)\n" +
                "{ ?x ns:price ?p .\n" +
                "   ?x dc:title ?title . \n" +
                "   ?x ns:discount ?discount \n" +
                "}";
        OWLObject price1 = null;

        try {

            price1 = runTests(p, querySelect1);

        } catch (OntopOWLException e) {

            assertEquals("it.unibz.krdb.obda.model.OBDAException", e.getCause().getClass().getName());
        }

        //variable cannot be assigned again in the same SELECT clause. SELECT Expressions, reuse the same variable in FILTER
        String querySelect2 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title (?p*(1-?discount) AS ?price)\n" +
                "{ ?x ns:price ?p .\n" +
                "  ?x dc:title ?title . \n" +
                "  ?x ns:discount ?discount . \n" +
                "  FILTER(?price < 20) \n" +
                "}";
        OWLObject price2 = null;
        try {
            price2 = runTests(p, querySelect2);
        } catch (OntopOWLException e) {

            assertEquals("it.unibz.krdb.obda.model.OBDAException", e.getCause().getClass().getName());
        }

    }

    /**
     * We don't support the union of BIND
     * SingletonSet operator is not supported
     * @throws Exception
     */
    @Test
    public void testFailingBind() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

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

            assertEquals("it.unibz.krdb.obda.model.OBDAException", e.getCause().getClass().getName());
            assertEquals("Operator not supported: SingletonSet", e.getCause().getLocalizedMessage().trim());
        }

        //error double bind and select
        String queryBind1 = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n" +
                "SELECT  ?title  (?fullPrice * (1 - ?discount) AS ?customerPrice) WHERE \n" +
                "{  ?x ns:discount ?discount .\n" +
                "   ?x dc:title ?title .\n" +
                "   BIND (?p AS ?fullPrice) \n" +
                "  ?x ns:price ?fullPrice .\n" +
                "}";

        try {
            OWLObject price = runTests(p, queryBind1);

        } catch (OntopOWLException e) {

            assertEquals("it.unibz.krdb.obda.model.OBDAException", e.getCause().getClass().getName());

        }

    }

    @Test
    public void testBindWithConcat() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

//        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
//                + "PREFIX  ns:  <http://example.org/ns#>\n"
//                + "SELECT  ?title ?price WHERE \n"
//                + "{  ?x ns:price ?p .\n"
//                + "   ?x ns:discount ?discount\n"
//                + "   BIND (CONCAT(xsd:string(?p*(1-?discount)), xsd:string(?p)) AS ?price)\n"
//                + "   FILTER(?price < 20)\n"
//                + "   ?x dc:title ?title .\n"
//                + "}";

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT((?title), \" title\") AS ?w)\n"
             + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial title\"");
        expectedValues.add("\"The Semantic Web title\"");
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
