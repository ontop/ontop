package org.semanticweb.ontop.sesame.tests.general;


import junit.framework.TestCase;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class DatetimeStampSesameTest extends TestCase {

	String owlfile = "src/test/resources/northwind/northwind-dmo.owl";
	String mappingfile = "src/test/resources/northwind/mapping-northwind-dmo.ttl";
	String queryfile = "";

	Logger log = LoggerFactory.getLogger(this.getClass());
	OWLOntology owlontology;
	Model mappings;
	RepositoryConnection con;
	QuestPreferences pref;

	public DatetimeStampSesameTest() {
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

		Properties p = new Properties();
		p.setProperty(QuestPreferences.ABOX_MODE,
				QuestConstants.VIRTUAL);
		p.setProperty(QuestPreferences.REWRITE, "true");
		p.setProperty(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		// set jdbc params in config
		p.setProperty(QuestPreferences.DB_NAME, "northwind");
		p.setProperty(QuestPreferences.JDBC_URL,
				"jdbc:mysql://10.7.20.39/northwind?sessionVariables=sql_mode='ANSI'");
		p.setProperty(QuestPreferences.DB_USER, "fish");
		p.setProperty(QuestPreferences.DB_PASSWORD, "fish");
		p.setProperty(QuestPreferences.JDBC_DRIVER, "com.mysql.jdbc.Driver");
		pref = new QuestPreferences();
	}

	public void setUp() {

		Repository repo;
		try {
			repo = new SesameVirtualRepo("virtualExample2", owlontology,
					mappings, pref);
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
	
	private int runQuery(String query){
		int resultCount = 0;
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					query);
			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				BindingSet setResult= result.next();
				for (String name: setResult.getBindingNames()){
					log.debug(name + " " + setResult.getValue(name));
				}

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
	public void testOrdersDate() {

		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/> select * {?x :RequiredDate ?y}" ;
		//read expected result
		int expectedResult = 830;
		
		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results "+obtainedResult);
		assertEquals(expectedResult, obtainedResult);

		//read next query
		sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/> select * {?x :ShippedDate ?y}" ;
		//read expected result
		expectedResult = 809;

		obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

	@Test
	public void testEmployeesDate() {

		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/> select * {?x :HireDate ?y}" ;
		//read expected result
		int expectedResult = 9;

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(expectedResult, obtainedResult);

		//read next query
		sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/> select * {?x :BirthDate ?y}" ;
		//read expected result
		expectedResult = 9;

		obtainedResult = runQuery(sparqlQuery);
		log.debug("results "+obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

	@Test
	public void testBinary() {

		//read next query
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/> select * {?x :Photo ?y}" ;
		//read expected result
		int expectedResult = 9;

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(expectedResult, obtainedResult);

		//read next query
		sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Categories/> select * {?x :Picture ?y}" ;
		//read expected result
		expectedResult = 8;

		obtainedResult = runQuery(sparqlQuery);
		log.debug("results "+obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

}
