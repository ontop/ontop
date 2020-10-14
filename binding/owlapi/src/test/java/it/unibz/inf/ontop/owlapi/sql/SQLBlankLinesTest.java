package it.unibz.inf.ontop.owlapi.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.owlapi.AbstractOWLAPITest;
import org.junit.*;

import static org.junit.Assert.assertFalse;

/**
 * Tests that the generated SQL contains no blank lines
 * This leads to problems if the SQL is copied to the oracle command line client
 * @author Dag Hovland
 *
 */

public class SQLBlankLinesTest extends AbstractOWLAPITest  {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/sqlgenerator/create-h2.sql",
				"/sqlgenerator/blanklines.obda",
				"/sqlgenerator/blanklines.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}


	@Test
	public void testNoSQLBlankLines() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT * WHERE {?x a :Class1}";

		String sql = checkReturnedValuesAndReturnSql(query, "x", ImmutableList.of());
		assertFalse(sql.matches("(?ms)(.*)\\n\\n(.*)"));
	}

}
