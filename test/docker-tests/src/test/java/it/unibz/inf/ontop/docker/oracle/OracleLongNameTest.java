package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

/***
 * Oracle long name.
 */
public class OracleLongNameTest extends AbstractVirtualModeTest {

	
	private final static String owlFile = "/oracle/oraclesql/o.owl";
	private final static String obdaFile1 = "/oracle/oraclesql/o1.obda";
	private final static String propertyFile = "/oracle/oracle.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before() {
		CONNECTION = createReasoner(owlFile, obdaFile1, propertyFile);
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
	 * Short variable name
	 */
	@Test
	public void testShortVarName() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?x WHERE { ?x a :Country}";
		checkThereIsAtLeastOneResult(query);
	}

	/**
	 * Short variable name
	 */
	@Test
	public void testLongVarName() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
				"SELECT ?veryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongVarName WHERE { ?veryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongVarName a :Country}";
		checkThereIsAtLeastOneResult(query);
	}
}

