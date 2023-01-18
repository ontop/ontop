package it.unibz.inf.ontop.docker.benchmark;

/*
 * #%L
 * ontop-quest-owlapi
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

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;

import it.unibz.inf.ontop.owlapi.OntopOWLEngine;

import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Ignore("used only for benchmark tests")
public class QuestOWLExample_OntowisTests {

    static class Constants {
        static final int NUM_FILTERS = 3;
        static final int NUM_SQL_JOINS = 4;

        static final int NUM_RUNS = 2;
        static final int NUM_WARM_UPS = 4;
    }

	interface ParamConst{
		// Postgres
		String POSTGRESInt = "src/test/resources/benchmark/example/postgres-NoViews-joins-int.obda";
		String POSTGRESStr = "src/test/resources/benchmark/example/postgres-NoViews-joins-str.obda";
		String POSTGRESIntView = "src/test/resources/benchmark/example/postgres-Views-joins-int.obda";
		String POSTGRESStrView = "src/test/resources/benchmark/example/postgres-Views-joins-str.obda";
		
		// MySQL
		String MYSQLInt = "src/test/resources/benchmark/example/mysql-NoViews-joins-int.obda";
		String MYSQLStr = "src/test/resources/benchmark/example/mysql-NoViews-joins-str.obda";
		String MYSQLIntView = "src/test/resources/benchmark/example/mysql-Views-joins-int.obda";
		String MYSQLStrView = "src/test/resources/benchmark/example/mysql-Views-joins-str.obda";
		
		// DB2
		String DB2Int = "src/test/resources/benchmark/example/db2-NoViews-joins-int.obda";
		String DB2IntView = "src/test/resources/benchmark/example/db2-Views-joins-int.obda";
	}
	
	enum DBType {
		MYSQL, POSTGRES, SMALL_POSTGRES, DB2
	}
	
	public static class Settings{
		static String obdaFile;
		static DBType type;
		static boolean mKeys = false;
	}

	// private static final String QuestOWLExample_OntowisTests = null;
	final String obdaFile;
	final DBType dbType;
	final boolean mKeys;

	final String owlfile = "src/test/resources/benchmark/example/ontowis-5joins-int.owl";
	final String usrConstrinFile = "src/test/resources/benchmark/example/funcCons.txt";


	// Internal Modifiable State
	OntopOWLEngine reasoner;

	public QuestOWLExample_OntowisTests(String obdaFile, DBType type, boolean mKeys){
		this.obdaFile = obdaFile;
		this.dbType = type;
		this.mKeys = mKeys;
	}

	// Exclude from T-Mappings
	//final String tMappingsConfFile = "src/test/resources/benchmark/example/tMappingsConf.conf";

	public void runQuery(String obdaFile) throws Exception {

		//	queries[30]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 .   } LIMIT 100000  ";

		OWLConnection conn =  createStuff(mKeys);

		// Results
//		String[] resultsOne = new String[31];
//		String[] resultsTwo = new String[31];
//		String[] resultsThree = new String[31];

		// Create Queries to be run
		QueryFactory queryFactory = new QueryFactory(dbType);

		// Run the tests on the queries


        List<List<Long>> resultsOne_list = new ArrayList<>(); // There is a list for each run.
        List<List<Long>> resultsTwo_list = new ArrayList<>();
        List<List<Long>> resultsThree_list = new ArrayList<>();


        runQueries(conn, queryFactory.warmUpQueries);


        for(int i = 0; i < Constants.NUM_RUNS; i++) {
            List<Long> resultsOne = runQueries(conn, queryFactory.queriesOneSPARQL);
            resultsOne_list.add(resultsOne);

            List<Long> resultsTwo = runQueries(conn, queryFactory.queriesTwoSPARQL);
            resultsTwo_list.add(resultsTwo);

            List<Long> resultsThree = runQueries(conn, queryFactory.queriesThreeSPARQL);
            resultsThree_list.add(resultsThree);
        }
		closeEverything(conn);

        List<Long> avg_resultsOne = average(resultsOne_list);
        List<Long> avg_resultsTwo = average(resultsTwo_list);
        List<Long> avg_resultsThree = average(resultsThree_list);

		generateFile(avg_resultsOne, avg_resultsTwo, avg_resultsThree);

	}

    public List<Long> average(List<List<Long>> lists ){

        int numList = lists.size();

        int size = lists.get(0).size();

        List<Long> results = new ArrayList<>();

        for(int i = 0 ; i < size; i++){
            long sum = 0;
            for (List<Long> list : lists) {
                sum += list.get(i);
            }
            results.add(sum/numList);
        }
        return results;
    }

	/**
	 * @param resultsOne
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private void generateFile( List<Long> resultsOne, List<Long> resultsTwo, List<Long> resultsThree) throws IOException {
		/*
		 * Generate File !
		 */
		PrintWriter writer = new PrintWriter("src/test/resources/benchmark/example/table.txt", StandardCharsets.UTF_8);
		PrintWriter writerG = new PrintWriter("src/test/resources/benchmark/example/graph.txt", StandardCharsets.UTF_8);

		int sizeQueriesArray = Constants.NUM_FILTERS * Constants.NUM_SQL_JOINS;
		int nF = Constants.NUM_FILTERS;
		
		int j=0;
		
		while (j<sizeQueriesArray){
			writer.println(resultsOne.get(j) + " & " + resultsTwo.get(j) + " & " + resultsThree.get(j)); // table

			if (j< Constants.NUM_FILTERS){
                String gline = "(1," + resultsOne.get(j) + ")" + "(2," + resultsTwo.get(j) + ")"
                        + "(3," + resultsThree.get(j) + ")" + "(4," + resultsOne.get(j + nF*1) + ")"
                        + "(5," + resultsTwo.get(j + nF*1) + ")" + "(6," + resultsThree.get(j + nF*1) + ")"
                        + "(7," + resultsOne.get(j + nF*2) + ")" + "(8," + resultsTwo.get(j + nF*2) + ")"
                        + "(9," + resultsThree.get(j + nF*2) + ")" + "(10," + resultsOne.get(j + nF*3) + ")"
                        + "(11," + resultsTwo.get(j + nF*3) + ")" + "(12," + resultsThree.get(j + nF*3) + ")";
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
	private void closeEverything(OWLConnection conn) throws Exception {
		/*
		 * Close connection and resources
		 */

		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
		this.reasoner.close();
	}
	
	private OWLConnection createStuff(boolean manualKeys) {

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		//		TEST preference.setProperty(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
//		factory.setOBDAController(obdaModel);
//		factory.setPreferenceHolder(preference);

		/*
		 * USR CONSTRAINTS !!!!
		 */
		OntopSQLOWLAPIConfiguration config;
		if (manualKeys){
			System.out.println();
			//factory.setImplicitDBConstraints(constr);
			config = OntopSQLOWLAPIConfiguration.defaultBuilder()
					.ontologyFile(owlfile)
					.basicImplicitConstraintFile(usrConstrinFile)
					.nativeOntopMappingFile(new File(obdaFile))
					.build();
		} else {
			config = OntopSQLOWLAPIConfiguration.defaultBuilder()
					.ontologyFile(owlfile)
					.nativeOntopMappingFile(new File(obdaFile))
					.build();
		}
		/*
		 * T-Mappings Handling!!
		 */
		//TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
		//factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());

		OntopOWLEngine reasoner = new SimpleOntopOWLEngine(config);

		this.reasoner = reasoner;
		/*
		 * Prepare the data connection for querying.
		 */
		return reasoner.getConnection();
	}


	private List<Long> runQueries(OWLConnection conn, String[] queries) throws OWLException {
		
		//int nWarmUps = Constants.NUM_WARM_UPS;
		//int nRuns = Constants.NUM_RUNS;

        List<Long> results = new ArrayList<>();
		
		int j=0;
		while (j < queries.length){
			String sparqlQuery = queries[j];
			OWLStatement st = conn.createStatement();
			try {

				//for (int i=0; i<nRuns; ++i){
					long t1 = System.currentTimeMillis();
					TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
					int columnSize = rs.getColumnCount();
					int count = 0;
					while (rs.hasNext()) {
						count ++;
						//System.out.print("\n");
					}
					long t2 = System.currentTimeMillis();
					//time = time + (t2-t1);
					long time =  (t2-t1);
					System.out.println("partial time:" + time);
					rs.close();
				//}

				/*
				 * Print the query summary
				 */
				OntopOWLStatement qst = (OntopOWLStatement) st;

				System.out.println();
				System.out.println("The input SPARQL query:");
				System.out.println("=======================");
				System.out.println(sparqlQuery);
				System.out.println();

				System.out.println("The output SQL query:");
				System.out.println("=====================");
				System.out.println(qst.getExecutableQuery(sparqlQuery));

				System.out.println("Query Execution Time:");
				System.out.println("=====================");
				//System.out.println((time/nRuns) + "ms");

				//results[j] = (time/nRuns)+"" ;
                results.add(j, time);

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

        return results;
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
							+ "--DB2Int; --DB2IntView;"
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
			Settings.type = DBType.MYSQL;
			break;
		}
		case "--MYSQLIntView":{
			Settings.obdaFile = ParamConst.MYSQLIntView;
			Settings.type = DBType.MYSQL;
			break;
		}
		case "--MYSQLStr":{
			Settings.obdaFile = ParamConst.MYSQLStr;
			Settings.type = DBType.MYSQL;
			break;
		}
		case "--MYSQLStrView":{
			Settings.obdaFile = ParamConst.MYSQLStrView;
			Settings.type = DBType.MYSQL;
			break;
		}
		case "--POSTGRESInt":{
			Settings.obdaFile = ParamConst.POSTGRESInt;
			Settings.type = DBType.POSTGRES;
			break;
		}

		case "--POSTGRESStr":{
			Settings.obdaFile = ParamConst.POSTGRESStr;
			Settings.type = DBType.POSTGRES;
			break;
		}
		case "--POSTGRESIntView":{
			Settings.obdaFile = ParamConst.POSTGRESIntView;
			Settings.type = DBType.POSTGRES;
			break;
		}
		case "--POSTGRESStrView":{
			Settings.obdaFile = ParamConst.POSTGRESStrView;
			Settings.type = DBType.POSTGRES;
			break;
		}
		case "--POSTGRESSmallInt":{
			Settings.obdaFile = ParamConst.POSTGRESInt;
			Settings.type = DBType.SMALL_POSTGRES;
			break;
		}

		case "--POSTGRESSmallStr":{
			Settings.obdaFile = ParamConst.POSTGRESStr;
			Settings.type = DBType.SMALL_POSTGRES;
			break;
		}
		case "--POSTGRESSmallIntView":{
			Settings.obdaFile = ParamConst.POSTGRESIntView;
			Settings.type = DBType.SMALL_POSTGRES;
			break;
		}
		case "--POSTGRESSmallStrView":{
			Settings.obdaFile = ParamConst.POSTGRESStrView;
			Settings.type = DBType.SMALL_POSTGRES;
			break;
		}
		case "--DB2Int":{
			Settings.obdaFile = ParamConst.DB2Int;
			Settings.type = DBType.DB2;
			break;
		}
		case "--DB2IntView":{
			Settings.obdaFile = ParamConst.DB2IntView;
			Settings.type = DBType.DB2;
			break;
		}
		default :{
			System.out.println(
					"Options:\n\n"
							+ "--mKeysON (default=off. SPECIFY AS FIRST OPTION!!)"
							+ "\n\n"
							+ "--POSTGRESInt; --POSTGRESIntView; --POSTGRESStr; --POSTGRESStrView"
							+ "--MYSQLInt; --MYSQLIntView; --MYSQLStr; --MYSQLStrView;"
							+ "--DB2Int; --DB2IntView;"
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
			return oneSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
		}
		
		static String twoSparqlJoinQuery(int nSqlJoins, int filter){
			return twoSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
		}
		
		static String threeSparqlJoinQuery(int nSqlJoins, int filter){
			return threeSparqlJoinTemplate(nSqlJoins) + filter(filter) + SPARQL_END;
		}
		
		static private String filter(int filter){
			return "Filter( ?y < "+filter+" )";
		}
		
		static private String oneSparqlJoinTemplate(int nSqlJoins) {
			return "PREFIX :	<http://www.example.org/> "
					+ "SELECT ?x ?y  "
					+ "WHERE {"
					+ "?x a  :"+nSqlJoins+"Tab1 . "
					+ "?x :Tab"+(nSqlJoins+1)+"unique2Tab"+(nSqlJoins+1)+" ?y . ";
		}
		static private String twoSparqlJoinTemplate(int nSqlJoins){
			return oneSparqlJoinTemplate(nSqlJoins) +
			"?x :hasString"+(nSqlJoins+1)+"j ?y1 . ";
		}
		static private String threeSparqlJoinTemplate(int nSqlJoins){
			return twoSparqlJoinTemplate(nSqlJoins) +
					"?x :hasString2"+(nSqlJoins+1)+"j ?y2 . ";
		}
	}
	
	static class QueryFactory {
		
		private final static int sizeQueriesArray = Constants.NUM_FILTERS * Constants.NUM_SQL_JOINS;

		final String[] queriesOneSPARQL = new String[sizeQueriesArray];
		final String[] queriesTwoSPARQL = new String[sizeQueriesArray];
		final String[] queriesThreeSPARQL = new String[sizeQueriesArray];

		final String[] warmUpQueries = new String[Constants.NUM_WARM_UPS];

		final int[] filters = new int[Constants.NUM_FILTERS];
		
		QueryFactory(DBType type){
			fillFilters(type);
			fillQueryArrays();
		}
		
		private void fillQueryArrays (){

            fillWarmUpQueries();

			// 1 SPARQL Join
			fillOneSparqlJoin();
			
			// 2 SPARQL Joins
			fillTwoSparqlJoins();
			
			// 3 SPARQL Joins
			fillThreeSparqlJoins();
		}

        private void fillWarmUpQueries() {
        	for(int i = 0; i < Constants.NUM_WARM_UPS; i++){
        		int limit = (i * 1000) + 1;
                warmUpQueries[i] = String.format("SELECT ?x WHERE { " +
                        "?x a <http://www.example.org/%dTab1> } LIMIT "+limit, i);
            }
        }


        private void fillFilters(DBType type) {
			switch(type){
			case MYSQL:	
				filters[0] = 1; // 0.001
				filters[1] = 100; // 0.1%
				filters[2] = 1000; //1%
				break;
			case POSTGRES:
				filters[0] = 100;   // 0.0001%
				filters[1] = 10000;  // 0.01%
				filters[2] = 100000; // 0.1%
				break;
			case SMALL_POSTGRES:
				filters[0] = 1; // 0.001%
				filters[1] = 100; // 0.005%
				filters[2] = 1000; // 0.01%
				break;
			case DB2:
				filters[0] = 100;
				filters[1] = 10000;
				filters[2] = 100000;
				break;
			}
		}
		
		private void fillOneSparqlJoin(){
			for( int i = 0; i < sizeQueriesArray; ++i ){
				if( i < Constants.NUM_FILTERS)	queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(1, filters[i % Constants.NUM_FILTERS]); // 1 SQL Join
				else if ( i < Constants.NUM_FILTERS * 2 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(2, filters[i % Constants.NUM_FILTERS]); // 2 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 3 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(3, filters[i % Constants.NUM_FILTERS]); // 3 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 4 ) queriesOneSPARQL[i] = QueryTemplates.oneSparqlJoinQuery(4, filters[i % Constants.NUM_FILTERS]); // 4 SQL Joins
			}
		}
		
		private void fillTwoSparqlJoins(){
			for( int i = 0; i < sizeQueriesArray; ++i ){
				if( i < Constants.NUM_FILTERS)	queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(1, filters[i % Constants.NUM_FILTERS]); // 1 SQL Join
				else if ( i < Constants.NUM_FILTERS * 2 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(2, filters[i % Constants.NUM_FILTERS]); // 2 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 3 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(3, filters[i % Constants.NUM_FILTERS]); // 3 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 4 ) queriesTwoSPARQL[i] = QueryTemplates.twoSparqlJoinQuery(4, filters[i % Constants.NUM_FILTERS]); // 4 SQL Joins
			}
		}
		
		private void fillThreeSparqlJoins(){
			for( int i = 0; i < sizeQueriesArray; ++i ){
				if( i < Constants.NUM_FILTERS)	queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(1, filters[i % Constants.NUM_FILTERS]); // 1 SQL Join
				else if ( i < Constants.NUM_FILTERS * 2 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(2, filters[i % Constants.NUM_FILTERS]); // 2 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 3 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(3, filters[i % Constants.NUM_FILTERS]); // 3 SQL Joins
				else if ( i < Constants.NUM_FILTERS * 4 ) queriesThreeSPARQL[i] = QueryTemplates.threeSparqlJoinQuery(4, filters[i % Constants.NUM_FILTERS]); // 4 SQL Joins
			}
		}	
	}
}
