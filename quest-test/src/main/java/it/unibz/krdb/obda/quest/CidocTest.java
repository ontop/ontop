package it.unibz.krdb.obda.quest;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;

public class CidocTest {


    private QuestOWL reasoner;
    private QuestOWLConnection conn;
    QuestOWLStatement st;
    private String owlFileName = "C:\\Users\\elem\\Desktop\\Arsol-Original\\cidoc_erlangen.owl";
    private String obdaFileName = "C:\\Users\\elem\\Desktop\\Arsol-Original\\cidoc_erlangen.obda";

    @Before
    public void setUp() throws Exception {
        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlFileName)));

        // Loading the OBDA data
        OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
        OBDAModel obdaModel = fac.getOBDAModel();
        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFileName);

        Properties p = new Properties();
        p.put(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.put(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
        p.put(QuestPreferences.PRINT_KEYS,QuestConstants.TRUE);
        p.put(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        factory.setOBDAController(obdaModel);
        factory.setPreferenceHolder(new QuestPreferences(p));

        //reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
        reasoner = factory.createReasoner(ontology);
        // Now we are ready for querying
        conn = reasoner.getConnection();
        st = conn.createStatement();
//        String sparqlQuery = "PREFIX : <http://erlangen-crm.org/current/>" +
//                "SELECT ?x WHERE {?x a :E25_Man-Made_Feature}";
//        String sparqlQuery = "PREFIX : <http://erlangen-crm.org/current/>"+
//                "SELECT ?x ?y WHERE {?x a :E41_Appellation; :P3_has_note ?y}";
        String sparqlQuery = "PREFIX : <http://erlangen-crm.org/current/>"+
                  "SELECT ?x ?y WHERE {?x a :E7_Activity; :P3_has_note ?y}";

        try {
            long t1 = System.currentTimeMillis();
            QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
            int columnSize = rs.getColumnCount();
            while (rs.nextRow()) {
                for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = rs.getOWLObject(idx);
                    System.out.print(binding.toString() + ", ");
                }
                System.out.print("\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

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
            System.out.println((t2-t1) + "ms");

        } finally {

			/*
			 * Close connection and resources
			 */
            if (st != null && !st.isClosed()) {
                st.close();
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            reasoner.dispose();
        }

    }

    @After
    public void tearDown() throws Exception{
        conn.close();
        reasoner.dispose();
    }


    @Test
    public void test1() throws OBDAException, OWLException {
        //String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Country} LIMIT 10";

        //QuestOWLStatement st = conn.createStatement();
    }
}
