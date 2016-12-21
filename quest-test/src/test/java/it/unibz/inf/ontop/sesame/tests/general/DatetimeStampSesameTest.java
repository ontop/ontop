package it.unibz.inf.ontop.sesame.tests.general;


import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.sesame.SesameVirtualRepo;
import junit.framework.TestCase;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import org.junit.Test;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Properties;

public class DatetimeStampSesameTest extends TestCase {

	String owlfile = "src/test/resources/northwind/northwind-dmo.owl";
	String mappingfile = "src/test/resources/northwind/mapping-northwind-dmo.ttl";

	Logger log = LoggerFactory.getLogger(this.getClass());
	RepositoryConnection con;
	Repository repository;

	public DatetimeStampSesameTest(){

		Properties connectionProperties = new Properties();
		// set jdbc params in config
		connectionProperties.setProperty(QuestCorePreferences.DB_NAME, "northwind");
		connectionProperties.setProperty(QuestCorePreferences.JDBC_URL,
				"jdbc:mysql://10.7.20.39/northwind?sessionVariables=sql_mode='ANSI'");
		connectionProperties.setProperty(QuestCorePreferences.DB_USER, "fish");
		connectionProperties.setProperty(QuestCorePreferences.DB_PASSWORD, "fish");
		connectionProperties.setProperty(QuestCorePreferences.JDBC_DRIVER, "com.mysql.jdbc.Driver");

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.ontologyFile(owlfile)
				.r2rmlMappingFile(mappingfile)
				.enableExistentialReasoning(true)
				.properties(connectionProperties)
				.build();
		try {
			repository = new SesameVirtualRepo("virtualExample2", configuration);
			repository.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(false);
		}
	}

	public void setUp() {
		try {
			con = repository.getConnection();

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
