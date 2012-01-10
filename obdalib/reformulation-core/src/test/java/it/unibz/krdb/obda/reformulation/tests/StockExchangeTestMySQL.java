package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.QueryStorageManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi2.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi2.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.owlapi2.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi2.QuestOWLFactory;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Moreover, it requires the data to be already loaded in the database.
 * 
 * The test requires a postgres database, the JDBC access properties and an
 * account that is able to create and drop tables on the database.
 * 
 * @author mariano
 * 
 */
public class StockExchangeTestMySQL extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	// private OBDADataSource stockDB;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	List<TestQuery> testQueries = new LinkedList<TestQuery>();
	private String driver;
	private String url;
	private String username;
	private String password;

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

		driver = "com.mysql.jdbc.Driver";
		url = "jdbc:mysql://obdalin.inf.unibz.it/questjunitdb";
		username = "obda";
		password = "obda09";
		log.debug("Driver: {}", driver);
		log.debug("Url: {}", url);
		log.debug("Username: {}", username);
		log.debug("Password: {}", password);

		fac = OBDADataFactoryImpl.getInstance();
		// stockDB = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP"
		// + System.currentTimeMillis()));
		// stockDB.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER,
		// driver);
		// stockDB.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD,
		// password);
		// stockDB.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		// stockDB.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME,
		// username);
		// stockDB.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY,
		// "true");
		// stockDB.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP,
		// "true");

		conn = DriverManager.getConnection(url, username, password);

		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-mysql.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line + "\n");

			if (line.contains(";")) {
				st.executeUpdate(bf.toString());
				bf.setLength(0);
			}
			line = in.readLine();
		}
		System.out.println(bf);

		// conn.commit();

		/*
		 * Loading the ontology and obda model
		 */

		String owlfile = "src/test/resources/test/stockexchange-unittest.owl";
		String obdafile = "src/test/resources/test/stockexchange-mysql-unittest.obda";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		DataManager ioManager = new DataManager(obdaModel);
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), obdaModel.getPrefixManager());

		/*
		 * Loading the queries (we have 11 queries)
		 */

		QueryController qcontroller = new QueryController();
		QueryStorageManager qman = new QueryStorageManager(qcontroller);

		qman.loadQueries(new File(obdafile).toURI());

		/* These are the distinct tuples that we know each query returns */
		int[] tuples = { 7, 1, 4, 1, 1, 2, 2, 1, 4, 3, 3 };
		int current = 0;
		for (QueryControllerGroup group : qcontroller.getGroups()) {
			for (QueryControllerQuery query : group.getQueries()) {
				TestQuery tq = new TestQuery();
				tq.id = query.getID();
				tq.query = query.getQuery();
				tq.distinctTuples = tuples[current];
				testQueries.add(tq);
				current += 1;
			}
		}

	}

	@Override
	public void tearDown() throws Exception {
		try {
			dropTables();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

		try {
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
			if (line.contains(";")) {
				st.executeUpdate(bf.toString());
				bf.setLength(0);
			}
			line = in.readLine();

		}
		st.close();
	}

	private void runTests(QuestPreferences p) throws Exception {

		// Creating a new instance of the reasoner
		OBDAOWLReasonerFactory factory = new QuestOWLFactory();

		factory.setPreferenceHolder(p);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(manager);
		reasoner.setPreferences(p);
		reasoner.loadOntologies(Collections.singleton(ontology));
		reasoner.loadOBDAModel(obdaModel);

		// One time classification call.
		reasoner.classify();

		// Now we are ready for querying
		QuestStatement st = reasoner.getStatement();

		List<Result> summaries = new LinkedList<StockExchangeTestMySQL.Result>();

		int qc = 0;
		for (TestQuery tq : testQueries) {
			log.debug("Executing query: {}", qc);
			log.debug("Query: {}", tq.query);
			// if (qc == 7)
			// continue;
			qc += 1;

			int count = 0;
			// if (qc > 8) {
			long start = System.currentTimeMillis();
			OBDAResultSet rs = st.execute(tq.query);
			long end = System.currentTimeMillis();
			while (rs.nextRow()) {
				count += 1;
			}
			rs.close();
			log.debug("SQL Query: \n{}", st.getUnfolding(tq.query));

			Result summary = new Result();
			summary.id = tq.id;
			summary.query = tq.query;
			summary.timeelapsed = end - start;
			summary.distinctTuples = count;
			summaries.add(summary);
			// }
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

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);

	}

	public void disabledtestSiEqNoSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);

	}

	public void disabledtestSiNoEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		runTests(p);
	}

	public void disabledtestSiNoEqNoSig() throws Exception {

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

	public void testDiEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.DIRECT);
		runTests(p);

	}

	public void disabledtestDiEqNoSig() throws Exception {

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
	public void disabledtestDiNoEqSig() throws Exception {

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
	public void disabledtestDiNoEqNoSig() throws Exception {

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

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);

	}

	public void disabledtestViEqNoSig() throws Exception {

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
	public void disabledtestViNoEqSig() throws Exception {

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
	public void disabledtestViNoEqNoSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");

		runTests(p);
	}
}
