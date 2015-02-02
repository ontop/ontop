package it.unibz.krdb.obda.owlrefplatform.owlapi3;



import it.unibz.krdb.obda.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class QuestOWLExample_ReasoningDisabled {


    static class QueryFactory{
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


    interface ParamConst{
        public static final String MYSQL_OBDA_FILE  = "src/main/resources/example/disableReasoning/ontowis-hierarchy-mysql.obda";
        public static final String POSTGRES_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-postgres.obda";
        public static final String DB2_OBDA_FILE = "src/main/resources/example/disableReasoning/ontowis-hierarchy-db2.obda";
    }



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

    static final String[] tMappingsConfFiles = {
            "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_0.conf",
            "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_1.conf",
            "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_2.conf",
            "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_3.conf",
            "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm_4.conf"

    } ;


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



		/*
		 * Printing results !
		 */


//        while (j<queries.length){
//            System.out.println( results[j]);
//            j++;
//        }

//		String sparqlQuery =
//				//"PREFIX :	<http://www.example.org/> \n" +
////						"SELECT ?x ?y  WHERE {?x a  :1Tab1 . ?x :Tab2unique2Tab2 ?y .}  " ;
//					"PREFIX :	<http://www.example.org/> SELECT ?x ?y  WHERE {?x a  :2Tab1 . ?x :Tab3unique2Tab3 ?y .}    " ;


//		FileWriter statsWriter = new FileWriter(statsFile);
//		statsWriter.write(Statistics.printStats());
//		statsWriter.flush();
//		statsWriter.close();

    }

    /**
     * Main client program
     */
    public static void main(String[] args) {

        String obdaFile = null;

        if(args.length == 0){
            obdaFile = ParamConst.MYSQL_OBDA_FILE;
        } else {

            switch (args[0]) {
                case "--MYSQL": {
                    obdaFile = ParamConst.MYSQL_OBDA_FILE;
                    break;
                }
                case "--POSTGRES": {
                    obdaFile = ParamConst.POSTGRES_OBDA_FILE;
                    break;
                }
                case "--DB2": {
                    obdaFile = ParamConst.DB2_OBDA_FILE;
                    break;
                }
            }

        }

        try {
            QuestOWLExample_ReasoningDisabled example = new QuestOWLExample_ReasoningDisabled(
                    DbType.MYSQL, obdaFile, tMappingsConfFiles[0]);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args){
//        for (String s : QueryFactory.createSPARQLs_one_concepts(DbType.MYSQL)) {
//            System.out.println(s);
//        }
//
//        for (String s : QueryFactory.createSPARQLs_two_concepts(DbType.MYSQL)) {
//            System.out.println(s);
//        }
//
//
//        for (String s : QueryFactory.createSPARQLs_three_concepts(DbType.MYSQL)) {
//            System.out.println(s);
//        }
//    }

}
