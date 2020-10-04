package it.unibz.inf.ontop.reformulation.tests;


import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class OntopOntologyMaterializerTest extends TestCase {

	private Connection jdbcconn;

	private static final Logger LOGGER =  LoggerFactory.getLogger(OntopOntologyMaterializerTest.class);

	private static final String url = "jdbc:h2:mem:questjunitdb";
	private static final String username = "sa";
	private static final String password = "";


	@Override
	public void setUp() throws Exception {
		String createDDL = readSQLFile("src/test/resources/materializer/createMaterializeTest.sql");

		jdbcconn = DriverManager.getConnection(url, username, password);
		try (Statement st = jdbcconn.createStatement()) {
			st.executeUpdate(createDDL);
			jdbcconn.commit();
		}
	}

	@Override
	public void tearDown() throws Exception {
		String dropDDL = readSQLFile("src/test/resources/materializer/dropMaterializeTest.sql");

		try (Statement st = jdbcconn.createStatement()) {
			st.executeUpdate(dropDDL);
			jdbcconn.commit();
		}
		jdbcconn.close();
	}


	private String readSQLFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		StringBuilder bf = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			bf.append(line + "\n");
			line = reader.readLine();
		}
		return bf.toString();
	}


	public void testDataWithModel() throws Exception {
	
	    File f = new File("src/test/resources/materializer/MaterializeTest.obda");

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();
		
		OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration);

		try (MaterializedGraphResultSet resultSet = materializer.materialize()) {
			int factCount = 0;

//			// Davide> Debug
//			PrintWriter writer = new PrintWriter("src/test/resources/materializer/output_assertions.txt");

			LOGGER.debug("Assertions:");
			while (resultSet.hasNext()) {
				RDFFact assertion = resultSet.next();
				factCount++;
			}
			//2 classes * 3 data rows for T1
			//2 properties * 7 tables * 3 data rows each T2-T8 - 2 redundant
			//3 data rows for T9
			assertEquals(49, factCount);
		}
	}
	
	public void testDataWithModelAndOnto() throws Exception {

		// read model
		File f = new File("src/test/resources/materializer/MaterializeTest.obda");

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile("src/test/resources/materializer/MaterializeTest.owl")
				.nativeOntopMappingFile(f)
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.enableTestMode()
				.build();

		// read onto
		// OWLOntology ontology = configuration.loadProvidedInputOntology();
		// Ontology onto = OWLAPITranslatorOWL2QL.translate(
		//		ontology.getOWLOntologyManager().getImportsClosure(ontology));
		// System.out.println(onto.tbox().getSubClassAxioms());
		// System.out.println(onto.tbox().getSubObjectPropertyAxioms());
		// System.out.println(onto.tbox().getSubDataPropertyAxioms());

		OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration);
		try (MaterializedGraphResultSet resultSet = materializer.materialize()) {
			int factCount = 0;
			while (resultSet.hasNext()) {
				RDFFact assertion = resultSet.next();
				factCount++;
				LOGGER.debug(assertion + "\n");
			}
			//3 data rows x2 for subclass prop
			//8 tables * 3 data rows each x2 for subclass - 2 redundant
			//3 since no subprop for obj prop
			assertEquals(49, factCount);
		}
	}
}
