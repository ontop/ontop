package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Sarah on 09/04/15.
 */
public class SameAsOntowisTest {

    class Constants {
        static final int NUM_FILTERS = 3;

//        static final int NUM_RUNS = 1;
        static final int NUM_WARM_UPS = 0;
    }

    interface ParamConst{
        // Postgres
        public static final String POSTGRES2DSten = "resources/sameAs/ontowis/ontowisOBDA2-ten.obda";
        public static final String POSTGRES2DSthirty = "resources/sameAs/ontowis/ontowisOBDA2-thirty.obda";
        public static final String POSTGRES2DSsixty = "resources/sameAs/ontowis/ontowisOBDA2-sixty.obda";
        public static final String POSTGRES3DSten = "resources/sameAs/ontowis/ontowisOBDA3-ten.obda";
        public static final String POSTGRES3DSthirty = "resources/sameAs/ontowis/ontowisOBDA3-thirty.obda";
        public static final String POSTGRES3DSsixty = "resources/sameAs/ontowis/ontowisOBDA3-sixty.obda";


    }

    public static class Settings{
        static String obdaFile;
        static String resultFileName;
        static int runs;
        static  int NUM_TABLES ;
        static  int NUM_OBJECTS ;
        static  int NUM_DATA;


    }

    final String obdaFile;


    final String owlfile = "resources/sameAs/ontowis/ontowis.owl";


    // Internal Modifiable State
    QuestOWL reasoner ;

    public SameAsOntowisTest(String obdaFile){
        this.obdaFile = obdaFile;
    }


    public void runQuery() throws Exception {

        long t1 = System.currentTimeMillis();
        QuestOWLConnection conn =  createStuff();
        long t2 = System.currentTimeMillis();

        long time =  (t2-t1);
        System.out.println("offline time: " + time);

        // Create Queries to be run
        QueryFactory queryFactory = new QueryFactory();

        // Run the tests on the queries


        List<List<Long>> selectivityLow_list = new ArrayList<>(); // There is a list for each run.
        List<List<Long>> selectivityMiddle_list = new ArrayList<>();
        List<List<Long>> selectivityHigh_list = new ArrayList<>();


        runQueries(conn, queryFactory.warmUpQueries);


        for(int i = 0; i < Settings.runs; i++) {
            List<Long> selectivityLow = runQueries(conn, queryFactory.filter0SPARQL);
            selectivityLow_list.add(selectivityLow);

        }

        for(int i = 0; i < Settings.runs; i++) {
            List<Long> selectivityMiddle = runQueries(conn, queryFactory.filter1SPARQL);
            selectivityMiddle_list.add(selectivityMiddle);

        }

        for(int i = 0; i < Settings.runs; i++) {
            List<Long> selectivityHigh = runQueries(conn, queryFactory.filter2SPARQL);
            selectivityHigh_list.add(selectivityHigh);

        }

//
        closeEverything(conn);

        List<Long> avg_selectivityLow = average(selectivityLow_list);

        List<Long> avg_selectivityMiddle = average(selectivityMiddle_list);

        List<Long> avg_selectivityHigh = average(selectivityHigh_list);

        generateFile(avg_selectivityLow, avg_selectivityMiddle, avg_selectivityHigh, queryFactory.filter0SPARQL, time);

//        runQueries(conn, queryFactory.queriesTest);

    }

//    public void runEachFilterQuery() throws Exception {
//
//        QuestOWLConnection conn =  createStuff();
//
//
//        // Create Queries to be run
//        QueryFactory queryFactory = new QueryFactory();
//
//        // Run the tests on the queries
//
//        List<List<Long>> selectivity_list = new ArrayList<>(Constants.NUM_RUNS);
////
//        runQueries(conn, queryFactory.warmUpQueries);
//
//        for(int i = 0; i < Constants.NUM_RUNS; i++) {
//            List<Long> selectivity = runQueries(conn, queryFactory.filterSPARQL);
//            selectivity_list.add(selectivity);
//        }
//
////
//        closeEverything(conn);
//
//        List<Long> avg_selectivity = average(selectivity_list);
//        generateFile(avg_selectivity, queryFactory.filterSPARQL);
////
//
//    }

    public List<Long> average(List<List<Long>> lists ){

        int numList = lists.size();

        int size = lists.get(0).size();

        List<Long> results = new ArrayList<>(numList);

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
     * @param resultsLow

     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    private void generateFile( List<Long> resultsLow, List<Long> resultsMiddle, List<Long> resultsHigh, List<String> queries, long time) throws FileNotFoundException, UnsupportedEncodingException {
		/*
		 * Generate File !
		 */
        PrintWriter writer = new PrintWriter("resources/results/"+Settings.resultFileName+"table.txt", "UTF-8");
        PrintWriter writerQ = new PrintWriter("resources/results/"+Settings.resultFileName+"queries.txt", "UTF-8");

        writer.println("offline time: " + time);

        int j=0;

        while (j<resultsLow.size()){
            writer.println(j + " & "+ resultsLow.get(j) + " & " + resultsMiddle.get(j) + " & " + resultsHigh.get(j)); // table
            writerQ.println(j + " & " +queries.get(j));

            j++;
        }
        writer.close();
        writerQ.close();

    }

//    private void generateFile( List<Long> results, List<String> queries) throws FileNotFoundException, UnsupportedEncodingException {
//		/*
//		 * Generate File !
//		 */
//        PrintWriter writer = new PrintWriter("resources/results/"+Settings.resultFileName+Settings.filters+"table.txt", "UTF-8");
//        PrintWriter writerQ = new PrintWriter("resources/results/"+Settings.resultFileName+Settings.filters+"queries.txt", "UTF-8");
//
//
//        int j=0;
//
//        while (j<results.size()){
//            writer.println (j + " & "+ results.get(j)); // table
//            writerQ.println(j + " & " +queries.get(j));
//
//            j++;
//        }
//        writer.close();
//        writerQ.close();
//
//
//    }

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
    private QuestOWLConnection createStuff() throws OBDAException, OWLOntologyCreationException, IOException, InvalidPredicateDeclarationException, InvalidMappingException{

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
        ioManager.load(obdaFile);

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
        QuestPreferences preference = new QuestPreferences();
        preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        preference.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);
        factory.setPreferenceHolder(preference);




        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

        this.reasoner = reasoner;
		/*
		 * Prepare the data connection for querying.
		 */
        QuestOWLConnection conn = reasoner.getConnection();

        return conn;

    }




    private List<Long> runQueries(QuestOWLConnection conn, List<String> queries) throws OWLException {

        //int nWarmUps = Constants.NUM_WARM_UPS;
        //int nRuns = Constants.NUM_RUNS;

        List<Long> results = new ArrayList<>();

        int j=0;
        int length = queries.size();
        while (j < length){
            String sparqlQuery = queries.get(j);
            QuestOWLStatement st = conn.createStatement();
            try {

                long time = 0;
                int count = 0;

                //for (int i=0; i<nRuns; ++i){
                long t1 = System.currentTimeMillis();
                QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
                int columnSize = rs.getColumnCount();
                count = 0;
                while (rs.nextRow()) {
                    count ++;
                    for (int idx = 1; idx <= columnSize; idx++) {
                        @SuppressWarnings("unused")
                        OWLObject binding = rs.getOWLObject(idx);
//                        System.out.print(binding.toString() + ", ");
                    }
//                    System.out.print("\n");
                }
                long t2 = System.currentTimeMillis();
                //time = time + (t2-t1);
                time =  (t2-t1);
                System.out.println("partial time:" + time);
                rs.close();
                //}

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
                System.out.println(time + "ms");

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


        if(args.length <2){

                System.out.println(
                        "Options:\n\n"
                                + "--POSTGRES2DSten; --POSTGRES2DSthirty; --POSTGRES2DSsixty"
                                 + "--POSTGRES3DSten; --POSTGRES3DSthirty; --POSTGRES3DSsixty");
                System.exit(0);

            }

         else {
            defaults(args[0], args[1]);
        }

        try {
            System.out.println(Settings.obdaFile);

            SameAsOntowisTest example = new SameAsOntowisTest(Settings.obdaFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void defaults(String string, String run) {

        switch(string){

            case "--POSTGRES2DSten":{
                Settings.obdaFile = ParamConst.POSTGRES2DSten;
                Settings.resultFileName = "POSTGRES2DSten";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;


                break;
            }

            case "--POSTGRES2DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSthirty;
                Settings.resultFileName = "POSTGRES2DSthirty";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;

                break;
            }

            case "--POSTGRES2DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSsixty;
                Settings.resultFileName = "POSTGRES2DSsixty";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;

                break;
            }

            case "--POSTGRES3DSten":{
                Settings.obdaFile = ParamConst.POSTGRES3DSten;
                Settings.resultFileName = "POSTGRES3DSten";
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;

                break;
            }

            case "--POSTGRES3DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSthirty;
                Settings.resultFileName = "POSTGRES3DSthirty";
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;

                break;
            }

            case "--POSTGRES3DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSsixty;
                Settings.resultFileName = "POSTGRES3DSsixty";
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;

                break;
            }

            default :{
                System.out.println(
                        "Options:\n\n 2 parameters"
                                + "--POSTGRES2DSten; --POSTGRES2DSthirty; --POSTGRES2DSsixty"
                                + "--POSTGRES3DSten; --POSTGRES3DSthirty; --POSTGRES3DSsixty"

                                + "and filter (1,2,3)");
                System.exit(0);
                break;
            }


        }
        Pattern isInteger = Pattern.compile("\\d+");

        if(isInteger.matcher(run).matches()) {
            Settings.runs = Integer.parseInt(run);
            if(Settings.runs>3 || Settings.runs <0){

                System.out.println("error setting the run use 1,2 or 3");
                System.exit(0);
            }
        }
        else{

            System.out.println("error setting the run");
            System.exit(0);
        }
    }
    static class QueryTemplates{

        private final static String SPARQL_BEGIN =  "PREFIX :	<http://www.example.org/> "
                + "SELECT *  "
                + "WHERE {";
        private final static String SPARQL_END = "}";

        static String classSparqlQuery(int n, int filter){
            String result = SPARQL_BEGIN + oneClassSparqlTemplate(n)  + SPARQL_END + limit(filter);
            return result;
        }

        static String dataSparqlQuery(int n, int filter){
            String result = SPARQL_BEGIN + dataSparqlTemplate(n) + filter(filter) + SPARQL_END;
            return result;
        }

        static String objectSparqlQuery(int n, int filter) {
            String result = SPARQL_BEGIN + objectSparqlTemplate(n) + filter(filter) + SPARQL_END;
            return result;
        }

        static String classAndObjectSparqlQuery(int nclass, int ndata, int nobject,  int filter) {

            String result;


            if (ndata == 0 ) {
                result = SPARQL_BEGIN + oneClassSparqlTemplate(nclass) + dataSparqlTemplate(ndata) + objectSparqlTemplate(nobject) + SPARQL_END + limit(filter);
            }
            //in  case data property is present add filter instead of limit
            else{
                result = SPARQL_BEGIN + oneClassSparqlTemplate(nclass)  +dataSparqlTemplate(ndata)  + objectSparqlTemplate(nobject) + filter (filter, ndata) + SPARQL_END;

            }


            return result;
        }

        static String classAndObjectSparqlQuery(int nclass, int ndata, int nobject) {
            String result = SPARQL_BEGIN + oneClassSparqlTemplate(nclass)  +dataSparqlTemplate(ndata)  + objectSparqlTemplate(nobject) + SPARQL_END;
            return result;
        }


        static private String filter(int filter){
            return "Filter( ?y < "+filter+" )";
        }

        static private String filter(int filter, int n){
            String templ ="";
            if(n >0) {
                int previous = n - 1;
                if (previous > 0) {
                    templ += "Filter( ?y" + previous + " < " + filter + " ) ";
                }
                templ += "Filter( ?y" + n + " < " + filter + " ) ";
            }
            return templ;
        }

        static private String limit(int filter){
            return "LIMIT " +filter;
        }

//

        static private String oneClassSparqlTemplate(int n) {
            String templ ="?x a :A" + n + " . ";


            return templ;
        }
        static private String classSparqlTemplate(int n) {
            String templ ="";
            for (int i = 1; i<=n; i ++) {
                 templ+="?x a :A" + i + " . ";
            }

            return templ;
        }

        static private String dataSparqlTemplate(int n) {
            String templ ="";
            if(n >0) {
                int previous = n - 1;
                if (previous > 0) {
                    templ += "?x :S" + previous + " ?y" + previous + " . ";
                }
                templ += "?x :S" + n + " ?y" + n + " . ";

            }

            return templ;
        }

        static private String objectSparqlTemplate(int n) {
            String templ ="";
            if(n >0) {
                int previous = n - 1;
                if (previous > 0) {
                    templ += "?x :R" + previous + " ?w" + previous + " . ";
                }
                templ += "?x :R" + n + " ?w" + n + " . ";
            }
            return templ;
        }

        static private String oneSparqlTwoDataProperty(int n) {
            String templ =
                    "PREFIX :	<http://www.example.org/> "
                            + "SELECT *  "
                            + "WHERE {"
                            + "?x a :A"+n+" . "
                            + "?x :S"+n+" ?y . "
                            + "?x :S"+n +1 +" ?w . ";
            return templ;
        }

        static private String oneSparqlObjectAndTwoDataProperty(int n) {
            String templ =
                    "PREFIX :	<http://www.example.org/> "
                            + "SELECT * "
                            + "WHERE {"
                            + "?x a :A"+n+" . "
                            + "?x :S"+n+" ?y . "
                            + "?x :S"+n +1 +" ?w . "
                            + "?x :R ?z . ";
            return templ;
        }
    };



    class QueryFactory {

        private final  int sizeQueriesArray = Settings.NUM_TABLES * (Settings.NUM_TABLES * Settings.NUM_OBJECTS * Settings.NUM_DATA);

//        private final static int sizeQueries = 15;

//        List<String> filterSPARQL =new ArrayList<>(sizeQueriesArray);
        List<String> filter0SPARQL =new ArrayList<>(sizeQueriesArray);
        List<String> filter1SPARQL = new ArrayList<>(sizeQueriesArray);
        List<String> filter2SPARQL = new ArrayList<>(sizeQueriesArray);



        List<String> warmUpQueries = new ArrayList<>();

        int[] filters = new int[3];

        QueryFactory(){
//            fillFilters();
            fillQueryArrays();
        }

        private void fillQueryArrays (){

            fillWarmUpQueries();
            fillFilters();

            for(int i=0; i<filters.length; i++) {
                fillQueries(i);
            }
            // 1 SPARQL limit 100
//            fillLimit1000();
//
//            fillLimit10000();
//
//            fillLimit100000();
//
//            fillNoLimit();
//
//            fillSparqlQueries();



        }

        private void fillWarmUpQueries() {
            for(int i = 0; i < Constants.NUM_WARM_UPS; i++) {
                int limit = (i * 1000) + 1;
                warmUpQueries.add(i, String.format("SELECT ?x WHERE { " +
                        "?x a <http://www.example.org/A%d>  } LIMIT "+limit, i));
            }
        }


        private void fillFilters() {

                    filters[0] = 1000;   // 0.001%
                    filters[1] = 10000;  // 0.01%
                    filters[2] = 100000; // 0.1%

        }

        private void fillQueries(){

            int results=0;

            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {


//                        filterSPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, Settings.filters)); // 1 SQL Join
                        results++;

                    }

                }
            }
        }

        private void fillQueries(int filter){
            int results=0;
            switch(filter){
                case 0:


                    for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                        for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {



                                filter0SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[filter]));
                                results++;

                            }

                        }
                    }
                    break;
                case 1:


                    for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                        for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {



                                filter1SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[filter]));
                                results++;

                            }

                        }
                    }
                    break;
                case 2:


                    for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                        for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {



                                filter2SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[filter]));
                                results++;

                            }

                        }
                    }
                    break;
            }

        }


//        private void fillLimit1000(){
//            int results=0;
//
//            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){
//
//                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {
//
//                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
//
////                        if (ndata == 0 && nobject == 0) {
////                            filter0SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[0]);
////                            results++;
////                        } else {
//
//                            filter0SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[0])); // 1 SQL Join
//                            results++;
////                        }
//                    }
//
//                }
//            }
//        }

//        private void fillLimit10000(){
//            int results=0;
//
//            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){
//
//                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {
//
//                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
//
////                        if (ndata == 0 && nobject == 0) {
////                            filter1SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[1]);
////                            results++;
////                        } else {
//
//                            filter1SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[1]));
//                            results++;
////                        }
//                    }
//
//                }
//            }
//        }

//        private void fillLimit100000(){
//            int results=0;
//
//            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){
//
//                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {
//
//                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
////
////                        if (ndata == 0 && nobject == 0) {
////                            filter2SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[2]);
////                            results++;
////                        } else {
//
//                            filter2SPARQL.add(results,QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[2]));
//                            results++;
////                        }
//                    }
//
//                }
//            }
//        }

//        private void fillNoLimit(){
//
//            int results=0;
//
//            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){
//
//                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {
//
//                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
////
//
//                        filterNoLimitSPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject));
//                        results++;
////                        }
//                    }
//
//                }
//            }
//
//
//
//        }


//        private void fillSparqlQueries(){
//
////            queriesTest[0] = "PREFIX :	<http://www.example.org/> SELECT *  WHERE {?x a :A3 . ?x :S1 ?y1 . ?x :S2 ?y2 . ?x :R1 ?w1 . }LIMIT 100";
//
//            queriesTest.add(0, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1. } LIMIT 100000 ");
//            queriesTest.add(1, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2. }  LIMIT 100000");
//
//            queriesTest.add(2, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S1 ?y . Filter( ?y < 100000)} ");
//            queriesTest.add(3, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S1 ?y . Filter( ?y < 100000)} ");
//            queriesTest.add(4,  "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S2 ?y . Filter( ?y < 100000)} ");
//            queriesTest.add(5, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S2 ?y . Filter( ?y < 100000)} ");
//            queriesTest.add(6, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y . ?x :S1 ?w . Filter( ?w < 100000) } ");
//            queriesTest.add(7, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y .?x :S2 ?w . Filter( ?w < 100000) } ");
//            queriesTest.add(8, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y . ?x :S1 ?w . Filter( ?w < 100000) } ");
//            queriesTest.add(9, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y .?x :S2 ?w . Filter( ?w < 100000) } ");
//            queriesTest.add(10, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S1 ?y .?x :S2 ?w .  Filter( ?y < 100000)  Filter( ?w < 100000)} ");
//            queriesTest.add(11, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S1 ?y .?x :S2 ?w .  Filter( ?y < 100000)  Filter( ?w < 100000)} ");
//            queriesTest.add(12, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y . ?x :S1 ?w .?x :S2 ?z .  Filter( ?w < 100000)  Filter( ?z < 100000)} ");
//            queriesTest.add(13, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y . ?x :S1 ?w .?x :S2 ?z .  Filter( ?w < 100000)  Filter( ?z < 100000)} ");
//            queriesTest.add(14, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A3 . ?x :S3 ?y .  Filter( ?y < 100000) } ");
//
//
//        }
    };
}