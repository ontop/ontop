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

import static org.junit.Assert.assertEquals;

/***
 * Tests that oracle identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifiers are treated as uppercase, and
 * that the case of quoted identifiers is not changed
 */
public class OracleIdentifierTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/oracle/identifiers/identifiers.owl";
	private static final String obdafile = "/oracle/identifiers/identifiers-oracle.obda";
	private static final String propertiesfile = "/oracle/oracle.properties";

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


	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Argentina>", val);
	}


	/**
	 * Test use of lowercase, unquoted table and column identifiers (also in target) with uppercase table identifiers
	 * @throws Exception
	 */

	@Test
	public void testUpperCaseTableUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country2} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-Argentina>", val);
	}
	
	/**
	 * Test use of lowercase, quoted alias in a view definition 
	 * @throws Exception
	 */

	@Test
	public void testLowerCaseColumnViewDefQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country4} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-Argentina>", val);
	}

	/**
	 * Test use of lowercase, unquoted alias in a view definition 
	 * @throws Exception
	 */

	@Test
	public void testLowerCaseColumnViewDefUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country5} ORDER BY ?x";
		String val =  runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country5-Argentina>", val);
	}



}

