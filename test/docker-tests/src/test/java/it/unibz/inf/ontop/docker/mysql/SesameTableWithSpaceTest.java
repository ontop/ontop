package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import junit.framework.TestCase;
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
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class SesameTableWithSpaceTest extends TestCase {

	private static final String owlFile = "/mysql/northwind/1.4a.owl";
	private static final String ttlFile = "/mysql/northwind/mapping-northwind-1421066727259.ttl";
	private static final String propertyFile = "/mysql/northwind/northwind.properties";

	final String owlFileName =  this.getClass().getResource(owlFile).toString();
	final String ttlFileName =  this.getClass().getResource(ttlFile).toString();
	final String propertyFileName =  this.getClass().getResource(propertyFile).toString();

	OWLOntology owlontology;
	Model mappings;
	RepositoryConnection con;

	public SesameTableWithSpaceTest() throws Exception {
		// create owlontology from file
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		URL owlFileName =  this.getClass().getResource(owlFile);
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(owlFileName.getPath()).getParentFile(), false);
		man.addIRIMapper(iriMapper);

		owlontology = man.loadOntologyFromOntologyDocument(new File(owlFileName.getPath()));

		// create RDF Graph from ttl file
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		InputStream in =  this.getClass().getResourceAsStream(ttlFile);
		URL documentUrl = new URL("file://" + ttlFile);
		mappings = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(mappings);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());
	}

	public void setUp() {
		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.r2rmlMappingFile(ttlFileName)
				.enableExistentialReasoning(true)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();

		Repository repo = OntopRepository.defaultRepository(configuration);
		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		con = repo.getConnection();
	}

	public void tearDown() {
		if (con != null && con.isOpen()) {
			con.close();
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

		int obtainedResult = count(sparqlQuery);
		System.out.println(obtainedResult);
//		int expectedResult = 14366 ;
		assertEquals(2155, obtainedResult);
	}

}
