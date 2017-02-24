package it.unibz.inf.ontop.failing;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

public class MsSQLASKTest extends AbstractVirtualModeTest {


	 private static final String owlfile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	 private static final String obdafile =
	 "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mssql.obda";
	private static final String propertyfile =
			"src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mssql.properties";

	public MsSQLASKTest() {
		super(owlfile, obdafile, propertyfile);
	}

	//	@Test test fails since mssql does not have limit and offset
	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  runQueryAndReturnBooleanX(query);
		assertTrue(val);
		
	}
}