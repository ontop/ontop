package it.unibz.inf.ontop.ask;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

public class MySQLASKTest extends AbstractVirtualModeTest {



	 static final String owlfile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	 static final String obdafile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";
	static final String propertyfile =
			"src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.properties";

	public MySQLASKTest() {
		super(owlfile, obdafile, propertyfile);
	}

	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  runQueryAndReturnBooleanX(query);
		assertTrue(val);
		
	}
}