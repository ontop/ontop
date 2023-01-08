package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

/**
 * Test to check if the sql parser supports regex correctly when written with postgres syntax. 
 * Translated in a datalog function and provides the correct results
 */
public class RegexPostgresSQLTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/pgsql/regex/stockBolzanoAddress.owl";
	private static final String obdafile = "/pgsql/regex/stockexchangeRegex.obda";
	private static final String propertiesfile = "/pgsql/regex/stockexchangeRegex.properties";

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

	/**
	 * Test use of regex in Postgres
	 * select id, street, number, city, state, country from address where city ~* 'b.+z'
	 * @throws Exception
	 */
	@Test
	public void testPostgresRegex() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :BolzanoAddress}";
        countResults(2, query);
	}
	
	/**
	 * Test use of regex in Postgres with not
	 * select "id", "name", "lastname", "dateofbirth", "ssn" from "broker" where  "name" !~* 'J.+a'
	 * @throws Exception
	 */
	@Test
	public void testPostgresRegexNot() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> SELECT ?x WHERE {?x a :PhysicalPerson}";
		countResults(3, query);
	}
}
