package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
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

		/*
         * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);

        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

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