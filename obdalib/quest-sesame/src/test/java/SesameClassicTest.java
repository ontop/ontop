import java.io.File;
import java.io.IOException;
import java.net.URI;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.*;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import sesameWrapper.RepositoryConnection;
import sesameWrapper.SesameClassicInMemoryRepo;
import sesameWrapper.SesameClassicJDBCRepo;
import sesameWrapper.SesameClassicRepo;

import junit.framework.TestCase;


public class SesameClassicTest extends TestCase {

	sesameWrapper.RepositoryConnection con = null;
	Repository repo = null;
	String baseURI = "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#";
	
	public void setupInMemory() throws Exception 
	{
	
			//create a sesame in-memory H2 repository	
			String owlfile = "src/test/resources/onto2.owl";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			
			repo = new SesameClassicInMemoryRepo("my_name", owlfile);
	
			repo.initialize();
			
			con = (RepositoryConnection) repo.getConnection();
		
	}
	
	public void setupJDBC() throws Exception
	{
	
		//create a sesame JDBC repository	
			
			String owlfile = "src/test/resources/onto2.owl";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			
			repo = new SesameClassicJDBCRepo("my_name", owlfile);
	
			repo.initialize();
			
			con = (RepositoryConnection) repo.getConnection();
		
	}
	
	
	public void addFromFile() throws RDFParseException, RepositoryException, IOException
	{
	
	///add data to repo
		File file = new File("src/test/resources/onto2plus.owl");
				
		  if (file==null)
			  System.out.println("FiLE not FOUND!");
		  else
		  {
			  System.out.println("Add from file.");
		      con.add(file, baseURI, RDFFormat.RDFXML);
		  }	
		   
	}
	
	public void addFromURI() throws RepositoryException
	{
		
		ValueFactory f = repo.getValueFactory();

		// create some resources and literals to make statements out of
		org.openrdf.model.URI alice = f.createURI("http://example.org/people/alice");
		org.openrdf.model.URI bob = f.createURI("http://example.org/people/bob");
		org.openrdf.model.URI age = f.createURI(baseURI + "age");
		org.openrdf.model.URI person = f.createURI(baseURI+ "Person");
		Literal bobsAge = f.createLiteral(5);
		Literal alicesAge = f.createLiteral(14);

		
		      // alice is a person
		      con.add(alice, RDF.TYPE, person);
		      // alice's name is "Alice"
		      con.add(alice, age, alicesAge);

		      // bob is a person
		      con.add(bob, RDF.TYPE, person);
		      // bob's name is "Bob"
		      con.add(bob, age, bobsAge);
		 
	}
	
	
	public void tupleQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	
		
		///query repo
		      String queryString = "PREFIX : \n<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>\n " +
		      		"SELECT ?x ?y WHERE { ?x a :Person. ?x :age ?y } ";
		      TupleQuery tupleQuery = (con).prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		      TupleQueryResult result = tupleQuery.evaluate();
		     
		      System.out.println(result.getBindingNames());
		      
		    	  while (result.hasNext()) {
		    		   BindingSet bindingSet = result.next();
		    		   Value valueOfX =  bindingSet.getValue("x");
		    		   Literal valueOfY = (Literal) bindingSet.getValue("y");
		    		   System.out.println(valueOfX.stringValue()
		    				   +", "+valueOfY.floatValue());
		    	  }
		         result.close();
	}
	
	
	public void booleanQuery() throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{	
		
		///query repo
		      String queryString = "PREFIX : \n<http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#>\n " +
		      		"ASK { :3 a :Person} ";
		      BooleanQuery boolQuery = (con).prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
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
		//addFromURI();
		addFromFile();
		tupleQuery();
		//booleanQuery();
		close();
		}
		catch(Exception e)
		{e.printStackTrace();
		throw e;}
		
	}
	
//	public void test2() throws Exception
//	{
//		setupJDBC();
//		close();
//	}

}
