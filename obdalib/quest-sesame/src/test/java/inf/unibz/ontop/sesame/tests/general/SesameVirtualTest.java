package inf.unibz.ontop.sesame.tests.general;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import sesameWrapper.SesameVirtualRepo;


public class SesameVirtualTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			String owlfile = "C:/Project/Test Cases/Book.owl";
			String obdafile = "C:/Project/Test Cases/Book.obda";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			///query repo
			 try {
			      String queryString = "select * where {<http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819752067.owl#book/History%20of%20Art>  ?z ?y }";
			      		//"<http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819752067.owl#Book>}";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      try {
			    	  List<String> bindings = result.getBindingNames();
			    	  while (result.hasNext()) {
			    		   BindingSet bindingSet = result.next();
			    		   for (String b : bindings)
			    			   System.out.println(bindingSet.getBinding(b));
			    	  }
			      }
			      finally {
			         result.close();
			      }
			      
			      queryString =  "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
			      GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
			      GraphQueryResult gresult = graphQuery.evaluate();
			      while(gresult.hasNext())
			      {
			    	  Statement s = gresult.next();
			    	  System.out.println(s.toString());
			      }
			      
				  System.out.println("Closing...");
				 
			     con.close();
			    	  
			   }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	
	System.out.println("Done.");	
	}

}

