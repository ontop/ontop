package it.unibz.inf.ontop.rdf4j.tests.general;

import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.injection.QuestCoreSettings;

import java.util.Properties;

import org.junit.Test;


public class OptiqueIntegrationTest extends AbstractVirtualSesameTest {
	private static final String owlfile = "src/test/resources/example/npd-v2-ql_a.owl";
	private static final String mappingfile = "src/test/resources/example/npd-v2-ql_a.ttl";

	public OptiqueIntegrationTest() {
		super(owlfile, mappingfile, buildProperties());
	}

	private static Properties buildProperties() {
		Properties p = new Properties();
		p.put(QuestCoreSettings.REWRITE, true);
		// set jdbc params in config
		p.setProperty(QuestCoreSettings.JDBC_NAME, "npd");
		p.setProperty(QuestCoreSettings.JDBC_URL,
				"jdbc:mysql://10.7.20.39/npd?sessionVariables=sql_mode='ANSI'");
		p.setProperty(QuestCoreSettings.JDBC_USER, "fish");
		p.setProperty(QuestCoreSettings.JDBC_PASSWORD, "fish");
		p.setProperty(QuestCoreSettings.JDBC_DRIVER, "com.mysql.jdbc.Driver");

		return p;
	}


	@Test
	public void test1() {

		//read next query
		String sparqlQuery = "SELECT ?x WHERE {?x a <http://sws.ifi.uio.no/vocab/npd-v2#Field>}" ; 
		//read expected result
		//int expectedResult = 14366 ;
		int expectedResult = 101;
		
		int obtainedResult = count(sparqlQuery);
		System.out.println(obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

}
