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

public class SesameVirtualVistaTest extends TestCase {

	
	
	public void test() throws Exception
	{
		
		
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			//String owlfile = "/home/constant/Vista/urban_atlas_melod.owl";
			//String owlfile = "/home/constant/books.owl";
			String owlfile = "/home/constant/vista/vista.owl";
			String obdafile = "/home/constant/vista/vista_ftb.obda";
			String r2rml = "/home/constant/vista/vista_ftb.ttl";
			//String owlfile = 	"/home/timi/ontologies/helloworld/helloworld.owl";
			repo = new SesameVirtualRepo("my_name", owlfile, obdafile, false, "TreeWitness");
	
			repo.initialize();
			
			con = repo.getConnection();
			
			 String prefixes = "prefix ex: <http://meraka/moss/exampleBooks.owl#> \n "
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
						+ "PREFIX cd: <http://melodiesproject.eu/CityDistricts/ontology#> \n"
						+ "PREFIX geo: <http://www.opengis.net/ont/geosparql#> \n"
						+ "PREFIX vista: <http://melodiesproject.eu/vista/ontology#> \n"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" ;
			
			///query repo
			 try {
				 String preds = prefixes +  "select distinct ?p  where {" +
				 						"?s1 ?p ?g1 . " +
				 						"}";
				 
				 String datatype = prefixes +  "select distinct ?g1  \n"
				 		+ "where {" +
	 						"?s1 geo:asWKT ?g1 .\n "
					 		+ "?s1 rdf:type y:Field .  "
	 						+ "} limit 10 "  ;
				 
				 String ftb = prefixes + "select ?type   \n"
				 		+ " where { \n"
				 		+ "?s1 geo:asWKT ?g1 ."
				 		+ "?s1 rdf:type y:Field .  "
				 		+ "?s2 geo:asWKT ?g2 . "
				 		+ "?s2 rdf:type ?type ."
				 		+ "?s1 y:hasTFBValue ?tfb ."
				 		+ "filter (<http://www.opengis.net/def/function/geosparql/sfOverlaps>(?g1, ?g2)) ."
				 		+ "filter (?tfb < 0)"
				 		+ "}";
				 
				 String fieldsInBiotops =  prefixes +  "select distinct ?s1   where { \n" +
	 						"?s1 f:asWKT ?g1 . " +
	 						 "?s1 rdf:type f:Field ." +
	 						 "?s2 rdf:type bio:Biotope ." +
	 						"?s2 geo:asWKT ?g2 . " +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 
				 String fieldsInBsph = prefixes +  "select distinct ?s1  where {" +
	 						"?s1 f:asWKT ?g1 . "  +
	 						 "?s1 rdf:type f:Field ." +
	 						 "?s2 rdf:type bsph:BioshpereReserve ." +
	 						"?s2 geo:asWKT ?g2 . " +
	 				 	"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsInFfh = prefixes +  "select distinct ?s2  where {" +
	 					 	"?s1 f:asWKT ?g1 . " +
	 						"?s2 geo:asWKT ?g2 . " +
	 						 "?s1 rdf:type f:Field ." +
	 						 "?s2 rdf:type ffh:NaturaArea ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsInNap = prefixes +  "select distinct ?s2  where {" +
	 						"?s1 f:asWKT ?g1 . " +
	 						"?s2 geo:asWKT ?g2 . "
	 						+ "?s1 rdf:type f:Field ."
	 						+ "?s2 rdf:type nap:NaturalPark ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsInNsg = prefixes +  "select distinct ?s1  where {" +
	 						"?s1 f:asWKT ?g1 . " +
	 						"?s2 geo:asWKT ?g2 . "
	 						+ "?s1 rdf:type f:Field ."
	 						+ "?s2 rdf:type nsg:ConservationArea ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsInNtp = prefixes +  "select distinct ?s1  where {" +
	 						"?s1 f:asWKT ?g1 . "
	 						+ "?s1 rdf:type f:Field ." +
	 						"?s2 geo:asWKT ?g2 . " +
	 						 "?s2 rdf:type ntp:NaturalPark ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsIntwsg = prefixes +  "select distinct ?s2  where {" +
	 						"?s1 f:asWKT ?g1 . " +
	 						"?s2 geo:asWKT ?g2 ." +
	 						 "?s1 rdf:type f:Field ." +
	 						 "?s2 rdf:type twsg:DrinkingWaterProtectionZone . " +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 
				 String fieldsInBpa = prefixes +  "select distinct ?s2 ?g2  where {" +
	 						"?s1 f:asWKT ?g1 . "
	 						+ "?s1 rdf:type f:Field2 ." +
	 						"?s2 geo:asWKT ?g2 . " +
	 						 "?s2 rdf:type bpa:BirdProtectionArea ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";
				 

				 String fieldsInAnything = prefixes +  "select distinct ?s2 ?t  where {" +
	 						"?s1 f:asWKT ?g1 . " +
	 						"?s2 geo:asWKT ?g2 ." +
	 						 "?s1 rdf:type f:Field2 ." +
	 						 "?s2 rdf:type ?t . " +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
	 						"} limit 10 ";

				 
				 
				 String ftbq = prefixes + "select  distinct ?s1   \n"
					 		+ " where { \n"
					 	+ "?s1 geo:asWKT ?g1 ."
					 		+ "?s1 rdf:type vista:Field .  "
					 		+ "?s2 geo:asWKT ?g2 . "
					 		+ "?s1 vista:hasTFBValue ?tfb ."
					 		+ "?s2 rdf:type ?type."
					 		+ "?s2 rdf:type vista:ProtectedArea."
					 //	+ "?type rdfs:subClassOf vista:ProtectedArea ."
					 	+ "?s1 geo:sfOverlaps ?s2 ."
					 		+ "filter (<http://www.opengis.net/def/function/geosparql/sfOverlaps>(?g1, ?g2)) ."
					 		+ "filter (?tfb < 0)"
					 		+ "}";	 
				 
				 String fieldsInCLC = prefixes + "select  ?s1   \n"
					 		+ " where { \n"
					 	//	+ "?s1 geo:asWKT ?g1 ."
					 		+ "?s1 rdf:type vista:Field .  "
					 	//	+ "?s2 geo:asWKT ?g2 . "
					 		+ "?s1 vista:hasTFBValue ?tfb ."
					 		+ "?s2 rdf:type vista:CLCArea."
					 		+ "?s1 geo:sfOverlaps ?s2 ."
					 		//+ "filter (<http://www.opengis.net/def/function/geosparql/sfOverlaps>(?g1, ?g2)) ."
					 		//+ "filter (?tfb < 0)"
					 		+ "}";	
				 
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, fieldsInCLC);
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

