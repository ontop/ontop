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
import org.semanticweb.owlapi.model.OWLObject;
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
 * Test if the datatypes xsd:date, xsd:time and xsd:year are returned correctly.
 *
 */

public class OntologyTypesDatatypeTest {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

    final String owlFile = "src/main/resources/testcases-datatypes/datetime/datatypes.owl";
    final String obdaFile = "src/main/resources/testcases-datatypes/datetime/datatypes-mysql.obda";

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

	private void runTests(Properties p, String query1, String expectedAnswer) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();


		try {
			executeQueryAssertResults(query1, st, expectedAnswer);
			
		} catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


		} finally {

			conn.close();
			reasoner.dispose();
		}
	}
	
	private void executeQueryAssertResults(String query, QuestOWLStatement st, String expectedAnswer) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);

        OWLObject answer = null;
		while (rs.nextRow()) {

			for (int i = 1; i <= rs.getColumnCount(); i++) {
				System.out.print(rs.getSignature().get(i-1));
                answer= rs.getOWLObject(i);
				System.out.print("=" + answer);
				System.out.print(" ");
			}
			System.out.println();
		}
		rs.close();
		assertEquals(expectedAnswer, answer.toString());
	}

    //With QuestOWL the results for xsd:date, xsd:time and xsd:year are returned as a plain literal since OWLAPI3 supports only xsd:dateTime
	@Test
	public void testDatatypeDate() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasDate ?y\n" +
                "   FILTER ( ?y = \"2013-03-18\"^^xsd:date ) .\n" +
                "}";

		runTests(p, query1, "\"2013-03-18\"");
	}

    @Test
    public void testDatatypeTime() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasTime ?y\n" +
                "   FILTER ( ?y = \"10:12:10\"^^xsd:time ) .\n" +
                "}";

        runTests(p, query1, "\"10:12:10\"");
    }

    @Test
    public void testDatatypeYear() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        String query1 = "PREFIX : <http://ontop.inf.unibz.it/test/datatypes#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x a :Row; :hasYear ?y\n" +
                "   FILTER ( ?y = \"2013\"^^xsd:gYear ) .\n" +
                "}";

        runTests(p, query1, "\"2013\"");
    }



}
