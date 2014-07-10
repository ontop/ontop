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
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import sesameWrapper.SesameVirtualRepo;





//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriter;
//import org.openrdf.query.resultio.sparqlkml.stSPARQLResultsKMLWriterFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class SesameR2RMLSpatialVirtualTest extends TestCase {

	
	
	public void test() throws Exception
	{
		//create a sesame repository
		RepositoryConnection con = null;
		Repository repo = null;
		
		try {
			
			String owlFile = "/home/constant/books.owl";
			String ttlFile = "/home/constant/mappings-ontop/npd-plainLiterals.ttl";
			
			//create owlontology from file
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(owlFile).getParentFile(), false);
			man.addIRIMapper(iriMapper);
			OWLOntology owlontology = man.loadOntologyFromOntologyDocument(new File(owlFile));
			
			
				//"/home/timi/ontologies/helloworld/helloworld.owl";
			RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
			
			//TupleQueryResultWriterFactory kml = new stSPARQLResultsKMLWriterFactory();
			
			InputStream in = new FileInputStream(ttlFile);
			URL documentUrl = new URL("file://" + ttlFile);
			TreeModel myGraph = new org.openrdf.model.impl.TreeModel();
			StatementCollector collector = new StatementCollector(myGraph);
			parser.setRDFHandler(collector);
			parser.parse(in, documentUrl.toString());
			
			QuestPreferences pref = new QuestPreferences();
			pref.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			pref.setCurrentValueOf(QuestPreferences.REWRITE, "true");
			pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
			//set jdbc params in config
			pref.setCurrentValueOf(QuestPreferences.DBNAME, "npd");
			pref.setCurrentValueOf(QuestPreferences.JDBC_URL, "jdbc:postgresql://localhost:5432/npd");
			pref.setCurrentValueOf(QuestPreferences.DBUSER, "postgres");
			pref.setCurrentValueOf(QuestPreferences.DBPASSWORD, "p1r3as");
			pref.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, "org.postgresql.Driver");
			
			
			
			repo = new SesameVirtualRepo("virtualSpatialExample", owlontology,myGraph,pref);

					
			
			

			//System.out.println(myGraph);
			/*
			 * Repository must be always initialized first
			 */
			repo.initialize();

			/*
			 * Get the repository connection
			 */
			 con = repo.getConnection();
			 
			 String prefixes = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			 		"prefix geo: <http://www.opengis.net/ont/geosparql#> \n";

			///query repo
			 try {
				 String spatialQuery = prefixes + "select distinct ?x1 ?x2 \n where \n{" +
				 						"?x1 geo:asWKT ?g1 . \n" +
				 						" ?x2 geo:asWKT ?g2 . \n" +
				 						"FILTER(SpatialOverlap(?g1,?g2)) \n" +
				 						"} limit 10";
				 String spatialConstantQuery = prefixes + "select distinct ?x1 ?g1 where {" +
	 						"?x1 geo:asWKT ?g1 . " +
	 						"FILTER(SpatialOverlap(\"POINT(2.497514 56.847728)\",?g1))" +
	 					//	"FILTER(!sameTerm(?g1,?g2))" +
	 						"} limit 10";
				
				 String geosparqlProjectionQuery = prefixes +
				 		//"prefix geo: <http://www.opengis.net/ont/geosparql#> \n" +
				 		"select distinct  ?y  where {" +
	 						"?x ?y ?z " +
	 						"} limit 10";
				 String spatialProjectionQuery = prefixes + "select distinct ?g1 where {" +
	 						"?x1 geo:asWKT ?g1 . " +
	 						"} limit 10";
				 String w = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				 		"PREFIX : <http://meraka/moss/exampleBooks.owl#>" +
				 		"SELECT distinct ?x1 " +
				 		"WHERE {" +
	 						"?x1 :hasWKTGeometry ?g1 . " +
	 						"?x1 rdf:type :fieldArea ." +
	 						"?x2 :hasWKTGeometry ?g2 . " +
	 						"?x2 rdf:type :wellborePoint ." +
	 						"FILTER(SpatialOverlap(?g1,?g2))" +
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
			      
	
			      TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, spatialQuery);
			      
				  TupleQueryResultHandler handler = new SPARQLResultsTSVWriter(System.out);
				  
				 // TupleQueryResultHandler spatialHandler  = kml.getWriter(System.out);
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

