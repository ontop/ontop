package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;


/**
 * Test class to solve the bug that generates unbound variables in the mapping.
 * Use the postgres IMDB database and a simple obda file with the problematic mapping.
 *
 * Solved modifying the method enforce equalities in DatalogNormalizer
 * to consider the case of nested equivalences in mapping
 */
public class UnboundVariableIMDbTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/pgsql/imdb/ontologyIMDB.owl";
	private static final String obdafile = "/pgsql/imdb/ontologyIMDBSimplify.obda";
	private static final String propertyfile = "/pgsql/imdb/movieontology.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before()  {
		CONNECTION = createReasoner(owlfile, obdafile, propertyfile);
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws Exception {
		CONNECTION.close();
	}

	@Test
	public void testIMDBSeries() throws Exception {
		String query = "PREFIX : <http://www.seriology.org/seriology#>\n" +
				"SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";
		countResults(10, query);
	}

	@Test
	public void testSubStr2WrongArgument() throws OWLException {
		countResults(2, "SELECT * WHERE {\n" +
				"  {\n" +
				"    SELECT DISTINCT ?b {\n" +
				"      {\n" +
				"        SELECT * {\n" +
				"          VALUES ?b { 2 }\n" +
				"        }\n" +
				"      }\n" +
				"      UNION {\n" +
				"        SELECT * {\n" +
				"          VALUES ?b { \"aa\" \"aa\" }\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"  BIND (SUBSTR(\"yyy\", ?b) AS ?v)\n" +
				"}");
	}

	@Test
	public void testSubStr3WrongArgument() throws OWLException {
		countResults(2, "SELECT * WHERE {\n" +
				"  {\n" +
				"    SELECT DISTINCT ?b {\n" +
				"      {\n" +
				"        SELECT * {\n" +
				"          VALUES ?b { 2 }\n" +
				"        }\n" +
				"      }\n" +
				"      UNION {\n" +
				"        SELECT * {\n" +
				"          VALUES ?b { \"aa\" \"aa\" }\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"  BIND (SUBSTR(\"yyy\", ?b, ?b) AS ?v)\n" +
				"}");
	}
}
