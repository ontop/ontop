package it.unibz.krdb.sql;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
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

import java.io.File;

public class TeradataSQLAdapterTest {
    final String owlfile = "src/test/resources/teradata/financial.owl";
    final String obdafile = "src/test/resources/teradata/financial.obda";
    QuestOWLConnection conn;
    QuestOWLStatement st;
    QuestOWL reasoner;


    @Before
    public void setUp() throws Exception {

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
            * * Prepare the configuration for the Quest instance. The example below shows the setup for 
            * * "Virtual ABox" mode 
            */
        QuestPreferences preference = new QuestPreferences();
        preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

            /* 
            * Create the instance of Quest OWL reasoner. 
            */
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(preference).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

            /* 
            * Prepare the data connection for querying. 
            */
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();


    }

    @After
    public void tearDown() throws Exception{
        /* 
            * Close connection and resources 
            * */
        if (st != null && !st.isClosed()) {
            st.close();
        }

        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        reasoner.dispose();

    }

    public  void runQuery(){

        String sparqlQuery =
                "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "select ?x ?y where {?x :hasAccount ?y}";
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

        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void test() throws Exception {
        try {
            TeradataSQLAdapterTest example = new TeradataSQLAdapterTest();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
