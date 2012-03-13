package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.QueryStorageManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;

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

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
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
 * 
 * 
 * @author mariano
 * 
 */
public class StockExchangeTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	List<TestQuery> testQueries = new LinkedList<TestQuery>();

	final String owlfile = "src/test/resources/test/stockexchange-unittest.owl";
	final String obdafile = "src/test/resources/test/stockexchange-unittest.obda";
	
	/* These are the distinct tuples that we know each query returns */
	final int[] tuples = { 
			7, 0, 4, 1,								// Simple queries group
			1, 2, 2, 1, 4, 3, 3, 					// CQs group
			0, -1, 2, 								// String: Incompatible, Invalid, OK
			0, 2, 2, 0, 2, 2, 0, 0, 0, 		    	// Integer: (Incompatible, OK, OK); (Incompatible, OK, OK); (Incompatible, No result, No result)
			0, 1, 1, 0, 1, 1, 0, 1, 1,  			// Decimal: (Incompatible, OK, OK); (Incompatible, OK, OK); (Incompatible, OK, OK)
			0, 2, 2, 0, 2, 2, 0, 0, 0,  			// Double: (Incompatible, OK, OK); (Incompatible, OK, OK); (Incompatible, No result, No result)
			0, 0, 0, -1, -1, -1, -1, -1, 1,  	 	// Date time: (Incompatible, Incompatible, Incompatible); (Invalid, Invalid, Invalid); (Invalid, Invalid, OK)
			0, 0, 0, 0, 5, 5, -1, 0, 5, -1, -1, 5,  // Boolean: (Incompatible, Incompatible, Incompatible, Incompatible); (OK, OK, Invalid, Invalid); (OK, Invalid, Invalid, OK)
            2, 5,								    // FILTER: String (EQ, NEQ)
            2, 5, 5, 7, 0, 2,					    // FILTER: Integer (EQ, NEQ, GT, GTE, LT, LTE)
            1, 3, 2, 3, 1, 2,					    // FILTER: Decimal (EQ, NEQ, GT, GTE, LT, LTE)
            2, 0, 0, 2, 0, 2,					    // FILTER: Double (EQ, NEQ, GT, GTE, LT, LTE)
            1, 3, 2, 3, 1, 2,					    // FILTER: Date Time (EQ, NEQ, GT, GTE, LT, LTE)
            5, 5								    // FILTER: Boolean (EQ, NEQ)
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

	@Override
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
//		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:stockclient1";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-postgres.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		DataManager ioManager = new DataManager(obdaModel);
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getOntologyID().getOntologyIRI().toURI(), obdaModel.getPrefixManager());
	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}
	
	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-drop-postgres.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}

	private void prepareTestQueries(int[] answer) {
		/*
		 * Loading the queries (we have 61 queries)
		 */
		QueryController qcontroller = new QueryController();
		QueryStorageManager qman = new QueryStorageManager(qcontroller);

		qman.loadQueries(new File(obdafile).toURI());
		
		int counter = 0;
		for (QueryControllerGroup group : qcontroller.getGroups()) {
			for (QueryControllerQuery query : group.getQueries()) {
				TestQuery tq = new TestQuery();
				tq.id = query.getID();
				tq.query = query.getQuery();
				tq.distinctTuples = answer[counter];
				testQueries.add(tq);
				counter += 1;
			}
		}
	}
	
	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
	
		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		reasoner.loadOBDAModel(obdaModel);

		// Now we are ready for querying
		OBDAStatement st = reasoner.getStatement();

		List<Result> summaries = new LinkedList<StockExchangeTest.Result>();

		int qc = 0;
		for (TestQuery tq : testQueries) {
			log.debug("Executing query: {}", qc);
			log.debug("Query: {}", tq.query);
			
			qc += 1;
			
			int count = 0;
			long start = System.currentTimeMillis();
			long end = 0;
			try {
				OBDAResultSet rs = st.execute(tq.query);
				end = System.currentTimeMillis();
				while (rs.nextRow()) {
					count += 1;
				}
			} catch (Exception e) {
				end = System.currentTimeMillis();
				count = -1;
			}

			Result summary = new Result();
			summary.id = tq.id;
			summary.query = tq.query;
			summary.timeelapsed = end - start;
			summary.distinctTuples = count;
			summaries.add(summary);
		}

		/* Closing resources */
		st.close();
		reasoner.dispose();

		boolean fail = false;
		/* Comparing and printing results */

		int totaltime = 0;
		for (int i = 0; i < testQueries.size(); i++) {
			TestQuery tq = testQueries.get(i);
			Result summary = summaries.get(i);
			totaltime += summary.timeelapsed;
			fail = fail | tq.distinctTuples != summary.distinctTuples;
			String out = "Query: %3d   Tup. Ex.: %6d Tup. ret.: %6d    Time elapsed: %6.3f s     ";
			log.debug(String.format(out, i, tq.distinctTuples, summary.distinctTuples, (double) summary.timeelapsed / (double) 1000)
					+ "   " + (tq.distinctTuples == summary.distinctTuples ? " " : "ERROR"));
		}
		log.debug("==========================");
		log.debug(String.format("Total time elapsed: %6.3f s", (double) totaltime / (double) 1000));
		assertFalse(fail);
	}


    
	public void testSiEqSig() throws Exception {
		
		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);
	}

	public void testSiEqNoSig() throws Exception {
	    
		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);
	}

	public void testSiNoEqSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);
	}

	public void testSiNoEqNoSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);
	}

	/*
	 * Direct
	 */
	public void disabletestDiEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(p);
	}

	public void disabletestDiEqNoSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(p);
	}

	/***
	 * This is a very slow test, disable it if you are doing rutine checks.
	 * 
	 * @throws Exception
	 */
	public void disabletestDiNoEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(p);
	}

	/***
	 * This is a very slow test, disable it if you are doing rutine checks.
	 * 
	 * @throws Exception
	 */
	public void disabletestDiNoEqNoSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(p);
	}

	public void testViEqSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}

	public void testViEqNoSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");

		runTests(p);
	}

	/***
	 * This is a very slow test, disable it if you are doing rutine checks.
	 * 
	 * @throws Exception
	 */
	public void testViNoEqSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}

	/***
	 * This is a very slow test, disable it if you are doing rutine checks.
	 * 
	 * @throws Exception
	 */
	public void testViNoEqNoSig() throws Exception {

		prepareTestQueries(tuples);
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");

		runTests(p);
	}
}
