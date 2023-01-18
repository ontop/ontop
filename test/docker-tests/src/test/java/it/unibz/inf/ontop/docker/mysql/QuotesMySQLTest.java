package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

/**
 * Github issue #315
 */
public class QuotesMySQLTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/mysql/quotes/stockExchangeQuotesMySQL.owl";
	private static final String obdafile = "/mysql/quotes/stockExchangeQuotesMySQL.obda";
	private static final String propertiesfile = "/mysql/quotes/stockExchangeQuotesMySQL.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before() {
		CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
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
	public void testQuotes() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2020/6/untitled-ontology-19#> SELECT * WHERE {?x :firstName ?y}";
		checkThereIsAtLeastOneResult(query);
	}
}
