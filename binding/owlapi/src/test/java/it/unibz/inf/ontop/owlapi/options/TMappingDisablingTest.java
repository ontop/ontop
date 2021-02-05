package it.unibz.inf.ontop.owlapi.options;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import it.unibz.inf.ontop.utils.OWLAPITestingTools;
import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import junit.framework.TestCase;

/**
 * 
 * @author Davide
 * 
 * It tests the options for disabling the t-mappings. Currently it does not work with 
 * concepts that are equivalent (e.g., the tests will fail when using the ontology
 * src/test/resources/test/tmapping/exampleTMapping.owl
 *
 */
public class TMappingDisablingTest extends TestCase {
	
	private Connection connection;

	private final String owlfile = "src/test/resources/test/tmapping/exampleTMappingNoEquivalence.owl";
	private final String obdafile = "src/test/resources/test/tmapping/exampleTMapping.obda";
	private final String propertyFile = "src/test/resources/test/tmapping/exampleTMapping.properties";
	private final String tMappingsConfFile = "src/test/resources/test/tmapping/tMappingsConf.conf";

	@Before
	public void setUp() throws Exception {
		createTables();
	}

	@After
	public void tearDown() throws Exception{
		OWLAPITestingTools.executeFromFile(connection, "src/test/resources/test/tmapping/drop-tables.sql");
		connection.close();
	}
	
	private void createTables() throws SQLException, IOException {
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb;";
		String username = "sa";
		String password = "";
		
		connection = DriverManager.getConnection(url, username, password);
		OWLAPITestingTools.executeFromFile(connection, "src/test/resources/test/tmapping/create-tables.sql");
	}
	
	public void testDisableTMappings() throws Exception {
		
		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.propertyFile(propertyFile)
				.enableTestMode()
				.build();
		OntopOWLReasoner reasoner = factory.createReasoner(configuration);
		OWLConnection conn = reasoner.getConnection();
		
		String sparqlQuery = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Boy }";
		
		String sparqlQuery1 = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Man }";
		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
			assertTrue(!rs.hasNext());
			rs.close();
			rs = st.executeSelectQuery(sparqlQuery1);
			assertTrue(rs.hasNext());
			rs.close();
		}
		finally {
			reasoner.dispose();
		}
	}
	
	public void testDisableSelectedTMappings() throws Exception {

		OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.propertyFile(propertyFile)
				.tMappingExclusionConfig(TMappingExclusionConfig.parseFile(tMappingsConfFile))
				.enableTestMode()
				.build();
		OntopOWLReasoner reasoner = factory.createReasoner(configuration);
		OWLConnection conn = reasoner.getConnection();

		String sparqlQuery = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Boy }";
		
		String sparqlQuery1 = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Man }";

		try (OWLStatement st = conn.createStatement()) {
			TupleOWLResultSet  rs = st.executeSelectQuery(sparqlQuery);
			assertTrue(!rs.hasNext());
			rs.close();
			rs = st.executeSelectQuery(sparqlQuery1);
			assertTrue(rs.hasNext());
			rs.close();
		}
		finally {
			reasoner.dispose();
		}
	}
}
