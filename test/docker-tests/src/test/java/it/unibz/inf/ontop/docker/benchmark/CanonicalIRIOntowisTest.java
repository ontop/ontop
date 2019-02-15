package it.unibz.inf.ontop.docker.benchmark;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Ignore ("used only for benchmark tests")
public class CanonicalIRIOntowisTest {

    class Constants {
        static final int NUM_FILTERS = 3;

        //        static final int NUM_RUNS = 1;
        static final int NUM_WARM_UPS = 0;
    }

    interface ParamConst{
        // Postgres
        public static final String POSTGRES2DSten = "ontowisOBDA2-ten.obda";
        public static final String POSTGRES2DStenP = "ontowisOBDA2-ten.properties";
        public static final String POSTGRES2DSthirty = "ontowisOBDA2-thirty.obda";
        public static final String POSTGRES2DSthirtyP = "ontowisOBDA2-thirty.properties";
        public static final String POSTGRES2DSsixty = "ontowisOBDA2-sixty.obda";
        public static final String POSTGRES2DSsixtyP = "ontowisOBDA2-sixty.properties";
        public static final String POSTGRES3DSten = "ontowisOBDA3-ten.obda";
        public static final String POSTGRES3DStenP = "ontowisOBDA3-ten.properties";
        public static final String POSTGRES3DSthirty = "ontowisOBDA3-thirty.obda";
        public static final String POSTGRES3DSthirtyP = "ontowisOBDA3-thirty.properties";
        public static final String POSTGRES3DSsixty = "ontowisOBDA3-sixty.obda";
        public static final String POSTGRES3DSsixtyP = "ontowisOBDA3-sixty.properties";


    }

    public static class Settings{
        static String obdaFile;
        static String propertyFile;
        static String resultFileName;
        static int runs;
        static  int NUM_TABLES ;
        static  int NUM_OBJECTS ;
        static  int NUM_DATA;


    }

    final String obdaFile;

    final String propertyFile;


    final String owlfile = "ontowis.owl";


    // Internal Modifiable State
    OntopOWLReasoner reasoner ;

    public CanonicalIRIOntowisTest(String obdaFile, String propertyFile){
        this.obdaFile = obdaFile;
        this.propertyFile = propertyFile;
    }


    public void runQuery() throws Exception {

        long t1 = System.currentTimeMillis();
        OWLConnection conn =  createStuff();
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
        PrintWriter writer = new PrintWriter("results/"+ Settings.resultFileName+"table.txt", "UTF-8");
        PrintWriter writerQ = new PrintWriter("results/"+ Settings.resultFileName+"queries.txt", "UTF-8");
        PrintWriter writerG = new PrintWriter("results/"+ Settings.resultFileName+"graph.txt", "UTF-8");

        writer.println("offline time: " + time);

        if(Settings.obdaFile.equals(CanonicalIRIOntowisTest.ParamConst.POSTGRES2DSten) || Settings.obdaFile.equals(CanonicalIRIOntowisTest.ParamConst.POSTGRES2DSthirty) || Settings.obdaFile.equals(CanonicalIRIOntowisTest.ParamConst.POSTGRES2DSsixty) ) {

            writerG.print(resultsLow.get(0) + " & " + resultsLow.get(1) + " & " + resultsLow.get(4) + " & " + resultsLow.get(2) + " & " + resultsLow.get(8) + " & " + resultsLow.get(5) + " & " + resultsLow.get(6) + " & " + resultsLow.get(9) + " & " + resultsLow.get(10));
            writerG.println("");
            writerG.print(resultsLow.get(16) + " & " + resultsLow.get(17) + " & " + resultsLow.get(20) + " & " + resultsLow.get(3) + " & " + resultsLow.get(12) + " & " + resultsLow.get(21) + " & " + resultsLow.get(7) + " & " + resultsLow.get(13) + " & " + resultsLow.get(11));
            writerG.println("");
            writerG.print(resultsLow.get(32) + " & " + resultsLow.get(33) + " & " + resultsLow.get(36) + " & " + resultsLow.get(18) + " & " + resultsLow.get(24) + " & " + resultsLow.get(37) + " & " + resultsLow.get(22) + " & " + resultsLow.get(25) + " & " + resultsLow.get(14));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsLow.get(19) + " & " + resultsLow.get(28) + " & " + " & " + resultsLow.get(23) + " & " + resultsLow.get(29) + " & " + resultsLow.get(15));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsLow.get(34) + " & " + resultsLow.get(40) + " & " + " & " + resultsLow.get(38) + " & " + resultsLow.get(41) + " & " + resultsLow.get(26));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsLow.get(35) + " & " + resultsLow.get(44) + " & " + " & " + resultsLow.get(39) + " & " + resultsLow.get(45) + " & " + resultsLow.get(27));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(30));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(31));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(42));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(43));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(46));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(47));
            writerG.println("");
            writerG.println("");
            writerG.print(resultsMiddle.get(0) + " & " + resultsMiddle.get(1) + " & " + resultsMiddle.get(4) + " & " + resultsMiddle.get(2) + " & " + resultsMiddle.get(8) + " & " + resultsMiddle.get(5) + " & " + resultsMiddle.get(6) + " & " + resultsMiddle.get(9) + " & " + resultsMiddle.get(10));
            writerG.println("");
            writerG.print(resultsMiddle.get(16) + " & " + resultsMiddle.get(17) + " & " + resultsMiddle.get(20) + " & " + resultsMiddle.get(3) + " & " + resultsMiddle.get(12) + " & " + resultsMiddle.get(21) + " & " + resultsMiddle.get(7) + " & " + resultsMiddle.get(13) + " & " + resultsMiddle.get(11));
            writerG.println("");
            writerG.print(resultsMiddle.get(32) + " & " + resultsMiddle.get(33) + " & " + resultsMiddle.get(36) + " & " + resultsMiddle.get(18) + " & " + resultsMiddle.get(24) + " & " + resultsMiddle.get(37) + " & " + resultsMiddle.get(22) + " & " + resultsMiddle.get(25) + " & " + resultsMiddle.get(14));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsMiddle.get(19) + " & " + resultsMiddle.get(28) + " & " + " & " + resultsMiddle.get(23) + " & " + resultsMiddle.get(29) + " & " + resultsMiddle.get(15));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsMiddle.get(34) + " & " + resultsMiddle.get(40) + " & " + " & " + resultsMiddle.get(38) + " & " + resultsMiddle.get(41) + " & " + resultsMiddle.get(26));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsMiddle.get(35) + " & " + resultsMiddle.get(44) + " & " + " & " + resultsMiddle.get(39) + " & " + resultsMiddle.get(45) + " & " + resultsMiddle.get(27));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(30));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(31));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(42));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(43));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(46));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(47));
            writerG.println("");
            writerG.println("");
            writerG.print(resultsHigh.get(0) + " & " + resultsHigh.get(1) + " & " + resultsHigh.get(4) + " & " + resultsHigh.get(2) + " & " + resultsHigh.get(8) + " & " + resultsHigh.get(5) + " & " + resultsHigh.get(6) + " & " + resultsHigh.get(9) + " & " + resultsHigh.get(10));
            writerG.println("");
            writerG.print(resultsHigh.get(16) + " & " + resultsHigh.get(17) + " & " + resultsHigh.get(20) + " & " + resultsHigh.get(3) + " & " + resultsHigh.get(12) + " & " + resultsHigh.get(21) + " & " + resultsHigh.get(7) + " & " + resultsHigh.get(13) + " & " + resultsHigh.get(11));
            writerG.println("");
            writerG.print(resultsHigh.get(32) + " & " + resultsHigh.get(33) + " & " + resultsHigh.get(36) + " & " + resultsHigh.get(18) + " & " + resultsHigh.get(24) + " & " + resultsHigh.get(37) + " & " + resultsHigh.get(22) + " & " + resultsHigh.get(25) + " & " + resultsHigh.get(14));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsHigh.get(19) + " & " + resultsHigh.get(28) + " & " + " & " + resultsHigh.get(23) + " & " + resultsHigh.get(29) + " & " + resultsHigh.get(15));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsHigh.get(34) + " & " + resultsHigh.get(40) + " & " + " & " + resultsHigh.get(38) + " & " + resultsHigh.get(41) + " & " + resultsHigh.get(26));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + resultsHigh.get(35) + " & " + resultsHigh.get(44) + " & " + " & " + resultsHigh.get(39) + " & " + resultsHigh.get(45) + " & " + resultsHigh.get(27));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(30));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(31));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(42));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(43));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(46));
            writerG.println("");
            writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(47));
            writerG.println("");

        }
        else{
            writerG.print(resultsLow.get(0) + " & " + resultsLow.get(1) + " & " + resultsLow.get(5) + " & " + resultsLow.get(2) + " & " + resultsLow.get(10) + " & " + resultsLow.get(6) + " & " + resultsLow.get(7) + " & " + resultsLow.get(11) + " & " + resultsLow.get(12));
            writerG.println("");
            writerG.print(resultsLow.get(25) + " & " + resultsLow.get(26) + " & " + resultsLow.get(30) + " & " + resultsLow.get(3) + " & " + resultsLow.get(15) + " & " + resultsLow.get(31) + " & " + resultsLow.get(8) + " & " + resultsLow.get(16) + " & " + resultsLow.get(13));
            writerG.println("");
            writerG.print(resultsLow.get(50) + " & " + resultsLow.get(51) + " & " + resultsLow.get(55) + " & " + resultsLow.get(4) + " & " + resultsLow.get(20) + " & " + resultsLow.get(56) + " & " + resultsLow.get(9) + " & " + resultsLow.get(21) + " & " + resultsLow.get(14));
            writerG.println("");
            writerG.print(resultsLow.get(75) + " & " + resultsLow.get(76) + " & " + resultsLow.get(80) + " & " + resultsLow.get(27) + " & " + resultsLow.get(35) + " & " + resultsLow.get(81) + " & " + resultsLow.get(32) + " & " + resultsLow.get(36) + " & " + resultsLow.get(17));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(28) + " & " + resultsLow.get(40) + " & "  + " & " + resultsLow.get(33) + " & " + resultsLow.get(41) + " & " + resultsLow.get(18));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(29) + " & " + resultsLow.get(45) + " & "  + " & " + resultsLow.get(34) + " & " + resultsLow.get(46) + " & " + resultsLow.get(19));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(52) + " & " + resultsLow.get(60) + " & "  + " & " + resultsLow.get(57) + " & " + resultsLow.get(61) + " & " + resultsLow.get(22));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(53) + " & " + resultsLow.get(65) + " & "  + " & " + resultsLow.get(58) + " & " + resultsLow.get(66) + " & " + resultsLow.get(23));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(54) + " & " + resultsLow.get(70) + " & "  + " & " + resultsLow.get(59) + " & " + resultsLow.get(71) + " & " + resultsLow.get(24));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(77) + " & " + resultsLow.get(85) + " & "  + " & " + resultsLow.get(82) + " & " + resultsLow.get(86) + " & " + resultsLow.get(37));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(78) + " & " + resultsLow.get(90) + " & "  + " & " + resultsLow.get(83) + " & " + resultsLow.get(91) + " & " + resultsLow.get(38));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsLow.get(79) + " & " + resultsLow.get(95) + " & "  + " & " + resultsLow.get(84) + " & " + resultsLow.get(96) + " & " + resultsLow.get(39));
            writerG.println("");
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(42 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(47 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(62 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(67 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(72 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(87 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(92 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsLow.get(97 + i));
                writerG.println("");
            }
            writerG.println("");

            writerG.print(resultsMiddle.get(0) + " & " + resultsMiddle.get(1) + " & " + resultsMiddle.get(5) + " & " + resultsMiddle.get(2) + " & " + resultsMiddle.get(10) + " & " + resultsMiddle.get(6) + " & " + resultsMiddle.get(7) + " & " + resultsMiddle.get(11) + " & " + resultsMiddle.get(12));
            writerG.println("");
            writerG.print(resultsMiddle.get(25) + " & " + resultsMiddle.get(26) + " & " + resultsMiddle.get(30) + " & " + resultsMiddle.get(3) + " & " + resultsMiddle.get(15) + " & " + resultsMiddle.get(31) + " & " + resultsMiddle.get(8) + " & " + resultsMiddle.get(16) + " & " + resultsMiddle.get(13));
            writerG.println("");
            writerG.print(resultsMiddle.get(50) + " & " + resultsMiddle.get(51) + " & " + resultsMiddle.get(55) + " & " + resultsMiddle.get(4) + " & " + resultsMiddle.get(20) + " & " + resultsMiddle.get(56) + " & " + resultsMiddle.get(9) + " & " + resultsMiddle.get(21) + " & " + resultsMiddle.get(14));
            writerG.println("");
            writerG.print(resultsMiddle.get(75) + " & " + resultsMiddle.get(76) + " & " + resultsMiddle.get(80) + " & " + resultsMiddle.get(27) + " & " + resultsMiddle.get(35) + " & " + resultsMiddle.get(81) + " & " + resultsMiddle.get(32) + " & " + resultsMiddle.get(36) + " & " + resultsMiddle.get(17));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(28) + " & " + resultsMiddle.get(40) + " & "  + " & " + resultsMiddle.get(33) + " & " + resultsMiddle.get(41) + " & " + resultsMiddle.get(18));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(29) + " & " + resultsMiddle.get(45) + " & "  + " & " + resultsMiddle.get(34) + " & " + resultsMiddle.get(46) + " & " + resultsMiddle.get(19));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(52) + " & " + resultsMiddle.get(60) + " & "  + " & " + resultsMiddle.get(57) + " & " + resultsMiddle.get(61) + " & " + resultsMiddle.get(22));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(53) + " & " + resultsMiddle.get(65) + " & "  + " & " + resultsMiddle.get(58) + " & " + resultsMiddle.get(66) + " & " + resultsMiddle.get(23));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(54) + " & " + resultsMiddle.get(70) + " & "  + " & " + resultsMiddle.get(59) + " & " + resultsMiddle.get(71) + " & " + resultsMiddle.get(24));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(77) + " & " + resultsMiddle.get(85) + " & "  + " & " + resultsMiddle.get(82) + " & " + resultsMiddle.get(86) + " & " + resultsMiddle.get(37));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(78) + " & " + resultsMiddle.get(90) + " & "  + " & " + resultsMiddle.get(83) + " & " + resultsMiddle.get(91) + " & " + resultsMiddle.get(38));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsMiddle.get(79) + " & " + resultsMiddle.get(95) + " & "  + " & " + resultsMiddle.get(84) + " & " + resultsMiddle.get(96) + " & " + resultsMiddle.get(39));
            writerG.println("");
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(42 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(47 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(62 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(67 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(72 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(87 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(92 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsMiddle.get(97 + i));
                writerG.println("");
            }
            writerG.println("");

            writerG.print(resultsHigh.get(0) + " & " + resultsHigh.get(1) + " & " + resultsHigh.get(5) + " & " + resultsHigh.get(2) + " & " + resultsHigh.get(10) + " & " + resultsHigh.get(6) + " & " + resultsHigh.get(7) + " & " + resultsHigh.get(11) + " & " + resultsHigh.get(12));
            writerG.println("");
            writerG.print(resultsHigh.get(25) + " & " + resultsHigh.get(26) + " & " + resultsHigh.get(30) + " & " + resultsHigh.get(3) + " & " + resultsHigh.get(15) + " & " + resultsHigh.get(31) + " & " + resultsHigh.get(8) + " & " + resultsHigh.get(16) + " & " + resultsHigh.get(13));
            writerG.println("");
            writerG.print(resultsHigh.get(50) + " & " + resultsHigh.get(51) + " & " + resultsHigh.get(55) + " & " + resultsHigh.get(4) + " & " + resultsHigh.get(20) + " & " + resultsHigh.get(56) + " & " + resultsHigh.get(9) + " & " + resultsHigh.get(21) + " & " + resultsHigh.get(14));
            writerG.println("");
            writerG.print(resultsHigh.get(75) + " & " + resultsHigh.get(76) + " & " + resultsHigh.get(80) + " & " + resultsHigh.get(27) + " & " + resultsHigh.get(35) + " & " + resultsHigh.get(81) + " & " + resultsHigh.get(32) + " & " + resultsHigh.get(36) + " & " + resultsHigh.get(17));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(28) + " & " + resultsHigh.get(40) + " & "  + " & " + resultsHigh.get(33) + " & " + resultsHigh.get(41) + " & " + resultsHigh.get(18));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(29) + " & " + resultsHigh.get(45) + " & "  + " & " + resultsHigh.get(34) + " & " + resultsHigh.get(46) + " & " + resultsHigh.get(19));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(52) + " & " + resultsHigh.get(60) + " & "  + " & " + resultsHigh.get(57) + " & " + resultsHigh.get(61) + " & " + resultsHigh.get(22));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(53) + " & " + resultsHigh.get(65) + " & "  + " & " + resultsHigh.get(58) + " & " + resultsHigh.get(66) + " & " + resultsHigh.get(23));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(54) + " & " + resultsHigh.get(70) + " & "  + " & " + resultsHigh.get(59) + " & " + resultsHigh.get(71) + " & " + resultsHigh.get(24));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(77) + " & " + resultsHigh.get(85) + " & "  + " & " + resultsHigh.get(82) + " & " + resultsHigh.get(86) + " & " + resultsHigh.get(37));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(78) + " & " + resultsHigh.get(90) + " & "  + " & " + resultsHigh.get(83) + " & " + resultsHigh.get(91) + " & " + resultsHigh.get(38));
            writerG.println("");
            writerG.print( " & " + " & "  + " & " + resultsHigh.get(79) + " & " + resultsHigh.get(95) + " & "  + " & " + resultsHigh.get(84) + " & " + resultsHigh.get(96) + " & " + resultsHigh.get(39));
            writerG.println("");
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(42 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(47 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(62 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(67 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(72 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(87 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(92 + i));
                writerG.println("");
            }
            for (int i=0; i<3; i++) {
                writerG.print(" & " + " & " + " & " + " & " + " & " + " & " + " & " + " & " + resultsHigh.get(97 + i));
                writerG.println("");
            }

        }


        writerG.close();
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
    private void closeEverything(OWLConnection conn) throws OWLException {
		/*
		 * Close connection and resources
		 */

        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        this.reasoner.dispose();
    }

    /**
     * @throws OWLOntologyCreationException
     * @throws InvalidMappingException
     * @throws IOException
     * @throws OWLException
     */
    private OWLConnection createStuff() throws OWLOntologyCreationException, IOException, InvalidMappingException{

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlfile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyFile)
                .sameAsMappings(true)
                .build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();


        OntopOWLReasoner reasoner = factory.createReasoner(config);

        this.reasoner = reasoner;
		/*
		 * Prepare the data connection for querying.
		 */
        OWLConnection conn = reasoner.getConnection();

        return conn;

    }




    private List<Long> runQueries(OWLConnection conn, List<String> queries) throws OWLException {

        //int nWarmUps = Constants.NUM_WARM_UPS;
        //int nRuns = Constants.NUM_RUNS;

        List<Long> results = new ArrayList<>();

        int j=0;
        int length = queries.size();
        while (j < length){
            String sparqlQuery = queries.get(j);
            OWLStatement st = conn.createStatement();
            try {

                long time = 0;
                int count = 0;

                //for (int i=0; i<nRuns; ++i){
                long t1 = System.currentTimeMillis();
                TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery);
                while (rs.hasNext()) {
                    count ++;
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
                DefaultOntopOWLStatement qst = (DefaultOntopOWLStatement) st;
                String sqlQuery = qst.getRewritingRendering(sparqlQuery);

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

            CanonicalIRIOntowisTest example = new CanonicalIRIOntowisTest(Settings.obdaFile, Settings.propertyFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void defaults(String string, String run) {

        switch(string){

            case "--POSTGRES2DSten":{
                Settings.obdaFile = ParamConst.POSTGRES2DSten;
                Settings.propertyFile = ParamConst.POSTGRES2DStenP;
                Settings.resultFileName = "POSTGRES2DSten";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;


                break;
            }

            case "--POSTGRES2DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSthirty;
                Settings.propertyFile = ParamConst.POSTGRES2DSthirtyP;
                Settings.resultFileName = "POSTGRES2DSthirty";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;

                break;
            }

            case "--POSTGRES2DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES2DSsixty;
                Settings.propertyFile = ParamConst.POSTGRES2DSsixtyP;
                Settings.resultFileName = "POSTGRES2DSsixty";
                Settings.NUM_TABLES = 3;
                Settings.NUM_OBJECTS = 3;
                Settings.NUM_DATA=3;

                break;
            }

            case "--POSTGRES3DSten":{
                Settings.obdaFile = ParamConst.POSTGRES3DSten;
                Settings.propertyFile = ParamConst.POSTGRES3DStenP;
                Settings.resultFileName = "POSTGRES3DSten";
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;

                break;
            }

            case "--POSTGRES3DSthirty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSthirty;
                Settings.propertyFile = ParamConst.POSTGRES3DSthirtyP;
                Settings.resultFileName = "POSTGRES3DSthirty";
                Settings.NUM_TABLES = 4;
                Settings.NUM_OBJECTS = 4;
                Settings.NUM_DATA=4;

                break;
            }

            case "--POSTGRES3DSsixty":{
                Settings.obdaFile = ParamConst.POSTGRES3DSsixty;
                Settings.propertyFile = ParamConst.POSTGRES3DSsixtyP;
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

            for (int i = 0; i< Settings.NUM_TABLES; i ++){

                for (int ndata = 0; ndata<= Settings.NUM_DATA; ndata++) {

                    for (int nobject = 0; nobject <= Settings.NUM_OBJECTS; nobject++) {


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


                    for (int i = 0; i< Settings.NUM_TABLES; i ++){

                        for (int ndata = 0; ndata<= Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <= Settings.NUM_OBJECTS; nobject++) {



                                filter0SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[filter]));
                                results++;

                            }

                        }
                    }
                    break;
                case 1:


                    for (int i = 0; i< Settings.NUM_TABLES; i ++){

                        for (int ndata = 0; ndata<= Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <= Settings.NUM_OBJECTS; nobject++) {



                                filter1SPARQL.add(results, QueryTemplates.classAndObjectSparqlQuery(i + 1, ndata, nobject, filters[filter]));
                                results++;

                            }

                        }
                    }
                    break;
                case 2:


                    for (int i = 0; i< Settings.NUM_TABLES; i ++){

                        for (int ndata = 0; ndata<= Settings.NUM_DATA; ndata++) {

                            for (int nobject = 0; nobject <= Settings.NUM_OBJECTS; nobject++) {



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