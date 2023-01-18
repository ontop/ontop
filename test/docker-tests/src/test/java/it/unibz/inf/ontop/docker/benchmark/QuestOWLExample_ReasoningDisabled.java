package it.unibz.inf.ontop.docker.benchmark;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;

import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Ignore("used only for benchmark tests")
public class QuestOWLExample_ReasoningDisabled {

    static class Constants {
        static final int NUM_FILTERS = 3;
        static final int NUM_SQL_JOINS = 4;

        static final int NUM_RUNS = 2;
        static final int NUM_WARM_UPS = 4;
    }

    interface ParamConst{
        String MYSQL_OBDA_FILE  = "/benchmark/example/disableReasoning/mysql_obdalin3.obda";
        String MYSQL_PROPERTY_FILE  = "/benchmark/example/disableReasoning/mysql_obdalin3.properties";

        String POSTGRES_OBDA_FILE = "/benchmark/example/disableReasoning/pgsql_obdalin3.obda";
        String POSTGRES_PROPERTY_FILE = "/benchmark/example/disableReasoning/pgsql_obdalin3.properties";

        String DB2_OBDA_FILE = "/benchmark/example/disableReasoning/db2_obdalin3.obda";
        String DB2_PROPERTY_FILE = "/benchmark/example/disableReasoning/db2_obdalin3.properties";

        String MYSQL_SMALL_OBDA_FILE  = "/benchmark/example/disableReasoning/mysql_vulcan.obda";
        String MYSQL_SMALL_PROPERTY_FILE  = "/benchmark/example/disableReasoning/mysql_vulcan.properties";

        String POSTGRES_SMALL_OBDA_FILE = "/benchmark/example/disableReasoning/pgsql_obdalin3.obda";
        String POSTGRES_SMALL_PROPERTY_FILE = "/benchmark/example/disableReasoning/pgsql_obdalin3.properties";

        //String DB2_SMALL_OBDA_FILE = "src/test/resources/benchmark/example/disableReasoning/ontowis-hierarchy-db2.obda";
        //String DB2_SMALL_PROPERTY_FILE = "src/test/resources/benchmark/example/disableReasoning/ontowis-hierarchy-db2.properties";


        String[] tMappingConfFiles = {
                "/benchmark/example/disableReasoning/ontowis-hierarchy-tm_1.conf",
                "/benchmark/example/disableReasoning/ontowis-hierarchy-tm_2.conf",
                "/benchmark/example/disableReasoning/ontowis-hierarchy-tm_3.conf",
                "/benchmark/example/disableReasoning/ontowis-hierarchy-tm_4.conf"
        };
    }

    enum DbType {
        MYSQL, POSTGRES, SMALL_POSTGRES, DB2, SMALL_MYSQL
    }

    public static class Settings {
        static String obdaFile;
        static DbType dbType;
        static String propertyFile;
        static String tMappingConfFile;
        public static String tableFileName;
    }

    static class QueryFactory {
        static private int[] getFilters(DbType type) {
            int[] filters = new int[Constants.NUM_FILTERS];

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
                case SMALL_MYSQL:
                    filters[0] = 1; // 0.001%
                    filters[1] = 100; // 0.005%
                    filters[2] = 1000; // 0.01%
                    break;
            }
            return filters;
        }

        static String prefix = "PREFIX : <http://www.example.org/> \n";


        static public List<String> createSPARQLs_three_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

//            for(int i1 = 1; i1 <= 4; i1++) {
//                for (int i2 = 1; i2 <= 4; i2++) {
//                    for (int i3 = 1; i3 <= 4; i3++) {
            int[][] indexes = {{1, 2, 3}, {1, 3, 4}, {2, 3, 4}, {4, 4, 4}};
            for(int[] index : indexes){
                int i1 = index[0];
                int i2 = index[1];
                int i3 = index[2];
                for (int filter : getFilters(dbType)) {
                    String sparql = String.format(
                            "PREFIX : <http://www.example.org/> " +
                                    "SELECT DISTINCT ?x ?y ?z ?w " +
                                    " WHERE {" +
                                    "?x a :A%d . ?x :R ?y . " +
                                    "?y a :A%d . ?y :R ?z . " +
                                    "?z a :A%d . ?z :S ?w . " +
                                    "FILTER (?w < %d)  }",
                            i1, i2, i3, filter);
                    sparqls.add(sparql);
                }
//                        }
//                    }
//                }
            }
            return sparqls;
        }

        static public List<String> createSPARQLs_three_concepts_opt(DbType dbType){
            List<String> sparqls = new ArrayList<>();

//            for(int i1 = 1; i1 <= 4; i1++) {
//                for (int i2 = 1; i2 <= 4; i2++) {
//                    for (int i3 = 1; i3 <= 4; i3++) {
            int[][] indexes = {{1, 2, 3}, {1, 3, 4}, {2, 3, 4}, {4, 4, 4}};
            for(int[] index : indexes){
                int i1 = index[0];
                int i2 = index[1];
                int i3 = index[2];
                for (int filter : getFilters(dbType)) {
                    String sparql = String.format(
                            "PREFIX : <http://www.example.org/> " +
                                    "SELECT DISTINCT ?x ?y ?z ?w " +
                                    " WHERE {" +
                                    "?x a :A%d . OPTIONAL { ?x :R ?y . " +
                                    "?y a :A%d . OPTIONAL { ?y :R ?z . " +
                                    "?z a :A%d . OPTIONAL { ?z :S ?w . " +
                                    " } } } FILTER (?w < %d) }",
                            i1, i2, i3, filter);
                    sparqls.add(sparql);
                }
//                        }
//                    }
//                }
            }
            return sparqls;
        }
        
        static public List<String> createSPARQLs_two_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            //for(int i1 = 1; i1 <= 4; i1++) {
            //    for (int i2 = 1; i2 <= 4; i2++) {


            int[][] indexes = {{1, 2}, {2, 3}, {3, 4}, {4, 4}};


            for(int[] index : indexes){

                int i1 = index[0];
                int i2 = index[1];
                        for (int filter : getFilters(dbType)) {
                            String sparql = String.format(
                                    "PREFIX : <http://www.example.org/> " +
                                            "SELECT DISTINCT ?x ?y ?z   " +
                                            " WHERE {" +
                                            "?x a :A%d. ?x :R ?y . " +
                                            "?y a :A%d. ?y :S ?z   .  " +
                                            "FILTER (?z < %d) }",
                                    i1, i2, filter);
                            sparqls.add(sparql);
                        }
            }
            //    }
            //}
            return sparqls;
        }
        
        static public List<String> createSPARQLs_two_concepts_opt(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            //for(int i1 = 1; i1 <= 4; i1++) {
            //    for (int i2 = 1; i2 <= 4; i2++) {


            int[][] indexes = {{1, 2}, {2, 3}, {3, 4}, {4, 4}};


            for(int[] index : indexes){

                int i1 = index[0];
                int i2 = index[1];
                        for (int filter : getFilters(dbType)) {
                            String sparql = String.format(
                                    "PREFIX : <http://www.example.org/> " +
                                            "SELECT DISTINCT ?x ?y ?z   " +
                                            " WHERE {" +
                                            "?x a :A%d. "
                                            + "OPTIONAL { ?x :R ?y . " +
                                            "?y a :A%d. OPTIONAL { ?y :S ?z   .  " +
                                            " } } FILTER (?z < %d) }",
                                    i1, i2, filter);
                            sparqls.add(sparql);
                        }
            }
            //    }
            //}
            return sparqls;
        }

        static public List<String> createSPARQLs_one_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            for(int i1 = 1; i1 <= 4; i1++) {
                    for (int filter : getFilters(dbType)) {
                        String sparql = String.format(
                                "PREFIX : <http://www.example.org/> " +
                                        " SELECT  DISTINCT ?x  ?y     " +
                                        " WHERE {" +
                                        "?x a :A%d. ?x :S ?y ." +
                                        "FILTER (?y < %d) }",
                                i1, filter);
                        sparqls.add(sparql);


                }
            }
            return sparqls;
        }
        
        static public List<String> createSPARQLs_one_concepts_opt(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            for(int i1 = 1; i1 <= 4; i1++) {
                    for (int filter : getFilters(dbType)) {
                        String sparql = String.format(
                                "PREFIX : <http://www.example.org/> " +
                                        " SELECT  DISTINCT ?x  ?y     " +
                                        " WHERE {" +
                                        "?x a :A%d. OPTIONAL{ ?x :S ?y } FILTER (?y < %d) } " 
                                        ,
                                i1, filter);
                        sparqls.add(sparql);


                }
            }
            return sparqls;
        }

        static public  List<String> getWarmUpQueries() {
            List<String> warmUpQueries = new ArrayList<>();
            for(int i = 0; i < Constants.NUM_WARM_UPS; i++){
                int limit = (i * 1000) + 1;
                warmUpQueries.add(String.format("SELECT ?x WHERE { " +
                        "?x a <http://www.example.org/%dTab1> } LIMIT " + limit, i));
            }
            return warmUpQueries;
        }
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
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    private void generateFile(List<Long> resultsOne, List<Long> resultsTwo, List<Long> resultsThree, String tableFileName) throws IOException {
        /*
		 * Generate File !
		 */
        //String tableFileName = "table.txt";
        PrintWriter writer = new PrintWriter("src/test/resources/benchmark/example/disableReasoning/" + tableFileName, StandardCharsets.UTF_8);
        PrintWriter writerG = new PrintWriter("src/test/resources/benchmark/example/disableReasoning/graph.txt", StandardCharsets.UTF_8);

        //writer.write(String.format("%s\n", "# group 1"));
        int j = 0;
        int g = 0;
        for(Long result: resultsOne){
            writer.write(String.format("%d, %d, %d, %d, %d\n", g, j, j / Constants.NUM_FILTERS, j % Constants.NUM_FILTERS, result));
            j++;
        }

        //writer.write(String.format("%s\n", "# group 2"));
        j = 0;
        g++;
        for(Long result: resultsTwo){
            writer.write(String.format("%d, %d, %d, %d, %d\n", g, j, j / Constants.NUM_FILTERS, j % Constants.NUM_FILTERS, result));
            j++;
        }

        //writer.write(String.format("%s\n", "# group 3"));
        j = 0;
        g++;
        for(Long result: resultsThree){
            writer.write(String.format("%d, %d, %d, %d, %d\n", g, j, j / Constants.NUM_FILTERS, j % Constants.NUM_FILTERS, result));
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

    private OntopOWLConnection createStuff() throws IOException {

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */

//		TEST preference.setCurrentValueOf(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings

        TMappingExclusionConfig tMapConfig = TMappingExclusionConfig.parseFile(getClass().getResource(Settings.tMappingConfFile).getPath());

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(new File(getClass().getResource(Settings.obdaFile).getPath()))
                .ontologyFile(getClass().getResource(owlfile).getPath())
                .propertyFile(getClass().getResource(Settings.propertyFile).getPath())
                .tMappingExclusionConfig(tMapConfig)
                //.enableIRISafeEncoding(false)
                .build();
        this.reasoner = new SimpleOntopOWLEngine(config);
		/*
		 * Prepare the data connection for querying.
		 */
        return reasoner.getConnection();
    }

    private OntopOWLEngine reasoner;


    public QuestOWLExample_ReasoningDisabled(DbType dbType, String obdaFile, String tMappingsConfFile, String propertyFile){
        Settings.obdaFile = obdaFile;
        Settings.dbType = dbType;
        Settings.tMappingConfFile = tMappingsConfFile;
        Settings.propertyFile = propertyFile;
    }

    /*
     * Use the sample database using H2 from
     * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
     *
     * Please use the pre-bundled H2 server from the above link
     *
     */
    final String owlfile = "/benchmark/example/disableReasoning/ontowis-hierarchy.owl";
    //final String obdaFile = "src/test/resources/benchmark/example/ontowis-5joins-int-view.obda";
    //final String obdaFile;// = "src/test/resources/benchmark/example/ontowis-5joins-int-view.obda";

    //private final DbType dbType;

    // Exclude from T-Mappings
    String tMappingsConfFile;

    public void runQuery() throws Exception {

        //	queries[30]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 .   } LIMIT 100000  ";

        // QuestOWLConnection conn =  createStuff();

        // Results
        List<List<Long>> resultsOne_list = new ArrayList<>();
        List<List<Long>> resultsTwo_list = new ArrayList<>();
        List<List<Long>> resultsThree_list = new ArrayList<>();



        // for testing TIMEOUT ONLY

//        int length = QueryFactory.createSPARQLs_three_concepts(Settings.dbType).size();
//        runQueries(conn, Lists.newArrayList(QueryFactory.createSPARQLs_three_concepts(Settings.dbType).get(length-1)));
//        runQueries(conn, Lists.newArrayList(QueryFactory.createSPARQLs_three_concepts(Settings.dbType).get(length-1)));

        // System.exit(0);

        runQueries(QueryFactory.getWarmUpQueries());

        for(int i = 0; i < Constants.NUM_RUNS; i++) {
            List<Long> resultsOne = runQueries(//conn,
                    QueryFactory.createSPARQLs_one_concepts_opt(Settings.dbType));
            resultsOne_list.add(resultsOne);

            List<Long> resultsTwo = runQueries(//conn,
                    QueryFactory.createSPARQLs_two_concepts_opt(Settings.dbType));
            resultsTwo_list.add(resultsTwo);

            List<Long> resultsThree = runQueries(//conn,
                    QueryFactory.createSPARQLs_three_concepts_opt(Settings.dbType));
            resultsThree_list.add(resultsThree);
        }
        //closeEverything(conn);

        List<Long> avg_resultsOne = average(resultsOne_list);
        List<Long> avg_resultsTwo = average(resultsTwo_list);
        List<Long> avg_resultsThree = average(resultsThree_list);

        generateFile(avg_resultsOne, avg_resultsTwo, avg_resultsThree, Settings.tableFileName);

    }

    private List<Long> runQueries(//QuestOWLConnection conn,
                                  List<String> queries) throws Exception {

        //int nWarmUps = Constants.NUM_WARM_UPS;
        //int nRuns = Constants.NUM_RUNS;

        List<Long> results = new ArrayList<>();

        for(String sparqlQuery:queries){
            //String sparqlQuery = queries[j];

            OntopOWLConnection conn;
            OntopOWLStatement st = null;
            try {

                // Warm ups
//				for (int i=0; i<nWarmUps; ++i){
//					QuestOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
//					int columnSize = rs.getColumnCount();
//					while (rs.hasNext()) {
//						for (int idx = 1; idx <= columnSize; idx++) {
//							@SuppressWarnings("unused")
//							OWLObject binding = rs.getOWLObject(idx);
//							//System.out.print(binding.toString() + ", ");
//						}
//						//System.out.print("\n");
//					}
//				}
//

                conn = createStuff();

                st = conn.createStatement();

                //for (int i=0; i<nRuns; ++i){
                long t1 = System.currentTimeMillis();
                TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
                int count = 0;
                while (rs.hasNext()) {
                    count ++;
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
                IQ executableQuery = st.getExecutableQuery(sparqlQuery);

                System.out.println();
                System.out.println("The input SPARQL query:");
                System.out.println("=======================");
                System.out.println(sparqlQuery);
                System.out.println();

                System.out.println("The output SQL query:");
                System.out.println("=====================");
                System.out.println(executableQuery);

                System.out.println("Query Execution Time:");
                System.out.println("=====================");
                System.out.println(time + "ms");

                results.add(time);

                System.out.println("The number of results:");
                System.out.println("=====================");
                System.out.println(count);

                closeEverything(conn);

            } finally {
                if (st != null && !st.isClosed()) {
                    st.close();
                }
            }

        }

        return results;
    }


    /**
     * Main client program
     */
    public static void main(String[] args) {

        String arg;

        if(args.length > 0){
            arg = args[0];
            Settings.tMappingConfFile = ParamConst.tMappingConfFiles[Integer.parseInt(args[1])];
            Settings.tableFileName = String.format("table-%s.txt", args[1]);
        } else {
            arg = "--MYSQL-SMALL";
            Settings.tMappingConfFile = ParamConst.tMappingConfFiles[0];
            Settings.tableFileName = "table-0.txt";
        }

        defaults(arg);

        try {
            QuestOWLExample_ReasoningDisabled example = new QuestOWLExample_ReasoningDisabled(
                    Settings.dbType, Settings.obdaFile, Settings.tMappingConfFile, Settings.propertyFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void defaults(String string) {

        switch(string){
            case "--MYSQL-SMALL":{
                Settings.obdaFile = ParamConst.MYSQL_SMALL_OBDA_FILE;
                Settings.dbType = DbType.MYSQL;
                Settings.propertyFile = ParamConst.MYSQL_SMALL_PROPERTY_FILE;
                break;
            }
            case "--MYSQL":{
                Settings.obdaFile = ParamConst.MYSQL_OBDA_FILE;
                Settings.dbType = DbType.MYSQL;
                Settings.propertyFile = ParamConst.MYSQL_PROPERTY_FILE;
                break;
            }
            case "--POSTGRES-SMALL":{
                Settings.obdaFile = ParamConst.POSTGRES_SMALL_OBDA_FILE;
                Settings.dbType = DbType.SMALL_POSTGRES;
                Settings.propertyFile = ParamConst.POSTGRES_SMALL_PROPERTY_FILE;
                break;
            }
            case "--POSTGRES":{
                Settings.obdaFile = ParamConst.POSTGRES_OBDA_FILE;
                Settings.dbType = DbType.POSTGRES;
                Settings.propertyFile = ParamConst.POSTGRES_PROPERTY_FILE;
                break;
            }
            case "--DB2":{
                Settings.obdaFile = ParamConst.DB2_OBDA_FILE;
                Settings.dbType = DbType.DB2;
                Settings.propertyFile = ParamConst.DB2_PROPERTY_FILE;
                break;
            }

            default :{
                System.out.println(
                        "Options:\n\n"
                                + "\n\n"
                                + "--MYSQL-SMALL; "
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

}
