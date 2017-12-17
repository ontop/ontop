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

import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.utils.querymanager.QueryIOManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.utils.querymanager.QueryController;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerEntity;
import it.unibz.inf.ontop.utils.querymanager.QueryControllerQuery;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

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
@Ignore // GUOHUI: 2016-01-16 SI+Mapping mode is disabled
public class TreeWitnessRewriterH2Test{

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private SQLPPMapping obdaModel = null;
	private OWLOntology ontology;

	List<TestQuery> testQueries = new LinkedList<TestQuery>();

	final String testCase = "twr-predicate";
	final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";
	final String qfile = "src/test/resources/test/treewitness/" + testCase + ".q";

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";

	/* These are the distinct tuples that we know each query returns */
	final int[] tuples = { 7, 0, 4, 1, // Simple queries group
			1, 2, 2, 1, 4, 3, 3, // CQs group
			0, 1, // Literal
			0, -1, 2, // String: Incompatible, Invalid, OK
			0, 2, 2, 0, 2, 2, 0, 0, 0, // Integer: (Incompatible, OK, OK);
										// (Incompatible, OK, OK);
										// (Incompatible, No result, No result)
			0, 1, 1, 0, 1, 1, 0, 1, 1, // Decimal: (Incompatible, OK, OK);
										// (Incompatible, OK, OK);
										// (Incompatible, OK, OK)
			0, 2, 2, 0, 2, 2, 0, 0, 0, // Double: (Incompatible, OK, OK);
										// (Incompatible, OK, OK);
										// (Incompatible, No result, No result)
			0, 0, 0, -1, -1, -1, -1, -1, 1, // Date time: (Incompatible,
											// Incompatible, Incompatible);
											// (Invalid, Invalid, Invalid);
											// (Invalid, Invalid, OK)
			0, 0, 0, 0, 5, 5, -1, 0, 5, -1, -1, 5, // Boolean: (Incompatible,
													// Incompatible,
													// Incompatible,
													// Incompatible); (OK, OK,
													// Invalid, Invalid); (OK,
													// Invalid, Invalid, OK)
			2, 5, // FILTER: String (EQ, NEQ)
			2, 5, 5, 7, 0, 2, // FILTER: Integer (EQ, NEQ, GT, GTE, LT, LTE)
			1, 3, 2, 3, 1, 2, // FILTER: Decimal (EQ, NEQ, GT, GTE, LT, LTE)
			2, 0, 0, 2, 0, 2, // FILTER: Double (EQ, NEQ, GT, GTE, LT, LTE)
			1, 3, 2, 3, 1, 2, // FILTER: Date Time (EQ, NEQ, GT, GTE, LT, LTE)
			5, 5, // FILTER: Boolean (EQ, NEQ)
			10, // FILTER: LangMatches
			1, 2, 1, 3, 2, // Nested boolean expression
			3, 3, 5, 5, 3, 7, 7, 7, 3, 10 // Query modifiers: LIMIT, OFFSET, and
											// ORDER BY
	};

	public class TestQuery {
		public String id = "";
		public String query = "";
		public int distinctTuples = -1;
	}

	public class Result {
		public String id = "";
		public String query = "";
		public int distinctTuples = -1;
		public long timeelapsed = -1;
	}

	@Before
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/treewitness/bsbm-schema-create-mysql.sql");
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

		FileReader reader = new FileReader("src/test/resources/test/treewitness/bsbm-schema-drop-mysql.sql");
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

	private void prepareTestQueries(int[] answer) throws Exception {
		/*
		 * Loading the queries (we have 61 queries)
		 */
		QueryController qcontroller = new QueryController();
		QueryIOManager qman = new QueryIOManager(qcontroller);

		qman.load(new File(qfile));

		int counter = 0;
		// for (QueryControllerGroup group : qcontroller.getGroups()) {
		for (QueryControllerEntity entity : qcontroller.getElements()) {
			if (!(entity instanceof QueryControllerQuery))
				continue;
			QueryControllerQuery query = (QueryControllerQuery) entity;
			TestQuery tq = new TestQuery();
			tq.id = query.getID();
			tq.query = query.getQuery();
			tq.distinctTuples = answer[counter];
			testQueries.add(tq);
			counter += 1;
		}
		// }
	}

	private void runTests(Properties p) throws Exception {

		OntopSQLOWLAPIConfiguration obdaConfiguration = OntopSQLOWLAPIConfiguration.defaultBuilder().properties(p)
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		OntopOWLReasoner reasoner;
		try (OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadVirtualAbox(obdaConfiguration, p)) {
			OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
			reasoner = factory.createReasoner(loader.getConfiguration());
		}

		// Now we are ready for querying
		OWLConnection owlConnection = reasoner.getConnection();
		OWLStatement st = owlConnection.createStatement();

		List<Result> summaries = new LinkedList<TreeWitnessRewriterH2Test.Result>();

		boolean fail = false;

		int qc = 0;
		for (TestQuery tq : testQueries) {
			log.debug("Executing query: {}", qc);
			String query = tq.query;
			log.debug("Query: {}", query);

			qc += 1;

			int count = 0;
			long start = System.currentTimeMillis();
			long end = 0;
			try {

				if (query.toLowerCase().contains("select")) {
					TupleOWLResultSet rs = st.executeSelectQuery(query);

					end = System.currentTimeMillis();
					while (rs.hasNext()) {
						count += 1;
					}
				} else {
					GraphOWLResultSet rs = st.executeGraphQuery(query);
					while (rs.hasNext()) {
						rs.next();
						count++;
					}
				}
			} catch (Exception e) {
				fail = true;
				log.debug(e.getMessage(), e);
				end = System.currentTimeMillis();
				count = -1;
			}

			Result summary = new Result();
			summary.id = tq.id;
			summary.query = query;
			summary.timeelapsed = end - start;
			summary.distinctTuples = count;
			summaries.add(summary);
		}

		/* Closing resources */
		st.close();
		owlConnection.close();
		reasoner.dispose();

		// /* Comparing and printing results */
		//
		// int totaltime = 0;
		// for (int i = 0; i < testQueries.size(); i++) {
		// TestQuery tq = testQueries.get(i);
		// Result summary = summaries.get(i);
		// totaltime += summary.timeelapsed;
		// fail = fail | tq.distinctTuples != summary.distinctTuples;
		// String out =
		// "Query: %3d   Tup. Ex.: %6d Tup. ret.: %6d    Time elapsed: %6.3f s     ";
		// log.debug(String.format(out, i, tq.distinctTuples,
		// summary.distinctTuples, (double) summary.timeelapsed / (double) 1000)
		// + "   " + (tq.distinctTuples == summary.distinctTuples ? " " :
		// "ERROR"));
		// }
		// log.debug("==========================");
//		log.debug(String.format("Total time elapsed: %6.3f s", (double) totaltime / (double) 1000));
		assertFalse(fail);
	}

    @Test
	public void testViEqSig() throws Exception {

		prepareTestQueries(tuples);
		Properties p  = new Properties();
		p.setProperty(OntopReformulationSettings.EXISTENTIAL_REASONING, "true");

		runTests(p);
	}

}
