package org.semanticweb.ontop.reformulation.tests;

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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The following tests take the Stock exchange scenario and execute the queries
 * of the scenario to validate the results. The validation is simple, we only
 * count the number of distinct tuples returned by each query, which we know in
 * advance.
 * 
 * We execute the scenario in different modes, virtual, classic, with and
 * without optimizations.
 * 
 * The data is obtained from an inmemory database with the stock exchange
 * tuples. If the scenario is run in classic, this data gets imported
 * automatically by the reasoner.
 */
public class JoinElminationMappingTest extends TestCase {
	private Connection conn;

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/ontologies/scenarios/join-elimination-test.owl";
	final String obdafile = "src/test/resources/test/ontologies/scenarios/join-elimination-test.obda";

	@Override
	public void setUp() throws Exception {
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		String createStr = 
				"CREATE TABLE address (" + "id integer NOT NULL," + "street character varying(100),"
				+ "number integer," + "city character varying(100)," + "state character varying(100),"
				+ "country character varying(100), PRIMARY KEY(id)" + ");";

		st.executeUpdate(createStr);
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
	}

	@Override
	public void tearDown() throws Exception {
			dropTables();
			conn.close();
	}

	private void dropTables() throws SQLException, IOException {
		Statement st = conn.createStatement();
		st.executeUpdate("DROP TABLE address;");
		st.close();
		conn.commit();
	}
	
	private void runTests(QuestPreferences p) throws Exception {
		// Creating a new instance of the reasoner
		QuestOWLFactory factory;
		if (p.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL) ||
				p.getBoolean(QuestPreferences.OBTAIN_FROM_MAPPINGS)) {
			factory = new QuestOWLFactory(new File(obdafile), p);
		}
		else {
			factory = new QuestOWLFactory(p);
		}

		QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());
		reasoner.flush();

		// Now we are ready for querying
		QuestOWLStatement st = reasoner.getStatement();

		boolean fail = false;

		String query = 
				"PREFIX : <http://it.unibz.krdb/obda/ontologies/join-elimination-test.owl#> \n" +
				"SELECT ?x WHERE {?x :R ?y. ?y a :A}";
		try {
			System.out.println("\n\nSQL:\n" + st.getUnfolding(query));
			QuestOWLResultSet rs = st.executeTuple(query);
			rs.nextRow();
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			fail = true;
		}
		/* Closing resources */
		st.close();
		reasoner.dispose();

		/* Comparing and printing results */
		assertFalse(fail);
	}

//	public void testSiEqSig() throws Exception {
//		Properties p  = new Properties();
//		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
//		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
//		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
//		runTests(new QuestPreferences(p));
//	}
//
//	public void testSiEqNoSig() throws Exception {
//		Properties p  = new Properties();
//		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
//		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
//		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
//		runTests(new QuestPreferences(p));
//	}
//
//	public void testSiNoEqSig() throws Exception {
//		Properties p  = new Properties();
//		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
//		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
//		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
//		runTests(new QuestPreferences(p));
//	}
//
//	public void testSiNoEqNoSig() throws Exception {
//		Properties p  = new Properties();
//		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
//		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
//		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
//		runTests(new QuestPreferences(p));
//	}

	/*
	 * Direct Mapping
	 */
	public void disabletestDiEqSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(new QuestPreferences(p));
	}

	public void disabletestDiEqNoSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(new QuestPreferences(p));
	}

	/**
	 * This is a very slow test, disable it if you are doing routine checks.
	 * 
	 * @throws Exception
	 */
	public void disabletestDiNoEqSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(new QuestPreferences(p));
	}

	/**
	 * This is a very slow test, disable it if you are doing routine checks.
	 * 
	 * @throws Exception
	 */
	public void disabletestDiNoEqNoSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setProperty(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setProperty(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setProperty(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(new QuestPreferences(p));
	}

	public void testViEqSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		runTests(new QuestPreferences(p));
	}

	public void testViEqNoSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		runTests(new QuestPreferences(p));
	}

	/**
	 * This is a very slow test, disable it if you are doing routine checks.
	 */
	public void testViNoEqSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		runTests(new QuestPreferences(p));
	}

	/**
	 * This is a very slow test, disable it if you are doing routine checks.
	 */
	public void testViNoEqNoSig() throws Exception {
		Properties p  = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		runTests(new QuestPreferences(p));
	}
}
