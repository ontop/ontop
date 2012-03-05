import java.io.File;
import java.net.URI;
import java.net.URL;

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
import org.openrdf.repository.*;
import org.openrdf.rio.RDFFormat;

import sesameWrapper.SesameClassicRepo;

import junit.framework.TestCase;


public class SesameClassicTest extends TestCase {

	
	public void test()
	{
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");
		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			String owlfile = "/home/timi/workspace/obdalib-parent/quest-owlapi3/src/test/resources/test/ontologies/translation/onto2.owl";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameClassicRepo("my_name", owlfile);
	
			repo.initialize();
			
			con = repo.getConnection();
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	
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
		
		*/
		
		///query repo
		 try {
		      String queryString = "SELECT x, y FROM {x} p {y}";
		      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SERQL, queryString);
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
			  
		      con.close();
		   }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		   ValueFactory fac = repo.getValueFactory();
		   
		  
	System.out.println("Done.");	
	}

}
