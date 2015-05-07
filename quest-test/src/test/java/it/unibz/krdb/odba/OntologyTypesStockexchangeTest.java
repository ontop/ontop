package it.unibz.krdb.odba;

/*
 * #%L
 * ontop-test
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
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test if the datatypes are assigned correctly.
 * 
 * NOTE: xsd:string and rdfs:Literal are different.
 * 
 */

public class OntologyTypesStockexchangeTest {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlFile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	final String obdaFile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-db2.obda";

	@Before
	public void setUp() throws Exception {

		fac = OBDADataFactoryImpl.getInstance();
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);
		
	}

	private void runTests(Properties p, String query1, int numberResults) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();


		try {
			executeQueryAssertResults(query1, st, numberResults);
			
		} catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


		} finally {

			conn.close();
			reasoner.dispose();
		}
	}
	
	private void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {
			count++;
			for (int i = 1; i <= rs.getColumnCount(); i++) {
				System.out.print(rs.getSignature().get(i-1));
				System.out.print("=" + rs.getOWLObject(i));
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedRows, count);
	}


	@Test //we need xsd:string to work correctly 
	public void testQuotedLiteral() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\".}";

		runTests(p, query1, 0 );
	}

    @Test
    public void testDatatypeString() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?street WHERE {?x a :Address; :inStreet ?street; :inCity \"Bolzano\"^^xsd:string .}";

        runTests(p, query1, 2 );
    }
    
    


    @Test //we need xsd:string to work correctly 
    public void testAddressesQuotedLiteral() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n" +
                "SELECT DISTINCT * WHERE {      \n" +
                "\t    $x a :Address . \n" +
                "\t\t$x :addressID $id. \n" +
                "\t\t$x :inStreet \"Via Marconi\". \n" +
                "\t\t$x :inCity \"Bolzano\". \n" +
                "\t\t$x :inCountry $country. \n" +
                "\t\t$x :inState $state. \n" +
                "\t\t$x :hasNumber $number.\n" +
                "}";

        runTests(p, query1, 0 );
    }

    @Test //in db2 there is no boolean type we refer to it in the database with a smallint 1 for true and a smallint 0 for false
    public void testBooleanDatatype() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:integer . }";

        runTests(p, query1, 5 );
    }

    @Test
    public void testBooleanInteger() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares 1 . }";

        runTests(p, query1, 5 );
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBoolean() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares TRUE . }";

        runTests(p, query1, 0 );
    }

    //in db2 there is no boolean datatype, it is substitute with smallint
    @Test
    public void testBooleanTrueDatatype() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\"^^xsd:boolean . }";

        runTests(p, query1, 0 );
    }


    @Test
    public void testFilterBoolean() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type = 1 ). }";

        runTests(p, query1, 5 );
    }

    @Test
    public void testNotFilterBoolean() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x ?amount WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares ?type. FILTER ( ?type != 1 ). }";

        runTests(p, query1, 5 );
    }
    
    @Test //a quoted integer is treated as a literal
    public void testQuotedInteger() throws Exception {

    	  QuestPreferences p = new QuestPreferences();
          p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
          p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
          p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

          String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Stock; :amountOfShares ?amount; :typeOfShares \"1\" . }";

          runTests(p, query1, 0 );
    }


    @Test //a quoted datatype is treated as a literal
    public void testDatatype() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }";

        runTests(p, query1, 1 );
    }

    @Test //a quoted datatype is treated as a literal
    public void testQuotedDatatype() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00\" . }";

        runTests(p, query1, 0 );
    }

    @Test //a quoted datatype is treated as a literal
    public void testDatatypeTimezone() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>\n SELECT DISTINCT ?x WHERE { ?x a :Transaction; :transactionID ?id; :transactionDate \"2008-04-02T00:00:00+06:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> . }";

        runTests(p, query1, 1 );
    }
}
