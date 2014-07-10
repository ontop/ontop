package inf.unibz.ontop.sesame.tests.general;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import sesameWrapper.SesameVirtualRepo;

//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;

public class SesameVirtualTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			//String owlfile = "/home/constant/books.owl";
			String obdafile = "/home/constant/npd.obda";
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			//repo = new SesameVirtualRepo("my_name", obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			String prefixes = "ex: <http://meraka/moss/exampleBooks.owl#>";
			
			///query repo
			 try {
				 String spatialQuery = "select distinct ?x1 ?g1 where {" +
				 						"?x1 ex:hasSerialization ?g1 . " +
				 						" ?x2 ex:hasSerialization ?g2 . " +
				 						"FILTER(SpatialOverlap(?g1,?g2))" +
				 					//	"FILTER(!sameTerm(?g1,?g2))" +
				 						"} limit 10";
				 String spatialConstantQuery = "select distinct ?x1 ?g1 where {" +
	 						"?x1 <http://meraka/moss/exampleBooks.owl#hasSerialization> ?g1 . " +
	 						"FILTER(SpatialOverlap(\"POINT(2.497514 56.847728)\",?g1))" +
	 					//	"FILTER(!sameTerm(?g1,?g2))" +
	 						"} limit 10";
				
				 String geosparqlProjectionQuery = "" +
				 		//"prefix geo: <http://www.opengis.net/ont/geosparql#> \n" +
				 		"select distinct ?x ?y ?z where {" +
	 						"?x ?y ?z " +
	 						"} limit 10";
				 String spatialProjectionQuery = "select distinct ?g1 where {" +
	 						"?x1 <http://meraka/moss/exampleBooks.owl#hasSerialization> ?g1 . " +
	 						"} limit 10";
				 String wellboresInFields = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				 		"PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
				 		"SELECT distinct ?x1 " +
				 		"WHERE {" +
	 						"?x1 :hasWKTGeometry ?g1 . " +
	 						"?x1 rdf:type :fieldArea ." +
	 						"?x2 :hasWKTGeometry ?g2 . " +
	 						"?x2 rdf:type :wellborePoint ." +
	 						"FILTER(SpatialOverlap(?g2,?g1))" +
	 						"} limit 10";
				 
				 String qualitativeSpatialQuery = "select distinct ?x1 where {" +
	 						"?x1 <http://meraka/moss/exampleBooks.owl#overlaps> ?x2 . " +
	 						"} limit 10";
				 
				 String NonSpatialQuery = "select distinct ?x1 where {" +
	 						"?x1 <http://meraka/moss/exampleBooks.owl#hasSerialization> ?g1 . " +
	 						"?x2 <http://meraka/moss/exampleBooks.owl#hasSerialization> ?g2 . " +
	 					//	"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"FILTER(!sameTerm(?x1,?x2))" +
	 						"} limit 10";
				 
			      String queryString = 
			      		"select distinct ?x ?o where {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o ." +
			      		"?x <http://meraka/moss/exampleBooks.owl#writtenBy> ?a . " +
			      		"?x2 <http://meraka/moss/exampleBooks.owl#writtenBy> ?a2. " +
			      	//	"FILTER(SpatialOverlap(?a,?a2)) }" ;
			       	"FILTER(!sameTerm(?a,?a2)) }" ;
			      	//	"?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o2." +
			      		//"FILTER(sameTerm(?x,?x2))} " ;
			      
			      String query2 = "select distinct ?s where { ?s <http://meraka/moss/exampleBooks.owl#writtenBy> ?o}";
			      		//"FILTER (isURI(?o)) }" ;
			      		//"?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://meraka/moss/exampleBooks.owl#Book> ." +
			      		//"FILTER(isURI(?x1)) }";
			      		//"<http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819752067.owl#Book>}";
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, wellboresInFields);
			      
				  TupleQueryResultHandler handler = new SPARQLResultsTSVWriter(System.out);
				  
				//  TupleQueryResultHandler spatialHandler  = new stSPARQLResultsKMLWriter(System.out);
			     //  tupleQuery.evaluate(handler);
			   
			      
			     /* queryString =  "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
			      GraphQuery graphQuery = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
			      GraphQueryResult gresult = graphQuery.evaluate();
			      while(gresult.hasNext())
			      {
			    	  Statement s = gresult.next();
			    	  System.out.println(s.toString());
			      }
			      */
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

