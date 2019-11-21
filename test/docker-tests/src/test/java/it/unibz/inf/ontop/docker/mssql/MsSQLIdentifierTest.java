package it.unibz.inf.ontop.docker.mssql;


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
 * Tests that mssql identifiers for tables and columns are treated
 * correctly. Especially, that the unquoted identifers are treated as lowercase (for columns)
 */
public class MsSQLIdentifierTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/mssql/identifiers.owl";
	private static final String obdafile = "/mssql/identifiers-mssql.obda";
	private static final String propertyfile = "/mssql/identifiers-mssql.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertyfile);
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
	 * Test use of lowercase column identifiers (also in target)
	 * @throws Exception
	 */
	@Test
	public void testLowercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-991>", val);
	}

	@Test
	public void testUppercaseUnquoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country2} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country2-211>", val);	
	}

	@Test
	public void testLowercaseQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country3} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country3-112>", val);
	}

	@Test
	public void testAliasQuoted() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Country4} ORDER BY ?x";
		String val = runQueryAndReturnStringOfIndividualX(query);
		assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country4-111>", val);
	}
}
