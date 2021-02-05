package it.unibz.inf.ontop.docker.mysql;


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
 * Github issue #315
 */
public class QuotesMySQLTest extends AbstractVirtualModeTest {

	static final String owlfile = "/mysql/quotes/stockExchangeQuotesMySQL.owl";
	static final String obdafile = "/mysql/quotes/stockExchangeQuotesMySQL.obda";
	static final String propertiesfile = "/mysql/quotes/stockExchangeQuotesMySQL.properties";

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

	@Test
	public void testQuotes() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2020/6/untitled-ontology-19#> SELECT * WHERE {?x :firstName ?y}";
//		runQuery(query);
		checkThereIsAtLeastOneResult(query);
	}

	
	
	

		
}
