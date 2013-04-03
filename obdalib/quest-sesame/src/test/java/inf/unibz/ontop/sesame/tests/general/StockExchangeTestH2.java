package inf.unibz.ontop.sesame.tests.general;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.QueryStorageManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
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

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriterFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sesameWrapper.SesameVirtualRepo;

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
public class StockExchangeTestH2 extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	List<TestQuery> testQueries = new LinkedList<TestQuery>();

	final String owlfile = "src/test/resources/stockexchange-unittest.owl";
	final String obdafile = "src/test/resources/stockexchange-h2-unittest.obda";

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

	@Override
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader(
				"src/test/resources/stockexchange-create-h2.sql");
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
		ontology = manager
				.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(new File(obdafile));
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

		FileReader reader = new FileReader(
				"src/test/resources/stockexchange-drop-h2.sql");
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

	private void prepareTestQueries(int[] answer) throws Exception {
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

		// //create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;

		repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
		repo.initialize();
		con = repo.getConnection();

		List<Result> summaries = new LinkedList<StockExchangeTestH2.Result>();
		TupleQueryResultWriterFactory fac = new SPARQLResultsJSONWriterFactory();
		TupleQueryResultWriter writer = fac.getWriter(System.out);
		

		int qc = 0;
		for (TestQuery tq : testQueries) {
			log.debug("Executing query: {}", qc);
			log.debug("Query: {}", tq.query);

			qc += 1;

			int count = 0;
			long start = System.currentTimeMillis();
			long end = 0;
			TupleQueryResult result = null;
			try {
				TupleQuery tupleQuery = con.prepareTupleQuery(
						QueryLanguage.SPARQL, tq.query);
				result = tupleQuery.evaluate();

				end = System.currentTimeMillis();
				writer.startQueryResult(result.getBindingNames());
				while (result.hasNext()) {
					BindingSet binding = result.next();
					count += 1;
					
					writer.handleSolution(binding);

				}
				writer.endQueryResult();
			} catch (Exception e) {
				log.debug(e.getMessage(), e);
				end = System.currentTimeMillis();
				count = -1;
			} finally {
				if (result != null)
					try {
						result.close();
					} catch (Exception e) {
					}
			}

			Result summary = new Result();
			summary.id = tq.id;
			summary.query = tq.query;
			summary.timeelapsed = end - start;
			summary.distinctTuples = count;
			summaries.add(summary);
		}

		/* Closing resources */
		con.close();
		repo.shutDown();

		boolean fail = false;
		/* Comparing and printing results */

		int totaltime = 0;
		for (int i = 0; i < testQueries.size(); i++) {
			TestQuery tq = testQueries.get(i);
			Result summary = summaries.get(i);
			totaltime += summary.timeelapsed;
			fail = fail | tq.distinctTuples != summary.distinctTuples;
			String out = "Query: %3d   Tup. Ex.: %6d Tup. ret.: %6d    Time elapsed: %6.3f s     ";
			log.debug(String.format(out, i, tq.distinctTuples,
					summary.distinctTuples, (double) summary.timeelapsed
							/ (double) 1000)
					+ "   "
					+ (tq.distinctTuples == summary.distinctTuples ? " "
							: "ERROR"));
		}
		log.debug("==========================");
		log.debug(String.format("Total time elapsed: %6.3f s",
				(double) totaltime / (double) 1000));
		assertFalse(fail);
	}

	public void testViEqSig() throws Exception {

		prepareTestQueries(tuples);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}

}
