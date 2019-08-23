package it.unibz.inf.ontop.docker.failing.mssql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@Ignore("Mssql does not have limit and offset")
public class MsSQLASKTest extends AbstractVirtualModeTest {


	 private static final String owlfile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	 private static final String obdafile =
	 "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mssql.obda";
	private static final String propertyfile =
			"/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mssql.properties";

	public MsSQLASKTest() {
		super(owlfile, obdafile, propertyfile);
	}

	//	@Test test fails since mssql does not have limit and offset
	@Test
	public void testTrue() throws Exception {
		String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
		boolean val =  runQueryAndReturnBooleanX(query);
		assertTrue(val);
		
	}
}