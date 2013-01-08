package inf.unibz.ontop.sesame.tests.general;
import junit.framework.TestCase;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
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
			
			String owlfile = "../quest-owlapi3/src/test/resources/test/bsbm.owl";
			String obdafile = "../quest-owlapi3/src/test/resources/test/bsbm.obda";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			///query repo
			 try {
			      String queryString = "SELECT ?x ?y WHERE {?x ?p ?y}";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			      TupleQueryResult result = tupleQuery.evaluate();
			      try {
			    	  while (result.hasNext()) {
			    		   BindingSet bindingSet = result.next();
			    		   Value valueOfX = bindingSet.getValue("x");
			    		   Value valueOfY = bindingSet.getValue("y");
			    		   System.out.println("x="+valueOfX.toString()+", y="+valueOfY.toString());
			    	  }
			      }
			      finally {
			         result.close();
			      }
				  System.out.println("Closing..,");
			      con.close();
			   }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			
			con.close();
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	
	System.out.println("Done.");	
	}

}

