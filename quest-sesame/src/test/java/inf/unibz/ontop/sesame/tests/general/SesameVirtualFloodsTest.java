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

public class SesameVirtualFloodsTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			//String owlfile = "/home/constant/Vista/urban_atlas_melod.owl";
			//String owlfile = "/home/constant/books.owl";
			String owlfile = "/home/constant/wp8/floods.owl";
			String obdafile = "/home/constant/wp8/wp8.obda";
			String r2rml = "/home/constant/vista/vista_ftb.ttl";
			//String owlfile = 	"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			 String prefixes = "prefix ex: <http://meraka/moss/exampleBooks.owl#> \n "
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
						+ "PREFIX f: <http://melodiesproject.eu/floods/> \n"
						+ "PREFIX geo: <http://www.opengis.net/ont/geosparql#> \n"
						+ "PREFIX gadm: <http://melodiesproject.eu/gadm/> \n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
						+ "PREFIX osm: <http://melodiesproject.eu/osm/> \n"
						+ "PREFIX clc: <http://melodiesproject.eu/clc/> \n" ;
			
			///query repo
			 try {
				 String preds = prefixes +  "select distinct ?p  where {" +
				 						"?s1 ?p ?g1 . " +
				 						"}";
				 
				 String floodsType = prefixes +  "select distinct ?type  \n"
				 		+ "where {" 
					 		+ "?s1 f:type ?type .  \n"
	 						+ "} limit 10 "  ;
				 
				 String gadm = prefixes +  "select  ?city ?country ?name0 ?name1 ?name2 ?name3  \n"
					 		+ "where {" 
						 		+ "?s1 f:type ?type . \n "
						 		+ "?s1 geo:asWKT ?g1 . \n"
						 		+ "?s1 f:city ?city . \n"
						 		+ "?s1 f:country ?country . \n"
						 		+ "?s2 gadm:name0 ?name0 . \n"
						 		+ "?s2 gadm:name1 ?name1 . \n"
						 		+ "?s2 gadm:name2 ?name2 . \n"
						 		+ "?s2 gadm:name3 ?name3 . \n"
						 		+ "?s2 geo:asWKT ?g2 . "
						 		+ "filter(<http://www.opengis.net/def/function/geosparql/sfIntersects>(?g1, ?g2)) \n"

		 						+ "}  "  ;
		
				 
				 String osm = prefixes +  "select   ?cat ?type   \n"
					 		+ "where {" 
						 		+ "?s1 f:type ?type . \n "
						 		+ "?s1 geo:asWKT ?g1 . \n"
						 		+ "?s1 f:city ?city . \n"
						 		+ "?s1 f:country ?country . \n"
						 		+ "?s2 rdf:type osm:Waterway . \n"
						 		+ "?s2 osm:waterwayCategory ?cat . "
						 	//+ "?s2 geo:hasGeometry ?geom2 .\n"
						 	+ "?s2 geo:asWKT ?g2 . "
						 //	+ "?s2 osm:wName ?wname \n"
						 	//	+ "?s3 gadm:name2 ?name2 . \n"
						 	//	+ "?s3 rdf:type gadm:AdministrativeDivision .\n"
						 //		+ "?s3 gadm:name3 ?name3 . \n"
						 //		+ "?s3 geo:asWKT ?g3 . \n"
						 		+ "filter(<http://www.opengis.net/def/function/geosparql/sfIntersects>(?g1, ?g2)) \n"
						// 		+ "filter(<http://www.opengis.net/def/function/geosparql/sfIntersects>(?g1, ?g3)) \n"
		 						+ "} limit 5 "  ;
				 
				 String floods_clc = prefixes +  "select   ?s2 ?type ?city ?country ?id \n"
					 		+ "where {" 
						 		+ "?s1 f:type ?type . \n "
						 		+ "?s1 geo:asWKT ?g1 . \n"
						 		+ "?s1 f:city ?city . \n"
						 		+ "?s1 f:country ?country . \n"
						 		+ "?s2 rdf:type clc:WaterBody . "
						 		+ " \n"
						 	+ "?s2 geo:asWKT ?g2 . \n"
						 	+ "?s2 clc:id ?id . \n"
						 		+ "filter(<http://www.opengis.net/def/function/geosparql/sfIntersects>(?g1, ?g2)) \n"
		 						+ "} "  ;
		
				 String flooded_buildings = prefixes +  "select   ?build ?name  ?type ?city ?country \n"
					 		+ "where {" 
						 		+ "?s1 f:type ?type . \n "
						 		+ "?s1 geo:asWKT ?g1 . \n"
						 		+ "?s1 f:city ?city . \n"
						 		+ "?s1 f:country ?country . \n"
						 	+ "?s2 geo:asWKT ?g2 . \n"
						 	+ "?s2 rdf:type osm:Building ."
						 	+ "?s2 osm:hasName ?name . \n"
						 	+ "?s2 osm:buildingCategory ?build . \n"
						 		+ "filter(<http://www.opengis.net/def/function/geosparql/sfIntersects>(?g1, ?g2)) \n"
		 						+ "}"  ;
		
		
				 
				 
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, flooded_buildings );
			      FileOutputStream f = new FileOutputStream("/home/constant/ontop-kml/Vista.kml");
				  TupleQueryResultHandler handler = new SPARQLResultsTSVWriter(System.out);
				  //TupleQueryResultWriterFactory kml = new stSPARQLResultsKMLWriterFactory();

				  //TupleQueryResultHandler spatialHandler  = new stSPARQLResultsKMLWriter(f);
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

