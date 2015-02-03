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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;


public class QuestOWLExample_OntowisTests {
	interface ParamConst{
		// Postgres
		public static final String POSTGRESInt = "src/main/resources/example/postgres-NoViews-joins-int.obda";
		public static final String POSTGRESStr = "src/main/resources/example/postgres-NoViews-joins-str.obda";
		public static final String POSTGRESIntView = "src/main/resources/example/postgres-Views-joins-int.obda";
		public static final String POSTGRESStrView = "src/main/resources/example/postgres-Views-joins-str.obda";
		
		// MySQL
		public static final String MYSQLInt = "src/main/resources/example/mysql-NoViews-joins-int.obda";
		public static final String MYSQLStr = "src/main/resources/example/mysql-NoViews-joins-str.obda";
		public static final String MYSQLIntView = "src/main/resources/example/mysql-Views-joins-int.obda";
		public static final String MYSQLStrView = "src/main/resources/example/mysql-Views-joins-str.obda";
		
	}
	
	enum DbType{
		MYSQL, POSTGRES, SMALL_POSTGRES
	}
	
	public static class Settings{
		static String obdaFile;
		static DbType type;
		static boolean mKeys = false;
	}

	// private static final String QuestOWLExample_OntowisTests = null;
	final String obdafile;
	final DbType type;
	boolean mKeys = false;

	final String owlfile = "src/main/resources/example/ontowis-5joins-int.owl";
	final String usrConstrinFile = "src/main/resources/example/funcCons.txt";


	// Internal Modifiable State
	QuestOWL reasoner ;

	public QuestOWLExample_OntowisTests(String obdaFile, DbType type, boolean mKeys){
		this.obdafile = obdaFile;
		this.type = type;
		this.mKeys = mKeys;
	}

	// Exclude from T-Mappings
	//final String tMappingsConfFile = "src/main/resources/example/tMappingsConf.conf";

	public void runQuery(String obdaFile) throws Exception {

		//	queries[30]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 .   } LIMIT 100000  ";

		QuestOWLConnection conn =  createStuff(mKeys);

		// Results
		String[] resultsOne = new String[31];
		String[] resultsTwo = new String[31];
		String[] resultsThree = new String[31];

		// Create Queries to be run
		QueryFactory queries = new QueryFactory(type);

		// Run the tests on the queries
		runQueries(conn, queries.queriesOneSPARQL, resultsOne);		
		runQueries(conn, queries.queriesTwoSPARQL, resultsTwo);
		runQueries(conn, queries.queriesThreeSPARQL, resultsThree);

		closeEverything(conn);
		generateFile(resultsOne, resultsTwo, resultsThree,obdaFile);

	}

	/**
	 * @param resultsOne
	 * @param obdaFile 
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private void generateFile( String[] resultsOne, String[] resultsTwo, String[] resultsThree, String obdaFile) throws FileNotFoundException, UnsupportedEncodingException {
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
		 * Load the OBDA model from an externa
		 * l .obda file
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
			System.out.println();
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
			String[] queries, String[] results) throws OWLException {
		int j=0;
		while (j < queries.length){
			String sparqlQuery = queries[j];
			QuestOWLStatement st = conn.createStatement();
			try {

				// Warm ups
				for (int i=0; i<1; i++){
					QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
					int columnSize = rs.getColumnCount();
					while (rs.nextRow()) {
						for (int idx = 1; idx <= columnSize; idx++) {
							@SuppressWarnings("unused")
							OWLObject binding = rs.getOWLObject(idx);
							//System.out.print(binding.toString() + ", ");
						}
						//System.out.print("\n");
					}
				}
				
				
				long time = 0;
				int count = 0;
				
				for (int i=0; i<3; i++){
					long t1 = System.currentTimeMillis();
					QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
					int columnSize = rs.getColumnCount();
					count = 0;
					while (rs.nextRow()) {
						count ++;
						for (int idx = 1; idx <= columnSize; idx++) {
							@SuppressWarnings("unused")
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
				System.out.println((time/3) + "ms");

				results[j] = (time/3)+"" ;

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

		//QuestOWLExample_OntowisTests.Settings s = new Settings();
		Settings.mKeys = false;

		switch(args[0]){
		case "--help":{
			System.out.println(
					"Options:\n\n"
							+ "--mKeysON (default=off. SPECIFY AS FIRST OPTION!!)"
							+ "\n\n"
							+ "--POSTGRESInt; --POSTGRESIntView; --POSTGRESStr; --POSTGRESStrView"
							+ "--MYSQLInt; --MYSQLIntView; --MYSQLStr; --MYSQLStrView;"
							+ "--DB2; "
							+ "--MYSQL-VIEW; --POSTGRES-VIEW; --DB2-VIEW"
							+ "\n\n"
							+ "The concepts for which T-mappings should"
							+ "be disabled are defined the file tMappingsConf.conf");
			System.exit(0);
			break;
		}	
		case "--mKeysON":{
			Settings.mKeys = true;	
			defaults(args[1]);
			break;
		}
		default:
			defaults(args[0]);
			break;
		}			
		try {
			System.out.println(Settings.obdaFile);

			QuestOWLExample_OntowisTests example = new QuestOWLExample_OntowisTests(Settings.obdaFile, Settings.type, Settings.mKeys);
			example.runQuery(Settings.obdaFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void defaults(String string) {

		switch(string){
		case "--MYSQLInt":{
			Settings.obdaFile = ParamConst.MYSQLInt;
			Settings.type = DbType.MYSQL;
			break;
		}
		case "--MYSQLIntView":{
			Settings.obdaFile = ParamConst.MYSQLIntView;
			Settings.type = DbType.MYSQL;
			break;
		}
		case "--MYSQLStr":{
			Settings.obdaFile = ParamConst.MYSQLStr;
			Settings.type = DbType.MYSQL;
			break;
		}
		case "--MYSQLStrView":{
			Settings.obdaFile = ParamConst.MYSQLStrView;
			Settings.type = DbType.MYSQL;
			break;
		}
		case "--POSTGRESInt":{
			Settings.obdaFile = ParamConst.POSTGRESInt;
			Settings.type = DbType.POSTGRES;
			break;
		}

		case "--POSTGRESStr":{
			Settings.obdaFile = ParamConst.POSTGRESStr;
			Settings.type = DbType.POSTGRES;
			break;
		}
		case "--POSTGRESIntView":{
			Settings.obdaFile = ParamConst.POSTGRESIntView;
			Settings.type = DbType.POSTGRES;
			break;
		}
		case "--POSTGRESStrView":{
			Settings.obdaFile = ParamConst.POSTGRESStrView;
			Settings.type = DbType.POSTGRES;
			break;
		}
		case "--POSTGRESSmallInt":{
			Settings.obdaFile = ParamConst.POSTGRESInt;
			Settings.type = DbType.SMALL_POSTGRES;
			break;
		}

		case "--POSTGRESSmallStr":{
			Settings.obdaFile = ParamConst.POSTGRESStr;
			Settings.type = DbType.SMALL_POSTGRES;
			break;
		}
		case "--POSTGRESSmallIntView":{
			Settings.obdaFile = ParamConst.POSTGRESIntView;
			Settings.type = DbType.SMALL_POSTGRES;
			break;
		}
		case "--POSTGRESSmallStrView":{
			Settings.obdaFile = ParamConst.POSTGRESStrView;
			Settings.type = DbType.SMALL_POSTGRES;
			break;
		}
		default :{
			System.out.println(
					"Options:\n\n"
							+ "--mKeysON (default=off. SPECIFY AS FIRST OPTION!!)"
							+ "\n\n"
							+ "--POSTGRESInt; --POSTGRESIntView; --POSTGRESStr; --POSTGRESStrView"
							+ "--MYSQLInt; --MYSQLIntView; --MYSQLStr; --MYSQLStrView;"
							+ "--DB2; "
							+ "--MYSQL-VIEW; --POSTGRES-VIEW; --DB2-VIEW"
							+ "\n\n"
							+ "The concepts for which T-mappings should"
							+ "be disabled are defined the file tMappingsConf.conf");
			System.exit(0);
			break;
		}
		}
	}
	static class QueryTemplates{
		
		private final static String SPARQL_END = "}";
		
		static String oneSparqlJoinQuery(int nSqlJoins, int filter){
			String result = oneSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
			return result;
		}
		
		static String twoSparqlJoinQuery(int nSqlJoins, int filter){
			String result = twoSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
			return result;
		}
		
		static String threeSparqlJoinQuery(int nSqlJoins, int filter){
			String result = threeSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
			return result;
		}
		
		static private String filter(int filter){
			return "Filter( ?y < "+filter+" )";
		}
		
		static private String oneSparqlJoinTemplate(int nSqlJoins) {
			String templ = 
					"PREFIX :	<http://www.example.org/> "
							+ "SELECT ?x ?y  "
							+ "WHERE {"
							+ "?x a  :"+nSqlJoins+"Tab1 . "
							+ "?x :Tab"+(nSqlJoins+1)+"unique2Tab"+(nSqlJoins+1)+" ?y . ";
			return templ;
		}
		static private String twoSparqlJoinTemplate(int nSqlJoins){
			String templ =
					oneSparqlJoinTemplate(nSqlJoins) +
					"?x :hasString"+(nSqlJoins+1)+"j ?y1 . "; // Additional Sparql join 1
			return templ;
		}
		static private String threeSparqlJoinTemplate(int nSqlJoins){
			String templ = twoSparqlJoinTemplate(nSqlJoins) +
					"?x :hasString2"+(nSqlJoins+1)+"j ?y2 . "; // Additional Sparql Join 2
			return templ;
		}
	};
	
	class QueryFactory {
		
		String[] queriesOneSPARQL = new String[24];
		String[] queriesTwoSPARQL = new String[24];
		String[] queriesThreeSPARQL = new String[24];
		
		int[] filters = new int[6];
		
		QueryFactory(DbType type){
			fillFilters(type);
			fillQueryArrays();
		}
		
		private void fillQueryArrays (){
			// 1 SPARQL Join
			fillOneSparqlJoin();
			
			// 2 SPARQL Joins
			fillTwoSparqlJoins();
			
			// 3 SPARQL Joins
			fillThreeSparqlJoins();
		}
		
		
		private void fillFilters(DbType type) {
			switch(type){
			case MYSQL:	
				filters[0] = 1; // 0.001%
				filters[1] = 5; // 0.005%
				filters[2] = 10; // 0.01%
				filters[3] = 50; // 0.05%
				filters[4] = 100; // 0.1%
				filters[5] = 1000; //1%
				break;
			case POSTGRES:
				filters[0] = 100;   // 0.0001%
				filters[1] = 500;  // 0.0005%
				filters[2] = 1000;  // 0.001%
				filters[3] = 5000; // 0.005%
				filters[4] = 10000; // 0.01%
				filters[5] = 100000; // 0.1%
				break;
			case SMALL_POSTGRES:
				filters[0] = 1; // 0.001%
				filters[1] = 5; // 0.005%
				filters[2] = 10; // 0.01%
				filters[3] = 50; // 0.05%
				filters[4] = 100; // 0.1%
				filters[5] = 1000; //1%
				break;
			default:
				break;
			}
		}
		
		private void fillOneSparqlJoin(){
			for( int i = 0; i < 24; ++i ){
				if( i < 6 )	queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(1, filters[i % 6]); // 1 SQL Join
				else if ( i < 12 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(2, filters[i % 6]); // 2 SQL Joins
				else if ( i < 18 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(3, filters[i % 6]); // 3 SQL Joins
				else if ( i < 24 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(4, filters[i % 6]); // 4 SQL Joins
			}
		}
		
		private void fillTwoSparqlJoins(){
			for( int i = 0; i < 24; ++i ){
				if( i < 6 )	queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(1, filters[i % 6]); // 1 SQL Join
				else if ( i < 12 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(2, filters[i % 6]); // 2 SQL Joins
				else if ( i < 18 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(3, filters[i % 6]); // 3 SQL Joins
				else if ( i < 24 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(4, filters[i % 6]); // 4 SQL Joins
			}
		}
		
		private void fillThreeSparqlJoins(){
			for( int i = 0; i < 24; ++i ){
				if( i < 6 )	queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(1, filters[i % 6]); // 1 SQL Join
				else if ( i < 12 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(2, filters[i % 6]); // 2 SQL Joins
				else if ( i < 18 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(3, filters[i % 6]); // 3 SQL Joins
				else if ( i < 24 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(4, filters[i % 6]); // 4 SQL Joins
			}
		}	
	};
}
