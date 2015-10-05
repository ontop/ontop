package it.unibz.krdb.odba;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;

/**
 * Created by elem on 21/09/15.
 */
public class EPNetTest {

        /*
         * Use the sample database using H2 from
         * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
         *
         * Please use the pre-bundled H2 server from the above link
         *
         * Test with not latin Character
         *
         */
        final String owlfile = "src/test/resources/bug.owl";
        final String obdafile = "src/test/resources/bug.obda";

        @Test
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

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
            QuestOWLFactory factory = new QuestOWLFactory();
            factory.setOBDAController(obdaModel);
            factory.setPreferenceHolder(preference);
            QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
            QuestOWLConnection conn = reasoner.getConnection();
            QuestOWLStatement st = conn.createStatement();

		/*
		 * Get the book information that is stored in the database
		 */
//        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
//                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
//                "select *\n" +
//                "where {\n" +
//                "?x rdf:type :Amphora .\n" +
//                "?x :hasProductionPlace ?pl .\n" +
//                "?pl rdf:type :Place .\n" +
//                "?pl dcterms:title \"La Corregidora\" .\n" +
//                "?pl :hasLatitude ?lat .\n" +
//                "?pl :hasLongitude ?long\n" +
//                "}\n" +
//                "limit 50\n";
            String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                    "select ?x\n" +
                    "where {\n" +
                    "?x rdf:type :AmphoraSection .\n" +
                    "}\n" +
                    "limit 5\n";

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

        /**
         * Main client program
         */
        public static void main(String[] args) {
            try {
                EPNetTest example = new EPNetTest();
                example.runQuery();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}
