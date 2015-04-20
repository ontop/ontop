package inf.unibz.ontop.sesame.tests.general;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sarah on 09/04/15.
 */
public class SameAsOntowisTest {

    class Constants {
        static final int NUM_FILTERS = 3;


        static final int NUM_RUNS = 1;
        static final int NUM_WARM_UPS = 0;
    }

    interface ParamConst{
        // Postgres
        public static final String POSTGRES2DSten = "src/test/resources/sameAs/ontowis/ontowisOBDA2-ten.obda";
        public static final String POSTGRES2DSthirty = "src/test/resources/sameAs/ontowis/ontowisOBDA2-thirty.obda";
        public static final String POSTGRES2DSsixty = "src/test/resources/sameAs/ontowis/ontowisOBDA2-sixty.obda";
        public static final String POSTGRES3DSten = "src/test/resources/sameAs/ontowis/ontowisOBDA3-ten.obda";
        public static final String POSTGRES3DSthirty = "src/test/resources/sameAs/ontowis/ontowisOBDA3-thirty.obda";
        public static final String POSTGRES3DSsixty = "src/test/resources/sameAs/ontowis/ontowisOBDA3-sixty.obda";


    }

    public static class Settings{
        static String obdaFile;
        static  int NUM_TABLES ;
        static  int NUM_OBJECTS ;
        static  int NUM_DATA;

    }

    final String obdaFile;


    final String owlfile = "src/test/resources/sameAs/ontowis/ontowis.owl";


    // Internal Modifiable State
    QuestOWL reasoner ;

    public SameAsOntowisTest(String obdaFile){
        this.obdaFile = obdaFile;
    }


    public void runQuery() throws Exception {

        QuestOWLConnection conn =  createStuff();


        // Create Queries to be run
        QueryFactory queryFactory = new QueryFactory();

        // Run the tests on the queries


        List<List<Long>> selectivityLow_list = new ArrayList<>(); // There is a list for each run.
        List<List<Long>> selectivityMiddle_list = new ArrayList<>();
        List<List<Long>> selectivityHigh_list = new ArrayList<>();
        List<List<Long>> selectivityNoLimit_list = new ArrayList<>();

        List<List<Long>> selectivityDifferent_list = new ArrayList<>();

        runQueries(conn, queryFactory.warmUpQueries);


        for(int i = 0; i < Constants.NUM_RUNS; i++) {
            List<Long> selectivityLow = runQueries(conn, queryFactory.filter0SPARQL);
            selectivityLow_list.add(selectivityLow);

            List<Long> selectivityMiddle = runQueries(conn, queryFactory.filter1SPARQL);
            selectivityMiddle_list.add(selectivityMiddle);

            List<Long> selectivityHigh = runQueries(conn, queryFactory.filter2SPARQL);
            selectivityHigh_list.add(selectivityHigh);

            List<Long> selectivityNoLimit = runQueries(conn, queryFactory.filterNoLimitSPARQL);
            selectivityNoLimit_list.add(selectivityNoLimit);

            List<Long> selectivityDifferent = runQueries(conn, queryFactory.queriesTest);
            selectivityDifferent_list.add(selectivityDifferent);
        }
        closeEverything(conn);

        List<Long> avg_selectivityLow = average(selectivityLow_list);
        List<Long> avg_selectivityMiddle = average(selectivityMiddle_list);
        List<Long> avg_selectivityHigh = average(selectivityHigh_list);
        List<Long> avg_selectivityNoLimit = average(selectivityNoLimit_list);
        List<Long> avg_selectivityDifferent = average(selectivityDifferent_list);

        generateFile(avg_selectivityLow, avg_selectivityMiddle, avg_selectivityHigh, avg_selectivityNoLimit, avg_selectivityDifferent);

//        runQueries(conn, queryFactory.queriesTest);

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
     * @param resultsLow

     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    private void generateFile( List<Long> resultsLow, List<Long> resultsMiddle, List<Long> resultsHigh, List<Long> resultsNoLimit, List<Long> resultsDifferent) throws FileNotFoundException, UnsupportedEncodingException {
		/*
		 * Generate File !
		 */
        PrintWriter writer = new PrintWriter("src/main/resources/example/table.txt", "UTF-8");
        PrintWriter writerG = new PrintWriter("src/main/resources/example/differentTest.txt", "UTF-8");

//        int sizeQueriesArray = Constants.NUM_TABLES * (Constants.NUM_TABLES * Constants.NUM_OBJECTS * Constants.NUM_DATA);
        int nF = Constants.NUM_FILTERS;

        int j=0;

        while (j<resultsLow.size()){
            writer.println(resultsLow.get(j) + " & " + resultsMiddle.get(j) + " & " + resultsHigh.get(j) + " & " + resultsNoLimit.get(j)); // table

//            if (j<Constants.NUM_FILTERS){
//                String gline = "(1," + resultsLow.get(j) + ")" + "(2," + resultsMiddle.get(j) + ")"
//                        + "(3," + resultsHigh.get(j) + ")" + "(4," + resultsLow.get(j + nF*1) + ")"
//                        + "(5," + resultsMiddle.get(j + nF*1) + ")" + "(6," + resultsHigh.get(j + nF*1) + ")"
//                        + "(7," + resultsLow.get(j + nF*2) + ")" + "(8," + resultsMiddle.get(j + nF*2) + ")"
//                        + "(9," + resultsHigh.get(j + nF*2) + ")" + "(10," + resultsLow.get(j + nF*3) + ")"
//                        + "(11," + resultsMiddle.get(j + nF*3) + ")" + "(12," + resultsHigh.get(j + nF*3) + ")";
//                writerG.println(gline);
//            }
            j++;
        }
        writer.close();
        int value = 0;
        while (value < resultsDifferent.size()){
            writerG.println(resultsDifferent.get(j));
            value++;
        }
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


        switch(args[0]){
            case "--help":{
                System.out.println(
                        "Options:\n\n"
                                + "--POSTGRES2DSten; --POSTGRES2DSthirty; --POSTGRES2DSsixty"
                                 + "--POSTGRES3DSten; --POSTGRES3DSthirty; --POSTGRES3DSsixty");
                System.exit(0);
                break;
            }

            default:
                defaults(args[0]);
                break;
        }
        try {
            System.out.println(Settings.obdaFile);

            SameAsOntowisTest example = new SameAsOntowisTest(Settings.obdaFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void defaults(String string) {

        switch(string){

            case "--POSTGRES2DSten":{
                Settings.obdaFile = ParamConst.POSTGRES2DSten;
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;

                break;
            }

            case "--POSTGRES2DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSthirty;
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;
                break;
            }

            case "--POSTGRES2DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSsixty;
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;
                break;
            }

            case "--POSTGRES3DSten":{
                Settings.obdaFile = ParamConst.POSTGRES3DSten;
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;
                break;
            }

            case "--POSTGRES3DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSthirty;
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;
                break;
            }

            case "--POSTGRES3DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSsixty;
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;
                break;
            }



            default :{
                System.out.println(
                        "Options:\n\n"
                                + "--POSTGRES2DSten; --POSTGRES2DSthirty; --POSTGRES2DSsixty"
                                + "--POSTGRES3DSten; --POSTGRES3DSthirty; --POSTGRES3DSsixty");
                System.exit(0);
                break;
            }
        }
    }
    static class QueryTemplates{

        private final static String SPARQL_BEGIN =  "PREFIX :	<http://www.example.org/> "
                + "SELECT *  "
                + "WHERE {";
        private final static String SPARQL_END = "}";

        static String classSparqlQuery(int n, int filter){
            String result = SPARQL_BEGIN + oneClassSparqlTemplate(n)  + SPARQL_END + filterClass(filter);
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
            String result = SPARQL_BEGIN + oneClassSparqlTemplate(nclass)  +dataSparqlTemplate(ndata)  + objectSparqlTemplate(nobject) + SPARQL_END + filterClass(filter);
            return result;
        }

        static String classAndObjectSparqlQuery(int nclass, int ndata, int nobject) {
            String result = SPARQL_BEGIN + oneClassSparqlTemplate(nclass)  +dataSparqlTemplate(ndata)  + objectSparqlTemplate(nobject) + SPARQL_END;
            return result;
        }


        static private String filter(int filter){
            return "Filter( ?y < "+filter+" )";
        }

        static private String filterClass(int filter){
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

//        private final static int sizeQueriesArray = Constants.NUM_TABLES * (Constants.NUM_TABLES * Constants.NUM_OBJECTS * Constants.NUM_DATA);

//        private final static int sizeQueries = 15;

        List<String> filter0SPARQL =new ArrayList<>();
        List<String> filter1SPARQL = new ArrayList<>();
        List<String> filter2SPARQL = new ArrayList<>();
        List<String> filterNoLimitSPARQL = new ArrayList<>();

        List<String> queriesTest = new ArrayList<>();


        List<String> warmUpQueries = new ArrayList<>();

        int[] filters = new int[Constants.NUM_FILTERS];

        QueryFactory(){
            fillFilters();
            fillQueryArrays();
        }

        private void fillQueryArrays (){

            fillWarmUpQueries();

            // 1 SPARQL limit 100
            fillLimit1000();

            fillLimit10000();

            fillLimit100000();

            fillNoLimit();

            fillSparqlQueries();



        }

        private void fillWarmUpQueries() {
            for(int i = 0; i < Constants.NUM_WARM_UPS; i++) {
                int limit = (i * 1000) + 1;
                warmUpQueries.add(i, String.format("SELECT ?x WHERE { " +
                        "?x a <http://www.example.org/A%d>  } LIMIT "+limit, i));
            }
        }


        private void fillFilters() {

                    filters[0] = 1000;   // 0.0001%
                    filters[1] = 10000;  // 0.01%
                    filters[2] = 100000; // 0.1%

        }

        private void fillLimit1000(){
            int results=0;

            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {

//                        if (ndata == 0 && nobject == 0) {
//                            filter0SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[0]);
//                            results++;
//                        } else {

                            filter0SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[0])); // 1 SQL Join
                            results++;
//                        }
                    }

                }
            }
        }

        private void fillLimit10000(){
            int results=0;

            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {

//                        if (ndata == 0 && nobject == 0) {
//                            filter1SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[1]);
//                            results++;
//                        } else {

                            filter1SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[1]));
                            results++;
//                        }
                    }

                }
            }
        }

        private void fillLimit100000(){
            int results=0;

            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
//
//                        if (ndata == 0 && nobject == 0) {
//                            filter2SPARQL[results] = QueryTemplates.classSparqlQuery(i + 1, filters[2]);
//                            results++;
//                        } else {

                            filter2SPARQL.add(results,QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[2]));
                            results++;
//                        }
                    }

                }
            }
        }

        private void fillNoLimit(){

            int results=0;

            for ( int i = 0 ; i< Settings.NUM_TABLES; i ++){

                for ( int ndata = 0 ; ndata<=Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <=Settings.NUM_OBJECTS; nobject++) {
//

                        filterNoLimitSPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject));
                        results++;
//                        }
                    }

                }
            }



        }


        private void fillSparqlQueries(){

//            queriesTest[0] = "PREFIX :	<http://www.example.org/> SELECT *  WHERE {?x a :A3 . ?x :S1 ?y1 . ?x :S2 ?y2 . ?x :R1 ?w1 . }LIMIT 100";

            queriesTest.add(0, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1. } LIMIT 100000 ");
            queriesTest.add(1, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2. }  LIMIT 100000");

            queriesTest.add(2, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S1 ?y . Filter( ?y < 100000)} ");
            queriesTest.add(3, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S1 ?y . Filter( ?y < 100000)} ");
            queriesTest.add(4,  "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S2 ?y . Filter( ?y < 100000)} ");
            queriesTest.add(5, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S2 ?y . Filter( ?y < 100000)} ");
            queriesTest.add(6, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y . ?x :S1 ?w . Filter( ?w < 100000) } ");
            queriesTest.add(7, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y .?x :S2 ?w . Filter( ?w < 100000) } ");
            queriesTest.add(8, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y . ?x :S1 ?w . Filter( ?w < 100000) } ");
            queriesTest.add(9, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y .?x :S2 ?w . Filter( ?w < 100000) } ");
            queriesTest.add(10, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :S1 ?y .?x :S2 ?w .  Filter( ?y < 100000)  Filter( ?w < 100000)} ");
            queriesTest.add(11, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :S1 ?y .?x :S2 ?w .  Filter( ?y < 100000)  Filter( ?w < 100000)} ");
            queriesTest.add(12, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A2 . ?x :R1 ?y . ?x :S1 ?w .?x :S2 ?z .  Filter( ?w < 100000)  Filter( ?z < 100000)} ");
            queriesTest.add(13, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A1 . ?x :R1 ?y . ?x :S1 ?w .?x :S2 ?z .  Filter( ?w < 100000)  Filter( ?z < 100000)} ");
            queriesTest.add(14, "PREFIX :<http://www.example.org/> SELECT * WHERE { ?x a :A3 . ?x :S3 ?y .  Filter( ?y < 100000) } ");


        }
    };
}