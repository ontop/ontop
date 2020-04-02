package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static junit.framework.TestCase.assertTrue;


public class H2ASKTest {

	 static final String owlFile = "src/test/resources/stockexchange/stockexchange.owl";
	 static final String obdaFile = "src/test/resources/stockexchange/stockexchange-h2.obda";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		String url = "jdbc:h2:mem:questrepository";
		String user = "fish";
		String password = "fish";
		sqlConnection = DriverManager.getConnection(url, user, password);

		try (java.sql.Statement s = sqlConnection.createStatement()) {
			String text = new Scanner( new File("src/test/resources/stockexchange/stockexchange-create-h2.sql") ).useDelimiter("\\A").next();
			s.execute(text);
		}

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.jdbcUrl(url)
				.jdbcUser(user)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();
	}

	@After
	public void tearDown() throws Exception {
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

	@Test
	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  runQueryAndReturnBooleanX(query);
		assertTrue(val);
		
	}

	private boolean runQueryAndReturnBooleanX(String query) throws Exception {
		OWLStatement st = conn.createStatement();
		boolean retval;
		try {
			BooleanOWLResultSet rs = st.executeAskQuery(query);
			retval = rs.getValue();
		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				assertTrue(false);
			}
			conn.close();
			reasoner.dispose();
		}
		return retval;
	}
}