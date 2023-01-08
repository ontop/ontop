package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractRDF4JVirtualModeTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class OptiqueIntegrationTest extends AbstractRDF4JVirtualModeTest {
	private static final String owlfile = "/mysql/npd/npd-v2-ql_a.owl";
	private static final String mappingfile = "/mysql/npd/npd-v2-ql_a.ttl";
	private static final String propertyfile = "/mysql/npd/npd-v2-ql_a.properties";

	private static RepositoryConnection con;

	@BeforeClass
	public static void setUp() {
		Repository repo = createR2RMLReasoner(owlfile, mappingfile, propertyfile);
		con = repo.getConnection();
	}

	@AfterClass
	public static void tearDown() {
		if (con != null && con.isOpen()) {
			con.close();
		}
	}


	@Test
	public void test1() {
		String sparqlQuery = "SELECT ?x WHERE {?x a <http://sws.ifi.uio.no/vocab/npd-v2#Field>}" ;

		int obtainedResult = extecuteQueryAndGetCount(con, sparqlQuery);
		assertEquals(98, obtainedResult);
	}
}
