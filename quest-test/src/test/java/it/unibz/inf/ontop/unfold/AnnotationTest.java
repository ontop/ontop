package it.unibz.inf.ontop.unfold;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
public class AnnotationTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    final String owlFile = "src/test/resources/annotation/doid.owl";
    final String obdaFile = "src/test/resources/annotation/doid.obda";

    private String runTestQuery1() throws Exception {

        // Creating a new instance of the reasoner
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .enableOntologyAnnotationQuerying(true)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);
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

        String results = runTestQuery1();
        assertEquals("<http://purl.obolibrary.org/obo/DOID_0060293>", results);
    }



}

