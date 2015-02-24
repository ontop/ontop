package org.semanticweb.ontop.owlrefplatform.owlapi3;


import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.util.Properties;


public class QuestOWLExample_ReasoningDisabled {

    interface ParamConst{
        public static final String MYSQL  = "src/main/resources/example/disableReasoning/ontowis-hierarchy-mysql.obda";
        public static final String POSTGRES = "src/main/resources/example/disableReasoning/ontowis-hierarchy-postgres.obda";
        public static final String DB2 = "src/main/resources/example/disableReasoning/ontowis-hierarchy-db2.obda";
        public static final String MYSQL_VIEW = "src/main/resources/example/disableReasoning/ontowis-5joins-int-view.obda";
        public static final String POSTGRES_VIEW = "src/main/resources/example/disableReasoning/ontowis-5joins-int-view-postgres.obda";
        public static final String DB2_VIEW = "src/main/resources/example/disableReasoning/ontowis-5joins-int-view-db2.obda";
    }

    public QuestOWLExample_ReasoningDisabled(String obdaFile){
        this.obdafile = obdaFile;
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
    final String usrConstrinFile = "src/main/resources/example/funcCons.txt";

    // Exclude from T-Mappings
    final String tMappingsConfFile = "src/main/resources/example/disableReasoning/ontowis-hierarchy-tm.conf";

    public void runQuery() throws Exception {

		/*
		 * Load the ontology from an external .owl file.
		 */
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 *
		 */
        Properties p = new Properties();
        ImplicitDBConstraints constr = new ImplicitDBConstraints(usrConstrinFile);
        p.put(QuestPreferences.DB_CONSTRAINTS, constr);

        QuestPreferences preference = new QuestPreferences(p);
//		TEST preference.setCurrentValueOf(QuestPreferences.T_MAPPINGS, QuestConstants.FALSE); // Disable T_Mappings

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), preference);

//		/*
//		 * T-Mappings Handling!!
//		 */
//        TMappingsConfParser tMapParser = new TMappingsConfParser(tMappingsConfFile);
//        factory.setExcludeFromTMappingsPredicates(tMapParser.parsePredicates());

        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
        QuestOWLConnection conn = reasoner.getConnection();


		/*
		 * Get the book information that is stored in the database
		 */

        String[] queries = new String[10];
        String[] results = new String[10];




        queries[0]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A5}  ";
        queries[1]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A4}  ";
        queries[2]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A3}  ";
        queries[3]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A2}  ";
        queries[4]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A1}  ";

        queries[5]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A5. ?x a  :B5}  ";
        queries[6]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A4. ?x a  :B4}  ";
        queries[7]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A3. ?x a  :B3}  ";
        queries[8]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A2. ?x a  :B2}  ";
        queries[9]=	"PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A1. ?x a  :B1}  ";
/*
        queries[10]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A5. FILTER (?x < 1000). }  ";
        queries[11]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A4. FILTER (?x < 1000). }  ";
        queries[12]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A3. FILTER (?x < 1000). }  ";
        queries[13]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A2. FILTER (?x < 1000). }  ";
        queries[14]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A1. FILTER (?x < 1000). }  ";

        queries[15]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A5. ?x a  :B5. FILTER (?x < 1000). }  ";
        queries[16]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A4. ?x a  :B4. FILTER (?x < 1000). }  ";
        queries[17]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A3. ?x a  :B3. FILTER (?x < 1000). }  ";
        queries[18]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A2. ?x a  :B2. FILTER (?x < 1000). }  ";
        queries[19]= "PREFIX : <http://www.example.org/> SELECT ?x   WHERE {?x a  :A1. ?x a  :B1. FILTER (?x < 1000). }  ";

*/
        /*
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
*/

        StringBuilder csvOut = new StringBuilder();


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
                    System.out.println("query results:" + count);
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

        switch(args[0]){
            case "--MYSQL":{
                obdaFile = ParamConst.MYSQL;
                break;
            }
            case "--POSTGRES":{
                obdaFile = ParamConst.POSTGRES;
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
            case "--POSTGRES-VIEW":{ //In the old jar, view is uncapitalized
                obdaFile = ParamConst.POSTGRES_VIEW;
                break;
            }
            case "--DB2-VIEW":{
                obdaFile = ParamConst.DB2_VIEW;
                break;
            }


        }
        try {
            QuestOWLExample_ReasoningDisabled example = new QuestOWLExample_ReasoningDisabled(obdaFile);
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
