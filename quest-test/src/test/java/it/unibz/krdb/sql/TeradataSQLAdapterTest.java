package it.unibz.krdb.sql;

import it.unibz.krdb.obda.exception.InvalidMappingException;
import it.unibz.krdb.obda.exception.InvalidPredicateDeclarationException;
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
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;

public class TeradataSQLAdapterTest {
    final String owlfile = "src/test/resources/teradata/financial.owl";
    final String obdafile = "src/test/resources/teradata/financial.obda";
    QuestOWLConnection conn;
    QuestOWLStatement st;
    QuestOWL reasoner;
    QuestOWLFactory factory;
    QuestOWLConfiguration config;
    QuestPreferences preference;
    ModelIOManager ioManager;
    OBDAModel obdaModel;
    OBDADataFactory fac;
    OWLOntologyManager manager;
    OWLOntology ontology;


    public  void runQuery(){

        try {
          /* 
             * Load the ontology from an external .owl file. 
            */
            manager = OWLManager.createOWLOntologyManager();
            ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

            /* 
            * Load the OBDA model from an external .obda file 
            */
            fac = OBDADataFactoryImpl.getInstance();
            obdaModel = fac.getOBDAModel();
            ioManager = new ModelIOManager(obdaModel);
            ioManager.load(obdafile);

            /* 
            * * Prepare the configuration for the Quest instance. The example below shows the setup for 
            * * "Virtual ABox" mode 
            */
            preference = new QuestPreferences();
            preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
            preference.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

            /* 
            * Create the instance of Quest OWL reasoner. 
            */
            factory = new QuestOWLFactory();
            config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(preference).build();
            reasoner = factory.createReasoner(ontology, config);

            /* 
            * Prepare the data connection for querying. 
            */
            conn = reasoner.getConnection();
            st = conn.createStatement();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (InvalidMappingException e) {
            e.printStackTrace();
        } catch (OWLException e) {
            e.printStackTrace();
        } catch (InvalidPredicateDeclarationException e) {
            e.printStackTrace();
        }

//        String sparqlQuery =
//                "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                        "select ?x ?y where {?x :hasAccount ?y}";
            String sparqlQuery =
            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?x where {?x a :Customer" +
                    " FILTER(STRBEFORE(\"aabc\", \"bc\") > \"a\")}\n";

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

         /* 
            * Close connection and resources 
            * */
        try {
            if (st != null && !st.isClosed()) {
                st.close();
            }


        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        reasoner.dispose();
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


//    String sparqlQuery =
//                "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                        "select ?x ?y where {?x :hasAccount ?y}" +
//                        "LIMIT 20";
//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer" +
//                    "   FILTER(STRSTARTS(?x,\"A\"))}\n";

//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer" +
//                    "   FILTER(STRENDS(?x,\"6\"))}\n";

//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer" +
//                    " FILTER(STRLEN(?x) > 1)}\n";

//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer" +
//                    " FILTER(STRLEN(UCASE(?x)) > 1)}\n";

//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer." +
//                    " FILTER(CONTAINS(?x, \"6\"))}\n";

//    String sparqlQuery =
//            "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/5/financial#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "select ?x where {?x a :Customer" +
//                    " FILTER(STRBEFORE(\"aabc\", \"bc\") > \"a\")}\n";