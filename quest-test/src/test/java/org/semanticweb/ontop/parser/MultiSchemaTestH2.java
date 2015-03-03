package org.semanticweb.ontop.parser;

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

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import junit.framework.TestCase;
import org.junit.After;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class MultiSchemaTestH2 extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private QuestOWLConnection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

    final String owlfile =
            "src/test/resources/multischemadb2.owl";
    final String obdafile =
            "src/test/resources/multischemah2.obda";
	private QuestOWL reasoner;

    private Connection sqlConnection;

	@Override
	public void setUp() throws Exception {

        String url = "jdbc:h2:mem:questrepository;";
        String username = "fish";
        String password = "fish";

        fac = OBDADataFactoryImpl.getInstance();

        sqlConnection = DriverManager.getConnection(url, username, password);
        Statement st = sqlConnection.createStatement();

        FileReader reader = new FileReader("src/test/resources/stockexchange-h2Schema.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }

        st.execute("DROP ALL OBJECTS DELETE FILES");
        st.executeUpdate(bf.toString());
        sqlConnection.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
	
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		conn = reasoner.getConnection();

		
	}


    @After
    public void tearDown() throws Exception {
        conn.close();
        reasoner.dispose();
        if (!sqlConnection.isClosed()) {
            java.sql.Statement s = sqlConnection.createStatement();
            try {
                s.execute("DROP ALL OBJECTS DELETE FILES");
            } catch (SQLException sqle) {
                System.out.println("Table not found, not dropping");
            } finally {
                s.close();
                sqlConnection.close();
            }
        }

    }
	

	
	private void runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		StringBuilder bf = new StringBuilder(query);
		try {
			

			QuestOWLResultSet rs = st.executeTuple(query);
			/*
			boolean nextRow = rs.nextRow();
			
			*/
			assertTrue(rs.nextRow());
//			while (rs.nextRow()){
//				OWLIndividual ind1 =	rs.getOWLIndividual("x")	 ;
//				System.out.println(ind1.toString());
//			}
		
/*
			assertEquals("<uri1>", ind1.toString());
			assertEquals("<uri1>", ind2.toString());
			assertEquals("\"value1\"", val.toString());
	*/		

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			conn.close();
			reasoner.dispose();
		}
	}

	/**
	 * Test use of two aliases to same table
	 * @throws Exception
	 */
	public void testOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Address}";
		runTests(query);
	}
	
	public void testTableOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Broker}";
		runTests(query);
	}
	
	public void testAliasOneSchema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :Worker}";
		runTests(query);
	}
	
	public void testSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x ?r WHERE { ?x :isBroker ?r }";
		runTests(query);
	}
	
	public void testMultischema() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE { ?x :hasFile ?r }";
		runTests(query);
	}
	
		
}
