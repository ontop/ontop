package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/***
 * Oracle long name.
 */
public class OracleLongNameTest extends AbstractVirtualModeTest {

	
	final static String owlFile = "/oracle/oraclesql/o.owl";
	final static String obdaFile1 = "/oracle/oraclesql/o1.obda";
	final static String propertyFile = "/oracle/oracle.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlFile, obdaFile1, propertyFile);
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

