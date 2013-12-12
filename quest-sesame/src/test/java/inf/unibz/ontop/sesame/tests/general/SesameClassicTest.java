package inf.unibz.ontop.sesame.tests.general;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameClassicInMemoryRepo;


public class SesameClassicTest extends TestCase {

	sesameWrapper.RepositoryConnection con = null;
	Repository repo = null;
	String baseURI = "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#";
	
	public void setupInMemory() throws Exception 
	{
		// create a sesame in-memory repository
		String owlfile = "src/test/resources/onto2.owl";

		repo = new SesameClassicInMemoryRepo("my_name", owlfile, false, "TreeWitness");
		repo.initialize();
		con = (RepositoryConnection) repo.getConnection();
	}
	
	
	public void addFromFile() throws RDFParseException, RepositoryException, IOException
	{
		// /add data to repo
		File file = new File("src/test/resources/onto2plus.owl");

		System.out.println("Add from file.");
		con.add(file, baseURI, RDFFormat.RDFXML);
	}
	
	
	public void addFromURI() throws RepositoryException
	{
		ValueFactory f = repo.getValueFactory();

		// create some resources and literals to make statements out of
		org.openrdf.model.URI alice = f.createURI(baseURI+"Alice");
		org.openrdf.model.URI bob = f.createURI(baseURI+"Bob");
		org.openrdf.model.URI age = f.createURI(baseURI + "age");
		org.openrdf.model.URI person = f.createURI(baseURI+ "Person");
		Literal bobsAge = f.createLiteral(5);
		Literal alicesAge = f.createLiteral(14);

		// alice is a person
		con.add(alice, RDF.TYPE, person);
		// alice's name is "Alice"
		//con.add(alice, age, alicesAge);

		// bob is a person
		con.add(bob, RDF.TYPE, person);
		// bob's name is "Bob"
		//con.add(bob, age, bobsAge);
	}
	
	
	public void tupleQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	

		// /query repo
		// con.setNamespace("onto",
		// "<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>");
		// System.out.println(con.getNamespaces().next().toString());
		String queryString = "PREFIX : \n<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>\n "
				+ "SELECT ?x ?y WHERE { ?x a :Person. ?x :age ?y } ";
		// String queryString =
		// "SELECT ?x ?y WHERE { ?x a onto:Person. ?x onto:age ?y } ";
		TupleQuery tupleQuery = (con).prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();

		System.out.println(result.getBindingNames());

		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			Value valueOfX = bindingSet.getValue("x");
			Literal valueOfY = (Literal) bindingSet.getValue("y");
			System.out.println(valueOfX.stringValue() + ", "+ valueOfY.floatValue());
		}
		result.close();
	}
	
	public void graphQuery() throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		String queryString = "CONSTRUCT {?x a ?y} WHERE { ?x a ?y} ";
		// String queryString =
		// "SELECT ?x ?y WHERE { ?x a onto:Person. ?x onto:age ?y } ";
		GraphQuery graphQuery = (con).prepareGraphQuery(QueryLanguage.SPARQL, queryString);
		GraphQueryResult result = graphQuery.evaluate();

		while (result.hasNext()) {
			Statement st = result.next();
			System.out.println(st.toString());
		}
		result.close();
	}
	
	public void booleanQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	
		
		// /query repo
		String queryString = "PREFIX : \n<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>\n "
				+ "ASK { :Lisa a :Person} ";
		BooleanQuery boolQuery = (con).prepareBooleanQuery(
				QueryLanguage.SPARQL, queryString);
		boolean result = boolQuery.evaluate();

		System.out.println(result);
	}
	
	public void close() throws RepositoryException
	{
		System.out.println("Closing...");
		con.close();
		System.out.println("Done.");
	}
	
	
	public void test1() throws Exception
	{
		try{
		setupInMemory();
		addFromURI();
		//addFromFile();
	//	tupleQuery();
		//booleanQuery();
		graphQuery();
		close();
		}
		catch(Exception e)
		{e.printStackTrace();
		throw e;}
		
	}
	

}
