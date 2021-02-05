package it.unibz.inf.ontop.docker.postgres;

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
 * Test to check if the sql parser supports regex correctly when written with postgres syntax. 
 * Translated in a datalog function and provides the correct results
 */
public class RegexPostgresSQLTest extends AbstractVirtualModeTest {


	static final String owlfile = "/pgsql/regex/stockBolzanoAddress.owl";
	static final String obdafile = "/pgsql/regex/stockexchangeRegex.obda";
	static final String propertiesfile = "/pgsql/regex/stockexchangeRegex.properties";

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
