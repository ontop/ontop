package it.unibz.inf.ontop.ask;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

public class OracleASKTest extends AbstractVirtualModeTest {

	 static final String owlfile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	 static final String obdafile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-oracle.obda";
	static final String propertiesfile =
			"src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-oracle.properties";

	public OracleASKTest() {
		super(owlfile, obdafile, propertiesfile);
	}

	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  runQueryAndReturnBooleanX(query);
		assertTrue(val);
		
	}
}