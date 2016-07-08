package it.unibz.inf.ontop.reformulation.tests;

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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.ClassAssertion;
import it.unibz.inf.ontop.ontology.ObjectPropertyAssertion;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.abox.QuestMaterializer;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class QuestOWLMaterializerTest extends TestCase {

	private Connection jdbcconn = null;

	private Logger log =  LoggerFactory.getLogger(this.getClass());

    public QuestOWLMaterializerTest() {
    }

	@Override
	public void setUp() throws Exception {
		createTables();
	}

	private String readSQLFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		StringBuilder bf = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = reader.readLine();
		}
		return bf.toString();
	}

	private void createTables() throws IOException, SQLException, URISyntaxException {
		String createDDL = readSQLFile("src/test/resources/test/materializer/createMaterializeTest.sql");

		// Initializing and H2 database with the data		
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";
		
		jdbcconn = DriverManager.getConnection(url, username, password);
		Statement st = jdbcconn.createStatement();

		st.executeUpdate(createDDL);
		jdbcconn.commit();
	}

	@Override
	public void tearDown() throws Exception {

			dropTables();
//			 conn.close();
			jdbcconn.close();
	}

	private void dropTables() throws SQLException, IOException {
		String dropDDL = readSQLFile("src/test/resources/test/materializer/dropMaterializeTest.sql");
		Statement st = jdbcconn.createStatement();
		st.executeUpdate(dropDDL);
		st.close();
		jdbcconn.commit();
	}

	public void testDataWithModel() throws Exception {
	
	    File f = new File("src/test/resources/test/materializer/MaterializeTest.obda");

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/test/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.build();

        /*
         * Load the OBDA model from an external .obda file
         */
        OBDAModel model = configuration.loadInputMappings().orElseThrow(IllegalStateException::new);

			QuestMaterializer mat = new QuestMaterializer(model, false);
			Iterator<Assertion> iterator = mat.getAssertionIterator();
			int classAss = 0;
			int propAss = 0;
			int objAss = 0;
			while (iterator.hasNext()) {
				Assertion assertion = iterator.next();
				if (assertion instanceof ClassAssertion)
					classAss++;
				
				else if (assertion instanceof ObjectPropertyAssertion)
					objAss++;
				
				else // DataPropertyAssertion
					propAss++;
			}
			Assert.assertEquals(3, classAss); //3 data rows for T1
			Assert.assertEquals(21, propAss); //7 tables * 3 data rows each T2-T8
			Assert.assertEquals(3, objAss); //3 data rows for T9

	}
	
	public void testDataWithModelAndOnto() throws Exception {

			// read model
			File f = new File("src/test/resources/test/materializer/MaterializeTest.obda");

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/test/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.build();

            OBDAModel model = configuration.loadInputMappings()
					.orElseThrow(IllegalStateException::new);

			// read onto
			Ontology onto =  OWLAPITranslatorUtility.translateImportsClosure(
					configuration.loadProvidedInputOntology());
			System.out.println(onto.getSubClassAxioms());
			System.out.println(onto.getSubObjectPropertyAxioms());
			System.out.println(onto.getSubDataPropertyAxioms());
			
			QuestMaterializer mat = new QuestMaterializer(model, onto, false);
			Iterator<Assertion> iterator = mat.getAssertionIterator();
			int classAss = 0;
			int propAss = 0;
			int objAss = 0;
			while (iterator.hasNext()) {
				Assertion assertion = iterator.next();
				if (assertion instanceof ClassAssertion) 
					classAss++;
	
				else if (assertion instanceof ObjectPropertyAssertion) 
					objAss++;
				
				else // DataPropertyAssertion
					propAss++;
			}
			Assert.assertEquals(6, classAss); //3 data rows x2 for subclass prop
			Assert.assertEquals(42, propAss); //8 tables * 3 data rows each x2 for subclass
			Assert.assertEquals(3, objAss); //3 since no subprop for obj prop

	}
}
