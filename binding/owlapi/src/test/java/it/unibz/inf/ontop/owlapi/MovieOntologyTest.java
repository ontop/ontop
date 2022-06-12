package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

import static it.unibz.inf.ontop.utils.OWLAPITestingTools.executeFromFile;
import static junit.framework.TestCase.assertTrue;

public class MovieOntologyTest {

	private Connection conn;

	final String testCase = "movieontology";
	final String owlfile = "src/test/resources/test/treewitness/" + testCase + ".owl"; 
	final String obdafile = "src/test/resources/test/treewitness/" + testCase + ".obda";
	final String propertyFile = "src/test/resources/test/treewitness/" + testCase + ".properties";


	@Before
	public void setUp() throws Exception {
		conn = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa",  "");
		executeFromFile(conn, "src/test/resources/test/treewitness/imdb-schema-create-h2.sql");
	}

	
	@After
	public void tearDown() throws Exception {
		executeFromFile(conn, "src/test/resources/test/treewitness/imdb-schema-drop-h2.sql");
	}

	@Test
	public void testOntologyLoad() throws Exception {

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.propertyFile(propertyFile)
				.enableTestMode()
				.build();


		//noinspection EmptyTryBlock
		try (OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config)) {

		}

		assertTrue(true);
	}
}

	

