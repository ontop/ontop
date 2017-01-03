package it.unibz.inf.ontop.rdf4j.tests.general;

import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;

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
		p.setProperty(QuestCorePreferences.ABOX_MODE,
				QuestConstants.VIRTUAL);
		p.setProperty(QuestCorePreferences.REWRITE, "true");
		p.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		// set jdbc params in config
		p.setProperty(QuestCorePreferences.DB_NAME, "npd");
		p.setProperty(QuestCorePreferences.JDBC_URL,
				"jdbc:mysql://10.7.20.39/npd?sessionVariables=sql_mode='ANSI'");
		p.setProperty(QuestCorePreferences.DB_USER, "fish");
		p.setProperty(QuestCorePreferences.DB_PASSWORD, "fish");
		p.setProperty(QuestCorePreferences.JDBC_DRIVER, "com.mysql.jdbc.Driver");

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
