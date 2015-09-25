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
import java.io.File;
import java.io.FileOutputStream;
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
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;
//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriterFactory;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import sesameWrapper.SesameVirtualRepo;

//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;

public class SesameVirtualGisatTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			//String owlfile = "/home/constant/gisat/urban_atlas_melod.owl";
			//String owlfile = "/home/constant/books.owl";
			String owlfile = "/home/constant/gisat/CityDistricts.owl";
			String obdafile = "/home/constant/gisat/gisat-datatype.obda";
			//String owlfile = 	"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			String prefixes = "prefix ex: <http://meraka/moss/exampleBooks.owl#> \n "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
					+ "PREFIX cd: <http://melodiesproject.eu/CityDistricts/ontology#> \n"
					+ "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#> \n"
					+ "PREFIX : <http://meraka/moss/exampleBooks.owl#> \n"
					+ "PREFIX ua: <http://geo.linkedopendata.gr/urban/ontology#>";
			
			///query repo
			 try {
				 String spatialQuery = prefixes +  "select distinct ?name  where {" +
				 						"?x1 cd:hasWKTGeometry ?g1 . " +
   			 					         "?x1  rdf:type cd:mcArea ."
   			 					       + "?x1 cd:hasName ?name ." +
				 						//" ?x2 ex:hasSerialization ?g2 . " +
				 					//	"FILTER(SpatialOverlap(?g1,?g2))" +
				 					//	"FILTER(!sameTerm(?g1,?g2))" +
				 						"} limit 10";
				 
				 String spatialConstantQuery = "select distinct ?x1 ?g1 where {" +
	 						"?x1 <http://meraka/moss/exampleBooks.owl#hasSerialization> ?g1 . " +
	 						"FILTER(SpatialOverlap(\"POINT(2.497514 56.847728)\",?g1))" +
	 					//	"FILTER(!sameTerm(?g1,?g2))" +
	 						"} limit 10";
				

				 String floodsInAreas = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				 		"PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
				 		"SELECT distinct ?x1 \n" +
				 		"WHERE {" +
	 						"?x1 :hasWKTGeometry ?g1 . " +
	 						"?x1 rdf:type :prahaArea ." +
	 					  //  "?x1 :hasLandUse ?use ." +
	 						"?x2 :hasWKTGeometry ?g2 . " +
	 						"?x2 rdf:type :floodArea ." +
	 						"FILTER(<http://www.opengis.net/def/function/geosparql/sfOverlaps>(?g2,?g1))" +
	 						"} limit 10";
				 
				 String praha = prefixes+
					 		"SELECT distinct ?x1 ?g1 ?code ?area  \n" +
					 		"WHERE { \n" +
		 						"?x1 ua:asWKT ?g1 . " +
		 					    "?x1 ua:hasArea ?area ." +
		 					     "?x1 ua:hasCode ?code ."
		 					     + "FILTER((?code = 11270 || ?code = 12100) && ?area < 5000)" +
		 					     "} limit 10" ;
		 				
				 String cd_selection = prefixes + "SELECT distinct ?partCD \n"
				 		+ "WHERE { \n"
				 		+ "?partCD cd:HAS_MC_2001_ID \"36\" . "
				 		+"?partCD rdf:type cd:mcArea . \n" 
				 		+ "?partCD cd:hasName ?nameCD .  \n"
				 		+ "?partCD cd:HAS_DAREA ?districtArea .  \n"
				 		+ "?partCD cd:HAS_UAREA ?urbanisedArea .  \n"
				 		+ "?partCD cd:HAS_GAREA ?greeneryArea .  \n"
				 		+ "?partCD geosparql:asWKT ?geom . \n"
				 		+ "} limit 10 ";
				 
				 String gisat = prefixes + "SELECT distinct ?x1  ?uageo  \n"
					 		+ "WHERE { \n"
					 		+ "?x1 ua:asWKT ?uageo . \n" 
	 					    + "?x1 ua:hasArea ?area . \n" 
	 					    + "?x1 ua:hasUACode ?code . \n" 
	 					    +  "FILTER((?code = 11270 || ?code = 12100) && ?area > 5000) ."
					 		+ "?partCD cd:hasID \"36\" .  \n"
					 		//+"?partCD rdf:type cd:mcArea . \n" 
					 		+ "?partCD cd:hasDistrictName ?nameCD .  \n"
					 		+ "?partCD cd:hasDArea ?districtArea .  \n"
					 		+ "?partCD cd:hasUArea ?urbanisedArea .  \n"
					 		+ "?partCD cd:hasGArea ?greeneryArea .  \n"
					 		+ "?partCD cd:asWKT ?cdgeo . \n"
	 						+"FILTER(<http://www.opengis.net/def/function/geosparql/sfContains>(?cdgeo,?uageo)) \n" 
					 		+ "}";
					 
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, gisat);
			      FileOutputStream f = new FileOutputStream("/home/constant/ontop-kml/gisat.kml");
				  TupleQueryResultHandler handler = new SPARQLResultsTSVWriter(System.out);
				  //TupleQueryResultWriterFactory kml = new stSPARQLResultsKMLWriterFactory();

				//  TupleQueryResultHandler spatialHandler  = new stSPARQLResultsKMLWriter(f);
			       tupleQuery.evaluate(handler);
			   
			      
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

