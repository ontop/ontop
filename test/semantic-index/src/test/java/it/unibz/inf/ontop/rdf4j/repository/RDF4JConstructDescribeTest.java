package it.unibz.inf.ontop.rdf4j.repository;

import java.io.File;
import java.util.Properties;

import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.*;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * This unit test is to ensure the correctness of construct and describe
 * queries in ontop through the Sesame API. All tests should be green.
 * @author timi
 *
 */
public class RDF4JConstructDescribeTest {

	private static Repository REPOSITORY;
	private static final String DATA_FILE_PATH = "src/test/resources/describeConstruct.ttl";
	
	@BeforeClass
	public static void setUp() throws Exception {

		SimpleDataset dataset = new SimpleDataset();
		File dataFile = new File(DATA_FILE_PATH);
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		dataset.addDefaultGraph(valueFactory.createIRI(dataFile.toURI().toString()));

		Properties properties = new Properties();
		properties.setProperty(OntopReformulationSettings.INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE, "false");

		try(OntopSemanticIndexLoader loader = OntopSemanticIndexLoader.loadRDFGraph(dataset, properties)) {
			REPOSITORY = OntopRepository.defaultRepository(loader.getConfiguration());
			REPOSITORY.initialize();
		}
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		REPOSITORY.shutDown();
	}
	
	@Test
	public void testInsertData() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				Statement s = gresult.next();
				result++;
				//System.out.println(s.toString());
			}
			Assert.assertEquals(4, result);
		}
	}
	@Test
	public void testDescribeUri0() throws Exception {
		boolean result = false;
		String queryString = "DESCRIBE <http://www.semanticweb.org/ontologies/test#p1>";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result = false;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertFalse(result);
		}
	}
	
	@Test
	public void testDescribeUri1() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE <http://example.org/D>";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
			}
			// None because INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE is false
			Assert.assertEquals(0, result);
		}
	}
	
	@Test
	public void testDescribeUri2() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE <http://example.org/C>";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertEquals(2, result);
		}
	}
	
	@Test
	public void testDescribeVar0() throws Exception {
		boolean result = false;
		String queryString = "DESCRIBE ?x WHERE {<http://example.org/C> ?x ?y }";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result = false;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertFalse(result);
		}
	}
	
	@Test
	public void testDescribeVar1() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> <http://example.org/A>}";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
		}
		Assert.assertEquals(1, result);
	}

	@Test
	public void testDescribeVar2() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertEquals(2, result);
		}
	}

	@Test
	public void testDescribeVar3() {
		int result = 0;
		String queryString = "DESCRIBE ?x <http://example.org/B> WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
			}
			Assert.assertEquals(3, result);
		}
	}

	@Test
	public void testDescribeVar4() {
		int result = 0;
		String queryString = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y} LIMIT 1";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				gresult.next();
			}
			Assert.assertEquals(2, result);
		}
	}

	@Test
	public void testDescribeVar5() {
		int result = 0;
		String queryString = "DESCRIBE ?x ?y <http://example.org/B> WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				gresult.next();
			}
			// TODO: check the number of results
		}
	}

	@Test
	public void testDescribeVar6() {
		int result = 0;
		String queryString = "DESCRIBE ?x ?y <http://example.org/B> WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> ?y}";

		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement triple = gresult.next();
				//System.out.println(triple);
			}
			Assert.assertEquals(2, result);
		}
	}
	
	@Test
	public void testConstruct0() throws Exception {
		boolean result = false;
		String queryString = "CONSTRUCT {?s ?p <http://www.semanticweb.org/ontologies/test/p1>} WHERE {?s ?p <http://www.semanticweb.org/ontologies/test/p1>}";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result = false;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}

			Assert.assertFalse(result);
		}
	}
	
	@Test
	public void testConstruct1() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT { ?s ?p <http://example.org/D> } WHERE { ?s ?p <http://example.org/D>}";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertEquals(1, result);
		}
	}
	
	@Test
	public void testConstruct2() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
					queryString);

			GraphQueryResult gresult = graphQuery.evaluate();
			while (gresult.hasNext()) {
				result++;
				Statement s = gresult.next();
				//System.out.println(s.toString());
			}
			Assert.assertEquals(2, result);
		}
	}

	@Test
	public void testGetStatements0() throws Exception {
		boolean result = false;
		Resource subj = REPOSITORY.getValueFactory().createIRI("http://www.semanticweb.org/ontologies/test/p1");
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			RepositoryResult<Statement> results = con.getStatements(subj, null, null, false, (Resource) null);
			while (results.hasNext()) {
				result = true;
				results.next();
			}
			Assert.assertFalse(result);
		}
	}
	
	@Test
	public void testGetStatements1() throws Exception {
		int result = 0;
		Value obj = REPOSITORY.getValueFactory().createIRI("http://example.org/D");
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			RepositoryResult<Statement> results = con.getStatements(null, null, obj, false, (Resource) null);
			while (results.hasNext()) {
				result++;
				results.next();
			}
			Assert.assertEquals(1, result);
		}
	}
	
	@Test
	public void testGetStatements2() throws Exception {
		int result = 0;
		Resource subj = REPOSITORY.getValueFactory().createIRI("http://example.org/C");
		try (RepositoryConnection con = REPOSITORY.getConnection()) {
			RepositoryResult<Statement> results = con.getStatements(subj, null, null, false, (Resource) null);
			while (results.hasNext()) {
				result++;
				results.next();
			}

			Assert.assertEquals(2, result);
		}
	}

}
