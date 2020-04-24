package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertTrue;


public class H2ASKTest {

	private static final String owlFile = "src/test/resources/stockexchange/stockexchange.owl";
	private static final String obdaFile = "src/test/resources/stockexchange/stockexchange-h2.obda";

	private OntopOWLReasoner reasoner;
	private OWLConnection conn;
	private Connection sqlConnection;

	@Before
	public void setUp() throws Exception {

		String url = "jdbc:h2:mem:questrepository";
		String user = "fish";
		String password = "fish";
		sqlConnection = DriverManager.getConnection(url, user, password);

		executeFromFile(sqlConnection,"src/test/resources/stockexchange/stockexchange-create-h2.sql");

		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.jdbcUrl(url)
				.jdbcUser(user)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			try (java.sql.Statement s = sqlConnection.createStatement()) {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			}
			finally {
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
		try (OWLStatement st = conn.createStatement()) {
			BooleanOWLResultSet rs = st.executeAskQuery(query);
			boolean retval = rs.getValue();
			return retval;
		}
	}
}