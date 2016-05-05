package it.unibz.krdb.obda.unfold;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class AnnotationTest {

    private OBDADataFactory fac;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlFile = "src/test/resources/annotation/doid.owl";
    final String obdaFile = "src/test/resources/annotation/doid.obda";

    @Before
    public void setUp() throws Exception {

        fac = OBDADataFactoryImpl.getInstance();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlFile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();

        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdaFile);

    }

    private String runTestQuery1(QuestPreferences p) throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).preferences(p).queryingAnnotationsInOntology(true).build();
        QuestOWL reasoner = factory.createReasoner(ontology, config);
        // Now we are ready for querying
        QuestOWLConnection conn = reasoner.getConnection();
        QuestOWLStatement st = conn.createStatement();

        String query = Joiner.on("\n").join(
                CharStreams.readLines(new FileReader("src/test/resources/annotation/q1.q")));

        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        long start = System.nanoTime();
        QuestOWLResultSet res = st.executeTuple(query);
        long end = System.nanoTime();

        double time = (end - start) / 1000;
        String result = "";
        int count = 0;
        while (res.nextRow()) {
            count += 1;
            if (count == 1) {
                for (int i = 1; i <= res.getColumnCount(); i++) {
                    log.debug("Example result " + res.getSignature().get(i - 1) + " = " + res.getOWLObject(i));

                }
                result = ToStringRenderer.getInstance().getRendering(res.getOWLObject("x"));
            }
        }
        log.debug("Total results: {}", count);

        assertFalse(count == 0);

        log.debug("Elapsed time: {} ms", time);

        st.close();
        conn.close();
        reasoner.dispose();

        return result;
    }



    @Test
    public void testAnnotationInOntology() throws Exception {

        QuestPreferences p = new QuestPreferences();

        String results = runTestQuery1(p);
        assertEquals("<http://purl.obolibrary.org/obo/DOID_0060293>", results);
    }



}

