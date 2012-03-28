import junit.framework.TestCase;

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
			
			String owlfile = "/home/timi/workspace/obdalib-parent/quest-owlapi3/src/test/resources/test/stockexchange-unittest.owl";
			String obdafile = "/home/timi/workspace/obdalib-parent/quest-owlapi3/src/test/resources/test/stockexchange-postgres-unittest.obda";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile);
	
			repo.initialize();
			
			con = repo.getConnection();
			
			
			con.close();
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	/*
	///add data to repo
		File file = new File("/home/timi/onto2plus.owl");
		String baseURI = "http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#";
		//"http://www.semanticweb.org/ontologies/helloworld.owl";
		
		  if (file==null)
			  System.out.println("FiLE not FOUND!");
		  else
		  try {
			
			  System.out.println("Add from file.");
		     con.add(file, baseURI, RDFFormat.RDFXML);
		     
		    //  con.close();
		   
		}
		catch (Exception e) {
		   // handle exception
			e.printStackTrace();
		}
		
		/*
		ValueFactory f = repo.getValueFactory();

		// create some resources and literals to make statements out of
		org.openrdf.model.URI alice = f.createURI("http://example.org/people/alice");
		org.openrdf.model.URI bob = f.createURI("http://example.org/people/bob");
		org.openrdf.model.URI age = f.createURI("http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#age");
		org.openrdf.model.URI person = f.createURI("http://it.unibz.krdb/obda/ontologies/test/translation/onto2.owl#Person");
		Literal bobsAge = f.createLiteral(5);
		Literal alicesAge = f.createLiteral(14);

		
		   try {
		      // alice is a person
		      con.add(alice, RDF.TYPE, person);
		      // alice's name is "Alice"
		      con.add(alice, age, alicesAge);

		      // bob is a person
		      con.add(bob, RDF.TYPE, person);
		      // bob's name is "Bob"
		      con.add(bob, age, bobsAge);
		      
		      System.out.println("Closing");
		      con.close();
		   }
		   catch(Exception e)
		   {
			   e.printStackTrace();
		   }
		
		
		
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
		  // ValueFactory fac = repo.getValueFactory();
		   
		  */
	System.out.println("Done.");	
	}

}

