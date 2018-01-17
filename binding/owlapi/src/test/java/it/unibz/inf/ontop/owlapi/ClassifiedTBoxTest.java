package it.unibz.inf.ontop.owlapi;


import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** Check failing ClassifiedTBox when equalities with top is present
 *
 */

public class ClassifiedTBoxTest {


    @Test
    public void test_wrong_property() throws Exception {
        ClassifiedTBox ontology = OWL2QLTranslatorTest.loadOntologyFromFileAndClassify("src/test/resources/ontology/venice.owl");
    }

}
