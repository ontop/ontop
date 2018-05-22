package it.unibz.inf.ontop.owlapi.sql;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * Test to check the correct parsing of IN and INTERSECT in SQLTableNameExtractor
 * to retrieve subselect queries
 *
 */

public class SQLInAndIntersectionTest {
	private OntopOWLConnection conn;
	private OntopOWLReasoner reasoner;


	final String owlfile = "src/test/resources/sqlin/routes.owl";
	final String obdafile = "src/test/resources/sqlin/routes.obda";

	private Connection sqlConnection;
	private static final String URL = "jdbc:h2:mem:routes";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	@After
	public void tearDown() throws Exception{
		if (conn != null)
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


	@Before
	public void setUp() throws Exception {
		sqlConnection= DriverManager.getConnection(URL, USER, PASSWORD);
		java.sql.Statement s = sqlConnection.createStatement();

		try {
			String text = new Scanner( new File("src/test/resources/sqlin/create-h2.sql") ).useDelimiter("\\A").next();
			s.execute(text);
			//Server.startWebServer(sqlConnection);

		} catch(SQLException sqle) {
			System.out.println("Exception in creating db from script");
		}

		s.close();
		// Loading the OWL file

		// Creating a new instance of the reasoner
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.nativeOntopMappingFile(obdafile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.build();
		reasoner = factory.createReasoner(config);

		// Now we are ready for querying
		conn = reasoner.getConnection();
	}


	/**
	 * Test to check the correct parsing of IN and INTERSECT for SQLTableNameExtractor
	 * @throws Exception
	 */
	@Test
	public void testComplexTableNamesRetrieval() throws Exception {

		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Route}";
		OntopOWLStatement st = conn.createStatement();


		String sql = st.getExecutableQuery(query).toString();
		boolean in = sql.matches("(?ms)(.*)IN(.*)");
		assertTrue(in);

		String query2 = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Vertex}";

		String sql2 = st.getExecutableQuery(query2).toString();
		boolean intersect = sql2.matches("(?ms)(.*)INTERSECT(.*)");
		assertTrue(intersect);

		st.close();
	}


}
