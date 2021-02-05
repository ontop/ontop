package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import static it.unibz.inf.ontop.injection.OntopSystemSettings.DEFAULT_QUERY_TIMEOUT;
import static org.junit.Assert.assertTrue;

/**
 * Tests that user-applied constraints can be provided through
 * sesameWrapper.OntopVirtualRepository
 * with manually instantiated metadata.
 *
 * This is quite similar to the setting in the optique platform
 * 
 * Some stuff copied from ExampleManualMetadata 
 * 
 * @author dhovl
 *
 */
public class RDF4JTimeoutTest {
	static String owlfile = "src/test/resources/userconstraints/uc.owl";
	static String obdafile = "src/test/resources/userconstraints/uc.obda";
	static String r2rmlfile = "src/test/resources/userconstraints/uc.ttl";

	static String uc_keyfile = "src/test/resources/userconstraints/keys.lst";
	static String uc_create = "src/test/resources/userconstraints/create.sql";

	private Connection sqlConnection;
	private RepositoryConnection conn;

	private static final String URL = "jdbc:h2:mem:countries";
	private static final String USER = "sa";
	private static final String PASSWORD = "";
	

	@Before
	public void init()  throws Exception {

		sqlConnection= DriverManager.getConnection(URL, USER, PASSWORD);

		try (java.sql.Statement s = sqlConnection.createStatement()) {
			try (Scanner sqlFile = new Scanner(new File(uc_create))) {
				String text = sqlFile.useDelimiter("\\A").next();
				s.execute(text);
			}

			for(int i = 1; i <= 10000; i++){
				s.execute("INSERT INTO TABLE1 VALUES (" + i + "," + i + ");");
			}
		}


		Properties properties = new Properties();
		properties.setProperty(DEFAULT_QUERY_TIMEOUT, "1");

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.r2rmlMappingFile(r2rmlfile)
				.ontologyFile(owlfile)
				.jdbcUrl(URL)
				.jdbcUser(USER)
				.jdbcPassword(PASSWORD)
				.enableTestMode()
				.properties(properties)
				.build();

		OntopRepository repo = OntopRepository.defaultRepository(configuration);
		repo.init();
		/*
		 * Prepare the data connection for querying.
		 */
		conn = repo.getConnection();
	}


	@After
	public void tearDown() throws Exception{
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
	public void testTimeout1() throws Exception {
		testTimeout(false);
	}

	@Test
	public void testTimeout2() throws Exception {
		testTimeout(true);
	}

	private void testTimeout(boolean useDefault) throws Exception {
		String queryString = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal2 ?v1; :hasVal2 ?v2.}";

		// execute query
		Query query = conn.prepareQuery(QueryLanguage.SPARQL, queryString);

		TupleQuery tq = (TupleQuery) query;
		if (!useDefault)
			tq.setMaxExecutionTime(1);
		boolean exceptionThrown = false;
		long start = System.currentTimeMillis();
		try {
			TupleQueryResult result = tq.evaluate();
			result.close();
		} catch (QueryEvaluationException e) {
			long end = System.currentTimeMillis();
			assertTrue(e.toString().contains("OntopTupleQuery timed out. More than 1 seconds passed"));
			assertTrue(end - start >= 1000);
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}
	
	@Test
	public void testNoTimeout() throws Exception {
		String queryString = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
        
        // execute query
        Query query = conn.prepareQuery(QueryLanguage.SPARQL, queryString);

        TupleQuery tq = (TupleQuery) query;
        // Negative values disable the test
		tq.setMaxExecutionTime(-1);
        TupleQueryResult result = tq.evaluate();
		assertTrue(result.hasNext());
		result.close();
	}
}
