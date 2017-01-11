package it.unibz.inf.ontop.rdf4j.tests.general;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import junit.framework.TestCase;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class SesameTableWithSpaceTest extends TestCase {

	String owlfile = "src/test/resources/northwind/1.4a.owl";
	String mappingfile = "src/test/resources/northwind/mapping-northwind-1421066727259.ttl";
	String queryfile = "";

	OWLOntology owlontology;
	Model mappings;
	RepositoryConnection con;
	Properties connectionProperties;

	public SesameTableWithSpaceTest() {
		// create owlontology from file
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(
				new File(owlfile).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		try {
			owlontology = man
					.loadOntologyFromOntologyDocument(new File(owlfile));

			// create RDF Graph from ttl file
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			InputStream in = new FileInputStream(mappingfile);
			URL documentUrl = new URL("file://" + mappingfile);
			mappings = new LinkedHashModel();
			StatementCollector collector = new StatementCollector(mappings);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());

		} catch (RDFParseException | RDFHandlerException | IOException
				| OWLOntologyCreationException e) {

			e.printStackTrace();
			assertFalse(false);
		}

		connectionProperties = new Properties();
		// set jdbc params in config
		connectionProperties.put(QuestCoreSettings.DB_NAME, "northwindSpaced");
		connectionProperties.put(QuestCoreSettings.JDBC_URL,
				"jdbc:mysql://10.7.20.39/northwindSpaced?sessionVariables=sql_mode='ANSI'");
		connectionProperties.put(QuestCoreSettings.DB_USER, "fish");
		connectionProperties.put(QuestCoreSettings.DB_PASSWORD, "fish");
		connectionProperties.put(QuestCoreSettings.JDBC_DRIVER,"com.mysql.jdbc.Driver");
	}

	public void setUp() {

		Repository repo;
		try {
			QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
					.ontologyFile(owlfile)
					.r2rmlMappingFile(mappingfile)
					.enableExistentialReasoning(true)
					.properties(connectionProperties)
					.build();

			repo = new OntopVirtualRepository("virtualExample2", configuration);
			/*
			 * Repository must be always initialized first
			 */
			repo.initialize();

			/*
			 * Get the repository connection
			 */
			con = repo.getConnection();

		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(false);
		}

	}

	public void tearDown() {
		try {
			if (con != null && con.isOpen()) {
				con.close();
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	private int count(String query){
		int resultCount = 0;
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					query);
			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				result.next();
				resultCount++;
			}
			
			result.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(false);
		}
		return resultCount++;
	}

	@Test
	public void test1() {

		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/> select * {?x a :OrderDetails}" ;
		//read expected result
//		int expectedResult = 14366 ;
		int expectedResult = 2155;
		
		int obtainedResult = count(sparqlQuery);
		System.out.println(obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

}
