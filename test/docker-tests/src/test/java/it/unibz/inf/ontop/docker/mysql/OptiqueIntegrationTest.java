package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class OptiqueIntegrationTest  {
	private static final String owlfile = "/mysql/npd/npd-v2-ql_a.owl";
	private static final String mappingfile = "/mysql/npd/npd-v2-ql_a.ttl";
	private static final String propertyfile = "/mysql/npd/npd-v2-ql_a.properties";

	private static RepositoryConnection con;


	@BeforeClass
	public static void setUp() {
		Repository repo;
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(OptiqueIntegrationTest.class.getResource(owlfile).toString())
				.r2rmlMappingFile(OptiqueIntegrationTest.class.getResource(mappingfile).toString())
				.enableExistentialReasoning(true)
				.propertyFile(OptiqueIntegrationTest.class.getResource(propertyfile).toString())
				.enableTestMode()
				.build();

		repo = OntopRepository.defaultRepository(configuration);
		/*
		 * Repository must be always initialized first
		 */
		repo.init();

		/*
		 * Get the repository connection
		 */
		con = repo.getConnection();
	}

	@AfterClass
	public static void tearDown() {
		if (con != null && con.isOpen()) {
			con.close();
		}
	}

	private int count(String query) {
		int resultCount = 0;
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
		try (TupleQueryResult result = tupleQuery.evaluate()) {
			while (result.hasNext()) {
				result.next();
				resultCount++;
			}
		}
		return resultCount;
	}


	@Test
	public void test1() {
		String sparqlQuery = "SELECT ?x WHERE {?x a <http://sws.ifi.uio.no/vocab/npd-v2#Field>}" ;

		int obtainedResult = count(sparqlQuery);
		assertEquals(98, obtainedResult);
	}
}
