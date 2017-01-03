package it.unibz.inf.ontop.rdf4j.tests.general;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.util.List;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import junit.framework.TestCase;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;


public class SesameVirtualTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			
			String owlfile = "src/test/resources/example/exampleBooks.owl";
			String obdafile = "src/test/resources/example/exampleBooks.obda";
			QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
					.ontologyFile(owlfile)
					.nativeOntopMappingFile(obdafile)
					.build();

			repo = new OntopVirtualRepository("my_name", configuration);
	
			repo.initialize();
			
			con = repo.getConnection();
			
			///query repo
			 try {
			      String queryString = "select * where {?x ?z ?y }";
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
			      
			      queryString =  "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o FILTER(?s = <http://meraka/moss/exampleBooks.owl#book/23/>)}";
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

