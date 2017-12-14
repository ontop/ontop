package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualSesameTest;
import org.junit.Test;


public class OptiqueIntegrationTest extends AbstractVirtualSesameTest {
	private static final String owlfile = "/mysql/npd/npd-v2-ql_a.owl";
	private static final String mappingfile = "/mysql/npd/npd-v2-ql_a.ttl";
	private static final String propertyfile = "/mysql/npd/npd-v2-ql_a.properties";

	public OptiqueIntegrationTest() {
		super(owlfile, mappingfile, propertyfile);
	}


	@Test
	public void test1() {

		//read next query
		String sparqlQuery = "SELECT ?x WHERE {?x a <http://sws.ifi.uio.no/vocab/npd-v2#Field>}" ; 
		//read expected result
		//int expectedResult = 14366 ;
		int expectedResult = 98;
		
		int obtainedResult = count(sparqlQuery);
		System.out.println(obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

}
