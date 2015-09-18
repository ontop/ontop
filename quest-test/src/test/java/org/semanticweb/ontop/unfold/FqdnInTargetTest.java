package org.semanticweb.ontop.unfold;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Tests the usage of a FQDN in the target of a mapping that will be converted in a sub-view
 *   (because of a SELECT DISTINCT).
 */
public class FqdnInTargetTest {

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/ontologyIMDB.owl";
    final String obdaFile = "src/test/resources/ontologyIMDB-fqdn.obda";


    @Before
    public void setUp() throws Exception {
        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));
    }

    private void runTests(Properties p) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFile), new QuestPreferences(p));

        QuestOWL reasoner = factory.createReasoner(ontology, new SimpleConfiguration());

        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        String query1 = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";


        try {
            int results = executeQuerySPARQL(query1, st);
            assertEquals(10, results);

        } catch (Exception e) {

            assertTrue(false);
            log.error(e.getMessage());

        } finally {

            st.close();
            conn.close();
            reasoner.dispose();
        }
    }

    public int executeQuerySPARQL(String query, QuestOWLStatement st) throws Exception {
        QuestOWLResultSet rs = st.executeTuple(query);
        int count = 0;
        while (rs.nextRow()) {

            count++;

            log.debug("result " + count + " "+ rs.getOWLObject("p"));

        }
        rs.close();

        return count;
    }



    @Test
    public void testIMDBSeries() throws Exception {

        Properties p = new Properties();
        p.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setProperty(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
        p.setProperty(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

        runTests(p);
    }

}
