package org.semanticweb.ontop.sesame.tests.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
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

public class OptiqueIntegrationTest extends TestCase {

	String owlfile = "src/test/resources/example/npd-v2-ql_a.owl";
	String mappingfile = "src/test/resources/example/npd-v2-ql_a.ttl";
	String queryfile = "";

	OWLOntology owlontology;
	Model mappings;
	RepositoryConnection con;
	QuestPreferences pref;

	public OptiqueIntegrationTest() {
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

		pref = new QuestPreferences();
		pref.setCurrentValueOf(QuestPreferences.ABOX_MODE,
				QuestConstants.VIRTUAL);
		pref.setCurrentValueOf(QuestPreferences.REWRITE, "true");
		pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
		// set jdbc params in config
		pref.setCurrentValueOf(QuestPreferences.DBNAME, "npd");
		pref.setCurrentValueOf(QuestPreferences.JDBC_URL,
				"jdbc:mysql://10.7.20.39/npd?sessionVariables=sql_mode='ANSI'");
		pref.setCurrentValueOf(QuestPreferences.DBUSER, "fish");
		pref.setCurrentValueOf(QuestPreferences.DBPASSWORD, "fish");
		pref.setCurrentValueOf(QuestPreferences.JDBC_DRIVER,"com.mysql.jdbc.Driver");
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
		String sparqlQuery = "SELECT ?x WHERE {?x a <http://sws.ifi.uio.no/vocab/npd-v2#Field>}" ; 
		//read expected result
		//int expectedResult = 14366 ;
		int expectedResult = 101;
		
		int obtainedResult = runQuery(sparqlQuery);
		System.out.println(obtainedResult);
		assertEquals(expectedResult, obtainedResult);

	}

}
