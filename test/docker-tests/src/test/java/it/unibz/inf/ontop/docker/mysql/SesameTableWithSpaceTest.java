package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractRDF4JVirtualModeTest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SesameTableWithSpaceTest extends AbstractRDF4JVirtualModeTest {

	private static final String owlFile = "/mysql/northwind/1.4a.owl";
	private static final String ttlFile = "/mysql/northwind/mapping-northwind-1421066727259.ttl";
	private static final String propertyFile = "/mysql/northwind/northwind.properties";

	private static RepositoryConnection con;

	@BeforeClass
	public static void setUp() {
		Repository repo = createR2RMLReasoner(owlFile, ttlFile, propertyFile);
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
		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/> select * {?x a :OrderDetails}" ;

		int obtainedResult = executeQueryAndGetCount(con, sparqlQuery);
		assertEquals(2155, obtainedResult);
	}

}
