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
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This test is adapted from SimpleMappingVirtualABoxTest.
 *
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 *
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MetaMappingVirtualABoxTest {


	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/metamapping.owl";
	final String obdafile = "src/test/resources/test/metamapping.obda";

	@Before
	public void setUp() throws Exception {

		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		// Roman: changed the database name to avoid conflict with other tests (in .obda as well)
		String url = "jdbc:h2:mem:questjunitdb2;DATABASE_TO_UPPER=FALSE";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/metamapping-create-h2.sql");
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

		FileReader reader = new FileReader("src/test/resources/test/metamapping-drop-h2.sql");
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

	private void runTests(QuestPreferences p) throws Exception {

        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).build();
       

		String query1 = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x a :A_1 }";
		String query2 = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :P_1 ?y }";
        try (QuestOWL reasoner = factory.createReasoner(ontology, config);
             // Now we are ready for querying
             QuestOWLConnection conn = reasoner.getConnection();
             QuestOWLStatement st = conn.createStatement();
             QuestOWLResultSet rs1 = st.executeTuple(query1);
        ) {
            assertTrue(rs1.nextRow());
			OWLObject ind = rs1.getOWLObject("x");
			//OWLIndividual ind2 = rs.getOWLIndividual("y");
			//OWLLiteral val = rs.getOWLLiteral("z");
			assertEquals("<uri1>", ind.toString());
			//assertEquals("<uri1>", ind2.toString());
			//assertEquals("\"value1\"", val.toString());


		}

        try (QuestOWL reasoner = factory.createReasoner(ontology, config);
             // Now we are ready for querying
             QuestOWLConnection conn = reasoner.getConnection();
             QuestOWLStatement st = conn.createStatement();
             QuestOWLResultSet rs2 = st.executeTuple(query2);
        ) {
            assertTrue(rs2.nextRow());
			OWLObject ind1 = rs2.getOWLObject("x");
            //OWLIndividual ind2 = rs.getOWLIndividual("y");
			OWLObject val = rs2.getOWLObject("y");
            assertEquals("<uri1>", ind1.toString());
            //assertEquals("<uri1>", ind2.toString());
            assertEquals("\"A\"", val.toString());
        }
	}



    @Test
	public void testViEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");

		runTests(p);
	}

	@Test(expected = IllegalConfigurationException.class)
	public void testClassicEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");

		runTests(p);
	}


}
