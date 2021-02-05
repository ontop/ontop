package it.unibz.inf.ontop.owlapi.sql;

import it.unibz.inf.ontop.owlapi.AbstractOWLAPITest;
import org.junit.*;

import static org.junit.Assert.assertTrue;

/**
 * Test to check the correct parsing of IN and INTERSECT in SQLTableNameExtractor
 * to retrieve subselect queries
 *
 */

public class SQLInAndIntersectionTest extends AbstractOWLAPITest  {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/sqlin/create-h2.sql",
				"/sqlin/routes.obda",
				"/sqlin/routes.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testComplexTableNamesRetrievalIn() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT * WHERE {?x a :Route}";

		String sql = getSqlTranslation(query);
		assertTrue(sql.matches("(?ms)(.*)IN(.*)"));
	}

	@Test
	public void testComplexTableNamesRetrievalIntersect() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT * WHERE {?x a :Vertex}";

		String sql = getSqlTranslation(query);
		assertTrue(sql.matches("(?ms)(.*)INTERSECT(.*)"));
	}
}
