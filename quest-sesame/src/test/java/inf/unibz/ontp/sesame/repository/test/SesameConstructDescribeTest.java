package inf.unibz.ontp.sesame.repository.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameClassicInMemoryRepo;
import sesameWrapper.SesameRepositoryConfig;
import sesameWrapper.SesameRepositoryFactory;

/**
 * This unit test is to ensure the correctness of construct and describe
 * queries in ontop through the Sesame API. All tests should be green.
 * @author timi
 *
 */
public class SesameConstructDescribeTest extends TestCase{

	RepositoryConnection con = null;
	Repository repo = null;
	ValueFactory fac = null;
	String fileName = "src/test/resources/describeConstruct.ttl";
	String owlFile = "src/test/resources/describeConstruct.owl";
	
	@Override
	public void setUp() throws Exception {
		
		try {
			System.out.println("In-memory quest repo.");			

			SesameRepositoryConfig config;
			SesameRepositoryFactory fact = new SesameRepositoryFactory();
			config = (SesameRepositoryConfig) fact.getConfig();
			config.setQuestType("quest-inmemory");
			config.setName("my_repo");
			
			repo = new SesameClassicInMemoryRepo("constructDescribe", owlFile, false, "TreeWitness");
			repo.initialize();
			con = (RepositoryConnection) repo.getConnection();
			fac = con.getValueFactory();
			File data = new File(fileName);
			System.out.println(data.getAbsolutePath());
			if (data.canRead())
				con.add(data, "http://www.semanticweb.org/ontologies/test", RDFFormat.TURTLE, (Resource)null);
			else
				throw new Exception("The specified file cannot be found or has restricted access.");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public void tearDown() throws Exception {
		con.close();
		repo.shutDown();
	}
	
	@Test
	public void testInsertData() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
				queryString);

		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			Statement s = gresult.next();
			result++;
			System.out.println(s.toString());
		}
		Assert.assertEquals(4, result);
	}
	@Test
	public void testDescribeUri0() throws Exception {
		boolean result = false;
		String queryString = "DESCRIBE <http://www.semanticweb.org/ontologies/test#p1>";
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
	
	@Test
	public void testDescribeUri1() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE <http://example.org/D>";
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
	
	@Test
	public void testDescribeUri2() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE <http://example.org/C>";
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
	
	@Test
	public void testDescribeVar0() throws Exception {
		boolean result = false;
		String queryString = "DESCRIBE ?x WHERE {<http://example.org/C> ?x ?y }";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
				queryString);

		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			result = false;
			Statement s = gresult.next();
			System.out.println(s.toString());
		}
		Assert.assertFalse(result);
	}
	
	@Test
	public void testDescribeVar1() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> <http://example.org/A>}";
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
	
	@Test
	public void testDescribeVar2() throws Exception {
		int result = 0;
		String queryString = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";
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
	
	@Test
	public void testConstruct0() throws Exception {
		boolean result = false;
		String queryString = "CONSTRUCT {?s ?p <http://www.semanticweb.org/ontologies/test/p1>} WHERE {?s ?p <http://www.semanticweb.org/ontologies/test/p1>}";
		GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL,
				queryString);

		GraphQueryResult gresult = graphQuery.evaluate();
		while (gresult.hasNext()) {
			result = false;
			Statement s = gresult.next();
			System.out.println(s.toString());
		}
		Assert.assertFalse(result);
	}
	
	@Test
	public void testConstruct1() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT { ?s ?p <http://example.org/D> } WHERE { ?s ?p <http://example.org/D>}";
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
	
	@Test
	public void testConstruct2() throws Exception {
		int result = 0;
		String queryString = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
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
	
	@Test
	public void testGetStatements0() throws Exception {
		boolean result = false;
		Resource subj = fac.createURI("http://www.semanticweb.org/ontologies/test/p1");
		RepositoryResult<Statement> results = con.getStatements(subj, null, null, false, (Resource)null);
		while (results.hasNext())
		{
			result = true;
			results.next();
		}
		Assert.assertFalse(result);
	}
	
	@Test
	public void testGetStatements1() throws Exception {
		int result = 0;
		Value obj = fac.createURI("http://example.org/D");
		RepositoryResult<Statement> results = con.getStatements(null, null, obj, false, (Resource)null);
		while (results.hasNext())
		{
			result++;
			results.next();
		}
		Assert.assertEquals(1, result);
	}
	
	@Test
	public void testGetStatements2() throws Exception {
		int result = 0;
		Resource subj = fac.createURI("http://example.org/C");
		RepositoryResult<Statement> results = con.getStatements(subj, null, null, false, (Resource)null);
		while (results.hasNext())
		{
			result++;
			results.next();
		}
		
		Assert.assertEquals(2, result);
	}

}
