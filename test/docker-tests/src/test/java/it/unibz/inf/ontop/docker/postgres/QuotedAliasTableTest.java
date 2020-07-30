package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Class to test that quotes from table names are removed correctly.
 * We use the npd database.
 *L
 */
public class QuotedAliasTableTest extends AbstractVirtualModeTest {
	static final String owlfile = "/pgsql/extended-npd-v2-ql_a_postgres.owl";
    static final String obdafile = "/pgsql/npd-v2.obda";
	static final String propertiesfile = "/pgsql/npd-v2.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertiesfile);
		CONNECTION = REASONER.getConnection();
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}


	/**
	 * Test OBDA table
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		String query = "PREFIX npdv: <http://sws.ifi.uio.no/vocab/npd-v2#> SELECT DISTINCT ?x WHERE {"
				+ "?x a npdv:CompanyReserve . "
				+	" }";
		
		// Now we are ready for querying obda
		// npd query 1
		countResults(52668, query);
	}
}
