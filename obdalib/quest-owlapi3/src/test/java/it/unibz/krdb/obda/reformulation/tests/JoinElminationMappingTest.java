package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
public class JoinElminationMappingTest extends TestCase {

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/ontologies/scenarios/join-elimination-test.owl";
	final String obdafile = "src/test/resources/test/ontologies/scenarios/join-elimination-test.obda";

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

		String createStr = "CREATE TABLE \"address\" (" + "\"id\" integer NOT NULL," + "    \"street\" character varying(100),"
				+ "   \"number\" integer," + "  \"city\" character varying(100)," + " \"state\" character varying(100),"
				+ "\"country\" character varying(100), PRIMARY KEY(\"id\")" + ");";

		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-h2.sql");

		st.executeUpdate(createStr);
		conn.commit();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		DataManager ioManager = new DataManager(obdaModel);
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getOntologyID().getOntologyIRI().toURI(),
				obdaModel.getPrefixManager());
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

		st.executeUpdate("DROP TABLE \"address\";");
		st.close();
		conn.commit();
	}

	// private void prepareTestQueries(int[] answer) {
	// /*
	// * Loading the queries (we have 61 queries)
	// */
	// QueryController qcontroller = new QueryController();
	// QueryStorageManager qman = new QueryStorageManager(qcontroller);
	//
	// qman.loadQueries(new File(obdafile).toURI());
	//
	// int counter = 0;
	// for (QueryControllerGroup group : qcontroller.getGroups()) {
	// for (QueryControllerQuery query : group.getQueries()) {
	// TestQuery tq = new TestQuery();
	// tq.id = query.getID();
	// tq.query = query.getQuery();
	// tq.distinctTuples = answer[counter];
	// testQueries.add(tq);
	// counter += 1;
	// }
	// }
	// }

	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		reasoner.loadOBDAModel(obdaModel);
		reasoner.flush();

		// Now we are ready for querying
		QuestOWLStatement st = (QuestOWLStatement)reasoner.getStatement();

		boolean fail = false;

		int count = 0;
		long start = System.currentTimeMillis();
		long end = 0;
		String query = "PREFIX : <http://it.unibz.krdb/obda/ontologies/join-elimination-test.owl#> \n" +
				"SELECT ?x WHERE {?x :R ?y. ?y a :A}";
		try {
			System.out.println("\n\nSQL:\n" + st.getUnfolding(query));
			OWLResultSet rs = st.execute(query);
			end = System.currentTimeMillis();
			while (rs.nextRow()) {
				count += 1;
			}
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

	public void testSiEqNoSig() throws Exception {

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

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}

	public void testViEqNoSig() throws Exception {

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

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "false");

		runTests(p);
	}
}
