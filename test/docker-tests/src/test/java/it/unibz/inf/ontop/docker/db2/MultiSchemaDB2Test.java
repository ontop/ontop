package it.unibz.inf.ontop.docker.db2;


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
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URIs directly.
 *
 */
public class MultiSchemaDB2Test extends AbstractVirtualModeTest {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL


	private static final String owlfile = "/db2/schema/multischemadb2.owl";
	private static final String obdafile = "/db2/schema/multischemadb2.obda";
	private static final String propertiesfile = "/db2/db2-stock.properties";

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
	 * Test use of two aliases to same table
	 * @throws Exception
	 */
	@Test
	public void testOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Address}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testTableOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Broker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testAliasOneSchema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE {?x a :Worker}";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testSchemaWhere() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x ?r WHERE { ?x :isBroker ?r }";
		checkThereIsAtLeastOneResult(query);
	}

	@Test
	public void testMultischema() throws Exception {
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?x WHERE { ?x :hasFile ?r }";
		checkThereIsAtLeastOneResult(query);
	}
}
