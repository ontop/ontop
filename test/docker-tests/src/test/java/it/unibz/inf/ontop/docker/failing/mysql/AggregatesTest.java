package it.unibz.inf.ontop.docker.failing.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Ignore("Not supported yet")
public class AggregatesTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	private static final String obdafile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";
	private static final String propertiesfile = "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mysql.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertiesfile);
		CONNECTION = REASONER.getConnection();
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}

//	@Override
//	public void setUp() throws Exception {
//
//		/*
//		 * Initializing and H2 database with the stock exchange data
//		 */
//		String driver = "com.mysql.jdbc.Driver";
//		String url = "jdbc:mysql://10.7.20.39/stockexchange_new";
//		String username = "fish";
//		String password = "fish";
//
//		//?sessionVariables=sql_mode='ANSI'
//
//		fac = OBDADataFactoryImpl.getInstance();
//
//		conn = DriverManager.getConnection(url, username, password);
//		conn.setAutoCommit(true);
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/stockexchange-create-mysql.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line + "\n");
//			line = in.readLine();
//		}
//		String[] sqls = bf.toString().split(";");
//		for (String st_sql: sqls) {
//			st.addBatch(st_sql);
//		}
//		//st.executeBatch();
//		//st.executeUpdate(bf.toString());
//		//conn.commit();
//
//		// Loading the OWL file
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
//
//		// Loading the OBDA data
//		obdaModel = fac.parse();
//
//		ModelIOManager ioManager = new ModelIOManager(obdaModel);
//		ioManager.load(obdafile);
//	}
//
//	@Override
//	public void tearDown() throws Exception {
//		try {
//		//	dropTables();
//			conn.close();
//		} catch (Exception e) {
//			log.debug(e.getMessage());
//		}
//	}
//
//	private void dropTables() throws SQLException, IOException {
//
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/stockexchange-drop-mysql.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//
//		st.executeUpdate(bf.toString());
//		st.close();
//		conn.commit();
//	}

	@Test
	public void testAggrCount() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		countResults(1, query);
	}


	@Test
	public void testAggrCount2() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";
		countResults(1, query);
	}

	@Test
	public void testAggrCount3() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x (COUNT(?value) AS ?count) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value } GROUP BY ?x";
		countResults(4, query);
	}

	@Test
	public void testAggrCount4() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x (COUNT(?y) AS ?count) WHERE { ?x :belongsToCompany ?y } GROUP BY ?x";
		//String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x ?y WHERE { ?x :belongsToCompany ?y } ";
		countResults(10, query);
	}

	@Test
	public void testAggrCount5() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (COUNT(?x) AS ?count) WHERE {?x a :Transaction. }";
		countResults(1, query);
	}
	
	/*
	public void testAggrCount5() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> " +
				"SELECT ?x (COUNT(?value) AS ?count) " +
				"WHERE {?x a :Transaction. ?x :amountOfTransaction ?value } " +
				"GROUP BY ?x HAVING (?value > 0)";

		runTests(p,query,3);

	}
	
*/

	@Test
	public void testAggrAVG() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?broker (AVG(?value) AS ?vavg) WHERE {?x :isExecutedBy ?broker. ?x :amountOfTransaction ?value } GROUP BY ?broker";
		countResults(1, query);
	}

	@Test
	public void testAggrSUM() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (SUM(?value) AS ?sum) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		countResults(1, query);
	}

	@Test
	public void testAggrMIN() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (MIN(?value) AS ?min) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		countResults(1, query);
	}

	@Test
	public void testAggrMAX() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT (MAX(?value) AS ?max) WHERE {?x a :Transaction. ?x :amountOfTransaction ?value }";
		countResults(1, query);
	}
}
