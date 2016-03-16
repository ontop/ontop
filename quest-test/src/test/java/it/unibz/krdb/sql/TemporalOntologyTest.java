package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;

public class TemporalOntologyTest {
    final String owlfile = "src/test/resources/temporalOBDA/mesowest.owl";
    final String obdafile = "src/test/resources/temporalOBDA/mesowest.obda";

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
        OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdafile);

        QuestPreferences preference = new QuestPreferences();
        preference.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

		/*
         * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(preference).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);



        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/elem/ontologies/2016/2/untitled-ontology-27#> \n" +
                " SELECT ?hurr \n" +
                " WHERE { ?hurr a :Hurricane }";

        try (/*
              * Prepare the data connection for querying.
		 	 */
             QuestOWLConnection conn = reasoner.getConnection();
             QuestOWLStatement st = conn.createStatement()){

            long t1 = System.currentTimeMillis();
            QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
            int columnSize = rs.getColumnCount();
            while (rs.nextRow()) {
                for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = rs.getOWLObject(idx);
                    System.out.print(ToStringRenderer.getInstance().getRendering(binding) + ", ");
                }
                System.out.print("\n");
            }
            rs.close();
            long t2 = System.currentTimeMillis();

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
            System.out.println((t2 - t1) + "ms");

        } finally {
            reasoner.dispose();
        }

        System.out.print("something");
    }


    public static void main(String[] args) {
        try {
            TemporalOntologyTest example = new TemporalOntologyTest();
            example.runQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}