package it.unibz.inf.ontop.docker.mysql;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class DatetimeStampSesameTest  {

	private static final String owlFile = "/mysql/northwind/northwind-dmo.owl";
	private static final String obdaFile = "/mysql/northwind/mapping-northwind-dmo.ttl";
	private static final String propertyFile = "/mysql/northwind/mapping-northwind-dmo.properties";

	Logger log = LoggerFactory.getLogger(this.getClass());
	RepositoryConnection con;
	Repository repository;

	public DatetimeStampSesameTest() {

		String owlFileName =  this.getClass().getResource(owlFile).toString();
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();

		OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.ontologyFile(owlFileName)
				.r2rmlMappingFile(obdaFileName)
				.propertyFile(propertyFileName)
				.enableExistentialReasoning(true)
				.enableTestMode()
				.build();

		repository = OntopRepository.defaultRepository(configuration);
		repository.initialize();
	}
	@Before
	public void setUp() {
		con = repository.getConnection();
	}

	@After
	public void tearDown() {
		if (con != null && con.isOpen()) {
			con.close();
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
		return resultCount;
	}

	@Test
	public void testRequiredDate() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>" +
				"select * {?x :RequiredDate ?y}";

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(830, obtainedResult);
	}

	@Test
	public void testShippedDate() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>" +
				"select * {?x :ShippedDate ?y}" ;

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(809, obtainedResult);
	}

	@Test
	public void testHireDate() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>\n" +
				"select * {?x :HireDate ?y}";

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(9, obtainedResult);
	}

	@Test
	public void testBirthDate() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>" +
				"select * {?x :BirthDate ?y}" ;

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results "+obtainedResult);
		assertEquals(9, obtainedResult);

	}

	@Ignore("The current SQL generator does not support xsd:base64Binary")
	@Test
	public void testPhoto() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>" +
				"select * {?x :Photo ?y}";

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results " + obtainedResult);
		assertEquals(9, obtainedResult);
	}

	@Ignore("The current SQL generator does not support xsd:base64Binary")
	@Test
	public void testBinary() {
		String sparqlQuery = "PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Categories/>" +
				"select * {?x :Picture ?y}" ;

		int obtainedResult = runQuery(sparqlQuery);
		log.debug("results "+obtainedResult);
		assertEquals(8, obtainedResult);
	}

}
