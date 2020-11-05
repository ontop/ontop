package it.unibz.inf.ontop.owlapi;

import org.junit.*;

import static junit.framework.TestCase.assertTrue;

public class H2ASKTest extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/stockexchange/stockexchange-create-h2.sql",
				"/stockexchange/stockexchange-h2.obda",
				"/stockexchange/stockexchange.owl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  executeAskQuery(query);
		assertTrue(val);
	}

}