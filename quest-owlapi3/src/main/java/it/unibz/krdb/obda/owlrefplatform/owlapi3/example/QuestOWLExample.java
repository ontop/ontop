package it.unibz.krdb.obda.owlrefplatform.owlapi3.example;

/*
 * #%L
 * ontop-quest-owlapi3
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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestOWLExample {
	
	/*
	 * Use the sample database using H2 from
	 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
	 * 
	 * Please use the pre-bundled H2 server from the above link
	 * 
	 */
	final String owlfile = "src/main/resources/example/ontowis-5joins-int.owl";
	//final String obdafile = "src/main/resources/example/ontowis-5joins-int-view.obda";
	final String obdafile = "src/main/resources/example/ontowis-5joins-int-view.obda";

	public void runQuery() throws Exception {

		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		QuestPreferences preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();


		/*
		 * Get the book information that is stored in the database
		 */
		
		String[] queries = new String[25];
		String[] results = new String[25];

		queries[0]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .}  ";
		queries[1]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 1000)  } ";
		queries[2]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 2000)  } ";
		queries[3]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 5000)  } ";
		queries[4]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 10000)  } ";
		queries[5]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 50000)  } ";


		queries[6]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y .}  ";
		queries[7]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 1000)  } ";
		queries[8]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 2000)  } ";
		queries[9]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 5000)  } ";
		queries[10]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 10000)  } ";
		queries[11]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 50000)  } ";


		queries[12]="PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :3Tab1 . ?x :Tab4unique2Tab4 ?y .} "; 
		queries[13]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 1000)  }"; 
		queries[14]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 2000)  } ";
		queries[15]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 5000)  } ";
		queries[16]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 10000)  } ";
		queries[17]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 50000)  } ";




		queries[18]="PREFIX :	<http://www.example.org/>  SELECT ?x ?y  WHERE {?x a  :4Tab1 . ?x :Tab5unique2Tab5 ?y .} ";
		queries[19]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 1000)  } ";
		queries[20]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 2000)  } ";
		queries[21]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 5000)  } ";
		queries[22]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 10000)  } ";
		queries[23]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 50000)  } ";




		queries[24]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 . } ";

		
		int j=0;
		while (j < queries.length){
			String sparqlQuery = queries[j];
			QuestOWLStatement st = conn.createStatement();
			try {
	            
				long time = 0;
				int count = 0;
				
				for (int i=0; i<4; i++){
					long t1 = System.currentTimeMillis();
					QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
					int columnSize = rs.getColumnCount();
					 count = 0;
					while (rs.nextRow()) {
						count ++;
						for (int idx = 1; idx <= columnSize; idx++) {
							OWLObject binding = rs.getOWLObject(idx);
							//System.out.print(binding.toString() + ", ");
						}
						//System.out.print("\n");
					}
					long t2 = System.currentTimeMillis();
					time = time + (t2-t1);
					System.out.println("partial time:" + time);
					rs.close();
				}
	            
	 			/*
				 * Print the query summary
				 */
				QuestOWLStatement qst = (QuestOWLStatement) st;
				String sqlQuery = qst.getUnfolding(sparqlQuery);

				System.out.println();
				System.out.println("The input SPARQL query:");
				System.out.println("=======================");
				System.out.println(sparqlQuery);
				System.out.println();
				
				System.out.println("The output SQL query:");
				System.out.println("=====================");
				System.out.println(sqlQuery);

	            System.out.println("Query Execution Time:");
	            System.out.println("=====================");
	            System.out.println((time/4) + "ms");
	            
	            results[j] = (time/4) + "ms";
				
	            System.out.println("The number of results:");
	            System.out.println("=====================");
	            System.out.println(count);
	            
			} finally {
				if (st != null && !st.isClosed()) {
					st.close();
				}
			}
			j++;
		}
		
		/*
		 * Close connection and resources
		 */
		
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
		reasoner.dispose();

		
		
		/*
		 * Printing results !
		 */
				
		j=0;
		while (j<queries.length){
			  System.out.println( results[j]);
			  j++;
		}
		
//		String sparqlQuery = 
//				//"PREFIX :	<http://www.example.org/> \n" +
////						"SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .}  " ;
//					"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y .}    " ;   
	
	}

	/**
	 * Main client program
	 */
	public static void main(String[] args) {
		try {
			QuestOWLExample example = new QuestOWLExample();
			example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
