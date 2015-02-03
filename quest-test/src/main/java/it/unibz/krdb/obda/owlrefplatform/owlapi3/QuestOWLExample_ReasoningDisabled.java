package it.unibz.krdb.obda.owlrefplatform.owlapi3;



import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.sql.ImplicitDBConstraints;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class QuestOWLExample_ReasoningDisabled {


    interface ParamConst{
        public static final String MYSQL_OBDA_FILE  = "src/main/resources/example/disableReasoning/ontowis-hierarchy-mysql.obda";
        public static final String POSTGRES_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-postgres.obda";
        public static final String DB2_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-db2.obda";

        public static final String MYSQL_SMALL_OBDA_FILE  = "src/main/resources/example/disableReasoning/ontowis-hierarchy-mysql.obda";
        public static final String POSTGRES_SMALL_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-postgres.obda";
        public static final String DB2_SMALL_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-db2.obda";


        static final String[] tMappingConfFiles = {
                "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_1.conf",
                "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_2.conf",
                "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_3.conf",
                "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_4.conf"
        };
    }

    enum DbType {
        MYSQL, POSTGRES, SMALL_POSTGRES, SMALL_MYSQL
    }

    public static class Settings {
        static String obdaFile;
        static DbType type;
        static String tmapConfigFile;
    }

    static class QueryFactory {
        static private int[] getFilters(DbType type) {
            int[] filters = new int[6];

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
            }
            return filters;
        }

        static String prefix = "PREFIX : <http://www.example.org/> \n";

        static List<String> createSPARQLQueries(DbType dbType){

            List<String> sparqls = new ArrayList<>();

            sparqls.addAll(createSPARQLs_one_concepts(dbType));
            sparqls.addAll(createSPARQLs_two_concepts(dbType));
            sparqls.addAll(createSPARQLs_three_concepts(dbType));

            return sparqls;
        }

        static public List<String> createSPARQLs_three_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            for(int i1 = 1; i1 <= 4; i1++) {
                for (int i2 = 1; i2 <= 4; i2++) {
                    for (int i3 = 1; i3 <= 4; i3++) {
                        for (int filter : getFilters(dbType)) {
                            String sparql = String.format(
                                    "PREFIX : <http://www.example.org/> " +
                                            "SELECT DISTINCT ?x ?y ?z " +
                                            " WHERE {" +
                                            "?x a :A%d . ?x :R ?y . " +
                                            "?y a :A%d . ?y :R ?z . " +
                                            "?z a :A%d . " +
                                            "Filter (?x < %d) }",
                                    i1, i2, i3, filter);
                            sparqls.add(sparql);
                        }
                    }
                }
            }
            return sparqls;
        }

        static public List<String> createSPARQLs_two_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            for(int i1 = 1; i1 <= 4; i1++) {
                for (int i2 = 1; i2 <= 4; i2++) {
                        for (int filter : getFilters(dbType)) {
                            String sparql = String.format(
                                    "PREFIX : <http://www.example.org/> " +
                                            "SELECT DISTINCT ?x ?y    " +
                                            " WHERE {" +
                                            "?x a :A%d. ?x :R ?y . " +
                                            "?y a :A%d .  " +
                                            "Filter (?x < %d) }",
                                    i1, i2, filter);
                            sparqls.add(sparql);
                        }

                }
            }
            return sparqls;
        }

        static public List<String> createSPARQLs_one_concepts(DbType dbType){
            List<String> sparqls = new ArrayList<>();

            for(int i1 = 1; i1 <= 4; i1++) {
                    for (int filter : getFilters(dbType)) {
                        String sparql = String.format(
                                "PREFIX : <http://www.example.org/> " +
                                        " SELECT  DISTINCT ?x       " +
                                        " WHERE {" +
                                        "?x a :A%d. " +
                                        "Filter (?x < %d) }",
                                i1, filter);
                        sparqls.add(sparql);


                }
            }
            return sparqls;
        }
    }

    /**
     * @param resultsOne
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     */
    private void generateFile(List<String> resultsOne, List<String> resultsTwo, List<String> resultsThree) throws FileNotFoundException, UnsupportedEncodingException {
        /*
		 * Generate File !
		 */
        PrintWriter writer = new PrintWriter("src/main/resources/example/table.txt", "UTF-8");
        PrintWriter writerG = new PrintWriter("src/main/resources/example/graph.txt", "UTF-8");

        int j = 0;
        while (j < 24) {
            writer.println(resultsOne.get(j) + " & " + resultsTwo.get(j) + " & " + resultsThree.get(j));

            if (j <= 5) {
                String gline = "(1," + resultsOne.get(j) + ")" + "(2," + resultsTwo.get(j) + ")"
                        + "(3," + resultsThree.get(j) + ")" + "(4," + resultsOne.get(j + 6) + ")"
                        + "(5," + resultsTwo.get(j + 6) + ")" + "(6," + resultsThree.get(j + 6) + ")"
                        + "(7," + resultsOne.get(j + 12) + ")" + "(8," + resultsTwo.get(j + 12) + ")"
                        + "(9," + resultsThree.get(j + 12) + ")" + "(10," + resultsOne.get(j + 18) + ")"
                        + "(11," + resultsTwo.get(j + 18) + ")" + "(12," + resultsThree.get(j + 18) + ")";
                writerG.println(gline);
            }
            j++;
        }
        writer.close();
        writerG.close();
    }

    /**
     * @param conn
     * @throws org.semanticweb.owlapi.model.OWLException
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
     * @throws it.unibz.krdb.obda.model.OBDAException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     * @throws it.unibz.krdb.obda.exception.InvalidMappingException
     * @throws it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException
     * @throws java.io.IOException
     * @throws OWLException
     */
    private QuestOWLConnection createStuff() throws OBDAException, OWLOntologyCreationException, IOException, InvalidPredicateDeclarationException, InvalidMappingException {

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



        TMappingExclusionConfig config = TMappingExclusionConfig.parseFile(Settings.tmapConfigFile);
        factory.setExcludeFromTMappingsPredicates(config);


        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

        this.reasoner = reasoner;
		/*
		 * Prepare the data connection for querying.
		 */
        QuestOWLConnection conn = reasoner.getConnection();

        return conn;
    }

    private QuestOWL reasoner;


    public QuestOWLExample_ReasoningDisabled(DbType dbType, String obdaFile, String tMappingsConfFile){
        this.obdafile = obdaFile;
        this.dbType = dbType;
        this.tMappingsConfFile = tMappingsConfFile;
    }

    /*
     * Use the sample database using H2 from
     * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
     *
     * Please use the pre-bundled H2 server from the above link
     *
     */
    final String owlfile = "src/main/resources/example/disableReasoning/ontowis-hierarchy.owl";
    //final String obdafile = "src/main/resources/example/ontowis-5joins-int-view.obda";
    final String obdafile;// = "src/main/resources/example/ontowis-5joins-int-view.obda";

    private final DbType dbType;

    // Exclude from T-Mappings
    String tMappingsConfFile;

    public void runQuery() throws Exception {

        //	queries[30]="PREFIX :	<http://www.example.org/>  SELECT ?x   WHERE {?x a  :4Tab1 .   } LIMIT 100000  ";

        QuestOWLConnection conn =  createStuff();

        // Results
        List<String> resultsOne = new ArrayList<>();
        List<String> resultsTwo = new ArrayList<>();
        List<String> resultsThree = new ArrayList<>();


        // Run the tests on the queries
        runQueries(conn, QueryFactory.createSPARQLs_one_concepts(dbType), resultsOne);
        runQueries(conn, QueryFactory.createSPARQLs_two_concepts(dbType), resultsTwo);
        runQueries(conn, QueryFactory.createSPARQLs_three_concepts(dbType), resultsTwo);

        closeEverything(conn);
        generateFile(resultsOne, resultsTwo, resultsThree);

    }

    private void runQueries(QuestOWLConnection conn,
                             List<String> queries, List<String> results) throws OWLException {
        int j=0;
        //while (j < queries.length){

        for(String sparqlQuery:queries){

            //String sparqlQuery = queries[j];
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

                for (int i = 0; i < 3; i++){
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
                QuestOWLStatement qst = st;
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

                //results[j] = (time/3)+"" ;
                results.set(j, (time/3) + "");

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


    public void runQuery0() throws Exception {

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
		 * T-Mappings Handling!!
		 */
        factory.setExcludeFromTMappingsPredicates(TMappingExclusionConfig.parseFile(tMappingsConfFile));

        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
        QuestOWLConnection conn = reasoner.getConnection();

		/*
		 * Get the book information that is stored in the database
		 */

        //String[] results = new String[20];

        List<String> queries = QueryFactory.createSPARQLQueries(dbType);
        List<String> results = new ArrayList<>(queries.size());

        StringBuilder csvOut = new StringBuilder();

        int j = 0;
        for (String sparqlQuery : queries) {
            //    String sparqlQuery = queries[j];
            QuestOWLStatement st = conn.createStatement();
            try {
                long time = 0;
                int count = 0;

                for (int i = 0; i < 4; i++) {
                    long t1 = System.currentTimeMillis();
                    QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
                    int columnSize = rs.getColumnCount();
                    count = 0;
                    while (rs.nextRow()) {
                        count++;
                        for (int idx = 1; idx <= columnSize; idx++) {
                            OWLObject binding = rs.getOWLObject(idx);
                            //System.out.print(binding.toString() + ", ");
                        }
                        //System.out.print("\n");
                    }
                    long t2 = System.currentTimeMillis();
                    time = time + (t2 - t1);
                    System.out.println("partial time:" + time);
                    System.out.println("query results:" + count);
                    rs.close();
                }

	 			/*
                 * Print the query summary
				 */
                QuestOWLStatement qst = st;
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
                System.out.println((time / 4) + "ms");

                //results.set(j, (time / 4) + "ms");
                results.add(j, (time / 4) + "ms");

                System.out.println("The number of results:");
                System.out.println("=====================");
                System.out.println(count);

            } finally {
                if (st != null && !st.isClosed()) {
                    st.close();
                }
            }
            j += 1;
            //break;
        }

		/*
		 * Close connection and resources
		 */

        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        reasoner.dispose();
    }

    /**
     * Main client program
     */
    public static void main(String[] args) {

        String obdaFile = null;
        String arg;
        if(args.length > 0){
            arg = args[0];
        } else {
            arg = "--MYSQL-SMALL";
        }

        String tMappingConfFile = ParamConst.tMappingConfFiles[Integer.parseInt(args[1])];


        defaults(arg);

        try {
            QuestOWLExample_ReasoningDisabled example = new QuestOWLExample_ReasoningDisabled(
                    DbType.MYSQL, obdaFile, tMappingConfFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void defaults(String string) {

        switch(string){
            case "--MYSQL-SMALL":{
                Settings.obdaFile = ParamConst.MYSQL_SMALL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
                break;
            }
            case "--MYSQL":{
                Settings.obdaFile = ParamConst.MYSQL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
                break;
            }
            case "--POSTGRES-SMALL":{
                Settings.obdaFile = ParamConst.MYSQL_SMALL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
                break;
            }
            case "--POSTGRES":{
                Settings.obdaFile = ParamConst.MYSQL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
                break;
            }
            case "--DB2-SMALL":{
                Settings.obdaFile = ParamConst.MYSQL_SMALL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
                break;
            }
            case "--DB2":{
                Settings.obdaFile = ParamConst.MYSQL_OBDA_FILE;
                Settings.type = DbType.MYSQL;
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

}
