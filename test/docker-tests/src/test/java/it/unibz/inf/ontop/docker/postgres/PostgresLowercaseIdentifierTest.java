package it.unibz.inf.ontop.docker.postgres;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

/***
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class PostgresLowercaseIdentifierTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/pgsql/identifiers/identifiers.owl";
	private static final String obdafile = "/pgsql/identifiers/identifiers-lowercase-postgres.obda";
	private static final String propertyfile = "/pgsql/identifiers/identifiers-lowercase-postgres.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before() {
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

	/**
	 * Test use of unquoted uppercase to access lowercase column and table names in postgres 
	 * (Postgres converts unquoted identifer letters to lowercase)
	 * @throws Exception
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		checkThereIsAtLeastOneResult(query);
	}
}
