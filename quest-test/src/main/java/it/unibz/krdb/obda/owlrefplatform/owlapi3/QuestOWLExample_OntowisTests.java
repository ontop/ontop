package it.unibz.krdb.obda.owlrefplatform.owlapi3;

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

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;
import it.unibz.krdb.sql.ImplicitDBConstraints;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

interface ParamConst{
	public static final String POSTGRESInt = "src/main/resources/example/postgres-NoViews-joins-int.obda";
	public static final String POSTGRESStr = "src/main/resources/example/postgres-NoViews-joins-str.obda";
	public static final String POSTGRESStrView = "src/main/resources/example/postgres-Views-joins-str.obda";
	public static final String POSTGRESIntView = "src/main/resources/example/postgres-Views-joins-int.obda";

	public static final String DB2 = "src/main/resources/example/ontowis-5joins-int-db2.obda";
	public static final String MYSQL_VIEW = "src/main/resources/example/ontowis-5joins-int-view.obda";
	public static final String MYSQL = "src/main/resources/example/ontowis-5joins-int.obda";
	public static final String DB2_VIEW = "src/main/resources/example/ontowis-5joins-int-view-db2.obda";
}
public class QuestOWLExample_OntowisTests {
	
	public QuestOWLExample_OntowisTests(String obdaFile){
		this.obdafile = obdaFile;
	}
	
	/*
	 * Use the sample database using H2 from
	 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
	 * 
	 * Please use the pre-bundled H2 server from the above link
	 * 
	 */
	String owlfile = "src/main/resources/example/ontowis-5joins-int.owl";
	String obdafile;
	String usrConstrinFile = "src/main/resources/example/funcCons.txt";
	
	QuestOWL reasoner ;
	
	
	
	// Exclude from T-Mappings
	//final String tMappingsConfFile = "src/main/resources/example/tMappingsConf.conf";
	
	public void runQuery() throws Exception {

		/*
		 * Get the book information that is stored in the database
		 */
		
		String[] queriesOneSPARQL = new String[24];
		String[] queriesTwoSPARQL = new String[24];
		String[] queriesThreeSPARQL = new String[24];
		String[] resultsOne = new String[31];
		String[] resultsTwo = new String[31];
		String[] resultsThree = new String[31];

		

		/*
		 * OneSPARQL JOINS
		 */
		//1 SQL JOIN
		queriesOneSPARQL[0]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y . Filter( ?y < 1000)  }  ";
		queriesOneSPARQL[1]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 10000)  } ";
		queriesOneSPARQL[2]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 20000)  } ";
		queriesOneSPARQL[3]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 50000)  } ";
		queriesOneSPARQL[4]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 100000)  } ";
		queriesOneSPARQL[5]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  Filter( ?y0 < 500000)  } ";


		queriesOneSPARQL[6]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y . Filter( ?y < 1000)  }  ";
		queriesOneSPARQL[7]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 10000)  } ";
		queriesOneSPARQL[8]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 20000)  } ";
		queriesOneSPARQL[9]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 50000)  } ";
		queriesOneSPARQL[10]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 100000)  } ";
		queriesOneSPARQL[11]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  Filter( ?y1 < 500000)  } ";


		queriesOneSPARQL[12]="PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :3Tab1 . ?x :Tab4unique2Tab4 ?y . Filter( ?y < 1000)  } "; 
		queriesOneSPARQL[13]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 10000)  }"; 
		queriesOneSPARQL[14]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 20000)  } ";
		queriesOneSPARQL[15]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 50000)  } ";
		queriesOneSPARQL[16]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 100000)  } ";
		queriesOneSPARQL[17]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  Filter( ?y2 < 500000)  } ";



		//4 SQL JOIN
		queriesOneSPARQL[18]="PREFIX :	<http://www.example.org/>  SELECT ?x ?y  WHERE {?x a  :4Tab1 . ?x :Tab5unique2Tab5 ?y . Filter( ?y < 1000) } ";
		queriesOneSPARQL[19]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 10000)  } ";
		queriesOneSPARQL[20]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 20000)  } ";
		queriesOneSPARQL[21]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 50000)  } ";
		queriesOneSPARQL[22]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 100000)  } ";
		queriesOneSPARQL[23]="PREFIX :	<http://www.example.org/>  select  ?x   ?y3   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y3.  Filter( ?y3 < 500000)  } ";

		
		
		
		
		
		/*
		 * TWO SPARQL JOINS
		 */
		//1 SQL JOIN
		queriesTwoSPARQL[0]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .  ?x :hasString2j ?y2 . Filter( ?y < 1000)  }  ";
		queriesTwoSPARQL[1]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString2j ?y2 .  Filter( ?y0 < 10000)  } ";
		queriesTwoSPARQL[2]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString2j ?y2 .  Filter( ?y0 < 20000)  } ";
		queriesTwoSPARQL[3]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString2j ?y2 .  Filter( ?y0 < 50000)  } ";
		queriesTwoSPARQL[4]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString2j ?y2 .  Filter( ?y0 < 100000)  } ";
		queriesTwoSPARQL[5]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.   ?x :hasString2j ?y2 .  Filter( ?y0 < 500000)  } ";


		queriesTwoSPARQL[6]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y .  ?x :hasString3j ?y2 .  Filter( ?y < 1000)  }  ";
		queriesTwoSPARQL[7]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString3j ?y2 .  Filter( ?y1 < 10000)  } ";
		queriesTwoSPARQL[8]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString3j ?y2 .  Filter( ?y1 < 20000)  } ";
		queriesTwoSPARQL[9]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString3j ?y2 .  Filter( ?y1 < 50000)  } ";
		queriesTwoSPARQL[10]=   "PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString3j ?y2 .  Filter( ?y1 < 100000)  } ";
		queriesTwoSPARQL[11]=   "PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString3j ?y2 .  Filter( ?y1 < 500000)  } ";


		queriesTwoSPARQL[12]="PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :3Tab1 . ?x :Tab4unique2Tab4 ?y .  ?x :hasString4j ?y2 .  Filter( ?y < 1000)  } "; 
		queriesTwoSPARQL[13]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString4j ?y2 .  Filter( ?y2 < 10000)  }"; 
		queriesTwoSPARQL[14]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString4j ?y2 .  Filter( ?y2 < 20000)  } ";
		queriesTwoSPARQL[15]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString4j ?y2 .  Filter( ?y2 < 50000)  } ";
		queriesTwoSPARQL[16]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString4j ?y2 .  Filter( ?y2 < 100000)  } ";
		queriesTwoSPARQL[17]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.   ?x :hasString4j ?y2 .  Filter( ?y2 < 500000)  } ";

		//4 SQL JOIN
		queriesTwoSPARQL[18]="PREFIX :	<http://www.example.org/>  SELECT *  WHERE {?x a  :4Tab1 . ?x :Tab5unique2Tab5 ?y . ?x :hasString5j ?y2 .   Filter( ?y < 1000)  } ";
		queriesTwoSPARQL[19]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 .   Filter( ?y < 10000)  } ";
		queriesTwoSPARQL[20]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 .   Filter( ?y < 20000)  } ";
		queriesTwoSPARQL[21]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 .   Filter( ?y < 50000)  } ";
		queriesTwoSPARQL[22]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 .   Filter( ?y < 100000)  } ";
		queriesTwoSPARQL[23]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 .   Filter( ?y  < 500000)  } ";

		
		
		
		

		/*
		 * Three SPARQL JOINS
		 */
		
		
		//1 SQL JOIN
		queriesThreeSPARQL[0]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .  ?x :hasString5j ?y2 . ?x :hasString22j ?y3 .Filter( ?y < 1000)  }  ";
		queriesThreeSPARQL[1]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString5j ?y2 . ?x :hasString22j ?y3 . Filter( ?y0 < 10000)  } ";
		queriesThreeSPARQL[2]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString5j ?y2 . ?x :hasString22j ?y3 . Filter( ?y0 < 20000)  } ";
		queriesThreeSPARQL[3]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString5j ?y2 . ?x :hasString22j ?y3 . Filter( ?y0 < 50000)  } ";
		queriesThreeSPARQL[4]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.  ?x :hasString5j ?y2 . ?x :hasString22j ?y3 . Filter( ?y0 < 100000)  } ";
		queriesThreeSPARQL[5]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y0   where { ?x a :1Tab1 . ?x :Tab2unique2Tab2 ?y0.   ?x :hasString5j ?y2 . ?x :hasString22j ?y3 . Filter( ?y0 < 500000)  } ";

		//2 SQL JOIN
		queriesThreeSPARQL[6]=	"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y .  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y < 1000)  }  ";
		queriesThreeSPARQL[7]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y1 < 10000)  } ";
		queriesThreeSPARQL[8]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y1 < 20000)  } ";
		queriesThreeSPARQL[9]=	"PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y1 < 50000)  } ";
		queriesThreeSPARQL[10]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y1 < 100000)  } ";
		queriesThreeSPARQL[11]="PREFIX :	<http://www.example.org/>  select  ?x   ?y1   where { ?x a :2Tab1 . ?x :Tab3unique2Tab3 ?y1.  ?x :hasString5j ?y2 . ?x :hasString23j ?y3 . Filter( ?y1 < 500000)  } ";

		//3 SQL JOIN
		queriesThreeSPARQL[12]="PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :3Tab1 . ?x :Tab4unique2Tab4 ?y .  ?x :hasString5j ?y2 . ?x :hasString25j ?y3 . Filter( ?y < 1000)  } "; 
		queriesThreeSPARQL[13]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString5j ?y2 . ?x :hasString24j ?y3 . Filter( ?y2 < 10000)  }"; 
		queriesThreeSPARQL[14]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString5j ?y2 . ?x :hasString24j ?y3 . Filter( ?y2 < 20000)  } ";
		queriesThreeSPARQL[15]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString5j ?y2 . ?x :hasString24j ?y3 . Filter( ?y2 < 50000)  } ";
		queriesThreeSPARQL[16]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.  ?x :hasString5j ?y2 . ?x :hasString24j ?y3 . Filter( ?y2 < 100000)  } ";
		queriesThreeSPARQL[17]="PREFIX :	<http://www.example.org/>  select  ?x   ?y2   where { ?x a :3Tab1 . ?x :Tab4unique2Tab4 ?y2.   ?x :hasString5j ?y2 . ?x :hasString24j ?y3 . Filter( ?y2 < 500000)  } ";

		//4 SQL JOIN
		queriesThreeSPARQL[18]="PREFIX :	<http://www.example.org/>  SELECT *  WHERE {?x a  :4Tab1 . ?x :Tab5unique2Tab5 ?y . ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y < 1000)  } ";
		queriesThreeSPARQL[19]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y < 10000)  } ";
		queriesThreeSPARQL[20]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y < 20000)  } ";
		queriesThreeSPARQL[21]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y < 50000)  } ";
		queriesThreeSPARQL[22]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y < 100000)  } ";
		queriesThreeSPARQL[23]="PREFIX :	<http://www.example.org/>  select  *   where { ?x a :4Tab1 . ?x :Tab5unique2Tab5 ?y. ?x :hasString5j ?y2 . ?x :hasString25j ?y3 .  Filter( ?y  < 500000)  } ";



	//	queries[30]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 .   } LIMIT 100000  ";

		QuestOWLConnection conn =  createStuff(false);
		
		runQueries(conn, queriesOneSPARQL, resultsOne);

		runQueries(conn, queriesTwoSPARQL, resultsTwo);
		
		runQueries(conn, queriesThreeSPARQL, resultsThree);
		
		
		closeEverything(conn);
		generateFile(resultsOne, resultsTwo, resultsThree);
		

	
	
	
	}

	/**
	 * @param queriesOneSPARQL
	 * @param resultsOne
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private void generateFile( String[] resultsOne, String[] resultsTwo, String[] resultsThree) throws FileNotFoundException, UnsupportedEncodingException {
		/*
		 * Generate File !
		 */
		PrintWriter writer = new PrintWriter("src/main/resources/example/table.txt", "UTF-8");
		PrintWriter writerG = new PrintWriter("src/main/resources/example/graph.txt", "UTF-8");
		
		
		
		
		int j=0;
		while (j<24){
			writer.println(resultsOne[j] + " & " + resultsTwo[j] + " & " + resultsThree[j]);
			
			if (j<=5){
				String gline = "(1,"+resultsOne[j]+")" + "(2,"+resultsTwo[j]+")" + "(3,"+resultsThree[j]+")" + "(4,"+resultsOne[j+6]+")" + "(5,"+resultsTwo[j+6]+")" + "(6,"+resultsThree[j+6]+")" + "(7,"+resultsOne[j+12]+")" + "(8,"+resultsTwo[j+12]+")" + "(9,"+resultsThree[j+12]+")" + "(10,"+resultsOne[j+18]+")" + "(11,"+resultsTwo[j+18]+")" + "(12,"+resultsThree[j+18]+")" ;
				writerG.println(gline);
			}
			
			
			j++;
		
		
		
		}
		
		
		writer.close();
		writerG.close();
	}

	/**
	 * @param conn
	 * @throws OWLException
	 */
	private void closeEverything(QuestOWLConnection conn) throws OWLException {
		/*
		 * Close connection and resources
		 */
		
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
		this.reasoner.dispose();
	}

	/**
	 * @param reasoner
	 * @param conn
	 * @param queriesOneSPARQL
	 * @param results
	 * @throws OBDAException 
	 * @throws OWLOntologyCreationException 
	 * @throws InvalidMappingException 
	 * @throws InvalidPredicateDeclarationException 
	 * @throws IOException 
	 * @throws OWLException
	 */
	
	
	
	private QuestOWLConnection createStuff(boolean manualKeys) throws OBDAException, OWLOntologyCreationException, IOException, InvalidPredicateDeclarationException, InvalidMappingException{

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
//		TEST preference.setCurrentValueOf(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings
		
		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		
		/*
		 * USR CONSTRAINTS !!!!
		 */
		
		if (manualKeys){
			ImplicitDBConstraints constr = new ImplicitDBConstraints(usrConstrinFile);
			factory.setImplicitDBConstraints(constr);
		}
		/*
		 * T-Mappings Handling!!
		 */
		//TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
		//factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		this.reasoner = reasoner;
		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();
		
		return conn;

	}
	
	
	private void runQueries( QuestOWLConnection conn,
			String[] queriesOneSPARQL, String[] results) throws OWLException {
		int j=0;
		while (j < queriesOneSPARQL.length){
			String sparqlQuery = queriesOneSPARQL[j];
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
	            
	            results[j] = (time/4)+"" ;
				
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
		
	

	}

	/**
	 * Main client program
	 */
	public static void main(String[] args) {
		
		String obdaFile = null;
		
		switch(args[0]){
		case "--help":{
			System.out.println(
					"Options:\n\n"
					+ "--MYSQL; --POSTGRES; --DB2; "
					+ "--MYSQL-VIEW; --POSTGRES-VIEW; --DB2-VIEW"
					+ "\n\n"
					+ "The concepts for which T-mappings should"
					+ "be disabled are defined the file tMappingsConf.conf");
			System.exit(0);
			break;
		}
		
		case "--MYSQL":{
			obdaFile = ParamConst.MYSQL;
			break;
		}
		case "--POSTGRESInt":{
			obdaFile = ParamConst.POSTGRESInt;
			break;
		}
		
		case "--POSTGRESStr":{
			obdaFile = ParamConst.POSTGRESStr;
			break;
		}
		case "--POSTGRESIntView":{
			obdaFile = ParamConst.POSTGRESIntView;
			break;
		}
		case "--POSTGRESStrView":{
			obdaFile = ParamConst.POSTGRESStrView;
			break;
		}
		case "--DB2" :{
			obdaFile = ParamConst.DB2;
			break;
		}
		case "--MYSQL-VIEW":{
			obdaFile = ParamConst.MYSQL_VIEW;
			break;
		}
		case "--DB2-VIEW":{
			obdaFile = ParamConst.DB2_VIEW;
		}
		}	
		try {
			System.out.println(obdaFile);
			
			QuestOWLExample_OntowisTests example = new QuestOWLExample_OntowisTests(obdaFile);
			example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
