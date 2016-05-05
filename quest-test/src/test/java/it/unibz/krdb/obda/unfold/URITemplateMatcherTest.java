package it.unibz.krdb.obda.unfold;

/*
 * #%L
 * ontop-reformulation-core
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
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import it.unibz.krdb.obda.utils.SQLScriptRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class URITemplateMatcherTest {

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlFile = "src/test/resources/oboe-core.owl";
	final String obdaFile = "src/test/resources/oboe-coreURIconstants.obda";

	private static Connection sqlConnection;


	@Before
	public void setUp() throws Exception {

		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "test";

		fac = OBDADataFactoryImpl.getInstance();

		sqlConnection = DriverManager
				.getConnection(url, username, password);

		FileReader reader = new FileReader("src/test/resources/smallDatasetURIconstants.sql");
		BufferedReader in = new BufferedReader(reader);
		SQLScriptRunner runner = new SQLScriptRunner(sqlConnection, true, false);
		runner.runScript(in);

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdaFile);

	}

	@After
	public void tearDown() throws Exception {

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

	@Test
	public void testURIConstant() throws Exception {
		QuestPreferences p = new QuestPreferences();

		String queryBind = "PREFIX : <http://www.ola.fr#>\n" +
				"  PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n" +
				"  SELECT ?s {\n" +
				"     ?s a oboe-core:Measurement ; oboe-core:usesStandard \n" +
				"             <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Unit> . \n" +
				"  }";



		String results = runTestQuery(p, queryBind);
		assertEquals("<http://www.ola.fr#measurement/unit/name/1>", results);
	}

	@Test
	public void testURIConstant2() throws Exception {
		QuestPreferences p = new QuestPreferences();

		String queryBind = "PREFIX : <http://www.ola.fr#>\n" +
				"  PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n" +
				"  SELECT ?s {\n" +
				"     <http://www.ola.fr#measurement/unit/name/1> a oboe-core:Measurement ; oboe-core:hasValue ?s \n" +
				"  }";



		String results = runTestQuery(p, queryBind);
		assertEquals("<http://urlconstants.org/32>", results);
	}


	private String runTestQuery(QuestPreferences p, String query) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).queryingAnnotationsInOntology(true).build();
		QuestOWL reasoner = factory.createReasoner(ontology, config);

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();


		log.debug("Executing query: ");
		log.debug("Query: \n{}", query);

		long start = System.nanoTime();
		QuestOWLResultSet res = st.executeTuple(query);
		long end = System.nanoTime();

		double time = (end - start) / 1000;
		String result = "";
		int count = 0;
		while (res.nextRow()) {
			count += 1;
			if (count == 1) {
				for (int i = 1; i <= res.getColumnCount(); i++) {
					log.debug("Example result " + res.getSignature().get(i - 1) + " = " + res.getOWLObject(i));

				}
				result = ToStringRenderer.getInstance().getRendering(res.getOWLObject("s"));
			}
		}
		log.debug("Total results: {}", count);

		assertFalse(count == 0);

		log.debug("Elapsed time: {} ms", time);

		conn.close();
		reasoner.dispose();

		return result;



	}

}
