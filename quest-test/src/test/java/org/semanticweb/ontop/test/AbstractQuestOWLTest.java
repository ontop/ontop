package org.semanticweb.ontop.test;

import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Abstract class for many tests
 */
public abstract class AbstractQuestOWLTest {
    private final OWLOntology ontology;
    private final String owlfile = "src/test/resources/person.owl";
    private final String obdafile = "src/test/resources/person1.obda";
    private final QuestOWL reasoner;

    protected AbstractQuestOWLTest(QuestPreferences preferences)
            throws IOException, InvalidMappingException, OWLOntologyCreationException {
        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdafile), preferences);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
        reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
    }

    protected QuestOWL getReasoner() {
        return reasoner;
    }

    protected void runTests(String query, int expectedValue) throws Exception {

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        try {

            executeQueryAssertResults(query, st, expectedValue);

        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
            }
            conn.close();
            reasoner.dispose();
        }
    }

    public void executeQueryAssertResults(String query, QuestOWLStatement st, int expectedRows) throws Exception {
        QuestOWLResultSet rs = st.executeTuple(query);
        int count = 0;
        while (rs.nextRow()) {
            count++;
            for (int i = 1; i <= rs.getColumnCount(); i++) {
                String varName = rs.getSignature().get(i-1);
                System.out.print(varName);
                //System.out.print("=" + rs.getOWLObject(i));
                System.out.print("=" + rs.getOWLObject(varName));
                System.out.print(" ");
            }
            System.out.println();
        }
        rs.close();
        assertEquals(expectedRows, count);
    }
}
