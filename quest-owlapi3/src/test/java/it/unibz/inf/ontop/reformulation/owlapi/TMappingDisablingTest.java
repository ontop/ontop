package it.unibz.inf.ontop.reformulation.owlapi;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final String owlfile = "src/test/resources/test/tmapping/exampleTMappingNoEquivalence.owl";
	private final String obdafile = "src/test/resources/test/tmapping/exampleTMapping.obda";
	private final String tMappingsConfFile = "src/test/resources/test/tmapping/tMappingsConf.conf";

	@Before
	public void setUp() throws Exception {
		createTables();
	}

	@After
	public void tearDown() throws Exception{
		dropTables();
		connection.close();
	}
	
	private void createTables() throws SQLException, IOException {
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb;";
		String username = "sa";
		String password = "";
		
		connection = DriverManager.getConnection(url, username, password);
		Statement st = connection.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/tmapping/create-tables.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		connection.commit();
		in.close();
	}
	
	private void dropTables() throws SQLException, IOException {

		Statement st = connection.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/tmapping/drop-tables.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		connection.commit();
		in.close();
	}
		
	public void testDisableTMappings() throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		QuestOWLConfiguration configuration = QuestOWLConfiguration.builder()
				.nativeOntopMappingFile(obdafile)
				.build();
		
		QuestOWL reasoner = factory.createReasoner(ontology, configuration);
		
		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = null;
		try {
			conn = reasoner.getConnection();
		} catch (OBDAException e1) {
			e1.printStackTrace();
		}
		
		
		String sparqlQuery = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Boy }";
		
		String sparqlQuery1 = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Man }";
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			assertTrue(!rs.nextRow());
			rs.close();
			rs = st.executeTuple(sparqlQuery1);
			assertTrue(rs.nextRow());
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		reasoner.dispose();
	}
	
	public void testDisableSelectedTMappings() throws DuplicateMappingException, InvalidMappingException, InvalidDataSourceException, IOException {
		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		} catch (OWLOntologyCreationException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.put(QuestPreferences.TMAPPING_EXCLUSION, TMappingExclusionConfig.parseFile(tMappingsConfFile));
		//p.put(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		QuestOWLConfiguration configuration = QuestOWLConfiguration.builder()
				.nativeOntopMappingFile(obdafile)
				.properties(p)
				.build();
		QuestOWL reasoner = factory.createReasoner(ontology, configuration);
		
		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = null;
		try {
			conn = reasoner.getConnection();
		} catch (OBDAException e1) {
			e1.printStackTrace();
		}
		
		
		String sparqlQuery = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Boy }";
		
		String sparqlQuery1 = 
				"PREFIX  : <http://www.semanticweb.org/sarah/ontologies/2014/4/untitled-ontology-73#> "
				+ "SELECT ?y WHERE { ?y a :Man }";
		QuestOWLStatement st = null;
		try {
			st = conn.createStatement();
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			assertTrue(!rs.nextRow());
			rs.close();
			rs = st.executeTuple(sparqlQuery1);
			assertTrue(rs.nextRow());
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		reasoner.dispose();
	}
}
