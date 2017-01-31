package it.unibz.inf.ontop.obda;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;


/**
 * Created by elem on 21/09/15.
 */
public class MetaMappingExpanderTest {

        /*
         * Use the sample database using H2 from
         * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
         *
         * Please use the pre-bundled H2 server from the above link
         *
         * Test with not latin Character
         *
         */
        final String owlfile = "src/test/resources/EPNet.owl";
        final String obdafile = "src/test/resources/EPNet.obda";

        @Test
        public void runQuery() throws Exception {

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
            QuestOWLFactory factory = new QuestOWLFactory();
            QuestConfiguration config = QuestConfiguration.defaultBuilder()
                    .ontologyFile(owlfile)
                    .nativeOntopMappingFile(obdafile)
                    .build();
            QuestOWL reasoner = factory.createReasoner(config);
		/*
		 * Prepare the data connection for querying.
		 */
            OntopOWLConnection conn = reasoner.getConnection();
            OntopOWLStatement st = conn.createStatement();

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
                    "?x rdf:type :AmphoraSection2026 .\n" +
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
}
